package com.dilly.gift.dto.response;

import com.dilly.gift.domain.gift.Gift;
import com.dilly.gift.domain.giftbox.GiftBox;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class GiftResponseDto {

    @Builder
    public record GiftResponse(
        @Schema(example = "photo")
        String type,
        @Schema(example = "www.example.com")
        String url
    ) {

        public static GiftResponse from(Gift gift) {
            return GiftResponse.builder()
                .type(String.valueOf(gift.getGiftType()).toLowerCase())
                .url(gift.getGiftUrl())
                .build();
        }
    }

    @Builder
    public record ItemResponse(
        @Schema(example = "1")
        Long giftBoxId,
        GiftResponse gift
    ) {

            public static ItemResponse from(GiftBox giftBox) {
                return ItemResponse.builder()
                    .giftBoxId(giftBox.getId())
                    .gift(GiftResponse.from(giftBox.getGift()))
                    .build();
            }
    }
}
