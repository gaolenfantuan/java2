<template>
  <div class="page">
    <!-- 漂浮爱心 -->
    <div
      v-for="(h, i) in hearts"
      :key="i"
      class="heart"
      :style="h"
    >{{ h.emoji }}</div>

    <!-- 登录 / 注册卡片 -->
    <div class="card" :class="{ wide: mode === 'register' }">
      <h1>甜心物语</h1>
      <p class="subtitle">{{ mode === 'login' ? '♡ 欢迎回来，今天也要甜甜的哦 ♡' : '♡ 加入我们，开启甜蜜之旅 ♡' }}</p>

      <!-- 登录 / 注册切换标签 -->
      <div class="tabs">
        <button
          type="button"
          class="tab"
          :class="{ active: mode === 'login' }"
          @click="switchMode('login')"
        >登录</button>
        <button
          type="button"
          class="tab"
          :class="{ active: mode === 'register' }"
          @click="switchMode('register')"
        >注册</button>
        <span class="tab-slider" :class="{ right: mode === 'register' }"></span>
      </div>

      <Transition name="slide" mode="out-in">
        <!-- ===== 登录表单 ===== -->
        <form v-if="mode === 'login'" key="login" @submit.prevent="handleLogin">
          <div class="field">
            <span class="icon">🍭</span>
            <input
              type="text"
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>
          <div class="field">
            <span class="icon">💖</span>
            <input
              type="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </div>

          <div class="row">
            <label><input type="checkbox" v-model="loginForm.remember" /> 记住我</label>
            <a href="#" @click.prevent="tip = '🍰 悄悄告诉你：演示版密码找不回啦，重新注册一个吧~'">忘记密码？</a>
          </div>

          <button type="submit" class="btn">登 录</button>
        </form>

        <!-- ===== 注册表单 ===== -->
        <form v-else key="register" @submit.prevent="handleRegister">
          <div class="field">
            <span class="icon">🍭</span>
            <input
              type="text"
              v-model="regForm.username"
              placeholder="设置用户名（3~12 个字符）"
              autocomplete="username"
            />
          </div>
          <div class="field">
            <span class="icon">🍓</span>
            <input
              type="password"
              v-model="regForm.password"
              placeholder="设置密码（至少 6 位）"
              autocomplete="new-password"
            />
          </div>
          <div class="field">
            <span class="icon">💖</span>
            <input
              type="password"
              v-model="regForm.confirm"
              placeholder="再输入一次密码"
              autocomplete="new-password"
            />
          </div>

          <div class="row">
            <label><input type="checkbox" v-model="regForm.agree" /> 我已阅读并同意 <a href="#" @click.prevent>《甜甜条款》</a></label>
          </div>

          <button type="submit" class="btn">注 册</button>
        </form>
      </Transition>

      <p class="tip">{{ tip }}</p>

      <div class="footer">
        <template v-if="mode === 'login'">
          还没有账号？<a href="#" @click.prevent="switchMode('register')">加入我们 🍬</a>
        </template>
        <template v-else>
          已有账号？<a href="#" @click.prevent="switchMode('login')">直接登录 ✨</a>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const mode = ref('login')
const tip = ref('')

const loginForm = reactive({ username: '', password: '', remember: true })
const regForm = reactive({ username: '', password: '', confirm: '', agree: false })

/* ===== 账号存储（localStorage 演示用） ===== */
const USERS_KEY = 'sweet-anime-users'
const loadUsers = () => JSON.parse(localStorage.getItem(USERS_KEY) || '[]')
const saveUsers = (users) => localStorage.setItem(USERS_KEY, JSON.stringify(users))

function switchMode(m) {
  mode.value = m
  tip.value = ''
}

/* ===== 登录 ===== */
function handleLogin() {
  const u = loginForm.username.trim()
  const p = loginForm.password
  if (!u || !p) {
    tip.value = '🍥 先填写用户名和密码嘛~'
    return
  }
  const user = loadUsers().find((x) => x.username === u)
  if (!user) {
    tip.value = '🍩 这个账号还不存在哦，先去注册一个吧~'
    return
  }
  if (user.password !== p) {
    tip.value = '🍫 密码不对呀，再想想~'
    return
  }
  tip.value = `💗 登录成功！欢迎回来，${u} ~`
}

/* ===== 注册 ===== */
function handleRegister() {
  const u = regForm.username.trim()
  const p = regForm.password
  const c = regForm.confirm

  if (!u || !p || !c) {
    tip.value = '🍥 信息要填写完整哦~'
    return
  }
  if (u.length < 3 || u.length > 12) {
    tip.value = '🍡 用户名需要 3~12 个字符~'
    return
  }
  if (p.length < 6) {
    tip.value = '🍩 密码至少 6 位，保护好自己的小秘密~'
    return
  }
  if (p !== c) {
    tip.value = '🍫 两次输入的密码不一样呢~'
    return
  }
  if (!regForm.agree) {
    tip.value = '🍰 记得勾选同意《甜甜条款》哦~'
    return
  }

  const users = loadUsers()
  if (users.some((x) => x.username === u)) {
    tip.value = '🍮 这个用户名已经被抢走啦，换一个吧~'
    return
  }

  users.push({ username: u, password: p })
  saveUsers(users)

  tip.value = `🎉 注册成功！${u}，快去登录吧~`
  // 自动把用户名带到登录表单
  loginForm.username = u
  loginForm.password = ''
  regForm.username = regForm.password = regForm.confirm = ''
  regForm.agree = false
  setTimeout(() => switchMode('login'), 1200)
}

