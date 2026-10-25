package com.project.back_end.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

// Login request body. "identifier" is the email for Doctor/Patient and the username for Admin.
public class Login {

    // The frontend sends this field as "email", so accept both names.
    @JsonAlias("email")
    private String identifier;
    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
