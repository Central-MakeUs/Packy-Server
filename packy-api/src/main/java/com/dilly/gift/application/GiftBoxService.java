package com.dilly.gift.application;

import com.dilly.exception.ErrorCode;
import com.dilly.exception.GiftBoxAccessDeniedException;
import com.dilly.exception.GiftBoxAlreadyOpenedException;
import com.dilly.exception.UnsupportedException;
import com.dilly.gift.adaptor.BoxReader;
import com.dilly.gift.adaptor.EnvelopeReader;
import com.dilly.gift.adaptor.GiftBoxReader;
import com.dilly.gift.adaptor.GiftBoxStickerReader;
import com.dilly.gift.adaptor.GiftBoxStickerWriter;
import com.dilly.gift.adaptor.GiftBoxWriter;
import com.dilly.gift.adaptor.LetterWriter;
import com.dilly.gift.adaptor.PhotoReader;
import com.dilly.gift.adaptor.PhotoWriter;
import com.dilly.gift.adaptor.ReceiverReader;
import com.dilly.gift.adaptor.ReceiverWriter;
import com.dilly.gift.domain.Box;
import com.dilly.gift.domain.letter.Envelope;
import com.dilly.gift.domain.gift.Gift;
import com.dilly.gift.domain.giftbox.GiftBox;
import com.dilly.gift.domain.giftbox.GiftBoxRole;
import com.dilly.gift.domain.gift.GiftType;
import com.dilly.gift.domain.letter.Letter;
import com.dilly.gift.domain.receiver.Receiver;
import com.dilly.gift.domain.receiver.ReceiverStatus;
import com.dilly.gift.dto.request.GiftBoxRequest;
import com.dilly.gift.dto.response.BoxResponse;
import com.dilly.gift.dto.response.EnvelopeResponse;
import com.dilly.gift.dto.response.GiftBoxIdResponse;
import com.dilly.gift.dto.response.GiftBoxResponse;
import com.dilly.gift.dto.response.GiftBoxesResponse;
import com.dilly.gift.dto.response.GiftResponseDto.GiftResponse;
import com.dilly.gift.dto.response.PhotoResponseDto.PhotoResponse;
import com.dilly.gift.dto.response.StickerResponse;
import com.dilly.global.utils.SecurityUtil;
import com.dilly.member.adaptor.MemberReader;
import com.dilly.member.domain.Member;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GiftBoxService {

    private final GiftBoxReader giftBoxReader;
    private final GiftBoxWriter giftBoxWriter;
    private final BoxReader boxReader;
    private final EnvelopeReader envelopeReader;
    private final LetterWriter letterWriter;
    private final PhotoReader photoReader;
    private final PhotoWriter photoWriter;
    private final GiftBoxStickerReader giftBoxStickerReader;
    private final GiftBoxStickerWriter giftBoxStickerWriter;
    private final MemberReader memberReader;
    private final ReceiverReader receiverReader;
    private final ReceiverWriter receiverWriter;

    public GiftBoxIdResponse createGiftBox(GiftBoxRequest giftBoxRequest) {
        Long memberId = SecurityUtil.getMemberId();
        Member sender = memberReader.findById(memberId);

        Box box = boxReader.findById(giftBoxRequest.boxId());
        Envelope envelope = envelopeReader.findById(giftBoxRequest.envelopeId());
        Letter letter = letterWriter.save(giftBoxRequest.letterContent(), envelope);

        GiftBox giftBox;
        if (giftBoxRequest.gift() == null) {
            giftBox = giftBoxWriter.save(box, letter, sender,
                giftBoxRequest.name(), giftBoxRequest.youtubeUrl(),
                giftBoxRequest.senderName(), giftBoxRequest.receiverName());
        } else {
            Gift gift = Gift.of(GiftType.valueOf(giftBoxRequest.gift().type().toUpperCase()),
                giftBoxRequest.gift().url());

            giftBox = giftBoxWriter.save(box, letter, gift, sender,
                giftBoxRequest.name(), giftBoxRequest.youtubeUrl(),
                giftBoxRequest.senderName(), giftBoxRequest.receiverName()
            );
        }

        giftBoxRequest.photos()
            .forEach(
                photoRequest -> photoWriter.save(giftBox, photoRequest.photoUrl(),
                    photoRequest.description(), photoRequest.sequence())
            );
        giftBoxRequest.stickers()
            .forEach(stickerRequest ->
                giftBoxStickerWriter.save(giftBox, stickerRequest.id(), stickerRequest.location())
            );

        return GiftBoxIdResponse.of(giftBox.getId(), giftBox.getUuid(),
            giftBox.getBox().getKakaoMessageImgUrl());
    }

    void checkIfGiftBoxOpenable(Member member, GiftBox giftBox) {
        if (giftBox.getSender().equals(member)) {
            if (giftBox.getSenderDeleted().equals(true)) {
                throw new GiftBoxAccessDeniedException();
            }
        } else {
            List<Long> receivers = receiverReader.findByGiftBox(giftBox).stream()
                .map(Receiver::getMember)
                .map(Member::getId)
                .toList();

            if (receivers.isEmpty() && giftBox.getSenderDeleted().equals(false)) {
                receiverWriter.save(member, giftBox);
                return;
            }

            if (receivers.contains(member.getId())) {
                Receiver receiver = receiverReader.findByMemberAndGiftBox(member, giftBox);
                if (receiver.getStatus().equals(ReceiverStatus.DELETED)) {
                    throw new GiftBoxAccessDeniedException();
                }
            } else {
                throw new GiftBoxAlreadyOpenedException();
            }
        }
    }

    public GiftBoxResponse openGiftBox(Long giftBoxId) {
        Long memberId = SecurityUtil.getMemberId();
        Member member = memberReader.findById(memberId);
        GiftBox giftBox = giftBoxReader.findById(giftBoxId);

        checkIfGiftBoxOpenable(member, giftBox);

        BoxResponse boxResponse = BoxResponse.of(giftBox.getBox());
        EnvelopeResponse envelopeResponse = EnvelopeResponse.of(giftBox.getLetter().getEnvelope());
        List<PhotoResponse> photos = photoReader.findAllByGiftBox(giftBox).stream()
            .map(PhotoResponse::from)
            .sorted(Comparator.comparingInt(PhotoResponse::sequence))
            .toList();
        List<StickerResponse> stickers = giftBoxStickerReader.findAllByGiftBox(giftBox).stream()
            .map(StickerResponse::of)
            .sorted(Comparator.comparingInt(StickerResponse::location))
            .toList();

        GiftResponse giftResponse = null;
        if (giftBox.getGift() != null) {
            giftResponse = GiftResponse.from(giftBox.getGift());
        }

        return GiftBoxResponse.of(giftBox, boxResponse, envelopeResponse, photos, stickers,
            giftResponse);
    }

    // TODO: 성능 개선 필요
    public Slice<GiftBoxesResponse> getGiftBoxes(LocalDateTime lastGiftBoxDate, String type,
        Pageable pageable) {
        Long memberId = SecurityUtil.getMemberId();
        Member member = memberReader.findById(memberId);

        if (type == null) {
            type = "all";
        }

        Slice<GiftBox> giftBoxSlice;
        List<GiftBoxesResponse> giftBoxesResponses = new ArrayList<>();
        switch (type) {
            case "sent" -> {
                giftBoxSlice = giftBoxReader.searchSentGiftBoxesBySlice(member,
                    lastGiftBoxDate, pageable);
                giftBoxesResponses = sliceToDto(giftBoxSlice, type, member);
            }
            case "received" -> {
                giftBoxSlice = giftBoxReader.searchReceivedGiftBoxesBySlice(member,
                    lastGiftBoxDate, pageable);
                giftBoxesResponses = sliceToDto(giftBoxSlice, type, member);
            }
            case "all" -> {
                Comparator<Object> comparator = getCreatedAtComparator(member);
                giftBoxSlice = giftBoxReader.searchAllGiftBoxesBySlice(member,
                    lastGiftBoxDate, comparator, pageable);
                giftBoxesResponses = sliceToDto(giftBoxSlice, type, member);
            }
            default -> giftBoxSlice = new SliceImpl<>(List.of(), pageable, false);
        }

        return new SliceImpl<>(giftBoxesResponses, pageable, giftBoxSlice.hasNext());
    }

    private List<GiftBoxesResponse> sliceToDto(Slice<GiftBox> giftBoxSlice, String type,
        Member member) {
        switch (type) {
            case "sent" -> {
                return giftBoxSlice.getContent().stream()
                    .map(GiftBoxesResponse::of)
                    .toList();
            }
            case "received" -> {
                return giftBoxSlice.getContent().stream()
                    .map(giftBox -> {
                        Receiver receiver = receiverReader.findByMemberAndGiftBox(member, giftBox);
                        return GiftBoxesResponse.of(receiver);
                    })
                    .toList();
            }
            case "all" -> {
                return giftBoxSlice.getContent().stream()
                    .map(giftBox -> {
                        Receiver receiver = receiverReader.findByMemberAndGiftBox(member, giftBox);
                        return GiftBoxesResponse.of(giftBox, member, receiver);
                    })
                    .toList();
            }
            default -> throw new UnsupportedException(ErrorCode.UNSUPPORTED_GIFTBOX_TYPE);
        }
    }

    private Comparator<Object> getCreatedAtComparator(Member member) {
        return Comparator.comparing((Object obj) -> {
            GiftBox giftBox = (GiftBox) obj;
            LocalDateTime createdAt;

            if (giftBox.getSender().equals(member)) {
                createdAt = giftBox.getCreatedAt();
            } else {
                Receiver receiver = receiverReader.findByMemberAndGiftBox(member, giftBox);
                createdAt = receiver.getCreatedAt();
            }

            return createdAt;
        });
    }

    public String deleteGiftBox(Long giftBoxId) {
        Long memberId = SecurityUtil.getMemberId();
        Member member = memberReader.findById(memberId);

        GiftBox giftBox = giftBoxReader.findById(giftBoxId);
        GiftBoxRole role = getGiftBoxRole(member, giftBox);

        if (role.equals(GiftBoxRole.SENDER)) {
            giftBox.delete();
        } else if (role.equals(GiftBoxRole.RECEIVER)) {
            Receiver receiver = receiverReader.findByMemberAndGiftBox(member, giftBox);
            receiver.delete();
        }

        return "선물박스가 삭제되었습니다";
    }

    private GiftBoxRole getGiftBoxRole(Member member, GiftBox giftBox) {
        List<Member> receivers = receiverReader.findByGiftBox(giftBox).stream()
            .map(Receiver::getMember)
            .toList();

        if (giftBox.getSender().equals(member)) {
            return GiftBoxRole.SENDER;
        } else if (receivers.contains(member)) { // 받은 사람일 경우
            return GiftBoxRole.RECEIVER;
        } else {
            throw new GiftBoxAccessDeniedException();
        }
    }
}