/* ===== 漂浮爱心数据 ===== */
const EMOJIS = ['💗', '💕', '🌸', '⭐', '🍬']
const hearts = Array.from({ length: 18 }, () => {
  const dur = 8 + Math.random() * 8
  return {
    emoji: EMOJIS[Math.floor(Math.random() * EMOJIS.length)],
    left: Math.random() * 100 + 'vw',
    fontSize: 14 + Math.random() * 18 + 'px',
    animationDuration: dur + 's',
    animationDelay: -Math.random() * dur + 's'
  }
})
</script>

<style>
/* 全局重置（非 scoped，作用于 html/body） */
* { margin: 0; padding: 0; box-sizing: border-box; }

html, body, #app {
  height: 100%;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  overflow: hidden;
}
</style>

<style scoped>
.page {
  height: 100%;
  background: #ffeef5 url("/bg.png") center/cover no-repeat fixed;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8vw;
}

/* ===== 漂浮爱心 ===== */
.heart {
  position: fixed;
  bottom: -30px;
  z-index: 1;
  pointer-events: none;
  animation: floatUp linear infinite;
  opacity: 0.7;
}
@keyframes floatUp {
  0%   { transform: translateY(0) scale(0.6) rotate(0deg); opacity: 0; }
  15%  { opacity: 0.8; }
  100% { transform: translateY(-110vh) scale(1.1) rotate(20deg); opacity: 0; }
}

/* ===== 登录/注册卡片 ===== */
.card {
  position: relative;
  z-index: 2;
  width: 380px;
  max-width: 92vw;
  padding: 42px 38px 34px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  border: 2px solid #ffd6e8;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 18px 50px rgba(255, 145, 190, 0.35);
  animation: cardIn 0.9s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(40px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 卡片顶部蝴蝶结装饰 */
.card::before {
  content: "🎀";
  position: absolute;
  top: -22px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 34px;
  filter: drop-shadow(0 4px 8px rgba(255, 120, 170, 0.4));
}

h1 {
  text-align: center;
  font-size: 27px;
  letter-spacing: 5px;
  background: linear-gradient(135deg, #ff6fa5, #ffa3c2);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 6px;
}
.subtitle {
  text-align: center;
  color: #d48aa8;
  font-size: 13px;
  letter-spacing: 2px;
  margin-bottom: 22px;
}

/* ===== 登录/注册切换标签 ===== */
.tabs {
  position: relative;
  display: flex;
  background: #ffe4ef;
  border-radius: 999px;
  padding: 4px;
  margin-bottom: 24px;
}
.tab {
  flex: 1;
  position: relative;
  z-index: 2;
  padding: 9px 0;
  border: none;
  background: none;
  border-radius: 999px;
  font-size: 14px;
  letter-spacing: 4px;
  color: #d48aa8;
  cursor: pointer;
  transition: color 0.3s ease;
}
.tab.active { color: #fff; font-weight: bold; }
.tab-slider {
  position: absolute;
  z-index: 1;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6fa5, #ffb0cd);
  box-shadow: 0 4px 12px rgba(255, 111, 165, 0.4);
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.tab-slider.right { transform: translateX(100%); }

/* 表单切换动画 */
.slide-enter-active, .slide-leave-active {
  transition: all 0.3s ease;
}
.slide-enter-from { opacity: 0; transform: translateX(24px); }
.slide-leave-to   { opacity: 0; transform: translateX(-24px); }

/* ===== 表单 ===== */
.field { position: relative; margin-bottom: 18px; }
.field input {
  width: 100%;
  padding: 14px 16px 14px 46px;
  border-radius: 999px;
  border: 2px solid #ffd6e8;
  background: #fff7fa;
  color: #8a5a70;
  font-size: 15px;
  outline: none;
  transition: all 0.25s ease;
}
.field input::placeholder { color: #d8a8bd; }
.field input:focus {
  border-color: #ff8fbf;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255, 143, 191, 0.18);
}
.field .icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 4px 22px;
  font-size: 13px;
  color: #c98ba4;
}
.row label { display: flex; align-items: center; gap: 6px; cursor: pointer; flex-wrap: wrap; }
.row input[type="checkbox"] { accent-color: #ff6fa5; }
.row a { color: #ff8fbf; text-decoration: none; }
.row a:hover { text-decoration: underline; }

.btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 999px;
  font-size: 16px;
  letter-spacing: 6px;
  font-weight: bold;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, #ff6fa5, #ffb0cd);
  box-shadow: 0 10px 24px rgba(255, 111, 165, 0.4);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.btn:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: 0 14px 30px rgba(255, 111, 165, 0.55);
}
.btn:active { transform: translateY(0); }

.footer {
  text-align: center;
  margin-top: 22px;
  font-size: 13px;
  color: #c98ba4;
}
.footer a { color: #ff6fa5; text-decoration: none; font-weight: bold; }
.footer a:hover { text-decoration: underline; }

.tip {
  text-align: center;
  margin-top: 14px;
  font-size: 12px;
  min-height: 16px;
  color: #ff6fa5;
}

/* 窄屏时卡片居中 */
@media (max-width: 720px) {
  .page { justify-content: center; padding-right: 0; }
}
</style>
