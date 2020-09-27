package org.example.server.CommandManager;


import org.example.server.Authentification.UserAuthentication;
import org.example.server.Collection.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.server.DatabaseManager.DatabaseManager;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "Convert2MethodRef", "SimplifyStreamApiCallChains"})
public class CommandExecutor {
    private static final Logger logger = LogManager.getLogger();

    private PriorityQueue<StudyGroup> groups = new PriorityQueue<>();
    private PriorityQueue<StudyGroup> unsavedGroups;
    private String[][] availableCommands = new String[16][1];
    private ArrayList<String> message = new ArrayList<>();
    public ArrayList<String> history = new ArrayList<>();
    private static CommandExecutor commandExecutor;
    private DatabaseManager databaseManager;

    private UserAuthentication authLib;

    private Date creationDate;

//    private Semester groupCurrentSemester;
//    private Coordinates groupCoordinates;
//    private int expelledStudentsAmount;
//    private JSONObject coordinatesJson;
//    private int studentsToExpelAmount;
//    private Country adminNationality;
//    private String semesterEnumValue;
//    private String groupIdentifier;
//    private String groupAdminName;
//    private int studentsCount;
//    private double adminHeight;
//    private Person groupAdmin;
//    private JSONObject admin;
//    private long lastGroupID;
//    private int adminWeight;
//    private long groupID;
//    private long xCord;
//    private long yCord;

    public CommandExecutor(String collectionsJSONFilePath) {
        Runtime.getRuntime().addShutdownHook(new Thread());
        creationDate = new Date();
        loadCollection();
        loadAvailableCommands();
    }

    public static CommandExecutor getInstance(String filepath){
        if (commandExecutor == null) {
            commandExecutor = new CommandExecutor(filepath);
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
        PriorityQueue<StudyGroup> groupPriorityQueue = new PriorityQueue<>();
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

    public ArrayList<String> info() {
        message.clear();
        message.add(
                String.format("Collection type: %s\n", groups.getClass().getName()) +
                String.format("Creation date: %s\n", creationDate) +
                String.format("Amount of elements: %s", groups.size())
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
        message.add(authLib.logoutUser(username, rawPassword));
        return message;
    }

    public ArrayList<String> register(String username, String rawPassword) {
        message.clear();
        message.add(authLib.createUser(username, rawPassword));
        return message;
    }

    public ArrayList<String> addStudyGroup(StudyGroup group, String author) {
        this.databaseManager.addGroup(
                group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount(), LocalDateTime.now(), author
        );
        groups.add(group);
        sortGroups();
        message.add("Element added.");
        return message;
    }

    public ArrayList<String> updateStudyGroup(StudyGroup group) {
        message.clear();

        if (this.databaseManager.getGroup(group.getId()) == null) {
            message.add("Group with given id doesn't exist!");
        } else {
            this.databaseManager.updateGroup(
                    group.getId(), group.getName(), group.getSemester(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                    group.getStudentsCount(), group.getGroupAdmin().getNationality(), group.getGroupAdmin().getHeight(),
                    group.getGroupAdmin().getWeight(), group.getGroupAdmin().getName(), group.getToExpelAmount(), group.getExpelledStudentsAmount()
            );
            message.add(String.format("Group with id '%s' was successfully updated", group.getId()));
        }
        return message;
    }

    public ArrayList<String> remove_by_id(Long id) {
        message.clear();
        boolean foundFlag = false;
        for (StudyGroup studyGroup : groups) {
            if (studyGroup.getId().equals(id)) {
                groups.remove(studyGroup);
                foundFlag = true;
                this.databaseManager.deleteById(id);
                message.add(String.format("Group with id '%s' was deleted", id));
                break;
            }
        }
        if (!foundFlag) {
            message.add("Element with given id doesn't exist!");
        }
        return message;
    }

    public ArrayList<String> clearCollection() {
        message.clear();
        this.databaseManager.clearAll();
        groups.clear();
        message.add("Collection cleared.");
        return message;
    }

//    private void saveCollection() {
//
//        try {
//
//            JSONArray groupCollection = new JSONArray();
//
//            for (StudyGroup studyGroup : groups) {
//                JSONObject group = new JSONObject();
//                JSONObject coordinatesCollections = new JSONObject();
//                JSONObject adminCollection = new JSONObject();
//
//                group.put("id", studyGroup.getId());
//
//                group.put("studentsCount", studyGroup.getStudentsCount());
//                group.put("name", studyGroup.getName());
//
//                coordinatesCollections.put("x", studyGroup.getCoordinates().getX());
//                coordinatesCollections.put("y", studyGroup.getCoordinates().getY());
//
//                group.put("coordinates", coordinatesCollections);
//                group.put("expelledStudents", studyGroup.getExpelledStudentsAmount());
//                group.put("shouldBeExpelled", studyGroup.getToExpelAmount());
//                group.put("semester", String.format("%s", studyGroup.getSemester()));
//
//                adminCollection.put("nameGroupAdmin", studyGroup.getGroupAdmin().getName());
//                adminCollection.put("height", studyGroup.getGroupAdmin().getHeight());
//                adminCollection.put("weight", studyGroup.getGroupAdmin().getWeight());
//                adminCollection.put("country", String.format("%s", studyGroup.getGroupAdmin().getNationality()));
//
//                group.put("admin", adminCollection);
//
//                groupCollection.add(group);
//            }
//
//            String prettifiedJSON = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(groupCollection);
//
//            FileWriter fileWriter = new FileWriter(this.collectionsJSONFilePath);
//            fileWriter.write(prettifiedJSON);
//            fileWriter.flush();
//
//        } catch (FileNotFoundException e){
//            logger.error("File not found");
//        } catch (IOException e) {
//            logger.error("Output error");
//        }
//        logger.info("Collection saved.");
//    }

    private void loadCollection() {
        this.groups = this.databaseManager.getAllGroups();
        unsavedGroups = new PriorityQueue<>();
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

    public ArrayList<String> remove_first() {
        message.clear();
        try {
            if (groups.isEmpty()) {
                throw new NoSuchElementException();
            }
            StudyGroup studyGroup = groups.poll();
            this.databaseManager.deleteById(studyGroup.getId());
            message.add("Element removed.");
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
            message.add(String.format("Element is output: %s", groups.peek()));
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

    public void setAuthLib(UserAuthentication authLib) {
        this.authLib = authLib;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}
