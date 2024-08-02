package com.atiperagithub.service;

import com.atiperagithub.dto.BranchWithCommitsDto;
import com.atiperagithub.dto.BranchWithShaDto;
import com.atiperagithub.dto.CommitDto;
import com.atiperagithub.dto.RepositoryDto;
import com.atiperagithub.dto.RepositoryResponseDto;
import com.atiperagithub.error.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubServiceTest {

    @Mock
    private GithubClient githubClient;

    @InjectMocks
    private GithubService githubService;

    private static final String TEST_USER = "testUser";
    private static final String TEST_REPO = "testRepo";
    private static final String NON_EXISTENT_USER = "nonExistentUser";

    @Test
    void should_map_branch_with_commitDto_to_branch_with_sha_correctly() {
        // Given
        BranchWithCommitsDto branchDto1 = new BranchWithCommitsDto("main", new CommitDto("sha123"));
        BranchWithCommitsDto branchDto2 = new BranchWithCommitsDto("develop", new CommitDto("sha456"));

        when(githubClient.makeRequestForBranches(TEST_USER, TEST_REPO))
                .thenReturn(Flux.just(branchDto1, branchDto2));

        // When
        Flux<BranchWithShaDto> result = githubService.fetchBranches(TEST_USER, TEST_REPO);

        // Then
        StepVerifier.create(result)
                .expectNext(new BranchWithShaDto("main", "sha123"))
                .expectNext(new BranchWithShaDto("develop", "sha456"))
                .verifyComplete();
    }

    @Test
    void should_throw_user_not_found_exception_when_non_existent_user_was_received() {
        // Given
        when(githubClient.makeRequestForUserRepos(NON_EXISTENT_USER))
                .thenReturn(Flux.error(new UserNotFoundException("User not found")));

        // When
        Flux<RepositoryResponseDto> result = githubService.fetchUserRepositoriesWithBranches(NON_EXISTENT_USER);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found")
                )
                .verify();
    }








    @Test
    void should_fetch_user_repositories_with_branches_and_filter_forks() {
        // Given
        RepositoryDto nonForkRepo = new RepositoryDto("testUser", false);
        RepositoryDto forkRepo = new RepositoryDto("forkRepo", true);

        when(githubClient.makeRequestForUserRepos("testUser"))
                .thenReturn(Flux.just(nonForkRepo, forkRepo));

        BranchWithCommitsDto branchDto = new BranchWithCommitsDto("main", new CommitDto("sha123"));
        when(githubClient.makeRequestForBranches(anyString(), anyString()))
                .thenReturn(Flux.just(branchDto));

        // When
        Flux<RepositoryResponseDto> result = githubService.fetchUserRepositoriesWithBranches(TEST_USER);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(repo ->
                        repo.repositoryName().equals(TEST_REPO) &&
                                repo.ownerLogin().equals(TEST_USER) &&
                                repo.branches().size() == 1 &&
                                repo.branches().get(0).name().equals("main") &&
                                repo.branches().get(0).sha().equals("sha123")
                )
                .verifyComplete();
    }

    @Test
    void should_create_RepositoryResponseDto_correctly() {
        // Given
        RepositoryDto repoDto = new RepositoryDto(TEST_USER, false);
        BranchWithCommitsDto branchDto = new BranchWithCommitsDto("main", new CommitDto("sha123"));

        when(githubClient.makeRequestForBranches(TEST_USER, TEST_REPO))
                .thenReturn(Flux.just(branchDto));

        // When
        Mono<RepositoryResponseDto> result = githubService.createRepositoryResponseDto(TEST_USER, repoDto);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(repo ->
                        repo.repositoryName().equals(TEST_REPO) &&
                                repo.ownerLogin().equals(TEST_USER) &&
                                repo.branches().size() == 1 &&
                                repo.branches().get(0).name().equals("main") &&
                                repo.branches().get(0).sha().equals("sha123")
                )
                .verifyComplete();
    }



}