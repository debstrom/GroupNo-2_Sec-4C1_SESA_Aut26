# GeoShield — Smart Emergency Response & Personal Safety Application

> One tap to alert trusted contacts, share your live location, and get help faster.

**Course Project · Group No. 2 · Section 4C1 · SESA · Autumn 2026**
Department of Computer Science and Engineering, University of Information Technology and Sciences (UITS)

---

## Table of Contents

- [About the Project](#about-the-project)
- [Problem Statement](#problem-statement)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Tech Stack](#tech-stack)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
- [Backend API](#backend-api)
- [Project Documents](#project-documents)
- [Current Status & Limitations](#current-status--limitations)
- [Future Scope](#future-scope)
- [Team](#team)

---

## About the Project

GeoShield is an Android-based personal safety system built for people who may not have time to unlock a phone, find a contact and make a call during an emergency. With a single SOS tap, the app collects the user's GPS location, alerts pre-selected trusted contacts, and keeps them updated. A companion **Parent/Guardian app** receives the alert as a full-screen alarm notification, so the guardian cannot easily miss it.

The interface is **bilingual (English / বাংলা)** and designed around the safety context of Bangladesh, including shortcuts to the nearest *thana* (police station) and hospital.

## Problem Statement

In a dangerous or medical emergency, a person often cannot:

- unlock the phone and open an app,
- search for a contact and call or type a message,
- explain exactly where they are, especially while moving.

Traditional SOS messages share only a static location. GeoShield reduces the steps between "something is wrong" and "someone knows", and keeps location information current.

## Key Features

### User App (`GeoShield-main`)

| Feature | Description |
|---|---|
| **One-tap SOS** | Large SOS button on the home screen starts the emergency flow immediately. |
| **SOS Active screen** | Shows live status for location streaming, contacts alerted, and evidence recording. |
| **Automatic SMS alerts** | Sends an SMS with a Google Maps link of the current location to all trusted contacts. |
| **Trusted contacts** | Add, edit and remove emergency contacts (stored locally with SQLite and synced with the backend). |
| **Fake call** | Schedules a realistic incoming call to help the user leave an uncomfortable or unsafe situation. |
| **Share live link** | Share the current location link through any installed app. |
| **Nearest help** | Quick access to the nearest thana and hospital, with tap-to-call. |
| **Safety heat map** | Area-wise safety levels (Safe / Moderate / Risky / Dangerous) with a searchable map view. |
| **Incident reporting** | Dialog for reporting incidents in an area. |
| **"I'm Safe" / false alarm** | Stops sharing and notifies contacts that the alert was cancelled. |
| **Bilingual UI** | Toggle between English and Bangla. |
| **Account & profile** | Sign up, log in, change password, view profile. |

### Guardian App (`geoshieldParent`)

- Guardian login using the child's user ID and email.
- Receives emergency alerts through **Firebase Cloud Messaging (FCM)**.
- Full-screen alert that can turn the screen on over the lock screen, with a foreground **alarm service** (sound and vibration).
- Displays the user's last known location.

### Backend (`geosheild_backend`)

- REST API built with Node.js and Express, backed by PostgreSQL.
- Stores users, emergency contacts, location history and emergency events.

## System Architecture

```
┌────────────────────┐        HTTP / JSON        ┌──────────────────────┐        ┌──────────────┐
│  GeoShield (User)  │ ────────────────────────► │   Express REST API   │ ─────► │  PostgreSQL  │
│  Android · Java    │                           │   Node.js            │        └──────────────┘
└─────────┬──────────┘                           └──────────┬───────────┘
          │ SMS (SmsManager)                                │ FCM token lookup
          ▼                                                 ▼
┌────────────────────┐                           ┌──────────────────────┐
│  Trusted contacts  │                           │ GeoShield Parent App │
└────────────────────┘                           │ Android · FCM alarm  │
                                                 └──────────────────────┘
```

## Tech Stack

| Layer | Technologies |
|---|---|
| User app | Java 17, Android XML layouts, Material Components, RecyclerView, Fragments, Volley, SQLite, SharedPreferences |
| Guardian app | Java, Firebase Cloud Messaging, Firebase Auth, Volley, Foreground Service |
| Backend | Node.js, Express 5, PostgreSQL (`pg`), `dotenv`, `cors`, `bcrypt`, `jsonwebtoken` |
| Tooling | Android Studio, Gradle, npm, Git & GitHub |

**Android requirements:** User app `minSdk 26`, Guardian app `minSdk 24`.

## Repository Structure

```
GroupNo-2_Sec-4C1_SESA_Aut26/
├── GeoShield-main/              # User-facing Android app
│   └── app/src/main/java/com/uits/geoshield/
│       ├── activities/          # Signup, Main, SOS Active, Fake Call
│       ├── fragments/           # Home, Map, Contacts, Profile
│       ├── adapters/            # Contact and help-place lists
│       ├── models/              # Contact, Zone, HelpPlace
│       ├── utils/               # SOS helper, repositories, prefs, share utils
│       └── views/               # Custom views (SOS button, heat map, action card)
├── geoshieldParent/             # Guardian / parent Android app (FCM alerts)
├── geosheild_backend/           # Node.js + Express + PostgreSQL API
└── projects-neccessary-files/   # SRS, PRD, proposal, ER diagram, test report, UI/UX design
```

## Getting Started

### Prerequisites

- Android Studio (latest stable) with JDK 17
- Node.js 18+ and npm
- A PostgreSQL database
- A Firebase project (for the Guardian app)
- A physical Android device with a SIM is recommended for SMS and call features

### 1. Run the backend

```bash
cd geosheild_backend
npm install
```

Create a `.env` file in `geosheild_backend/`:

```env
DATABASE_URL=postgresql://<user>:<password>@<host>:<port>/<database>
PORT=3000
```

Start the server:

```bash
npm start          # production
npm run dev        # development with nodemon
```

You should see `Database Connected Successfully` in the console. Opening `http://localhost:3000/` returns a hello message.

### 2. Run the user app (`GeoShield-main`)

1. Open the `GeoShield-main` folder in Android Studio and let Gradle sync.
2. In `app/src/main/res/values/strings.xml`, set `MAIN_URL` to your backend address (use your computer's LAN IP when testing on a real device, for example `http://192.168.x.x:3000`).
3. Run the app on a device or emulator and grant Location, SMS, Phone permissions when asked.

### 3. Run the Guardian app (`geoshieldParent`)

1. Create a Firebase project and add an Android app with package name `com.example.geoshieldparent`.
2. Download your own `google-services.json` and place it at `geoshieldParent/app/google-services.json`.
3. Set the backend URL used by the app, then build and run.
4. Log in with the child's user ID and registered email.

## Backend API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/` | Health check |
| `POST` | `/signup` | Register a new user |
| `POST` | `/login` | User login |
| `PUT` | `/changepass` | Change password |
| `GET` | `/profile` | Fetch user profile |
| `POST` | `/parent_login` | Guardian login; stores the guardian's FCM token |
| `POST` | `/emergency_contact` | Add an emergency contact |
| `GET` | `/emergency_contact` | List a user's emergency contacts |
| `PUT` | `/emergency_contact` | Update an emergency contact |
| `DELETE` | `/emergency_contact` | Remove an emergency contact |
| `POST` | `/location` | Save the user's current location |
| `GET` | `/location` | Get the user's latest location |
| `POST` | `/emergency` | Record an emergency event |

## Project Documents

All planning and design documents are in [`projects-neccessary-files/`](projects-neccessary-files/):

- `GeoShield_Project_Proposal.pdf` — problem statement, objectives, SWOT, schedule, risk and cost analysis
- `GeoSheild_SRS.ofc.pdf` — Software Requirements Specification (IEEE 830 style)
- `GeoShield_PRD.pdf` — Product Requirements Document
- `GeoShield_ER_Diagram.pdf` — database design
- `GeoShield_Test_Report.pdf` — testing report
- `GeoShield – Mobile UI UX Design (1).html` — UI/UX mockups

## Current Status & Limitations

This is an academic prototype. Known limitations:

- The safety heat map currently uses **sample zone data** (`ZoneRepository`) and is not yet connected to live incident data.
- The backend URL is configured manually in the app, and the apps allow cleartext HTTP for local testing. Use HTTPS in any real deployment.
- Password handling and authentication on the backend are basic and should be hardened (hashed passwords, token-based auth) before real-world use.
- Audio/video evidence upload is part of the design but is not fully implemented.

## Future Scope

- Live incident data feeding the safety heat map
- Secure authentication with hashed passwords and JWT sessions
- Background audio/video evidence recording and chunked upload
- Real-time live location tracking page for guardians
- Voice or shake-to-trigger SOS
- Integration with local emergency services and helplines
- iOS support

## Team

**Group No. 2 — Section 4C1 (SESA, Autumn 2026)**

| Name | ID |
|---|---|
| Debjeet Baral | 04325105101120 |
| Aishi Majumder | 04325105101127 |
| Mst Mymuna Akter | 04325105101128 |
| Iffati Rahaman Bhuiyan | 04325105101129 |

**Supervised by:**

| Name | Designation |
|---|---|
| Dhrubo Barua | Lecturer |
| Shad Ahmed | Lecturer |

---

> ⚠️ GeoShield is an academic project and does not replace official emergency services. In a real emergency, call **999**.
