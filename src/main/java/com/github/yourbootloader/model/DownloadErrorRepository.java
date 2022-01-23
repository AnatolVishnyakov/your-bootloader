package com.github.yourbootloader.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadErrorRepository extends JpaRepository<DownloadErrorEntity, Long> {
}
