# UI Tests — Home Page

> **Implementation Order**: Welcome page display → dashboard display → user-menu interaction → navigation & auth flow (login redirect, logout).

## 1. Welcome Page (Unauthenticated)

### 1.1 Unauthenticated home shows welcome with logo, tagline, and login button

```gherkin
Given the user is not authenticated
When the user navigates to the home page
Then the page displays the "RPM" logo
And the page displays the tagline "Удалённый мониторинг пациентов"
And the page displays a button with text "Войти"
And the dashboard shell is not displayed
```

## 2. Dashboard (Authenticated)

### 2.1 Authenticated home shows the dashboard shell with the current user

```gherkin
Given an authenticated user with first name "Иван" and last name "Петров"
When the user navigates to the home page
Then the page displays the top bar with the "RPM" logo
And the top bar displays the user's avatar with initials "ИП"
And the top bar displays the user's name "Иван Петров"
And the page displays the navigation sidebar
And the main area displays the page title "Главная"
And the main area displays placeholder dashboard content
```

## 3. User Menu

### 3.1 Opening the user menu shows the user's name, email, and logout action

```gherkin
Given an authenticated user with name "Иван Петров" and email "i.petrov@rpm.local"
And the user is on the dashboard
When the user clicks the avatar in the top bar
Then a menu opens displaying the name "Иван Петров"
And the menu displays the email "i.petrov@rpm.local"
And the menu displays an action with text "Выйти"
```

## 4. Navigation & Auth Flow

### 4.1 Clicking "Войти" on the welcome page opens the login page

```gherkin
Given the user is not authenticated
And the user is on the welcome page
When the user clicks the "Войти" button
Then the user is navigated to the login page
```

### 4.2 Successful login redirects to the dashboard

```gherkin
Given a registered ACTIVE user with login "ipetrov" and password "correct-pass" exists
And the user is on the login page
When the user signs in with login "ipetrov" and password "correct-pass"
Then the user is navigated to the home page
And the dashboard shell is displayed with the user's name in the top bar
```

### 4.3 Logging out from the user menu returns to the welcome page

```gherkin
Given an authenticated user is on the dashboard
And the user has opened the user menu
When the user clicks "Выйти"
Then the session is ended
And the user is shown the welcome page with the "Войти" button
```

---

## DSL Technical Reference

| Scenario | Page | Key Elements | Assertions |
|----------|------|--------------|------------|
| 1.1 | Welcome | RPM logo, tagline, "Войти" button | logo + tagline visible, button text exactly "Войти", dashboard shell absent |
| 2.1 | Dashboard | top bar (logo, avatar, name), sidebar, page title, placeholder | avatar initials "ИП" derived from firstName+lastName, name "Иван Петров", title "Главная", placeholder content all visible |
| 3.1 | Dashboard | avatar, dropdown menu (name, email, "Выйти") | clicking the avatar opens the menu; name + email exact text; "Выйти" action visible |
| 4.1 | Welcome → Login | "Войти" button | navigation to `/login`; login page elements visible |
| 4.2 | Login → Dashboard | login form, dashboard top bar | after submit, URL is home `/`; top bar shows the user's name |
| 4.3 | Dashboard → Welcome | "Выйти" action, welcome page | after logout the welcome page with the "Войти" button is shown; subsequent current-user request is unauthenticated (401) |
