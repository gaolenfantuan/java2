<script setup>
import { ref, reactive, nextTick, computed } from 'vue'
import { login as loginApi, register as registerApi } from '../api'

const activeTab = ref('login')
const isRegister = computed(() => activeTab.value === 'register')

const username = ref('')
const nickname = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const remember = ref(false)
const agree = ref(false)
const showPassword = ref(false)
const showConfirm = ref(false)
const loading = ref(false)

const invalid = reactive({ user: false, nick: false, phone: false, pass: false, confirm: false, agree: false })
const shaking = reactive({ user: false, nick: false, phone: false, pass: false, confirm: false, agree: false })

const maskShow = ref(false)
const modalTitle = ref('登录成功啦！')
const modalText = ref('')

function fieldClass(key) {
  return { invalid: invalid[key], shake: shaking[key] }
}
function clearAll() {
  Object.keys(invalid).forEach(k => { invalid[k] = false; shaking[k] = false })
}
function clearError(key) { invalid[key] = false }

async function shake(key) {
  shaking[key] = false
  await nextTick()
  shaking[key] = true
  setTimeout(() => { shaking[key] = false }, 400)
}
function fail(key) {
  invalid[key] = true
  shake(key)
  return false
}
function check(key, ok) {
  if (!ok) fail(key)
  return ok
}
function openModal(title, text) {
  modalTitle.value = title
  modalText.value = text
  maskShow.value = true
}
function connectionError() {
  openModal('连接失败', '后端服务没启动，或者数据库还没连上。请先启动 Spring Boot 服务（端口 8080）～')
}

function switchTab(tab) {
  activeTab.value = tab
  clearAll()
  password.value = ''
  confirmPassword.value = ''
  showPassword.value = false
  showConfirm.value = false
  loading.value = false
}

