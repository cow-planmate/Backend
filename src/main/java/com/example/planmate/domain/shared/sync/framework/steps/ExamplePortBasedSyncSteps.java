package com.example.planmate.domain.shared.sync.framework.steps;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.sync.framework.TargetedSyncStep;
import com.example.planmate.domain.shared.sync.ports.TimeTableCommandPort;
import com.example.planmate.domain.shared.sync.ports.TimeTablePlaceBlockCommandPort;

import lombok.RequiredArgsConstructor;

// 참고용: 포트 기반으로 동작하는 예시 스텝. 기존 스텝과 공존 가능.
@Component
@Profile("ports-demo")
@RequiredArgsConstructor
class PortBasedSyncTimeTableStep implements TargetedSyncStep {
    private final TimeTableCommandPort timeTableCommandPort;

    @Override
    public void execute(PlanSyncContext ctx) {
        // 실제 아이템은 캐시에서 읽거나 상위 과정에서 전달받을 수 있음.
        // 여기서는 기존 캐시 기반 서비스를 어댑터가 재사용하므로, 요청 아이템은 생략하고 planId만 사용.
    timeTableCommandPort.upsert(new TimeTableCommandPort.UpsertRequest(ctx.getPlanId(), java.util.List.of()));
        // 결과를 요약 DTO로 컨텍스트에 담거나, 필요 시 그대로 보관
    // 예시: 결과를 다른 레이어로 전달하거나 projection 갱신 등에 사용할 수 있음.
    // 여기서는 데모 목적이라 컨텍스트에 기존 타입 충돌을 피하기 위해 아무것도 저장하지 않음.
    }
}

@Component
@Profile("ports-demo")
@RequiredArgsConstructor
class PortBasedSyncTimeTablePlaceBlockStep implements TargetedSyncStep {
    private final TimeTablePlaceBlockCommandPort blockPort;
    private final TimeTableCommandPort timeTablePort;

    @Override
    public void execute(PlanSyncContext ctx) {
        // 타임테이블 upsert 결과가 있다면 그 매핑을 사용해 블록을 upsert한다.
        var ttResult = timeTablePort.upsert(new TimeTableCommandPort.UpsertRequest(ctx.getPlanId(), java.util.List.of()));
        blockPort.upsert(new TimeTablePlaceBlockCommandPort.UpsertRequest(java.util.List.of()), ttResult.insertedIdMap());
    }
}
