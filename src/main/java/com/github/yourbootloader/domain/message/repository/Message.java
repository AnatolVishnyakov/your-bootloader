package com.github.yourbootloader.domain.message.repository;

import com.github.yourbootloader.domain.chat.repository.Chat;
import com.github.yourbootloader.domain.users.repository.Users;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "message")
public class Message {
    @Id
    @GeneratedValue
    private Long id;
    private Integer messageId;
    private String title;
    @Column(columnDefinition = "text")
    private String url;
    private Long fileSize;
    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Users createdUser;
    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Chat chat;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
