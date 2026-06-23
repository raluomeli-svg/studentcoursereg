# Student Course Registration System (RCRS) — Implementation Plan

## Class Diagram Analysis

---

### 1. `Programme` — `<<enum>>`

**What it is:** A Java `enum` representing the academic programmes offered by the institution.

**Values:**
- `BIPMT` — Basic Instructional Package For Meteorological Technicians 
- `BIPM` — Basic Instructional Package For Meteorologists
- `MFC` — Meteorological Forecasting Course

**Usage:** Referenced by both `Student` (which programme a student belongs to) and `Course` (which programme a course belongs to). Acts as a type-safe constraint — only valid programme codes can be assigned.

**Java translation:** `public enum Programme { BIPMT, BIPM, MFC }`

---

### 2. `User` — `<<abstract>>`

**What it is:** The base (parent) class for all system users. It is abstract, meaning it cannot be instantiated directly — only through its subclasses (`Student` and `Admin`).

**Fields:**
- `userId: String` — unique identifier for any user
- `fullName: String` — display name
- `passwordHash: String` — hashed password (never plain text)

**Methods:**
- `login(): void` — initiates a session
- `logout(): void` — ends a session
- `verifyPassword(pwd: String): boolean` — checks a given password against the stored hash

**Java translation:** `public abstract class User { ... }`

---

### 3. `Student` — extends `User`

**What it is:** A concrete user subclass representing a registered student. Inherits all fields and methods from `User` and adds student-specific attributes.

**Fields:**
- `studentId: String` — follows the format `NMS/YYYY/NNNN` (e.g., NMS/2023/0042)
- `programme: Programme` — the enum value denoting the student's programme
- `yearOfAdmission: int` — the year the student was admitted
- `currentYear: int` — academic year the student is currently in
- `currentSemester: int` — current semester (e.g., 1 or 2)

**Methods:**
- `viewAvailableCourses(): List<Course>` — returns courses available for the student's level/semester
- `getRegistrations(): void` — fetches existing registration records for the student
- `submitRegistration(): Registration` — creates and submits a registration record

**Java translation:** `public class Student extends User { ... }`

---

### 4. `Admin` — extends `User`

**What it is:** A concrete user subclass representing an administrative staff member. Has elevated privileges to manage courses, view registrations, and generate reports.

**Fields:**
- `staffId: String` — staff identifier

**Methods:**
- `getRegistrationData(student): Student` — retrieves a student's registration details
- `removeCourse(code: String): void` — removes a course from the catalogue
- `viewCatalogue(): List<Course>` — returns all courses in the catalogue
- `seedCatalogue(): List<Registration>` — populates/seeds the course catalogue with initial data
- `generateStudentReport(): String` — generates a summary report for students

**Java translation:** `public class Admin extends User { ... }`

---

### 5. `Course`

**What it is:** Represents a single academic course with a unique code. Each course belongs to a specific programme, semester, and level.

**Fields:**
- `courseCode: String` — unique identifier for the course (e.g., `BIP110`)
- `courseTitle: String` — human-readable course name
- `programme: Programme` — which programme this course belongs to
- `semester: int` — which semester the course runs in (1 or 2)
- `level: int` — the year/level of study (1 or 2 for BIPMT; 1 for MFC and BIPM)
- `units: int` — **credit unit weight of the course** *(added from course catalogue — not in original class diagram but required for real data)*

**Methods:**
- `getCourseCode(): String` — returns the course code

**Java translation:** `public class Course { ... }`

---

### 6. `Catalogue`

**What it is:** A container/manager class that holds the full list of courses available in the system. It provides filtering and lookup operations over the course list.

**Fields:**
- `courses: List<Course>` — the internal collection of all courses

**Methods:**
- `filterByLvl_sem(sem: int): List<Course>` — returns courses matching a given level and/or semester
- `addCourse(course: Course): void` — adds a new course to the catalogue
- `removeCourse(course: Course): void` — removes an existing course
- `findByCode(code: String): Course` — looks up a course by its unique code
- `findCourses(): List<Course>` — retrieves courses by some criteria
- `getAllCourses(): List<Course>` — returns the full course list

