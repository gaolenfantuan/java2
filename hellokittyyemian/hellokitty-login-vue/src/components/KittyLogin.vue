<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import KittyAvatar from './KittyAvatar.vue'
import { login } from '../api/auth.js'
import { saveUser } from '../utils/auth.js'

const router = useRouter()
const route = useRoute()

// 注册成功跳回来时，自动带上刚注册的用户名（query.username）
// 被路由守卫赶回来时，query.redirect 记着原本想去的地址
const username = ref(route.query.username || '')
const password = ref('')
const rememberMe = ref(false)
const toast = ref('')
const showToast = ref(false)
const submitting = ref(false)

// 漂浮装饰
const decorations = ref([])

onMounted(() => {
  const icons = ['💗', '🎀', '✨', '💖', '🌸']
  for (let i = 0; i < 18; i++) {
    decorations.value.push({
      icon: icons[Math.floor(Math.random() * icons.length)],
      left: Math.random() * 100,
      size: 14 + Math.random() * 22,
      duration: 7 + Math.random() * 8,
      delay: Math.random() * 10
    })
  }
})

function showToastMsg(msg) {
  toast.value = msg
  showToast.value = true
  setTimeout(() => (showToast.value = false), 2500)
}

async function handleLogin() {
  // 前端简单校验，减少无意义的请求
  if (password.value.length < 4) {
    return showToastMsg('😿 密码至少 4 位哦~')
  }

  submitting.value = true
  try {
    // 真正请求后端：POST /api/auth/login
    const data = await login({
      username: username.value.trim(),
      password: password.value
    })

    // 想"记住我"就把用户信息存到 localStorage，否则只存 sessionStorage
    saveUser(data, rememberMe.value)

    showToastMsg(`🎀 登录成功，欢迎回来 ${data.nickname}~`)

    // 停顿一下让提示能看到，然后跳首页
    // 如果是从受保护页面被赶回来的，优先回原本那一页
    const target = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    setTimeout(() => router.replace(target), 600)
  } catch (e) {
    showToastMsg('😿 ' + e.message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <!-- 背景漂浮装饰 -->
    <div
      v-for="(d, i) in decorations"
      :key="i"
      class="heart"
      :style="{
        left: d.left + 'vw',
        fontSize: d.size + 'px',
        animationDuration: d.duration + 's',
        animationDelay: d.delay + 's'
      }"
    >
      {{ d.icon }}
    </div>

    <!-- 提示 -->
    <div class="toast" :class="{ show: showToast }">{{ toast }}</div>

    <!-- 登录卡片 -->
    <div class="card">
      <KittyAvatar />

      <h1>Hello Kitty</h1>
      <p class="sub">欢迎回来，今天也要元气满满哦 ♡</p>

      <form @submit.prevent="handleLogin">
        <div class="input-box">
          <span class="icon">👤</span>
          <input v-model="username" type="text" placeholder="用户名" required />
        </div>
        <div class="input-box">
          <span class="icon">🔒</span>
          <input v-model="password" type="password" placeholder="密码" required />
        </div>

        <div class="options">
          <label>
            <input v-model="rememberMe" type="checkbox" /> 记住我
          </label>
          <a href="#">忘记密码？</a>
        </div>

        <button type="submit" class="login-btn" :disabled="submitting">
          {{ submitting ? '登录中...' : '登 录' }}
        </button>
      </form>

      <p class="footer">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #ffd6e8 0%, #ffb6d5 40%, #ffe9f3 100%);
  overflow: hidden;
  position: relative;
}

/* 漂浮的爱心背景 */
.heart {
  position: absolute;
  color: #ff8ab8;
  opacity: 0.35;
  animation: floatUp 10s linear infinite;
  user-select: none;
}
@keyframes floatUp {
  0% { transform: translateY(110vh) rotate(0deg); }
  100% { transform: translateY(-10vh) rotate(360deg); }
}

/* 登录卡片 */
.card {
  position: relative;
  z-index: 2;
  width: 360px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 30px;
  padding: 40px 34px 34px;
  box-shadow: 0 15px 40px rgba(255, 105, 160, 0.35);
  text-align: center;
  border: 3px solid #ffc0da;
}

h1 {
  color: #ff4f8b;
  font-size: 22px;
  margin: 8px 0 4px;
  letter-spacing: 2px;
}
.sub { color: #c77a9c; font-size: 13px; margin-bottom: 22px; }

.input-box { position: relative; margin-bottom: 16px; }
.input-box .icon {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
  font-size: 16px;
}
.input-box input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  border: 2px solid #ffd3e5;
  border-radius: 50px;
  font-size: 14px;
  outline: none;
  background: #fff8fb;
  color: #7a4b5e;
  transition: 0.25s;
}
.input-box input:focus {
  border-color: #ff4f8b;
  box-shadow: 0 0 0 4px rgba(255, 79, 139, 0.15);
}

.options {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 12px; color: #c77a9c; margin: 4px 4px 20px;
}
.options label { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.options a { color: #ff4f8b; text-decoration: none; }
.options a:hover { text-decoration: underline; }

.login-btn {
  width: 100%;
  padding: 13px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  cursor: pointer;
  transition: 0.25s;
  box-shadow: 0 6px 16px rgba(255, 79, 139, 0.4);
}
.login-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 22px rgba(255, 79, 139, 0.5); }
.login-btn:active { transform: translateY(0); }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.footer { margin-top: 18px; font-size: 12px; color: #c77a9c; }
.footer a { color: #ff4f8b; text-decoration: none; font-weight: bold; }

/* 提示 */
.toast {
  position: fixed; top: 30px; left: 50%; transform: translateX(-50%) translateY(-80px);
  background: #ff4f8b; color: #fff;
  padding: 12px 26px; border-radius: 50px;
  font-size: 14px; z-index: 99;
  transition: 0.4s ease;
  box-shadow: 0 8px 20px rgba(255, 79, 139, 0.4);
}
.toast.show { transform: translateX(-50%) translateY(0); }
</style>
