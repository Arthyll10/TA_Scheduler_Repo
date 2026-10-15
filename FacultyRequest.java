public class FacultyRequest {
    private int id;
    private String facultyId, courseCode;
    private String status = "Pending";
    private String assignedTAId = "";

    public FacultyRequest(int id, String facultyId, String courseCode) {
        if (id <= 0)
            throw new IllegalArgumentException("Request ID must be positive.");
        this.id = id;
        this.facultyId = Person.requireText(facultyId, "Faculty ID");
        this.courseCode = Person.requireText(courseCode, "Course code");
    }

    public int getId() {
        return id;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedTAId() {
        return assignedTAId;
    }

    void approve(String taId) {
        assignedTAId = Person.requireText(taId, "TA ID");
        status = "Approved";
    }

    void reject() {
        status = "Rejected";
        assignedTAId = "";
    }

    void cancel() {
        status = "Cancelled";
        assignedTAId = "";
    }
}
