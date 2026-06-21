# UI Tests — User Login

> **Implementation Order**: Page display → basic interaction → form submission → validation feedback → server response → navigation.

## 1. Login Page Display

### 1.1 Login page shows login and password fields and submit button

```gherkin
Given the user navigates to the login page
Then the page displays a login input field
And the page displays a password input field
And the password field masks entered text
And the page displays a submit button with text "Sign In"
```

## 2. Login Form Interaction

### 2.1 Password visibility toggle shows and hides password

```gherkin
Given the user is on the login page
When the user enters text into the password field
Then the password field masks the entered text
When the user clicks the password visibility toggle
Then the password field reveals the entered text in plain form
When the user clicks the password visibility toggle again
Then the password field masks the entered text again
```

### 2.2 Login page shows loading state during submission

```gherkin
Given the login page is displayed
When the user submits valid credentials
Then the login button shows a loading indicator
And the form fields become disabled during submission
```

## 3. Login Error Display

### 3.1 Wrong credentials show error banner with "Invalid username or password"

```gherkin
Given the user is on the login page
And a registered user with login "ivan" and password "correct-pass" exists
When the user enters login "ivan"
And the user enters password "wrong-pass"
And the user clicks the "Sign In" button
Then an error banner appears with text "Invalid username or password"
And the login and password fields are cleared
```

### 3.2 Inactive account shows error banner with activation message

```gherkin
Given the user is on the login page
And an inactive user with login "pending" and password "some-pass" exists
When the user enters login "pending"
And the user enters password "some-pass"
And the user clicks the "Sign In" button
Then an error banner appears with text indicating the account requires activation
And the error banner contains a link to request a new activation email
```

### 3.3 Error banner dismiss button closes the banner

```gherkin
Given an error banner is visible on the page
When the user clicks the dismiss button on the banner
Then the error banner is no longer visible
```

## 4. Activation Page Display

### 4.1 Activation page shows password fields and complexity rules

```gherkin
Given the user navigates to the activation page with a valid token
Then the page displays a password input field
And the page displays a confirm password input field
And the page displays password complexity rules
And the page displays a submit button with text "Activate Account"
```

### 4.2 Activation page shows password strength indicator updating in real-time

```gherkin
Given the activation page is displayed
When the user types a weak password
Then the password strength indicator shows weak
When the user updates the password to a strong value
Then the password strength indicator updates to strong in real-time
```

### 4.3 Activation page shows error when passwords do not match

```gherkin
Given the activation page is displayed
When the user enters different values in the password and confirm password fields
Then an error message is displayed indicating the passwords do not match
```

### 4.4 Activation page shows loading state during submission

```gherkin
Given the activation page is displayed with a valid token
When the user submits a valid matching password
Then the activate button shows a loading indicator
And the form fields become disabled during submission
```

## 5. Activation Result Display

### 5.1 Successful activation shows success message and "Go to Sign In" button

```gherkin
Given the user is on the activation page with a valid token
When the user enters a valid password meeting all complexity rules
And the user enters the same password in the confirm field
And the user clicks the "Activate Account" button
Then the page displays a green check icon
And the page displays the text "Account Activated!"
And the page displays a button with text "Go to Sign In"
```

### 5.2 Expired token shows error message and "Request New Link" button

```gherkin
Given the user navigates to the activation page with an expired token
Then the page displays a red X icon
And the page displays the text "Link Expired"
And the page displays a button with text "Request New Link"
```

## 6. Navigation

### 6.1 Clicking "Go to Sign In" navigates to login page

```gherkin
Given the user has completed account activation
And the success screen is displayed with button "Go to Sign In"
When the user clicks the "Go to Sign In" button
Then the user is navigated to the login page
```

---

## DSL Technical Reference

| Scenario | Page | Key Elements | Assertions |
|----------|------|-------------|------------|
| 1.1 | Login | login input, password input (type password), "Sign In" button | visible, input type=password for password field |
| 2.1 | Login | password field, visibility toggle icon/button | toggles input type between password and text |
| 2.2 | Login | "Sign In" button, login input, password input | on submit: button shows loading indicator, login + password fields disabled during the in-flight request |
| 3.1 | Login | login input, password input, "Sign In" button, error banner | error banner visible with exact text "Invalid username or password", fields cleared |
| 3.2 | Login | login input, password input, "Sign In" button, error banner | error banner visible with activation message, activation link present |
| 3.3 | Login | error banner, banner dismiss button | clicking dismiss hides the error banner (banner no longer visible) |
| 4.1 | Activation | password input, confirm password input, complexity rules list, "Activate Account" button | all fields visible, complexity rules displayed |
| 4.2 | Activation | password input, password strength indicator | indicator reflects strength of typed password and updates in real-time (weak → strong) as the value changes |
| 4.3 | Activation | password input, confirm password input, mismatch error message | when password ≠ confirm password, a "passwords do not match" error message is displayed |
| 4.4 | Activation | "Activate Account" button, password + confirm password inputs | on submit: button shows a loading indicator, password + confirm-password fields disabled during the in-flight activate request |
| 5.1 | Activation | password input, confirm password input, "Activate Account" button, success icon, success text, "Go to Sign In" button | green check icon visible, text "Account Activated!", button "Go to Sign In" visible |
| 5.2 | Activation | error icon, error text, "Request New Link" button | red X icon visible, text "Link Expired", button "Request New Link" visible |
| 6.1 | Activation → Login | "Go to Sign In" button | navigation to login page URL, login page elements visible |
