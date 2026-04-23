import axios from "axios";
import router from "@/router";

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
    if (err.response && err.response.status === 401) {
      console.log("login first");
      router.push("/login");
    }
    console.log("error occur");
    return Promise.reject(err);
  }
);

instance.interceptors.request.use(
  (config) => {
    const token =
      localStorage.getItem("token") || sessionStorage.getItem("token");
    let parsedToken = "";
    try {
      parsedToken = JSON.parse(token)?.token || "";
    } catch {
      parsedToken = token || "";
    }
    if (parsedToken) {
      config.headers.Authorization = parsedToken;
    }
    return config;
  },
  (err) => {
    return Promise.reject(err);
  }
);

export default instance;
