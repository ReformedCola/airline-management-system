# Airline Management System

In this project, we built a database system and an user friendly application using JDBC for psql, which can help travel agency to book flights and track useful information about different airlines, planes, and customers, etc..

## How to Run

**Please download PostgreSQL and Java (JDK) before you run this program**

1. PostgreSQL Scripts
   * Modify the address of csv files and run `createPostgreDB` to initialize database.
   * Run `startPostgreSQL` to start the database.
2. Java
   * Run `java/compile.sh` to compile the code from `src`.
   * Run `java/run.sh` to execute the code from `src` with dbname, port, user. 
   * (Ex: run.sh airline_DB 5432 jasonhe)

## Basic Query

We wrote 9 basic query for this project:

> 1. It can add planes with information user provided.

> 2. It can add pilot with information user provided.

> 3. It can add flight with information user provided.

> 4. It can add technician with information user provided.

> 5. It can book flights.

> 6. It can list the number of available seats for a given flight using the plane's total number of seats minus how many tickets are sold for the flight.

> 7. It can list the total number of repairs per plane in descending order.

> 8. It can list the total number of repairs per year in ascending order.

> 9. It can find the total number of passengers with a given status.