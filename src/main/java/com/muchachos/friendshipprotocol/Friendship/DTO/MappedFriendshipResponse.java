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
public class MappedFriendshipResponse {

    private String version;
    private String statusCode;
    private String phrase;

    public static MappedFriendshipResponse mapResponse(FriendshipResponse response) {
        String[] params = response.getResponse().split(" ");

        if (params.length < 3) throw new ResponseStatusException(
                HttpStatus.EXPECTATION_FAILED, ExceptionMessage.UNEXPECTED_RESPONSE);

        MappedFriendshipResponse mappedResponse = new MappedFriendshipResponse();
        mappedResponse.setVersion(params[0]);
        mappedResponse.setStatusCode(params[1]);

        StringBuilder phrase = new StringBuilder();
        for (int i = 2; i < params.length ; i++) {
            phrase.append(params[i]);
            if (i != params.length-1)
                phrase.append(" ");
        }
        mappedResponse.setPhrase(phrase.toString());

        return mappedResponse;
    }
}
