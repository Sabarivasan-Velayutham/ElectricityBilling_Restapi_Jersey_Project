# Electricity Management System - REST API

This project is a Java-based web application designed to manage electricity usage and billing for users. The application uses the Jersey framework to create RESTful web services for user management, billing, and authentication features.

## Features

- **User Management**: Create, read, update, and delete user information through RESTful endpoints.
- **Billing Management**: Check and update the billing status for users.
- **Token-based Authentication**: Secure authentication using JSON Web Tokens (JWT).
- **Authorization**: Verify user permissions using custom annotations.
- **Database Integration**: Interact with the database to store and retrieve user information.

## Technologies Used

- Jersey (REST API framework)
- JDBC for database interaction
- JWT for token-based authentication
- Jackson for JSON processing
- Maven for project management
- PostgreSQL as the database
- Apache Tomcat for running the application

## Installation and Setup

1. **Clone the repository**:

    ```bash
    git clone https://github.com/Sabarivasan-Velayutham/ElectricityBilling_Restapi_Jersey_Project.git
    ```

2. **Navigate to the project directory**:

    ```bash
    cd electricity-management-system
    ```

3. **Configure the database**:
    - Ensure that PostgreSQL is installed and running.
    - Update the database connection settings in your project configuration.

4. **Create the required tables in the database**:
    - Connect to your PostgreSQL database and execute the following SQL commands to create the `admininfo` and `userinfo` tables:

        ```sql
        CREATE TABLE admininfo (
            id SERIAL PRIMARY KEY,
            username VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL
        );

        CREATE TABLE userinfo (
            id SERIAL PRIMARY KEY,
            username VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            address VARCHAR(255),
            billamount DOUBLE PRECISION,
            billstatus VARCHAR(50)
        );
        ```

5. **Build the project**:

    ```bash
    mvn clean install
    ```

6. **Deploy the application**:
    - Deploy the application to your servlet container (e.g., Apache Tomcat) using your preferred method.

7. **Access the REST API**:
    - Access the REST API endpoints at `http://localhost:8080/electricity/webapi/v1/`.

## Token-based Authentication

- **Login**: Use the `/login` endpoint with a POST request to obtain a JSON Web Token (JWT) using admin or user credentials.
- **Authentication**: Include the JWT in the `Authorization` header as a Bearer token in your requests to secure endpoints.
- **Token Validation**: Custom filters ensure the validity and authenticity of the token before processing requests.
