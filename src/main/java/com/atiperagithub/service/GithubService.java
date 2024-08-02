package com.atiperagithub.service;

import com.atiperagithub.dto.BranchWithShaDto;
import com.atiperagithub.dto.RepositoryDto;
import com.atiperagithub.dto.RepositoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubClient githubClient;

    public Flux<RepositoryResponseDto> fetchUserRepositoriesWithBranches(final String userName) {
        return githubClient.makeRequestForUserRepos(userName)
                .filter(repository -> !repository.fork())
                .flatMap(repository -> createRepositoryResponseDto(userName, repository));
    }

    public Mono<RepositoryResponseDto> createRepositoryResponseDto(final String userName, final RepositoryDto repo) {
        return fetchBranches(userName, repo.name())
                .collectList()
                .map(branches -> RepositoryResponseDto.builder()
                        .ownerLogin(userName)
                        .repositoryName(repo.name())
                        .branches(branches)
                        .build());
    }

    public Flux<BranchWithShaDto> fetchBranches(final String userName, final String repoName) {
        return githubClient.makeRequestForBranches(userName, repoName)
                .map(branchDto -> new BranchWithShaDto(branchDto.name(), branchDto.commit().sha()));
    }
}