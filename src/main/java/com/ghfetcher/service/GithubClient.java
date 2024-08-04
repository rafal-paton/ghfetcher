package com.ghfetcher.service;

import com.ghfetcher.dto.BranchWithCommitsDto;
import com.ghfetcher.dto.RepositoryDto;
import reactor.core.publisher.Flux;

interface GithubClient {

    Flux<BranchWithCommitsDto> makeRequestForBranches(final String userName, final String repoName);

    Flux<RepositoryDto> makeRequestForUserRepos(final String userName);
}