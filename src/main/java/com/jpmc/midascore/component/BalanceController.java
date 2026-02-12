package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Balance getBalance(@RequestParam Long userId) {

        Optional<UserRecord> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return new Balance(0.0f);
        }

        UserRecord user = optionalUser.get();
        return new Balance(user.getBalance());
    }
}
