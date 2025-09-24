package com.example.planmate.domain.shared.service.sharedService;

import com.example.planmate.domain.shared.dto.WRequest;
import com.example.planmate.domain.shared.dto.WResponse;

public interface SharedService<Req extends WRequest, Res extends WResponse> {

    public Res create(Req request);
    public Res update(Req request);
    public Res delete(Req request);
}
