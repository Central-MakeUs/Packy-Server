package com.dilly.gift.api;

import com.dilly.gift.application.GiftService;
import com.dilly.gift.dto.response.PhotoResponseDto.PhotoWithoutSequenceResponse;
import com.dilly.global.response.DataResponseDto;
import com.dilly.global.response.SliceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모아보기 관련 API")
@RestController
@RequestMapping("/api/v1/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @Operation(summary = "사진 모아보기")
    @Parameter(in = ParameterIn.QUERY,
        description = "한 페이지에 보여줄 사진 개수. 기본값은 6개",
        name = "size",
        schema = @Schema(type = "integer"))
    @GetMapping("/photos")
    public DataResponseDto<SliceResponseDto<PhotoWithoutSequenceResponse>> getPhotos(
        @PageableDefault(size = 6)
        @Parameter(hidden = true)
        Pageable pageable,
        @Schema(description = "마지막 사진의 id", type = "integer")
        @RequestParam(value = "last-photo-id", required = false)
        Long lastPhotoId
    ) {
        return DataResponseDto.from(
            SliceResponseDto.from(giftService.getPhotos(lastPhotoId, pageable)));
    }
}
