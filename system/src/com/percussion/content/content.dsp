# Microsoft Developer Studio Project File - Name="content" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=content - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "content.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "content.mak" CFG="content - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "content - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "content - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "content - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f content.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "content.exe"
# PROP BASE Bsc_Name "content.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f content.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "content.exe"
# PROP Bsc_Name "content.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "content - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f content.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "content.exe"
# PROP BASE Bsc_Name "content.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f content.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "content.exe"
# PROP Bsc_Name "content.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "content - Java Virtual Machine Release"
# Name "content - Java Virtual Machine Debug"

!IF  "$(CFG)" == "content - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "content - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSMimeContent.java
# End Source File
# Begin Source File

SOURCE=.\IPSMimeContentDescriptor.java
# End Source File
# Begin Source File

SOURCE=.\IPSMimeContentTypes.java
# End Source File
# PROP Default_Filter ""



# End Group
# Begin Group "Tests"

# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\HTMLException.java
# End Source File
# PROP Default_Filter ""

# End Group










# Begin Source File

SOURCE=.\HTMLAttr.java
# End Source File
# Begin Source File

SOURCE=.\HTMLDocumentFragment.java
# End Source File
# Begin Source File

SOURCE=.\HTMLElement.java
# End Source File
# Begin Source File

SOURCE=.\HTMLNamedNodeMap.java
# End Source File
# Begin Source File

SOURCE=.\HTMLNode.java
# End Source File
# Begin Source File

SOURCE=.\HTMLNodeList.java
# End Source File
# Begin Source File

SOURCE=.\HTMLText.java
# End Source File
# Begin Source File

SOURCE=.\PSContentFactory.java
# End Source File
# Begin Source File

SOURCE=.\PSHtmlParser.java
# End Source File
# Begin Source File

SOURCE=.\PSMimeContentAdapter.java
# End Source File
# End Target
# End Project
