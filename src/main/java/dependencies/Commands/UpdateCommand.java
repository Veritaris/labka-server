package dependencies.Commands;

import dependencies.Collection.StudyGroup;
import dependencies.UserAuthorization.User;

import java.util.HashMap;

public class UpdateCommand extends Commands{
    private final User user;
    private StudyGroup studyGroup;


    public UpdateCommand(String name, StudyGroup studyGroup, User user){
        this.name = name;
        this.studyGroup = studyGroup;
        this.user = user;
    }
    @Override
    public HashMap<String, String> execute(){
        return manager.updateStudyGroup(studyGroup, user);
    }
}
