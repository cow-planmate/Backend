package com.example.planmate.domain.shared.service;

import com.example.planmate.domain.shared.framework.dto.WRequest;
import com.example.planmate.domain.shared.framework.dto.WResponse;

public interface SharedService<Req extends WRequest, Res extends WResponse> {

    public Res create(Req request);
    public Res read(Req request);
    public Res update(Req request);
    public Res delete(Req request);
}
