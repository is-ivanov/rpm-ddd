import { defineConfig } from "allure";

export default defineConfig({
  name: "RPM DDD Test Report",
  output: "./target/allure-report",
  plugins: {
    awesome: {
      options: {
        singleFile: true,
        reportLanguage: "en",
      },
    },
  },
});
