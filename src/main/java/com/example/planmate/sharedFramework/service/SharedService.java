package com.example.planmate.sharedFramework.service;

import com.example.planmate.domain.webSocket.dto.WRequest;
import com.example.planmate.domain.webSocket.dto.WResponse;

/**
 * SharedService
 *
 * A small framework-style base class for realtime CRUD-style operations.
 * Extend this class with concrete Request/Response types and implement the
 * four template methods (onCreate/onRead/onUpdate/onDelete).
 *
 * Usage:
 *   class MyPlanService extends SharedService<MyReq, MyRes> {
 *       @Override protected MyRes onUpdate(MyReq req) { return res; }
 *       // implement other hooks
 *   }
 */
public abstract class SharedService<Req extends WRequest, Res extends WResponse> {

    /** CRUD operation kinds for hook context */
    public enum Operation { CREATE, READ, UPDATE, DELETE }

    // ==== Public API (final) - calls into template methods ====

    public final Res create(Req request) {
        ensureNotNull(request);
        preValidate(Operation.CREATE, request);
        Res response = onCreate(request);
        postProcess(Operation.CREATE, request, response);
        return response;
    }

    public final Res read(Req request) {
        ensureNotNull(request);
        preValidate(Operation.READ, request);
        Res response = onRead(request);
        postProcess(Operation.READ, request, response);
        return response;
    }

    public final Res update(Req request) {
        ensureNotNull(request);
        preValidate(Operation.UPDATE, request);
        Res response = onUpdate(request);
        postProcess(Operation.UPDATE, request, response);
        return response;
    }

    public final Res delete(Req request) {
        ensureNotNull(request);
        preValidate(Operation.DELETE, request);
        Res response = onDelete(request);
        postProcess(Operation.DELETE, request, response);
        return response;
    }

    // ==== Template methods to implement in subclasses ====

    protected abstract Res onCreate(Req request);
    protected abstract Res onRead(Req request);
    protected abstract Res onUpdate(Req request);
    protected abstract Res onDelete(Req request);

    // ==== Optional hooks (no-op by default) ====

    /** Pre-validation or authorization hook per operation */
    protected void preValidate(Operation op, Req request) { /* no-op */ }

    /** Post-processing hook (logging, broadcasting, etc.) */
    protected void postProcess(Operation op, Req request, Res response) { /* no-op */ }

    // ==== Utilities ====

    protected void ensureNotNull(Req request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
    }
}
