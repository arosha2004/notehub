# 🎤 NoteHub — Speaker Notes (Strict 10 Minutes)
### Lean & Clear — Every Section Fits the Time Exactly

> ⚡ **These notes are SHORT and DIRECT. Say exactly what is written. Don't add extra words.**
> ⏱️ **Each screen = about 1 minute. Stick to the time!**

---

## ⏱️ TIME PLAN

| Section | Time |
|---|---|
| Opening | 45 sec |
| Login Screen | 1 min |
| Sign Up Screen | 1 min |
| Dashboard Screen | 1.5 mins |
| Notes Screen | 1.5 mins |
| Add Note Screen | 1 min |
| Uploads Screen | 45 sec |
| Settings + Dark Mode | 1.5 mins |
| Closing | 45 sec |
| **Total** | **~10 mins** |

---

---

## 🟦 OPENING ⏱️ 45 seconds

> *"Good morning. I am presenting my project — **NoteHub**, an Android note-taking application.*
>
> *It is built using **Kotlin**, **Jetpack Compose**, and **Material Design 3**.*
>
> *The app has **7 screens** — Login, Sign Up, Dashboard, Notes, Add Note, Uploads, and Settings.*
>
> *Let me walk you through each screen."*

---

## 📱 SCREEN 1 — LOGIN ⏱️ 1 minute

*[Show Login screen]*

> *"This is the **Login Screen** — the first screen when the app opens.*
>
> *It has a **gradient background**, NoteHub logo, and a white card with the login form.*
>
> *The **email** and **password** fields are both required.*
>
> *The password is hidden by default — the user can tap the **eye icon** to show or hide it.*
>
> *The **Sign In button** is disabled until both fields are filled. Once filled, it activates.*
>
> *Tapping Sign In shows a loading spinner and navigates to the Dashboard.*
>
> *There is also a **Sign Up link** at the bottom for new users."*

---

## 📱 SCREEN 2 — SIGN UP ⏱️ 1 minute

*[Navigate to Sign Up]*

> *"This is the **Sign Up Screen** — for new user registration.*
>
> *It has **4 fields** — Full Name, Email, Password, and Confirm Password.*
>
> *Each password field has an eye toggle to show or hide the text.*
>
> *If the two passwords **do not match**, a red error message appears: 'Passwords do not match'.*
>
> *The **Create Account button** is only enabled when all 4 fields are filled AND both passwords match.*
>
> *The screen is **scrollable** because there are many fields.*
>
> *There is a Login link at the bottom to go back."*

---

## 📱 SCREEN 3 — DASHBOARD ⏱️ 1.5 minutes

*[Log in → Dashboard]*

> *"After login, the user lands on the **Dashboard** — the home screen.*
>
> *At the top is a **Welcome Card** with an indigo-to-purple gradient background, a welcome message, and a Create New Note button.*
>
> *Below that is the **Overview section** — a 2 by 2 statistics grid showing:*
> - *Total Notes — in blue*
> - *This Month — in green*
> - *Categories — in yellow*
> - *Uploads — in blue*
>
> *Each stat card has a colored icon inside a softly tinted rounded box.*
>
> *At the bottom is the **Quick Actions section** — two colored buttons: 'New Note' in blue and 'Upload File' in green.*
>
> *The entire screen is scrollable.*
>
> *Currently the numbers are hardcoded — in future they will come from a database."*

---

## 📱 SCREEN 4 — NOTES ⏱️ 1.5 minutes

*[Tap Notes tab]*

> *"This is the **Notes Screen** — it displays all saved notes.*
>
> *The heading shows the total count — for example 'All Notes (1)'.*
>
> *Notes are shown in a **LazyColumn** — an efficient scrollable list that only renders what is visible on screen.*
>
> *Each note appears as a **Note Card** with:*
> - *A **colored bar** on the left — matching the note's color*
> - *A **category tag** — like Study or Work*
> - *The **title** in bold — max 1 line*
> - *A **content preview** — max 2 lines*
> - *The **date** at the bottom*
> - *A **3-dot menu** on the right*
>
> *If there are no notes, an empty state 'No notes yet' is shown.*
>
> *The **blue + button** in the bottom right opens the Add Note screen."*

---

## 📱 SCREEN 5 — ADD NOTE ⏱️ 1 minute

*[Tap + button]*

> *"This is the **Add Note Screen**.*
>
> *When it opens, the bottom navigation bar is hidden — this screen has its own top bar.*
>
> *On the top bar: a **back arrow** on the left and a **Save button** on the right.*
>
> *The Save button is **only active** when both title and content fields have text — otherwise it stays grey.*
>
> *The user selects a **Category** using filter chips — Study, Work, Personal, or Ideas.*
>
> *Then fills in the **Title** field and the **Content** area.*
>
> *There is also a **color picker** to assign a color to the note."*

---

## 📱 SCREEN 6 — UPLOADS ⏱️ 45 seconds

*[Tap Uploads tab]*

> *"This is the **Uploads Screen** — for managing uploaded files.*
>
> *At the top is a **blue header card** showing '0 files' currently.*
>
> *Since no files exist yet, the screen shows 'No uploads yet' in the center.*
>
> *When files are added in future, it will show a **storage progress bar**, **filter chips** for All Files, Images and Documents, and a **list of file cards**.*
>
> *The **upload button** in the bottom right initiates file uploads."*

---

## 📱 SCREEN 7 — SETTINGS ⏱️ 1.5 minutes

*[Tap Settings tab]*

> *"This is the **Settings Screen** — with 5 sections.*
>
> ***Section 1 — Profile Settings:** Edit Full Name and Email. A Save Changes button applies the changes.*
>
> ***Section 2 — Change Password:** Three password fields with eye toggles. The Update button is only active when all fields are filled and new passwords match.*
>
> ***Section 3 — Appearance — Dark Mode.*"
>
> *[TAP THE DARK MODE TOGGLE]*
>
> *"Watch — the entire app instantly switches to dark theme. Background becomes deep purple-black, cards turn dark, text turns white.*
>
> *[TAP AGAIN TO RETURN]*
>
> *This works through the **ThemeManager** — a singleton that holds the theme state using mutableStateOf. When toggled, NoteHubTheme picks the dark color scheme and the app redraws automatically.*
>
> ***Section 4 — Preferences:** Notifications toggle.*
>
> ***Section 5 — About:** Shows app version and policy links.*
>
> *At the bottom is the **red Log Out button** — it clears all screen history and returns the user to Login."*

---

## 🔚 CLOSING ⏱️ 45 seconds

> *"That concludes the NoteHub demo.*
>
> *The app features: secure login and registration, a stats dashboard, notes with color labels and categories, uploads, and settings with a working dark mode.*
>
> *Future improvements include: **Room Database** for saving notes, **Firebase** for real authentication, and complete **file upload** functionality.*
>
> *Thank you. I am happy to answer any questions."*

---

---

## ⚡ EMERGENCY TIME TIPS

> **Running slow?** → Cut the Uploads screen entirely. Say: *"The Uploads screen shows a file list — currently it shows an empty state as no files are uploaded yet."* and move on.
>
> **Running fast?** → After the Dashboard, pause and say: *"The bottom navigation bar switches between the four main screens — Dashboard, Notes, Uploads, and Settings. Each tab highlights in blue when active."*

---

> 🎤 **Remember: Short. Clear. Confident. Don't rush — 10 minutes is enough!**
>
> ✅ **All the best! 💪**
>
> 📁 *NoteHub — Strict 10-Minute Speaker Notes | 2026*
