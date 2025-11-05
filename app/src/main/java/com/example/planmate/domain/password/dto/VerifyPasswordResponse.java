package com.example.planmate.domain.password.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordResponse extends CommonResponse {
    boolean passwordVerified;
}
