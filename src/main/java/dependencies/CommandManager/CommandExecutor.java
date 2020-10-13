package dependencies.CommandManager;


import dependencies.UserAuthorization.User;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import server.Authorization.Authorization;
import dependencies.Collection.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.DatabaseManager.DatabaseManager;
import server.Exceptions.SameAdminException;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "Convert2MethodRef", "SimplifyStreamApiCallChains", "AccessStaticViaInstance"})
public class CommandExecutor {
    private static final Logger logger = LogManager.getLogger();
    private static DatabaseManager databaseManager;

    private List<StudyGroup> groups = Collections.synchronizedList(new ArrayList<>());
    private String[][] availableCommands = new String[16][1];
    private HashMap<String, String> message = new HashMap<String, String>(){{
        put("status", "");
        put("message", "");
    }};
    public ArrayList<String> history = new ArrayList<>();
    private static CommandExecutor commandExecutor;
    private static Authorization authLib;

    private Date creationDate;

    public CommandExecutor(DatabaseManager databaseManager, Authorization authLib) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveCollectionToDB));
        this.databaseManager = databaseManager;
        this.authLib = authLib;
        creationDate = new Date();
        loadCollection();
        loadAvailableCommands();
    }

    public static CommandExecutor getInstance(){
        if (commandExecutor == null) {
            commandExecutor = new CommandExecutor(databaseManager, authLib);
        }
        return commandExecutor;
    }

    private void loadAvailableCommands() {
        try (Scanner in = new Scanner(new FileReader("file_commands.txt"))) {
            String str;
            int i = 0;
            while (in.hasNextLine()) {
                str = in.nextLine();
                availableCommands[i] = str.split(":");
                i++;
            }
        } catch (FileNotFoundException e) {
            logger.error("File with commands not found!");
            System.exit(1);
        }
    }

    public void saveCollectionToDB() {
        this.databaseManager.saveToDB(this.groups);
    }

    public void sortGroups() {
        List<StudyGroup> groupPriorityQueue = Collections.synchronizedList(new ArrayList<>());
        groups.stream().sorted().forEachOrdered(r -> groupPriorityQueue.add(r));
        groups = groupPriorityQueue;
    }

    public HashMap<String, String> help() {
        message.clear();
        message.put("status", "200");
        StringBuilder m = new StringBuilder();
        for (int i = 0; i<15; i++) {
            m.append(String.format("* %s - %s\n", availableCommands[i][0], availableCommands[i][1]));
        }
        message.put("message", m.toString());
        return message;
    }

    public HashMap<String, String> info(User user) {
        message.clear();
        message.put("status", "200");
        message.put(
                "message",
                String.format(
                        "Collection type: %s\n" +
                                "Creation date: %s\n" +
                                "Collection size: %s\n" +
                                "Logged in as: %s",
                        groups.getClass().getName(),
                        creationDate,
                        groups.size(),
                        user.getUsername()
                )
        );
        return message;
    }

    public HashMap<String, String> show() {
        message.clear();
        message.put("status", "200");

        final String[] m = {""};
        groups.stream().forEachOrdered((p) -> {
            m[0] += p.toString();
        });

        message.put("message", m[0]);
        if (message.size() == 0) {
            message.put("message", "Collection is empty");
        }
        return message;
    }

    public HashMap<String, String> login(String username, String rawPassword) {
        message.clear();
        message = authLib.loginUser(username, rawPassword);
        return message;
    }

    public HashMap<String, String> logout(String username, String rawPassword) {
        message.clear();
        message = authLib.logoutUser(new User(username, rawPassword));
        return message;
    }

    public HashMap<String, String> register(String username, String rawPassword) {
        message.clear();
        message = authLib.registerUser(username, rawPassword);
        return message;
    }

    public HashMap<String, String> addStudyGroup(StudyGroup group, String author) {
        message.clear();
        try {
            databaseManager.addGroup(
                    group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                    group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                    group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount(), LocalDate.now(), author
            );
            groups = databaseManager.getAllGroups();
            sortGroups();
            message.put("status", "201");
            message.put("message", "Element added");
        } catch (SameAdminException e) {
            message.put("status", "400");
            message.put("message", "Group with given admin already exists");
        }
        return message;
    }

    public HashMap<String, String> updateStudyGroup(StudyGroup group, User user) {
        message.clear();

        if (databaseManager.getGroup(group.getId()) == null) {
            message.put("status", "404");
            message.put("message", "Group with given id doesn't exist!");
        } else {
            if (authLib.hasPermissionToEdit(user, group.getId())) {
                databaseManager.updateGroup(
                        group.getId(), group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                        group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                        group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount()
                );
                this.groups = this.databaseManager.getAllGroups();
                message.put("status", "200");
                message.put("message", "updated");
            } else {
                message.put("status", "403");
                message.put("message", "Permission denied");
            }
        }
        return message;
    }

    public HashMap<String, String> remove_by_id(Long id, User user) {
        message.clear();
        boolean foundFlag = false;
        for (StudyGroup studyGroup : groups) {
            if (studyGroup.getId().equals(id)) {
                if (authLib.hasPermissionToEdit(user, id)) {
                    groups.remove(studyGroup);
                    foundFlag = true;
                    databaseManager.deleteById(id);
                    message.put("status", "200");
                    message.put("message", String.format("Group with id '%s' was deleted", id));
                } else {
                    message.put("status", "403");
                    message.put("message", "Permission denied");
                }
                break;
            }
        }
        if (!foundFlag) {
            message.put("status", "404");
            message.put("message", "Element with given id doesn't exist!");
        }
        return message;
    }

    public HashMap<String, String> clearCollection(User user) {
        message.clear();
        databaseManager.clearAll(user);
        groups.clear();
        message.put("status", "200");
        message.put("message", "Collection cleared");
        return message;
    }

    private void loadCollection() {
        groups = databaseManager.getAllGroups();
        logger.info("Collection uploaded.");
    }

    public HashMap<String, String> exit() {
        message.clear();
        history.clear();
        message.put("status", "200");
        message.put("message", "Completition of work...");

        return message;
    }

    public HashMap<String, String> remove_first(User user) {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }

            StudyGroup studyGroup = groups.get(0);

            if (authLib.hasPermissionToEdit(user, studyGroup.getId())) {
                databaseManager.deleteById(studyGroup.getId());
                message.put("status", "200");
                message.put("message", "Element removed");
            } else {
                message.put("status", "403");
                message.put("message", "Permission denied");
            }
        } catch (NoSuchElementException e) {
            message.put("status", "404");
            message.put("message", "No such element");
        }
        return message;
    }

    public HashMap<String, String> head() {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }
            message.put("status", "200");
            message.put("message", String.format("Elements are: %s", groups.get(0)));
        } catch (NoSuchElementException e) {
            message.put("status", "404");
            message.put("message", "No such element");
        }
        return message;
    }

    public HashMap<String, String> getHistory() {
        message.clear();
        StringBuilder m = new StringBuilder();
        for (String c: this.history
             ) {
            m.append(c);
        }
        message.put("status", "200");
        message.put("message", m.toString());
        return message;
    }

    public void addToHistory(String commandName) {
        if (this.history.size() >= 7) {
            this.history.remove(0);
        }
        this.history.add(commandName);
    }

    public HashMap<String, String> filter_contains_name(String str) {
        message.clear();
        groups.stream().filter(
                studyGroup -> studyGroup.getName().contains(str)).forEachOrdered(
                        studyGroup -> {
                            message.put("status", "200");
                            message.put("message", studyGroup.toString());
                        }
        );

        if (message.isEmpty()) {
            message.put("status", "400");
            message.put("message", "No such element");
        }
        return message;
    }

    public HashMap<String, String> max_by_expelled_students() {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }

            Integer max0 = 0;
            StudyGroup studyGroupMax = null;

            for (StudyGroup studyGroup: groups) {
                if (studyGroup.getExpelledStudentsAmount()>max0) {
                    max0 = studyGroup.getExpelledStudentsAmount();
                    studyGroupMax = studyGroup;
                }
            }
            assert studyGroupMax != null;
            message.put("status", "200");
            message.put("message", studyGroupMax.toString());
        } catch (NoSuchElementException e) {
            message.put("status", "404");
            message.put("message", "No such element");
        }
        return message;
    }

    public HashMap<String, String> filter_by_group_admin(String fieldsGroupAdmin) {
        message.clear();

        boolean foundFlag = false;
        String[] adminArray = fieldsGroupAdmin.split(";");

        String nameA = adminArray[0];
        double heightA = Double.parseDouble(adminArray[1]);
        int weightA = Integer.parseInt(adminArray[2]);
        Country countryA = Country.valueOf(adminArray[3]);

        Person groupAdmin = new Person(nameA, heightA, weightA, countryA);

        for (StudyGroup studyGroup: groups) {
            if (studyGroup.getGroupAdmin().equals(groupAdmin)) {
                message.put("status", "200");
                message.put("message", studyGroup.toString());
                foundFlag = true;
            }
        }

        if (!foundFlag) {
            message.put("status", "404");
            message.put("message", "No such element");
        }

        return message;
    }
}
