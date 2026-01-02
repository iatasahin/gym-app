package dev.ilkersahin.java.spring.gym.model;

import java.util.Objects;

public abstract class User {
    private String firstName;
    private String lastname;
    private String userName;
    private String password;
    private boolean isActive;

    public User(String firstName, String lastname, String userName, String password, boolean isActive) {
        this.firstName = firstName;
        this.lastname = lastname;
        this.userName = userName;
        this.password = password;
        this.isActive = isActive;
    }

    public User() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstName='" + firstName + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName);
    }

}
