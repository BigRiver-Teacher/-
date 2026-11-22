<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import router from '../router/index'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register, sendRegisterCode, sendResetCode, resetPassword } from '@/api/auth'

/* ================================================================
   一、登录 / 注册状态
   ================================================================ */
const username = ref('')
const password = ref('')
const verifyCode = ref('')
const remember = ref(true)
const isLoading = ref(false)
/** 1 = 登录面板，0 = 注册面板 */
const turn = ref(1)

function turnRegister() {
  turn.value = 0
  refreshCaptcha()
}
function turnLogin() {
  turn.value = 1
}

/* ================================================================
   二、图形验证码（前端展示用，与既有逻辑保持一致）
   ================================================================ */
const captchaChars = ref<string[]>([])
const CAPTCHA_POOL = '34679ACEFHKLMNPQRTUVWXY'

function refreshCaptcha() {
  const chars: string[] = []
  for (let i = 0; i < 4; i++) {
    chars.push(CAPTCHA_POOL[Math.floor(Math.random() * CAPTCHA_POOL.length)])
  }
  captchaChars.value = chars
}

/* ================================================================
   三、忘记密码弹框
   ================================================================ */
const forgotDialogVisible = ref(false)
const forgotForm = reactive({
  email: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
})
const forgotLoading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const forgotFormRef = ref<any>(null)

function openForgotDialog() {
  forgotDialogVisible.value = true
  Object.assign(forgotForm, {
    email: '',
    verifyCode: '',
    newPassword: '',
    confirmPassword: '',
  })
  forgotFormRef.value?.resetFields()
}

function closeForgotDialog() {
  forgotDialogVisible.value = false
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  countdown.value = 0
}

