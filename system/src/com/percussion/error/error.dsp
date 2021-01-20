# Microsoft Developer Studio Project File - Name="error" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=error - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "error.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "error.mak" CFG="error - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "error - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "error - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "error - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f error.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "error.exe"
# PROP BASE Bsc_Name "error.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.error"
# PROP Rebuild_Opt "clean"
# PROP Target_File "error.exe"
# PROP Bsc_Name "error.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "error - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f error.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "error.exe"
# PROP BASE Bsc_Name "error.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.error"
# PROP Rebuild_Opt "clean"
# PROP Target_File "error.exe"
# PROP Bsc_Name "error.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "error - Java Virtual Machine Release"
# Name "error - Java Virtual Machine Debug"

!IF  "$(CFG)" == "error - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "error - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSException.java
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

SOURCE=.\PSErrorHandlerTest.java
# End Source File









# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSConnClosedException.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorException.java
# End Source File
# Begin Source File

SOURCE=.\PSEvaluationException.java
# End Source File
# Begin Source File

SOURCE=.\PSException.java
# End Source File
# Begin Source File

SOURCE=.\PSIllegalArgumentException.java
# End Source File
# Begin Source File

SOURCE=.\PSIllegalStateException.java
# End Source File
# Begin Source File

SOURCE=.\PSNativeException.java
# End Source File
# Begin Source File

SOURCE=.\PSRuntimeException.java
# End Source File








































































# PROP Default_Filter ""
# End Group
# Begin Group "Properties"

# PROP Default_Filter "*.properties"


# End Group










































































































































































































































































































































# Begin Source File

SOURCE=.\PSApplicationAuthorizationError.java
# End Source File
# Begin Source File

SOURCE=.\PSApplicationDesignError.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndAuthorizationError.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndError.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndQueryProcessingError.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndServerDownError.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndUpdateProcessingError.java
# End Source File
# Begin Source File

SOURCE=.\PSCatalogRequestError.java
# End Source File
# Begin Source File

SOURCE=.\PSDataConversionError.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorHttpCodes.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorHumanReadableNames.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorManager.java
# End Source File
# Begin Source File

SOURCE=.\PSErrorPagesBundle.properties
# End Source File
# Begin Source File

SOURCE=.\PSErrorStringBundle.properties
# End Source File
# Begin Source File

SOURCE=.\PSFatalError.java
# End Source File
# Begin Source File

SOURCE=.\PSHookRequestError.java
# End Source File
# Begin Source File

SOURCE=.\PSHtmlProcessingError.java
# End Source File
# Begin Source File

SOURCE=.\PSInternalError.java
# End Source File
# Begin Source File

SOURCE=.\PSLargeApplicationRequestQueueError.java
# End Source File
# Begin Source File

SOURCE=.\PSLargeBackEndRequestQueueError.java
# End Source File
# Begin Source File

SOURCE=.\PSLargeRequestQueueError.java
# End Source File
# Begin Source File

SOURCE=.\PSNonFatalError.java
# End Source File
# Begin Source File

SOURCE=.\PSPoorResponseTimeError.java
# End Source File
# Begin Source File

SOURCE=.\PSQueuedNotification.java
# End Source File
# Begin Source File

SOURCE=.\PSRemoteConsoleError.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestHandlerNotFoundError.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestPreProcessingError.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestWaitTooLongError.java
# End Source File
# Begin Source File

SOURCE=.\PSResponseSendError.java
# End Source File
# Begin Source File

SOURCE=.\PSUnknownProcessingError.java
# End Source File
# Begin Source File

SOURCE=.\PSValidationError.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlProcessingError.java
# End Source File
# End Target
# End Project
