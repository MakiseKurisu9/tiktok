<template>
  <div class="page-background">
    <v-card class="mx-auto mt-10" max-width="500">
      <v-window>
        <!-- 注册窗格 -->
        <v-window-item>
          <v-card-text>
            <v-text-field
              v-model="findPasswordForm.email"
              label="邮箱"
              variant="underlined"
            />
            <v-text-field
              v-model="findPasswordForm.newPassword"
              label="新密码"
              variant="underlined"
            />
            <v-text-field
              v-model="emailCodeForm.code"
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
              v-model="findPasswordForm.mailCode"
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
            <v-btn color="primary" @click="ChangePassword">
              修改密码
              <v-icon icon="mdi-check-bold" end />
            </v-btn>
          </v-card-actions>
          <v-card-actions>
            <v-spacer />
            <v-btn color="success" @click="toLogin">
              返回
              <v-icon icon="mdi-check-bold" end />
            </v-btn>
          </v-card-actions>
        </v-window-item>
      </v-window>
    </v-card>
  </div>
</template>

<script setup>
import { onMounted } from "vue";

import { reactive, ref } from "vue";
import { findPassword, sendMail, getCaptchaUrl } from "@/api/login";
import router from "@/router";

onMounted(() => {
  refreshCaptcha();
});

let emailCodeSending = ref(false);

const uuid = ref(generateUUID());
const captchaUrl = ref("");

function refreshCaptcha() {
  uuid.value = generateUUID();
  captchaUrl.value = getCaptchaUrl(uuid.value) + "?t=" + new Date().getTime();
}

const findPasswordForm = reactive({
  email: "",
  mailCode: "",
  newPassword: "",
});

const emailCodeForm = reactive({
  uuid: uuid.value,
  code: "",
  email: findPasswordForm.email,
});

function toLogin() {
  router.push("/Login");
}

// 修改密码
async function ChangePassword() {
  if (
    !findPasswordForm.email ||
    !findPasswordForm.newPassword ||
    !findPasswordForm.mailCode
  ) {
    alert("请填写所有字段！");
    return;
  }
  try {
    console.log(findPasswordForm);
    console.log(emailCodeForm);

    const res = await findPassword({
      email: findPasswordForm.email,
      code: findPasswordForm.mailCode,
      newPassword: findPasswordForm.newPassword,
    });
    alert("密码修改成功！");
  } catch (err) {
    console.log(err);
  } finally {
    clearFindPasswordForm();
  }
}

// 发送验证码
async function sendEmailCode() {
  if (!findPasswordForm.email || !findPasswordForm.newPassword) {
    console.log(findPasswordForm);
    alert("请确保除邮箱验证码外内容填写完毕");
    return;
  }
  emailCodeSending.value = true;
  await sendMail({
    email: findPasswordForm.email,
    code: emailCodeForm.code,
    uuid: uuid.value,
  });
  alert("邮箱验证码已发送，请查收邮箱");
  setTimeout(() => {
    emailCodeSending.value = false;
  }, 60 * 1000);
}

function clearFindPasswordForm() {
  const o = {
    email: "",
    mailCode: "",
    newPassword: "",
  };
  emailCodeForm.code = "";
  Object.assign(findPasswordForm, o);
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