async function sendVerifyCode() {
  if (!forgotForm.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(forgotForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  if (countdown.value > 0) return

  try {
    await sendResetCode(forgotForm.email)
    ElMessage.success('验证码已发送到您的邮箱')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败，请稍后重试')
  }
}

async function submitForgotPassword() {
  const valid = await forgotFormRef.value?.validate().catch(() => false)
  if (!valid) return

  forgotLoading.value = true
  try {
    await resetPassword({
      email: forgotForm.email,
      verifyCode: forgotForm.verifyCode,
      newPassword: forgotForm.newPassword,
    })
    ElMessage.success('密码重置成功！请使用新密码登录')
    closeForgotDialog()
  } catch {
    ElMessage.error('密码重置失败，请稍后重试')
  } finally {
    forgotLoading.value = false
  }
}

function forget() {
  openForgotDialog()
}

/* ================================================================
   四、登录 / 注册提交（逻辑与原有实现保持一致）
   ================================================================ */
async function Login() {
  if (isLoading.value) return

  if (!/^[0-9]{11}$/.test(username.value)) {
    ElMessage.warning('请输入11位手机号')
    return
  }
  if (!/^.{6,}$/.test(password.value)) {
    ElMessage.warning('密码长度不少于6位')
    return
  }

  isLoading.value = true
  try {
    const userStore = useUserStore()
    const success = await userStore.login(username.value, password.value)
    if (success) {
      ElMessage.success('登录成功！')
      setTimeout(() => {
        router.push('/home')
      }, 500)
    } else {
      ElMessage.error('登录失败，请检查账号密码')
    }
  } catch (err) {
    console.error('登录失败:', err)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

const registerForm = reactive({
  phone: '',
  verifyCode: '',
  password: '',
  passwordAgain: '',
})

async function sendVerifyCodeRegister() {
  const phone = registerForm.phone
  if (!phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  if (countdown.value > 0) return

  try {
    await sendRegisterCode(phone)
    ElMessage.success('验证码已发送到您的手机')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败，请稍后重试')
  }
}

async function handleRegister() {
  const phone = registerForm.phone
  const pwd = registerForm.password
  const passwordAgain = registerForm.passwordAgain
  const code = registerForm.verifyCode

  if (!phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  if (!code) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!pwd) {
    ElMessage.warning('请设置密码')
    return
  }
  if (pwd.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  if (pwd !== passwordAgain) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (isLoading.value) return
  isLoading.value = true

  try {
    await register({ phone, verifyCode: code, password: pwd })
    ElMessage.success('注册成功！')
    const userStore = useUserStore()
    const loginSuccess = await userStore.login(phone, pwd)
    if (loginSuccess) {
      setTimeout(() => {
        router.push('/home')
      }, 500)
    } else {
      ElMessage.warning('自动登录失败，请手动登录')
      turnLogin()
    }
  } catch {
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

/* ================================================================
   五、演示账号快捷填入
   ================================================================ */
const demoAccounts = [
  { label: '管理员', phone: '13800000000' },
  { label: '专家', phone: '13800001001' },
  { label: '陪护', phone: '13800002101' },
  { label: '家属', phone: '13800003001' },
]
const demoFilled = ref('')

function fillDemo(item: { label: string; phone: string }) {
  username.value = item.phone
  password.value = '123456'
  demoFilled.value = item.label
}

/* ================================================================
   六、动态背景：图标星座 / 视差 / 光斑 / 涟漪
   ================================================================ */
const pageRef = ref<HTMLElement | null>(null)
const cardRef = ref<HTMLElement | null>(null)

/** 背景图标星座：l/t 为百分比坐标，s 尺寸，d 视差深度，dur 漂浮周期 */
const bgIcons = [
  { n: 'Sunrise', l: 17, t: 9, s: 38, d: 0.9, dur: 16, delay: -3, rot: -8 },
  { n: 'FirstAidKit', l: 5, t: 17, s: 40, d: 1.5, dur: 14, delay: -7, rot: -12 },
  { n: 'TrendCharts', l: 11, t: 40, s: 27, d: 2.1, dur: 19, delay: -11, rot: 6 },
  { n: 'House', l: 3.5, t: 63, s: 31, d: 1.8, dur: 15, delay: -5, rot: 0 },
  { n: 'Bell', l: 14, t: 82, s: 34, d: 1.15, dur: 17, delay: -13, rot: 9 },
  { n: 'DataLine', l: 27, t: 94, s: 26, d: 1.35, dur: 18, delay: -2, rot: -6 },
  { n: 'Moon', l: 44, t: 95, s: 28, d: 1.6, dur: 21, delay: -9, rot: 12 },
  { n: 'Aim', l: 71, t: 94, s: 24, d: 2.4, dur: 22, delay: -4, rot: -5 },
  { n: 'Watch', l: 88, t: 80, s: 33, d: 1.25, dur: 15, delay: -8, rot: 8 },
  { n: 'Odometer', l: 94, t: 58, s: 36, d: 1.55, dur: 18, delay: -15, rot: -9 },
  { n: 'VideoCamera', l: 88, t: 30, s: 38, d: 1.3, dur: 16, delay: -6, rot: 10 },
  { n: 'Cpu', l: 95, t: 12, s: 27, d: 2.2, dur: 20, delay: -1, rot: 0 },
  { n: 'Service', l: 74, t: 5, s: 31, d: 1.7, dur: 17, delay: -12, rot: 7 },
  { n: 'Phone', l: 58, t: 4, s: 23, d: 2.6, dur: 19, delay: -4, rot: -14 },
]

const ripples = ref<{ id: number; x: number; y: number }[]>([])
const auraHot = ref(false)
let rippleSeq = 0

let rafId = 0
let curX = 0
let curY = 0
let tgtX = 0
let tgtY = 0
let lastMove = 0
let iconNodes: HTMLElement[] = []
let iconCenters: { x: number; y: number }[] = []
let cardBox = { cx: 0, cy: 0, hw: 1, hh: 1 }
let reducedMotion = false

function cacheGeometry() {
  if (!pageRef.value) return
  iconNodes = Array.from(pageRef.value.querySelectorAll<HTMLElement>('.bg-icon'))
  iconCenters = iconNodes.map((el) => {
    const r = el.getBoundingClientRect()
    return { x: r.left + r.width / 2, y: r.top + r.height / 2 }
  })
  if (cardRef.value) {
    const c = cardRef.value.getBoundingClientRect()
    cardBox = {
      cx: c.left + c.width / 2,
      cy: c.top + c.height / 2,
      hw: Math.max(c.width / 2, 1),
      hh: Math.max(c.height / 2, 1),
    }
  }
}

function tick() {
  const page = pageRef.value
  if (!page) {
    rafId = 0
    return
  }
  curX += (tgtX - curX) * 0.14
  curY += (tgtY - curY) * 0.14

  const w = window.innerWidth || 1
  const h = window.innerHeight || 1
  page.style.setProperty('--px', ((curX / w - 0.5) * 30).toFixed(2) + 'px')
  page.style.setProperty('--py', ((curY / h - 0.5) * 30).toFixed(2) + 'px')
  page.style.setProperty('--cx', curX.toFixed(1) + 'px')
  page.style.setProperty('--cy', curY.toFixed(1) + 'px')

  for (let i = 0; i < iconNodes.length; i++) {
    const c = iconCenters[i]
    if (!c) continue
    const dist = Math.hypot(curX - c.x, curY - c.y)
    iconNodes[i].style.setProperty('--prox', Math.max(0, 1 - dist / 270).toFixed(3))
  }

  if (cardRef.value) {
    const dx = Math.max(-1, Math.min(1, (curX - cardBox.cx) / cardBox.hw))
    const dy = Math.max(-1, Math.min(1, (curY - cardBox.cy) / cardBox.hh))
    cardRef.value.style.setProperty('--ry', (dx * 2.4).toFixed(2) + 'deg')
    cardRef.value.style.setProperty('--rx', (-dy * 2).toFixed(2) + 'deg')
  }

  const settled = Math.abs(tgtX - curX) < 0.2 && Math.abs(tgtY - curY) < 0.2
  if (!settled || Date.now() - lastMove < 600) {
    rafId = requestAnimationFrame(tick)
  } else {
    rafId = 0
  }
}

function onPageMove(e: MouseEvent) {
  const page = pageRef.value
  if (!page || reducedMotion) return
  page.style.setProperty('--mx', e.clientX + 'px')
  page.style.setProperty('--my', e.clientY + 'px')
  tgtX = e.clientX
  tgtY = e.clientY
  lastMove = Date.now()

  const target = e.target as HTMLElement | null
  auraHot.value = !!target?.closest?.('button, a, .el-input, .mode-switch, .demo-chip')

  if (!rafId) {
    rafId = requestAnimationFrame(tick)
  }
}

function onPageLeave() {
  tgtX = (window.innerWidth || 0) / 2
  tgtY = (window.innerHeight || 0) / 2
  lastMove = Date.now()
  if (!rafId) rafId = requestAnimationFrame(tick)
}

function onPageDown(e: MouseEvent) {
  if (reducedMotion) return
  const id = ++rippleSeq
  ripples.value.push({ id, x: e.clientX, y: e.clientY })
  window.setTimeout(() => {
    ripples.value = ripples.value.filter((r) => r.id !== id)
  }, 900)
}

/* ================================================================
   七、生命周期
   ================================================================ */
let resizeTimer: ReturnType<typeof setTimeout> | null = null

function handleResize() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(cacheGeometry, 120)
}

onMounted(() => {
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  refreshCaptcha()
  curX = tgtX = (window.innerWidth || 0) / 2
  curY = tgtY = (window.innerHeight || 0) / 2
  if (pageRef.value) {
    pageRef.value.style.setProperty('--cx', curX + 'px')
    pageRef.value.style.setProperty('--cy', curY + 'px')
    pageRef.value.style.setProperty('--mx', curX + 'px')
    pageRef.value.style.setProperty('--my', curY + 'px')
  }
  cacheGeometry()
  window.setTimeout(cacheGeometry, 260)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  if (rafId) cancelAnimationFrame(rafId)
  if (resizeTimer) clearTimeout(resizeTimer)
  window.removeEventListener('resize', handleResize)
})

const brandPoints = [
  { icon: 'Aim', title: '跌倒智能识别', desc: '姿态估计 + 跨帧跟踪 + 时序分类' },
  { icon: 'TrendCharts', title: '体征实时监测', desc: '步态、心率与活动度连续采集' },
  { icon: 'Bell', title: '分级告警联动', desc: '家属、社区与陪护多端同步触达' },
]

const passwordScore = computed(() => {
  const v = registerForm.password
  if (!v) return 0
  let s = 0
  if (v.length >= 6) s++
  if (v.length >= 10) s++
  if (/[a-zA-Z]/.test(v) && /\d/.test(v)) s++
  if (/[^a-zA-Z0-9]/.test(v)) s++
  return Math.min(s, 4)
})
const passwordHint = computed(() => ['', '偏弱', '一般', '较强', '很强'][passwordScore.value])
</script>

<template>
  <div
    ref="pageRef"
    class="login-page"
    @mousemove.passive="onPageMove"
    @mouseleave="onPageLeave"
    @mousedown="onPageDown"
  >
    <!-- ============================================================
         动态背景层
         ============================================================ -->
    <div class="scene" aria-hidden="true">
      <div class="scene__base"></div>

      <!-- 流光色块（朝夕：晨光到暮色） -->
      <div class="aurora aurora--1"></div>
      <div class="aurora aurora--2"></div>
      <div class="aurora aurora--3"></div>
      <div class="aurora aurora--4"></div>

      <!-- 细网格 -->
      <div class="scene__mesh"></div>

      <!-- 卡片背后的色雾：让磨砂玻璃有可折射的颜色 -->
      <div class="scene-orb scene-orb--1"></div>
      <div class="scene-orb scene-orb--2"></div>
      <div class="scene-orb scene-orb--3"></div>

      <!-- 图标星座：随鼠标做视差与近距响应 -->
      <div class="icon-field">
        <div
          v-for="(ic, i) in bgIcons"
          :key="i"
          class="bg-icon"
          :style="{
            left: ic.l + '%',
            top: ic.t + '%',
            '--d': ic.d,
            '--dur': ic.dur + 's',
            '--delay': ic.delay + 's',
            '--rot': ic.rot + 'deg',
            fontSize: ic.s + 'px',
          }"
        >
          <span class="bg-icon__inner">
            <el-icon><component :is="ic.n" /></el-icon>
          </span>
        </div>
      </div>

      <!-- 鼠标光斑 -->
      <div class="spotlight-glow"></div>
      <div class="spotlight"></div>

      <!-- 点击涟漪 -->
      <span
        v-for="r in ripples"
        :key="r.id"
        class="ripple"
        :style="{ left: r.x + 'px', top: r.y + 'px' }"
      ></span>

      <div class="scene__grain"></div>
    </div>

    <!-- ============================================================
         忘记密码弹框
         ============================================================ -->
    <el-dialog
      v-model="forgotDialogVisible"
      title="重置密码"
      width="460px"
      class="glass-dialog"
      :close-on-click-modal="false"
      @close="closeForgotDialog"
    >
      <el-form
        ref="forgotFormRef"
        :model="forgotForm"
        :rules="forgotRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入绑定的邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="验证码" prop="verifyCode">
          <div class="dialog-row">
            <el-input
              v-model="forgotForm.verifyCode"
              placeholder="请输入6位验证码"
              prefix-icon="Key"
              maxlength="6"
              @keyup.enter="submitForgotPassword"
            />
            <el-button :disabled="countdown > 0" @click="sendVerifyCode">
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="请输入6-20位新密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="submitForgotPassword"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="submitForgotPassword"
          />
        </el-form-item>

        <p class="dialog-tip">
          <el-icon><InfoFilled /></el-icon>
          验证码将发送到您的绑定邮箱，请及时查收
        </p>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeForgotDialog">取消</el-button>
          <el-button type="primary" :loading="forgotLoading" @click="submitForgotPassword">
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ============================================================
         主卡片
         ============================================================ -->
    <main class="card-wrap">
      <div ref="cardRef" class="login-card">
        <!-- ---------------- 左侧：品牌叙事 ---------------- -->
        <section class="brand-panel">
          <div class="brand-panel__halo"></div>
          <div class="brand-panel__dots"></div>

          <header class="brand-top">
            <span class="brand-mark">
              <svg viewBox="0 0 48 48" role="img" aria-label="朝夕护行标识">
                <path
                  d="M24 5.6 L39.4 11.8 V24.6 C39.4 33.4 32.6 40.1 24 43.4 C15.4 40.1 8.6 33.4 8.6 24.6 V11.8 Z"
                  fill="rgba(255,255,255,0.14)"
                  stroke="rgba(255,255,255,0.9)"
                  stroke-width="1.8"
                  stroke-linejoin="round"
                />
                <path
                  d="M13.6 25.4 h4.9 l3.2 -7.2 l4.2 13.4 l3.1 -6.2 h5.4"
                  fill="none"
                  stroke="#8ce8ff"
                  stroke-width="2.3"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </span>
            <div class="brand-title">
              <strong>朝夕护行</strong>
              <em>多模态防跌倒智慧养老平台</em>
            </div>
          </header>

          <div class="brand-copy">
            <p class="brand-eyebrow">
              <span class="brand-eyebrow__dot"></span>
              朝夕守护 · 步履安然
            </p>
            <h1>
              让每一次<span class="hl">跌倒风险</span><br />
              都在<em>3 秒</em>内被看见
            </h1>
            <p class="brand-desc">
              面向居家与社区养老场景，融合姿态估计、跨帧跟踪与时序分类，
              从感知、识别到告警处置形成闭环。
            </p>
          </div>

          <ul class="brand-points">
            <li v-for="p in brandPoints" :key="p.title">
              <span class="point-icon">
                <el-icon><component :is="p.icon" /></el-icon>
              </span>
              <span class="point-text">
                <b>{{ p.title }}</b>
                <i>{{ p.desc }}</i>
              </span>
            </li>
          </ul>

          <div class="brand-pulse" aria-hidden="true">
            <svg viewBox="0 0 360 46" preserveAspectRatio="none">
              <path
                class="brand-pulse__base"
                d="M0 24 H54 l8 -13 l7 25 l8 -13 H108 l10 -19 l9 35 l10 -19 H186 l8 -9 l7 17 l8 -9 H266 l9 -15 l10 27 l9 -15 H360"
              />
              <path
                class="brand-pulse__run"
                d="M0 24 H54 l8 -13 l7 25 l8 -13 H108 l10 -19 l9 35 l10 -19 H186 l8 -9 l7 17 l8 -9 H266 l9 -15 l10 27 l9 -15 H360"
              />
            </svg>
          </div>

          <footer class="brand-foot">
            <span class="live-dot"></span>
            <span>监护服务运行正常 · 7×24 小时在线</span>
          </footer>
        </section>

        <!-- ---------------- 右侧：登录 / 注册表单 ---------------- -->
        <section class="form-panel">
          <!-- 模式切换 -->
          <div class="mode-switch" role="tablist" aria-label="登录或注册">
            <button
              class="mode-switch__btn"
              :class="{ 'is-active': turn === 1 }"
              role="tab"
              :aria-selected="turn === 1"
              type="button"
              @click="turnLogin"
            >
              <el-icon><UserFilled /></el-icon>
              账号登录
            </button>
            <button
              class="mode-switch__btn"
              :class="{ 'is-active': turn === 0 }"
              role="tab"
              :aria-selected="turn === 0"
              type="button"
              @click="turnRegister"
            >
              <el-icon><EditPen /></el-icon>
              注册新账号
            </button>
            <span class="mode-switch__thumb" :class="{ 'is-right': turn === 0 }"></span>
          </div>

          <!-- ============ 登录 ============ -->
          <div v-if="turn === 1" class="form-inner" key="login">
            <header class="form-head">
              <h2>欢迎回来</h2>
              <p>使用手机号登录，系统将自动进入对应终端</p>
            </header>

            <div class="field">
              <label class="field-label" for="login-phone">手机号</label>
              <el-input
                id="login-phone"
                v-model="username"
                class="glass-input"
                size="large"
                placeholder="请输入 11 位手机号"
                maxlength="11"
                :disabled="isLoading"
                @keyup.enter="Login()"
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="field">
              <label class="field-label" for="login-pwd">密码</label>
              <el-input
                id="login-pwd"
                v-model="password"
                class="glass-input"
                size="large"
                type="password"
                show-password
                placeholder="请输入登录密码"
                :disabled="isLoading"
                @keyup.enter="Login()"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="field">
              <label class="field-label" for="login-code">验证码</label>
              <div class="field-row">
                <el-input
                  id="login-code"
                  v-model="verifyCode"
                  class="glass-input"
                  size="large"
                  placeholder="请输入右侧字符"
                  :disabled="isLoading"
                  @keyup.enter="Login()"
                >
                  <template #prefix>
                    <el-icon><Stamp /></el-icon>
                  </template>
                </el-input>
                <button
                  class="captcha-box"
                  type="button"
                  title="点击刷新验证码"
                  @click="refreshCaptcha"
                >
                  <svg viewBox="0 0 112 46" role="img" aria-label="图形验证码">
                    <rect width="112" height="46" rx="10" fill="rgba(236,246,255,0.72)" />
                    <g
                      font-family="Georgia, 'Times New Roman', serif"
                      font-weight="700"
                      font-size="20"
                      opacity="0.95"
                    >
                      <text
                        v-for="(ch, i) in captchaChars"
                        :key="i"
                        :x="15 + i * 24"
                        :y="31"
                        :fill="['#1a63c4', '#0b8f7c', '#b96a12', '#7448b8'][i % 4]"
                        :transform="`rotate(${-12 + i * 8} ${15 + i * 24} 31)`"
                      >
                        {{ ch }}
                      </text>
                    </g>
                    <g stroke="rgba(118,168,222,0.7)" stroke-width="1" fill="none">
                      <path d="M6 36 Q 30 20 58 34 T 106 28" />
                      <path d="M4 12 Q 34 26 62 10 T 108 18" />
                      <circle cx="18" cy="9" r="1.3" fill="rgba(118,168,222,0.7)" stroke="none" />
                      <circle cx="88" cy="37" r="1.3" fill="rgba(118,168,222,0.7)" stroke="none" />
                    </g>
                  </svg>
                </button>
              </div>
            </div>

            <div class="aux-row">
              <label class="remember">
                <el-switch v-model="remember" size="small" />
                <span>记住登录状态</span>
              </label>
              <a class="link" href="javascript:void(0)" @click="forget()">忘记密码？</a>
            </div>

            <button class="cta-btn" type="button" :disabled="isLoading" @click="Login()">
              <el-icon v-if="!isLoading" class="cta-btn__icon"><Unlock /></el-icon>
              <el-icon v-else class="cta-btn__icon is-spin"><Loading /></el-icon>
              <span>{{ isLoading ? '正在登录…' : '登 录' }}</span>
              <i class="cta-btn__shine"></i>
            </button>

            <div class="or-line"><span>首次使用朝夕护行</span></div>

            <button class="ghost-btn" type="button" :disabled="isLoading" @click="turnRegister">
              <el-icon><EditPen /></el-icon>
              立即注册守护账号
            </button>

            <div class="demo-row">
              <span class="demo-row__label">演示账号</span>
              <button
                v-for="d in demoAccounts"
                :key="d.phone"
                class="demo-chip"
                :class="{ 'is-on': demoFilled === d.label }"
                type="button"
                @click="fillDemo(d)"
              >
                {{ d.label }}
              </button>
            </div>
          </div>

          <!-- ============ 注册 ============ -->
          <div v-else class="form-inner" key="register">
            <header class="form-head">
              <h2>创建守护账号</h2>
              <p>注册后即可开启 24 小时智能守护</p>
            </header>

            <div class="field">
              <label class="field-label" for="reg-phone">手机号</label>
              <el-input
                id="reg-phone"
                v-model="registerForm.phone"
                class="glass-input"
                size="large"
                placeholder="请输入 11 位手机号"
                maxlength="11"
                :disabled="isLoading"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="field">
              <label class="field-label" for="reg-code">短信验证码</label>
              <div class="field-row">
                <el-input
                  id="reg-code"
                  v-model="registerForm.verifyCode"
                  class="glass-input"
                  size="large"
                  placeholder="请输入 6 位验证码"
                  maxlength="6"
                  :disabled="isLoading"
                  @keyup.enter="handleRegister"
                >
                  <template #prefix>
                    <el-icon><ChatDotRound /></el-icon>
                  </template>
                </el-input>
                <button
                  class="code-btn"
                  type="button"
                  :disabled="countdown > 0 || isLoading"
                  @click="sendVerifyCodeRegister"
                >
                  {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                </button>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="reg-pwd">设置密码</label>
              <el-input
                id="reg-pwd"
                v-model="registerForm.password"
                class="glass-input"
                size="large"
                type="password"
                show-password
                placeholder="至少 6 位，建议字母 + 数字"
                :disabled="isLoading"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
              <div v-if="registerForm.password" class="strength" :data-score="passwordScore">
                <span
                  v-for="n in 4"
                  :key="n"
                  class="strength__bar"
                  :class="{ 'is-on': n <= passwordScore }"
                ></span>
                <em>{{ passwordHint }}</em>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="reg-pwd2">确认密码</label>
              <el-input
                id="reg-pwd2"
                v-model="registerForm.passwordAgain"
                class="glass-input"
                size="large"
                type="password"
                show-password
                placeholder="请再次输入密码"
                :disabled="isLoading"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <el-icon><CircleCheck /></el-icon>
                </template>
              </el-input>
            </div>

            <button class="cta-btn" type="button" :disabled="isLoading" @click="handleRegister">
              <el-icon v-if="!isLoading" class="cta-btn__icon"><Select /></el-icon>
              <el-icon v-else class="cta-btn__icon is-spin"><Loading /></el-icon>
              <span>{{ isLoading ? '正在注册…' : '注 册' }}</span>
              <i class="cta-btn__shine"></i>
            </button>

            <button class="ghost-btn" type="button" :disabled="isLoading" @click="turnLogin">
              <el-icon><Back /></el-icon>
              已有账号，返回登录
            </button>
          </div>

          <p class="form-foot">登录即表示同意平台安全使用规范 · 数据仅用于长者健康监护</p>
        </section>
      </div>
    </main>

    <footer class="page-foot">© 2026 朝夕护行 · 多模态防跌倒智慧养老平台</footer>

    <!-- 跟随光标的光环（位于内容之上，作为明确的动效反馈） -->
    <div class="cursor-aura" :class="{ 'is-hot': auraHot }" aria-hidden="true"></div>
  </div>
</template>

<style scoped>
/* ================================================================
   设计令牌
   ================================================================ */
.login-page {
  --ink: #0d2949;
  --ink-2: #4a6a8d;
  --ink-3: #7d97b4;
  --brand: #1668d6;
  --brand-2: #27b0e0;
  --mint: #10b3a0;
  --glass-line: rgba(255, 255, 255, 0.72);

  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #eaf2fe;
  color: var(--ink);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* ================================================================
   动态背景
   ================================================================ */
.scene {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.scene__base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(120% 100% at 12% 0%, #f3f9ff 0%, rgba(243, 249, 255, 0) 55%),
    radial-gradient(100% 90% at 92% 8%, #e2f1ff 0%, rgba(226, 241, 255, 0) 58%),
    linear-gradient(135deg, #e7f1ff 0%, #eef6ff 42%, #f4fbfa 72%, #fdf7ef 100%);
}

.aurora {
  position: absolute;
  border-radius: 50%;
  filter: blur(72px);
  opacity: 0.62;
  will-change: transform;
}

.aurora--1 {
  width: 620px;
  height: 620px;
  left: -180px;
  top: -220px;
  background: radial-gradient(circle, #b9dcff 0%, rgba(185, 220, 255, 0) 68%);
  animation: drift-a 26s ease-in-out infinite;
}

.aurora--2 {
  width: 700px;
  height: 700px;
  right: -240px;
  top: -140px;
  background: radial-gradient(circle, #c3e9ff 0%, rgba(195, 233, 255, 0) 66%);
  animation: drift-b 32s ease-in-out infinite;
}

.aurora--3 {
  width: 660px;
  height: 660px;
  left: -160px;
  bottom: -260px;
  background: radial-gradient(circle, #c8f0e6 0%, rgba(200, 240, 230, 0) 68%);
  animation: drift-c 29s ease-in-out infinite;
}

.aurora--4 {
  width: 540px;
  height: 540px;
  right: -140px;
  bottom: -200px;
  background: radial-gradient(circle, #ffe6c4 0%, rgba(255, 230, 196, 0) 66%);
  animation: drift-a 24s ease-in-out infinite reverse;
}

@keyframes drift-a {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(70px, 50px, 0) scale(1.1);
  }
}
@keyframes drift-b {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1.04);
  }
  50% {
    transform: translate3d(-90px, 70px, 0) scale(0.94);
  }
}
@keyframes drift-c {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(0.96);
  }
  50% {
    transform: translate3d(80px, -60px, 0) scale(1.08);
  }
}

.scene__mesh {
  position: absolute;
  inset: -10%;
  background-image:
    linear-gradient(rgba(122, 170, 220, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(122, 170, 220, 0.09) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(90% 80% at 50% 45%, #000 20%, transparent 78%);
  -webkit-mask-image: radial-gradient(90% 80% at 50% 45%, #000 20%, transparent 78%);
  animation: mesh-pan 40s linear infinite;
}

/* 卡片背后的色雾：被卡片的 backdrop-filter 折射成磨砂质感 */
.scene-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(58px);
  opacity: 0.72;
  will-change: transform;
}

.scene-orb--1 {
  width: 380px;
  height: 380px;
  left: 50%;
  top: 6%;
  margin-left: -190px;
  background: radial-gradient(circle, #7fc4ff 0%, rgba(127, 196, 255, 0) 68%);
  animation: orb-float 18s ease-in-out infinite;
}

.scene-orb--2 {
  width: 420px;
  height: 420px;
  right: 8%;
  bottom: -6%;
  background: radial-gradient(circle, #7fe6d2 0%, rgba(127, 230, 210, 0) 68%);
  animation: orb-float 22s ease-in-out infinite reverse;
}

.scene-orb--3 {
  width: 320px;
  height: 320px;
  left: 58%;
  bottom: 4%;
  background: radial-gradient(circle, #ffd9ad 0%, rgba(255, 217, 173, 0) 68%);
  animation: orb-float 26s ease-in-out infinite;
}

@keyframes orb-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(-40px, 34px, 0) scale(1.12);
  }
}

@keyframes mesh-pan {
  to {
    background-position:
      56px 56px,
      56px 56px;
  }
}

/* ===== 图标星座 ===== */
.icon-field {
  position: absolute;
  inset: 0;
}

.bg-icon {
  position: absolute;
  transform: translate3d(calc(var(--px, 0px) * var(--d)), calc(var(--py, 0px) * var(--d)), 0);
  color: #2f7fd0;
  will-change: transform;
}

.bg-icon__inner {
  display: block;
  opacity: calc(0.2 + var(--prox, 0) * 0.58);
  transform: scale(calc(0.92 + var(--prox, 0) * 0.42));
  transform-origin: center;
  filter: drop-shadow(0 0 calc(var(--prox, 0) * 16px) rgba(47, 150, 235, 0.55));
  animation: icon-float var(--dur, 16s) ease-in-out infinite;
  animation-delay: var(--delay, 0s);
  transition: opacity 0.3s ease;
}

.bg-icon__inner :deep(svg) {
  transform: rotate(var(--rot, 0deg));
}

@keyframes icon-float {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 -14px;
  }
}

/* ===== 鼠标光斑 ===== */
.spotlight,
.spotlight-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.spotlight {
  background: radial-gradient(
    300px circle at var(--mx, 50%) var(--my, 50%),
    rgba(255, 255, 255, 0.92) 0%,
    rgba(255, 255, 255, 0.4) 34%,
    rgba(255, 255, 255, 0) 70%
  );
}

.spotlight-glow {
  background: radial-gradient(
    620px circle at var(--mx, 50%) var(--my, 50%),
    rgba(52, 148, 240, 0.24) 0%,
    rgba(52, 148, 240, 0.08) 44%,
    rgba(52, 148, 240, 0) 72%
  );
}

/* 跟随光标的柔和光环 */
.cursor-aura {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 8;
  width: 30px;
  height: 30px;
  margin: -15px 0 0 -15px;
  border-radius: 50%;
  border: 1.5px solid rgba(236, 248, 255, 0.88);
  background: radial-gradient(circle, rgba(70, 155, 240, 0.55) 0 2.5px, rgba(70, 155, 240, 0) 3px);
  box-shadow:
    0 0 0 1px rgba(56, 150, 235, 0.45),
    0 0 16px rgba(96, 190, 255, 0.7),
    inset 0 0 12px rgba(96, 190, 255, 0.4);
  opacity: 0;
  pointer-events: none;
  transform: translate3d(var(--cx, 50vw), var(--cy, 50vh), 0);
  transition:
    opacity 0.4s ease,
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    background 0.24s ease;
  will-change: transform;
}

.cursor-aura.is-hot {
  border-color: rgba(26, 118, 214, 0.9);
  background: rgba(56, 150, 235, 0.16);
  box-shadow:
    0 0 0 3px rgba(255, 255, 255, 0.5),
    0 0 26px rgba(96, 190, 255, 0.85),
    inset 0 0 14px rgba(96, 190, 255, 0.55);
}

.login-page:hover .cursor-aura {
  opacity: 1;
}

@media (hover: none) {
  .cursor-aura,
  .spotlight,
  .spotlight-glow {
    display: none;
  }
}

.login-page:hover .spotlight,
.login-page:hover .spotlight-glow {
  opacity: 1;
}

/* ===== 点击涟漪 ===== */
.ripple {
  position: absolute;
  width: 12px;
  height: 12px;
  margin: -6px 0 0 -6px;
  border-radius: 50%;
  border: 1.5px solid rgba(47, 150, 235, 0.55);
  background: rgba(120, 200, 255, 0.18);
  animation: ripple-out 0.85s cubic-bezier(0.16, 0.84, 0.44, 1) forwards;
}

@keyframes ripple-out {
  from {
    transform: scale(1);
    opacity: 0.85;
  }
  to {
    transform: scale(14);
    opacity: 0;
  }
}

.scene__grain {
  position: absolute;
  inset: 0;
  opacity: 0.5;
  background-image: radial-gradient(rgba(24, 74, 130, 0.055) 1px, transparent 1px);
  background-size: 4px 4px;
  mix-blend-mode: multiply;
}

/* ================================================================
   卡片
   ================================================================ */
.card-wrap {
  position: relative;
  z-index: 3;
  perspective: 1600px;
  animation: card-in 0.72s cubic-bezier(0.2, 0.9, 0.3, 1) both;
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(26px) scale(0.985);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-card {
  --rx: 0deg;
  --ry: 0deg;
  position: relative;
  display: grid;
  grid-template-columns: 1.02fr 1fr;
  width: 1040px;
  max-width: 93vw;
  min-height: 620px;
  border-radius: 28px;
  overflow: hidden;
  transform: rotateX(var(--rx)) rotateY(var(--ry));
  transform-style: preserve-3d;
  background: rgba(255, 255, 255, 0.38);
  -webkit-backdrop-filter: blur(30px) saturate(190%);
  backdrop-filter: blur(30px) saturate(190%);
  border: 1px solid rgba(255, 255, 255, 0.78);
  box-shadow:
    0 40px 90px -30px rgba(21, 74, 140, 0.35),
    0 18px 40px -24px rgba(21, 74, 140, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
}

/* ================================================================
   左侧品牌区
   ================================================================ */
.brand-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 42px 42px 34px;
  overflow: hidden;
  color: #eaf4ff;
  background: linear-gradient(
    158deg,
    rgba(9, 44, 86, 0.94) 0%,
    rgba(13, 72, 128, 0.9) 46%,
    rgba(10, 86, 108, 0.86) 100%
  );
}

.brand-panel__halo {
  position: absolute;
  width: 460px;
  height: 460px;
  right: -190px;
  top: -190px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(105, 205, 255, 0.42) 0%, rgba(105, 205, 255, 0) 65%);
  animation: halo-pulse 9s ease-in-out infinite;
}

@keyframes halo-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.85;
  }
  50% {
    transform: scale(1.16);
    opacity: 1;
  }
}

.brand-panel__dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.16) 1px, transparent 1px);
  background-size: 22px 22px;
  mask-image: radial-gradient(80% 70% at 75% 15%, #000 0%, transparent 70%);
  -webkit-mask-image: radial-gradient(80% 70% at 75% 15%, #000 0%, transparent 70%);
}

.brand-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 13px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0.06));
  border: 1px solid rgba(255, 255, 255, 0.34);
  box-shadow: 0 10px 24px -8px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
}

.brand-mark svg {
  width: 28px;
  height: 28px;
}

.brand-title {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.brand-title strong {
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #fff;
}

.brand-title em {
  font-size: 11.5px;
  font-style: normal;
  letter-spacing: 1.2px;
  color: rgba(213, 235, 255, 0.78);
}

.brand-copy {
  position: relative;
  margin-top: 44px;
}

.brand-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  padding: 5px 13px 5px 11px;
  font-size: 12px;
  letter-spacing: 1.4px;
  color: #b9e6ff;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-eyebrow__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6fe3c9;
  box-shadow: 0 0 0 3px rgba(111, 227, 201, 0.24);
}

.brand-copy h1 {
  margin: 0 0 14px;
  font-size: 30px;
  line-height: 1.44;
  font-weight: 800;
  letter-spacing: 1px;
  color: #fff;
}

.brand-copy h1 .hl {
  background: linear-gradient(100deg, #8ce8ff 0%, #6fe3c9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.brand-copy h1 em {
  font-style: normal;
  font-size: 34px;
  color: #8ce8ff;
  padding: 0 2px;
}

.brand-desc {
  margin: 0;
  max-width: 340px;
  font-size: 13px;
  line-height: 1.9;
  color: rgba(206, 230, 252, 0.82);
}

.brand-points {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin: 30px 0 0;
  padding: 0;
  list-style: none;
}

.brand-points li {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 9px 13px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.13);
  transition:
    background 0.25s ease,
    transform 0.25s ease,
    border-color 0.25s ease;
}

.brand-points li:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(140, 232, 255, 0.4);
  transform: translateX(3px);
}

.point-icon {
  display: grid;
  place-items: center;
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  font-size: 15px;
  color: #8ce8ff;
  background: rgba(140, 232, 255, 0.14);
  border: 1px solid rgba(140, 232, 255, 0.24);
}

.point-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.point-text b {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.6px;
  color: #f2f9ff;
}

.point-text i {
  font-style: normal;
  font-size: 11.5px;
  letter-spacing: 0.2px;
  color: rgba(202, 228, 250, 0.86);
}

/* ===== 品牌区心电图波形 ===== */
.brand-pulse {
  position: relative;
  margin-top: auto;
  padding-top: 26px;
}

.brand-pulse svg {
  display: block;
  width: 100%;
  height: 46px;
  overflow: visible;
}

.brand-pulse__base,
.brand-pulse__run {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.brand-pulse__base {
  stroke: rgba(140, 232, 255, 0.18);
  stroke-width: 1.6;
}

.brand-pulse__run {
  stroke: #6fe3c9;
  stroke-width: 2;
  stroke-dasharray: 46 320;
  filter: drop-shadow(0 0 6px rgba(111, 227, 201, 0.8));
  animation: pulse-run 5.2s linear infinite;
}

@keyframes pulse-run {
  from {
    stroke-dashoffset: 366;
  }
  to {
    stroke-dashoffset: 0;
  }
}

.brand-foot {
  position: relative;
  display: flex;
  align-items: center;
  gap: 9px;
  padding-top: 16px;
  font-size: 11.5px;
  letter-spacing: 0.6px;
  color: rgba(180, 214, 244, 0.7);
}

.live-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4ade9b;
  box-shadow: 0 0 0 0 rgba(74, 222, 155, 0.6);
  animation: live-pulse 2.2s ease-out infinite;
}

@keyframes live-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(74, 222, 155, 0.55);
  }
  70% {
    box-shadow: 0 0 0 9px rgba(74, 222, 155, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(74, 222, 155, 0);
  }
}

/* ================================================================
   右侧表单区
   ================================================================ */
.form-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px 48px 26px;
}

/* 表单区底纹：为磨砂输入框提供可折射的颜色 */
.form-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(72% 56% at 94% -6%, rgba(106, 180, 255, 0.55) 0%, rgba(106, 180, 255, 0) 70%),
    radial-gradient(70% 52% at 0% 104%, rgba(96, 222, 198, 0.46) 0%, rgba(96, 222, 198, 0) 70%),
    radial-gradient(46% 32% at 98% 72%, rgba(255, 208, 156, 0.26) 0%, rgba(255, 208, 156, 0) 76%),
    linear-gradient(158deg, rgba(206, 231, 255, 0.5) 0%, rgba(238, 250, 248, 0.34) 100%);
}

.form-panel > * {
  position: relative;
  z-index: 1;
}

.form-inner {
  animation: panel-in 0.36s cubic-bezier(0.2, 0.9, 0.3, 1) both;
}

@keyframes panel-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 模式切换 ===== */
.mode-switch {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  padding: 4px;
  margin-bottom: 24px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 1px 3px rgba(24, 80, 150, 0.07);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
}

.mode-switch__btn {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 40px;
  border: none;
  border-radius: 11px;
  background: transparent;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.6px;
  color: var(--ink-3);
  cursor: pointer;
  transition: color 0.24s ease;
}

.mode-switch__btn:hover {
  color: var(--ink-2);
}

.mode-switch__btn.is-active {
  color: var(--brand);
}

.mode-switch__btn .el-icon {
  font-size: 15px;
}

.mode-switch__thumb {
  position: absolute;
  z-index: 1;
  top: 4px;
  left: 4px;
  width: calc(50% - 6px);
  height: 40px;
  border-radius: 11px;
  background: #fff;
  box-shadow:
    0 4px 12px -2px rgba(24, 80, 150, 0.16),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
  transition: transform 0.32s cubic-bezier(0.34, 1.3, 0.64, 1);
}

.mode-switch__thumb.is-right {
  transform: translateX(calc(100% + 4px));
}

/* ===== 标题 ===== */
.form-head {
  margin-bottom: 22px;
}

.form-head h2 {
  margin: 0 0 7px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.8px;
  color: var(--ink);
}

.form-head p {
  margin: 0;
  font-size: 12.8px;
  letter-spacing: 0.3px;
  color: var(--ink-3);
}

/* ===== 表单字段 ===== */
.field {
  margin-bottom: 15px;
}

.field-label {
  display: block;
  margin-bottom: 7px;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.7px;
  color: #3c6189;
}

.field-row {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.field-row > :first-child {
  flex: 1;
  min-width: 0;
}

/* ===== 磨砂玻璃输入框 ===== */
.form-panel :deep(.glass-input .el-input__wrapper) {
  padding: 3px 13px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.66);
  -webkit-backdrop-filter: blur(20px) saturate(200%);
  backdrop-filter: blur(20px) saturate(200%);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.92),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 10px 24px -16px rgba(18, 68, 130, 0.55),
    0 2px 5px -3px rgba(18, 68, 130, 0.22);
  transition:
    background 0.24s ease,
    box-shadow 0.24s ease,
    transform 0.24s ease;
}

.form-panel :deep(.glass-input .el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.82);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.98),
    0 12px 26px -16px rgba(18, 68, 130, 0.6),
    0 2px 6px -3px rgba(18, 68, 130, 0.25);
}

.form-panel :deep(.glass-input .el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.95);
  box-shadow:
    inset 0 0 0 1.5px rgba(39, 128, 226, 0.65),
    0 0 0 4px rgba(39, 128, 226, 0.12),
    0 10px 24px -12px rgba(21, 74, 140, 0.5);
}

