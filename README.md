# TDEvaluation
Evaluation system
### Features

- Authorization and authentication
- User management
- Evaluation management
	 - Report for each user
	 - Enabled / Disable
- Question management
	- Support text and score question type
	- Draggable
- Submitting response

### User roles
- Manager
- Assessor

### Prerequisites
- IntelliJ IDEA
- PostgresSQL
- Java 8+
- sbt

### Installation
1. Config database connection in application.conf
```
default.db.url = "jdbc:postgresql://localhost:5432/evaluation?currentSchema=public"
default.db.user = "postgres"
```
2. Run the project
3. Create database table by go to [http://localhost:9000/setup](http://localhost:9000/setup "http://localhost:9000/setup"), then it should show
`CREATE TABLE SUCCESSFULLY`
4. Finish! default manager account: let try to sign in at [http://localhost:9000/signin](http://localhost:9000/signin "http://localhost:9000/signin")
```
Username: manager
Password: 123456
```

### Todo
- Improve authorization
- Change comment textbox in manager page to rich textbox
- Support multiple response for each question
- Optimize performance
- Handle duplicate unique value without 23505 Postgres SQL exception 
- Testing

### Authors
- KritPeAcH - Initial work

### Acknowledgments
All Tradition Chiang Mai staffs for the great taking care and giving the precious opportunity to learn and develop this software
