package com.ghfetcher.service;

import com.ghfetcher.dto.BranchWithShaDto;
import com.ghfetcher.dto.RepositoryDto;
import com.ghfetcher.dto.RepositoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubClient githubClient;

    public Flux<RepositoryResponseDto> fetchUserRepositoriesWithBranches(final String userName) {
        return githubClient.makeRequestForUserRepos(userName)
                .filter(repository -> !repository.fork())
                .flatMap(repository -> createRepositoryResponseDto(userName, repository));
    }

    Mono<RepositoryResponseDto> createRepositoryResponseDto(final String userName, final RepositoryDto repo) {
        return fetchBranches(userName, repo.name())
                .collectList()
                .map(branches -> RepositoryResponseDto.builder()
                        .ownerLogin(userName)
                        .repositoryName(repo.name())
                        .branches(branches)
                        .build());
    }

    Flux<BranchWithShaDto> fetchBranches(final String userName, final String repoName) {
        return githubClient.makeRequestForBranches(userName, repoName)
                .map(branchDto -> new BranchWithShaDto(branchDto.name(), branchDto.commit().sha()));
    }
}