.form-panel :deep(.glass-input .el-input__inner) {
  height: 44px;
  font-size: 14px;
  letter-spacing: 0.4px;
  color: var(--ink);
}

.form-panel :deep(.glass-input .el-input__inner::placeholder) {
  color: #90a6bd;
}

.form-panel :deep(.glass-input .el-input__prefix) {
  margin-right: 6px;
  color: #6f9bc9;
}

.form-panel :deep(.glass-input .el-input__prefix .el-icon) {
  font-size: 16px;
}

.form-panel :deep(.glass-input .el-input__suffix .el-icon) {
  color: #8aa6c2;
}

/* ===== 图形验证码 ===== */
.captcha-box {
  flex: 0 0 112px;
  width: 112px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 13px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.55);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  backdrop-filter: blur(16px) saturate(180%);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    inset 0 0 0 1px rgba(150, 190, 230, 0.22),
    0 6px 16px -12px rgba(21, 74, 140, 0.5);
}

.captcha-box:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px -14px rgba(21, 74, 140, 0.7);
}

.captcha-box svg {
  display: block;
  width: 100%;
  height: 46px;
}

/* ===== 获取验证码按钮 ===== */
.code-btn {
  flex: 0 0 118px;
  border-radius: 13px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.62);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  backdrop-filter: blur(16px) saturate(180%);
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: var(--brand);
  cursor: pointer;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 6px 16px -12px rgba(21, 74, 140, 0.5);
  transition: all 0.22s ease;
}

