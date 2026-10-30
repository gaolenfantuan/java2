# hellokitty-login-vue 🎀

Hello Kitty 主题登录 / 注册 / 首页 —— Vue 3 + Vite + vue-router，已对接 Spring Boot 后端。

## 启动方式

```bash
npm install      # 首次运行安装依赖
npm run dev      # 开发服务器 http://localhost:5173
npm run build    # 打包生产版本到 dist
```

> ⚠️ 登录和注册会真的请求后端，请先启动 `D:\workSpace\springBootTest` 里的 Spring Boot 服务（8080 端口）。

## 页面路由

| 路径 | 页面 | 是否需要登录 |
|------|------|-------------|
| `/` | 自动跳到 `/login` | — |
| `/login` | 登录页 | 否 |
| `/register` | 注册页 | 否 |
| `/home` | 首页（展示当前用户信息 + 退出登录） | **是** |
| 其他任意地址 | 自动跳回 `/login` | — |

**路由守卫**：未登录时直接访问 `/home`（或刷新页面）会被强制重定向到 `/login`，并带上 `?redirect=/home`；
登录成功后自动跳回原本想去的页面，否则跳 `/home`。

登录态判断：`localStorage`（勾选"记住我"）或 `sessionStorage` 里有 `kitty_user.token` 即视为已登录，
两者都没有就是未登录。退出登录会清空两处。

## 首页（/home）—— 已接后端

风格延续登录页的粉色 + Hello Kitty，数据全部来自后端 `/api/home/*`：

- **导航栏**：左侧 Logo，中间菜单（首页 / 发现 / 收藏 / 我的），右侧用户头像下拉（个人中心、我的收藏、退出登录）
- **搜索栏**：在导航栏内，输入 300ms 防抖后请求后端搜索（后端会匹配 标题 / 简介 / 标签，也支持「甜点」这类**分类名**关键词），带一键清空
- **欢迎横幅**：问候语 + 内容总数 / 分类数 / 累计点赞（来自 `/api/home/stats`）+ CSS 小猫
- **一级分类**：全部 / 甜点 / 穿搭 / 好物 / 心情，每个后面带内容数量
- **二级分类**：点开某个一级分类后才出现（如甜点 → 蛋糕 / 布丁 / 甜甜圈），**分级筛选**
- **排序**：默认 / 最热（点赞）/ 最新
- **内容网格**：来自 `/api/home/items`，纯 emoji + 渐变色封面，**不依赖任何外部图片**
- 搜不到时有空状态 + 「清空筛选条件」；后端连不上时显示错误提示 + 「重新加载」
- 点卡片会调详情接口（后端浏览量 +1），用 toast 显示分类和浏览量；详情页本身还没做

搜索、分类、排序、分页四个条件都在**后端 SQL 里**完成，不是前端过滤。

## 项目结构

```
hellokitty-login-vue/
├── index.html
├── vite.config.js               # 含 /api 代理 -> http://localhost:8080
├── .npmrc                       # 固定国内镜像源（本机访问 npmjs 会超时）
├── src/
│   ├── main.js                  # 注册路由
│   ├── style.css
│   ├── App.vue                  # 只有 <router-view />
│   ├── api/
│   │   ├── auth.js              # 登录、注册、用户名查重
│   │   └── home.js              # 分类树、搜索列表、热门、详情、统计
│   ├── router/
│   │   ├── index.js             # 路由表 + 标题
│   │   └── guard.js             # 路由守卫（纯 JS，可单测）
│   ├── utils/
│   │   └── auth.js              # 登录态存取：saveUser/getUser/isLoggedIn/clearUser
│   ├── views/
│   │   └── Home.vue             # 首页（需要登录）：横幅 + 分级筛选 + 搜索 + 卡片网格
│   └── components/
│       ├── KittyAvatar.vue      # CSS 画的 Hello Kitty 头像
│       ├── KittyLogin.vue       # 登录页
│       ├── KittyRegister.vue    # 注册页
│       └── home/
│           ├── HomeNav.vue      # 首页导航栏（Logo / 菜单 / 搜索 / 用户下拉）
│           └── CardItem.vue     # 首页内容卡片
```

## 接口对接说明

`src/api/auth.js` —— 账号相关，走 Vite 代理的相对路径 `/api/auth/...`：

| 方法 | 对应后端接口 | 说明 |
|------|-------------|------|
| `login({username, password})` | `POST /api/auth/login` | 成功后返回 `{id, username, nickname, token}` |
| `register({username, nickname, password})` | `POST /api/auth/register` | 返回新用户 id |
| `checkUsername(username)` | `GET /api/auth/check` | 返回 true 表示用户名已被占用 |

`src/api/home.js` —— 首页相关：

| 方法 | 对应后端接口 | 说明 |
|------|-------------|------|
| `fetchCategories()` | `GET /api/home/categories` | 分类树（一级 + children），每节点带 `count` |
| `fetchItems({keyword, categoryId, sort, page, size})` | `GET /api/home/items` | 返回 `{list, total, page, size, pages, hasNext}` |
| `fetchHot(limit)` | `GET /api/home/hot` | 按点赞数取热门 |
| `fetchItemDetail(id)` | `GET /api/home/items/{id}` | 详情，浏览量 +1 |
| `fetchStats()` | `GET /api/home/stats` | `{itemTotal, categoryTotal, likeTotal, hotTotal}` |

登录成功后会写入 `localStorage`（勾选"记住我"）或 `sessionStorage`，key 为 `kitty_user`。

## 联调排查

- 页面提示"连不上服务器"→ 后端没启动，或端口不是 8080
- 后端已改成 H2 内存数据库，**不需要装 MySQL**，启动即用
- 演示账号：`kitty` / `1234`；数据在内存里，后端重启会清空（只剩演示账号）
- 想看数据库里的用户：浏览器打开 http://localhost:8080/h2-console ，JDBC URL 填 `jdbc:h2:mem:hello_kitty`，用户 `sa`，密码留空
- `npm install` 卡住不动 → 本机访问 npmjs 会超时，项目里 `.npmrc` 已指向 npmmirror，确认它在项目根目录
