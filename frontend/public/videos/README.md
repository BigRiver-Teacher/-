# 演示视频目录（frontend/public/videos）

前端「实时监控」页面的视频来源都放在这里。`public/` 是 Vite 的静态资源根，
本目录下的文件在开发与构建后都会被映射到站点根路径 `/videos/<文件名>`。

## 放置规则

1. 视频文件直接放入本目录（不要建子目录，便于路径推算）；
2. 支持的格式：`.mp4` `.webm` `.ogg` `.m4v`（推荐 H.264 + AAC 的 mp4，浏览器兼容性最好）；
3. 单个文件建议不超过 50MB，避免拖慢仓库克隆与构建产物体积；
4. 文件名建议使用英文小写 + 连字符，避免空格与中文（中文可用，但需要 URL 编码）。

## 登记方式（二选一）

**方式 A：使用插入脚本（推荐，自动处理重名与配置）**

```bash
node scripts/05-insert-video.mjs --file D:/path/to/demo.mp4 --slot monitor-1
```

脚本会复制文件到本目录，并把相对项目根目录的路径写入
`frontend/src/config/videoRegistry.json` 的对应槽位。

**方式 B：手工登记**

1. 把视频复制到本目录，例如 `frontend/public/videos/monitor-1.mp4`；
2. 编辑 `frontend/src/config/videoRegistry.json`，把该槽位的 `path` 写成
   `frontend/public/videos/monitor-1.mp4`（相对项目根目录，不用绝对路径）。

## 路径解析规则

| 配置值 | 解析结果 | 说明 |
| --- | --- | --- |
| `frontend/public/videos/monitor-1.mp4` | `/videos/monitor-1.mp4` | 正确，去掉 `frontend/public` 前缀即为站点路径 |
| `D:\demo\monitor-1.mp4` | 拒绝 | 绝对路径不被允许，页面显示「视频引用失败」 |
| `src/video/monitor-1.mp4` | 拒绝 | 不在 `frontend/public` 下，构建后不会被复制 |
| `frontend/public/videos/missing.mp4` | 404 → 引用失败 | 文件不存在，页面显示「视频引用失败」 |
| `""`（空字符串） | 不播放 | 页面显示「未配置视频」占位 |

启动开发服务器或构建时，`vite.config.ts` 中的 `video-registry-guard` 插件会检查
每个已登记路径是否存在，缺失时在终端给出明确警告，便于提前发现问题。
