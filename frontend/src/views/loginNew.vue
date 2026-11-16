<script setup lang="ts">
import { ref, reactive, onBeforeUnmount } from 'vue'
import { UserFilled, Lock, Stamp, Management } from '@element-plus/icons-vue'
import router from '../router/index'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register, sendRegisterCode, sendResetCode, resetPassword } from '@/api/auth'

const username = ref('')
const password = ref('')
const verifyCode = ref('')
const value2 = ref(true)
const isLoading = ref(false)

// ===== 忘记密码弹框相关 =====
const forgotDialogVisible = ref(false)
const forgotForm = reactive({
  email: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
})
const forgotLoading = ref(false)
const countdown = ref(0)
let timer = null
let turn = ref(1)
function turnRegister() {
  turn.value = 0
  console.log('000')
  console.log(turn)
}
function turnLogin() {
  turn.value = 1
  console.log('111')
  console.log(turn)
}
// ===== 表单校验规则 =====
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
      validator: (rule, value, callback) => {
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

const forgotFormRef = ref(null)

// ===== 打开忘记密码弹框 =====
function openForgotDialog() {
  forgotDialogVisible.value = true
  // 重置表单
  Object.assign(forgotForm, {
    email: '',
    verifyCode: '',
    newPassword: '',
    confirmPassword: '',
  })
  forgotFormRef.value?.resetFields()
}

// ===== 关闭弹框 =====
function closeForgotDialog() {
  forgotDialogVisible.value = false
  // 清除倒计时
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  countdown.value = 0
}

// ===== 发送验证码 =====
async function sendVerifyCode() {
  // 校验邮箱
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

    // 开始倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败，请稍后重试')
  }
}

