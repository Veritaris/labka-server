package org.example.server.DatabaseManager;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class InitCommands {
    private final LinkedHashMap<String, String> commands = new LinkedHashMap<>();

    public InitCommands() {
        this.commands.put("createSchemaSQL", "create schema if not exists s285604;");
        this.commands.put("createStudyGroupTableSQL", "create table if not exists s285604.study_groups(" +
                "id bigint not null," +
                "name varchar(16) not null," +
                "semester varchar(16) not null," +
                "x bigint not null," +
                "y bigint not null," +
                "students_amount smallint not null," +
                "admin_id integer not null," +
                "students_to_expel smallint not null," +
                "expelled_students smallint not null," +
                "creation_date date not null," +
                "primary key (admin_id)" +
                ");");
        this.commands.put("createAdminTableSQL", "create table if not exists s285604.admins(" +
                "id integer not null," +
                "nationality varchar(32) not null," +
                "height numeric(1) not null," +
                "weight numeric(1) not null," +
                "name varchar(24) not null," +
                "foreign key (id) references s285604.study_groups(admin_id) on DELETE cascade" +
                ");");
        this.commands.put ("createUsersTableSQL", "create table if not exists s285604.users(" +
                "id integer not null," +
                "username varchar(32) not null," +
                "password varchar(32) not null," +
                "uuid uuid not null," +
                "primary key (id)" +
                ");");
        this.commands.put("createUsersIdSequenceSQL", "create sequence if not exists s285604.users_id_seq minvalue 0  start 0;");
        this.commands.put("createStudyGroupsIdSequenceSQL", "create sequence if not exists s285604.study_groups_id_seq minvalue 0 start 0;");
        this.commands.put("createAdminsIdSequenceSQL", "create sequence if not exists s285604.admin_id_seq minvalue 0 start 0;");
    }

    public HashMap<String, String> getCommands() {
        return commands;
    }
}
