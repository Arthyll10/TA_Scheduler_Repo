import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.IOException;
import java.io.File;

public class Main {
    static Scanner input = new Scanner(System.in);
    static final String DATA_FILE = "tas-data.txt";

    public static void main(String[] args) {
        ScheduleManager manager = new ScheduleManager(50, 50);
        boolean running = true;

        if (new File(DATA_FILE).exists()) {
            try {
                manager.loadData(DATA_FILE);
                System.out.println("[SUCCESS] Saved data loaded from " + DATA_FILE);
            } catch (IOException e) {
                System.out.println("[ERROR] " + e.getMessage());
                System.out.println(
                        "Startup stopped to protect your saved data. Repair or rename the file, then restart.");
                input.close();
                return;
            }
        }

        while (running) {
            System.out.println("=".repeat(60));
            System.out.println("                  TA SCHEDULING SYSTEM");
            System.out.println("                     Group 9 | CYB-202");
            System.out.println("=".repeat(60));
            System.out.println("  1. Add Teaching Assistant");
            System.out.println("  2. Add Course");
            System.out.println("  3. Assign TA to Course");
            System.out.println("  4. View Schedule and Export");
            System.out.println("  5. Exit");
            System.out.println("                   --- MANAGE RECORDS ---");
            System.out.println("  6. View / Edit / Remove TAs");
            System.out.println("  7. View / Edit / Remove Courses / Unassign TAs");
            System.out.println("  8. Manage TA Availability");
            System.out.println("  9. Manage Faculty");
            System.out.println(" 10. Faculty Requests and Eligible TAs");
            System.out.println(" 11. TA / Faculty / Admin Views");
            System.out.println("                    --- FILE STORAGE ---");
            System.out.println(" 12. Save Data");
            System.out.println(" 13. Reload Saved Data");
            System.out.println("-".repeat(60));

            try {
                System.out.print("Select option [1-13]: ");
                int option = input.nextInt();
                input.nextLine();
                switch (option) {
                    case 1: {
                        addTA(manager);
                        break;
                    }
                    case 2: {
                        addCourse(manager);
                        break;
                    }
                    case 3: {
                        assignTA(manager);
                        break;
                    }
                    case 4: {
                        manager.viewAndExportSchedule("schedule.txt");
                        break;
                    }
                    case 5: {
                        System.out.print("Save data before exiting? (y/n): ");
                        String answer = input.nextLine().trim();
                        if (answer.equalsIgnoreCase("y")) {
                            manager.saveData(DATA_FILE);
                            System.out.println("[SUCCESS] Data saved.");
                        } else if (!answer.equalsIgnoreCase("n")) {
                            throw new IllegalArgumentException("Enter y or n.");
                        }
                        running = false;
                        System.out.println("=".repeat(60));
                        System.out.println("Thanks for using the scheduler by Group 9 | CYB-202.");
                        System.out.println("=".repeat(60));
                        break;
                    }
                    case 6: {
                        manageTAs(manager);
                        break;
                    }
                    case 7: {
                        manageCourses(manager);
                        break;
                    }
                    case 8: {
                        manageAvailability(manager);
                        break;
                    }
                    case 9: {
                        manageFaculty(manager);
                        break;
                    }
                    case 10: {
                        manageRequests(manager);
                        break;
                    }
                    case 11: {
                        viewRoles(manager);
                        break;
                    }
                    case 12: {
                        manager.saveData(DATA_FILE);
                        System.out.println("[SUCCESS] Data saved to " + DATA_FILE);
                        break;
                    }
                    case 13: {
                        System.out.println("Reloading replaces current records and discards unsaved changes.");
                        System.out.print("Continue? (y/n): ");
                        String answer = input.nextLine().trim();
                        if (answer.equalsIgnoreCase("y")) {
                            manager.loadData(DATA_FILE);
                            System.out.println("[SUCCESS] Saved data loaded.");
                        } else
                            System.out.println("[INFO] Reload cancelled.");
                        break;
                    }
                    default: {
                        System.out.println("[ERROR] Choose an option from 1 to 13.");
                    }
                }
            } catch (InputMismatchException e) {
                if (input.hasNextLine())
                    input.nextLine();
                System.out.println("[ERROR] Invalid number. Please try the operation again.");
            } catch (NoSuchElementException e) {
                running = false;
                try {
                    manager.saveData(DATA_FILE);
                    System.out.println("[INFO] Input ended. Data saved before closing.");
                } catch (IOException saveError) {
                    System.out.println("[ERROR] Input ended and data could not be saved: " + saveError.getMessage());
                }
            } catch (SchedulingConflictException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (IOException e) {
                System.out.println("[ERROR] File operation failed: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
        input.close();
    }

    static TimeSlot readTimeSlot() {
        System.out.println("Day: 1 Mon | 2 Tue | 3 Wed | 4 Thu | 5 Fri | 6 Sat | 7 Sun");
        System.out.print("Day number         : ");
        int day = input.nextInt();
        input.nextLine();
        System.out.print("Start time (HH:mm) : ");
        String start = input.nextLine().trim();
        System.out.print("End time (HH:mm)   : ");
        String end = input.nextLine().trim();
        return new TimeSlot(day, TimeSlot.parseTime(start), TimeSlot.parseTime(end));
    }

    static void addTA(ScheduleManager manager) {
        System.out.println("=".repeat(60));
        System.out.println("ADD TEACHING ASSISTANT");
        System.out.println("=".repeat(60));
        System.out.print("Graduate TA? (y/n)  : ");
        String graduate = input.nextLine().trim();
        if (!graduate.equalsIgnoreCase("y") && !graduate.equalsIgnoreCase("n")) {
            throw new IllegalArgumentException("Enter y or n.");
        }
        System.out.println("                --- PERSONAL INFORMATION ---");
        System.out.print("TA ID              : ");
        String id = input.nextLine().trim();
        System.out.print("Full name          : ");
        String name = input.nextLine().trim();
        System.out.print("Email              : ");
        String email = input.nextLine().trim();
        System.out.println("                --- TEACHING INFORMATION ---");
        System.out.print("Subject expertise  : ");
        String expertise = input.nextLine().trim();
        if (graduate.equalsIgnoreCase("y"))
            System.out.println("Graduate TAs receive 3 extra hours above the base limit.");
        System.out.print("Base limit (hours) : ");
        int maxWorkload = input.nextInt();
        input.nextLine();
        TeachingAssistant ta;
        if (graduate.equalsIgnoreCase("y")) {
            System.out.println("                  --- GRADUATE DETAILS ---");
            System.out.print("Research domain    : ");
            String domain = input.nextLine().trim();
            System.out.print("Hourly stipend PHP : ");
            double stipend = input.nextDouble();
            input.nextLine();
            ta = new GraduateTA(id, name, email, expertise, maxWorkload, domain, stipend);
        } else
            ta = new TeachingAssistant(id, name, email, expertise, maxWorkload);
        System.out.println("          --- FIRST WEEKLY AVAILABILITY WINDOW ---");
        ta.addAvailability(readTimeSlot());
        manager.addTA(ta);
        System.out.println("[SUCCESS] TA added. Use option 8 for more availability windows.");
    }

    static void addCourse(ScheduleManager manager) {
        System.out.println("=".repeat(60));
        System.out.println("ADD COURSE / SECTION");
        System.out.println("=".repeat(60));
        System.out.print("Unique course code : ");
        String code = input.nextLine().trim();
        System.out.print("Course name        : ");
        String name = input.nextLine().trim();
        System.out.println("                  --- TA REQUIREMENTS ---");
        System.out.print("Required expertise : ");
        String expertise = input.nextLine().trim();
        System.out.print("Weekly hours / TA  : ");
        int hours = input.nextInt();
        input.nextLine();
        System.out.print("TAs required [1-50]: ");
        int count = input.nextInt();
        input.nextLine();
        System.out.println("                   --- WEEKLY MEETING ---");
        System.out.println("Workload hours include preparation; meeting time is checked separately.");
        TimeSlot slot = readTimeSlot();
        manager.addCourse(new Course(code, name, expertise, hours, slot, count));
        System.out.println("[SUCCESS] Course added.");
    }

    static void assignTA(ScheduleManager manager) throws SchedulingConflictException {
        System.out.println("                --- ASSIGN TA TO COURSE ---");
        manager.viewCourses();
        System.out.print("Course code        : ");
        String code = input.nextLine().trim();
        System.out.println("                    --- ELIGIBLE TAs ---");
        manager.viewEligibleTAs(code);
        System.out.print("TA ID              : ");
        String id = input.nextLine().trim();
        manager.assignTA(id, code);
        System.out.println("[SUCCESS] TA assigned to " + code + ".");
    }

    static void manageTAs(ScheduleManager manager) throws SchedulingConflictException {
        System.out.println("                     --- TA RECORDS ---");
        System.out.println("1. View all TAs | 2. Edit TA | 3. Remove TA | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        if (option == 0)
            return;
        if (option == 1) {
            manager.viewTAs();
            return;
        }
        if (option != 2 && option != 3)
            throw new IllegalArgumentException("Choose 0 to 3.");
        System.out.print("TA ID: ");
        String id = input.nextLine().trim();
        TeachingAssistant ta = manager.findTA(id);
        if (ta == null)
            throw new IllegalArgumentException("TA ID was not found.");
        if (option == 3) {
            System.out.println("Removing this TA also removes their assignments and cancels approved requests.");
            System.out.print("Remove TA? (y/n): ");
            String answer = input.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                manager.removeTA(id);
                System.out.println("[SUCCESS] TA removed.");
            } else
                System.out.println("[INFO] Removal cancelled.");
            return;
        }
        System.out.println(ta.getRoleDetails());
        System.out.println("Enter the updated details. ID and TA type stay fixed.");
        System.out.print("Full name          : ");
        String name = input.nextLine().trim();
        System.out.print("Email              : ");
        String email = input.nextLine().trim();
        System.out.print("Subject expertise  : ");
        String expertise = input.nextLine().trim();
        System.out.print("Base limit (hours) : ");
        int maxWorkload = input.nextInt();
        input.nextLine();
        String domain = "";
        double stipend = 0;
        if (ta instanceof GraduateTA) {
            System.out.print("Research domain    : ");
            domain = input.nextLine().trim();
            System.out.print("Hourly stipend PHP : ");
            stipend = input.nextDouble();
            input.nextLine();
        }
        manager.updateTA(id, name, email, expertise, maxWorkload, domain, stipend);
        System.out.println("[SUCCESS] TA updated. Existing assignments were checked.");
    }

    static void manageCourses(ScheduleManager manager) throws SchedulingConflictException {
        System.out.println("                   --- COURSE RECORDS ---");
        System.out.println("1. View courses | 2. Edit course | 3. Remove course | 4. Unassign TA | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        if (option == 0)
            return;
        if (option == 1) {
            manager.viewCourses();
            return;
        }
        if (option < 2 || option > 4)
            throw new IllegalArgumentException("Choose 0 to 4.");
        System.out.print("Course code: ");
        String code = input.nextLine().trim();
        Course course = manager.findCourse(code);
        if (course == null)
            throw new IllegalArgumentException("Course code was not found.");
        if (option == 3) {
            System.out.println("Removing this course releases TA workload and removes its faculty requests.");
            System.out.print("Remove course? (y/n): ");
            String answer = input.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                manager.removeCourse(code);
                System.out.println("[SUCCESS] Course removed.");
            } else
                System.out.println("[INFO] Removal cancelled.");
            return;
        }
        if (option == 4) {
            for (int i = 0; i < course.getAssignedCount(); i++) {
                TeachingAssistant ta = course.getAssignedTA(i);
                System.out.println(ta.getId() + " | " + ta.getName());
            }
            System.out.print("TA ID: ");
            String id = input.nextLine().trim();
            manager.unassignTA(id, code);
            System.out.println("[SUCCESS] TA unassigned and workload released.");
            return;
        }
        System.out.println("Enter the updated course details. Course code stays fixed.");
        System.out.print("Course name        : ");
        String name = input.nextLine().trim();
        System.out.print("Required expertise : ");
        String expertise = input.nextLine().trim();
        System.out.print("Weekly hours / TA  : ");
        int hours = input.nextInt();
        input.nextLine();
        System.out.print("TAs required [1-50]: ");
        int count = input.nextInt();
        input.nextLine();
        System.out.println("                   --- WEEKLY MEETING ---");
        TimeSlot slot = readTimeSlot();
        manager.updateCourse(code, name, expertise, hours, slot, count);
        System.out.println("[SUCCESS] Course updated. Existing assignments were checked.");
    }

    static void manageAvailability(ScheduleManager manager) throws SchedulingConflictException {
        System.out.println("                --- WEEKLY AVAILABILITY ---");
        System.out.print("TA ID: ");
        String id = input.nextLine().trim();
        System.out.print(manager.getAvailabilityReport(id));
        System.out.println("1. Change available status | 2. Add window | 3. Remove window | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        switch (option) {
            case 0:
                return;
            case 1: {
                System.out.print("Available for assignments? (y/n): ");
                String answer = input.nextLine().trim();
                if (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("n"))
                    throw new IllegalArgumentException("Enter y or n.");
                manager.setTAAvailable(id, answer.equalsIgnoreCase("y"));
                break;
            }
            case 2: {
                manager.addAvailability(id, readTimeSlot());
                break;
            }
            case 3: {
                System.out.print("Availability number to remove: ");
                int number = input.nextInt();
                input.nextLine();
                if (number <= 0)
                    throw new IllegalArgumentException("Availability number must be positive.");
                manager.removeAvailability(id, number - 1);
                break;
            }
            default:
                throw new IllegalArgumentException("Choose 0 to 3.");
        }
        System.out.println("[SUCCESS] Availability updated.");
    }

    static void manageFaculty(ScheduleManager manager) {
        System.out.println("                  --- FACULTY RECORDS ---");
        System.out.println("1. View faculty | 2. Add faculty | 3. Edit faculty | 4. Remove faculty | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        if (option == 0)
            return;
        if (option == 1) {
            manager.viewFaculty();
            return;
        }
        if (option < 2 || option > 4)
            throw new IllegalArgumentException("Choose 0 to 4.");
        System.out.print("Faculty ID: ");
        String id = input.nextLine().trim();
        if (option == 4) {
            if (manager.findFaculty(id) == null)
                throw new IllegalArgumentException("Faculty ID was not found.");
            System.out.println("Removing faculty removes their requests. Existing TA assignments remain.");
            System.out.print("Remove faculty? (y/n): ");
            String answer = input.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                manager.removeFaculty(id);
                System.out.println("[SUCCESS] Faculty removed.");
            } else
                System.out.println("[INFO] Removal cancelled.");
            return;
        }
        if (option == 3 && manager.findFaculty(id) == null)
            throw new IllegalArgumentException("Faculty ID was not found.");
        System.out.print("Full name          : ");
        String name = input.nextLine().trim();
        System.out.print("Email              : ");
        String email = input.nextLine().trim();
        System.out.print("Department         : ");
        String department = input.nextLine().trim();
        if (option == 2)
            manager.addFaculty(new Faculty(id, name, email, department));
        else
            manager.updateFaculty(id, name, email, department);
        System.out.println("[SUCCESS] Faculty record saved in memory.");
    }

    static void manageRequests(ScheduleManager manager) throws SchedulingConflictException {
        System.out.println("                  --- FACULTY REQUESTS ---");
        System.out.println("Each request asks for one TA position in a course.");
        System.out.println(
                "1. View requests | 2. Create request | 3. Approve | 4. Reject | 5. Find eligible TAs | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        switch (option) {
            case 0:
                return;
            case 1: {
                System.out.print(manager.getRequestsReport(null));
                break;
            }
            case 2: {
                manager.viewFaculty();
                System.out.print("Faculty ID: ");
                String facultyId = input.nextLine().trim();
                manager.viewCourses();
                System.out.print("Course code: ");
                String code = input.nextLine().trim();
                int id = manager.requestTA(facultyId, code);
                System.out.println("[SUCCESS] Pending request created: " + id);
                break;
            }
            case 3: {
                System.out.print(manager.getRequestsReport(null));
                System.out.print("Request ID: ");
                int id = input.nextInt();
                input.nextLine();
                FacultyRequest request = manager.findRequest(id);
                if (request == null)
                    throw new IllegalArgumentException("Request ID was not found.");
                manager.viewEligibleTAs(request.getCourseCode());
                System.out.print("TA ID to assign: ");
                String taId = input.nextLine().trim();
                manager.approveRequest(id, taId);
                System.out.println("[SUCCESS] Request approved and TA assigned.");
                break;
            }
            case 4: {
                System.out.print("Request ID: ");
                int id = input.nextInt();
                input.nextLine();
                manager.rejectRequest(id);
                System.out.println("[SUCCESS] Request rejected.");
                break;
            }
            case 5: {
                System.out.print("Course code: ");
                String code = input.nextLine().trim();
                manager.viewEligibleTAs(code);
                break;
            }
            default:
                throw new IllegalArgumentException("Choose 0 to 5.");
        }
    }

    static void viewRoles(ScheduleManager manager) {
        System.out.println("           --- ROLE VIEWS (NO LOGIN REQUIRED) ---");
        System.out.println("1. TA schedule | 2. Faculty requests | 3. Admin reports | 4. Edit admin profile | 0. Back");
        System.out.print("Select option: ");
        int option = input.nextInt();
        input.nextLine();
        switch (option) {
            case 0:
                return;
            case 1: {
                System.out.print("TA ID: ");
                String id = input.nextLine().trim();
                System.out.print(manager.getTASchedule(id));
                break;
            }
            case 2: {
                System.out.print("Faculty ID: ");
                String id = input.nextLine().trim();
                System.out.print(manager.getFacultyReport(id));
                break;
            }
            case 3: {
                System.out.println(manager.getAdmin().getRoleDetails());
                System.out.print(manager.getWorkloadReport());
                System.out.print(manager.getScheduleReport());
                System.out.print(manager.getRequestsReport(null));
                break;
            }
            case 4: {
                System.out.print("Admin name: ");
                String name = input.nextLine().trim();
                System.out.print("Admin email: ");
                String email = input.nextLine().trim();
                manager.updateAdmin(name, email);
                System.out.println("[SUCCESS] Admin profile updated.");
                break;
            }
            default:
                throw new IllegalArgumentException("Choose 0 to 4.");
        }
    }
}