.code-btn:hover:not(:disabled) {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
  box-shadow: 0 10px 22px -10px rgba(22, 104, 214, 0.6);
}

.code-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  color: #7f97ae;
}

/* ===== 密码强度 ===== */
.strength {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
}

.strength__bar {
  flex: 1;
  height: 3px;
  border-radius: 2px;
  background: rgba(120, 160, 200, 0.24);
  transition: background 0.24s ease;
}

.strength[data-score='1'] .strength__bar.is-on {
  background: #f0907a;
}
.strength[data-score='2'] .strength__bar.is-on {
  background: #f0b95a;
}
.strength[data-score='3'] .strength__bar.is-on {
  background: #4bb6de;
}
.strength[data-score='4'] .strength__bar.is-on {
  background: #10b3a0;
}

.strength em {
  flex: 0 0 auto;
  margin-left: 4px;
  font-style: normal;
  font-size: 11.5px;
  letter-spacing: 0.4px;
  color: var(--ink-3);
}

/* ===== 记住我 / 忘记密码 ===== */
.aux-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 18px;
  font-size: 12.5px;
  color: var(--ink-2);
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.remember :deep(.el-switch) {
  --el-switch-on-color: #10b3a0;
  --el-switch-off-color: #c3d2e2;
  height: 18px;
  line-height: 18px;
}

