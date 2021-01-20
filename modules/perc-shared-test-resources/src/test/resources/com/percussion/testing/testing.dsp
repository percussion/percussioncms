# Microsoft Developer Studio Project File - Name="testing" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=testing - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "testing.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "testing.mak" CFG="testing - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "testing - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "testing - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "testing - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f testing.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "testing.exe"
# PROP BASE Bsc_Name "testing.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.testing"
# PROP Rebuild_Opt "clean"
# PROP Target_File "testing.exe"
# PROP Bsc_Name "testing.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "testing - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f testing.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "testing.exe"
# PROP BASE Bsc_Name "testing.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.testing"
# PROP Rebuild_Opt "clean"
# PROP Target_File "testing.exe"
# PROP Bsc_Name "testing.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "testing - Java Virtual Machine Release"
# Name "testing - Java Virtual Machine Debug"

!IF  "$(CFG)" == "testing - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "testing - Java Virtual Machine Debug"

!ENDIF 


















# Begin Source File

SOURCE=.\PSAllTests.java
# End Source File
# Begin Source File

SOURCE=.\TestErrorCoverage.java
# End Source File
# End Target
# End Project
