public class Admin extends Person {
    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRoleDetails() {
        return "Role               : Admin\nName               : " + getName()
                + "\nResponsibilities   : Manage records, approve requests, and view reports.";
    }
}
