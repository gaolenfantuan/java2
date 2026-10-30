<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import KittyAvatar from './KittyAvatar.vue'
import { register, checkUsername } from '../api/auth.js'

const router = useRouter()

const username = ref('')
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const agree = ref(false)
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

async function handleRegister() {
  // 前端校验
  if (username.value.trim().length < 2) {
    return showToastMsg('😿 用户名至少 2 个字符哦~')
  }
  if (password.value.length < 4) {
    return showToastMsg('😿 密码至少 4 位哦~')
  }
  if (password.value !== confirmPassword.value) {
    return showToastMsg('😿 两次输入的密码不一致~')
  }
  if (!agree.value) {
    return showToastMsg('😿 要先同意用户协议才能注册哦~')
  }

  // 真正请求后端：POST /api/auth/register
  submitting.value = true
  const name = username.value.trim()
  try {
    await register({
      username: name,
      nickname: nickname.value.trim(),
      password: password.value
    })
    showToastMsg('🎉 注册成功，快去登录吧~')
    // 1.2 秒后回到登录页，并把用户名带过去自动填好
    setTimeout(() => router.replace({ path: '/login', query: { username: name } }), 1200)
  } catch (e) {
    showToastMsg('😿 ' + e.message)
  } finally {
    submitting.value = false
  }
}

// 用户名输入框失焦时，问一下后端这个用户名是不是被占用了
async function handleUsernameBlur() {
  const name = username.value.trim()
  if (name.length < 2) return
  try {
    const taken = await checkUsername(name)
    if (taken) showToastMsg('😿 这个用户名已经被占用啦~')
  } catch (e) {
    // 校验接口出错不打扰用户，真正提交时后端还会再校验一次
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

    <!-- 注册卡片 -->
    <div class="card">
      <KittyAvatar />

      <h1>注册新账号</h1>
      <p class="sub">和 Kitty 一起开启甜甜的新旅程 ♡</p>

      <form @submit.prevent="handleRegister">
        <div class="input-box">
          <span class="icon">👤</span>
          <input v-model="username" type="text" placeholder="用户名" required @blur="handleUsernameBlur" />
        </div>
        <div class="input-box">
          <span class="icon">🌸</span>
          <input v-model="nickname" type="text" placeholder="昵称（选填）" />
        </div>
        <div class="input-box">
          <span class="icon">🔒</span>
          <input v-model="password" type="password" placeholder="密码（至少 4 位）" required />
        </div>
        <div class="input-box">
          <span class="icon">🔑</span>
          <input v-model="confirmPassword" type="password" placeholder="确认密码" required />
        </div>

        <div class="agree">
          <label>
            <input v-model="agree" type="checkbox" />
            我已阅读并同意 <a href="#">《用户协议》</a>
          </label>
        </div>

        <button type="submit" class="register-btn" :disabled="submitting">
          {{ submitting ? '注册中...' : '注 册' }}
        </button>
      </form>

      <p class="footer">已有账号？<router-link to="/login">返回登录</router-link></p>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #ffe0ee 0%, #ffc6de 40%, #fff0f6 100%);
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

/* 注册卡片 */
.card {
  position: relative;
  z-index: 2;
  width: 360px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 30px;
  padding: 36px 34px 30px;
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

.input-box { position: relative; margin-bottom: 14px; }
.input-box .icon {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
  font-size: 16px;
}
.input-box input {
  width: 100%;
  padding: 11px 14px 11px 42px;
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

.agree {
  text-align: left;
  font-size: 12px;
  color: #c77a9c;
  margin: 2px 4px 16px;
}
.agree label { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.agree a { color: #ff4f8b; text-decoration: none; }
.agree a:hover { text-decoration: underline; }

.register-btn {
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
.register-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 22px rgba(255, 79, 139, 0.5); }
.register-btn:active { transform: translateY(0); }
.register-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.footer { margin-top: 16px; font-size: 12px; color: #c77a9c; }
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
