package com.sharedsync.framework.shared.service;

import com.sharedsync.framework.shared.framework.dto.WRequest;
import com.sharedsync.framework.shared.framework.dto.WResponse;

public interface SharedService<Req extends WRequest, Res extends WResponse> {

    public Res create(Req request);
    public Res read(Req request);
    public Res update(Req request);
    public Res delete(Req request);
}
