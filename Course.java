public class Course {
    private String courseCode, courseName, requiredExpertise;
    private int requiredHours, requiredTAs, assignedCount;
    private TimeSlot timeSlot;
    private TeachingAssistant[] assignedTAs;

    public Course(String code, String name, String expertise, int hours, TimeSlot slot, int requiredTAs) {
        courseCode = Person.requireText(code, "Course code");
        courseName = Person.requireText(name, "Course name");
        requiredExpertise = Person.requireText(expertise, "Required expertise");
        if (hours <= 0)
            throw new IllegalArgumentException("Hours must be greater than zero.");
        if (slot == null)
            throw new IllegalArgumentException("A course needs a weekly time slot.");
        if (requiredTAs < 1 || requiredTAs > 50) {
            throw new IllegalArgumentException("Required TAs must be between 1 and 50.");
        }
        requiredHours = hours;
        timeSlot = slot;
        this.requiredTAs = requiredTAs;
        assignedTAs = new TeachingAssistant[requiredTAs];
    }

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

    public int getRequiredTAs() {
        return requiredTAs;
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public boolean isAssigned() {
        return assignedCount > 0;
    }

    public boolean isFull() {
        return assignedCount == requiredTAs;
    }

    public TeachingAssistant getAssignedTA(int index) {
        if (index < 0 || index >= assignedCount)
            throw new IllegalArgumentException("Assigned TA was not found.");
        return assignedTAs[index];
    }

    public boolean hasTA(String id) {
        for (int i = 0; i < assignedCount; i++) {
            if (assignedTAs[i].getId().equalsIgnoreCase(id))
                return true;
        }
        return false;
    }

    void addAssignedTA(TeachingAssistant ta) {
        if (ta == null)
            throw new IllegalArgumentException("TA cannot be null.");
        if (hasTA(ta.getId()))
            throw new IllegalArgumentException("TA is already assigned to this course.");
        if (isFull())
            throw new IllegalStateException("All TA positions for this course are filled.");
        assignedTAs[assignedCount] = ta;
        assignedCount++;
    }

    void removeAssignedTA(String id) {
        for (int i = 0; i < assignedCount; i++) {
            if (assignedTAs[i].getId().equalsIgnoreCase(id)) {
                for (int j = i; j < assignedCount - 1; j++)
                    assignedTAs[j] = assignedTAs[j + 1];
                assignedCount--;
                assignedTAs[assignedCount] = null;
                return;
            }
        }
        throw new IllegalArgumentException("That TA is not assigned to this course.");
    }

    void replaceTA(TeachingAssistant replacement) {
        for (int i = 0; i < assignedCount; i++) {
            if (assignedTAs[i].getId().equalsIgnoreCase(replacement.getId()))
                assignedTAs[i] = replacement;
        }
    }
}
