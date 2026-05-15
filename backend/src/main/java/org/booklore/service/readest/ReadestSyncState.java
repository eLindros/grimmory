package org.booklore.model.dto.readest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadestSyncState {

    private String documentId;

    private Float progression;

    private Locator locator;

    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Locator {
        private String href;
        private String cfi;
    }
}