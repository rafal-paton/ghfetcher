package com.atiperagithub.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RepositoryResponseDto(String repositoryName, String ownerLogin, List<BranchWithShaDto> branches) {
}
