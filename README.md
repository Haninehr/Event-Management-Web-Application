# Event Management Web Application

## Overview
This project is a full-stack web application for managing and organizing various events such as conferences, trainings, concerts, scientific days, and cultural events. It was developed as part of the M2 ISII - Ingénierie Web module for the 2025/2026 academic year. The application supports two main user roles: Organizers (who create and manage events) and Participants (who browse, register, and receive updates on events).
The backend is built using a microservices architecture with Spring Boot, ensuring scalability and modularity. The frontend is developed in Angular, providing a modern and interactive user interface. Security is handled via JWT (JSON Web Token) for authentication and authorization.
Key features include event creation with media uploads, participant management, real-time notifications, and dashboards for statistics.


## Features
•For Organizers:<br>

User authentication (signup, signin) with role (Organizer).<br>
Create, update, delete, and manage events (including adding images, videos, or documents like posters and programs).<br>
Manage participant lists and statuses (e.g., accept/refuse registrations).<br>
View global statistics on organized events (e.g., views, registrations).<br>
Send automatic notifications for confirmations, updates, cancellations, and reminders.<br>

•For Participants:<br>

User authentication (signup, signin) with role (Participant).<br>
Browse and search events by criteria (keywords, location, date, etc.).<br>
Register or unregister for events.<br>
Receive notifications for registration status, event updates, or reminders.<br>

## Architecture
The application follows a microservices-based architecture with an API Gateway for routing requests. Communication between services is handled via REST APIs, and service discovery is managed by Eureka Server.

![Full Architecture](Full-Architecture/Full-Architecture01.jpg)
![Microservices](Full-Architecture/Full-Architecture02.jpg)
![Microservices](Full-Architecture/Full-Architecture03.jpg)
![Web Components](Full-Architecture/Full-Architecture04.jpg)
![Web Components](Full-Architecture/Full-Architecture05.jpg)


## Technologies Used

•Backend: Java, Spring Boot, Spring Cloud (Eureka, Gateway), JWT for security, REST APIs.<br>
•Frontend: Angular, TypeScript, HTML/CSS.<br>
•Databases: MySQL (one per microservice).<br>


## Installation and Setup
### Prerequisites

•Java JDK 17+<br>
•Node.js and npm (for Angular)<br>
•MySQL (create databases for each microservice)<br>
•Git<br>

### Steps
•Backend Setup:<br>
Navigate to each microservice directory (e.g., authentication-service, event-service, etc.).<br>
Update application.properties or application.yml with your database credentials.<br>
Build and run each service:<br>
->mvn clean install<br>
->mvn spring-boot:run<br>
Start Eureka Server first (port 8761), then other services.<br>
API Gateway will run on port 8085.<br>

•Frontend Setup:
Navigate to the Angular frontend directory.<br>
Install dependencies:<br>
->npm install<br>
Run the app:
->ng serve<br>
The frontend will be available at http://localhost:4200.<br>

•Database Setup:<br>
Create databases: auth_db, event_db, reg_db, notif_db.<br>

## Author
Rouibah Hanine (2025)

## License
License MIT License – feel free to use, modify, and share!