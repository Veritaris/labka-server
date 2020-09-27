To build project run `mvn clean compile assembly:single`

To run project on local machine follow this steps in project directory:

1. Ensure that you have PostgreSQL installed
2. `mkdir database`
3. `initdb database`
4. `pg_ctl -D database/ -l logs/database.log  start`

Now you have local database running. To stop it use `pg_ctl -D database stop`