**Java translation:** `public class Catalogue { ... }`

---

### 7. `Registration`

**What it is:** The core transactional entity of the system. It represents one student's course selection for a given semester. It enforces business rules around minimum and maximum course load.

**Fields:**
- `registrationId: String` — unique ID for this registration record
- `student: Student` — the student this registration belongs to
- `selectedCourses: Set<Course>` — courses the student has selected (a `Set` prevents duplicates)
- `semester: String` — the semester identifier, e.g., `"2020/1"` (year/semester)
- `submittedAt: LocalDateTime` — the timestamp when the registration was finalised

**Methods:**
- `addCourse(course: Course): void` — adds a course to the selection
- `removeCourse(course: Course): boolean` — removes a course; returns `true` if removed
- `getCourse(course: Course): boolean` — checks if a course is already selected
- `validateCourseCount(): void` — enforces min/max course limits; throws `RegistrationException` if violated
- `submit(): void` — finalises the registration; `throws RegistrationException`
- `getSelectedCourses(): Set<Course>` — returns the current selected courses

**Java translation:** `public class Registration { ... }`

---

### 8. `AuthenticationService`

**What it is:** A service class responsible for managing user authentication. It handles password hashing, session management, and user verification. It is not a model — it is a stateless (or lightly stateful) service.

**Methods:**
- `authenticate(pwd: String): User` — validates credentials and returns the authenticated User
- `hashPassword(pwd: String): String` — hashes a plain-text password
- `getCurrentUser(): User` — returns the currently logged-in user
- `endSession(): void` — clears the active session
- `validateSession(): boolean` — checks if there is an active valid session

**Java translation:** `public class AuthenticationService { ... }`

---

### 9. `DataStore` — `<<repository>>`

**What it is:** The persistence layer of the application. Acts as an in-memory (or file-backed) repository for students, courses, and registrations. The `<<repository>>` stereotype signals that this is a data access object (DAO/repository pattern).

**Fields:**
- `storage` — the internal data store (likely a `Map<String, Object>` or separate maps per entity type)

**Methods:**
- `saveStudent(s: Student): void` — persists a student
- `loadStudent(id: String): Student` — retrieves a student by ID
- `saveCourse(c: Course): void` — persists a course
- `loadCourse(code: String): void` — retrieves a course by code
- `deleteCourse(code: String): void` — removes a course from storage
- `loadRegistration(): void` — loads registration records
- `findRegistration(student: Student): List` — finds all registrations for a given student

**Java translation:** `public class DataStore { ... }`

---

### 10. `RegistrationException` — `<<extends>>`

**What it is:** A custom exception thrown when registration business rules are violated. The `<<extends>>` stereotype means it extends a standard Java exception (e.g., `Exception` or `RuntimeException`).

**Inner type — `ErrorType` (enum):**
- `TOO_FEW_COURSES` — thrown when the student has selected fewer than the minimum required courses
- `TOO_MANY_COURSES` — thrown when the student has selected more than the maximum allowed courses

**Fields:**
- `errorType: ErrorType` — the specific type of registration error

**Methods:**
- `getErrorCode(): int` — returns a numeric code for the error
- `getMessage(): String` — returns a human-readable error message (overrides `Exception.getMessage()`)

**Java translation:** `public class RegistrationException extends Exception { ... }` with `ErrorType` as a nested enum.

---

### 11. Course Catalogue Data — `NiMet_Courses_No_RSV.docx`

**What it is:** The real course data that must be seeded into the system via `Admin.seedCatalogue()` / `DataStore`. This is the source of truth for all `Course` objects loaded at runtime.

**Structure of each record:** `Programme | Year (level) | Semester | Course Code | Course Title | Units`

> **Parsing note:** The docx had OCR/formatting artefacts where the unit number was embedded inside the course title string (e.g., `"Synoptic Meteorology & Weather A4nalysis"` → title is `"Synoptic Meteorology & Weather Analysis"`, units = `4`). Cleaned values are shown below.

---

#### BIPMT — Basic Instructional Package for Meteorological Technicians

