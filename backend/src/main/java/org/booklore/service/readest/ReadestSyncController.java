package org.booklore.controller;

import lombok.RequiredArgsConstructor;
import org.booklore.model.dto.readest.ReadestSyncState;
import org.booklore.service.readest.ReadestSyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readest/sync")
public class ReadestSyncController {

    private final ReadestSyncService service;

    @GetMapping("/{documentId}")
    public ReadestSyncState getProgress(
            @PathVariable String documentId
    ) {
        return service.getProgress(documentId);
    }

    @PostMapping("/{documentId}")
    public ReadestSyncState updateProgress(
            @PathVariable String documentId,
            @RequestBody ReadestSyncState state
    ) {
        return service.updateProgress(documentId, state);
    }
}