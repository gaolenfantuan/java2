<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminLogin } from '../../api/admin.js'
import { saveAdmin, isAdminLoggedIn } from '../../utils/adminAuth.js'

const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const showPwd = ref(false)

/**
 * 登录后跳哪里：
 *   只有以 /admin 开头且不是登录页本身的地址才允许，防止被构造链接跳到外站
 */
function safeRedirect() {
  const r = route.query.redirect
  if (typeof r === 'string' && r.startsWith('/admin') && !r.startsWith('/admin/login')) {
    return r
  }
  return '/admin'
}

async function submit() {
  error.value = ''
  if (!username.value.trim()) {
    error.value = '请输入管理员账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const data = await adminLogin(username.value.trim(), password.value)
    saveAdmin(data)
    router.replace(safeRedirect())
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function goHome() {
  router.push('/home')
}

onMounted(() => {
  // 已经登录过就直接进后台，不用再输一遍
  if (isAdminLoggedIn()) router.replace(safeRedirect())
})
</script>

<template>
  <div class="admin-login">
    <!-- 背景漂浮装饰 -->
    <div class="bg">
      <span v-for="i in 14" :key="i" class="dot" :style="{
        left: (i * 7.3) % 100 + 'vw',
        fontSize: 12 + ((i * 5) % 20) + 'px',
        animationDuration: 9 + ((i * 3) % 9) + 's',
        animationDelay: (i % 10) + 's'
      }">{{ ['🎀', '💗', '✨', '🌸', '🍰'][i % 5] }}</span>
    </div>

    <div class="card">
      <div class="head">
        <div class="bow">🎀</div>
        <h1>Hello Kitty 管理后台</h1>
        <p>只有管理员可以进入哦 · Admin Only</p>
      </div>

      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span class="ico">👤</span>
          <input
            v-model="username"
            class="input"
            type="text"
            placeholder="管理员账号"
            autocomplete="username"
          />
        </label>

        <label class="field">
          <span class="ico">🔒</span>
          <input
            v-model="password"
            class="input"
            :type="showPwd ? 'text' : 'password'"
            placeholder="登录密码"
            autocomplete="current-password"
          />
          <button type="button" class="eye" :title="showPwd ? '隐藏密码' : '显示密码'" @click="showPwd = !showPwd">
            {{ showPwd ? '🙈' : '👁️' }}
          </button>
        </label>

        <p v-if="error" class="error">⚠️ {{ error }}</p>

        <button class="submit" type="submit" :disabled="loading">
          {{ loading ? '正在登录…' : '登 录 后 台' }}
        </button>
      </form>

      <div class="hint">
        <span>演示账号 <b>admin</b> / <b>admin123</b></span>
      </div>

      <div class="foot">
        <button class="link" @click="goHome">← 返回前台首页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(160deg, #ffd6e8 0%, #ffc2dd 38%, #ffe9f3 100%);
  box-sizing: border-box;
}

/* 背景 */
.bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.dot {
  position: absolute;
  bottom: -40px;
  color: #ff8ab8;
  opacity: 0.32;
  animation: floatUp 10s linear infinite;
  user-select: none;
}
@keyframes floatUp {
  0% { transform: translateY(0) rotate(0deg); }
  100% { transform: translateY(-110vh) rotate(360deg); }
}

/* 卡片 */
.card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 396px;
  padding: 30px 30px 22px;
  background: rgba(255, 255, 255, 0.94);
  border: 2px solid #ffd9e8;
  border-radius: 28px;
  box-shadow: 0 26px 60px rgba(214, 82, 138, 0.28);
  backdrop-filter: blur(6px);
}

.head {
  text-align: center;
  margin-bottom: 22px;
}
.bow {
  font-size: 34px;
  line-height: 1;
  filter: drop-shadow(0 4px 8px rgba(255, 79, 139, 0.35));
}
.head h1 {
  margin: 8px 0 5px;
  font-size: 18px;
  color: #ff4f8b;
  letter-spacing: 0.5px;
}
.head p {
  margin: 0;
  font-size: 11.5px;
  color: #c98fae;
  letter-spacing: 0.6px;
}

/* 表单 */
.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field {
  position: relative;
  display: flex;
  align-items: center;
}
.field .ico {
  position: absolute;
  left: 14px;
  font-size: 14px;
  opacity: 0.8;
  pointer-events: none;
}
.input {
  width: 100%;
  padding: 12px 42px 12px 40px;
  border: 2px solid #ffd3e5;
  border-radius: 50px;
  background: #fff8fb;
  color: #7a4b5e;
  font-size: 13.5px;
  font-family: inherit;
  outline: none;
  transition: 0.2s;
  box-sizing: border-box;
}
.input::placeholder {
  color: #d9a8bd;
}
.input:focus {
  border-color: #ff4f8b;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255, 79, 139, 0.14);
}
.eye {
  position: absolute;
  right: 8px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  line-height: 1;
}
.eye:hover {
  background: #fff0f6;
}

.error {
  margin: 0;
  padding: 8px 13px;
  border-radius: 12px;
  background: #fff0f4;
  border: 1px solid #ffd0de;
  color: #d92a62;
  font-size: 12px;
  line-height: 1.5;
}

.submit {
  margin-top: 4px;
  padding: 12px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 1px;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 10px 22px rgba(255, 79, 139, 0.38);
  transition: 0.2s;
}
.submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(255, 79, 139, 0.46);
}
.submit:disabled {
  opacity: 0.7;
  cursor: default;
}

.hint {
  margin-top: 14px;
  padding: 8px 12px;
  border-radius: 12px;
  background: #fff5f9;
  border: 1px dashed #ffd3e5;
  text-align: center;
  font-size: 11.5px;
  color: #c98fae;
}
.hint b {
  color: #ff4f8b;
}

.foot {
  margin-top: 10px;
  text-align: center;
}
.link {
  border: none;
  background: transparent;
  color: #c98fae;
  font-size: 12px;
  cursor: pointer;
  font-family: inherit;
  padding: 4px 8px;
}
.link:hover {
  color: #ff4f8b;
  text-decoration: underline;
}
</style>
