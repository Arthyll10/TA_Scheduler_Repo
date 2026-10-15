public class Course {
    private String courseCode, courseName, requiredExpertise;
    private int requiredHours;
    private TeachingAssistant assignedTA;

    public Course(String courseCode, String courseName, String requiredExpertise, int requiredHours) {
        this.courseCode = Person.checkText(courseCode);
        this.courseName = Person.checkText(courseName);
        this.requiredExpertise = Person.checkText(requiredExpertise);
        if (requiredHours <= 0)
            throw new IllegalArgumentException("Hours must be greater than zero.");
        this.requiredHours = requiredHours;
        this.assignedTA = null;
    }

    // Getters
    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getRequiredExpertise() {
        return requiredExpertise;
    }

    public int getRequiredHours() {
        return requiredHours;
    }

    public TeachingAssistant getAssignedTA() {
        return assignedTA;
    }

    // Setters
    public void setCourseCode(String courseCode) {
        this.courseCode = Person.checkText(courseCode);
    }

    public void setCourseName(String courseName) {
        this.courseName = Person.checkText(courseName);
    }

    public void setRequiredExpertise(String requiredExpertise) {
        this.requiredExpertise = Person.checkText(requiredExpertise);
    }

    public void setRequiredHours(int requiredHours) {
        if (requiredHours <= 0)
            throw new IllegalArgumentException("Hours must be greater than zero.");
        this.requiredHours = requiredHours;
    }

    public void setAssignedTA(TeachingAssistant assignedTA) {
        this.assignedTA = assignedTA;
    }

    public boolean isAssigned() {
        return assignedTA != null;
    }
}
