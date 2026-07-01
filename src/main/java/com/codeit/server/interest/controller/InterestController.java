package com.codeit.server.interest.controller;

import com.codeit.server.interest.dto.CursorPageResponse;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
import com.codeit.server.interest.dto.InterestUpdateRequest;
import com.codeit.server.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    // GET /api/interests - search with cursor-based pagination
    @GetMapping
    public ResponseEntity<CursorPageResponse<InterestResponse>> searchInterests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String nextAfter,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId
    ) {
        CursorPageResponse<InterestResponse> response =
                interestService.search(keyword, orderBy, cursor, nextAfter, size, userId);
        return ResponseEntity.ok(response);
    }

    // POST /api/interests - create a new interest
    @PostMapping
    public ResponseEntity<InterestResponse> create(
            @RequestBody @Valid InterestCreateRequest request,
            @RequestHeader(value = "Monew-Request-User-ID", required = true) UUID userId
    ) {
        InterestResponse response = interestService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PATCH /api/interests/{interestId} - update interest name/keywords
    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestResponse> update(
            @PathVariable UUID interestId,
            @RequestBody @Valid InterestUpdateRequest request,
            @RequestHeader(value = "Monew-Request-User-ID", required = true) UUID userId
    ) {
        InterestResponse response = interestService.update(interestId, request, userId);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/interests/{interestId} - hard delete an interest
    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> hardDelete(
            @PathVariable UUID interestId,
            @RequestHeader(value = "Monew-Request-User-ID", required = true) UUID userId
    ) {
        interestService.hardDelete(interestId, userId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/interests/{interestId}/subscriptions - subscribe to an interest
    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<InterestResponse> subscribe(
            @PathVariable UUID interestId,
            @RequestHeader(value = "Monew-Request-User-ID", required = true) UUID userId
    ) {
        InterestResponse response = interestService.subscribe(interestId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/interests/{interestId}/subscriptions - unsubscribe from an interest
    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable UUID interestId,
            @RequestHeader(value = "Monew-Request-User-ID", required = true) UUID userId
    ) {
        interestService.unsubscribe(interestId, userId);
        return ResponseEntity.noContent().build();
    }
}