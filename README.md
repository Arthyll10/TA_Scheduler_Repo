# Teaching Assistant Scheduler

Made by **Group 9 (CYB-202)** for our **6OOP (Object-Oriented Programming)** class.

A console Java program using arrays, loops, inheritance and direct `Scanner` input. No external libraries are needed. The program has 11 classes; the added classes are `TimeSlot`, `Faculty`, `Admin` and `FacultyRequest`.

## Run

From this folder with a JDK installed:

```sh
javac *.java
java Main
```

The code uses APIs available in Java 17+. It was compiled and tested here on Java 25. If `javac` is missing from PATH but the JDK compiler module is installed, use:

```sh
java -m jdk.compiler/com.sun.tools.javac.Main *.java
java Main
```

## Menu

| Option | Action |
| --- | --- |
| 1 | Add a regular or graduate TA and their first availability window |
| 2 | Add a course/section with a weekly meeting and required TA count |
| 3 | Show eligible TAs and manually assign one |
| 4 | Print the full schedule and export `schedule.txt` |
| 5 | Exit, choosing whether to save |
| 6 | View, edit or remove TAs |
| 7 | View, edit or remove courses; unassign TAs |
| 8 | Change a TA's available status or add/remove weekly availability windows |
| 9 | View, add, edit or remove faculty |
| 10 | Create, approve or reject faculty requests; view eligible TAs |
| 11 | View TA schedules, faculty requests and admin reports; edit admin profile |
| 12 | Save all records to `tas-data.txt` |
| 13 | Reload saved records after confirmation |

## Scheduling rules

- Each course has one recurring weekly meeting. Use a distinct code for each section, such as `CS101-A` and `CS101-B`.
- Each TA may have up to 21 availability windows. Enter days as 1–7 (Monday–Sunday) and times as `HH:mm`, such as `09:30`. Meetings must start and end on the same day. `24:00` is allowed as an endpoint.
- A meeting must be fully covered by the TA's windows. Adjacent windows can cover it together. Back-to-back meetings are allowed; overlapping meetings are rejected.
- Expertise must match, ignoring letter case. Each TA has one expertise field.
- Weekly workload hours include preparation and are entered separately from meeting duration. Each assigned TA receives the course's full weekly workload hours.
- Regular TAs use their base workload limit. Graduate TAs receive **3 additional hours**.
- Multiple TAs can be assigned up to the course's required count. Duplicate assignments are rejected.
- Edits are checked against every existing assignment before being applied. Unassign a TA first if changing availability, expertise or limits would invalidate an assignment.
- IDs and course codes stay fixed during edits. Person IDs are unique across TAs, faculty and the reserved `ADMIN` ID. IDs and course codes are matched without regard to case.
- Text fields must be nonblank and cannot contain `|` or line breaks because the data file uses `|` as a separator. Email is stored as text, without email-address verification.

## Faculty requests and roles

Each request asks for **one TA position**. A faculty member cannot have two pending requests for the same course. Approval runs the same qualification, time and workload checks as a manual assignment. Requests can be Pending, Approved, Rejected or Cancelled.

Unassigning or removing a TA cancels their approved requests. Removing a course deletes its requests and releases workload. Removing faculty deletes their requests but keeps existing TA assignments. Pending requests do not reserve positions; another assignment may fill the course before a request is approved.

TA, GraduateTA, Faculty and Admin override `getRoleDetails()`. Role views are menu options accessible to anyone running the program; there is no login or access control.

## Files

`tas-data.txt` preserves profiles, availability, courses, assignments, requests and the admin profile. It loads automatically at startup. Save through option 12 or choose yes when exiting. If console input ends unexpectedly, the program attempts to save completed operations.

`schedule.txt` is a readable report, not the file used to restore records. Both files are stored in the directory from which you run the program.

Loading validates a separate set of records before replacing the current state. An invalid reload keeps existing records. An invalid startup file stops startup so it cannot be accidentally overwritten. Saving uses a normal `FileWriter` to overwrite the selected text file. Keep a separate copy of important saved data; an interrupted write can leave an incomplete file.

The default capacities are 50 TAs, 50 courses, 50 faculty and 200 requests. Request history occupies capacity until its faculty or course is removed. Run only one instance against a data file at a time.

## Check the logic

A plain Java test is provided, with no testing framework:

```sh
javac -d /tmp/tas-test-classes *.java tests/SchedulerTest.java
java -cp /tmp/tas-test-classes SchedulerTest
```

The tests cover scheduling conflicts, workload limits, rejected edits, reference updates, removals, requests, save/load round trips, malformed data, array capacity and report spacing. They write test data to temporary directories.

## Updating the project paper

The broader scope features are implemented. Update the class list and UML to include the four added classes and a course's array of assigned TAs. Describe `TimeSlot`, the three-hour graduate allowance, one weekly meeting per course, faculty request statuses, and the two different text files. Match the implemented names: `SchedulingConflictException`, `getRoleDetails()`, `currentWorkload` and `maxWorkload`.
