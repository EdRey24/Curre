# Curre!

Curre! is an Android running app that tracks runs and automatically keeps trusted contacts informed through safety check-ins and email alerts.

## Highlights

Here are the main features of Curre!:

1. Emergency Contacts to be informed of your runs
2. Start and End Run emails to Emergency Contacts with last location
3. Multiple saftey modes to fit your preference
4. Distance, duration, pace, and calorie tracking for runs
5. Map of route ran with segmentation for paused segments

## Authors

| Name           | Email          | Role(s)                                                   |
| -------------- | -------------- | --------------------------------------------------------- |
| Edward Reyna   | edwarz@bu.edu  | Full-Stack Developer / DevOps Engineer                    |
| Catalina Huynh | chuynhk@bu.edu | Quality Assurance (QA) Engineer / Co-Project Manager (PM) |
| Yuting Lin     | linyt@bu.edu   | UI/UX Designer / Frontend Developer                       |
| Namig Mirzayev | namigm@bu.edu  | Technical Lead Architect                                  |

TA: GitHub username is TimJackman

## Curre - Local Development Setup

This guide will walk you through compiling and running both the Spring Boot backend and the Android frontend on your local machine.

### Prerequisites

Before you begin, ensure you have the following installed on your computer:

1. **Java Development Kit (JDK) 21 or higher**
2. **IntelliJ IDEA** (Recommended for the Spring Boot backend)
3. **Android Studio** (Required for the Android frontend)
4. Android Emulator configured in Android Studio using the Medium Phone config.

---

### Step 1: Configure Environment Variables

The Curre backend relies on **SendGrid** (for emergency emails) and **Twilio** (for emergency SMS text messages). To test the safety alert features, you must provide your own API keys.

You will need to gather the following credentials:

- `sendgrid.api.key`
- `sendgrid.from.email`
- `twilio.account.sid`
- `twilio.auth.token`
- `twilio.from.phone`

_(Note: If you do not have these, you can create free trial accounts at SendGrid.com and Twilio.com)._

---

### Step 2: Running the Backend (Spring Boot)

The backend handles user authentication, run tracking, and emergency safety monitoring.

1. Open **IntelliJ IDEA**.
2. Click **File > Open...** and select the `backend` folder from this repository.
3. Wait for IntelliJ to index the files and sync the dependencies (watch the progress bar at the bottom right).
4. Locate the main application class (`CurreServerApplication.java`) in the `backend/src/main/java/edu/bu/cs411/group10/curre` directory.

#### Adding your API Keys to IntelliJ

To add the API keys to your local Run Configuration:

1. Next to the green Play button at the top of IntelliJ, click the dropdown menu and select **Edit Configurations...**.
2. Select the Spring Boot application on the left side.
3. Look for the field labeled **Environment variables**. _(If you don't see it, click "Modify options" or "Alt-M" and check "Environment variables")._
4. Click the small folder/document icon next to the input box to open the variables window.
5. Add the following variables with your specific keys:
   - Name: `sendgrid.api.key` | Value: `<insert here>`
   - Name: `sendgrid.from.email` | Value: `<insert here>`
   - Name: `twilio.account.sid` | Value: `<insert here>`
   - Name: `twilio.auth.token` | Value: `<insert here>`
   - Name: `twilio.from.phone` | Value: `<insert here>`
6. Click **Apply** and **OK**.

#### Start the Server

1. Click the green **Play** button next to the `main` method to start the server.
2. The Spring Boot backend should now be running on `http://localhost:8080`.
   _(Note: The SQLite database file will automatically be generated in your backend directory upon starting)._

---

### Step 3: Running the Frontend (Android App)

1. Open **Android Studio**.
2. Click **File > Open...** and select the `frontend` folder from this repository.
3. Wait for Android Studio to completely finish the Gradle Sync (this can take a few minutes on the first run).

#### Compile and Run

1. Select your target device (the Medium Phone emulator) from the device dropdown menu in the top toolbar.
2. Click the green **Run 'app'** button (Shift + F10).
3. The APK will compile and automatically launch on your device!
