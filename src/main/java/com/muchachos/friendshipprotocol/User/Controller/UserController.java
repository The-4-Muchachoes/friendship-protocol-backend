package com.muchachos.friendshipprotocol.User.Controller;

import com.muchachos.friendshipprotocol.Friendship.Entity.Friendship;
import com.muchachos.friendshipprotocol.User.Entity.User;
import com.muchachos.friendshipprotocol.User.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/friends")
    private ResponseEntity<?> getFriends(@RequestHeader("Authorization") String email) {
        return new ResponseEntity<>(
                userService.getFriendsByStatus(email, Friendship.FRIENDS),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/requests")
    private ResponseEntity<?> getFriendRequests(@RequestHeader("Authorization") String email) {
        return new ResponseEntity<>(
                userService.getFriendsByStatus(email, Friendship.PENDING),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/blocked")
    private ResponseEntity<?> getBlockedUsers(@RequestHeader("Authorization") String email) {
        return new ResponseEntity<>(
                userService.getFriendsByStatus(email, Friendship.BLOCKED),
                HttpStatus.OK
        );
    }

    @PostMapping(path = "/login")
    private ResponseEntity<?> login(@RequestBody User user) {
        return new ResponseEntity<>(
                userService.login(user),
                HttpStatus.OK
        );
    }

    @PostMapping(path = "/signup")
    private ResponseEntity<?> signup(@RequestBody User user) {
        return new ResponseEntity<>(
                userService.signup(user),
                HttpStatus.CREATED
        );
    }
}
