// 获取验证码图片链接
// 返回图片地址字符串，前端可直接用这个地址 <img :src="url" />
import request from "@/utils/request";

export function getCaptchaUrl(uuid) {
  return `/api/login/captcha.jpg/${uuid}`;
}

// 发送邮箱验证码
export function sendMail(emailCodeDTO) {
  return request.post("/login/sendMail", emailCodeDTO);
}

// 注册
export function registry(registryDTO) {
  return request.post("/login/registry", registryDTO);
}

// 登录
export function login(loginDTO) {
  return request.post("/login/logIn", loginDTO);
}

// 找回密码
export function findPassword(findPasswordDTO) {
  return request.post("/login/findPassword", findPasswordDTO);
}