.link {
  color: var(--brand);
  text-decoration: none;
  letter-spacing: 0.3px;
  transition: color 0.2s ease;
}

.link:hover {
  color: #0d4ea5;
  text-decoration: underline;
}

/* ===== 主行动按钮 ===== */
.cta-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  overflow: hidden;
  font-size: 15.5px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(102deg, #1360cf 0%, #1d8fe0 52%, #21b4d2 100%);
  box-shadow:
    0 16px 32px -14px rgba(19, 96, 207, 0.72),
    inset 0 1px 0 rgba(255, 255, 255, 0.34);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    filter 0.2s ease;
}

.cta-btn__icon {
  font-size: 17px;
  opacity: 0.95;
}

.cta-btn__icon.is-spin {
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.cta-btn:hover:not(:disabled) {
  transform: translateY(-1.5px);
  box-shadow:
    0 22px 38px -14px rgba(19, 96, 207, 0.8),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  filter: saturate(112%);
}

.cta-btn:active:not(:disabled) {
  transform: translateY(0) scale(0.994);
  box-shadow: 0 10px 20px -12px rgba(19, 96, 207, 0.7);
}

.cta-btn:disabled {
  cursor: not-allowed;
  filter: grayscale(18%);
  opacity: 0.82;
}

/* 注册面板：主按钮与返回按钮之间留出呼吸间距 */
.cta-btn + .ghost-btn {
  margin-top: 12px;
}

/* 掠过光带 */
.cta-btn__shine {
  position: absolute;
  top: 0;
  left: -40%;
  width: 34%;
  height: 100%;
  background: linear-gradient(
    100deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.42) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-18deg);
  animation: shine-sweep 4.6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shine-sweep {
  0%,
  62% {
    left: -40%;
  }
  88%,
  100% {
    left: 116%;
  }
}

/* ===== 分割线 ===== */
.or-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 0 13px;
  font-size: 11.5px;
  letter-spacing: 0.6px;
  color: #93a9bf;
}

