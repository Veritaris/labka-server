package org.example.server.CommandManager;


import org.example.server.Collection.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("UnusedReturnValue")
public class CommandObject implements Serializable {

    private ArrayList<String> message;
    private StudyGroup studyGroup;
    private String stringArgument;
    private Long longArgument;

    private String username = "";
    private String password = "";

    private boolean isScripted = false;
    private boolean isFailed = false;
    private String failReason;
    private String name;

    public CommandObject(String name, StudyGroup studyGroup){
        this.name = name;
        this.studyGroup = studyGroup;
    }

    public CommandObject(String name, Long longArgument){
        this.name = name;
        this.longArgument = longArgument;
    }

    public CommandObject(String name, String stringArgument){
        this.name = name;
        this.stringArgument = stringArgument;
    }

    public CommandObject(String name){
        this.name = name;
    }

    public void setMessage(ArrayList<String> message){
        this.message = message;
    }

    public ArrayList<String> getMessage() {
        return this.message;
    }

    public String getName() {
        return this.name;
    }

    public StudyGroup getStudyGroup() {
        return this.studyGroup;
    }

    public Long getLongArgument() {
        return this.longArgument;
    }

    public String getStringArgument() {
        return this.stringArgument;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFailed() {
        return this.isFailed;
    }

    public void setIsFailed(boolean isFailed) {
        this.isFailed = isFailed;
    }

    public String getFailReason() {
        return this.failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public void clearMessage() {
        if (this.message != null) {
            this.message = new ArrayList<>();
        }
    }

    public void setSender(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getSenderUsername() {
        return this.username;
    }

    public String getSenderPassword() {
        return this.password;
    }

    @Override
    public String toString(){
        return String.format("CommandObject<" +
                "command=\"%s\"", getName()) +
                ((getStudyGroup() != null) ? String.format(", firstArgument='%s'", getStudyGroup()) : "") +
                ((getLongArgument() != null) ? String.format(", secondArgument='%s'", getLongArgument()) : "") +
                ((getStringArgument() != null) ? String.format(", thirdArgument='%s'", getStringArgument()) : "") +
                ((getMessage() != null) ? String.format(", message='%s'", getMessage()) : "") +
                ">";

    }

    public boolean isScripted() {
        return this.isScripted;
    }

    public void setScripted(boolean scripted) {
        this.isScripted = scripted;
    }
}
