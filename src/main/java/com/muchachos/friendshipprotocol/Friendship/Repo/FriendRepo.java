package com.muchachos.friendshipprotocol.Friendship.Repo;

import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRepo extends JpaRepository<Friend, Long> {

    boolean existsByEmail(String email);
    Optional<Friend> findFriendByEmail(String email);
}
