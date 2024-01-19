package com.dilly.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.dilly.gift.MemberGiftBoxRepository;
import com.dilly.gift.application.GiftService;
import com.dilly.gift.dao.BoxRepository;
import com.dilly.gift.dao.GiftBoxRepository;
import com.dilly.gift.dao.LetterRepository;
import com.dilly.gift.dao.MessageRepository;
import com.dilly.gift.dao.PhotoRepository;

import jakarta.transaction.Transactional;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = "spring.profiles.active=test"
)
@TestPropertySource(locations = {"classpath:application-test.yml", "classpath:data.sql"})
@Transactional
public abstract class IntegrationTestSupport {

	@LocalServerPort
	protected int port;

	@Autowired
	protected GiftService giftService;

	@Autowired
	protected BoxRepository boxRepository;

	@Autowired
	protected MessageRepository messageRepository;

	@Autowired
	protected LetterRepository letterRepository;

	@Autowired
	protected PhotoRepository photoRepository;

	@Autowired
	protected GiftBoxRepository giftBoxRepository;

	@Autowired
	protected MemberGiftBoxRepository memberGiftBoxRepository;
}