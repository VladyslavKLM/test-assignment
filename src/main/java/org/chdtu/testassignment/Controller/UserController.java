package org.chdtu.testassignment.Controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.chdtu.testassignment.Model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final Map<String, User> userStore = new HashMap<>();

    @Value("${user.minimumAge}")
    private int minimumAge;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        LocalDate today = LocalDate.now();
        if (user.getBirthDate().isAfter(today.minusYears(minimumAge))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userStore.put(user.getEmail(), user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PatchMapping("/{email}")
    public ResponseEntity<User> updateUserPartial(@PathVariable String email, @RequestBody Map<String, Object> updates) {
        User user = userStore.get(email);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("address")) {
            user.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updates.get("phoneNumber"));
        }
        if (updates.containsKey("birthDate")) {
            LocalDate birthDate = LocalDate.parse((String) updates.get("birthDate"));



            if (birthDate.isAfter(LocalDate.now())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }




            user.setBirthDate(birthDate);
        }

        userStore.put(email, user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{email}")
    public ResponseEntity<User> updateUserFull(@PathVariable String email, @Valid @RequestBody User updatedUser) {
        User user = userStore.get(email);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userStore.put(email, updatedUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        if (!userStore.containsKey(email)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userStore.remove(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByBirthDate(@RequestParam @NotNull LocalDate from, @RequestParam @NotNull LocalDate to) {
        if (from.isAfter(to)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<User> results = userStore.values().stream()
                .filter(user -> user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to))
                .collect(Collectors.toList());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}