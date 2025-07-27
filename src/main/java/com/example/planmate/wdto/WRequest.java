package com.example.planmate.wdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class WRequest {
    private String type;
    private String object;
}
