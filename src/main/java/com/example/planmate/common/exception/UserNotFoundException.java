package com.example.planmate.common.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("존재하지 않는 유저 입니다.");
    }
}
