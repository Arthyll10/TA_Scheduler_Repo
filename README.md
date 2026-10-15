# Teaching Assistant Scheduler

Made by **Group 9 (CYB-202)** for our **6OOP (Object-Oriented Programming)** class.

A console Java program for assigning teaching assistants (TAs) to courses. It checks that the TA's expertise matches the course and that the TA's workload limit isn't exceeded. Graduate TAs get 3 extra hours on top of their limit.

## Run

From this folder with a JDK installed:

```sh
javac *.java
java Main
```

## Menu

| Option | Action |
| --- | --- |
| 1 | Add a regular or graduate TA |
| 2 | Add a course |
| 3 | Assign a TA to a course |
| 4 | View the schedule and export `schedule.txt` |
| 5 | Exit, choosing whether to save |
| 6 | View all TAs and courses |
| 7 | Change a TA's availability |
| 8 | Unassign a TA from a course |
| 9 | Remove a TA |
| 10 | Remove a course |
| 11 | Save all records to `tas-data.txt` |
| 12 | Load saved records |

## Classes

- `Person`: base class with ID, name and email
- `TeachingAssistant`: extends `Person` and adds expertise, workload and availability
- `GraduateTA`: extends `TeachingAssistant` and adds research domain, stipend and +3 workload hours
- `Course`: a course with required expertise, weekly hours and an assigned TA
- `ScheduleManager`: stores the records and handles assigning, saving and loading
- `SchedulingConflictException`: thrown when an assignment breaks a scheduling rule
- `Main`: the console menu