**Year 1 · Semester 1** (8 courses)
| Code | Title | Units |
|---|---|---|
| BIP110 | Meteorological Observations, Coding & Plotting I | 4 |
| BIP111 | General Physics | 3 |
| BIP112 | General Studies | 2 |
| BIP113 | General Statistics | 3 |
| BIP114 | General Meteorology | 3 |
| BIP115 | Agrometeorology | 4 |
| BIP116 | General Mathematics | 3 |
| BIP117 | Introduction to Computer Studies | 2 |

**Year 1 · Semester 2** (8 courses)
| Code | Title | Units |
|---|---|---|
| BIP120 | Upper Air Observations | 3 |
| BIP121 | Meteorological Instrumentation | 3 |
| BIP122 | Vector Analysis | 3 |
| BIP123 | Physical Meteorology | 3 |
| BIP124 | Climatology | 3 |
| BIP125 | Hydrometeorology | 3 |
| BIP126 | Remote Sensing & GIS | 3 |
| BIP127 | Aeronautical Meteorology I | 3 |

**Year 2 · Semester 1** (8 courses)
| Code | Title | Units |
|---|---|---|
| BIP210 | Aeronautical Meteorology II | 3 |
| BIP211 | Synoptic Meteorology & Weather Analysis | 4 |
| BIP212 | Differential Equation | 3 |
| BIP213 | Research Methodology and Applications | 3 |
| BIP214 | Satellite Meteorology | 3 |
| BIP215 | Environmental Science | 3 |
| BIP216 | Atmospheric Thermodynamics | 3 |
| BIP217 | Field Trip | 3 |

**Year 2 · Semester 2** (8 courses)
| Code | Title | Units |
|---|---|---|
| BIP220 | Dynamic Meteorology | 3 |
| BIP221 | Climate Change | 2 |
| BIP222 | Meteorological Observations, Coding & Plotting II | 3 |
| BIP223 | Marine Meteorology | 3 |
| BIP224 | Climatological Returns | 3 |
| BIP225 | Oral Examination | 2 |
| BIP226 | On the Job Training (OJT) | 3 |
| BIP227 | Project | 6 |

---

#### MFC — Meteorological Forecasting Course

**Year 1 · Semester 1** (12 courses)
| Code | Title | Units |
|---|---|---|
| BIP310 | Physical Meteorology | 3 |
| BIP311 | Agricultural Meteorology | 3 |
| BIP312 | Aeronautical Meteorology II | 3 |
| BIP313 | Atmospheric Thermodynamics | 3 |
| BIP314 | Biometeorology | 3 |
| BIP315 | Climatology | 3 |
| BIP316 | Introduction to Data Analysis | 3 |
| BIP317 | Vector Analysis | 2 |
| BIP318 | Differential Equation | 3 |
| BIP319 | Research Methodology | 3 |
| BIP320 | Marine Meteorology II | 3 |
| BIP330 | OJT | 3 |

**Year 1 · Semester 2** (10 courses)
| Code | Title | Units |
|---|---|---|
| BIP321 | Dynamic Meteorology | 3 |
| BIP322 | Synoptic Meteorology and Weather Analysis | 3 |
| BIP323 | Hydrological Meteorology | 2 |
| BIP324 | Satellite Meteorology | 3 |
| BIP325 | Military Meteorology | 2 |
| BIP326 | Urban Climatology | 3 |
| BIP327 | Introduction to Numerical Weather Prediction | 2 |
| BIP328 | Applied Meteorology | 2 |
| BIP329 | Field Trip | 2 |
| BIP331 | Student Project | 6 |

---

#### BIPM — Basic Instructional Package for Meteorologists

**Year 1 · Semester 1** (10 courses)
| Code | Title | Units |
|---|---|---|
| BIP410 | Upper Air Observations | 3 |
| BIP411 | Meteorological Observations, Coding & Plotting | 4 |
| BIP412 | Aeronautic Meteorology I | 3 |
| BIP413 | Research Methodology | 3 |
| BIP414 | Climatology | 3 |
| BIP415 | Physical Meteorology | 3 |
| BIP416 | Agricultural Meteorology | 4 |
| BIP417 | Remote Sensing / Satellite Meteorology | — |
| BIP418 | Marine Meteorology | 3 |
| BIP419 | Atmospheric Thermodynamics | 3 |

