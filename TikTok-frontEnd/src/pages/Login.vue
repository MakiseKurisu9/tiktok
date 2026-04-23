<template>
  <div class="page-background">
    <v-card class="mx-auto mt-10" max-width="500">
      <!-- 标签页切换 -->
      <v-tabs v-model="tab" align-tabs="center" grow>
        <v-tab value="login">登录</v-tab>
        <v-tab value="register">注册</v-tab>
      </v-tabs>

      <v-window v-model="tab">
        <!-- 登录窗格 -->
        <v-window-item value="login">
          <v-card-text>
            <v-text-field
              v-model="loginForm.username"
              label="用户名"
              variant="underlined"
            />
            <v-text-field
              v-model="loginForm.password"
              label="密码"
              type="password"
              variant="underlined"
            />
            <v-text-field
              v-model="loginForm.code"
              label="图形验证码"
              variant="underlined"
            />
            <v-row justify="center">
              <v-col cols="auto">
                <img
                  :src="captchaUrl"
                  @click="refreshCaptcha"
                  class="captcha-img"
                  title="点击刷新验证码"
                />
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn color="primary" @click="onLogin">
              登录
              <v-icon icon="mdi-login" end />
            </v-btn>
          </v-card-actions>
          <v-card-actions>
            <v-spacer />
            <v-btn color="primary" @click="toFindPassword">
              找回密码
              <v-icon icon="mdi-login" end />
            </v-btn>
          </v-card-actions>
        </v-window-item>

        <!-- 注册窗格 -->
        <v-window-item value="register">
          <v-card-text>
            <v-text-field
              v-model="registerForm.username"
              label="用户名"
              variant="underlined"
            />
            <v-text-field
              v-model="registerForm.email"
              label="邮箱"
              variant="underlined"
            />
            <v-text-field
              v-model="registerForm.password"
              label="密码"
              type="password"
              variant="underlined"
            />
            <v-text-field
              v-model="registerForm.code"
              label="图形验证码"
              variant="underlined"
            />
            <v-row justify="center">
              <v-col cols="auto">
                <img
                  :src="captchaUrl"
                  @click="refreshCaptcha"
                  class="captcha-img"
                  title="点击刷新验证码"
                />
              </v-col>
            </v-row>
            <v-text-field
              v-model="registerForm.emailCode"
              label="邮箱验证码"
              variant="underlined"
            >
              <template #append-inner>
                <v-btn
                  size="small"
                  variant="text"
                  :disabled="emailCodeSending"
                  @click="sendEmailCode"
                >
                  {{ emailCodeSending ? "请等待60秒后再次获取" : "发送验证码" }}
                </v-btn>
              </template>
            </v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn color="success" @click="onRegister">
              注册
              <v-icon icon="mdi-check-bold" end />
            </v-btn>
          </v-card-actions>
        </v-window-item>
      </v-window>
    </v-card>

    <!-- Snackbar 消息提示 -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
      location="top"
      rounded="pill"
      elevation="4"
    >
      <div class="d-flex align-center ga-2">
        <v-icon :icon="snackbar.icon" />
        <span>{{ snackbar.message }}</span>
      </div>
      <template #actions>
        <v-btn variant="text" icon="mdi-close" @click="snackbar.show = false" />
      </template>
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getCaptchaUrl, login, registry, sendMail } from "@/api/login.js";
import router from "@/router";
import { useTokenStore } from "@/pinia/token";
import { useUserStore } from "@/pinia/user";

const userStore = useUserStore();
const tokenStore = useTokenStore();

onMounted(() => {
  refreshCaptcha();
});

const tab = ref("login");

const loginForm = reactive({
  username: "",
  password: "",
  code: "",
});

const registerForm = reactive({
  username: "",
  email: "",
  password: "",
  emailCode: "",
  code: "",
});

const uuid = ref(generateUUID());
const captchaUrl = ref("");

// ── Snackbar ──────────────────────────────────────────────────────────────────
const snackbar = reactive({
  show: false,
  message: "",
  color: "success",
  icon: "mdi-check-circle",
  timeout: 3000,
});

