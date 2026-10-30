<template>
  <div class="kitty">
    <div class="ear left"></div>
    <div class="ear right"></div>
    <div class="head">
      <div class="eye l"></div>
      <div class="eye r"></div>
      <div class="nose"></div>
      <div class="whisker wl1"></div>
      <div class="whisker wl2"></div>
      <div class="whisker wr1"></div>
      <div class="whisker wr2"></div>
    </div>
    <div class="bow">
      <div class="petal p1"></div>
      <div class="petal p2"></div>
      <div class="knot"></div>
    </div>
  </div>
</template>

<style scoped>
/* 纯 CSS Hello Kitty 头像 */
.kitty { position: relative; width: 120px; height: 100px; margin: 0 auto 6px; }

.kitty .ear { position: absolute; top: 0; width: 0; height: 0; }
.kitty .ear.left {
  left: 8px;
  border-left: 5px solid transparent;
  border-right: 40px solid transparent;
  border-bottom: 32px solid #fff;
  transform: rotate(-30deg);
  filter: drop-shadow(0 0 1px #f7a8c4);
}
.kitty .ear.right {
  right: 8px;
  border-right: 5px solid transparent;
  border-left: 40px solid transparent;
  border-bottom: 32px solid #fff;
  transform: rotate(30deg);
  filter: drop-shadow(0 0 1px #f7a8c4);
}
.kitty .head {
  position: absolute; top: 14px; left: 50%; transform: translateX(-50%);
  width: 100px; height: 82px;
  background: #fff;
  border-radius: 50%;
  box-shadow: inset 0 0 0 2px #ffd3e5;
}
.kitty .eye {
  position: absolute; top: 46px;
  width: 8px; height: 12px;
  background: #222; border-radius: 50%;
}
.kitty .eye.l { left: 30px; }
.kitty .eye.r { right: 30px; }
.kitty .nose {
  position: absolute; top: 58px; left: 50%; transform: translateX(-50%);
  width: 12px; height: 9px;
  background: #ffb200; border-radius: 50%;
}
.kitty .whisker {
  position: absolute; top: 56px;
  width: 24px; height: 2px; background: #222; border-radius: 2px;
}
.kitty .whisker.wl1 { left: -2px; transform: rotate(12deg); }
.kitty .whisker.wl2 { left: 2px; top: 64px; transform: rotate(-6deg); }
.kitty .whisker.wr1 { right: -2px; transform: rotate(-12deg); }
.kitty .whisker.wr2 { right: 2px; top: 64px; transform: rotate(6deg); }
/* 蝴蝶结 */
.kitty .bow { position: absolute; top: 4px; right: 6px; z-index: 3; }
.kitty .bow .petal {
  position: absolute; top: 0;
  width: 20px; height: 15px;
  background: #ff4f8b; border-radius: 60%;
}
.kitty .bow .p1 { left: -14px; transform: rotate(-18deg); }
.kitty .bow .p2 { left: 8px; transform: rotate(18deg); }
.kitty .bow .knot {
  position: absolute; top: 4px; left: 1px;
  width: 10px; height: 10px; background: #e83e76; border-radius: 50%;
}
</style>
