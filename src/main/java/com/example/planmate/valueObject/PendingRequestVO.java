package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class PendingRequestVO {
    private int requestId;
    private int senderId;
    private String senderNickname;
    private int planId;
    private String planName;
    private String type;
}