function showMessage(message, type = "success") {
  const config = {
    success: { color: "success", icon: "mdi-check-circle" },
    error: { color: "error", icon: "mdi-alert-circle" },
    info: { color: "info", icon: "mdi-information" },
    warning: { color: "warning", icon: "mdi-alert" },
  };
  const { color, icon } = config[type] ?? config.info;
  snackbar.message = message;
  snackbar.color = color;
  snackbar.icon = icon;
  snackbar.timeout = type === "error" ? 5000 : 3000;
  snackbar.show = true;
}

// Helper: extract a human-readable error message from an API error
function parseError(e) {
  // Axios-style response error
  const data = e?.response?.data;
  if (data) {
    if (typeof data === "string" && data.trim()) return data.trim();
    if (data.message) return data.message;
    if (data.msg) return data.msg;
    if (data.error) return data.error;
  }
  // Plain Error object
  if (e?.message) return e.message;
  return "操作失败，请稍后重试";
}

// ── Captcha ───────────────────────────────────────────────────────────────────
function refreshCaptcha() {
  uuid.value = generateUUID();
  captchaUrl.value = getCaptchaUrl(uuid.value) + "?t=" + new Date().getTime();
  loginForm.code = "";
  registerForm.code = "";
}

// ── Email code ────────────────────────────────────────────────────────────────
const emailCodeSending = ref(false);

async function sendEmailCode() {
  if (
    !registerForm.email ||
    !registerForm.code ||
    !registerForm.username ||
    !registerForm.password
  ) {
    showMessage("请确保除邮箱验证码外内容填写完毕", "warning");
    return;
  }
  try {
    emailCodeSending.value = true;
    await sendMail({
      email: registerForm.email,
      code: registerForm.code,
      uuid: uuid.value,
    });
    showMessage("邮箱验证码已发送，请查收邮箱", "success");
    setTimeout(() => {
      emailCodeSending.value = false;
    }, 60 * 1000);
  } catch (e) {
    emailCodeSending.value = false;
    showMessage(parseError(e), "error");
    refreshCaptcha();
  }
}

// ── Login ─────────────────────────────────────────────────────────────────────
async function onLogin() {
  try {
    const res = await login({
      username: loginForm.username,
      password: loginForm.password,
      code: loginForm.code,
      uuid: uuid.value,
    });
    userStore.setUser(res.data.userInfo);
    tokenStore.setToken(res.data.token);
    showMessage("登录成功", "success");
    clearLoginForm();
    setTimeout(() => router.push("/"), 800);
  } catch (e) {
    showMessage(parseError(e), "error");
    refreshCaptcha();
  }
}

// ── Register ──────────────────────────────────────────────────────────────────
async function onRegister() {
  try {
    await registry({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      emailCode: registerForm.emailCode,
      code: registerForm.code,
      uuid: uuid.value,
    });
    tokenStore.removeToken();
    userStore.removeUser();
    showMessage("注册成功，请登录", "success");
    tab.value = "login";
    clearRegistryForm();
    refreshCaptcha();
  } catch (e) {
    // Show the server's error message so the user knows what went wrong
    showMessage(parseError(e), "error");
    refreshCaptcha();
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function toFindPassword() {
  router.push("/findPassword");
}

function clearRegistryForm() {
  Object.assign(registerForm, {
    username: "",
    email: "",
    password: "",
    emailCode: "",
    code: "",
  });
}

function clearLoginForm() {
  Object.assign(loginForm, { username: "", password: "", code: "" });
}

function generateUUID() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0,
      v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
</script>

<style scoped>
.v-card {
  width: 10000%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 12px;
  padding: 20px 10px;
}

.v-card-text {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.v-text-field {
  margin-bottom: 8px;
}

img {
  border: 1px solid #ccc;
  height: 80px;
  border-radius: 4px;
  padding: 2px;
  transition: all 0.3s ease;
  margin-left: 8px;
  vertical-align: middle;
}

img:hover {
  box-shadow: 0 0 6px rgba(0, 123, 255, 0.6);
  transform: scale(1.05);
  cursor: pointer;
}

.v-card-actions {
  padding-bottom: 16px;
}

.v-btn {
  text-transform: none;
  font-weight: 600;
}

.v-btn[disabled] {
  opacity: 0.6 !important;
  pointer-events: none;
}
</style>
