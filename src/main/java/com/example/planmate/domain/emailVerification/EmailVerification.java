package com.example.planmate.domain.emailVerification;

import java.io.Serializable;

import com.example.planmate.domain.emailVerification.enums.EmailVerificationPurpose;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
        if (inputCode == this.code && purpose.equals(this.purpose)) {
            this.verified = true;
            return true;
        }
        return false;
    }
}