// ===== 提交重置密码 =====
async function submitForgotPassword() {
  // 表单校验
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

function goToRegister() {
  router.push('/register')
}

function forget() {
  openForgotDialog()
}

async function Login() {
  if (isLoading.value) return

  // 手机号校验：11位数字
  if (!/^[0-9]{11}$/.test(username.value)) {
    ElMessage.warning('请输入11位手机号')
    return
  }
  // 密码校验：不少于6位
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
// ===== 表单数据 =====
const registerForm = reactive({
  phone: '',
  verifyCode: '',
  password: '',
  passwordAgain: '',
})

// const isLoading = ref(false)
// const countdown = ref(0)
// let timer = null

// ===== 发送验证码 =====
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
      if (countdown.value <= 0) {
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
  const password = registerForm.password
  const passwordAgain = registerForm.passwordAgain
  const verifyCode = registerForm.verifyCode

  if (!phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  if (!verifyCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!password) {
    ElMessage.warning('请设置密码')
    return
  }
  if (password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  if (password !== passwordAgain) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (isLoading.value) return
  isLoading.value = true

  try {
    await register({
      phone,
      verifyCode,
      password,
    })
    ElMessage.success('注册成功！')
    // 注册成功后自动登录并跳转到首页
    const userStore = useUserStore()
    const loginSuccess = await userStore.login(phone, password)
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
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <div class="login-page">
    <!-- ===== 背景装饰：柔和光斑 + 波纹线 ===== -->
    <div class="bg-layer" aria-hidden="true">
      <div class="blob blob-a"></div>
      <div class="blob blob-b"></div>
      <svg class="bg-waves" viewBox="0 0 1440 900" preserveAspectRatio="xMidYMid slice">
        <g fill="none" stroke="#a9cdf5" stroke-opacity="0.28" stroke-width="1.1">
          <path d="M-80 120 C 160 40, 360 200, 620 120 S 1020 20, 1280 120 S 1600 200, 1760 100" />
          <path d="M-80 190 C 160 110, 360 270, 620 190 S 1020 90, 1280 190 S 1600 270, 1760 170" />
          <path d="M-80 260 C 160 180, 360 340, 620 260 S 1020 160, 1280 260 S 1600 340, 1760 240" />
          <path d="M-40 700 C 200 620, 420 800, 700 700 S 1100 600, 1380 700 S 1660 800, 1820 700" />
          <path d="M-40 770 C 200 690, 420 870, 700 770 S 1100 670, 1380 770 S 1660 870, 1820 770" />
          <path d="M-40 840 C 200 760, 420 940, 700 840 S 1100 740, 1380 840 S 1660 940, 1820 840" />
        </g>
        <g fill="none" stroke="#8fc0f2" stroke-opacity="0.18" stroke-width="1.1">
          <path d="M1180 40 C 1180 140, 1300 140, 1300 40" />
          <path d="M1120 40 C 1120 180, 1360 180, 1360 40" />
          <path d="M1060 40 C 1060 220, 1420 220, 1420 40" />
        </g>
      </svg>
    </div>

    <!-- ===== 忘记密码弹框 ===== -->
    <el-dialog
      v-model="forgotDialogVisible"
      title="重置密码"
      width="460px"
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
        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入绑定的邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item label="验证码" prop="verifyCode">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input
              v-model="forgotForm.verifyCode"
              placeholder="请输入6位验证码"
              prefix-icon="Key"
              maxlength="6"
              style="flex: 1"
              @keyup.enter="submitForgotPassword"
            />
            <el-button :disabled="countdown > 0" @click="sendVerifyCode" style="flex-shrink: 0">
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 新密码 -->
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

        <!-- 确认密码 -->
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

        <!-- 提示信息 -->
        <div style="color: #999; font-size: 13px; padding-left: 80px; margin-bottom: 10px">
          <el-icon><InfoFilled /></el-icon>
          验证码将发送到您的绑定邮箱，请及时查收
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeForgotDialog">取消</el-button>
          <el-button type="primary" :loading="forgotLoading" @click="submitForgotPassword">
            确认重置
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- ===== 登录卡片 ===== -->
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <section class="brand-panel">
        <div class="brand-top">
          <div class="brand-logo">
            <el-icon :size="20"><Management /></el-icon>
          </div>
          <span class="brand-name">朝夕护行</span>
          <span class="brand-tag">智慧养老监护平台</span>
        </div>

        <!-- 守护插画（内联 SVG，无外部依赖） -->
        <div class="brand-illustration">
          <svg viewBox="0 0 420 300" role="img" aria-label="智慧守护插画">
            <defs>
              <linearGradient id="shieldFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#ffffff" />
                <stop offset="1" stop-color="#eaf4ff" />
              </linearGradient>
              <linearGradient id="heartFill" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stop-color="#8fc4ff" />
                <stop offset="1" stop-color="#4a90e2" />
              </linearGradient>
            </defs>

            <!-- 背景圆 -->
            <circle cx="214" cy="150" r="116" fill="#ffffff" fill-opacity="0.55" />
            <circle cx="214" cy="150" r="116" fill="none" stroke="#bcdcff" stroke-opacity="0.8" stroke-dasharray="3 7" />

            <!-- 守护盾牌 -->
            <path
              d="M214 62 L286 92 V168 C286 210 254 240 214 252 C174 240 142 210 142 168 V92 Z"
              fill="url(#shieldFill)"
              stroke="#5d9fe0"
              stroke-width="2.4"
              stroke-linejoin="round"
            />

            <!-- 心形 -->
            <path
              d="M214 186 C 198 172, 186 162, 186 148 C 186 138, 194 132, 202 132 C 208 132, 212 136, 214 140 C 216 136, 220 132, 226 132 C 234 132, 242 138, 242 148 C 242 162, 230 172, 214 186 Z"
              fill="url(#heartFill)"
              opacity="0.9"
            />

            <!-- 心跳曲线 -->
            <polyline
              points="152,158 176,158 188,138 200,178 212,150 224,158 276,158"
              fill="none"
              stroke="#4a90e2"
              stroke-width="3"
              stroke-linecap="round"
              stroke-linejoin="round"
              opacity="0.85"
            />

            <!-- 小屋（居家守护） -->
            <g transform="translate(52,186)">
              <path d="M18 44 V18 L40 2 L62 18 V44 Z" fill="#ffffff" stroke="#7fb2f0" stroke-width="2" stroke-linejoin="round" />
              <path d="M32 44 V30 H46 V44" fill="none" stroke="#7fb2f0" stroke-width="2" stroke-linejoin="round" />
              <path d="M10 20 L40 0 L70 20" fill="none" stroke="#7fb2f0" stroke-width="2" stroke-linecap="round" />
            </g>

            <!-- 长者与手杖 -->
            <g transform="translate(322,152)" stroke="#7fb2f0" stroke-width="2" stroke-linecap="round" fill="none">
              <circle cx="0" cy="0" r="10.5" fill="#ffffff" />
              <path d="M-7 20 Q0 14 7 20" />
              <path d="M0 20 V38" />
              <path d="M0 38 L-7 54" />
              <path d="M0 38 L7 54" />
              <path d="M12 22 Q20 22 20 30 V56" stroke="#4a90e2" stroke-width="2.4" />
              <path d="M-4 18 L4 22" opacity="0.7" />
            </g>

            <!-- 铃铛（告警提醒） -->
            <g transform="translate(96,74)" stroke="#7fb2f0" stroke-width="2" fill="#ffffff" stroke-linejoin="round">
              <path d="M0 18 C 0 6, 8 0, 14 0 C 20 0, 28 6, 28 18 L 30 24 H -2 Z" />
              <path d="M10 27 H18" stroke-linecap="round" />
              <path d="M14 -6 V-1" stroke-linecap="round" />
            </g>

            <!-- 星芒点缀 -->
            <g fill="#9cc8f5" fill-opacity="0.9">
              <path d="M60 62 l4 -8 4 8 8 4 -8 4 -4 8 -4 -8 -8 -4 z" />
              <path d="M344 60 l3 -6 3 6 6 3 -6 3 -3 6 -3 -6 -6 -3 z" />
              <path d="M372 226 l3 -6 3 6 6 3 -6 3 -3 6 -3 -6 -6 -3 z" />
              <path d="M48 232 l2.5 -5 2.5 5 5 2.5 -5 2.5 -2.5 5 -2.5 -5 -5 -2.5 z" />
            </g>

            <!-- 底部弧线 -->
            <path d="M120 276 Q 214 300 308 276" fill="none" stroke="#cfe4fb" stroke-width="2" stroke-dasharray="4 8" stroke-linecap="round" />
          </svg>
        </div>

        <div class="brand-copy">
          <h2>让每一次守护，都恰好及时</h2>
          <p>多模态智能感知，跌倒即时报响，守护长者安享每一天</p>
        </div>

        <ul class="brand-points">
          <li>跌倒智能识别</li>
          <li>体征数据监测</li>
          <li>分级告警联动</li>
        </ul>
      </section>

      <!-- 右侧表单区 -->
      <section class="form-panel">
        <!-- ===== 登录 ===== -->
        <div v-if="turn" class="form-inner">
          <header class="form-head">
            <h1>欢迎回来</h1>
            <p>请登录您的朝夕护行守护账号</p>
          </header>

          <div class="field">
            <label class="field-label">手机号</label>
            <el-input
              v-model="username"
              size="large"
              placeholder="请输入手机号"
              :disabled="isLoading"
              @keyup.enter="Login()"
            >
              <template #prefix>
                <el-icon><UserFilled /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="field">
            <label class="field-label">密码</label>
            <el-input
              v-model="password"
              size="large"
              type="password"
              show-password
              placeholder="请输入密码"
              :disabled="isLoading"
              @keyup.enter="Login()"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="field">
            <label class="field-label">验证码</label>
            <div class="field-row">
              <el-input
                v-model="verifyCode"
                size="large"
                placeholder="请输入验证码"
                :disabled="isLoading"
                @keyup.enter="Login()"
              >
                <template #prefix>
                  <el-icon><Stamp /></el-icon>
                </template>
              </el-input>
              <div class="captcha">
                <svg viewBox="0 0 96 42" role="img" aria-label="验证码">
                  <rect width="96" height="42" rx="8" fill="#eef6ff" />
                  <g font-family="Georgia, 'Times New Roman', serif" font-weight="700" font-size="20" fill="#4a90e2" opacity="0.9">
                    <text x="10" y="28" transform="rotate(-12 10 28)">7</text>
                    <text x="30" y="30" transform="rotate(9 30 30)" fill="#e08a3c">k</text>
                    <text x="52" y="27" transform="rotate(-6 52 27)" fill="#5aa469">3</text>
                    <text x="70" y="31" transform="rotate(14 70 31)" fill="#c9618d">p</text>
                  </g>
                  <g stroke="#b9d6f5" stroke-width="1" opacity="0.8">
                    <path d="M6 34 Q 30 20 56 32 T 92 26" fill="none" />
                    <path d="M4 12 Q 34 24 60 10 T 94 18" fill="none" />
                    <circle cx="16" cy="8" r="1.4" fill="#b9d6f5" stroke="none" />
                    <circle cx="78" cy="34" r="1.4" fill="#b9d6f5" stroke="none" />
                  </g>
                </svg>
              </div>
            </div>
          </div>

          <div class="workOther">
            <div class="remember">
              <el-switch
                v-model="value2"
                class="ml-2"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              />记住我
            </div>
            <div class="forget">
              <a href="javascript:void(0)" @click="forget()">忘记密码？</a>
              <a href="javascript:void(0)" @click="turnRegister()">注册</a>
            </div>
          </div>

          <div class="submit">
            <button class="submit-btn" @click="Login()" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </div>

          <p class="form-foot">登录即表示同意平台安全使用规范 · 数据仅用于长者健康监护</p>
        </div>

        <!-- ===== 注册 ===== -->
        <div v-else class="form-inner">
          <header class="form-head">
            <h1>创建账号</h1>
            <p>注册后即可开启 24 小时智能守护</p>
          </header>

          <div class="field">
            <label class="field-label">手机号</label>
            <el-input
              v-model="registerForm.phone"
              size="large"
              placeholder="请输入手机号"
              maxlength="11"
              :disabled="isLoading"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><UserFilled /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="field">
            <label class="field-label">验证码</label>
            <div class="field-row">
              <el-input
                v-model="registerForm.verifyCode"
                size="large"
                placeholder="请输入验证码"
                maxlength="6"
                :disabled="isLoading"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <el-icon><Stamp /></el-icon>
                </template>
              </el-input>
              <el-button
                class="code-btn"
                :disabled="countdown > 0 || isLoading"
                @click="sendVerifyCodeRegister"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </div>

          <div class="field">
            <label class="field-label">密码</label>
            <el-input
              v-model="registerForm.password"
              size="large"
              type="password"
              show-password
              placeholder="请设置密码"
              :disabled="isLoading"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="field">
            <label class="field-label">确认密码</label>
            <el-input
              v-model="registerForm.passwordAgain"
              size="large"
              type="password"
              show-password
              placeholder="请确认密码"
              :disabled="isLoading"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="workOther">
            <div class="return">
              <a href="javascript:void(0)" @click="turnLogin()">返回登录</a>
            </div>
          </div>

          <div class="submit">
            <button class="submit-btn" @click="handleRegister" :disabled="isLoading">
              {{ isLoading ? '注册中...' : '注册' }}
            </button>
          </div>

          <p class="form-foot">注册即表示同意平台安全使用规范 · 数据仅用于长者健康监护</p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ================================================================
   全局
   ================================================================ */
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #eef5ff 0%, #e6f0fd 46%, #f4f9ff 100%);
  font-family:
    'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}

/* ===== 背景装饰 ===== */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.55;
}

.blob-a {
  width: 520px;
  height: 520px;
  left: -140px;
  top: -160px;
  background: radial-gradient(circle, #cfe4ff 0%, rgba(207, 228, 255, 0) 68%);
}

.blob-b {
  width: 640px;
  height: 640px;
  right: -200px;
  bottom: -240px;
  background: radial-gradient(circle, #d7e9ff 0%, rgba(215, 233, 255, 0) 68%);
}

.bg-waves {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

/* ================================================================
   卡片
   ================================================================ */
.login-card {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 0.92fr 1fr;
  width: 1000px;
  max-width: 92vw;
  min-height: 560px;
  border-radius: 26px;
  background: #ffffff;
  box-shadow:
    0 26px 70px rgba(31, 88, 176, 0.14),
    0 6px 20px rgba(31, 88, 176, 0.06);
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 34px 36px 30px;
  background:
    radial-gradient(120% 90% at 8% 0%, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0) 58%),
    linear-gradient(160deg, #dcecff 0%, #eaf4ff 52%, #f6fbff 100%);
  border-right: 1px solid rgba(160, 198, 240, 0.28);
}

.brand-panel::after {
  content: '';
  position: absolute;
  right: -1px;
  top: 12%;
  bottom: 12%;
  width: 1px;
  background: linear-gradient(180deg, rgba(160, 198, 240, 0), rgba(160, 198, 240, 0.6), rgba(160, 198, 240, 0));
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, #5aa9f7 0%, #2f7fe0 100%);
  box-shadow: 0 8px 18px rgba(47, 127, 224, 0.32);
}

.brand-name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 2px;
  color: #1f4e8c;
}

.brand-tag {
  margin-left: auto;
  font-size: 12px;
  color: #5d82ad;
  letter-spacing: 1px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(160, 198, 240, 0.5);
}

.brand-illustration {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 0 2px;
}

.brand-illustration svg {
  width: 100%;
  max-width: 360px;
  height: auto;
}

.brand-copy h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #1f4e8c;
}

.brand-copy p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.75;
  color: #5d82ad;
}

.brand-points {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 18px 0 0;
  padding: 0;
  list-style: none;
}

.brand-points li {
  font-size: 12.5px;
  color: #3a6ea8;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(160, 198, 240, 0.55);
}

/* ===== 右侧表单区 ===== */
.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 46px 56px 38px;
  background: #fff;
}

.form-inner {
  width: 100%;
}

.form-head {
  margin-bottom: 26px;
}

.form-head h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #16304f;
}

.form-head p {
  margin: 0;
  font-size: 13.5px;
  color: #8aa2bd;
  letter-spacing: 0.4px;
}

.field {
  margin-bottom: 16px;
}

.field-label {
  display: block;
  margin-bottom: 7px;
  font-size: 13px;
  font-weight: 600;
  color: #5a7797;
  letter-spacing: 0.5px;
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

.captcha {
  flex: 0 0 96px;
  width: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2edf9;
  background: #f4f9ff;
}

.captcha svg {
  width: 100%;
  height: 42px;
  display: block;
}

.code-btn {
  flex: 0 0 112px;
  height: 44px;
  border-radius: 12px;
  font-size: 13px;
  border: 1px solid #cfe3fb;
  background: #f4f9ff;
  color: #2f7fe0;
  transition: all 0.2s ease;
}

.code-btn:hover:not(:disabled) {
  background: #2f7fe0;
  border-color: #2f7fe0;
  color: #fff;
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== 输入框润色 ===== */
.form-panel :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f7faff;
  box-shadow: 0 0 0 1px #e2edf9 inset;
  padding: 2px 14px;
  transition: all 0.2s ease;
}

.form-panel :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c3dcf7 inset;
  background: #fff;
}

.form-panel :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow:
    0 0 0 1px #5aa9f7 inset,
    0 6px 16px rgba(90, 169, 247, 0.16);
}

.form-panel :deep(.el-input__inner) {
  height: 42px;
  font-size: 14px;
  color: #23405f;
}

.form-panel :deep(.el-input__prefix) {
  color: #8fb6e4;
}

.form-panel :deep(.el-input__prefix .el-icon) {
  font-size: 17px;
}

/* ===== 记住我 / 忘记密码 ===== */
.workOther {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 14px;
  font-size: 13px;
  color: #6c88a6;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.remember :deep(.el-switch) {
  --el-switch-on-color: #22c55e;
  --el-switch-off-color: #f87171;
}

.forget a {
  color: #2f7fe0;
  margin-left: 14px;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s ease;
}

.forget a:hover {
  color: #1d64b8;
  text-decoration: underline;
}

.return a {
  color: #2f7fe0;
  cursor: pointer;
  text-decoration: none;
}

.return a:hover {
  color: #1d64b8;
  text-decoration: underline;
}

/* ===== 提交按钮 ===== */
.submit {
  margin-top: 6px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  text-indent: 6px;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, #4e9cf5 0%, #2f7fe0 100%);
  box-shadow: 0 12px 26px rgba(47, 127, 224, 0.3);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    opacity 0.18s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(47, 127, 224, 0.36);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 8px 18px rgba(47, 127, 224, 0.28);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
}

.form-foot {
  margin: 18px 0 0;
  text-align: center;
  font-size: 11.5px;
  line-height: 1.6;
  color: #a9bdd4;
  letter-spacing: 0.3px;
}

/* ===== 弹框样式 ===== */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ================================================================
   响应式
   ================================================================ */
@media (max-width: 980px) {
  .login-card {
    grid-template-columns: 1fr;
    max-width: 92vw;
    min-height: auto;
  }

  .brand-panel {
    padding: 24px 26px 12px;
    border-right: none;
    border-bottom: 1px solid rgba(160, 198, 240, 0.28);
  }

  .brand-panel::after {
    display: none;
  }

  .brand-illustration {
    display: none;
  }

  .brand-copy h2 {
    font-size: 19px;
  }

  .brand-points {
    margin-top: 12px;
  }

  .form-panel {
    padding: 28px 26px 30px;
  }
}

@media (max-width: 480px) {
  .brand-tag {
    display: none;
  }

  .form-head h1 {
    font-size: 24px;
  }

  .captcha {
    flex-basis: 82px;
    width: 82px;
  }

  .code-btn {
    flex-basis: 98px;
    font-size: 12px;
  }
}
</style>
