# DBImport Application

This Spring Boot application imports and processes data from an included file into a PostgreSQL database. It groups records by `match_id`, processes each group concurrently (while preserving order), and logs the minimum and maximum insertion timestamps.

> **Note:** This implementation currently reads from a static file located in the classpath. For continuous streaming data in production, consider integrating a messaging system and reactive/event-driven processing.

---

## Prerequisites

- **Java 17 or later**
- **Gradle 7+**
- **PostgreSQL** (installed locally or via Docker)

---

## Database Setup

1. **Install PostgreSQL** if not already installed.

2. **Create a New Database:**

   Open a terminal and log into your PostgreSQL server using the credentials specified in the application properties. For example, if your `application.properties` file contains:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```
   then you can create (or verify) the database using the following commands in psql:

   Connect to PostgreSQL (if needed, replace "postgres" with your superuser)
   ```
   psql -U postgres
   ```

   Create the database (if it doesn't exist)
   ```
   CREATE DATABASE postgres;
   ```

   Alternatively, if you wish to use a different database name, update the `spring.datasource.url` property accordingly.

### Included Data File

The sample data file (`fo_random.txt`) is included in the repository under `src/main/resources/data/`.

## Running the application

1. **Clone the repository**
   ```
   git clone https://github.com/pmishko/db-import.git
   cd DBImport
   ```

2. **Run the Application**

   You can run the application using Gradle:

   ```
   ./gradlew bootRun
   ```

3. **Verify the Process**

   **On Startup:**
   The application will process the included data file automatically.

   **Console Logs:**
   Check for messages indicating:
    - The start and completion of file processing.
    - The number of lines read and grouped.
    - The minimum and maximum date_insert values.

   **Database Verification:**
   Use your PostgreSQL client to verify that the records have been inserted as expected.