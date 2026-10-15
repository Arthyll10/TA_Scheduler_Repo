public class Faculty extends Person {
    private String department;

    public Faculty(String id, String name, String email, String department) {
        super(id, name, email);
        setDepartment(department);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = Person.requireText(department, "Department");
    }

    @Override
    public String getRoleDetails() {
        return String.format("Role               : Faculty%nName               : %s%nDepartment         : %s",
                getName(), department);
    }
}
