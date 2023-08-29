# metadata
This modules contains all the backend support required by DTS for 
* Metadata Indexing
* Cookie Consent services
* Blog Post Visit services 

Read/Write data to/from DB from published pages.
Provides REST services for above actions.

This module also contains logic to implement liquibase to connect to database
* In /src/main/resources/ we can see masterChangeLog.xml and changeLog.xml.
* changeLog.xml contains changes which we want to implement in any of our database tables.
* masterChangeLog.xml is the mail file which should include all the changeLog files.
* In /src/main/java/webapp/WEB-INF/, we have beans.xml which contains bean id metadataLiquibase.
* in beans.xml, we are providing the path to our database connection and path to masterChangeLog.xml.

## Building

```
mvn clean install
```