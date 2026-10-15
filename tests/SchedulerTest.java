import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

// No testing library needed: run this class after compiling the program.
public class SchedulerTest {
    static int checks;

    interface Action {
        void run() throws Exception;
    }

    static void check(boolean result, String message) {
        checks++;
        if (!result)
            throw new AssertionError(message);
    }

    static void rejects(Class<? extends Exception> type, Action action) throws Exception {
        try {
            action.run();
        } catch (Exception e) {
            check(type.isInstance(e), "Wrong exception: " + e);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    static TeachingAssistant ta(String id, boolean graduate) {
        TeachingAssistant ta = graduate
                ? new GraduateTA(id, id, "ta@school.edu", "Java", 10, "AI", 150)
                : new TeachingAssistant(id, id, "ta@school.edu", "Java", 10);
        ta.addAvailability(new TimeSlot(1, 480, 720));
        ta.addAvailability(new TimeSlot(2, 480, 720));
        return ta;
    }

    static Course course(String id, int day, int start, int end, int hours, int count) {
        return new Course(id, id, "java", hours, new TimeSlot(day, start, end), count);
    }

    public static void main(String[] args) throws Exception {
        ScheduleManager m = new ScheduleManager(10, 10);
        m.addTA(ta("T1", false));
        m.addTA(ta("T2", true));
        m.addTA(ta("T3", false));
        m.addCourse(course("C1", 1, 540, 600, 5, 2));
        m.addCourse(course("C2", 1, 600, 660, 5, 2));
        m.addCourse(course("Overlap", 1, 570, 630, 1, 1));
        m.addCourse(course("Tuesday", 2, 540, 600, 3, 2));
        m.addCourse(course("Outside", 1, 720, 780, 1, 1));
        rejects(IllegalArgumentException.class, () -> m.addTA(ta("t1", false)));
        rejects(IllegalArgumentException.class, () -> m.addFaculty(new Faculty("T1", "F", "e", "CS")));
        m.assignTA("t1", "c1");
        m.assignTA("T1", "C2");
        check(m.findTA("T1").getCurrentWorkload() == 10, "Adjacent meetings and case-insensitive IDs");
        rejects(SchedulingConflictException.class, () -> m.assignTA("T1", "Overlap"));
        rejects(SchedulingConflictException.class, () -> m.assignTA("T1", "Tuesday"));
        rejects(SchedulingConflictException.class, () -> m.assignTA("T1", "C1"));
        rejects(SchedulingConflictException.class, () -> m.assignTA("T3", "Outside"));
        m.assignTA("T2", "C1");
        m.assignTA("T2", "C2");
        m.assignTA("T2", "Tuesday");
        check(m.findTA("T2").getCurrentWorkload() == 13, "Graduate bonus and different days");
        rejects(SchedulingConflictException.class, () -> m.assignTA("T3", "C1"));
        rejects(SchedulingConflictException.class, () -> m.setTAAvailable("T1", false));
        rejects(SchedulingConflictException.class, () -> m.removeAvailability("T1", 0));
        rejects(SchedulingConflictException.class, () -> m.updateTA("T1", "Bad", "e", "Python", 10, "", 0));
        rejects(SchedulingConflictException.class, () -> m.updateTA("T1", "Bad", "e", "Java", 9, "", 0));
        rejects(SchedulingConflictException.class,
                () -> m.updateCourse("C1", "Bad", "Java", 6, new TimeSlot(1, 540, 600), 2));
        rejects(SchedulingConflictException.class,
                () -> m.updateCourse("C1", "Bad", "Java", 5, new TimeSlot(1, 590, 650), 2));
        rejects(SchedulingConflictException.class,
                () -> m.updateCourse("C1", "Bad", "Java", 5, new TimeSlot(1, 540, 600), 1));
        check(m.findTA("T1").getName().equals("T1") && m.findTA("T1").isAvailable(), "Rejected edits preserve TA");
        check(m.findCourse("C1").getCourseName().equals("C1") && m.findTA("T1").getCurrentWorkload() == 10,
                "Rejected course edits preserve state");
        m.updateTA("T1", "Updated", "new@school.edu", "Java", 11, "", 0);
        check(m.findCourse("C1").getAssignedTA(0) == m.findTA("T1"), "Edited TA references updated");
        m.updateCourse("C1", "New course", "Java", 4, new TimeSlot(1, 540, 600), 2);
        check(m.findTA("T1").getCurrentWorkload() == 9 && m.findTA("T2").getCurrentWorkload() == 12,
                "Course edit updates all workloads");
        m.addFaculty(new Faculty("F1", "Teacher", "f@school.edu", "CS"));
        int request = m.requestTA("F1", "Tuesday");
        rejects(IllegalArgumentException.class, () -> m.requestTA("F1", "Tuesday"));
        rejects(SchedulingConflictException.class, () -> m.approveRequest(request, "T1"));
        check(m.findRequest(request).getStatus().equals("Pending"), "Failed approval remains pending");
        m.approveRequest(request, "T3");
        rejects(IllegalStateException.class, () -> m.rejectRequest(request));
        int rejected = m.requestTA("F1", "Outside");
        m.rejectRequest(rejected);
        int pending = m.requestTA("F1", "Overlap");
        m.updateFaculty("F1", "Updated Faculty", "e", "IT");
        m.updateAdmin("Coordinator", "admin@school.edu");
        Path dir = Files.createTempDirectory("tas-test-");
        Path file = dir.resolve("data.txt");
        m.saveData(file.toString());
        ScheduleManager loaded = new ScheduleManager(10, 10);
        loaded.loadData(file.toString());
        check(loaded.getScheduleReport().equals(m.getScheduleReport()), "Schedule round trip");
        check(loaded.getFacultyReport("F1").equals(m.getFacultyReport("F1")), "Faculty requests round trip");
        check(loaded.findTA("T2") instanceof GraduateTA && loaded.findTA("T2").getCurrentWorkload() == 12,
                "Subtype and workload restored");
        check(loaded.getAdmin().getName().equals("Coordinator"), "Admin restored");
        Path second = dir.resolve("second.txt");
        loaded.saveData(second.toString());
        check(Files.readString(file).equals(Files.readString(second)), "Complete deterministic round trip");
        String valid = Files.readString(file), before = loaded.getScheduleReport();
        String[] invalid = { valid.replace("END", ""), valid + "junk\n",
                valid.replace("ASSIGN|C1|T1", "ASSIGN|C1|missing"),
                valid.replace("NEXT_REQUEST|4", "NEXT_REQUEST|1"), valid.replace("REGULAR", "UNKNOWN"),
                valid.replace("AVAIL|T1|1|480|720", "AVAIL|T1|1|700|720") };
        for (String text : invalid) {
            Files.writeString(second, text);
            rejects(IOException.class, () -> loaded.loadData(second.toString()));
            check(before.equals(loaded.getScheduleReport()), "Failed load preserves existing records");
        }
        rejects(IOException.class, () -> m.saveData(dir.resolve("missing/data.txt").toString()));
        check(valid.equals(Files.readString(file)), "Unrelated save failure leaves saved data intact");
        m.unassignTA("T3", "Tuesday");
        check(m.findTA("T3").getCurrentWorkload() == 0 && m.findRequest(request).getStatus().equals("Cancelled"),
                "Unassign releases hours and cancels approval");
        m.saveData(file.toString());
        loaded.loadData(file.toString());
        check(loaded.findRequest(request).getStatus().equals("Cancelled"), "Cancelled request round trip");
        m.removeTA("T2");
        check(m.findCourse("C1").getAssignedCount() == 1 && m.findTA("T2") == null, "Remove TA clears all courses");
        m.removeCourse("C1");
        check(m.findTA("T1").getCurrentWorkload() == 5, "Remove course frees workload");
        m.removeCourse("Overlap");
        check(m.findRequest(pending) == null, "Remove course deletes associated requests");
        m.removeFaculty("F1");
        check(m.findRequest(request) == null && m.findRequest(rejected) == null, "Remove faculty clears requests");
        m.setTAAvailable("T3", false);
        rejects(SchedulingConflictException.class, () -> m.assignTA("T3", "Tuesday"));
        TeachingAssistant windows = new TeachingAssistant("W", "W", "e", "Java", 10);
        windows.addAvailability(new TimeSlot(1, 600, 660));
        windows.addAvailability(new TimeSlot(1, 540, 600));
        check(windows.isAvailableFor(new TimeSlot(1, 550, 650)), "Adjacent unordered availability covers meeting");
        check(!windows.isAvailableFor(new TimeSlot(1, 550, 661)), "Partial availability rejected");
        rejects(IllegalArgumentException.class, () -> new TimeSlot(1, 600, 600));
        rejects(IllegalArgumentException.class, () -> TimeSlot.parseTime("24:01"));
        rejects(IllegalArgumentException.class, () -> new GraduateTA("G", "G", "e", "Java", 10, "AI", Double.NaN));
        rejects(IllegalArgumentException.class, () -> ta("A|B", false));
        check(TimeSlot.parseTime("24:00") == 1440, "Midnight endpoint");
        ScheduleManager small = new ScheduleManager(1, 1);
        small.addTA(ta("Only", false));
        rejects(IllegalStateException.class, () -> small.addTA(ta("Extra", false)));
        rejects(IOException.class, () -> small.loadData(file.toString()));
        check(small.getTACount() == 1 && small.findTA("Only") != null, "Capacity failure preserves state");
        check(!m.getScheduleReport().contains("\n\n"), "Report has no unnecessary blank lines");
        System.out.println("Passed " + checks + " checks. Test files: " + dir);
    }
}
