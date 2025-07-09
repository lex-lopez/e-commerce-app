## About this Repository 

This is a full e-commerce app that implements proper authentication

- Requirements
    - You need to set up env variables for connecting to your MySQL, variables are referenced in `application.yaml`

    1. `MYSQL_URL` - recommended to use: `jdbc:mysql://localhost:3306/store?createDatabaseIfNotExist=true`
    2. `MYSQL_USER` - your MySQL user
    3. `MYSQL_PASSWORD` - your MySQL password

- (Optional) if you want to run individual goals from flyway (e.g. `mvn flyway:migrate`) you need to manually update `flyway.conf` file to add:
    1. `FLYWAY_URL` - recommended to use: `jdbc:mysql://localhost:3306/store?createDatabaseIfNotExist=true`
    2. `FLYWAY_USER` - your MySQL user
    3. `FLYWAY_PASSWORD` - your MySQL password