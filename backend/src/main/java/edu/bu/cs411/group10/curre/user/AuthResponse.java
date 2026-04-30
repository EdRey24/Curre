package edu.bu.cs411.group10.curre.user;

public class AuthResponse {

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String message;

    public AuthResponse() {}

    public AuthResponse(Long userId, String email, String firstName, String lastName, String message) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