**Year 1 · Semester 2** (10 courses)
| Code | Title | Units |
|---|---|---|
| BIP420 | Climate Change Sciences | 2 |
| BIP421 | GIS Applications in Meteorology | 3 |
| BIP422 | Synoptic Meteorology & Weather Analysis | — |
| BIP423 | Aeronautical Meteorology II | 3 |
| BIP424 | Hydrometeorology | 3 |
| BIP425 | Dynamic Meteorology | 3 |
| BIP426 | Applied Meteorology | 3 |
| BIP427 | Statistical Applications in Meteorology | — |
| BIP428 | OJT | 3 |
| BIP429 | Project | 6 |

> **`—` units:** BIP417, BIP422, BIP427 had no parseable unit value in the source document. Confirm with stakeholder before seeding.

---

## Relationships

| Relationship | From → To | Type | Description |
|---|---|---|---|
| Inheritance | `Student` → `User` | Generalisation (extends) | Student IS-A User; inherits userId, fullName, passwordHash, login/logout |
| Inheritance | `Admin` → `User` | Generalisation (extends) | Admin IS-A User; same base identity but elevated role |
| Association | `Student` → `Programme` | Uses (enum value) | A student belongs to exactly one programme |
| Association | `Course` → `Programme` | Uses (enum value) | A course belongs to exactly one programme |
| Aggregation | `Catalogue` → `Course` | Has-many (1 → *) | Catalogue holds a list of courses; courses can exist independently |
| Composition | `Registration` → `Student` | Owns (1 → 1) | A registration is meaningless without a student |
| Association | `Registration` → `Course` | Uses-many (1 → *) | A registration references a set of courses |
| Dependency | `Admin` → `Catalogue` | Uses | Admin calls Catalogue methods to manage courses |
| Dependency | `Student` → `Registration` | Creates | Student initiates and submits a Registration |
| Dependency | `AuthenticationService` → `User` | Returns/Uses | Auth service produces/validates User objects |
| Repository | `DataStore` → `Student`, `Course`, `Registration` | Stores | DataStore persists and retrieves all core entities |
| Throws | `Registration` → `RegistrationException` | Throws | submit() and validateCourseCount() throw this exception |
| Extends | `RegistrationException` → `Exception` | Generalisation | Custom exception built on Java's standard Exception |

---

## Proposed Java File Structure (Separation of Concerns)

```
src/
└── main/
    └── java/
        └── com/nimet/rcrs/
            │
            ├── Main.java                     ← Entry point — bootstraps and drives the CLI loop
            │
            ├── model/                        ← Pure data/domain objects
            │   ├── Programme.java            (enum — Programme values)
            │   ├── User.java                 (abstract class — base user)
            │   ├── Student.java              (extends User)
            │   ├── Admin.java                (extends User)
            │   ├── Course.java               (course entity)
            │   ├── Catalogue.java            (course collection + filtering)
            │   └── Registration.java         (core transaction entity)
            │
            ├── service/                      ← Business logic & operations
            │   └── AuthenticationService.java
            │
            ├── repository/                   ← Data persistence layer
            │   └── DataStore.java
            │
            └── exception/                    ← Custom exceptions
                ├── RegistrationException.java
                └── ErrorType.java            (enum — TOO_FEW, TOO_MANY)
```

> **Note on "similar models in the same file":** In Java, only one `public` top-level class is allowed per file. The grouping above achieves separation of concerns via packages. `ErrorType` may optionally live as a nested `enum` inside `RegistrationException.java` since they are tightly coupled.

---

## TODO Checklist

### Setup
- [ ] Create Maven/Gradle project with package `com.nimet.rcrs`
- [ ] Create the directory structure above
- [ ] Add `java.time.*` import where `LocalDateTime` is used

---

### `Main.java` — Entry Point & Startup Sequence