.or-line::before,
.or-line::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(120, 160, 200, 0),
    rgba(120, 160, 200, 0.42),
    rgba(120, 160, 200, 0)
  );
}

/* ===== 次要按钮 ===== */
.ghost-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  border-radius: 14px;
  border: 1.5px solid rgba(39, 128, 226, 0.45);
  background: rgba(255, 255, 255, 0.55);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  backdrop-filter: blur(16px) saturate(180%);
  font-size: 14px;
  font-weight: 650;
  letter-spacing: 1.2px;
  color: var(--brand);
  cursor: pointer;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: all 0.22s ease;
}

.ghost-btn:hover:not(:disabled) {
  border-color: var(--brand);
  background: rgba(235, 245, 255, 0.95);
  color: #0d4ea5;
  transform: translateY(-1px);
  box-shadow: 0 12px 26px -14px rgba(19, 96, 207, 0.7);
}

.ghost-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== 演示账号 ===== */
.demo-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 16px;
}

.demo-row__label {
  font-size: 11.5px;
  letter-spacing: 0.6px;
  color: #93a9bf;
}

.demo-chip {
  padding: 4px 11px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.48);
  font-size: 11.5px;
  letter-spacing: 0.4px;
  color: #4a6a8d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.demo-chip:hover {
  border-color: rgba(39, 128, 226, 0.5);
  color: var(--brand);
  background: #fff;
}

