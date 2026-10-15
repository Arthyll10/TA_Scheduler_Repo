public abstract class Person {
    private String id, name, email;

    public Person(String id, String name, String email) {
        this.id = requireText(id, "ID");
        setName(name);
        setEmail(email);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = requireText(name, "Name");
    }

    public void setEmail(String email) {
        this.email = requireText(email, "Email");
    }

    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(field + " cannot contain | or line breaks.");
        }
        return value.trim();
    }

    public abstract String getRoleDetails();
}
