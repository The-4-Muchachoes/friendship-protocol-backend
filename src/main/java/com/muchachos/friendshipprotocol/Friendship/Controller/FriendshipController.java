package com.muchachos.friendshipprotocol.Friendship.Controller;

import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipDTO;
import com.muchachos.friendshipprotocol.Friendship.Service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@RestController
@CrossOrigin
@RequestMapping(path = "/api/friendrequests")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
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

    private void checkRequest(FriendshipDTO request, String method) {
        // set standard values (should be null on request)
        request.setSrcHost(FriendshipDTO.HOST);
        request.setVersion(FriendshipDTO.VERSION);
        request.setMethod(method);

        // set 'http://' in front of destHost if missing. Should be 'https' in production
        if (!request.getDestHost().startsWith("http://") && !request.getDestHost().startsWith("https://"))
            request.setDestHost("http://" + request.getDestHost());

        // check user that sent request exists
        if (!friendshipService.userExistsByEmail(request.getSrc()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not logged in");

        // check dest is not empty
        if (request.getDest() == null || request.getDest().equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must not be empty");

        // check destHost is not empty
        if (request.getDestHost() == null || request.getDestHost().equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host must not be empty");

        // test that destHost is a valid URI
        try {
            new URL(request.getDestHost()).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host is not valid");
        }
    }
}
