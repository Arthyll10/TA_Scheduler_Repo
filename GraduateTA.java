public class GraduateTA extends TeachingAssistant {
    private String researchDomain;
    private double hourlyStipend;

    public GraduateTA(String id, String name, String email, String expertise,
            int maxWorkload, String researchDomain, double hourlyStipend) {
        super(id, name, email, expertise, maxWorkload);
        this.researchDomain = Person.checkText(researchDomain);
        if (!Double.isFinite(hourlyStipend) || hourlyStipend < 0) {
            throw new IllegalArgumentException("Stipend must be a finite number of zero or more.");
        }
        this.hourlyStipend = hourlyStipend;
    }

    // Getters
    public String getResearchDomain() {
        return researchDomain;
    }

    public double getHourlyStipend() {
        return hourlyStipend;
    }

    // Setters
    public void setResearchDomain(String researchDomain) {
        this.researchDomain = Person.checkText(researchDomain);
    }

    public void setHourlyStipend(double hourlyStipend) {
        if (!Double.isFinite(hourlyStipend) || hourlyStipend < 0) {
            throw new IllegalArgumentException("Stipend must be a finite number of zero or more.");
        }
        this.hourlyStipend = hourlyStipend;
    }

    @Override
    public boolean canAcceptAssignment(int hours, String requiredExpertise) {
        if (hours <= 0) {
            return false;
        }

        if (!isAvailable()) {
            return false;
        }

        if (!getExpertise().equalsIgnoreCase(requiredExpertise)) {
            return false;
        }

        if ((long) getCurrentWorkload() + hours > (long) getMaxWorkload() + 3
                || (long) getCurrentWorkload() + hours > Integer.MAX_VALUE) {
            return false;
        }

        return true;
    }

    @Override
    public String getRoleDetails() {
        return String.format(
                "Graduate Teaching Assistant : %s\nExpertise : %s\nWorkload : %d/%d\nResearch Domain : %s\nHourly Stipend : Php%.2f",
                getName(), getExpertise(), getCurrentWorkload(), ((long) getMaxWorkload() + 3), researchDomain,
                hourlyStipend);
    }
}
