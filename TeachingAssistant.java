public class TeachingAssistant extends Person {
    private String expertise;
    private int maxWorkload, currentWorkload;
    private boolean isAvailable;
    private TimeSlot[] availability = new TimeSlot[21];
    private int availabilityCount;

    public TeachingAssistant(String id, String name, String email, String expertise, int maxWorkload) {
        super(id, name, email);
        this.expertise = Person.requireText(expertise, "Expertise");
        if (maxWorkload <= 0) {
            throw new IllegalArgumentException("Workload must be greater than zero.");
        }
        this.maxWorkload = maxWorkload;
        this.currentWorkload = 0;
        this.isAvailable = true;
    }

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

    public long getWorkloadLimit() {
        return maxWorkload;
    }

    public int getAvailabilityCount() {
        return availabilityCount;
    }

    public TimeSlot getAvailability(int index) {
        if (index < 0 || index >= availabilityCount) {
            throw new IllegalArgumentException("Availability number was not found.");
        }
        return availability[index];
    }

    // ScheduleManager controls changes that could affect existing assignments.
    void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    void setCurrentWorkload(int workload) {
        if (workload < 0 || workload > getWorkloadLimit()) {
            throw new IllegalArgumentException("Invalid current workload.");
        }
        currentWorkload = workload;
    }

    void addAvailability(TimeSlot slot) {
        if (slot == null)
            throw new IllegalArgumentException("Availability cannot be null.");
        if (availabilityCount == availability.length) {
            throw new IllegalStateException("A TA can have up to 21 availability windows.");
        }
        for (int i = 0; i < availabilityCount; i++) {
            TimeSlot existing = availability[i];
            if (existing.getDay() == slot.getDay()
                    && existing.getStartMinute() == slot.getStartMinute()
                    && existing.getEndMinute() == slot.getEndMinute()) {
                throw new IllegalArgumentException("That availability window already exists.");
            }
        }
        availability[availabilityCount] = slot;
        availabilityCount++;
    }

    void removeAvailability(int index) {
        getAvailability(index);
        for (int i = index; i < availabilityCount - 1; i++) {
            availability[i] = availability[i + 1];
        }
        availabilityCount--;
        availability[availabilityCount] = null;
    }

    public boolean isAvailableFor(TimeSlot slot) {
        return isAvailableFor(slot, -1);
    }

    // During removal, skip the selected window to check the remaining availability.
    boolean isAvailableFor(TimeSlot slot, int skippedIndex) {
        if (!isAvailable || slot == null)
            return false;
        // Adjacent windows can jointly cover one course meeting.
        int coveredUntil = slot.getStartMinute();
        while (coveredUntil < slot.getEndMinute()) {
            int nextEnd = coveredUntil;
            for (int i = 0; i < availabilityCount; i++) {
                if (i == skippedIndex) {
                    continue;
                }
                TimeSlot window = availability[i];
                if (window.getDay() == slot.getDay()
                        && window.getStartMinute() <= coveredUntil
                        && window.getEndMinute() > nextEnd) {
                    nextEnd = window.getEndMinute();
                }
            }
            if (nextEnd == coveredUntil)
                return false;
            coveredUntil = nextEnd;
        }
        return true;
    }

    public boolean canAcceptAssignment(int hours, String requiredExpertise) {
        long total = (long) currentWorkload + hours;
        return hours > 0 && isAvailable && expertise.equalsIgnoreCase(requiredExpertise)
                && total <= getWorkloadLimit() && total <= Integer.MAX_VALUE;
    }

    void addWorkload(int hours) {
        if (!canAcceptAssignment(hours, expertise)) {
            throw new IllegalArgumentException("The TA cannot accept these additional hours.");
        }
        currentWorkload += hours;
    }

    @Override
    public String getRoleDetails() {
        return String.format(
                "Role               : Teaching Assistant%nName               : %s%n"
                        + "Expertise          : %s%nWeekly workload    : %d/%d hours%nAvailable          : %s",
                getName(), expertise, currentWorkload, getWorkloadLimit(), isAvailable ? "Yes" : "No");
    }
}
