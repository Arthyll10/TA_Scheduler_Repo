import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class ScheduleManager {
    private TeachingAssistant[] taList;
    private Course[] courseList;
    private int taCount, courseCount;

    public ScheduleManager(int taCapacity, int courseCapacity) {
        this.taList = new TeachingAssistant[taCapacity];
        this.courseList = new Course[courseCapacity];
        this.taCount = 0;
        this.courseCount = 0;
    }

    private TeachingAssistant findTA(String taID) {
        for (int i = 0; i < taCount; i++) {
            if (taList[i].getId().equalsIgnoreCase(taID)) {
                return taList[i];
            }
        }

        return null;
    }

    private Course findCourse(String courseCode) {
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i].getCourseCode().equalsIgnoreCase(courseCode)) {
                return courseList[i];
            }
        }

        return null;
    }

    public void addTA(TeachingAssistant ta) {
        if (ta == null) {
            throw new IllegalArgumentException("TA cannot be null.");
        }

        if (taCount >= taList.length) {
            throw new IllegalStateException("The TA list is full.");
        }

        if (findTA(ta.getId()) != null) {
            throw new IllegalArgumentException("TA ID already exists.");
        }

        taList[taCount] = ta;
        taCount++;
    }

    public void addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }

        if (courseCount >= courseList.length) {
            throw new IllegalStateException("The course list is full.");
        }

        if (findCourse(course.getCourseCode()) != null) {
            throw new IllegalArgumentException("Course code already exists.");
        }

        courseList[courseCount] = course;
        courseCount++;
    }

    public void assignTA(String taID, String courseCode) throws SchedulingConflictException {
        TeachingAssistant ta = findTA(taID);
        Course course = findCourse(courseCode);

        if (ta == null) {
            throw new SchedulingConflictException("TA ID was not found.");
        }

        if (course == null) {
            throw new SchedulingConflictException("Course code was not found.");
        }

        if (course.isAssigned()) {
            throw new SchedulingConflictException("This course already has an assigned TA.");
        }

        if (!ta.canAcceptAssignment(course.getRequiredHours(), course.getRequiredExpertise())) {
            throw new SchedulingConflictException(
                    "Assignment rejected. Check hours, availability, expertise, and workload.");
        }

        course.setAssignedTA(ta);
        ta.addWorkload(course.getRequiredHours());
    }

    public void viewTAs() {
        System.out.println("             --- TA LIST ---");
        if (taCount == 0)
            System.out.println("No TAs registered.");
        for (int i = 0; i < taCount; i++) {
            TeachingAssistant ta = taList[i];
            System.out.println("ID: " + ta.getId() + " | Email: " + ta.getEmail());
            System.out.println(ta.getRoleDetails());
            System.out.println("Available: " + ta.isAvailable());
            System.out.println("-".repeat(40));
        }
    }

    public void viewCourses() {
        System.out.println("           --- COURSE LIST ---");
        if (courseCount == 0)
            System.out.println("No courses registered.");
        for (int i = 0; i < courseCount; i++) {
            Course course = courseList[i];
            System.out.println(course.getCourseCode() + " - " + course.getCourseName());
            System.out.println("Expertise: " + course.getRequiredExpertise());
            System.out.println("Weekly hours: " + course.getRequiredHours());
            if (course.isAssigned()) {
                System.out.println("Assigned TA: " + course.getAssignedTA().getId());
            } else {
                System.out.println("Assigned TA: None");
            }
            System.out.println("-".repeat(40));
        }
    }

    public void changeAvailability(String taId, boolean available) {
        TeachingAssistant ta = findTA(taId);
        if (ta == null)
            throw new IllegalArgumentException("TA ID was not found.");
        if (!available && ta.getCurrentWorkload() > 0) {
            throw new IllegalStateException("Unassign this TA from all courses before marking unavailable.");
        }
        ta.setAvailable(available);
    }

    public void unassignTA(String courseCode) {
        Course course = findCourse(courseCode);
        if (course == null)
            throw new IllegalArgumentException("Course code was not found.");
        if (!course.isAssigned())
            throw new IllegalStateException("This course has no assigned TA.");
        TeachingAssistant ta = course.getAssignedTA();
        ta.setCurrentWorkload(ta.getCurrentWorkload() - course.getRequiredHours());
        course.setAssignedTA(null);
    }

    public void removeTA(String taId) {
        TeachingAssistant ta = findTA(taId);
        if (ta == null)
            throw new IllegalArgumentException("TA ID was not found.");
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i].getAssignedTA() == ta) {
                throw new IllegalStateException("Unassign this TA from all courses before removing.");
            }
        }
        for (int i = 0; i < taCount; i++) {
            if (taList[i] == ta) {
                for (int j = i; j < taCount - 1; j++) {
                    taList[j] = taList[j + 1];
                }
                taCount--;
                taList[taCount] = null;
                return;
            }
        }
    }

    public void removeCourse(String courseCode) {
        Course course = findCourse(courseCode);
        if (course == null)
            throw new IllegalArgumentException("Course code was not found.");
        if (course.isAssigned())
            throw new IllegalStateException("Unassign the TA before removing this course.");
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i] == course) {
                for (int j = i; j < courseCount - 1; j++) {
                    courseList[j] = courseList[j + 1];
                }
                courseCount--;
                courseList[courseCount] = null;
                return;
            }
        }
    }

    public void saveData(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("TAS-SIMPLE-1\n" + taCount + "\n");
            for (int i = 0; i < taCount; i++) {
                TeachingAssistant ta = taList[i];
                String type = "Regular";
                if (ta instanceof GraduateTA)
                    type = "Graduate";
                writer.write(type + "\n" + ta.getId() + "\n" + ta.getName() + "\n"
                        + ta.getEmail() + "\n" + ta.getExpertise() + "\n"
                        + ta.getMaxWorkload() + "\n" + ta.isAvailable() + "\n");
                if (ta instanceof GraduateTA) {
                    GraduateTA graduate = (GraduateTA) ta;
                    writer.write(graduate.getResearchDomain() + "\n" + graduate.getHourlyStipend() + "\n");
                }
            }
            writer.write(courseCount + "\n");
            for (int i = 0; i < courseCount; i++) {
                Course course = courseList[i];
                String taId = "";
                if (course.isAssigned())
                    taId = course.getAssignedTA().getId();
                writer.write(course.getCourseCode() + "\n" + course.getCourseName() + "\n"
                        + course.getRequiredExpertise() + "\n" + course.getRequiredHours() + "\n" + taId + "\n");
            }
            writer.write("END\n");
        }
    }

    public void loadData(String filePath) throws IOException {
        ScheduleManager loaded = new ScheduleManager(taList.length, courseList.length);
        try (Scanner file = new Scanner(new FileReader(filePath))) {
            if (!file.nextLine().equals("TAS-SIMPLE-1"))
                throw new IllegalArgumentException("Wrong file format.");
            int savedTAs = Integer.parseInt(file.nextLine());
            if (savedTAs < 0 || savedTAs > taList.length)
                throw new IllegalArgumentException("Invalid TA count.");
            for (int i = 0; i < savedTAs; i++) {
                String type = file.nextLine();
                String id = file.nextLine();
                String name = file.nextLine();
                String email = file.nextLine();
                String expertise = file.nextLine();
                int maxWorkload = Integer.parseInt(file.nextLine());
                String available = file.nextLine();
                if (!available.equals("true") && !available.equals("false")) {
                    throw new IllegalArgumentException("Invalid availability value.");
                }
                TeachingAssistant ta;
                if (type.equals("Graduate")) {
                    String domain = file.nextLine();
                    double stipend = Double.parseDouble(file.nextLine());
                    ta = new GraduateTA(id, name, email, expertise, maxWorkload, domain, stipend);
                } else if (type.equals("Regular")) {
                    ta = new TeachingAssistant(id, name, email, expertise, maxWorkload);
                } else {
                    throw new IllegalArgumentException("Unknown TA type.");
                }
                ta.setAvailable(Boolean.parseBoolean(available));
                loaded.addTA(ta);
            }
            int savedCourses = Integer.parseInt(file.nextLine());
            if (savedCourses < 0 || savedCourses > courseList.length)
                throw new IllegalArgumentException("Invalid course count.");
            for (int i = 0; i < savedCourses; i++) {
                String code = file.nextLine();
                String name = file.nextLine();
                String expertise = file.nextLine();
                int hours = Integer.parseInt(file.nextLine());
                String taId = file.nextLine();
                loaded.addCourse(new Course(code, name, expertise, hours));
                if (!taId.isEmpty())
                    loaded.assignTA(taId, code);
            }
            if (!file.nextLine().equals("END") || file.hasNextLine()) {
                throw new IllegalArgumentException("Incomplete file or unexpected extra records.");
            }
            if (file.ioException() != null)
                throw file.ioException();
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException
                | SchedulingConflictException e) {
            throw new IOException("Could not load data: " + e.getMessage(), e);
        }
        taList = loaded.taList;
        courseList = loaded.courseList;
        taCount = loaded.taCount;
        courseCount = loaded.courseCount;
    }

    public void viewAndExportSchedule(String filePath) throws IOException {
        String report = "TEACHING ASSISTANT SCHEDULE\n";

        if (courseCount == 0) {
            report += "No courses registered.\n";
        }

        for (int i = 0; i < courseCount; i++) {
            Course course = courseList[i];

            report += String.format(
                    "Course: %s - %s%n"
                            + "Required Expertise: %s%n"
                            + "Required Hours: %d%n",
                    course.getCourseCode(),
                    course.getCourseName(),
                    course.getRequiredExpertise(),
                    course.getRequiredHours());

            if (course.isAssigned()) {
                TeachingAssistant ta = course.getAssignedTA();

                report += String.format(
                        "TA ID: %s%nEmail: %s%n%s%n",
                        ta.getId(),
                        ta.getEmail(),
                        ta.getRoleDetails());
            } else {
                report += "Status: Unassigned\n";
            }

            report += "-".repeat(40) + "\n";
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(report);
        }

        System.out.print(report);
        System.out.println("Exported to " + filePath);
    }
}
