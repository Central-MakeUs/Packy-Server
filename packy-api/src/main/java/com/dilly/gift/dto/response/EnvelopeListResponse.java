package com.dilly.gift.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EnvelopeListResponse(
    @Schema(example = "1")
    Long id,
    @Schema(example = "1")
    Long sequence,
    @Schema(example = "ED76A5")
    String borderColorCode,
    @Schema(example = "www.example.com")
    String imgUrl
) {

}