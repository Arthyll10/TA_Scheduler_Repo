public class TeachingAssistant extends Person {
    private String expertise;
    private int maxWorkload, currentWorkload;
    private boolean isAvailable;

    public TeachingAssistant(String id, String name, String email, String expertise, int maxWorkload) {
        super(id, name, email);

        this.expertise = Person.checkText(expertise);
        if (maxWorkload <= 0)
            throw new IllegalArgumentException("Workload must be greater than zero.");
        this.maxWorkload = maxWorkload;
        this.currentWorkload = 0;
        this.isAvailable = true;
    }

    // Getters
    public String getExpertise() {
        return expertise;
    }

    public int getMaxWorkload() {
        return maxWorkload;
    }

    public int getCurrentWorkload() {
        return currentWorkload;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    // Setters
    public void setExpertise(String expertise) {
        this.expertise = Person.checkText(expertise);
    }

    public void setMaxWorkload(int maxWorkload) {
        if (maxWorkload <= 0)
            throw new IllegalArgumentException("Workload must be greater than zero.");
        this.maxWorkload = maxWorkload;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public void setCurrentWorkload(int currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public boolean canAcceptAssignment(int hours, String requiredExpertise) {
        if (hours <= 0) {
            return false;
        }

        if (!isAvailable) {
            return false;
        }

        if (!expertise.equalsIgnoreCase(requiredExpertise)) {
            return false;
        }

        if ((long) currentWorkload + hours > maxWorkload) {
            return false;
        }

        return true;
    }

    public void addWorkload(int hours) {
        currentWorkload += hours;
    }

    @Override
    public String getRoleDetails() {
        return String.format("Teaching Assistant : %s\nExpertise: %s\nWorkload: %d/%d",
                getName(), expertise, currentWorkload, maxWorkload);
    }
}
