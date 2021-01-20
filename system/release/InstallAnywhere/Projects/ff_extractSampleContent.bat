@echo off
rem extract sample content data from a given database and copy it into FF directory for further manufacturing.
rem System.out.println( "Usage:" );
rem System.out.println( "java com.percussion.tablefactory.tools.PSCatalogTableData <props> <tablesToExport> <schemaFile> <dataFile>" );
rem System.out.println( "where:" );
rem System.out.println( "props - path to dbms props file" );
rem System.out.println( "tablesToExport - path to xml file that contains table names to export; simple dtd similar to the table def;" );
rem System.out.println( "i.e.: <tables><table name='tableName'/></tables> " );
rem System.out.println( "note: if this file doesn't exist it creates it by cataloging all the table names in a given schema and exits." );
rem System.out.println( "schemaFile - path to xml file to which schema results are written" );
rem System.out.println( "dataFile - path to xml file to which data results are written" );

echo about to extract sample ff content from the database, please wait..

set root=%1\%2

set classpath=%root%\build\classes;%root%\Tools\log4j\log4j.jar;%root%\Tools\xerces\xercesImpl.jar;%root%\Tools\xerces\xmlParserAPIs.jar;%root%\Tools\saxon\saxon.jar;%root%\jdbc\jtds\jtds.jar;%root%\jdbc\jtds\jtds.jar;%root%\jdbc\oracle9\classes12.jar;%classpath%

java com.percussion.tablefactory.tools.PSCatalogTableData ff_rxrepository.properties ff_tablesToExport.xml ff_sampleSchema.xml ff_sampleData.xml

echo extraction completed

echo Notes: the ff_sampleData.xml is a temporary output file that has to be merged with
echo the \FastForward\SampleContent\Config\Data\RxffSampleTableData.xml
echo from where it is actually being picked up for FastForward manufacturing.
echo ------
echo Warning: The NEXTNUMBER table has to be examined prior to checking in this data file.
echo also you MUST modify the table factory actions setting them to 'r' and set all onTableCreateOnly to 'no'.
echo ------
