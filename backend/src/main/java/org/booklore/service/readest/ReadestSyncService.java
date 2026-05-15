package org.booklore.service.readest;
    private final UserBookFileProgressRepository fileProgressRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public ReadestSyncState getProgress(String documentId) {

        BookLoreUser authUser = authenticationService.getAuthenticatedUser();
        Long userId = authUser.getId();

        BookFileEntity file = resolver.resolve(documentId);

        return fileProgressRepository
                .findByUserIdAndBookFileId(userId, file.getId())
                .map(progress -> mapToDto(documentId, progress))
                .orElseGet(() -> ReadestSyncState.builder()
                        .documentId(documentId)
                        .progression(0f)
                        .updatedAt(Instant.EPOCH)
                        .build());
    }

    @Transactional
    public ReadestSyncState updateProgress(
            String documentId,
            ReadestSyncState incoming
    ) {

        BookLoreUser authUser = authenticationService.getAuthenticatedUser();
        Long userId = authUser.getId();

        BookLoreUserEntity user = userRepository.findById(userId)
                .orElseThrow();

        BookFileEntity file = resolver.resolve(documentId);

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
            return mapToDto(documentId, entity);
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