<script setup>
/**
 * 玩法三：甜点趣味答题
 * 题目由后端随机给出（题干里不含答案），提交后后端判分并落库，答对 +5 积分。
 */
import { ref, computed, onMounted } from 'vue'
import { fetchQuizQuestions, submitQuizAnswer } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'
import AchievementPopup from './AchievementPopup.vue'

const props = defineProps({
  userId: { type: Number, default: 0 }
})
const emit = defineEmits(['changed'])

const questions = ref([])
const index = ref(0)
const selected = ref('')
const result = ref(null)
const loading = ref(true)
const error = ref('')
const busy = ref(false)
const finished = ref(false)
const roundPoints = ref(0)
const roundCorrect = ref(0)
const popupShow = ref(false)
const popupItems = ref([])

const current = computed(() => questions.value[index.value] || null)
const progress = computed(() =>
  questions.value.length ? Math.round(((index.value + (result.value ? 1 : 0)) * 100) / questions.value.length) : 0
)

async function load() {
  loading.value = true
  error.value = ''
  finished.value = false
  index.value = 0
  selected.value = ''
  result.value = null
  roundPoints.value = 0
  roundCorrect.value = 0
  try {
    const list = await fetchQuizQuestions(props.userId, 5)
    questions.value = list
    if (!list.length) error.value = '题库空的，等等再来吧~'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

/** 选项样式：提交后标出对错 */
function optionClass(key) {
  if (!result.value) return { picked: selected.value === key }
  return {
    correct: key === result.value.answerKey,
    wrong: key === result.value.chosen && !result.value.correct,
    dim: key !== result.value.answerKey && key !== result.value.chosen
  }
}

async function pick(key) {
  if (result.value || busy.value) return
  selected.value = key
  busy.value = true
  try {
    const res = await submitQuizAnswer(props.userId, current.value.id, key)
    result.value = res
    if (res.correct) {
      roundPoints.value += res.pointsAwarded
      roundCorrect.value += 1
      showToast.success(res.message)
    } else {
      showToast.error(res.message)
    }
    if (res.newAchievements && res.newAchievements.length) {
      popupItems.value = res.newAchievements
      popupShow.value = true
    }
    emit('changed')
  } catch (e) {
    selected.value = ''
    showToast.error(e.message)
  } finally {
    busy.value = false
  }
}

function next() {
  if (index.value + 1 >= questions.value.length) {
    finished.value = true
    return
  }
  index.value += 1
  selected.value = ''
  result.value = null
}

onMounted(load)
</script>

<template>
  <div class="quiz">
    <section v-if="error" class="g-empty">
      <div class="ge-emoji">😿</div>
      <h3>{{ error }}</h3>
      <button class="ge-btn" @click="load">重新加载</button>
    </section>

    <section v-else-if="loading" class="g-empty">
      <div class="ge-emoji">🎀</div>
      <h3>正在出题…</h3>
    </section>

    <!-- 本轮结束 -->
    <section v-else-if="finished" class="q-done">
      <div class="qd-emoji">🧁</div>
      <h2>本轮答完啦！</h2>
      <p class="qd-num">
        答对 <strong>{{ roundCorrect }}</strong> / {{ questions.length }} 题 · 获得
        <strong>+{{ roundPoints }}</strong> 积分
      </p>
      <p class="qd-tip">
        {{ roundCorrect === questions.length ? '全对！你就是甜点小博士本博 🎓' : '错题会记住解析，下次一定行～' }}
      </p>
      <button class="qd-btn" @click="load">再来一组</button>
    </section>

    <!-- 答题中 -->
    <section v-else-if="current" class="q-wrap">
      <div class="q-head">
        <span class="q-no">第 {{ index + 1 }} / {{ questions.length }} 题</span>
        <span class="q-progress"><i :style="{ width: progress + '%' }"></i></span>
        <span class="q-points">答对 +{{ current.points }} 分</span>
      </div>

      <article class="q-card">
        <div class="q-tags">
          <span class="q-tag">{{ current.category }}</span>
          <span class="q-tag soft">{{ current.difficulty }}</span>
        </div>
        <h2 class="q-title">{{ current.content }}</h2>

        <div class="q-options">
          <button
            v-for="op in current.options"
            :key="op.key"
            class="q-option"
            :class="optionClass(op.key)"
            @click="pick(op.key)"
          >
            <span class="opt-key">{{ op.key }}</span>
            <span class="opt-text">{{ op.text }}</span>
            <span v-if="result && op.key === result.answerKey" class="opt-mark">✓</span>
            <span v-else-if="result && op.key === result.chosen && !result.correct" class="opt-mark bad">✕</span>
          </button>
        </div>

        <div v-if="result" class="q-result" :class="{ ok: result.correct }">
          <p class="qr-head">
            <span class="qr-icon">{{ result.correct ? '🎉' : '🤔' }}</span>
            {{ result.message }}
          </p>
          <p v-if="result.explanation" class="qr-desc">💡 {{ result.explanation }}</p>
        </div>
      </article>

      <div class="q-actions">
        <button v-if="!result" class="q-submit" disabled>
          {{ busy ? '判分中…' : '选一个答案吧' }}
        </button>
        <button v-else class="q-submit" @click="next">
          {{ index + 1 >= questions.length ? '看本轮成绩' : '下一题 ›' }}
        </button>
      </div>
    </section>

    <AchievementPopup :show="popupShow" :items="popupItems" @close="popupShow = false" />
  </div>
</template>

<style scoped>
.quiz { animation: fadeUp 0.4s ease backwards; }

/* 头部 */
.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.q-no { font-size: 12px; color: #b96a8d; font-weight: bold; white-space: nowrap; }
.q-progress {
  flex: 1;
  height: 7px;
  border-radius: 50px;
  background: #ffe3ee;
  overflow: hidden;
}
.q-progress i {
  display: block;
  height: 100%;
  border-radius: 50px;
  background: linear-gradient(90deg, #ff9dc0, #ff4f8b);
  transition: width 0.4s ease;
}
.q-points { font-size: 11.5px; color: #d1689a; white-space: nowrap; }

/* 题卡 */
.q-card {
  padding: 22px 22px 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  border: 2px solid #ffd9e8;
  box-shadow: 0 14px 30px rgba(255, 105, 160, 0.14);
}
.q-tags { display: flex; gap: 6px; margin-bottom: 10px; }
.q-tag {
  padding: 2px 10px;
  border-radius: 50px;
  font-size: 10.5px;
  font-weight: bold;
  background: #ffe9f3;
  color: #ff4f8b;
}
.q-tag.soft { background: #f3f0ff; color: #7c6bd6; }

.q-title {
  margin: 0 0 16px;
  font-size: 16.5px;
  line-height: 1.55;
  color: #7a4b5e;
  letter-spacing: 0.3px;
}

.q-options { display: flex; flex-direction: column; gap: 9px; }
.q-option {
  display: flex;
  align-items: center;
  gap: 11px;
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #ffe0ee;
  border-radius: 16px;
  background: #fffafc;
  color: #7a4b5e;
  font-size: 13.5px;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: 0.2s;
}
.q-option:hover { border-color: #ffb0cf; background: #fff0f6; transform: translateX(3px); }
.q-option.picked { border-color: #ff6fa5; background: #fff0f6; }
.q-option.correct { border-color: #6cc79a; background: #effaf3; }
.q-option.wrong { border-color: #f08aa6; background: #fff1f5; }
.q-option.dim { opacity: 0.55; }

.opt-key {
  flex: 0 0 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  background: #ffe9f3;
  color: #ff4f8b;
}
.q-option.correct .opt-key { background: #d7f5e4; color: #2f9c6a; }
.q-option.wrong .opt-key { background: #ffdbe4; color: #d63b6b; }
.opt-text { flex: 1; }
.opt-mark { font-weight: bold; color: #2f9c6a; }
.opt-mark.bad { color: #d63b6b; }

/* 结果 */
.q-result {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 16px;
  background: #fff1f5;
  border: 1px dashed #ffb0cf;
  animation: fadeUp 0.3s ease;
}
.q-result.ok { background: #effaf3; border-color: #a8dfc3; }
.qr-head { margin: 0 0 4px; font-size: 13px; font-weight: bold; color: #d63b6b; }
.q-result.ok .qr-head { color: #2f9c6a; }
.qr-icon { margin-right: 4px; }
.qr-desc { margin: 0; font-size: 12px; line-height: 1.65; color: #8a6b78; }

/* 按钮 */
.q-actions { margin-top: 14px; }
.q-submit {
  width: 100%;
  padding: 13px 18px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(255, 79, 139, 0.3);
  transition: 0.2s;
}
.q-submit:disabled { background: #ffd9e8; color: #d1689a; box-shadow: none; cursor: default; }
.q-submit:hover:not(:disabled) { transform: translateY(-2px); }

/* 成绩页 */
.q-done {
  padding: 40px 24px;
  border-radius: 26px;
  background: linear-gradient(150deg, #fff8fb, #ffeaf4);
  border: 2px solid #ffd0e3;
  text-align: center;
  box-shadow: 0 16px 34px rgba(255, 105, 160, 0.16);
}
.qd-emoji { font-size: 52px; margin-bottom: 8px; animation: pop 1.4s ease-in-out infinite; }
.q-done h2 { margin: 0 0 10px; font-size: 19px; color: #ff4f8b; }
.qd-num { margin: 0 0 6px; font-size: 13.5px; color: #7a4b5e; }
.qd-num strong { color: #ff4f8b; font-size: 17px; }
.qd-tip { margin: 0 0 18px; font-size: 12px; color: #b98ba0; }
.qd-btn {
  padding: 11px 28px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 13.5px;
  font-weight: bold;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(255, 79, 139, 0.3);
  transition: 0.2s;
}
.qd-btn:hover { transform: translateY(-2px); }

/* 空态 */
.g-empty {
  background: rgba(255, 255, 255, 0.88);
  border: 2px dashed #ffc0da;
  border-radius: 22px;
  padding: 38px 22px;
  text-align: center;
}
.ge-emoji { font-size: 36px; margin-bottom: 8px; }
.g-empty h3 { margin: 0 0 10px; font-size: 14px; color: #ff4f8b; }
.ge-btn {
  padding: 8px 20px;
  border: 2px solid #ff4f8b;
  border-radius: 50px;
  background: #fff;
  color: #ff4f8b;
  font-size: 12.5px;
  font-weight: bold;
  cursor: pointer;
  font-family: inherit;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}
@keyframes pop {
  0%, 100% { transform: scale(1) rotate(-3deg); }
  50% { transform: scale(1.08) rotate(3deg); }
}
</style>
