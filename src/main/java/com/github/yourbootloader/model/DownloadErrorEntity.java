package com.github.yourbootloader.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DownloadErrorEntity {

    @Id
    private Long id;

    private String url;

    private String stacktrace;

}
