# Microsoft Developer Studio Project File - Name="ipc" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=ipc - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "ipc.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "ipc.mak" CFG="ipc - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "ipc - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "ipc - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "ipc - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f ipc.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "ipc.exe"
# PROP BASE Bsc_Name "ipc.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f ipc.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "ipc.exe"
# PROP Bsc_Name "ipc.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "ipc - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f ipc.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "ipc.exe"
# PROP BASE Bsc_Name "ipc.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f ipc.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "ipc.exe"
# PROP Bsc_Name "ipc.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "ipc - Java Virtual Machine Release"
# Name "ipc - Java Virtual Machine Debug"

!IF  "$(CFG)" == "ipc - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "ipc - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# End Group
# Begin Group "Tests"

# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\PSIpcObjectNotFoundException.java
# End Source File
# Begin Source File

SOURCE=.\PSIpcOSException.java
# End Source File
# End Group
# Begin Source File

SOURCE=.\PSEventSemaphore.java
# End Source File
# Begin Source File

SOURCE=.\PSMutexSemaphore.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemory.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryInputStream.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryOutputStream.java
# End Source File
# End Target
# End Project
