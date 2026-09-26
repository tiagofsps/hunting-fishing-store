package com.tiagofsps.hunting_fishing_store.user.seeding;

import com.tiagofsps.hunting_fishing_store.common.seeding.Seeder;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class UserSeeder implements Seeder {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private static final String DEFAULT_ADMIN_EMAIL = "admin@huntingfishing.com";
    private static final String DEFAULT_USER_EMAIL = "user@huntingfishing.com";
    private static final String DEFAULT_PASSWORD = "Password123!";
    private static final String DEMO_HUNTING_LICENSE = "DEMO-HUNT-0001";
    private static final String DEMO_FISHING_LICENSE = "DEMO-FISH-0001";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
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

        seedFixedAccounts(faker);
    }

    /**
     * Seeds the fixed demo accounts on an empty database: an admin and a
     * regular user, both with the generic demo password. The admin email and
     * password can be overridden via ADMIN_EMAIL/ADMIN_PASSWORD; in production
     * those variables are mandatory.
     */
    private void seedFixedAccounts(Faker faker) {
        boolean hasAdminCredentials = adminEmail != null && !adminEmail.isBlank()
                && adminPassword != null && !adminPassword.isBlank();

        if (environment.acceptsProfiles(Profiles.of("prod")) && !hasAdminCredentials) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL and ADMIN_PASSWORD environment variables must be set to seed the admin user in production");
        }

        String adminEmailValue = hasAdminCredentials ? adminEmail.trim() : DEFAULT_ADMIN_EMAIL;
        String adminPasswordValue = hasAdminCredentials ? adminPassword : DEFAULT_PASSWORD;

        User admin = new User();
        admin.setEmail(adminEmailValue);
        admin.setPassword(passwordEncoder.encode(adminPasswordValue));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPhone(faker.phoneNumber().cellPhone());
        admin.setEnabled(true);
        admin.setRole(Role.ADMIN);

        admin.getAddresses().add(buildAddress(admin, AddressType.SHIPPING, faker));
        admin.getAddresses().add(buildAddress(admin, AddressType.BILLING, faker));

        userRepository.save(admin);
        log.info("Seeded admin account: email={}, role={}", admin.getEmail(), admin.getRole());

        User demoUser = new User();
        demoUser.setEmail(DEFAULT_USER_EMAIL);
        demoUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        demoUser.setFirstName("Demo");
        demoUser.setLastName("User");
        demoUser.setPhone(faker.phoneNumber().cellPhone());
        demoUser.setEnabled(true);
        demoUser.setRole(Role.USER);
        demoUser.setHuntingLicenseNumber(DEMO_HUNTING_LICENSE);
        demoUser.setFishingLicenseNumber(DEMO_FISHING_LICENSE);

        demoUser.getAddresses().add(buildAddress(demoUser, AddressType.SHIPPING, faker));
        demoUser.getAddresses().add(buildAddress(demoUser, AddressType.BILLING, faker));

        userRepository.save(demoUser);
        log.info("Seeded demo user account: email={}, role={}", demoUser.getEmail(), demoUser.getRole());
    }

    private Address buildAddress(User user, AddressType type, Faker faker) {
        Address address = new Address();
        address.setRecipientName(user.getFirstName() + " " + user.getLastName());
        address.setRecipientPhone(user.getPhone());
        address.setStreet(faker.address().streetAddress());
        address.setCity(faker.address().city());
        address.setPostalCode(faker.address().zipCode());
        address.setCountryCode("PT");
        address.setAddressType(type);
        address.setUser(user);
        return address;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}