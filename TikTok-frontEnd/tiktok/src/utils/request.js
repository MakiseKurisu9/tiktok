import axios from "axios";

const baseURL = "/api";

const instance = axios.create({ baseURL });

instance.interceptors.response.use(
  (result) => {
    if (result.data.success === true) {
      return result.data;
    }
    console.log("server occur error");
    return Promise.reject(result.data);
  },
  (err) => {
    //判断相应码，如果401，则为未登录，需要跳转到登录页面
    if (err.response.status === 401) {
      console.log("login first");
      router.push("/login");
      //主页面3个请求，防止重新弹出提示框
    }
    console.log("error occur");

    return Promise.reject(err); //异步状态转为失败
  }
);

instance.interceptors.request.use(
  (config) => {
    // 从 localStorage 或 sessionStorage 中获取 token
    const token =
      localStorage.getItem("token") || sessionStorage.getItem("token");
    let parsedToken = JSON.parse(token)?.token || "";
    // 如果 token 存在，直接设置到 Authorization 头
    if (token) {
      config.headers.Authorization = parsedToken;
    }
    console.log("Request Headers:", config.headers);
    return config;
  },
  (err) => {
    return Promise.reject(err); // 正确处理错误
  }
);
export default instance;
