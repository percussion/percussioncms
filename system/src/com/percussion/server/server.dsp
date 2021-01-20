# Microsoft Developer Studio Project File - Name="server" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=server - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "server.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "server.mak" CFG="server - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "server - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "server - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "server - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f server.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "server.exe"
# PROP BASE Bsc_Name "server.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.server"
# PROP Rebuild_Opt "clean"
# PROP Target_File "server.exe"
# PROP Bsc_Name "server.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "server - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f server.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "server.exe"
# PROP BASE Bsc_Name "server.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.server"
# PROP Rebuild_Opt "clean"
# PROP Bsc_Name "server.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "server - Java Virtual Machine Release"
# Name "server - Java Virtual Machine Debug"

!IF  "$(CFG)" == "server - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "server - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSCgiVariables.java
# End Source File
# Begin Source File

SOURCE=.\IPSConsoleCommand.java
# End Source File
# Begin Source File

SOURCE=.\IPSHttpErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSRequestHandler.java
# End Source File
# Begin Source File

SOURCE=.\IPSServerErrors.java
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

SOURCE=.\PSRequestParserTest.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestTest.java
# End Source File
















# PROP Default_Filter ""
# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSConsoleCommandException.java
# End Source File
# Begin Source File

SOURCE=.\PSInvalidRequestTypeException.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestParsingException.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestValidationException.java
# End Source File
































# PROP Default_Filter ""



# End Group








































































































































































































































































































































































































































































































# Begin Source File

SOURCE=.\JDK12InterruptHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSApplicationHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSApplicationStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSConsole.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommand.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandLogDump.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandLogFlush.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandParser.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandRestartApplication.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandRestartServer.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowApplications.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowStatusApplication.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowStatusHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowStatusObjectStore.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowStatusServer.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandShowVersion.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandStartApplication.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandStartServer.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandStopApplication.java
# End Source File
# Begin Source File

SOURCE=.\PSConsoleCommandStopServer.java
# End Source File
# Begin Source File

SOURCE=.\PSFileRequestHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSHandlerStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSHookRequestHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSHttpRequestDispatcher.java
# End Source File
# Begin Source File

SOURCE=.\PSHttpSocketListener.java
# End Source File
# Begin Source File

SOURCE=.\PSObjectStoreListener.java
# End Source File
# Begin Source File

SOURCE=.\PSQueuedRequest.java
# End Source File
# Begin Source File

SOURCE=.\PSRemoteConsole.java
# End Source File
# Begin Source File

SOURCE=.\PSRemoteConsoleHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSRequest.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestPageMap.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestParser.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestQueue.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSResponse.java
# End Source File
# Begin Source File

SOURCE=.\PSServer.java
# End Source File
# Begin Source File

SOURCE=.\PSServerBrand.java
# End Source File
# Begin Source File

SOURCE=.\PSServerEventStats.java
# End Source File
# Begin Source File

SOURCE=.\PSServerLogHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSServerStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSServerThreadStats.java
# End Source File
# Begin Source File

SOURCE=.\PSServerUserStats.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryApplications.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryControlBlock.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryHttpRequestEvent.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryHttpResponse.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryListener.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryRequest.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryRequestEvent.java
# End Source File
# Begin Source File

SOURCE=.\PSSharedMemoryResponse.java
# End Source File
# Begin Source File

SOURCE=.\PSUserSession.java
# End Source File
# Begin Source File

SOURCE=.\PSUserSessionManager.java
# End Source File
# Begin Source File

SOURCE=.\PSUserThread.java
# End Source File
# Begin Source File

SOURCE=.\PSUserThreadPool.java
# End Source File
# End Target
# End Project
