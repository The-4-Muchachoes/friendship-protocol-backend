package com.muchachos.friendshipprotocol.User.Service;

import com.muchachos.friendshipprotocol.Friendship.Entity.Friend;
import com.muchachos.friendshipprotocol.Friendship.Entity.Friendship;
import com.muchachos.friendshipprotocol.Friendship.Repo.FriendshipRepo;
import com.muchachos.friendshipprotocol.User.Entity.User;
import com.muchachos.friendshipprotocol.User.Repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;

    public UserServiceImpl(UserRepo userRepo, FriendshipRepo friendshipRepo) {
        this.userRepo = userRepo;
        this.friendshipRepo = friendshipRepo;
    }

    @Override
    public User login(User user) {
        return userRepo.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No user exists with these credentials"));
    }

    public User signup(User user) {
        if (userRepo.existsByEmail(user.getEmail()))
            throw new ResponseStatusException(HttpStatus.FOUND, "Email is already registered");

        if (user.getId() != null)
            user.setId(null);

        return userRepo.save(user);
    }

    @Override
    public Set<Friend> getFriendsByStatus(String email, String status) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No user exists with these credentials"));

        List<Friendship> friendships = friendshipRepo.findFriendshipsByUserAndStatus(user, status);

        if (status.equals(Friendship.PENDING))
            friendships.removeIf(friendship -> !friendship.getReceiver().equals(user.getEmail()));



        return friendships
                .stream()
                .map(Friendship::getFriend)
                .collect(Collectors.toSet());
    }
}
