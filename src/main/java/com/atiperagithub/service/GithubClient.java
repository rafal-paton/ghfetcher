package com.atiperagithub.service;

import com.atiperagithub.dto.BranchWithCommitsDto;
import com.atiperagithub.dto.RepositoryDto;
import reactor.core.publisher.Flux;

public interface GithubClient {

    Flux<BranchWithCommitsDto> makeRequestForBranches(final String userName, final String repoName);

    Flux<RepositoryDto> makeRequestForUserRepos(final String userName);
}
