package dependencies.CommandManager;


import dependencies.UserAuthorization.User;
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
    private ArrayList<String> message = new ArrayList<>();
    public ArrayList<String> history = new ArrayList<>();
    private static CommandExecutor commandExecutor;
    private static Authorization authLib;

    private Date creationDate;


    public CommandExecutor(DatabaseManager databaseManager, Authorization authLib) {
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

    public void sortGroups() {
        List<StudyGroup> groupPriorityQueue = Collections.synchronizedList(new ArrayList<>());
        groups.stream().sorted().forEachOrdered(r -> groupPriorityQueue.add(r));
        groups = groupPriorityQueue;
    }

    public ArrayList<String> help() {
        message.clear();
        for (int i = 0; i<15; i++) {
            message.add(String.format("* %s - %s", availableCommands[i][0], availableCommands[i][1]));
        }
        return message;
    }

    public ArrayList<String> info(User user) {
        message.clear();
        message.add(
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

    public ArrayList<String> show() {
        message.clear();
        groups.stream().forEachOrdered((p) -> message.add(p.toString()));
        return message;
    }

    public ArrayList<String> login(String username, String rawPassword) {
        message.clear();
        message.add(authLib.loginUser(username, rawPassword));
        return message;
    }

    public ArrayList<String> logout(String username, String rawPassword) {
        message.clear();
        message.add(authLib.logoutUser(new User(username, rawPassword)));
        return message;
    }

    public ArrayList<String> register(String username, String rawPassword) {
        message.clear();
        message.add(authLib.registerUser(username, rawPassword));
        return message;
    }

    public ArrayList<String> addStudyGroup(StudyGroup group, String author) {
        message.clear();
        try {
            databaseManager.addGroup(
                    group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                    group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                    group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount(), LocalDate.now(), author
            );
            groups = databaseManager.getAllGroups();
            sortGroups();
            message.add("Element added.");
        } catch (SameAdminException e) {
            message.add("Cannot create group with this admin: he is already related to another group");
        }
        return message;
    }

    public ArrayList<String> updateStudyGroup(StudyGroup group, User user) {
        message.clear();

        if (databaseManager.getGroup(group.getId()) == null) {
            message.add("Group with given id doesn't exist!");
        } else {
            if (authLib.hasPermissionToEdit(user, group.getId())) {
                databaseManager.updateGroup(
                        group.getId(), group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                        group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                        group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount()
                );
                message.add(String.format("Group with id '%s' was successfully updated", group.getId()));
            } else {
                message.add("Sorry, you do not have permission to edit this group");
            }
        }
        return message;
    }

    public ArrayList<String> remove_by_id(Long id, User user) {
        message.clear();
        boolean foundFlag = false;
        for (StudyGroup studyGroup : groups) {
            if (studyGroup.getId().equals(id)) {
                if (authLib.hasPermissionToEdit(user, id)) {
                    groups.remove(studyGroup);
                    foundFlag = true;
                    databaseManager.deleteById(id);
                    message.add(String.format("Group with id '%s' was deleted", id));
                } else {
                    message.add("Sorry, you do not have permission to edit this group");
                }
                break;
            }
        }
        if (!foundFlag) {
            message.add("Element with given id doesn't exist!");
        }
        return message;
    }

    public ArrayList<String> clearCollection(User user) {
        message.clear();
        databaseManager.clearAll(user);
        groups.clear();
        message.add("Collection cleared.");
        return message;
    }

    private void loadCollection() {
        groups = databaseManager.getAllGroups();
        logger.info("Collection uploaded.");
    }

    private PriorityQueue<StudyGroup> differencesForGroup() {
        return null;
    }

    public ArrayList<String> exit() {
        message.clear();
        history.clear();
        message.add("Completion of work...");

        return message;
    }

    public ArrayList<String> remove_first(User user) {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }

            StudyGroup studyGroup = groups.get(0);

            if (authLib.hasPermissionToEdit(user, studyGroup.getId())) {
                databaseManager.deleteById(studyGroup.getId());
                message.add("Element removed.");
            } else {
                message.add("Sorry, you do not have permission to edit this group");
            }
        } catch (NoSuchElementException e) {
            message.add("Collection is empty");
        }
        return message;
    }

    public ArrayList<String> head() {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }
            message.add(String.format("Element is output: %s", groups.get(0)));
        } catch (NoSuchElementException e) {
            message.add("Collection is empty!");
        }
        return message;
    }

    public ArrayList<String> getHistory() {
        return this.history;
    }

    public void addToHistory(String commandName) {
        if (this.history.size() >= 7) {
            this.history.remove(0);
        }
        this.history.add(commandName);
    }

    public ArrayList<String> filter_contains_name(String str) {
        message.clear();
        groups.stream().filter(
                studyGroup -> studyGroup.getName().contains(str)).forEachOrdered(
                        studyGroup -> message.add(studyGroup.toString())
        );

        if (message.isEmpty()) {
            message.add("No matches found!");
        }
        return message;
    }

    public ArrayList<String> max_by_expelled_students() {
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
            message.add(studyGroupMax.toString());
        } catch (NoSuchElementException e) {
            message.add("Collection is empty!");
        }
        return message;
    }

    public ArrayList<String> filter_by_group_admin(String fieldsGroupAdmin) {
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
                message.add(studyGroup.toString());
                foundFlag = true;
            }
        }

        if (!foundFlag) {
            message.add("No matches found!");
        }

        return message;
    }
}
