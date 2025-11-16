package com.example.planmate.move.shared.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.example.planmate.move.shared.framework.dto.CacheDto;
import com.example.planmate.move.shared.framework.dto.WRequest;
import com.example.planmate.move.shared.framework.dto.WResponse;
import com.example.planmate.move.shared.framework.repository.AutoCacheRepository;

/**
 * Template-style base service that encapsulates the common create/update/delete
 * workflow shared by the distributed shared services. Concrete services supply
 * request/response wiring and ID handling via the constructor arguments.
 */
public abstract class AbstractSharedCacheService<Req extends WRequest, Res extends WResponse, DTO extends CacheDto<ID>, ID>
        implements SharedService<Req, Res> {

    private final AutoCacheRepository<?, ID, DTO> cacheRepository;
    private final Function<Req, List<DTO>> requestExtractor;
    private final BiConsumer<Res, List<DTO>> responseWriter;
    private final Supplier<Res> responseFactory;
    private final Function<DTO, ID> idExtractor;
    private final UnaryOperator<DTO> createTransformer;
    private final UnaryOperator<DTO> updateTransformer;

    protected AbstractSharedCacheService(
            AutoCacheRepository<?, ID, DTO> cacheRepository,
            Function<Req, List<DTO>> requestExtractor,
            BiConsumer<Res, List<DTO>> responseWriter,
            Supplier<Res> responseFactory,
            Function<DTO, ID> idExtractor,
            UnaryOperator<DTO> createTransformer,
            UnaryOperator<DTO> updateTransformer) {
        this.cacheRepository = cacheRepository;
        this.requestExtractor = requestExtractor;
        this.responseWriter = responseWriter;
        this.responseFactory = responseFactory;
        this.idExtractor = idExtractor;
        this.createTransformer = createTransformer != null ? createTransformer : UnaryOperator.identity();
        this.updateTransformer = updateTransformer != null ? updateTransformer : UnaryOperator.identity();
    }

    @Override
    public Res read(Req request) {
        throw new UnsupportedOperationException("Read operation is not implemented for AbstractSharedCacheService. Override in subclasses if needed.");
    }

    @Override
    public Res create(Req request) {
        Res response = responseFactory.get();
        List<DTO> payload = safePayload(request);

        if (payload.isEmpty()) {
            responseWriter.accept(response, Collections.emptyList());
            return response;
        }

        List<DTO> prepared = payload.stream()
                .map(createTransformer)
                .collect(Collectors.toList());

        List<DTO> saved = cacheRepository.saveAll(prepared);
        responseWriter.accept(response, saved);
        return response;
    }

    @Override
    public Res update(Req request) {
        Res response = responseFactory.get();
        List<DTO> payload = safePayload(request);

        if (payload.isEmpty()) {
            responseWriter.accept(response, Collections.emptyList());
            return response;
        }

        List<DTO> updated = payload.stream()
                .map(updateTransformer)
                .map(cacheRepository::update)
                .collect(Collectors.toList());

        responseWriter.accept(response, updated);
        return response;
    }

    @Override
    public Res delete(Req request) {
        Res response = responseFactory.get();
        List<DTO> payload = safePayload(request);

        if (payload.isEmpty()) {
            responseWriter.accept(response, Collections.emptyList());
            return response;
        }

        List<ID> ids = payload.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!ids.isEmpty()) {
            cacheRepository.deleteAllById(ids);
        }

        responseWriter.accept(response, payload);
        return response;
    }

    private List<DTO> safePayload(Req request) {
        List<DTO> payload = requestExtractor.apply(request);
        return payload != null ? payload : Collections.emptyList();
    }
}
