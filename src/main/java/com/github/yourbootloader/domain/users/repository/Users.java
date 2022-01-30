package com.github.yourbootloader.domain.users.repository;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Data
@Entity
public class Users {
    @Id
    @SequenceGenerator(name = "users_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;
    private String firstName;
    private Boolean isBot;
    private String lastName;
    private String userName;
    private String languageCode;
    private Boolean canJoinGroups;
    private Boolean canReadAllGroupMessages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
