package com.ghfetcher.error;

public record WrongHeaderResponseDto(Integer status, String message) {
}