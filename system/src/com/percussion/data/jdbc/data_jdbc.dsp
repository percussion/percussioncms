# Microsoft Developer Studio Project File - Name="data_jdbc" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=data_jdbc - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "data_jdbc.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "data_jdbc.mak" CFG="data_jdbc - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "data_jdbc - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "data_jdbc - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "data_jdbc - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f data_jdbc.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "data_jdbc.exe"
# PROP BASE Bsc_Name "data_jdbc.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.data.jdbc"
# PROP Rebuild_Opt "clean"
# PROP Target_File "data_jdbc.exe"
# PROP Bsc_Name "data_jdbc.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "data_jdbc - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f data_jdbc.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "data_jdbc.exe"
# PROP BASE Bsc_Name "data_jdbc.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "E:\E2\Tools\makeit.bat DEBUG"="1 com.percussion.data.jdbc"
# PROP Rebuild_Opt "clean"
# PROP Target_File "data_jdbc.exe"
# PROP Bsc_Name "data_jdbc.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "data_jdbc - Java Virtual Machine Release"
# Name "data_jdbc - Java Virtual Machine Debug"

!IF  "$(CFG)" == "data_jdbc - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "data_jdbc - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSDriverMetaData.java
# End Source File
# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# PROP Default_Filter ""

# End Group
# Begin Group "Tests"

# Begin Source File

SOURCE=.\PSFileSystemConnectionTest.java
# End Source File
# Begin Source File

SOURCE=.\PSFileSystemStatementTest.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlDatabaseMetaDataTest.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlStatementTest.java
# End Source File




































# PROP Default_Filter ""




# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# End Group













































































































































# Begin Source File

SOURCE=.\PSFileSystemConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSFileSystemDatabaseMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSFileSystemDriver.java
# End Source File
# Begin Source File

SOURCE=.\PSFileSystemDriverMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSFileSystemStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSJdbcDriver.java
# End Source File
# Begin Source File

SOURCE=.\PSJdbcDriverMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSOdbcDriverMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlDatabaseMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlDocumentQuery.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlDriver.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlDriverMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlStatement.java
# End Source File
# End Target
# End Project
