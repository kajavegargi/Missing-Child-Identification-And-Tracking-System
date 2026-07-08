# Missing Child Identification & Tracking System

A relational database system for tracking missing child cases, linking children, parents, guardians, DNA samples, and locations — with a Java Swing desktop interface for data entry, case management, and DNA-based matching.

## Problem Statement

Missing child cases are currently tracked manually or through disconnected databases maintained by different agencies, which leads to:

- Duplication of data and incomplete child records
- Slow matching between reported missing and found children
- Lack of a central reference linking children, parents, DNA samples, and locations
- Inability to quickly identify biological relations when only DNA samples are available

This project addresses these gaps with a relational database — enforced with constraints and triggers for data consistency — paired with a Java desktop application for end-to-end case management, from initial report to case closure.

## Features

- **Person management** — add and track Parents, Children, and Guardians
- **Missing child reporting** — report a new missing case or flag an existing child as missing
- **DNA-based matching** — compare a child's DNA sequence against parent records to identify the closest biological match, with a computed match accuracy score
- **Case tracking** — view, update, and close cases; track status as Open, Under Investigation, or Closed
- **Location tracking** — log locations tied to reports and found records
- **Reporting queries** — missing children by city, average child age, case counts by status, and more

## Tech Stack

- **Database:** MySQL
- **Application:** Java (Swing GUI, JDBC)

## Database Schema

| Table | Purpose |
|---|---|
| `Person` | Core identity record for every individual (parent, child, or guardian) |
| `Address` | Residential address details |
| `Parent` | Parent-specific details, linked to `Person` and `Address` |
| `Child` | Child-specific details — DOB, distinguishing marks, missing status |
| `Guardian` | Guardian details and relationship to child |
| `Location` | Named locations with coordinates, used in reports and found records |
| `DNA_Profile` | DNA sample data linked to either a child or a parent |
| `Lost_Case` | A missing child case — status, description, date reported |
| `Found_Record` | A found/match record with match accuracy and location found |

## Repository Structure

```
├── DBMS_Project_Queries.sql   # Table creation statements + all SQL queries
├── MainMenuUI.java            # Main application menu (entry point)
├── AddPersonUI.java           # Add Person form (Child / Parent)
├── ReportMissingUI.java       # Report Missing Child form
├── SearchLostChildUI.java     # Search lost child & top DNA-matched parent
├── UpdateCaseSatus.java       # Update case status form
├── ViewOpenCasesUI.java       # View all open cases
└── README.md
```

## Getting Started

### Prerequisites

- Java JDK 17+ (uses text blocks)
- MySQL Server
- MySQL Connector/J (JDBC driver) on the classpath

### Setup

1. Create the database and tables:
   ```sql
   CREATE DATABASE Dbms_proj;
   USE Dbms_proj;
   -- run DBMS_Project_Queries.sql
   ```
2. Update the DB credentials (`URL`, `USER`, `PASSWORD`) in each Java file to match your local MySQL setup.
3. Compile and run `MainMenu` (in `MainMenuUI.java`) as the application entry point:
   ```bash
   javac Dbms_proj/*.java
   java Dbms_proj.MainMenu
   ```

## Team

**SY Computer Engineering, A2 Batch — 2025-26**

- Gargi Kajave (UCE2024438)
- Riya Lele (UCE2024445)
- Avani Limaye (UCE2024446)
- Shreeya Mandke (UCE2024449)

## License

This project was developed as part of a DBMS Mini Project coursework submission.
