package com.github.yourbootloader.domain.chat.repository;

import com.github.yourbootloader.domain.users.repository.Users;
import lombok.Data;

@Data
//@Entity
public class Chat {
//    @Id
//    @Column(name = "id", nullable = false)
    private Long id;
//    @ManyToOne
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
