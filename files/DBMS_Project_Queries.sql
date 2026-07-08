USE Dbms_proj;

CREATE TABLE Person (
    PersonID INT AUTO_INCREMENT PRIMARY KEY,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    PersonType ENUM('Parent', 'Child', 'Guardian') NOT NULL
);

CREATE TABLE Address (
    AddressID INT AUTO_INCREMENT PRIMARY KEY,
    Address VARCHAR(100),
    City VARCHAR(50),
    State VARCHAR(50),
    Country VARCHAR(50)
);

CREATE TABLE Parent (
    ParentID INT AUTO_INCREMENT PRIMARY KEY,
    PersonID INT,
    Email VARCHAR(100),
    AddressID INT,
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID),
    FOREIGN KEY (AddressID) REFERENCES Address(AddressID)
);

CREATE TABLE Child (
    ChildID INT AUTO_INCREMENT PRIMARY KEY,
    PersonID INT,
    DOB DATE,
    DistinguishingMark VARCHAR(255),
    Status VARCHAR(20),
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID)
);

CREATE TABLE Guardian (
    GuardianID INT AUTO_INCREMENT PRIMARY KEY,
    PersonID INT,
    RelationshipToChild VARCHAR(50),
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID)
);

CREATE TABLE Location (
    LocationID INT AUTO_INCREMENT PRIMARY KEY,
    PlaceName VARCHAR(100),
    City VARCHAR(50),
    State VARCHAR(50),
    Country VARCHAR(50),
    Latitude DECIMAL(10,6),
    Longitude DECIMAL(10,6)
);

CREATE TABLE DNA_Profile (
    DNAID INT AUTO_INCREMENT PRIMARY KEY,
    ChildID INT,
    ParentID INT,
    SampleData VARCHAR(255),
    DNASequence TEXT,
    FOREIGN KEY (ChildID) REFERENCES Child(ChildID),
    FOREIGN KEY (ParentID) REFERENCES Parent(ParentID)
);

CREATE TABLE Lost_Case (
    CaseID INT AUTO_INCREMENT PRIMARY KEY,
    CaseStatus VARCHAR(50),
    Description VARCHAR(255),
    DateReported DATE,
    ChildID INT,
    ParentID INT,
    LocationID INT,
    FOREIGN KEY (ChildID) REFERENCES Child(ChildID),
    FOREIGN KEY (ParentID) REFERENCES Parent(ParentID),
    FOREIGN KEY (LocationID) REFERENCES Location(LocationID)
);

CREATE TABLE Found_Record (
    MatchID INT AUTO_INCREMENT PRIMARY KEY,
    MatchDate DATE,
    MatchAccuracy DECIMAL(5,2),
    Remarks VARCHAR(255),
    LocationFound INT,
    DNAID INT,
    ChildID INT,
    FOREIGN KEY (LocationFound) REFERENCES Location(LocationID),
    FOREIGN KEY (DNAID) REFERENCES DNA_Profile(DNAID),
    FOREIGN KEY (ChildID) REFERENCES Child(ChildID)
);

-- ================= QUERIES =================

-- 1. Insert a new person
INSERT INTO person (FirstName, LastName, Role, Email)
VALUES ('Ramesh', 'Kumar', 'parent', 'ramesh.kumar@gmail.com');

-- 2. Insert parent address
SELECT * FROM address;

-- 3. Insert a new child
INSERT INTO person (FirstName, LastName, Role)
VALUES ('Ananya', 'Kumar', 'child');

select * from child;

SELECT * FROM PERSON;

-- 4. Show all persons with not null email
select * from person where email is not null;

-- 5. Show all parents with address
SELECT p.FirstName, p.LastName, a.Address, a.City, a.State, a.Country
FROM parent pa
JOIN person p ON pa.PersonID = p.PersonID
JOIN address a ON pa.AddressID = a.AddressID;

-- 6. Count total parents
SELECT COUNT(*) AS TotalParents FROM person WHERE Role = 'parent';

