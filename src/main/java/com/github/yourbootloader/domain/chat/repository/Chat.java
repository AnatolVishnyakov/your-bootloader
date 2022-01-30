package com.github.yourbootloader.domain.chat.repository;

import com.github.yourbootloader.domain.users.repository.Users;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Chat {
    @Id
    @SequenceGenerator(name = "chat_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    private Users user;
    private String type;
    private String title;
    private String description;
    private String inviteLink;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
