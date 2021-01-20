# Microsoft Developer Studio Project File - Name="server_content" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=server_content - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "server_content.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "server_content.mak"\
 CFG="server_content - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "server_content - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "server_content - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "server_content - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f server_content.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "server_content.exe"
# PROP BASE Bsc_Name "server_content.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.server.content"
# PROP Rebuild_Opt "clean"
# PROP Target_File "server_content.exe"
# PROP Bsc_Name "server_content.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "server_content - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f server_content.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "server_content.exe"
# PROP BASE Bsc_Name "server_content.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.server.content"
# PROP Rebuild_Opt "clean"
# PROP Target_File "server_content.exe"
# PROP Bsc_Name "server_content.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "server_content - Java Virtual Machine Release"
# Name "server_content - Java Virtual Machine Debug"

!IF  "$(CFG)" == "server_content - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "server_content - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

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

# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# End Group



























# Begin Source File

SOURCE=.\PSContentParser.java
# End Source File
# Begin Source File

SOURCE=.\PSFormContentParser.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlContentParser.java
# End Source File
# End Target
# End Project
