import eslint from '@eslint/js';
import { defineConfig } from 'eslint/config';
import eslintConfigPrettier from 'eslint-config-prettier';
import oxlint from 'eslint-plugin-oxlint';
import pluginVue from 'eslint-plugin-vue';
import vueA11y from 'eslint-plugin-vuejs-accessibility';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig(
  {
    ignores: ['dist/**', 'coverage/**', 'node_modules/**', 'test-results/**', 'playwright-report/**', '**/*.d.ts'],
  },
  eslint.configs.recommended,
  ...tseslint.configs.recommendedTypeChecked,
  ...pluginVue.configs['flat/recommended'],
  ...vueA11y.configs['flat/recommended'],
  {
    files: ['**/*.{ts,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: { ...globals.browser },
      parserOptions: {
        parser: tseslint.parser,
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
        extraFileExtensions: ['.vue'],
      },
    },
    rules: {
      'vue/multi-word-component-names': ['error', { ignores: ['App'] }],
      // Task 246: accept EITHER `for`/id association OR nesting. The rule's default
      // (`required: {every: ['nesting','id']}`) demands both and flags correct markup
      // such as `<label for="login">` + `<input id="login">`. See the ADR
      // `app-level-a11y-decision.md`. Sibling rule `no-static-element-interactions`
      // takes no options (`schema: []`), so keyboard-only wrapper handlers are
      // suppressed per-site with a justification comment instead.
      'vuejs-accessibility/label-has-for': ['error', { required: { some: ['nesting', 'id'] } }],
      // Task 205: forbid blind `as` casts on an unvalidated network response body.
      // Validate at the boundary with a schema (`schema.parse(await response.json())`)
      // instead — a trust-cast hides backend-contract drift. See frontend-rules.md
      // (Humble Object Pattern) and vue-ts/coding.md § "Schema Validation (zod)".
      'no-restricted-syntax': [
        'error',
        {
          // (await response.json()) as T  — `.json()` call wrapped in `await`
          selector:
            "TSAsExpression[expression.type='AwaitExpression'][expression.argument.callee.property.name='json']",
          message:
            'Do not cast an unvalidated network response with `as`. Validate the body at the boundary with a schema, e.g. `schema.parse(await response.json())`.',
        },
        {
          // await response.json() as T  /  response.json() as T  — `.json()` call cast directly
          selector: "TSAsExpression[expression.type='CallExpression'][expression.callee.property.name='json']",
          message:
            'Do not cast an unvalidated network response with `as`. Validate the body at the boundary with a schema, e.g. `schema.parse(await response.json())`.',
        },
      ],
    },
  },
  {
    files: ['*.config.{js,ts}', 'vite.config.ts', 'playwright.config.ts', 'acceptance/**/*.ts'],
    languageOptions: {
      globals: { ...globals.node },
    },
  },
  {
    files: ['**/*.js'],
    ...tseslint.configs.disableTypeChecked,
  },
  eslintConfigPrettier,
  ...oxlint.buildFromOxlintConfigFile('.oxlintrc.json'),
);
