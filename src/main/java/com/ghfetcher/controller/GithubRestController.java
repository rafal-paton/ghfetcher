package com.ghfetcher.controller;

import com.ghfetcher.dto.RepositoryResponseDto;
import com.ghfetcher.error.WrongAcceptHeaderException;
import com.ghfetcher.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GithubRestController {

    private final GithubService githubService;

    @GetMapping("/{userName}")
    public Flux<RepositoryResponseDto> fetchUserRepositoriesWithBranches(
            @PathVariable String userName,
            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader
    ) {
        if (!acceptHeader.equals(MediaType.APPLICATION_JSON_VALUE)) {
            log.error("Unsupported media type requested: : {}", acceptHeader);
            throw new WrongAcceptHeaderException("Only JSON type is supported!");
        }
        return githubService.fetchUserRepositoriesWithBranches(userName);
    }
}