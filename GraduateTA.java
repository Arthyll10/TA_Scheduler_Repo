public class GraduateTA extends TeachingAssistant {
    private String researchDomain;
    private double hourlyStipend;

    public GraduateTA(String id, String name, String email, String expertise,
            int maxWorkload, String researchDomain, double hourlyStipend) {
        super(id, name, email, expertise, maxWorkload);
        setResearchDomain(researchDomain);
        setHourlyStipend(hourlyStipend);
    }

    public String getResearchDomain() {
        return researchDomain;
    }

    public double getHourlyStipend() {
        return hourlyStipend;
    }

    public void setResearchDomain(String domain) {
        researchDomain = Person.requireText(domain, "Research domain");
    }

    public void setHourlyStipend(double stipend) {
        if (!Double.isFinite(stipend) || stipend < 0) {
            throw new IllegalArgumentException("Stipend must be a finite number of zero or more.");
        }
        hourlyStipend = stipend;
    }

    @Override
    public long getWorkloadLimit() {
        return (long) getMaxWorkload() + 3;
    }

    @Override
    public boolean canAcceptAssignment(int hours, String requiredExpertise) {
        // The parent check uses this graduate TA's overridden workload limit.
        return super.canAcceptAssignment(hours, requiredExpertise);
    }

    @Override
    public String getRoleDetails() {
        return String.format(
                "Role               : Graduate Teaching Assistant%nName               : %s%n"
                        + "Expertise          : %s%nWeekly workload    : %d/%d hours%nAvailable          : %s%n"
                        + "Research domain    : %s%nHourly stipend     : PHP %.2f/hr",
                getName(), getExpertise(), getCurrentWorkload(), getWorkloadLimit(),
                isAvailable() ? "Yes" : "No", researchDomain, hourlyStipend);
    }
}
