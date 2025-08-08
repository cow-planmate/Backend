package com.example.planmate.collaborationRequest.entity;
public enum CollaborationRequestStatus {
    PENDING,
    APPROVED,
    DENIED,
    ACCEPTED,
    DECLINED;

    public static CollaborationRequestStatus getAcceptedStatus(CollaborationRequestType type) {
        return switch (type) {
            case INVITE -> ACCEPTED;
            case REQUEST -> APPROVED;
        };
    }

    public static CollaborationRequestStatus getRejectedStatus(CollaborationRequestType type) {
        return switch (type) {
            case INVITE -> DECLINED;
            case REQUEST -> DENIED;
        };
    }
}