package com.muchachos.friendshipprotocol.Friendship.Service;

import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipDTO;
import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import org.springframework.http.ResponseEntity;

public interface FriendshipService {

    Friend getFriend(Friend friend);
    ResponseEntity<?> handleFriendshipRequestFromClient(FriendshipDTO request);
    ResponseEntity<?> handleFriendshipRequestFromRemoteHost(FriendshipDTO dto);
}
