[По-русски](doc/README_rus.md)

# Doc-storage

---
*The simplest service for storing documents.*

<img src="/src/main/resources/static/img/background.webp" alt="doc-storage" width="570">

---

Implemented functions:
  1) User authorization (Http-Basic);
  2) Registration of new users with sending email to confirm the account;
  3) Adding a new document to the storage - the user uploads the file and indicates its name and description in a special form;
  4) Opening access to a loaded document for everyone or for a specific user with the rights: viewing, changing, deleting;
  5) Output of lists of loaded or accessed (shared for user) documents with sorting according to the main attributes;
  6) Search for documents (in the title, in the description, by the date of creation);
  7) View (opening) .docx and .pdf files in the browser;
  8) The simplest validation of data entered into the textual fields to exclude malicious scripts and SQL infection.

---

Technologies:

- Programming languages: Java 21 & JavaScript;
- Assembly system: Maven;
- Backend-Framework: Spring Framework 6.2.1 (Web-MVC, Security, Data-JPA, Test);
- Servet Container: Apache Tomcat 11.0.1;
- Database: MSSql Server Express, H2 (tests), Flyway;
- ORM: Hibernate 6.5.3;
- Testing: Junit, Mockito;
- Linters: Checkstyle 10.21.0, Spotbugs 4.8.6.5;
- Frontend-Framework: Twitter Bootstrap;

---

Instructions for launching the application.
This software is necessary to start the application:

Git (guide - https://learn.microsoft.com/ru-ru/devops/develop/git/install-and-set-up-git);

JDK (java SE21+, guide - https://blog.sf.education/ustanovka-jdk-poshagovaya-instrukciya-dlya-novichkov/);

Apache Maven (guide for Windows - https://byanr.com/installation-guides/maven-windows-11/);

Docker & docker-compose (guide for Windows WSL install -  https://learn.microsoft.com/ru-ru/windows/wsl/install).

### Configuration files docker-compose.yml and flyway.conf are required to start the application. Files can be obtained from the developer.

Launch the terminal / powershell, execute the commands alternately, waiting for the completion of each:

```
cd {target directory for downloading the project}

git clone https://github.com/RuslanYapparov/doc-storage.git

cd doc-storage/

mvn install

```

It is necessary to place docker-compose.yml and flyway.conf files in the root folder and perform a containerization launch

```
cp {the directory which contains configuration files}/docker-compose.yml ./

cp {the directory which contains configuration files}/flyway.conf ./

docker compose up
```
At the first launch, it is also necessary to create a database structure, for example, by launching a Flyway-migration with a script using a command
```
mvn flyway:migrate
```
When accessing port 8080 of the current machine, the application interface will be loaded.

---

This application is made in accordance with the specification:

[Тестовое задание Java.docx](doc/%D0%A2%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%BE%D0%B5%20%D0%B7%D0%B0%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5%20Java.docx)