package com.muchachos.friendshipprotocol.Friendship.Entity;

import com.muchachos.friendshipprotocol.User.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    public static String
            PENDING = "PENDING",
            FRIENDS = "FRIENDS",
            DENIED = "DENIED",
            BLOCKED = "BLOCKED",
            BLOCKED_BY_DEST = "BLOCKED_BY_DEST";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;

    private String sender;
    private String receiver;

    @ManyToOne
    private User user;

    @ManyToOne
    private Friend friend;

    public Friendship(String status, User user, Friend friend, boolean isRemoteHost) {
        this.status = status;
        this.user = user;
        this.friend = friend;

        this.sender = isRemoteHost ? friend.getEmail() : user.getEmail();
        this.receiver = isRemoteHost ? user.getEmail() : friend.getEmail();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(user, that.user) && Objects.equals(friend, that.friend);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, friend);
    }
}
