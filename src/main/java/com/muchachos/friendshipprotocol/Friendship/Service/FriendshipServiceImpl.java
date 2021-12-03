package com.muchachos.friendshipprotocol.Friendship.Service;

import com.muchachos.friendshipprotocol.Config.StaticStrings.API;
import com.muchachos.friendshipprotocol.Config.StaticStrings.ExceptionMessage;
import com.muchachos.friendshipprotocol.Config.StaticStrings.StatusCode;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipDTO;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipRequest;
import com.muchachos.friendshipprotocol.Friendship.DTO.FriendshipResponse;
import com.muchachos.friendshipprotocol.Friendship.DTO.MappedFriendshipResponse;
import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import com.muchachos.friendshipprotocol.Friendship.Entity.Friendship;
import com.muchachos.friendshipprotocol.Friendship.Repo.FriendRepo;
import com.muchachos.friendshipprotocol.Friendship.Repo.FriendshipRepo;
import com.muchachos.friendshipprotocol.User.Entity.User;
import com.muchachos.friendshipprotocol.User.Repo.UserRepo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Logger;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendRepo friendRepo;
    private final FriendshipRepo friendshipRepo;
    private final UserRepo userRepo;
    private final Logger logger = Logger.getLogger("Response from Remote Host");

    private final RestTemplate restTemplate = new RestTemplate();

    public FriendshipServiceImpl(FriendRepo friendRepo, FriendshipRepo friendshipRepo, UserRepo userRepo) {
        this.friendRepo = friendRepo;
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Friend getFriend(Friend friend) {
        friend.setId(null);
        return friendRepo.findFriendByEmail(friend.getEmail())
                .orElseGet(() -> friendRepo.save(friend));
    }

    @Override
    public ResponseEntity<?> handleFriendshipRequestFromClient(FriendshipDTO request) {

        // Send request to remote server
        MappedFriendshipResponse response = handleRequest(request);

        // If the request isn't executed on the remote server the response is
        // returned to the client without executing on this server
        switch (response.getStatusCode()) {
            case "500":
            case "501":
            case "530":
                return ResponseEntity.ok(response);
        }

        return executeRequest(request, false);
    }

    @Override
    public ResponseEntity<?> handleFriendshipRequestFromRemoteHost(FriendshipDTO dto) {

        // Swaps src, srcHost, dest and destHost values to enable reuse of request handler methods
        dto.swapSrcAndDest();

        return executeRequest(dto, true);
    }

    /**
     * Sends request to remote server and handles response
     */
    private MappedFriendshipResponse handleRequest(FriendshipDTO dto) {

        ResponseEntity<String> responseEntity = sendRequest(dto);

        String response = (String) parseJSON(responseEntity).get("response");

        FriendshipResponse friendshipResponse = new FriendshipResponse(response);

        MappedFriendshipResponse mappedResponse = MappedFriendshipResponse.mapResponse(friendshipResponse);

        logResponse(mappedResponse.getStatusCode(), response);

        return mappedResponse;
    }

    /**
     * Methods that execute the request on the database.
     * Response depends on if the request came from the client or a remote server
     */
    private ResponseEntity<?> addFriend(FriendshipDTO dto, boolean isRemoteHost) {

        Friendship friendship;

        if (!userRepo.existsByEmail(dto.getSrc())) {

            // Response depending on if request is from other server or from client
            if (isRemoteHost) {
                return ResponseEntity.ok(new FriendshipResponse(
                        FriendshipDTO.VERSION,
                        StatusCode.ERROR_COMMAND,
                        "Error: No user found by that email"));
            } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found by that email");
        }

        User user = userRepo.findByEmail(dto.getSrc()).orElseThrow();
        Friend friend = getFriend(new Friend(dto.getDest(), dto.getDestHost()));

        // Check if action can be performed if the friendship already exists, else creates new friendship
        if (friendshipRepo.existsByUser_EmailAndFriend_Email(dto.getSrc(), dto.getDest())) {
            friendship = friendshipRepo
                    .findFriendshipsByUser_EmailAndFriend_Email(dto.getSrc(), dto.getDest())
                    .orElseThrow();

            List<String> illegalStatus = List.of(Friendship.PENDING, Friendship.FRIENDS, Friendship.BLOCKED);
            if (illegalStatus.contains(friendship.getStatus())) {

                // Response depending on if request is from other server or from client
                if (isRemoteHost) {
                    return ResponseEntity.ok(new FriendshipResponse(
                            FriendshipDTO.VERSION,
                            StatusCode.ACCESS_DENIED,
                            "Error: You do not have permission to perform this action"));

                } else throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Error: You do not have permission to perform this action");

            }
        } else friendship = new Friendship(Friendship.PENDING, user, friend, isRemoteHost);

        // Save friendship
        friendship = friendshipRepo.save(friendship);
        user.getFriendships().add(friendship);
        friend.getFriendships().add(friendship);
        userRepo.save(user);
        friendRepo.save(friend);

        // Response depending on if request is from other server or from client
        if (isRemoteHost) {
            return ResponseEntity.ok(new FriendshipResponse(
                    FriendshipDTO.VERSION,
                    StatusCode.SUCCESS,
                    "Success: Friend request sent successfully"));
        } else
            return ResponseEntity.ok(friendship);
    }

    private ResponseEntity<?> changeFriendshipStatus(FriendshipDTO dto, boolean isRemoteHost) {

        // Check if friendship exists and responds with an error if it doesn't
        if (!friendshipRepo.existsByUser_EmailAndFriend_Email(dto.getSrc(), dto.getDest())) {

            // Response depending on if request is from other server or from client
            if (isRemoteHost) {
                return ResponseEntity.ok(new FriendshipResponse(
                        FriendshipDTO.VERSION,
                        StatusCode.ERROR_COMMAND,
                        "Error: No friend request exist between those users"));
            } else throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Error: No friend request exist between those users");
        }

        Friendship friendship = friendshipRepo
                .findFriendshipsByUser_EmailAndFriend_Email(dto.getSrc(), dto.getDest())
                .orElseThrow();

        // Helper strings
        String receiver = isRemoteHost ? dto.getDest() : dto.getSrc(); // depends on where request came from
        String message = "";
        String statusCode = StatusCode.SUCCESS;

        // Performs action if the request is legal else responds with an error
        // Example: The sender of a friend request cannot accept it
        switch (dto.getMethod().toUpperCase()) {
            case "ACCEPT":
                if (friendship.getReceiver().equals(receiver) && friendship.getStatus().equals(Friendship.PENDING)) {

                    String friend = isRemoteHost ? dto.getSrc() : dto.getDest();
                    friendship.setStatus("FRIENDS");
                    message = "Success: Friend request accepted. You are now friends with " + friend;

                } else {
                    statusCode = StatusCode.ACCESS_DENIED;
                    message = "Error: You do no have permission to perform this action";
                }
                break;

            case "DENY":
                if (friendship.getReceiver().equals(receiver) && friendship.getStatus().equals(Friendship.PENDING)) {

                    friendship.setStatus("DENIED");
                    message = "Success: Friend request denied";

                } else {
                    statusCode = StatusCode.ACCESS_DENIED;
                    message = "Error: You do no have permission to perform this action";
                }
                break;

            case "BLOCK":
                String friend = isRemoteHost ? dto.getSrc() : dto.getDest();
                friendship.setStatus("BLOCKED");
                message = "Success: User " + friend + " has been blocked";
                break;

            case "REMOVE":
                if (!friendship.getStatus().equals(Friendship.BLOCKED)) {
                    return removeFriend(friendship, isRemoteHost);

                } else {
                    statusCode = StatusCode.ACCESS_DENIED;
                    message = "Error: You do no have permission to perform this action";
                }
        }

        friendship = friendshipRepo.save(friendship);

        // Response depending on if request is from other server or from client
        if (isRemoteHost) {
            return ResponseEntity.ok(new FriendshipResponse(
                    FriendshipDTO.VERSION,
                    statusCode,
                    message));
        } else
            return ResponseEntity.ok(friendship);
    }

    private ResponseEntity<?> removeFriend(Friendship friendship, boolean isRemoteHost) {

        String friend = friendship.getFriend().getEmail();
        friendshipRepo.delete(friendship);

        // Response depending on if request is from other server or from client
        if (isRemoteHost)
            return ResponseEntity.ok(new FriendshipResponse(
                    FriendshipDTO.VERSION,
                    StatusCode.SUCCESS,
                    "Success: You are no longer friends with " + friend));

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


    /**
     * Helper Methods
     */
    private JSONObject parseJSON(ResponseEntity<String> response) {
        try {
            return (JSONObject) new JSONParser().parse(response.getBody());

        } catch (ParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.EXPECTATION_FAILED, ExceptionMessage.UNEXPECTED_RESPONSE);
        }
    }

    private ResponseEntity<?> executeRequest(FriendshipDTO dto, boolean isRemoteHost) {
        ResponseEntity<?> response;

        switch (dto.getMethod().toUpperCase()) {
            case "ADD":
                response = addFriend(dto, isRemoteHost);
                break;
            case "ACCEPT":
            case "DENY":
            case "BLOCK":
            case "REMOVE":
                response = changeFriendshipStatus(dto, isRemoteHost);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    private ResponseEntity<String> sendRequest(FriendshipDTO dto) {

        FriendshipRequest request = new FriendshipRequest(dto);

        String api = dto.getDestHost() + API.REQUEST;
        ResponseEntity<String> response;

        try {
            // Send request to remote host
            response = restTemplate.postForEntity(api, request, String.class);

        } catch (RestClientException e) {
            if (e.getMessage().contains("Connection refused"))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid host");
            else
                throw new ResponseStatusException(
                        HttpStatus.EXPECTATION_FAILED, ExceptionMessage.UNEXPECTED_RESPONSE);

        } catch (HttpMessageConversionException e) {
            throw new ResponseStatusException(
                    HttpStatus.EXPECTATION_FAILED, ExceptionMessage.UNEXPECTED_RESPONSE);
        }

        if (response.getStatusCode() != HttpStatus.OK)
            throw new ResponseStatusException(
                    HttpStatus.EXPECTATION_FAILED, ExceptionMessage.UNEXPECTED_RESPONSE);

        return response;
    }

    private void logResponse(String statusCode, String response) {

        if (statusCode.equals(StatusCode.SUCCESS))
            logger.info(response);
         else
             logger.warning(response);
    }
}