.demo-chip.is-on {
  border-color: rgba(16, 179, 160, 0.6);
  color: #0a8577;
  background: rgba(222, 250, 244, 0.9);
}

/* ===== 底部说明 ===== */
.form-foot {
  margin: 20px 0 0;
  text-align: center;
  font-size: 11px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  color: #a3b6c9;
}

/* ===== 页面页脚 ===== */
.page-foot {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 18px;
  z-index: 3;
  text-align: center;
  font-size: 11.5px;
  letter-spacing: 0.8px;
  color: rgba(84, 116, 152, 0.62);
}

/* ===== 弹框内布局 ===== */
.dialog-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.dialog-row > :first-child {
  flex: 1;
  min-width: 0;
}

.dialog-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 6px;
  padding-left: 80px;
  font-size: 12px;
  color: #93a9bf;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ================================================================
   响应式
   ================================================================ */
@media (max-width: 1120px) {
  .login-card {
    width: 960px;
    grid-template-columns: 1fr 1fr;
  }
  .brand-panel {
    padding: 36px 32px 30px;
  }
  .form-panel {
    padding: 36px 36px 22px;
  }
}

@media (max-height: 880px) {
  .login-card {
    min-height: 0;
    max-height: 94vh;
    overflow-y: auto;
  }
  .brand-panel {
    padding: 34px 34px 24px;
  }
  .brand-copy {
    margin-top: 28px;
  }
  .brand-copy h1 {
    margin-bottom: 11px;
    font-size: 24px;
    line-height: 1.4;
  }
  .brand-copy h1 em {
    font-size: 27px;
  }
  .brand-desc {
    font-size: 12.5px;
    line-height: 1.8;
  }
  .brand-points {
    gap: 7px;
    margin-top: 20px;
  }
  .brand-points li {
    padding: 7px 11px;
  }
  .brand-pulse {
    padding-top: 16px;
  }
  .brand-pulse svg {
    height: 34px;
  }
  .brand-foot {
    padding-top: 12px;
  }
  .form-panel {
    padding: 30px 40px 20px;
  }
  .form-head {
    margin-bottom: 18px;
  }
  .form-head h2 {
    font-size: 24px;
  }
  .field {
    margin-bottom: 12px;
  }
  .aux-row {
    margin: 2px 0 14px;
  }
  .or-line {
    margin: 14px 0 11px;
  }
  .demo-row {
    margin-top: 13px;
  }
  .form-foot {
    margin-top: 14px;
  }
}

