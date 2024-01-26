package com.dilly.gift;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Getter;

@Entity
@Getter
public class Music {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private String youtubeUrl;

    private Long sequence;

	@OneToMany(mappedBy = "music")
	private List<MusicHashtag> hashtags;
}
