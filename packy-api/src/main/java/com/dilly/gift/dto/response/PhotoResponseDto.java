package com.dilly.gift.dto.response;

import com.dilly.gift.domain.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class PhotoResponseDto {

    @Builder
    public record PhotoResponse(
        @Schema(example = "www.example.com")
        String photoUrl,
        @Schema(example = "우리 같이 트리 만든 날 :)")
        String description,
        @Schema(example = "1")
        Integer sequence
    ) {

        public static PhotoResponse from(Photo photo) {
            return PhotoResponse.builder()
                .photoUrl(photo.getImgUrl())
                .description(photo.getDescription())
                .sequence(photo.getSequence())
                .build();
        }
    }

    @Builder
    public record PhotoWithoutSequenceResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "www.example.com")
        String photoUrl,
        @Schema(example = "우리 같이 트리 만든 날 :)")
        String description
    ) {

        public static PhotoWithoutSequenceResponse from(Photo photo) {
            return PhotoWithoutSequenceResponse.builder()
                .id(photo.getId())
                .photoUrl(photo.getImgUrl())
                .description(photo.getDescription())
                .build();
        }
    }
}
