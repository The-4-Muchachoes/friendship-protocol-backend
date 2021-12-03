package com.muchachos.friendshipprotocol.Config;

import com.muchachos.friendshipprotocol.User.Entity.User;
import com.muchachos.friendshipprotocol.User.Repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSetup implements CommandLineRunner {

    private final UserRepo userRepo;

    public DataSetup(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) {
        User me = new User("me@email.com");
        User you = new User("you@email.com");

        userRepo.saveAll(List.of(me, you));
    }
}
