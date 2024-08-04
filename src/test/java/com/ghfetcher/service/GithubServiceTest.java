package com.ghfetcher.service;

import com.ghfetcher.dto.BranchWithCommitsDto;
import com.ghfetcher.dto.BranchWithShaDto;
import com.ghfetcher.dto.CommitDto;
import com.ghfetcher.dto.RepositoryDto;
import com.ghfetcher.dto.RepositoryResponseDto;
import com.ghfetcher.error.UserNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThat;

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
                .assertNext(branch -> {
                    assertThat(branch.name()).isEqualTo("main");
                    assertThat(branch.sha()).isEqualTo("sha123");
                })
                .assertNext(branch -> {
                    assertThat(branch.name()).isEqualTo("develop");
                    assertThat(branch.sha()).isEqualTo("sha456");
                })
                .verifyComplete();
    }

    @Test
    void should_create_RepositoryResponseDto_correctly() {
        // Given
        RepositoryDto repo = new RepositoryDto(TEST_REPO, false);

        BranchWithCommitsDto branchDto1 = new BranchWithCommitsDto("main", new CommitDto("sha123"));
        BranchWithCommitsDto branchDto2 = new BranchWithCommitsDto("dev", new CommitDto("sha456"));

        when(githubClient.makeRequestForBranches(anyString(), anyString()))
                .thenReturn(Flux.just(branchDto1, branchDto2));

        // When
        Mono<RepositoryResponseDto> result = githubService.createRepositoryResponseDto(TEST_USER, repo);

        //Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.ownerLogin()).isEqualTo(TEST_USER);
                    assertThat(response.repositoryName()).isEqualTo(TEST_REPO);
                    assertThat(response.branches()).hasSize(2);
                    assertThat(response.branches().get(0).name()).isEqualTo("main");
                    assertThat(response.branches().get(0).sha()).isEqualTo("sha123");
                    assertThat(response.branches().get(1).name()).isEqualTo("dev");
                    assertThat(response.branches().get(1).sha()).isEqualTo("sha456");
                })
                .verifyComplete();
    }

    @Test
    void should_fetch_user_repositories_with_branches_and_filter_forks() {
        // Given
        RepositoryDto nonForkRepo = new RepositoryDto("nonForkRepo", false);
        RepositoryDto forkRepo = new RepositoryDto("forkRepo", true);

        when(githubClient.makeRequestForUserRepos(TEST_USER))
                .thenReturn(Flux.just(nonForkRepo, forkRepo));

        BranchWithCommitsDto branchDto = new BranchWithCommitsDto("main", new CommitDto("sha123"));
        when(githubClient.makeRequestForBranches(anyString(), anyString()))
                .thenReturn(Flux.just(branchDto));

        // When
        Flux<RepositoryResponseDto> result = githubService.fetchUserRepositoriesWithBranches(TEST_USER);

        // Then
        StepVerifier.create(result)
                .assertNext(repo -> {
                    assertThat(repo.repositoryName()).isEqualTo("nonForkRepo");
                    assertThat(repo.ownerLogin()).isEqualTo(TEST_USER);
                    assertThat(repo.branches()).hasSize(1);
                    assertThat(repo.branches().get(0).name()).isEqualTo("main");
                    assertThat(repo.branches().get(0).sha()).isEqualTo("sha123");
                })
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
}