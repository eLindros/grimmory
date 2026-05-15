package org.booklore.service.readest;

import lombok.RequiredArgsConstructor;
import org.booklore.model.entity.BookFileEntity;
import org.booklore.repository.BookFileRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadestDocumentResolver {

    private final BookFileRepository bookFileRepository;

    public BookFileEntity resolve(String documentId) {
        return bookFileRepository.findBySha256(documentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No book file found for documentId: " + documentId));
    }
}