**Startup sequence:**

```
1. Bootstrap
   └── new DataStore()
   └── new Catalogue()
   └── new AuthenticationService(dataStore)
   └── admin.seedCatalogue()          // loads all 60 courses into DataStore + Catalogue

2. Login prompt (loop until valid)
   └── prompt: userId + password
   └── authService.authenticate(userId, password)
       ├── success → returns User (Student or Admin)
       └── failure → print error, retry

3. Role dispatch  (instanceof check on returned User)
   │
   ├── Student menu loop
   │   ├── [1] View available courses
   │   │     └── student.viewAvailableCourses()
   │   │           └── catalogue.filterByLvl_sem(student.currentYear, student.currentSemester)
   │   │
   │   ├── [2] Register courses
   │   │     └── new Registration(student, semester)
   │   │     └── loop: addCourse() until done
   │   │     └── registration.submit()         // throws RegistrationException
   │   │           ├── success → confirm message
   │   │           └── catch RegistrationException → print errorType + message, stay in menu
   │   │
   │   ├── [3] View my registrations
   │   │     └── student.getRegistrations()
   │   │
   │   └── [0] Logout
   │         └── authService.endSession()  → back to step 2
   │
   └── Admin menu loop
       ├── [1] View catalogue
       │     └── admin.viewCatalogue()
       │
       ├── [2] Add course
       │     └── prompt course details → new Course(...)
       │     └── catalogue.addCourse(course)  +  dataStore.saveCourse(course)
       │
       ├── [3] Remove course
       │     └── prompt courseCode → admin.removeCourse(code)
       │
       ├── [4] View student registration
       │     └── prompt studentId → admin.getRegistrationData(student)
       │
       ├── [5] Generate report
       │     └── admin.generateStudentReport()
       │
       └── [0] Logout
             └── authService.endSession()  → back to step 2
```

**Implementation tasks:**
- [x] **`Main.java`** — Create class with `public static void main(String[] args)`; instantiate `DataStore`, `Catalogue`, `AuthenticationService`; call seed on startup
- [x] Implement the login loop using `Scanner` for console input; handle failed auth gracefully with a retry limit (e.g., 3 attempts then exit)
- [x] Implement student menu loop dispatching to `Student` and `Registration` methods
- [x] Implement admin menu loop dispatching to `Admin`, `Catalogue`, and `DataStore` methods
- [x] Wrap `registration.submit()` in a `try/catch RegistrationException` block; print `errorType` and `getMessage()` on failure
- [x] Exit condition: `[0] Logout` returns to login prompt; a second `[0]` (or `CTRL+C`) exits the JVM

---

### `exception/` package
- [x] **`ErrorType.java`** — Create enum with values `TOO_FEW_COURSES`, `TOO_MANY_COURSES`
- [x] **`RegistrationException.java`** — Extend `Exception`, add `errorType: ErrorType` field, implement `getErrorCode()` and override `getMessage()`

---

### `model/` package
- [x] **`Programme.java`** — Create enum with `BIPMT`, `BIPM`, `MFC` (confirmed by updated class diagram and course catalogue)
- [x] **`User.java`** — Create `abstract class User` with `userId`, `fullName`, `passwordHash` fields; declare `login()`, `logout()`, `verifyPassword()` methods
- [x] **`Student.java`** — Extend `User`; add `studentId`, `programme`, `yearOfAdmission`, `currentYear`, `currentSemester`; implement `viewAvailableCourses()`, `getRegistrations()`, `submitRegistration()`
- [x] **`Admin.java`** — Extend `User`; add `staffId`; implement `getRegistrationData()`, `removeCourse()`, `viewCatalogue()`, `seedCatalogue()`, `generateStudentReport()`
- [x] **`Course.java`** — Add `courseCode`, `courseTitle`, `programme`, `semester`, `level`, **`units`** (credit units from catalogue); implement `getCourseCode()`; override `equals()`/`hashCode()` on `courseCode` for use in `Set<Course>`
- [x] **`Catalogue.java`** — Add `courses: List<Course>`; implement all filter/add/remove/find methods
- [x] **`Registration.java`** — Add all fields; implement `addCourse()`, `removeCourse()`, `getCourse()`, `validateCourseCount()` (enforce min/max — **see programme-specific counts below**), `submit()` (calls validate, sets `submittedAt`), `getSelectedCourses()`; annotate `submit()` with `throws RegistrationException`

