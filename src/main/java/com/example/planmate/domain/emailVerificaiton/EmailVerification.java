package com.example.planmate.domain.emailVerificaiton;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmailVerification implements Serializable {

    private String email;
    private EmailVerificationPurpose purpose;
    private int code;
    private boolean verified;

    public EmailVerification(String email, EmailVerificationPurpose purpose, int code) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
        this.verified = false;
    }
    public boolean verify(EmailVerificationPurpose purpose, int inputCode) {
        if(inputCode == this.code && purpose.equals(this.purpose)) {
            this.verified = true;
            return true;
        }
        return false;
    }
}
