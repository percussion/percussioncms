# Microsoft Developer Studio Project File - Name="mail" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=mail - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "mail.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "mail.mak" CFG="mail - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "mail - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "mail - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "mail - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f mail.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "mail.exe"
# PROP BASE Bsc_Name "mail.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.mail"
# PROP Rebuild_Opt "clean"
# PROP Target_File "mail.exe"
# PROP Bsc_Name "mail.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "mail - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f mail.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "mail.exe"
# PROP BASE Bsc_Name "mail.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.mail"
# PROP Rebuild_Opt "clean"
# PROP Target_File "mail.exe"
# PROP Bsc_Name "mail.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "mail - Java Virtual Machine Release"
# Name "mail - Java Virtual Machine Debug"

!IF  "$(CFG)" == "mail - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "mail - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSMailErrors.java
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

SOURCE=.\PSMailMessageTest.java
# End Source File




# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSMailSendException.java
# End Source File








# PROP Default_Filter ""
# End Group




























# Begin Source File

SOURCE=.\PSMailMessage.java
# End Source File
# Begin Source File

SOURCE=.\PSMailProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSSmtpMailProvider.java
# End Source File
# End Target
# End Project