async function submitLogin() {
  clearAll()
  const account = username.value.trim()
  const pwd = password.value
  let ok = true
  ok = check('user', account.length > 0) && ok
  ok = check('pass', pwd.length >= 6) && ok
  if (!ok) return

  loading.value = true
  try {
    const res = await loginApi({ username: account, password: pwd })
    if (res.code === 200) {
      localStorage.setItem('petToken', res.data.token)
      openModal('登录成功啦！', '欢迎回来，' + res.data.nickname + '！毛孩子们正等着你呢～')
    } else {
      openModal('登录失败', res.message || '账号或密码不对哦～')
    }
  } catch (e) {
    connectionError()
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  clearAll()
  const nick = nickname.value.trim()
  const tel = phone.value.trim()
  const pwd = password.value
  const confirm = confirmPassword.value
  let ok = true
  ok = check('nick', nick.length >= 2 && nick.length <= 12) && ok
  ok = check('phone', /^1[3-9]\d{9}$/.test(tel)) && ok
  ok = check('pass', pwd.length >= 6) && ok
  ok = check('confirm', confirm.length > 0 && confirm === pwd) && ok
  ok = check('agree', agree.value) && ok
  if (!ok) return

  loading.value = true
  try {
    // 后端用 username 字段存手机号，昵称单独存 nickname
    const res = await registerApi({ username: tel, nickname: nick, password: pwd })
    if (res.code === 200) {
      openModal('注册成功啦！', '欢迎加入，' + nick + '！已经帮你切回登录页啦～')
      switchTab('login')
      username.value = tel
    } else {
      openModal('注册失败', res.message || '注册失败，请稍后再试～')
    }
  } catch (e) {
    connectionError()
  } finally {
    loading.value = false
  }
}

function socialLogin(way) {
  openModal('演示提示', '正在唤起 ' + way + ' 授权，演示页面不会真的跳转哦～')
}
function forgot() {
  openModal('忘记密码', '重置链接已发送到你的邮箱（演示效果）～')
}
</script>

<template>
  <section class="panel login">
    <div class="tag">{{ isRegister ? '🎉 新用户送爪印徽章' : 'NEW! 领养申请通道开启' }}</div>
    <h1>{{ isRegister ? '加入毛茸茸大家庭！' : '欢迎回来！' }}</h1>
    <p class="sub">{{ isRegister ? '填好资料，开启你的领养之旅 ~' : '登录账号，继续你的领养之旅 ~' }}</p>

    <div class="tabs">
      <div class="tab" :class="{ active: !isRegister }" @click="switchTab('login')">🐾 登录</div>
      <div class="tab" :class="{ active: isRegister }" @click="switchTab('register')">✨ 注册</div>
    </div>

    <!-- 登录表单 -->
    <form v-if="!isRegister" novalidate @submit.prevent="submitLogin">
      <div class="field" :class="fieldClass('user')">
        <label for="username">昵称 / 手机号</label>
        <div class="input-wrap">
          <span class="ico">🐱</span>
          <input id="username" v-model="username" type="text" placeholder="铲屎官，报上名来！"
                 autocomplete="username" @input="clearError('user')">
        </div>
        <p class="err">请先填写你的昵称或手机号哦～</p>
      </div>

      <div class="field" :class="fieldClass('pass')">
        <label for="password">密码</label>
        <div class="input-wrap">
          <span class="ico">🔒</span>
          <input id="password" v-model="password" :type="showPassword ? 'text' : 'password'"
                 placeholder="输入 6 位以上密码" autocomplete="current-password" @input="clearError('pass')">
          <button class="eye" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</button>
        </div>
        <p class="err">密码至少 6 位，别让毛孩子失望呀～</p>
      </div>

      <div class="row">
        <label class="remember"><input v-model="remember" type="checkbox"> 记住我</label>
        <a href="#" @click.prevent="forgot">忘记密码？</a>
      </div>

      <button class="btn" type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录领养站 →' }}</button>
    </form>

    <!-- 注册表单 -->
    <form v-else novalidate @submit.prevent="submitRegister">
      <div class="field" :class="fieldClass('nick')">
        <label for="nickname">昵称</label>
        <div class="input-wrap">
          <span class="ico">🐶</span>
          <input id="nickname" v-model="nickname" type="text" placeholder="起个可爱的名字（2-12字）"
                 autocomplete="nickname" @input="clearError('nick')">
        </div>
        <p class="err">昵称需要 2-12 个字哦～</p>
      </div>

      <div class="field" :class="fieldClass('phone')">
        <label for="phone">手机号</label>
        <div class="input-wrap">
          <span class="ico">📱</span>
          <input id="phone" v-model="phone" type="tel" placeholder="输入 11 位手机号"
                 autocomplete="tel" @input="clearError('phone')">
        </div>
        <p class="err">请输入正确的 11 位手机号～</p>
      </div>

      <div class="field" :class="fieldClass('pass')">
        <label for="regPassword">密码</label>
        <div class="input-wrap">
          <span class="ico">🔒</span>
          <input id="regPassword" v-model="password" :type="showPassword ? 'text' : 'password'"
                 placeholder="至少 6 位密码" autocomplete="new-password" @input="clearError('pass')">
          <button class="eye" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</button>
        </div>
        <p class="err">密码至少 6 位哦～</p>
      </div>

      <div class="field" :class="fieldClass('confirm')">
        <label for="confirmPassword">确认密码</label>
        <div class="input-wrap">
          <span class="ico">🔑</span>
          <input id="confirmPassword" v-model="confirmPassword" :type="showConfirm ? 'text' : 'password'"
                 placeholder="再输入一次密码" autocomplete="new-password" @input="clearError('confirm')">
          <button class="eye" type="button" :aria-label="showConfirm ? '隐藏密码' : '显示密码'"
                  @click="showConfirm = !showConfirm">{{ showConfirm ? '🙈' : '👁️' }}</button>
        </div>
        <p class="err">两次密码输入不一致～</p>
      </div>

      <div class="agree-row" :class="{ invalid: invalid.agree, shake: shaking.agree }">
        <label class="remember">
          <input v-model="agree" type="checkbox" @change="clearError('agree')">
          我已阅读并同意《领养协议》与《隐私政策》
        </label>
        <p class="err">要先同意协议才能领养毛孩子哦～</p>
      </div>

      <button class="btn" type="submit" :disabled="loading">{{ loading ? '注册中...' : '创建账号 →' }}</button>
    </form>

    <p class="switch-hint">
      <template v-if="isRegister">已有账号？<a href="#" @click.prevent="switchTab('login')">立即登录 →</a></template>
      <template v-else>还没有账号？<a href="#" @click.prevent="switchTab('register')">免费注册 →</a></template>
    </p>

    <div class="divider">{{ isRegister ? '或者用以下方式快速注册' : '或者用以下方式快速登录' }}</div>
    <div class="social">
      <button type="button" @click="socialLogin('微信')">💬 微信登录</button>
      <button type="button" @click="socialLogin('QQ')">🐧 QQ 登录</button>
    </div>
    <p class="tip">{{ isRegister ? '信息会保存到后端数据库，请勿使用常用密码 🐾' : '登录即表示同意《领养协议》与《隐私政策》🐾' }}</p>
  </section>

  <div class="mask" :class="{ show: maskShow }" @click.self="maskShow = false">
    <div class="modal">
      <div class="big">🎉🐾🎉</div>
      <h2>{{ modalTitle }}</h2>
      <p>{{ modalText }}</p>
      <button class="btn" type="button" @click="maskShow = false">好耶，出发！</button>
    </div>
  </div>
</template>
