package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.*;
import be.uantwerpen.fti.se.imagineframe_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile({"production", "db_dev", "test", "dev"})
public class DatabaseLoader {
    private final RegistrationService registrationService;
    @Value("${frontend_url}")
    private String frontendUrl;

    @Value("${administrator_group_name}")
    private String adminGroupName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final GroupRepository groupRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final BatchRepository batchRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;

    private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);

    public DatabaseLoader(UserRepository userRepository, PrivilegeRepository privilegeRepository, GroupRepository groupRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder
            , TagRepository tagRepository, BatchRepository batchRepository, EventRepository eventRepository, RegistrationService registrationService, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.privilegeRepository = privilegeRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.batchRepository = batchRepository;
        this.eventRepository = eventRepository;
        this.registrationService = registrationService;
        this.projectRepository = projectRepository;
    }

    @PostConstruct
    private void initDatabase() {

        logger.info("frontendURL: {}", frontendUrl);
        logger.info("adminGroupName: {}", adminGroupName);
        logger.info("activeProfile: {}", activeProfile);

        // Then use this method like so:
        Privilege p_logon = createPrivilegeIfNotFoundOrUpdate("logon", "User is logged in.");
        Privilege p_profile_write = createPrivilegeIfNotFoundOrUpdate("profile_write", "Can edit own profile.");
        Privilege p_admin_read = createPrivilegeIfNotFoundOrUpdate("admin_read", "Can read everything. Should be removed!");
        Privilege p_admin_write = createPrivilegeIfNotFoundOrUpdate("admin_write", "Can edit everything. Should be removed!");
        Privilege p_groups_read = createPrivilegeIfNotFoundOrUpdate("groups_read", "Can read all groups.");
        Privilege p_groups_write = createPrivilegeIfNotFoundOrUpdate("groups_write", "Can edit groups.");
        Privilege p_privileges_read = createPrivilegeIfNotFoundOrUpdate("privileges_read", "Can read privileges.");
        Privilege p_privileges_write = createPrivilegeIfNotFoundOrUpdate("privileges_write", "Can edit privileges.");
        Privilege p_event_create = createPrivilegeIfNotFoundOrUpdate("event_create", "Can create events.");
        Privilege p_event_manage = createPrivilegeIfNotFoundOrUpdate("event_manage", "Can edit events.");
        Privilege p_project_read = createPrivilegeIfNotFoundOrUpdate("project_read", "Can read projects.");
        Privilege p_project_write = createPrivilegeIfNotFoundOrUpdate("project_write", "Can edit projects.");
        Privilege p_user_edit = createPrivilegeIfNotFoundOrUpdate("user_edit", "Can edit users, except password.");
        Privilege p_registration_request_read = createPrivilegeIfNotFoundOrUpdate("registration_edit", "Read and change all current registration requests.");
        Privilege p_password_edit = createPrivilegeIfNotFoundOrUpdate("password_edit","Force change passwords from users.");
        Privilege p_product_write = createPrivilegeIfNotFoundOrUpdate("product_write", "Can write products.");
        Privilege p_product_read = createPrivilegeIfNotFoundOrUpdate("product_read", "Can read products.");
        Privilege p_product_manager = createPrivilegeIfNotFoundOrUpdate("product_manager", "Can manage products and export reports.");


        // Create groups
        Group administrators = createAdminGroup();


        // Member group
        Group member = new Group("iMagineer");
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(p_logon);
        privileges.add(p_profile_write);
        privileges.add(p_event_create);
        privileges.add(p_project_read);
        member.setPrivileges(privileges);
        groupRepository.save(member);

        // Tester group
        Group tester = new Group("Tester");
        privileges = new HashSet<>();
        privileges.add(p_logon);
        privileges.add(p_event_create);
        privileges.add(p_event_manage);
        privileges.add(p_project_read);
        privileges.add(p_project_write);
        tester.setPrivileges(privileges);
        groupRepository.save(tester);

        // Logon group
        Group logon = new Group("Logon");
        privileges = new HashSet<>();
        privileges.add(p_logon);
        privileges.add(p_profile_write);
        //privileges.add(p_project_read);
        logon.setPrivileges(privileges);
        groupRepository.save(logon);

        // Create users
        // Create admin user
        User adminUser = createUserIfNotFound(
                "admin@uantwerpen.be",
                "admin",
                "admin",
                "UA",
                "admin.ua",
                Set.of(administrators)
        );

        User john_doe = createUserIfNotFound(
                "john.doe@uantwerpen.be",
                "password",
                "John",
                "Doe",
                "john.doe",
                Set.of(tester)
        );

        User jane_doe = createUserIfNotFound(
                "jane.doe@uantwerpen.be",
                "password",
                "Jane",
                "Doe",
                "jane.doe",
                Set.of(tester)
        );

        User zane_zoe = createUserIfNotFound(
                "zane.zoe@uantwerpen.be",
                "password",
                "Zane",
                "Zoe",
                "zane.zoe",
                Set.of(logon)
        );


        /* INITIALISATION OF TEST DATA!! */
        if (!Objects.equals(activeProfile, "production") && !Objects.equals(activeProfile, "db_dev")) {
            List<User> loopUsers = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                User u = new User("user" + i + "@uantwerpen.be", passwordEncoder.encode("password"));
                u.setFirstName("User");
                u.setLastName("Number " + i);
                u.setUsername("user" + i);
                u.setGroups(Set.of(member));
                userRepository.save(u);

                if (i < 4) {
                    loopUsers.add(u);
                }
            }

            //Create Products
            Product product1 = new Product("Cocacola", "Refreshment", null, null);
            Product product2 = new Product("Pepsi", "Refreshment", null, null);
            Map<String, String> properties = new HashMap<>();
            properties.put("sweetness", "8");  // Clave: "sweetness", Valor: "8"
            properties.put("Taste", "Cola");
            Product product3 = new Product("Dr Pepper", "Refreshment", properties, null);
            productRepository.save(product1);
            productRepository.save(product2);


            Tag tag1 = new Tag("soda");
            Tag tag2 = new Tag("cola");
            Tag tag3 = new Tag("expensive");
            Tag tag4 = new Tag("10/10");
            product3.addTag(tag1);
            product3.addTag(tag2);
            product3.addTag(tag3);
            product3.addTag(tag4);
            Batch batch = new Batch(product1, 23, 24.02);
            Batch batch1 = new Batch(product1, 46, 50);
            Batch batch2 = new Batch(product1, 12, 34);
            batchRepository.save(batch);
            batchRepository.save(batch1);
            batchRepository.save(batch2);
            tagRepository.save(tag1);
            tagRepository.save(tag2);
            tagRepository.save(tag3);
            tagRepository.save(tag4);
            productRepository.save(product3);

            // add projects
            Project project1 = new Project("Web Application Redesign", "Redesign our company's web application with modern UI and improved functionality", null, ProjectStatus.IN_PROGRESS,jane_doe);
            Project project2 = new Project("Mobile App Development", "Create a new mobile application for both iOS and Android platforms", null, ProjectStatus.PLANNING,jane_doe);
            Project privacyProject = new Project("Privacy Test Project", "A project to test privacy settings for contributors", null, ProjectStatus.IN_PROGRESS, jane_doe);

            project1.getUsers().add(jane_doe);
            project1.getUsers().add(john_doe);
            project2.getUsers().add(jane_doe);
            privacyProject.getUsers().add(jane_doe);
            for (int i = 0; i < 4; i++) {
                privacyProject.getUsers().add(loopUsers.get(i));
            }

            loopUsers.get(0).setPrivacyLevel(PrivacyLevel.PRIVATE);
            loopUsers.get(1).setPrivacyLevel(PrivacyLevel.IMAGINEERS_ONLY);
            loopUsers.get(2).setPrivacyLevel(PrivacyLevel.PUBLIC);
            loopUsers.get(3).setPrivacyLevel(PrivacyLevel.PUBLIC);
            userRepository.saveAll(loopUsers);

            projectRepository.save(project1);
            projectRepository.save(project2);
            projectRepository.save(privacyProject);

            // Create custom events
            List<Event> events = new ArrayList<>();

            events.add(new Event("Tech Hackathon", "A 24-hour coding challenge.", "San Francisco, CA",
                    LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 16, 9, 0),
                    "https://example.com/hackathon.jpg", EventLabel.HACKATHON,loopUsers.get(3)));

            events.add(new Event("AI & ML Bootcamp", "Learn AI and ML from experts.", "Online",
                    LocalDateTime.of(2025, 7, 10, 10, 0), LocalDateTime.of(2025, 7, 20, 17, 0),
                    "https://example.com/bootcamp.jpg", EventLabel.LEARNING,jane_doe));

            events.add(new Event("Startup Meetup", "Networking event for entrepreneurs.", "New York, NY",
                    LocalDateTime.of(2025, 8, 5, 18, 0), LocalDateTime.of(2025, 8, 5, 22, 0),
                    "https://example.com/meetup.jpg", EventLabel.MEETING,jane_doe));

            events.add(new Event("Food Festival", "Taste gourmet food from top chefs.", "Miami, FL",
                    LocalDateTime.of(2025, 9, 12, 12, 0), LocalDateTime.of(2025, 9, 12, 20, 0),
                    "https://example.com/foodfest.jpg", EventLabel.EATING,jane_doe));

            events.add(new Event("Cocktail Night", "Exclusive cocktail tasting event.", "Las Vegas, NV",
                    LocalDateTime.of(2025, 10, 20, 19, 0), LocalDateTime.of(2025, 10, 20, 23, 59),
                    "https://example.com/cocktail.jpg", EventLabel.DRINKING,jane_doe));

            events.add(new Event("Coding Marathon", "Solve complex coding problems in a race.", "Austin, TX",
                    LocalDateTime.of(2025, 11, 3, 9, 0), LocalDateTime.of(2025, 11, 3, 18, 0),
                    "https://example.com/codingmarathon.jpg", EventLabel.CODING,jane_doe));

            events.add(new Event("Movie Premiere", "Exclusive premiere of a blockbuster film.", "Los Angeles, CA",
                    LocalDateTime.of(2025, 12, 5, 18, 30), LocalDateTime.of(2025, 12, 5, 21, 0),
                    "https://example.com/moviepremiere.jpg", EventLabel.MOVIE,jane_doe));

            events.add(new Event("New Year’s Eve Party", "Celebrate the new year in style.", "New York, NY",
                    LocalDateTime.of(2025, 12, 31, 21, 0), LocalDateTime.of(2026, 1, 1, 2, 0),
                    "https://example.com/newyearparty.jpg", EventLabel.PARTY,jane_doe));

            events.add(new Event("Tech Conference", "A conference about emerging technologies.", "Seattle, WA",
                    LocalDateTime.of(2025, 5, 18, 10, 0), LocalDateTime.of(2025, 5, 19, 16, 0),
                    "https://example.com/techconference.jpg", EventLabel.LEARNING,jane_doe,false));

            Optional<User> eventOwner = null;
            events.add(new Event("Casual Hangout", "A get-together for tech enthusiasts.", "Chicago, IL",
                    LocalDateTime.of(2025, 4, 10, 18, 0), LocalDateTime.of(2025, 4, 10, 22, 0),
                    "https://example.com/hangout.jpg", EventLabel.OTHER,jane_doe,false));

            eventRepository.saveAll(events);

            for (int i = 1; i <= 20; i++) {
                Registration request = new Registration(
                        "requestUser" + i + "@example.com",
                        "requestUser" + i,
                        "Password",
                        "Password",
                        "FirstName" + i,
                        "LastName" + i
                );
                registrationService.saveRegistration(request);
            }

            // Two registration with the same email. These can exist, but need to be handled correctly.
            Registration registration = new Registration("same@email.com", "username", "password", "password", "firstName", "lastName");
            Registration sameRegistration = new Registration("same@email.com", "otherUsername", "password", "password", "firstName", "lastName");
            registrationService.saveRegistration(registration);
            registrationService.saveRegistration(sameRegistration);
        }
    }

    private Privilege createPrivilegeIfNotFoundOrUpdate(String name, String description) {
        Privilege privilege;
        if (privilegeRepository.findByName(name).isPresent()) {
            privilege = privilegeRepository.findByName(name).get();
            if (!privilege.getDescription().equals(description)) {
                privilege.setDescription(description);
                privilegeRepository.save(privilege);
            }
        } else {
            privilege = new Privilege(name, description);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    private Group createGroupIfNotFound(String name, Set<Privilege> privileges) {
        return groupRepository.findByName(name).orElseGet(() -> {
            Group group = new Group(name);
            group.setPrivileges(privileges);
            return groupRepository.save(group);
        });
    }

    private User createUserIfNotFound(String email, String password, String firstName, String lastName, String username, Set<Group> groups) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(email, passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setGroups(groups);
            return userRepository.save(user);
        });
    }

    private Group createAdminGroup(){
        // Try and get the current admin group (Id = 1)
        Group adminGroup;
        try {
            adminGroup = groupRepository.findById(1L).get();

            // Check if the name is still valid
            if (!Objects.equals(adminGroup.getName(), adminGroupName)) {
                adminGroup.setName(adminGroupName);

            }
        } catch (NoSuchElementException e) {
            // Create admin group
            adminGroup = new Group(adminGroupName);
        }

        // Add ALL privileges
        Set<Privilege> privilegesAdmin = new HashSet<>();
        for (Privilege privilege : privilegeRepository.findAll()) {
            privilegesAdmin.add(privilege);
        }
        adminGroup.setPrivileges(privilegesAdmin);

        return groupRepository.save(adminGroup);
    }



}
