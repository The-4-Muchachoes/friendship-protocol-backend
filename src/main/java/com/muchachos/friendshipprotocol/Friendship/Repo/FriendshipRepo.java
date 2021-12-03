package com.muchachos.friendshipprotocol.Friendship.Repo;

import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import com.muchachos.friendshipprotocol.Friendship.Entity.Friendship;
import com.muchachos.friendshipprotocol.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

    List<Friendship> findFriendshipsByUser(User user);
    Optional<Friendship> findFriendshipsByUser_EmailAndFriend_Email(String userEmail, String friendEmail);
    boolean existsByUser_EmailAndFriend_Email(String userEmail, String friendEmail);
}
