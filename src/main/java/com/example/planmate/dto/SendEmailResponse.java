package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailResponse extends CommonResponse{
    boolean isVerificationSent;
}
