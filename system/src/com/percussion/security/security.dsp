# Microsoft Developer Studio Project File - Name="security" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=security - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "security.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "security.mak" CFG="security - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "security - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "security - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "security - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f security.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "security.exe"
# PROP BASE Bsc_Name "security.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.security"
# PROP Rebuild_Opt "clean"
# PROP Target_File "security.exe"
# PROP Bsc_Name "security.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "security - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f security.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "security.exe"
# PROP BASE Bsc_Name "security.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.security"
# PROP Rebuild_Opt "clean"
# PROP Target_File "security.exe"
# PROP Bsc_Name "security.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "security - Java Virtual Machine Release"
# Name "security - Java Virtual Machine Debug"

!IF  "$(CFG)" == "security - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "security - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSSecurityErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSSecurityProviderMetaData.java
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

SOURCE=.\PSOsProviderMetaDataTest.java
# End Source File
# Begin Source File

SOURCE=.\PSSecurityProviderTest.java
# End Source File
















# PROP Default_Filter ""


# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSAuthenticationFailedException.java
# End Source File
# Begin Source File

SOURCE=.\PSAuthenticationRequiredException.java
# End Source File
# Begin Source File

SOURCE=.\PSAuthenticationUnsupportedException.java
# End Source File
# Begin Source File

SOURCE=.\PSAuthorizationException.java
# End Source File
# Begin Source File

SOURCE=.\PSFiltersNotSupportedException.java
# End Source File
# Begin Source File

SOURCE=.\PSGroupsNotSupportedException.java
# End Source File
# Begin Source File

SOURCE=.\PSNativeMethodException.java
# End Source File
# Begin Source File

SOURCE=.\PSRoleAlreadyDefinedException.java
# End Source File
# Begin Source File

SOURCE=.\PSRoleNotDefinedException.java
# End Source File
# Begin Source File

SOURCE=.\PSUnsupportedProviderException.java
# End Source File
# Begin Source File

SOURCE=.\PSUsersNotSupportedException.java
# End Source File
























































































# PROP Default_Filter ""











# End Group















































































































































































































# Begin Source File

SOURCE=.\PSAclHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSDataEncryptionError.java
# End Source File
# Begin Source File

SOURCE=.\PSDataEncryptionHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSFilterEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSGroupEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSHostAddressFilterEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSHostAddressProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSHostAddressProviderMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSOdbcProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSOdbcProviderMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSOsProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSOsProviderMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSRoleEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSRoleHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSRoleManager.java
# End Source File
# Begin Source File

SOURCE=.\PSSecurityProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSSecurityProviderMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSSecurityProviderPool.java
# End Source File
# Begin Source File

SOURCE=.\PSUserAttributes.java
# End Source File
# Begin Source File

SOURCE=.\PSUserEntry.java
# End Source File
# Begin Source File

SOURCE=.\PSWebServerProvider.java
# End Source File
# Begin Source File

SOURCE=.\PSWebServerProviderMetaData.java
# End Source File
# End Target
# End Project