@media (max-width: 880px) {
  .login-card {
    grid-template-columns: 1fr;
    width: 520px;
    min-height: auto;
    max-height: 94vh;
    overflow-y: auto;
  }
  .brand-panel {
    padding: 18px 24px 15px;
  }
  .brand-copy {
    margin-top: 12px;
  }
  .brand-eyebrow,
  .brand-desc,
  .brand-points,
  .brand-pulse {
    display: none;
  }
  .brand-copy h1 {
    margin-bottom: 0;
    font-size: 18.5px;
    line-height: 1.5;
  }
  .brand-copy h1 br {
    display: none;
  }
  .brand-copy h1 em {
    font-size: 20px;
  }
  .brand-foot {
    padding-top: 10px;
    font-size: 10.5px;
  }
  .form-panel {
    padding: 20px 26px 20px;
  }
  .mode-switch {
    margin-bottom: 18px;
  }
  .form-head {
    margin-bottom: 16px;
  }
  .form-head h2 {
    font-size: 22px;
  }
  .field {
    margin-bottom: 11px;
  }
  .field-label {
    margin-bottom: 5px;
  }
  .aux-row {
    margin: 2px 0 14px;
  }
  .or-line {
    margin: 14px 0 11px;
  }
  .demo-row {
    margin-top: 12px;
  }
  .form-foot {
    display: none;
  }
  .icon-field {
    opacity: 0.6;
  }
  .page-foot {
    display: none;
  }
}

@media (max-width: 560px) {
  .login-card {
    width: 100%;
    max-width: 100%;
    max-height: 100%;
    border-radius: 0;
    border: none;
  }
  .form-panel {
    padding: 18px 18px 16px;
  }
  .brand-panel {
    padding: 16px 18px 14px;
  }
  .page-foot {
    display: none;
  }
  .captcha-box {
    flex-basis: 88px;
    width: 88px;
  }
  .code-btn {
    flex-basis: 104px;
    font-size: 11.5px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .aurora,
  .scene__mesh,
  .bg-icon__inner,
  .brand-panel__halo,
  .live-dot,
  .cta-btn__shine,
  .card-wrap {
    animation: none !important;
  }
  .login-card {
    transform: none !important;
  }
  .spotlight,
  .spotlight-glow,
  .cursor-aura {
    display: none;
  }
  .brand-pulse__run {
    animation: none !important;
  }
}
</style>

<style>
/* 弹框（teleport 到 body，需全局作用域） */
.glass-dialog {
  border-radius: 18px !important;
  border: 1px solid rgba(255, 255, 255, 0.85) !important;
  background: rgba(255, 255, 255, 0.86) !important;
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  backdrop-filter: blur(24px) saturate(180%);
  box-shadow: 0 30px 70px -28px rgba(21, 74, 140, 0.45) !important;
}

.glass-dialog .el-dialog__title {
  font-weight: 700;
  letter-spacing: 0.8px;
  color: #0d2949;
}

.glass-dialog .el-input__wrapper {
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: inset 0 0 0 1px rgba(180, 205, 230, 0.7);
}

.glass-dialog .el-input__wrapper.is-focus {
  box-shadow:
    inset 0 0 0 1.5px rgba(39, 128, 226, 0.7),
    0 0 0 3px rgba(39, 128, 226, 0.1);
}

.glass-dialog .el-button {
  border-radius: 11px;
}
</style>
