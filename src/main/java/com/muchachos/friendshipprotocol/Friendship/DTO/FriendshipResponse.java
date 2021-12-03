package com.muchachos.friendshipprotocol.Friendship.DTO;

import com.muchachos.friendshipprotocol.Config.StaticStrings.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponse {

    private String response; // VERSION sp STATUS_CODE sp PHRASE

    public FriendshipResponse(String version, String statusCose, String phrase) {
        this.response = version + " " + statusCose + " " + phrase;
    }
}
