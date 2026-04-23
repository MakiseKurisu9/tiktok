import { defineStore } from "pinia";
import { ref } from "vue";

export const useTokenStore = defineStore(
  "token",
  () => {
    const token = ref("");
    const setToken = (newToken) => {
      token.value = newToken;
      localStorage.setItem("token", token.value);
    };
    const removeToken = () => {
      token.value = "";
      localStorage.removeItem("token");
    };
    return {
      token,
      setToken,
      removeToken,
    };
  },
  {
    persist: {
      storage: localStorage, // 或 sessionStorage
      paths: ["token"],
    },
  }
);