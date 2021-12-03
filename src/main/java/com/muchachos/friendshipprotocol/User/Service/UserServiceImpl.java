package com.muchachos.friendshipprotocol.User.Service;

import com.muchachos.friendshipprotocol.User.Entity.User;
import com.muchachos.friendshipprotocol.User.Repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User signup(User user) {
        if (userRepo.existsByEmail(user.getEmail()))
            throw new ResponseStatusException(HttpStatus.FOUND);

        if (user.getId() != null)
            user.setId(null);

        return userRepo.save(user);
    }
}
