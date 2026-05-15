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

    @GetMapping("/bookfile/{bookFileId}")
    public ReadestSyncState getProgress(
            @PathVariable Long bookFileId
    ) {
        return service.getProgress(bookFileId);
    }

    @PostMapping("/bookfile/{bookFileId}")
    public ReadestSyncState updateProgress(
            @PathVariable Long bookFileId,
            @RequestBody ReadestSyncState state
    ) {
        return service.updateProgress(bookFileId, state);
    }
}