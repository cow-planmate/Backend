package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class PendingRequestVO {
    private int requestId;
    private String senderId;
    private String senderNickname;
    private String planId;
    private String planName;
    private String type;
}
