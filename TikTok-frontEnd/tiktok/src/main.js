import "./assets/main.css";

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "vuetify/styles";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import "@mdi/font/css/materialdesignicons.css";

import { createPinia } from "pinia";
import { createPersistedState } from "pinia-plugin-persistedstate";

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: "mdi",
  },
  theme: {
    defaultTheme: "dark",
  },
});
const pinia = createPinia();
const persist = createPersistedState();
pinia.use(persist);

createApp(App).use(vuetify).use(router).use(pinia).mount("#app");
