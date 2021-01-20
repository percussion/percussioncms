# Microsoft Developer Studio Project File - Name="conn" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=conn - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "conn.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "conn.mak" CFG="conn - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "conn - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "conn - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "conn - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f conn.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "conn.exe"
# PROP BASE Bsc_Name "conn.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.conn"
# PROP Rebuild_Opt "clean"
# PROP Target_File "conn.exe"
# PROP Bsc_Name "conn.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "conn - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f conn.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "conn.exe"
# PROP BASE Bsc_Name "conn.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "E:\E2\Tools\makeit.bat DEBUG"="1 com.percussion.conn"
# PROP Rebuild_Opt "clean"
# PROP Target_File "conn.exe"
# PROP Bsc_Name "conn.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "conn - Java Virtual Machine Release"
# Name "conn - Java Virtual Machine Debug"

!IF  "$(CFG)" == "conn - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "conn - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSConnection.java
# End Source File
# Begin Source File

SOURCE=.\IPSConnectionErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSTransportListener.java
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

# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSInvalidPortException.java
# End Source File
# Begin Source File

SOURCE=.\PSServerException.java
# End Source File


















# PROP Default_Filter ""


# End Group






























# Begin Source File

SOURCE=.\PSDesignerConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSSocketConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSSocketConnectionListener.java
# End Source File
# End Target
# End Project
