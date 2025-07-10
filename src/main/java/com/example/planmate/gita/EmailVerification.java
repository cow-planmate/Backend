package com.example.planmate.gita;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmailVerification implements Serializable {

    private String email;
    private int code;
    private boolean verified;

    public EmailVerification(String email, int code) {
        this.email = email;
        this.code = code;
        this.verified = false;
    }
    public boolean verify(int inputCode) {
        if(inputCode == this.code) {
            this.verified = true;
            return true;
        }
        return false;
    }
}
