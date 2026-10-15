import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.IOException;

public class Main {
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        ScheduleManager manager = new ScheduleManager(5, 7);
        boolean running = true;

        while (running) {
            System.out.println("=".repeat(40));
            System.out.println("        TA SCHEDULING SYSTEM");
            System.out.println("        By Group 9 | CYB-202");
            System.out.println("=".repeat(40));
            System.out.println("1. Add Teaching Assistant");
            System.out.println("2. Add Course");
            System.out.println("3. Assign TA to Course");
            System.out.println("4. View Schedule and Export");
            System.out.println("5. Exit");
            System.out.println("------------ MANAGE RECORDS ------------");
            System.out.println("6. View All TAs and Courses");
            System.out.println("7. Change TA Availability");
            System.out.println("8. Unassign TA from Course");
            System.out.println("9. Remove TA");
            System.out.println("10. Remove Course");
            System.out.println("------------- FILE STORAGE -------------");
            System.out.println("11. Save All Records");
            System.out.println("12. Load Saved Records");
            System.out.println("=".repeat(40));

            try {
                System.out.print("Select option [1-12]: ");
                int option = input.nextInt();
                input.nextLine();
                System.out.println("=".repeat(40));

                switch (option) {
                    case 1: {
                        System.out.print("Graduate TA? (y/n): ");
                        String graduate = input.nextLine().trim();

                        if (!graduate.equalsIgnoreCase("y") && !graduate.equalsIgnoreCase("n")) {
                            throw new IllegalArgumentException("Invalid Input, enter y/n.");
                        }

                        System.out.print("ID: ");
                        String id = input.nextLine().trim();
                        System.out.print("Name: ");
                        String name = input.nextLine().trim();
                        System.out.print("Email: ");
                        String email = input.nextLine().trim();
                        System.out.print("Expertise: ");
                        String expertise = input.nextLine().trim();

                        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || expertise.isEmpty()) {
                            throw new IllegalArgumentException("Fields cannot be blank. Try Again");
                        }

                        System.out.print("Max weekly workload (+3 hours if Graduate TA): ");
                        int maxWorkload = input.nextInt();
                        input.nextLine();

                        if (maxWorkload <= 0) {
                            throw new IllegalArgumentException("Workload must be greater than zero.");
                        }

                        TeachingAssistant ta;

                        if (graduate.equalsIgnoreCase("y")) {
                            System.out.print("Research domain: ");
                            String domain = input.nextLine().trim();

                            if (domain.isEmpty()) {
                                throw new IllegalArgumentException("Research domain cannot be blank.");
                            }

                            System.out.print("Hourly stipend: ");
                            double stipend = input.nextDouble();
                            input.nextLine();

                            if (!Double.isFinite(stipend) || stipend < 0) {
                                throw new IllegalArgumentException("Stipend must be a finite number of zero or more.");
                            }

                            ta = new GraduateTA(id, name, email, expertise, maxWorkload, domain, stipend);
                        } else {
                            ta = new TeachingAssistant(id, name, email, expertise, maxWorkload);
                        }

                        manager.addTA(ta);
                        System.out.println("TA added.");
                        System.out.println("=".repeat(40));
                        break;
                    }

                    case 2: {
                        System.out.print("Course Code:");
                        String code = input.nextLine().trim();
                        System.out.print("Course name: ");
                        String name = input.nextLine().trim();
                        System.out.print("Required expertise: ");
                        String expertise = input.nextLine().trim();

                        if (code.isEmpty() || name.isEmpty() || expertise.isEmpty()) {
                            throw new IllegalArgumentException("Fields cannot be blank.");
                        }

                        System.out.print("Required weekly hours: ");
                        int hours = input.nextInt();
                        input.nextLine();

                        if (hours <= 0) {
                            throw new IllegalArgumentException("Hours must be greater than zero.");
                        }

                        Course course = new Course(code, name, expertise, hours);

                        manager.addCourse(course);
                        System.out.println("Course added.");
                        break;
                    }

                    case 3: {
                        manager.viewTAs();
                        manager.viewCourses();
                        System.out.print("TA ID: ");
                        String taId = input.nextLine().trim();
                        System.out.print("Course code: ");
                        String code = input.nextLine().trim();

                        if (taId.isEmpty() || code.isEmpty()) {
                            throw new IllegalArgumentException("Fields cannot be blank.");
                        }

                        manager.assignTA(taId, code);
                        System.out.println("TA assigned successfully.");
                        break;
                    }

                    case 4: {
                        manager.viewAndExportSchedule("schedule.txt");
                        break;
                    }

                    case 5: {
                        System.out.print("Save before exiting? (y/n): ");
                        String save = input.nextLine().trim();
                        if (save.equalsIgnoreCase("y")) {
                            manager.saveData("tas-data.txt");
                        } else if (!save.equalsIgnoreCase("n")) {
                            throw new IllegalArgumentException("Enter y or n.");
                        }
                        running = false;
                        System.out.println("Thanks for using the scheduler by G#9 | CYB-202");
                        break;
                    }

                    case 6: {
                        manager.viewTAs();
                        manager.viewCourses();
                        break;
                    }
                    case 7: {
                        manager.viewTAs();
                        System.out.print("TA ID: ");
                        String id = input.nextLine().trim();
                        System.out.print("Available? (y/n): ");
                        String answer = input.nextLine().trim();
                        if (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("n")) {
                            throw new IllegalArgumentException("Enter y or n.");
                        }
                        manager.changeAvailability(id, answer.equalsIgnoreCase("y"));
                        System.out.println("Availability updated.");
                        break;
                    }
                    case 8: {
                        manager.viewCourses();
                        System.out.print("Course code: ");
                        String code = input.nextLine().trim();
                        manager.unassignTA(code);
                        System.out.println("TA unassigned. Workload reduced.");
                        break;
                    }
                    case 9: {
                        manager.viewTAs();
                        System.out.print("TA ID to remove: ");
                        String id = input.nextLine().trim();
                        manager.removeTA(id);
                        System.out.println("TA removed.");
                        break;
                    }
                    case 10: {
                        manager.viewCourses();
                        System.out.print("Course code to remove: ");
                        String code = input.nextLine().trim();
                        manager.removeCourse(code);
                        System.out.println("Course removed.");
                        break;
                    }
                    case 11: {
                        manager.saveData("tas-data.txt");
                        System.out.println("All records saved to tas-data.txt.");
                        break;
                    }
                    case 12: {
                        System.out.print("Replace current records with saved data? (y/n): ");
                        String answer = input.nextLine().trim();
                        if (answer.equalsIgnoreCase("y")) {
                            manager.loadData("tas-data.txt");
                            System.out.println("All records loaded from tas-data.txt.");
                        } else if (answer.equalsIgnoreCase("n")) {
                            System.out.println("Load cancelled.");
                        } else {
                            throw new IllegalArgumentException("Enter y or n.");
                        }
                        break;
                    }
                    default: {
                        System.out.println("Enter a valid option [1-12].");
                    }
                }
            } catch (InputMismatchException e) {
                if (input.hasNextLine())
                    input.nextLine();
                System.out.println("Invalid number. Please try again.");
            } catch (NoSuchElementException e) {
                running = false;
                System.out.println("Input ended. Unsaved changes were not saved.");
            } catch (SchedulingConflictException e) {
                System.out.println("Assignment failed: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File operation failed: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
        input.close();
    }
}
