#!/usr/bin/env node
/**
 * 视频插入工具
 *
 * 把视频复制到 frontend/public/videos/，并在 frontend/src/config/videoRegistry.json
 * 的对应槽位写入【相对项目根目录】的路径（禁止绝对路径）。
 *
 * 用法：
 *   node scripts/05-insert-video.mjs --file <视频路径> --slot <槽位ID> [--label 名称] [--rename|--overwrite]
 *   node scripts/05-insert-video.mjs --remove <槽位ID> [--delete-file]
 *   node scripts/05-insert-video.mjs --list
 */

import { copyFileSync, existsSync, mkdirSync, readFileSync, statSync, unlinkSync, writeFileSync } from 'node:fs'
import { basename, dirname, extname, isAbsolute, join, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const SCRIPT_DIR = dirname(fileURLToPath(import.meta.url))
/** 项目根目录（以 scripts/ 的上一级为准，与仓库根一致） */
const PROJECT_ROOT = resolve(SCRIPT_DIR, '..')

const VIDEO_DIR_REL = 'frontend/public/videos'
const REGISTRY_REL = 'frontend/src/config/videoRegistry.json'
const VIDEO_DIR = join(PROJECT_ROOT, VIDEO_DIR_REL)
const REGISTRY_FILE = join(PROJECT_ROOT, REGISTRY_REL)

const ALLOWED_EXT = ['.mp4', '.webm', '.ogg', '.m4v']
const SLOT_ID_RE = /^[A-Za-z0-9_-]+$/
const ABSOLUTE_PATH_RE = /^([a-zA-Z]:[\\/]|\\\\|\/)/

const FALLBACK_REGISTRY = {
  publicDir: 'frontend/public',
  slots: {},
}

function fail(message, hint) {
  console.error(`\n[失败] ${message}`)
  if (hint) console.error(`        ${hint}`)
  process.exitCode = 1
  return false
}

function info(message) {
  console.log(`[信息] ${message}`)
}

function isInside(parent, child) {
  const rel = relative(parent, child)
  return rel !== '' && !rel.startsWith('..') && !isAbsolute(rel)
}

/** 解析命令行参数，支持 --key value 与 --flag 两种形式 */
function parseArgs(argv) {
  const args = { _: [] }
  for (let i = 0; i < argv.length; i += 1) {
    const token = argv[i]
    if (!token.startsWith('--')) {
      args._.push(token)
      continue
    }
    const key = token.slice(2)
    const next = argv[i + 1]
    if (!next || next.startsWith('--')) {
      args[key] = true
      continue
    }
    args[key] = next
    i += 1
  }
  return args
}

function printUsage() {
  console.log(`
视频插入工具

  node scripts/05-insert-video.mjs --file <视频路径> --slot <槽位ID> [选项]
  node scripts/05-insert-video.mjs --remove <槽位ID> [--delete-file]
  node scripts/05-insert-video.mjs --list

参数：
  --file <path>      要插入的视频文件（支持 .mp4/.webm/.ogg/.m4v）
  --slot <id>        目标槽位 ID，如 monitor-1、monitor-2
  --label <名称>     槽位显示名称（可选，默认沿用已有值或槽位 ID）
  --rename           目标目录已存在同名文件时，自动重命名为 xxx-2、xxx-3
  --overwrite        目标目录已存在同名文件时，直接覆盖
  --remove <id>      清空指定槽位的视频引用
  --delete-file      与 --remove 搭配，同时删除 videos 目录下的该文件
  --list             列出当前已登记的视频引用

示例：
  node scripts/05-insert-video.mjs --file D:/demo/fall.mp4 --slot monitor-1
  node scripts/05-insert-video.mjs --file ./fall.mp4 --slot monitor-2 --rename
`)
}

/** 读取引用记录；文件不存在时返回默认结构，JSON 损坏时抛错（避免写坏原文件） */
function readRegistry() {
  if (!existsSync(REGISTRY_FILE)) return { registry: structuredClone(FALLBACK_REGISTRY), created: true }

  const raw = readFileSync(REGISTRY_FILE, 'utf-8')
  try {
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') throw new Error('内容不是对象')
    if (!parsed.slots || typeof parsed.slots !== 'object') parsed.slots = {}
    if (!parsed.publicDir) parsed.publicDir = 'frontend/public'
    return { registry: parsed, created: false }
  } catch (error) {
    throw new Error(`${REGISTRY_REL} 解析失败（${error.message}），为保证配置不被破坏，本次未做任何修改`)
  }
}

function writeRegistry(registry) {
  writeFileSync(REGISTRY_FILE, `${JSON.stringify(registry, null, 2)}\n`, 'utf-8')
}

/** 确保目标目录存在；不存在则创建，存在但不是目录则报错 */
function ensureVideoDir() {
  if (existsSync(VIDEO_DIR)) {
    if (!statSync(VIDEO_DIR).isDirectory()) {
      return fail(`目标路径不是文件夹：${VIDEO_DIR_REL}`, '请检查该路径是否被同名文件占用')
    }
    return true
  }

  try {
    mkdirSync(VIDEO_DIR, { recursive: true })
    info(`目标目录不存在，已自动创建：${VIDEO_DIR_REL}`)
    return true
  } catch (error) {
    return fail(`目标目录创建失败：${VIDEO_DIR_REL}`, error.message)
  }
}

/** 目标目录同名文件处理：返回最终文件名，冲突且未指定策略时返回 null */
function resolveTargetName(sourceName, { rename, overwrite }) {
  const ext = extname(sourceName)
  const stem = basename(sourceName, ext)
  let candidate = sourceName
  let index = 1

  while (existsSync(join(VIDEO_DIR, candidate))) {
    if (overwrite) {
      info(`目标目录已存在同名文件，按 --overwrite 覆盖：${candidate}`)
      return candidate
    }
    if (!rename) return null
    index += 1
    candidate = `${stem}-${index}${ext}`
  }

  return candidate
}

function runList() {
  const { registry } = readRegistry()
  const slotIds = Object.keys(registry.slots)
  if (slotIds.length === 0) {
    info('当前没有任何视频槽位')
    return true
  }

  console.log('\n槽位 ID        文件                                 状态')
  console.log('─'.repeat(78))
  for (const slotId of slotIds) {
    const slot = registry.slots[slotId] ?? {}
    const configured = (slot.path ?? '').trim()
    if (!configured) {
      console.log(`${slotId.padEnd(14)} ${'-'.padEnd(36)} 未配置`)
      continue
    }
    const absolute = resolve(PROJECT_ROOT, configured)
    const exists = existsSync(absolute)
    console.log(
      `${slotId.padEnd(14)} ${configured.replace('frontend/public/', '').padEnd(36)} ${
        exists ? '正常' : '文件缺失'
      }`,
    )
  }
  console.log('')
  return true
}

function runInsert(args) {
  const slotId = String(args.slot ?? '').trim()
  const fileArg = String(args.file ?? '').trim()

  if (!fileArg) return fail('缺少 --file 参数', '示例：--file D:/demo/fall.mp4 --slot monitor-1')
  if (!slotId) return fail('缺少 --slot 参数', '示例：--slot monitor-1')
  if (!SLOT_ID_RE.test(slotId)) {
    return fail(`槽位 ID 不合法：${slotId}`, '仅允许字母、数字、下划线与连字符')
  }

  // 1) 源文件校验
  const sourcePath = resolve(fileArg)
  if (!existsSync(sourcePath)) return fail(`源视频不存在：${sourcePath}`)
  if (!statSync(sourcePath).isFile()) return fail(`源路径不是文件：${sourcePath}`)

  const ext = extname(sourcePath).toLowerCase()
  if (!ALLOWED_EXT.includes(ext)) {
    return fail(`不支持的视频格式：${ext || '(无扩展名)'}`, `支持：${ALLOWED_EXT.join(' ')}`)
  }

  // 2) 目标目录
  if (!ensureVideoDir()) return false

  // 3) 同名处理
  const targetName = resolveTargetName(basename(sourcePath), {
    rename: Boolean(args.rename),
    overwrite: Boolean(args.overwrite),
  })

  if (!targetName) {
    return fail(
      `目标目录已存在同名文件：${VIDEO_DIR_REL}/${basename(sourcePath)}`,
      '可改用 --rename 自动编号，或 --overwrite 覆盖，或先用 --file 指定重命名后的文件',
    )
  }

  // 4) 复制并校验
  const targetPath = join(VIDEO_DIR, targetName)
  try {
    copyFileSync(sourcePath, targetPath)
  } catch (error) {
    return fail(`复制失败：${relative(PROJECT_ROOT, targetPath)}`, error.message)
  }

  const sourceSize = statSync(sourcePath).size
  const targetSize = existsSync(targetPath) ? statSync(targetPath).size : -1
  if (targetSize !== sourceSize) {
    try {
      unlinkSync(targetPath)
    } catch {
      /* 清理失败不阻断报错 */
    }
    return fail('复制后文件大小不一致，已回滚', `源 ${sourceSize} 字节 / 目标 ${targetSize} 字节`)
  }

  // 5) 写入引用记录（相对项目根目录）
  const relativePath = `${VIDEO_DIR_REL}/${targetName}`
  let registry
  try {
    const result = readRegistry()
    registry = result.registry
    if (result.created) info(`引用记录不存在，将新建：${REGISTRY_REL}`)
  } catch (error) {
    try {
      unlinkSync(targetPath)
    } catch {
      /* ignore */
    }
    return fail(error.message, '已回滚复制的视频文件')
  }

  const previous = registry.slots[slotId] ?? {}
  registry.slots[slotId] = {
    label: String(args.label ?? previous.label ?? slotId),
    path: relativePath,
    updatedAt: new Date().toISOString(),
  }

  try {
    mkdirSync(dirname(REGISTRY_FILE), { recursive: true })
    writeRegistry(registry)
  } catch (error) {
    return fail(`写入引用记录失败：${REGISTRY_REL}`, error.message)
  }

  // 6) 回读校验：路径必须是相对路径，且能被正确解析到实际文件
  const check = readRegistry().registry.slots[slotId]
  const urlPath = `/${relativePath.replace(/^frontend\/public\//, '')}`
  const problems = []

  if (ABSOLUTE_PATH_RE.test(check.path)) problems.push('记录中的路径为绝对路径')
  if (!check.path.startsWith('frontend/public/')) problems.push('记录中的路径不在 frontend/public 下')
  if (!existsSync(resolve(PROJECT_ROOT, check.path))) problems.push('按记录路径找不到文件')

  console.log('\n──────────── 插入结果 ────────────')
  console.log(`  槽位      : ${slotId}`)
  console.log(`  文件      : ${targetName}`)
  console.log(`  相对路径  : ${check.path}`)
  console.log(`  访问地址  : ${encodeURI(urlPath)}`)
  console.log(`  引用记录  : ${REGISTRY_REL}`)
  console.log('──────────────────────────────────')

  if (problems.length > 0) {
    return fail('路径校验未通过，请检查引用记录', problems.join('；'))
  }

  info('路径解析校验通过，前端可直接加载该视频')
  return true
}

function runRemove(args) {
  const slotId = String(args.remove ?? '').trim()
  if (!slotId) return fail('缺少 --remove 参数', '示例：--remove monitor-1')

  let registry
  try {
    registry = readRegistry().registry
  } catch (error) {
    return fail(error.message)
  }

  const slot = registry.slots[slotId]
  if (!slot) return fail(`引用记录中不存在槽位：${slotId}`, '可用 --list 查看当前槽位')

  const oldPath = (slot.path ?? '').trim()

  slot.path = ''
  slot.updatedAt = new Date().toISOString()

  try {
    writeRegistry(registry)
  } catch (error) {
    return fail(`写入引用记录失败：${REGISTRY_REL}`, error.message)
  }
  info(`已清空槽位 ${slotId} 的视频引用`)

  if (args['delete-file'] && oldPath) {
    const absolute = resolve(PROJECT_ROOT, oldPath)
    if (!isInside(VIDEO_DIR, absolute)) {
      return fail(`拒绝删除，该文件不在 ${VIDEO_DIR_REL} 内：${oldPath}`)
    }
    if (existsSync(absolute)) {
      try {
        unlinkSync(absolute)
        info(`已删除文件：${oldPath}`)
      } catch (error) {
        return fail(`删除文件失败：${oldPath}`, error.message)
      }
    } else {
      info(`文件本就不存在，无需删除：${oldPath}`)
    }
  } else if (oldPath) {
    info(`视频文件仍保留在 ${oldPath}，如需一并删除请加 --delete-file`)
  }

  return true
}

function main() {
  const args = parseArgs(process.argv.slice(2))

  if (args.help || args.h) {
    printUsage()
    return
  }
  if (args.list) {
    runList()
    return
  }
  if (args.remove) {
    runRemove(args)
    return
  }
  runInsert(args)
}

main()
