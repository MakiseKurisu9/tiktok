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
            >
            </v-text-field>
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
            >
            </v-text-field>
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
  code: "", // 图形验证码
});

const registerForm = reactive({
  username: "",
  email: "",
  password: "",
  emailCode: "",
  code: "", // 图形验证码
});

const uuid = ref(generateUUID());
const captchaUrl = ref("");

function refreshCaptcha() {
  uuid.value = generateUUID();
  captchaUrl.value = getCaptchaUrl(uuid.value) + "?t=" + new Date().getTime();
  loginForm.code = "";
  registerForm.code = "";
}

// 发送邮箱验证码
const emailCodeSending = ref(false);

function toFindPassword() {
  router.push("/findPassword");
}

async function sendEmailCode() {
  if (
    !registerForm.email ||
    !registerForm.code ||
    !registerForm.username ||
    !registerForm.password
  ) {
    console.log(registerForm);
    alert("请确保除邮箱验证码外内容填写完毕");
    return;
  }
  emailCodeSending.value = true;
  await sendMail({
    email: registerForm.email,
    code: registerForm.code,
    uuid: uuid.value,
  });
  alert("邮箱验证码已发送，请查收邮箱");
  setTimeout(() => {
    emailCodeSending.value = false;
  }, 60 * 1000);
}

// 登录请求
async function onLogin() {
  console.log(uuid.value);
  console.log(loginForm);

  const res = await login({
    username: loginForm.username,
    password: loginForm.password,
    code: loginForm.code,
    uuid: uuid.value,
  });
  alert("登录成功");
  console.log(res.data.token);
  console.log(res.data.userInfo);
  userStore.setUser(res.data.userInfo);
  tokenStore.setToken(res.data.token);
  clearLoginForm();

  router.push("/");
}

// 注册请求
async function onRegister() {
  console.log(registerForm);
  const res = await registry({
    username: registerForm.username,
    email: registerForm.email,
    password: registerForm.password,
    emailCode: registerForm.emailCode,
    code: registerForm.code,
    uuid: uuid.value,
  });
  alert("注册成功，请登录");
  tab.value = "login";
  clearRegistryForm();
  console.log(registerForm);

  refreshCaptcha();
}

function clearRegistryForm() {
  const o = {
    username: "",
    email: "",
    password: "",
    emailCode: "",
    code: "", // 图形验证码
  };
  Object.assign(registerForm, o);
}

function clearLoginForm() {
  const o = {
    username: "",

    password: "",

    code: "", // 图形验证码
  };
  Object.assign(loginForm, o);
}

// 生成 UUID
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
