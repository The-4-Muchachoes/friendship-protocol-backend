package com.muchachos.friendshipprotocol.User.Service;

import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import com.muchachos.friendshipprotocol.User.Entity.User;

import java.util.Set;

public interface UserService {

    User signup(User user);
    User login(User user);
    Set<Friend> getFriendsByStatus(String email, String status);
}
