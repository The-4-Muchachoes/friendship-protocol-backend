package com.muchachos.friendshipprotocol.Friendship.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String host;

    @OneToMany(mappedBy = "friend")
    @JsonIgnore
    private Set<Friendship> friendships = new HashSet<>();

    public Friend(String email, String host) {
        this.email = email;
        this.host = host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return Objects.equals(email, friend.email) && Objects.equals(host, friend.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, host);
    }
}
