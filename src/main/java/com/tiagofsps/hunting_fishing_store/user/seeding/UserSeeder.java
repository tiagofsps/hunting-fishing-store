package com.tiagofsps.hunting_fishing_store.user.seeding;

import com.tiagofsps.hunting_fishing_store.common.seeding.Seeder;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class UserSeeder implements Seeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void seed() {
        if (userRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("en", "US"));
        List<User> users = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(faker.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase()));
            user.setPassword(passwordEncoder.encode("password123"));
            user.setPhone(faker.phoneNumber().cellPhone());
            user.setEnabled(true);

            Address address = new Address();
            address.setRecipientName(firstName + " " + lastName);
            address.setRecipientPhone(user.getPhone());
            address.setStreet(faker.address().streetAddress());
            address.setCity(faker.address().city());
            address.setPostalCode(faker.address().zipCode());
            address.setCountryCode("US");
            address.setAddressType(AddressType.SHIPPING);
            address.setUser(user);
            
            user.getAddresses().add(address);
            users.add(user);
        }

        userRepository.saveAll(users);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
