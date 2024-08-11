package com.ghfetcher.controller;

import com.ghfetcher.dto.RepositoryResponseDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubRestControllerTest {

    private final WebTestClient webTestClient;
    private final WireMockServer wireMockServer;

    @Autowired
    public GithubRestControllerTest(WebTestClient webTestClient, WireMockServer wireMockServer) {
        this.webTestClient = webTestClient;
        this.wireMockServer = wireMockServer;
    }

    @Test
    void should_return_filtered_user_repositories_with_branches() {
        wireMockServer.stubFor(get(urlPathMatching("/users/.+/repos"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("repositories.json")
                ));

        wireMockServer.stubFor(get(urlPathMatching("/repos/.+/.+/branches"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("branches.json")
                ));

        webTestClient.get()
                .uri("/api/github/rafal-paton")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(RepositoryResponseDto.class)
                .hasSize(1)
                .value(repo -> {
                    assertThat(repo).isNotNull();
                    assertThat(repo).hasSize(1);
                    RepositoryResponseDto repository = repo.get(0);
                    assertThat(repository).isNotNull();
                    assertThat(repository.repositoryName()).isEqualTo("songify");
                    assertThat(repository.ownerLogin()).isEqualTo("rafal-paton");
                    assertThat(repository.branches()).hasSize(2);
                    assertThat(repository.branches().get(0)).isNotNull();
                    assertThat(repository.branches().get(0).name()).isEqualTo("first");
                    assertThat(repository.branches().get(0).sha()).isEqualTo("123456789");
                    assertThat(repository.branches().get(1)).isNotNull();
                    assertThat(repository.branches().get(1).name()).isEqualTo("second");
                    assertThat(repository.branches().get(1).sha()).isEqualTo("987654321");
                });
    }

    @Test
    void should_return_406_not_acceptable_for_wrong_accept_header() {
        webTestClient.get().uri("/api/github/testUser")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .exchange()
                .expectStatus().isEqualTo(406)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(406)
                .jsonPath("$.message").isEqualTo("Wrong header 'accept'. Only JSON is acceptable.");
    }

    @Test
    void should_return_404_user_not_found_for_non_existent_user() {
        wireMockServer.stubFor(get(urlEqualTo("/users/nonExistentUser/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get().uri("/api/github/nonExistentUser")
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void should_handle_parallel_requests_within_expected_time() {
        wireMockServer.stubFor(get(urlPathMatching("/users/.+/repos"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withFixedDelay(100)
                        .withBodyFile("repositories.json")
                ));

        wireMockServer.stubFor(get(urlPathMatching("/repos/.+/.+/branches"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withFixedDelay(200)
                        .withBodyFile("branches.json")
                ));

        long startTime = System.currentTimeMillis();

        webTestClient.get()
                .uri("/api/github/rafal-paton")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(RepositoryResponseDto.class)
                .hasSize(1)
                .value(repo -> {
                    assertThat(repo).isNotNull();
                    assertThat(repo).hasSize(1);
                    RepositoryResponseDto repository = repo.get(0);
                    assertThat(repository).isNotNull();
                    assertThat(repository.repositoryName()).isEqualTo("songify");
                    assertThat(repository.ownerLogin()).isEqualTo("rafal-paton");
                    assertThat(repository.branches()).hasSize(2);
                    assertThat(repository.branches().get(0)).isNotNull();
                    assertThat(repository.branches().get(0).name()).isEqualTo("first");
                    assertThat(repository.branches().get(0).sha()).isEqualTo("123456789");
                    assertThat(repository.branches().get(1)).isNotNull();
                    assertThat(repository.branches().get(1).name()).isEqualTo("second");
                    assertThat(repository.branches().get(1).sha()).isEqualTo("987654321");
                });

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        assertThat(elapsedTime).isLessThan(400);
    }
}