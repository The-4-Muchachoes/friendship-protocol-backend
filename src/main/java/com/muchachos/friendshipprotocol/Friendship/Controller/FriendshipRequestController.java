package com.muchachos.friendshipprotocol.Friendship.Controller;

import com.muchachos.friendshipprotocol.Config.StaticStrings.StatusCode;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipDTO;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipRequest;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipResponse;
import com.muchachos.friendshipprotocol.Friendship.Service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/friendship")
public class FriendshipRequestController {

    private final FriendshipService friendshipService;

    public FriendshipRequestController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping(path = "/{method}")
    private ResponseEntity<?> friendshipRequest(
            @RequestBody FriendshipDTO request,
            @PathVariable String method) {

        switch (method.toUpperCase()) {
            case "ADD":
            case "ACCEPT":
            case "DENY":
            case "BLOCK":
            case "REMOVE":
                checkRequest(request, method.toUpperCase());
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid method parameter");
        }

        return friendshipService.handleFriendshipRequestFromClient(request);
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

    private void checkRequest(FriendshipDTO request, String method) {
        request.setSrcHost(FriendshipDTO.HOST);
        request.setVersion(FriendshipDTO.VERSION);
        request.setMethod(method);

        if (request.getSrc() == null || request.getDest() == null || request.getDestHost() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
}
