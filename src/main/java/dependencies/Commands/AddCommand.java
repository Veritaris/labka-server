package dependencies.Commands;


import dependencies.Collection.StudyGroup;

import java.util.ArrayList;
import java.util.HashMap;

public class AddCommand extends Commands {
    private StudyGroup studyGroup;
    private String author;

    public AddCommand(String name, StudyGroup studyGroup, String author) {
        this.name = name;
        this.studyGroup = studyGroup;
        this.author = author;
    }

    @Override
    public HashMap<String, String> execute() {
        return manager.addStudyGroup(studyGroup, this.author);
    }
}
