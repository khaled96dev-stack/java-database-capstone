Section 1 : Architecture Summary:
This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.

Section 2 : Numbered Flow of Data and Control:
1. User Interface: The Admin Dashboard and Doctor Dashboard allow users to interact with the system through Thymeleaf Controllers, while REST Modules communicate through a JSON API.

2. Request Handling: Thymeleaf Controllers and REST Controllers receive user requests and forward them to the Service Layer.

3. Business Logic: The Service Layer processes requests, applies business rules, and coordinates operations between the controllers and repositories.

4. Repository Access: The Service Layer communicates with MySQL Repositories and the MongoDB Repository to retrieve or store data.

5. MySQL Data Management: MySQL Repositories access the MySQL Database, while JPA entities and models represent data such as Patients, Doctors, Appointments, and Admins.

6. MongoDB Data Management: The MongoDB Repository accesses the MongoDB Database, which stores prescription-related documents using MongoDB models.

7. Response Delivery: The Service Layer returns processed results to the controllers. Thymeleaf Controllers display data on dashboards, while REST Controllers return JSON responses to the REST Modules.
