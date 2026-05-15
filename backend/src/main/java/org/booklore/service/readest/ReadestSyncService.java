package org.booklore.service.readest;
import org.booklore.model.dto.BookLoreUser;
import org.booklore.model.dto.readest.ReadestSyncState;
import org.booklore.model.entity.BookFileEntity;
import org.booklore.model.entity.BookLoreUserEntity;
import org.booklore.model.entity.UserBookFileProgressEntity;
import org.booklore.repository.UserBookFileProgressRepository;
import org.booklore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadestSyncService {

    private final ReadestDocumentResolver resolver;
    private final UserBookFileProgressRepository fileProgressRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public ReadestSyncState getProgress(Long bookFileId) {

        BookLoreUser authUser = authenticationService.getAuthenticatedUser();
        Long userId = authUser.getId();

        BookFileEntity file = resolver.resolve(bookFileId);

        return fileProgressRepository
                .findByUserIdAndBookFileId(userId, file.getId())
                .map(progress -> mapToDto(documentId, progress))
                .orElseGet(() -> ReadestSyncState.builder()
                        .documentId(String.valueOf(bookFileId))
                        .progression(0f)
                        .updatedAt(Instant.EPOCH)
                        .build());
    }

    @Transactional
    public ReadestSyncState updateProgress(
            Long bookFileId,
            ReadestSyncState incoming
    ) {

        BookLoreUser authUser = authenticationService.getAuthenticatedUser();
        Long userId = authUser.getId();

        BookLoreUserEntity user = userRepository.findById(userId)
                .orElseThrow();

        BookFileEntity file = resolver.resolve(bookFileId);

        UserBookFileProgressEntity entity = fileProgressRepository
                .findByUserIdAndBookFileId(userId, file.getId())
                .orElseGet(() -> {
                    UserBookFileProgressEntity newEntity = new UserBookFileProgressEntity();
                    newEntity.setUser(user);
                    newEntity.setBookFile(file);
                    return newEntity;
                });

        Instant incomingTime = incoming.getUpdatedAt() != null
                ? incoming.getUpdatedAt()
                : Instant.now();

        Instant existingTime = entity.getLastReadTime();

        if (existingTime != null && existingTime.isAfter(incomingTime)) {
            log.debug("Ignoring outdated Readest sync update");
            return mapToDto(String.valueOf(bookFileId), entity);
        }

        entity.setProgressPercent(incoming.getProgression());

        if (incoming.getLocator() != null) {
            entity.setPositionHref(incoming.getLocator().getHref());
            entity.setPositionData(incoming.getLocator().getCfi());
        }

        entity.setLastReadTime(incomingTime);

        UserBookFileProgressEntity saved = fileProgressRepository.save(entity);

        return mapToDto(documentId, saved);
    }

    private ReadestSyncState mapToDto(
            String documentId,
            UserBookFileProgressEntity entity
    ) {
        return ReadestSyncState.builder()
                .documentId(documentId)
                .progression(entity.getProgressPercent())
                .updatedAt(entity.getLastReadTime())
                .locator(ReadestSyncState.Locator.builder()
                        .href(entity.getPositionHref())
                        .cfi(entity.getPositionData())
                        .build())
                .build();
    }
}