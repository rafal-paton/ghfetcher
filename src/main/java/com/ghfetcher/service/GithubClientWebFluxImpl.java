package com.ghfetcher.service;

import com.ghfetcher.dto.BranchWithCommitsDto;
import com.ghfetcher.dto.RepositoryDto;
import com.ghfetcher.error.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
class GithubClientWebFluxImpl implements GithubClient {

    private final WebClient webClient;

    @Override
    public Flux<RepositoryDto> makeRequestForUserRepos(final String userName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{userName}/repos")
                        .build(userName))
                .retrieve()
                .bodyToFlux(RepositoryDto.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Flux.error(new UserNotFoundException("User not found"));
                    }
                    return Flux.error(ex);
                });
    }

    @Override
    public Flux<BranchWithCommitsDto> makeRequestForBranches(final String userName, final String repoName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{userName}/{repoName}/branches")
                        .build(userName, repoName))
                .retrieve()
                .bodyToFlux(BranchWithCommitsDto.class);
    }
}