package com.muchachos.friendshipprotocol.Friendship.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipRequest {

    private String request; // METHOD sp SRC sp SRC_HOST sp DEST sp DEST_HOST sp VERSION

    public FriendshipRequest(FriendshipDTO dto) {
        request = mapRequest(dto);
    }

    public static FriendshipDTO mapToFriendshipDTO(FriendshipRequest friendshipRequest) {

        if (friendshipRequest.getRequest() == null)
            return null;

        String[] request = friendshipRequest.getRequest().split(" ");

        if (request.length != 6)
            return null;

        FriendshipDTO response = new FriendshipDTO(
                request[0], request[1], request[2], request[3], request[4], request[5]
        );

        if (!response.isValid())
            return null;

        return response;
    }

    public static String mapRequest(FriendshipDTO dto) {
        String request = dto.getMethod() + " " +
                dto.getSrc() + " " +
                dto.getSrcHost() + " " +
                dto.getDest() + " " +
                dto.getDestHost() + " " +
                dto.getVersion();

        return request;
    }

}
