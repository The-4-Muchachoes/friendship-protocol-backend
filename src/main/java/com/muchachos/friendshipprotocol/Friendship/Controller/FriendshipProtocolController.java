package com.muchachos.friendshipprotocol.Friendship.Controller;

import com.muchachos.friendshipprotocol.Config.StaticStrings.StatusCode;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipDTO;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipRequest;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipResponse;
import com.muchachos.friendshipprotocol.Friendship.Service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = "/friendship")
public class FriendshipProtocolController {

    private final FriendshipService friendshipService;

    public FriendshipProtocolController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    private ResponseEntity<?> friendshipRequest(@RequestBody FriendshipRequest request) {

        FriendshipDTO dto = FriendshipRequest.mapToFriendshipDTO(request);

        if (dto == null)
            return ResponseEntity.ok(new FriendshipResponse(
                    FriendshipDTO.VERSION,
                    StatusCode.ERROR_PARAMETER,
                    "Error: Invalid protocol parameters"));

        return friendshipService.handleFriendshipRequestFromRemoteHost(dto);
    }
}
