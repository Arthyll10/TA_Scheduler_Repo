public abstract class Person {
    private String id, name, email;

    public Person(String id, String name, String email) {
        this.id = checkText(id);
        this.name = checkText(name);
        this.email = checkText(email);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setId(String id) {
        this.id = checkText(id);
    }

    public void setName(String name) {
        this.name = checkText(name);
    }

    public void setEmail(String email) {
        this.email = checkText(email);
    }

    public static String checkText(String text) {
        if (text == null || text.trim().isEmpty() || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException("Text fields must be nonblank and on one line.");
        }
        return text.trim();
    }

    public abstract String getRoleDetails();

}