-- 7. Find parents by city
SELECT p.FirstName, p.LastName, a.City
FROM parent pa
JOIN person p ON pa.PersonID = p.PersonID
JOIN address a ON pa.AddressID = a.AddressID
WHERE a.City = 'Pune';

-- 8. Show missing children
SELECT p.FirstName, p.LastName, c.DOB, c.DistinguishingMark
FROM child c
JOIN person p ON c.PersonID = p.PersonID
WHERE c.Status = 'Missing';

-- 9. Count missing children
SELECT COUNT(*) AS MissingChildren FROM child WHERE Status = 'Missing';

-- 10. Find children with "scar" in distinguishing mark
SELECT p.FirstName, p.LastName, c.DistinguishingMark
FROM child c
JOIN person p ON c.PersonID = p.PersonID
WHERE c.DistinguishingMark LIKE '%scar%';

-- 11. Show children with DNA profile
SELECT p.FirstName, p.LastName, dp.DNASequence
FROM child c
JOIN person p ON c.PersonID = p.PersonID
JOIN dna_profile dp ON dp.ChildID = c.ChildID;

-- 12. Show parents with DNA profile
SELECT p.FirstName, p.LastName, dp.DNASequence
FROM parent pa
JOIN person p ON pa.PersonID = p.PersonID
JOIN dna_profile dp ON dp.ParentID = pa.ParentID;

-- 13. Children with DNA match above 70%
SELECT p.FirstName, p.LastName, f.MatchAccuracy
FROM found_record f
JOIN child c ON f.ChildID = c.ChildID
JOIN person p ON c.PersonID = p.PersonID
WHERE f.MatchAccuracy > 70;

-- 14. Show found matches
SELECT f.MatchID, p.FirstName, p.LastName, f.MatchAccuracy, f.MatchDate
FROM found_record f
JOIN child c ON f.ChildID = c.ChildID
JOIN person p ON c.PersonID = p.PersonID;

-- 15. Show all lost cases
SELECT lc.CaseID, p.FirstName, p.LastName, lc.CaseStatus, lc.DateReported
FROM lost_case lc
JOIN child c ON lc.ChildID = c.ChildID
JOIN person p ON c.PersonID = p.PersonID;

-- 16. Count open cases
SELECT COUNT(*) AS OpenCases FROM lost_case WHERE CaseStatus = 'Open';

-- 17. Update a case to "Closed"
UPDATE lost_case SET CaseStatus = 'Closed' WHERE CaseID = 1;

-- 18. Show all closed cases
SELECT * FROM lost_case WHERE CaseStatus = 'Closed';

-- 19. Insert a sample location
INSERT INTO location (PlaceName, City, State, Country, Latitude, Longitude)
VALUES ('Phoenix Mall', 'Pune', 'Maharashtra', 'India', 18.5204, 73.8567);

select * from location;

-- 20. Found children
select * from found_record;

-- 21. Count of missing children by city
SELECT l.City, COUNT(*) AS MissingCount
FROM lost_case lc
JOIN child c ON lc.ChildID = c.ChildID
JOIN location l ON lc.LocationID = l.LocationID
WHERE c.Status = 'Missing'
GROUP BY l.City;

-- 22. Show all DNA profiles with owner type
SELECT DNAID,
    CASE
        WHEN ChildID IS NOT NULL THEN 'Child'
        WHEN ParentID IS NOT NULL THEN 'Parent'
    END AS OwnerType,
    DNASequence
FROM dna_profile;

-- 23. Find average age of all children (approximation)
SELECT AVG(TIMESTAMPDIFF(YEAR, c.DOB, CURDATE())) AS AverageAge
FROM child c
WHERE c.DOB IS NOT NULL;

-- 24. Update case description
UPDATE lost_case SET Description = 'Reported missing near school area' WHERE CaseID = 16;

SELECT * FROM LOST_CASE;

-- 25. Show number of cases per status
SELECT CaseStatus, COUNT(*) AS TotalCases
FROM lost_case
GROUP BY CaseStatus;
