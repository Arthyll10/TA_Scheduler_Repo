import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.FileReader;
import java.io.FileWriter;

public class ScheduleManager {
    private TeachingAssistant[] taList;
    private Course[] courseList;
    private Faculty[] facultyList;
    private FacultyRequest[] requestList;
    private int taCount, courseCount, facultyCount, requestCount;
    private int nextRequestId = 1;
    private Admin admin = new Admin("ADMIN", "Department Coordinator", "Not configured");

    public ScheduleManager(int taCapacity, int courseCapacity) {
        this(taCapacity, courseCapacity, 50, 200);
    }

    public ScheduleManager(int taCapacity, int courseCapacity, int facultyCapacity, int requestCapacity) {
        if (taCapacity <= 0 || courseCapacity <= 0 || facultyCapacity <= 0 || requestCapacity <= 0) {
            throw new IllegalArgumentException("Roster capacities must be positive.");
        }
        taList = new TeachingAssistant[taCapacity];
        courseList = new Course[courseCapacity];
        facultyList = new Faculty[facultyCapacity];
        requestList = new FacultyRequest[requestCapacity];
    }

    public int getTACount() {
        return taCount;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getFacultyCount() {
        return facultyCount;
    }

    public Admin getAdmin() {
        return admin;
    }

    public TeachingAssistant findTA(String id) {
        for (int i = 0; i < taCount; i++)
            if (taList[i].getId().equalsIgnoreCase(id))
                return taList[i];
        return null;
    }

    public Course findCourse(String code) {
        for (int i = 0; i < courseCount; i++)
            if (courseList[i].getCourseCode().equalsIgnoreCase(code))
                return courseList[i];
        return null;
    }

    public Faculty findFaculty(String id) {
        for (int i = 0; i < facultyCount; i++)
            if (facultyList[i].getId().equalsIgnoreCase(id))
                return facultyList[i];
        return null;
    }

    public FacultyRequest findRequest(int id) {
        for (int i = 0; i < requestCount; i++)
            if (requestList[i].getId() == id)
                return requestList[i];
        return null;
    }

    private TeachingAssistant requireTA(String id) {
        TeachingAssistant ta = findTA(id);
        if (ta == null)
            throw new IllegalArgumentException("TA ID was not found.");
        return ta;
    }

    private Course requireCourse(String code) {
        Course course = findCourse(code);
        if (course == null)
            throw new IllegalArgumentException("Course code was not found.");
        return course;
    }

    private Faculty requireFaculty(String id) {
        Faculty faculty = findFaculty(id);
        if (faculty == null)
            throw new IllegalArgumentException("Faculty ID was not found.");
        return faculty;
    }

    private void checkPersonId(String id) {
        if (findTA(id) != null || findFaculty(id) != null || admin.getId().equalsIgnoreCase(id)) {
            throw new IllegalArgumentException("Person ID already exists.");
        }
    }

    public void addTA(TeachingAssistant ta) {
        if (ta == null)
            throw new IllegalArgumentException("TA cannot be null.");
        checkPersonId(ta.getId());
        if (taCount == taList.length)
            throw new IllegalStateException("The TA list is full.");
        if (ta.getCurrentWorkload() != 0)
            throw new IllegalArgumentException("A new TA must have zero assigned workload.");
        taList[taCount] = ta;
        taCount++;
    }

    public void addCourse(Course course) {
        if (course == null)
            throw new IllegalArgumentException("Course cannot be null.");
        if (findCourse(course.getCourseCode()) != null)
            throw new IllegalArgumentException("Course code already exists.");
        if (courseCount == courseList.length)
            throw new IllegalStateException("The course list is full.");
        if (course.isAssigned())
            throw new IllegalArgumentException("Register the course before assigning TAs.");
        courseList[courseCount] = course;
        courseCount++;
    }

    public void addFaculty(Faculty faculty) {
        if (faculty == null)
            throw new IllegalArgumentException("Faculty cannot be null.");
        checkPersonId(faculty.getId());
        if (facultyCount == facultyList.length)
            throw new IllegalStateException("The faculty list is full.");
        facultyList[facultyCount] = faculty;
        facultyCount++;
    }

    public void updateFaculty(String id, String name, String email, String department) {
        Faculty old = requireFaculty(id);
        Faculty replacement = new Faculty(old.getId(), name, email, department);
        for (int i = 0; i < facultyCount; i++)
            if (facultyList[i] == old)
                facultyList[i] = replacement;
    }

    public void updateAdmin(String name, String email) {
        admin = new Admin(admin.getId(), name, email);
    }

    // ignoredCourse is used during edits: its old hours/time must not be counted
    // twice.
    private void checkAssignment(TeachingAssistant ta, Course course, Course ignoredCourse)
            throws SchedulingConflictException {
        if (!ta.isAvailable())
            throw new SchedulingConflictException("The TA is marked unavailable.");
        if (!ta.getExpertise().equalsIgnoreCase(course.getRequiredExpertise())) {
            throw new SchedulingConflictException("The TA's expertise does not match this course.");
        }
        if (!ta.isAvailableFor(course.getTimeSlot())) {
            throw new SchedulingConflictException("The course is outside the TA's weekly availability.");
        }
        long hours = 0;
        for (int i = 0; i < courseCount; i++) {
            Course existing = courseList[i];
            if (existing != ignoredCourse && existing.hasTA(ta.getId())) {
                hours += existing.getRequiredHours();
                if (existing.getTimeSlot().overlaps(course.getTimeSlot())) {
                    throw new SchedulingConflictException("Time overlaps with " + existing.getCourseCode() + ".");
                }
            }
        }
        hours += course.getRequiredHours();
        if (hours > ta.getWorkloadLimit() || hours > Integer.MAX_VALUE) {
            throw new SchedulingConflictException("Assignment would exceed the TA's workload limit.");
        }
    }

    public void assignTA(String taId, String courseCode) throws SchedulingConflictException {
        TeachingAssistant ta = findTA(taId);
        Course course = findCourse(courseCode);
        if (ta == null)
            throw new SchedulingConflictException("TA ID was not found.");
        if (course == null)
            throw new SchedulingConflictException("Course code was not found.");
        if (course.hasTA(taId))
            throw new SchedulingConflictException("TA is already assigned to this course.");
        if (course.isFull())
            throw new SchedulingConflictException("All TA positions for this course are filled.");
        checkAssignment(ta, course, null);
        course.addAssignedTA(ta);
        recalculateWorkloads();
    }

    public void unassignTA(String taId, String courseCode) {
        requireTA(taId);
        requireCourse(courseCode).removeAssignedTA(taId);
        cancelApprovedRequests(taId, courseCode);
        recalculateWorkloads();
    }

    private void recalculateWorkloads() {
        for (int i = 0; i < taCount; i++) {
            long hours = 0;
            for (int j = 0; j < courseCount; j++) {
                if (courseList[j].hasTA(taList[i].getId()))
                    hours += courseList[j].getRequiredHours();
            }
            if (hours > Integer.MAX_VALUE)
                throw new IllegalStateException("Workload is outside the supported range.");
            taList[i].setCurrentWorkload((int) hours);
        }
    }

    public void updateTA(String id, String name, String email, String expertise,
            int maxWorkload, String domain, double stipend) throws SchedulingConflictException {
        TeachingAssistant old = requireTA(id);

        // Build the edited record, then check it before replacing the original.
        TeachingAssistant updated;
        if (old instanceof GraduateTA) {
            updated = new GraduateTA(old.getId(), name, email, expertise, maxWorkload, domain, stipend);
        } else {
            updated = new TeachingAssistant(old.getId(), name, email, expertise, maxWorkload);
        }
        updated.setAvailable(old.isAvailable());
        for (int i = 0; i < old.getAvailabilityCount(); i++) {
            updated.addAvailability(old.getAvailability(i));
        }
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i].hasTA(id)) {
                checkAssignment(updated, courseList[i], courseList[i]);
            }
        }
        for (int i = 0; i < taCount; i++) {
            if (taList[i] == old) {
                taList[i] = updated;
            }
        }
        for (int i = 0; i < courseCount; i++) {
            courseList[i].replaceTA(updated);
        }
        recalculateWorkloads();
    }

    public void setTAAvailable(String id, boolean available) throws SchedulingConflictException {
        TeachingAssistant ta = requireTA(id);
        if (!available && ta.getCurrentWorkload() > 0) {
            throw new SchedulingConflictException("Unassign this TA before marking them unavailable.");
        }
        ta.setAvailable(available);
    }

    public void addAvailability(String id, TimeSlot slot) {
        // Adding available time cannot invalidate an existing assignment.
        TeachingAssistant ta = requireTA(id);
        ta.addAvailability(slot);
    }

    public void removeAvailability(String id, int index) throws SchedulingConflictException {
        TeachingAssistant ta = requireTA(id);
        ta.getAvailability(index); // Check that the selected window exists.
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i].hasTA(id) && !ta.isAvailableFor(courseList[i].getTimeSlot(), index)) {
                throw new SchedulingConflictException(
                        "This window is needed for " + courseList[i].getCourseCode() + ".");
            }
        }
        ta.removeAvailability(index);
    }

    public void updateCourse(String code, String name, String expertise, int hours,
            TimeSlot slot, int requiredTAs) throws SchedulingConflictException {
        Course old = requireCourse(code);
        Course replacement = new Course(old.getCourseCode(), name, expertise, hours, slot, requiredTAs);
        if (requiredTAs < old.getAssignedCount()) {
            throw new SchedulingConflictException("Unassign TAs before reducing positions below the assigned count.");
        }
        for (int i = 0; i < old.getAssignedCount(); i++) {
            TeachingAssistant ta = old.getAssignedTA(i);
            checkAssignment(ta, replacement, old);
            replacement.addAssignedTA(ta);
        }
        for (int i = 0; i < courseCount; i++)
            if (courseList[i] == old)
                courseList[i] = replacement;
        recalculateWorkloads();
    }

    public void removeTA(String id) {
        TeachingAssistant ta = requireTA(id);
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i].hasTA(id))
                courseList[i].removeAssignedTA(id);
        }
        cancelApprovedRequests(id, null);
        for (int i = 0; i < taCount; i++) {
            if (taList[i] == ta) {
                for (int j = i; j < taCount - 1; j++)
                    taList[j] = taList[j + 1];
                taCount--;
                taList[taCount] = null;
                break;
            }
        }
        recalculateWorkloads();
    }

    public void removeCourse(String code) {
        Course course = requireCourse(code);
        for (int i = 0; i < courseCount; i++) {
            if (courseList[i] == course) {
                for (int j = i; j < courseCount - 1; j++)
                    courseList[j] = courseList[j + 1];
                courseCount--;
                courseList[courseCount] = null;
                break;
            }
        }
        for (int i = requestCount - 1; i >= 0; i--) {
            if (requestList[i].getCourseCode().equalsIgnoreCase(code))
                removeRequestAt(i);
        }
        recalculateWorkloads();
    }

    public void removeFaculty(String id) {
        Faculty faculty = requireFaculty(id);
        for (int i = 0; i < facultyCount; i++) {
            if (facultyList[i] == faculty) {
                for (int j = i; j < facultyCount - 1; j++)
                    facultyList[j] = facultyList[j + 1];
                facultyCount--;
                facultyList[facultyCount] = null;
                break;
            }
        }
        for (int i = requestCount - 1; i >= 0; i--) {
            if (requestList[i].getFacultyId().equalsIgnoreCase(id))
                removeRequestAt(i);
        }
    }

    private void removeRequestAt(int index) {
        for (int i = index; i < requestCount - 1; i++)
            requestList[i] = requestList[i + 1];
        requestCount--;
        requestList[requestCount] = null;
    }

    private void cancelApprovedRequests(String taId, String courseCode) {
        for (int i = 0; i < requestCount; i++) {
            FacultyRequest request = requestList[i];
            if (request.getStatus().equals("Approved") && request.getAssignedTAId().equalsIgnoreCase(taId)
                    && (courseCode == null || request.getCourseCode().equalsIgnoreCase(courseCode)))
                request.cancel();
        }
    }

    public int requestTA(String facultyId, String courseCode) {
        Faculty faculty = requireFaculty(facultyId);
        Course course = requireCourse(courseCode);
        if (course.isFull())
            throw new IllegalStateException("This course already has all required TAs.");
        if (requestCount == requestList.length)
            throw new IllegalStateException("The request list is full.");
        if (nextRequestId == Integer.MAX_VALUE)
            throw new IllegalStateException("Request ID limit reached.");
        for (int i = 0; i < requestCount; i++) {
            FacultyRequest existing = requestList[i];
            if (existing.getStatus().equals("Pending") && existing.getFacultyId().equalsIgnoreCase(facultyId)
                    && existing.getCourseCode().equalsIgnoreCase(courseCode)) {
                throw new IllegalArgumentException("This faculty member already has a pending request for the course.");
            }
        }
        FacultyRequest request = new FacultyRequest(nextRequestId, faculty.getId(), course.getCourseCode());
        nextRequestId++;
        requestList[requestCount] = request;
        requestCount++;
        return request.getId();
    }

    public void approveRequest(int id, String taId) throws SchedulingConflictException {
        FacultyRequest request = requirePendingRequest(id);
        assignTA(taId, request.getCourseCode());
        request.approve(requireTA(taId).getId());
    }

    public void rejectRequest(int id) {
        requirePendingRequest(id).reject();
    }

    private FacultyRequest requirePendingRequest(int id) {
        FacultyRequest request = findRequest(id);
        if (request == null)
            throw new IllegalArgumentException("Request ID was not found.");
        if (!request.getStatus().equals("Pending"))
            throw new IllegalStateException("Only pending requests can be reviewed.");
        return request;
    }

    public String getAvailabilityReport(String id) {
        TeachingAssistant ta = requireTA(id);
        String report = "Availability for " + ta.getName() + " (" + ta.getId() + ")\n";
        report += "Accepting assignments: " + (ta.isAvailable() ? "Yes" : "No") + "\n";
        if (ta.getAvailabilityCount() == 0)
            report += "No availability windows. Add one before assigning a course.\n";
        for (int i = 0; i < ta.getAvailabilityCount(); i++)
            report += (i + 1) + ". " + ta.getAvailability(i) + "\n";
        return report;
    }

    public void viewTAs() {
        if (taCount == 0)
            System.out.println("No TAs registered.");
        for (int i = 0; i < taCount; i++) {
            TeachingAssistant ta = taList[i];
            System.out.println("-".repeat(60));
            System.out.println("TA ID              : " + ta.getId());
            System.out.println("Email              : " + ta.getEmail());
            System.out.println(ta.getRoleDetails());
            System.out.print(getAvailabilityReport(ta.getId()));
        }
    }

    public void viewCourses() {
        if (courseCount == 0)
            System.out.println("No courses registered.");
        for (int i = 0; i < courseCount; i++) {
            Course course = courseList[i];
            System.out.printf("%s | %s | %s | %d hours/TA | %d/%d TAs%n",
                    course.getCourseCode(), course.getCourseName(), course.getTimeSlot(),
                    course.getRequiredHours(), course.getAssignedCount(), course.getRequiredTAs());
        }
    }

    public void viewFaculty() {
        if (facultyCount == 0)
            System.out.println("No faculty registered.");
        for (int i = 0; i < facultyCount; i++) {
            System.out.println("-".repeat(60));
            System.out.println("Faculty ID         : " + facultyList[i].getId());
            System.out.println("Email              : " + facultyList[i].getEmail());
            System.out.println(facultyList[i].getRoleDetails());
        }
    }

    public void viewEligibleTAs(String courseCode) {
        Course course = requireCourse(courseCode);
        boolean found = false;
        for (int i = 0; i < taCount; i++) {
            TeachingAssistant ta = taList[i];
            if (course.isFull() || course.hasTA(ta.getId()))
                continue;
            try {
                checkAssignment(ta, course, null);
                System.out.println(ta.getId() + " | " + ta.getName() + " | " + ta.getCurrentWorkload() + "/"
                        + ta.getWorkloadLimit() + " hours");
                found = true;
            } catch (SchedulingConflictException e) {
                // This TA is not eligible; continue checking the remaining roster.
            }
        }
        if (!found)
            System.out.println("No eligible TAs for the available positions.");
    }

    public String getScheduleReport() {
        String border = "=".repeat(60);
        String divider = "-".repeat(60);
        String report = border + "\nTEACHING ASSISTANT SCHEDULE REPORT\nRegistered courses: " + courseCount + "\n"
                + border + "\n";
        if (courseCount == 0)
            report += "No courses registered.\n";
        for (int i = 0; i < courseCount; i++) {
            Course course = courseList[i];
            report += String.format("COURSE %d: %s - %s%n%s%nWeekly meeting     : %s%nRequired expertise : %s%n"
                    + "Weekly hours / TA  : %d%nTA positions       : %d/%d filled%n",
                    i + 1, course.getCourseCode(), course.getCourseName(), divider, course.getTimeSlot(),
                    course.getRequiredExpertise(), course.getRequiredHours(), course.getAssignedCount(),
                    course.getRequiredTAs());
            if (!course.isAssigned())
                report += "Status             : Unassigned\n";
            for (int j = 0; j < course.getAssignedCount(); j++) {
                TeachingAssistant ta = course.getAssignedTA(j);
                report += "--- ASSIGNED TA " + (j + 1) + " ---\nTA ID              : " + ta.getId()
                        + "\nEmail              : " + ta.getEmail() + "\n" + ta.getRoleDetails() + "\n";
            }
            report += divider + "\n";
        }
        return report + border + "\n";
    }

    public String getTASchedule(String id) {
        TeachingAssistant ta = requireTA(id);
        String report = ta.getRoleDetails() + "\n" + getAvailabilityReport(id) + "--- ASSIGNED COURSES ---\n";
        boolean found = false;
        for (int i = 0; i < courseCount; i++) {
            Course course = courseList[i];
            if (course.hasTA(id)) {
                report += course.getCourseCode() + " | " + course.getCourseName() + " | " + course.getTimeSlot()
                        + " | " + course.getRequiredHours() + " hours\n";
                found = true;
            }
        }
        if (!found)
            report += "No assigned courses.\n";
        return report;
    }

    public String getRequestsReport(String facultyId) {
        String report = "--- FACULTY REQUESTS ---\n";
        boolean found = false;
        for (int i = 0; i < requestCount; i++) {
            FacultyRequest r = requestList[i];
            if (facultyId == null || r.getFacultyId().equalsIgnoreCase(facultyId)) {
                report += String.format("Request %d | Faculty: %s | Course: %s | %s | Assigned TA: %s%n",
                        r.getId(), r.getFacultyId(), r.getCourseCode(), r.getStatus(),
                        r.getAssignedTAId().isEmpty() ? "None" : r.getAssignedTAId());
                found = true;
            }
        }
        if (!found)
            report += "No requests.\n";
        return report;
    }

    public String getFacultyReport(String id) {
        Faculty faculty = requireFaculty(id);
        return faculty.getRoleDetails() + "\n" + getRequestsReport(id);
    }

    public String getWorkloadReport() {
        String report = "--- WEEKLY WORKLOAD REPORT ---\n";
        if (taCount == 0)
            return report + "No TAs registered.\n";
        for (int i = 0; i < taCount; i++) {
            TeachingAssistant ta = taList[i];
            report += String.format("%s | %s | %d/%d hours | Remaining: %d%n",
                    ta.getId(), ta.getName(), ta.getCurrentWorkload(), ta.getWorkloadLimit(),
                    ta.getWorkloadLimit() - ta.getCurrentWorkload());
        }
        return report;
    }

    public void viewAndExportSchedule(String filePath) throws IOException {
        String report = getScheduleReport();
        writeFile(filePath, report);
        System.out.print(report);
        System.out.println("[SUCCESS] Exported to " + filePath);
    }

    private void writeFile(String filePath, String contents) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(contents);
        }
    }

    public void saveData(String filePath) throws IOException {
        String data = "TAS|1\nADMIN|" + admin.getId() + "|" + admin.getName() + "|" + admin.getEmail() + "\n";
        for (int i = 0; i < taCount; i++) {
            TeachingAssistant ta = taList[i];
            String type = "REGULAR", domain = "";
            double stipend = 0;
            if (ta instanceof GraduateTA) {
                type = "GRADUATE";
                domain = ((GraduateTA) ta).getResearchDomain();
                stipend = ((GraduateTA) ta).getHourlyStipend();
            }
            data += "TA|" + ta.getId() + "|" + ta.getName() + "|" + ta.getEmail() + "|" + ta.getExpertise()
                    + "|" + ta.getMaxWorkload() + "|" + ta.isAvailable() + "|" + type + "|" + domain + "|" + stipend
                    + "\n";
            for (int j = 0; j < ta.getAvailabilityCount(); j++) {
                TimeSlot slot = ta.getAvailability(j);
                data += "AVAIL|" + ta.getId() + "|" + slot.getDay() + "|" + slot.getStartMinute() + "|"
                        + slot.getEndMinute() + "\n";
            }
        }
        for (int i = 0; i < facultyCount; i++) {
            Faculty f = facultyList[i];
            data += "FACULTY|" + f.getId() + "|" + f.getName() + "|" + f.getEmail() + "|" + f.getDepartment() + "\n";
        }
        for (int i = 0; i < courseCount; i++) {
            Course c = courseList[i];
            TimeSlot slot = c.getTimeSlot();
            data += "COURSE|" + c.getCourseCode() + "|" + c.getCourseName() + "|" + c.getRequiredExpertise()
                    + "|" + c.getRequiredHours() + "|" + slot.getDay() + "|" + slot.getStartMinute()
                    + "|" + slot.getEndMinute() + "|" + c.getRequiredTAs() + "\n";
        }
        for (int i = 0; i < courseCount; i++) {
            Course c = courseList[i];
            for (int j = 0; j < c.getAssignedCount(); j++)
                data += "ASSIGN|" + c.getCourseCode() + "|" + c.getAssignedTA(j).getId() + "\n";
        }
        for (int i = 0; i < requestCount; i++) {
            FacultyRequest r = requestList[i];
            data += "REQUEST|" + r.getId() + "|" + r.getFacultyId() + "|" + r.getCourseCode() + "|" + r.getStatus()
                    + "|" + r.getAssignedTAId() + "\n";
        }
        data += "NEXT_REQUEST|" + nextRequestId + "\nEND\n";
        writeFile(filePath, data);
    }

    public void loadData(String filePath) throws IOException {
        ScheduleManager loaded = new ScheduleManager(taList.length, courseList.length, facultyList.length,
                requestList.length);
        int lineNumber = 1;
        boolean ended = false, hasAdmin = false, hasNextId = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            if (!"TAS|1".equals(reader.readLine()))
                throw new IOException("Unsupported or missing TAS file header.");
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (ended)
                    throw new IllegalArgumentException("Unexpected data after END.");
                String[] fields = line.split("\\|", -1);
                switch (fields[0]) {
                    case "ADMIN": {
                        fieldCount(fields, 4);
                        if (hasAdmin || !fields[1].equals("ADMIN"))
                            throw new IllegalArgumentException("Invalid admin record.");
                        loaded.updateAdmin(fields[2], fields[3]);
                        hasAdmin = true;
                        break;
                    }
                    case "TA": {
                        fieldCount(fields, 10);
                        TeachingAssistant ta;
                        if (fields[7].equals("GRADUATE")) {
                            ta = new GraduateTA(fields[1], fields[2], fields[3], fields[4], Integer.parseInt(fields[5]),
                                    fields[8], Double.parseDouble(fields[9]));
                        } else if (fields[7].equals("REGULAR") && fields[8].isEmpty() && fields[9].equals("0.0")) {
                            ta = new TeachingAssistant(fields[1], fields[2], fields[3], fields[4],
                                    Integer.parseInt(fields[5]));
                        } else
                            throw new IllegalArgumentException("Invalid TA type or fields.");
                        if (!fields[6].equals("true") && !fields[6].equals("false"))
                            throw new IllegalArgumentException("Invalid availability flag.");
                        ta.setAvailable(Boolean.parseBoolean(fields[6]));
                        loaded.addTA(ta);
                        break;
                    }
                    case "AVAIL": {
                        fieldCount(fields, 5);
                        loaded.addAvailability(fields[1], new TimeSlot(Integer.parseInt(fields[2]),
                                Integer.parseInt(fields[3]), Integer.parseInt(fields[4])));
                        break;
                    }
                    case "FACULTY": {
                        fieldCount(fields, 5);
                        loaded.addFaculty(new Faculty(fields[1], fields[2], fields[3], fields[4]));
                        break;
                    }
                    case "COURSE": {
                        fieldCount(fields, 9);
                        loaded.addCourse(new Course(fields[1], fields[2], fields[3], Integer.parseInt(fields[4]),
                                new TimeSlot(Integer.parseInt(fields[5]), Integer.parseInt(fields[6]),
                                        Integer.parseInt(fields[7])),
                                Integer.parseInt(fields[8])));
                        break;
                    }
                    case "ASSIGN": {
                        fieldCount(fields, 3);
                        loaded.assignTA(fields[2], fields[1]);
                        break;
                    }
                    case "REQUEST": {
                        fieldCount(fields, 6);
                        loaded.restoreRequest(fields);
                        break;
                    }
                    case "NEXT_REQUEST": {
                        fieldCount(fields, 2);
                        if (hasNextId)
                            throw new IllegalArgumentException("Duplicate request counter.");
                        loaded.nextRequestId = Integer.parseInt(fields[1]);
                        hasNextId = true;
                        break;
                    }
                    case "END": {
                        fieldCount(fields, 1);
                        ended = true;
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown record type.");
                }
            }
            if (!ended || !hasAdmin || !hasNextId)
                throw new IllegalArgumentException("Incomplete data file.");
            if (loaded.nextRequestId <= 0)
                throw new IllegalArgumentException("Invalid request counter.");
            for (int i = 0; i < loaded.requestCount; i++) {
                if (loaded.requestList[i].getId() >= loaded.nextRequestId)
                    throw new IllegalArgumentException("Request counter must exceed saved IDs.");
            }
        } catch (IllegalArgumentException | IllegalStateException | SchedulingConflictException e) {
            throw new IOException("Invalid data on or near line " + lineNumber + ": " + e.getMessage(), e);
        }
        // Replace live state only after the entire file has passed validation.
        taList = loaded.taList;
        courseList = loaded.courseList;
        facultyList = loaded.facultyList;
        requestList = loaded.requestList;
        taCount = loaded.taCount;
        courseCount = loaded.courseCount;
        facultyCount = loaded.facultyCount;
        requestCount = loaded.requestCount;
        nextRequestId = loaded.nextRequestId;
        admin = loaded.admin;
    }

    private static void fieldCount(String[] fields, int expected) {
        if (fields.length != expected)
            throw new IllegalArgumentException("Wrong number of fields.");
    }

    private void restoreRequest(String[] fields) {
        int id = Integer.parseInt(fields[1]);
        if (findRequest(id) != null)
            throw new IllegalArgumentException("Duplicate request ID.");
        if (requestCount == requestList.length)
            throw new IllegalStateException("The request list is full.");
        Faculty faculty = requireFaculty(fields[2]);
        Course course = requireCourse(fields[3]);
        FacultyRequest request = new FacultyRequest(id, faculty.getId(), course.getCourseCode());
        switch (fields[4]) {
            case "Approved": {
                TeachingAssistant ta = requireTA(fields[5]);
                if (!course.hasTA(ta.getId()))
                    throw new IllegalArgumentException("Approved request has no matching assignment.");
                request.approve(ta.getId());
                break;
            }
            case "Pending": {
                if (!fields[5].isEmpty())
                    throw new IllegalArgumentException("Pending request cannot have an assigned TA.");
                for (int i = 0; i < requestCount; i++) {
                    FacultyRequest r = requestList[i];
                    if (r.getStatus().equals("Pending") && r.getFacultyId().equalsIgnoreCase(fields[2])
                            && r.getCourseCode().equalsIgnoreCase(fields[3]))
                        throw new IllegalArgumentException("Duplicate pending request.");
                }
                break;
            }
            case "Rejected": {
                if (!fields[5].isEmpty())
                    throw new IllegalArgumentException("Rejected request cannot have an assigned TA.");
                request.reject();
                break;
            }
            case "Cancelled": {
                if (!fields[5].isEmpty())
                    throw new IllegalArgumentException("Cancelled request cannot have an assigned TA.");
                request.cancel();
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown request status.");
        }
        requestList[requestCount] = request;
        requestCount++;
    }
}
