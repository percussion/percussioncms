# Microsoft Developer Studio Project File - Name="log" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=log - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "log.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "log.mak" CFG="log - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "log - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "log - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "log - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f log.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "log.exe"
# PROP BASE Bsc_Name "log.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.log"
# PROP Rebuild_Opt "clean"
# PROP Target_File "log.exe"
# PROP Bsc_Name "log.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "log - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f log.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "log.exe"
# PROP BASE Bsc_Name "log.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.log"
# PROP Rebuild_Opt "clean"
# PROP Target_File "log.exe"
# PROP Bsc_Name "log.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "log - Java Virtual Machine Release"
# Name "log - Java Virtual Machine Debug"

!IF  "$(CFG)" == "log - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "log - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSLogReader.java
# End Source File
# Begin Source File

SOURCE=.\IPSLogReaderFilter.java
# End Source File
# Begin Source File

SOURCE=.\IPSLogWriter.java
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



# End Group
# Begin Group "Tests"

# Begin Source File

SOURCE=.\PSLogHandlerTest.java
# End Source File
# Begin Source File

SOURCE=.\PSLogManagerTest.java
# End Source File
















# PROP Default_Filter ""



# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# End Group

























































































































































































































































# Begin Source File

SOURCE=.\PSApplicationLogReaderFilter.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndLogReader.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndLogWriter.java
# End Source File
# Begin Source File

SOURCE=.\PSFileLogReader.java
# End Source File
# Begin Source File

SOURCE=.\PSFileLogReaderWriterPair.java
# End Source File
# Begin Source File

SOURCE=.\PSFileLogWriter.java
# End Source File
# Begin Source File

SOURCE=.\PSLogApplicationStart.java
# End Source File
# Begin Source File

SOURCE=.\PSLogApplicationStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSLogApplicationStop.java
# End Source File
# Begin Source File

SOURCE=.\PSLogBasicUserActivity.java
# End Source File
# Begin Source File

SOURCE=.\PSLogDetailedUserActivity.java
# End Source File
# Begin Source File

SOURCE=.\PSLogEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSLogError.java
# End Source File
# Begin Source File

SOURCE=.\PSLogExecutionPlan.java
# End Source File
# Begin Source File

SOURCE=.\PSLogFullUserActivity.java
# End Source File
# Begin Source File

SOURCE=.\PSLogHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSLogInformation.java
# End Source File
# Begin Source File

SOURCE=.\PSLogManager.java
# End Source File
# Begin Source File

SOURCE=.\PSLogManagerThreadTests.java
# End Source File
# Begin Source File

SOURCE=.\PSLogMultipleHandlers.java
# End Source File
# Begin Source File

SOURCE=.\PSLogQueueThread.java
# End Source File
# Begin Source File

SOURCE=.\PSLogServerStart.java
# End Source File
# Begin Source File

SOURCE=.\PSLogServerStop.java
# End Source File
# Begin Source File

SOURCE=.\PSLogServerWarning.java
# End Source File
# Begin Source File

SOURCE=.\PSLogStringBundle.java
# End Source File
# Begin Source File

SOURCE=.\PSLogSubMessage.java
# End Source File
# Begin Source File

SOURCE=.\PSOutputLogReaderFilter.java
# End Source File
# Begin Source File

SOURCE=.\PSServerLogReaderFilter.java
# End Source File
# End Target
# End Project