---

### `service/` package
- [x] **`AuthenticationService.java`** — Implement `authenticate()`, `hashPassword()`, `getCurrentUser()`, `endSession()`, `validateSession()`; manage a `currentUser` session field internally

---

### `repository/` package
- [x] **`DataStore.java`** — Implement in-memory storage (e.g., `HashMap` per entity type); implement all save/load/delete/find methods

---

### Integration & Wiring
- [ ] Wire `AuthenticationService` to `User` hierarchy (accept `Student` or `Admin` on login)
- [ ] Wire `Student.viewAvailableCourses()` to call `Catalogue.filterByLvl_sem()` based on the student's `currentYear` and `currentSemester`
- [ ] Wire `Registration.validateCourseCount()` to throw correct `RegistrationException` with appropriate `ErrorType`
- [ ] Wire `Admin` methods to operate on `Catalogue` and `DataStore`
- [ ] Wire `DataStore` into `Student`, `Admin`, and `AuthenticationService` via constructor injection or a singleton instance

---

### Course Catalogue Seeding
- [ ] **`DataStore` seed method** — Hard-code (or load from a resource file) all 60 courses from the catalogue into `DataStore` on initialisation; group by programme for clarity
- [ ] **`Admin.seedCatalogue()`** — Call `DataStore` seed; populate the `Catalogue` instance with the loaded courses
- [ ] **Resolve missing unit values** — Confirm credit units for BIP417 (Remote Sensing / Satellite Met.), BIP422 (Synoptic Meteorology & Weather Analysis — BIPM), and BIP427 (Statistical Applications in Meteorology) before seeding
- [ ] **Course code pattern** — All real codes follow `BIPxxx` (3 digits); the `x` range encodes the programme (1xx=BIPMT, 2xx=BIPMT Yr2, 3xx=MFC, 4xx=BIPM); document this in `Course.java` or seeding logic
- [ ] **Verify no duplicate codes across programmes** — BIP310/BIP410 etc. are distinct codes; confirm no accidental collisions in the catalogue data

---

### Validation & Business Rules
- [ ] `studentId` must match format `NMS/YYYY/NNNN` — enforce in constructor or setter
- [ ] `courseCode` must be unique — enforce via `DataStore` before saving
- [ ] `Registration.selectedCourses` is a `Set<Course>` — requires `Course.equals()`/`hashCode()` based on `courseCode`
- [ ] `semester` string in `Registration` must follow `YYYY/N` format (e.g., `2024/1`)
- [ ] **`validateCourseCount()` — programme-aware min/max rules:**
  - BIPMT: exactly 8 courses per semester (consistent across all 4 semesters)
  - MFC: 12 courses in Sem 1, 10 courses in Sem 2 (confirm if student must register ALL or can select a subset)
  - BIPM: 10 courses in Sem 1, 10 courses in Sem 2 (same question — mandatory or selectable)
  - **Decision needed:** Are course selections mandatory (register everything) or elective (choose from available)? This affects whether `validateCourseCount()` checks for an exact count or a range.

---

### Testing (optional but recommended)
- [ ] Unit test `RegistrationException` error types
- [ ] Unit test `Registration.validateCourseCount()` for boundary conditions (too few, too many, exact limits) — test separately for BIPMT (8), MFC (12/10), and BIPM (10/10)
- [ ] Unit test `Catalogue.filterByLvl_sem()` — verify it returns the correct subset for each programme/year/semester combination from the real catalogue data
- [ ] Unit test that all 60 seeded courses have valid (non-null, non-blank) codes, titles, programmes, semesters, levels, and units
- [ ] Unit test `AuthenticationService.hashPassword()` and `authenticate()`
- [ ] Integration test: Student logs in → views courses → adds to registration → submits
