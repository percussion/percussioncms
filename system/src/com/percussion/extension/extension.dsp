# Microsoft Developer Studio Project File - Name="extension" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=extension - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "extension.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "extension.mak" CFG="extension - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "extension - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "extension - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "extension - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f extension.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "extension.exe"
# PROP BASE Bsc_Name "extension.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f extension.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "extension.exe"
# PROP Bsc_Name "extension.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "extension - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f extension.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "extension.exe"
# PROP BASE Bsc_Name "extension.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "NMAKE /f extension.mak"
# PROP Rebuild_Opt "/a"
# PROP Target_File "extension.exe"
# PROP Bsc_Name "extension.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "extension - Java Virtual Machine Release"
# Name "extension - Java Virtual Machine Debug"

!IF  "$(CFG)" == "extension - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "extension - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSExtension.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionDef.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionDefFactory.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionHandler.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionListener.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionManager.java
# End Source File
# Begin Source File

SOURCE=.\IPSExtensionParamDef.java
# End Source File
# Begin Source File

SOURCE=.\IPSUdfProcessor.java
# End Source File
# End Group
# Begin Group "Tests"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\PSExtensionManagerTest.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionRefTest.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptTest.java
# End Source File
# Begin Source File

SOURCE=.\PSTestingExtension.java
# End Source File
# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\PSExtensionException.java
# End Source File
# End Group
# Begin Source File

SOURCE=.\PSExtensionClassLoader.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionDef.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionDefFactory.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionHandlerConfiguration.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionHandlerHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionManager.java
# End Source File
# Begin Source File

SOURCE=.\PSExtensionRef.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaExtensionHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptCallException.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptCompileException.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptExtensionHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptFunction.java
# End Source File
# Begin Source File

SOURCE=.\PSJavaScriptUdfExtension.java
# End Source File
# End Target
# End Project
