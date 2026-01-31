package com.example.planmate.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextPageTokenDTO {
    private String token; // "travelId:categoryId:themeId_or_query" 형태의 완성된 키
    private Integer page;
}
