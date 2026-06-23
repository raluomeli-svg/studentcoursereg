# NiMet Course Registration System (RCRS)

A desktop course registration application for the **Nigerian Meteorological Agency (NiMet) Meteorological Training School**. Built with plain Java and Swing — no external frameworks.

---

## Programmes supported

| Code  | Full Name |
|-------|-----------|
| BIPMT | Basic Instructional Package for Meteorological Technicians |
| BIPM  | Basic Instructional Package for Meteorologists |
| MFC   | Meteorological Forecasting Course |

---

## Prerequisites

- **Java JDK 11 or higher** — [Download from adoptium.net](https://adoptium.net)
- Verify your installation: `java -version`

No build tool (Maven/Gradle) is required. The project uses plain `javac`.

---

## Running in IntelliJ IDEA (recommended)

1. Open IntelliJ IDEA and choose **File → Open**, then select the `StudentCourseReg` folder.
2. IntelliJ will detect the project from `StudentCourseReg.iml` automatically.
3. Ensure the JDK is configured: **File → Project Structure → Project → SDK**.
4. In the Project panel, navigate to `src/com/nimet/rcrs/ui/RcrsApp.java`.
5. Click the **green play button (▶)** next to the `main` method, then select **Run 'RcrsApp.main()'**.

The login window will open on screen.

---

## Running from the command line

From the project root directory:

```bash
# 1. Compile
javac -d out/production $(find src -name "*.java")

# 2. Run
java -cp out/production com.nimet.rcrs.ui.RcrsApp
```

**On Windows (PowerShell):**

```powershell
# 1. Compile
$files = Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName
javac -d out\production $files

# 2. Run
java -cp out\production com.nimet.rcrs.ui.RcrsApp
```

---

## Default login credentials

| Role    | User ID         | Password     |
|---------|-----------------|--------------|
| Admin   | `admin01`       | `admin123`   |
| Student | `NMS/2024/0001` | `student123` |

> Passwords are stored as SHA-256 hashes. Students with a temporary password are prompted to change it on first login.

---

## Running the tests

The project uses **JUnit 4** (JARs are in the `lib/` folder — no download needed).

**In IntelliJ IDEA:**
1. Right-click the `test` folder in the Project panel.
2. Select **Run 'All Tests'**.

Results appear in the **Run** panel at the bottom. Green checkmarks = passed, red X = failed.

**From the command line:**

```powershell
# Compile tests (run after compiling sources above)
$testFiles = Get-ChildItem -Recurse -Filter "*.java" test | Select-Object -ExpandProperty FullName
javac -cp "out\production;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" -d out\test $testFiles

# Run all tests
java -cp "out\production;out\test;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" `
    org.junit.runner.JUnitCore `
    com.nimet.rcrs.model.CatalogueTest `
    com.nimet.rcrs.model.RegistrationTest `
    com.nimet.rcrs.service.AuthenticationServiceTest
```

Expected output: `OK (40 tests)`

---

## Project structure

```
StudentCourseReg/
├── src/
│   ├── Main.java                          # Legacy entry point
│   └── com/nimet/rcrs/
│       ├── exception/                     # RegistrationException, ErrorType
│       ├── model/                         # Course, Catalogue, Registration, Student, Admin, User, Result, Programme
│       ├── repository/                    # DataStore (in-memory persistence + seed data)
│       ├── service/                       # AuthenticationService
│       └── ui/                            # RcrsApp (main window), LoginPanel, StudentDashboard, AdminDashboard
├── test/
│   └── com/nimet/rcrs/
│       ├── model/                         # CatalogueTest, RegistrationTest
│       └── service/                       # AuthenticationServiceTest
├── lib/
│   ├── junit-4.13.2.jar
│   └── hamcrest-core-1.3.jar
└── StudentCourseReg.iml
```
