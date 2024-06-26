package com.dilly.gift.domain.giftbox;

import com.dilly.gift.domain.Box;
import com.dilly.gift.domain.Photo;
import com.dilly.gift.domain.gift.Gift;
import com.dilly.gift.domain.letter.Letter;
import com.dilly.gift.domain.receiver.Receiver;
import com.dilly.gift.domain.sticker.GiftBoxSticker;
import com.dilly.global.domain.BaseTimeEntity;
import com.dilly.member.domain.Member;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftBox extends BaseTimeEntity {

    @Id
    @Tsid
    private Long id;

    private String uuid;

    private String name;

    private String senderName;

    private String receiverName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    private Box box;

    @OneToOne(fetch = FetchType.LAZY)
    private Letter letter;

    @Builder.Default
    @OneToMany(mappedBy = "giftBox")
    private List<Photo> photos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "giftBox")
    private List<GiftBoxSticker> giftBoxStickers = new ArrayList<>();

    private String youtubeUrl;

    // TODO: Optional로 수정
    @Embedded
    private Gift gift;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GiftBoxType giftBoxType = GiftBoxType.PRIVATE;

    @Builder.Default
    private Boolean senderDeleted = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliverStatus deliverStatus = DeliverStatus.WAITING;

    @Builder.Default
    @OneToMany(mappedBy = "giftBox")
    private List<Receiver> receivers = new ArrayList<>();

    public void delete() {
        this.senderDeleted = true;
    }

    public void updateDeliverStatus(DeliverStatus deliverStatus) {
        this.deliverStatus = deliverStatus;
    }
}
