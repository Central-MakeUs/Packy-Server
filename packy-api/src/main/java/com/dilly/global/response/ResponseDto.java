package com.dilly.global.response;

import com.dilly.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResponseDto {

	private final String code;
	private final String message;

	public static ResponseDto of(ErrorCode errorCode) {
		return new ResponseDto(errorCode.toString(), errorCode.getMessage());
	}
}
