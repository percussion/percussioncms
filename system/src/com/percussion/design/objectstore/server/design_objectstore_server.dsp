# Microsoft Developer Studio Project File - Name="design_objectstore_server" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=design_objectstore_server - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "design_objectstore_server.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "design_objectstore_server.mak"\
 CFG="design_objectstore_server - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "design_objectstore_server - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "design_objectstore_server - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "design_objectstore_server - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f design_objectstore_server.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "design_objectstore_server.exe"
# PROP BASE Bsc_Name "design_objectstore_server.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.design.objectstore.server"
# PROP Rebuild_Opt "clean"
# PROP Target_File "design_objectstore_server.exe"
# PROP Bsc_Name "design_objectstore_server.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "design_objectstore_server - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f design_objectstore_server.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "design_objectstore_server.exe"
# PROP BASE Bsc_Name "design_objectstore_server.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.design.objectstore.server"
# PROP Rebuild_Opt "clean"
# PROP Target_File "design_objectstore_server.exe"
# PROP Bsc_Name "design_objectstore_server.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "design_objectstore_server - Java Virtual Machine Release"
# Name "design_objectstore_server - Java Virtual Machine Debug"

!IF  "$(CFG)" == "design_objectstore_server - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "design_objectstore_server - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSApplicationListener.java
# End Source File
# Begin Source File

SOURCE=.\IPSLockerId.java
# End Source File
# Begin Source File

SOURCE=.\IPSObjectStoreHandler.java
# End Source File
# Begin Source File

SOURCE=.\IPSObjectStoreLockManager.java
# End Source File
# Begin Source File

SOURCE=.\IPSServerConfigurationListener.java
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

SOURCE=.\PSXmlObjectStoreHandlerTest.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlObjectStoreLockManagerTest.java
# End Source File


















# PROP Default_Filter ""


# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSLockAcquisitionException.java
# End Source File









# PROP Default_Filter ""

# End Group






















































































# Begin Source File

SOURCE=.\PSApplicationSummary.java
# End Source File
# Begin Source File

SOURCE=.\PSApplicationSummaryCollection.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndObjectStoreHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSObjectStoreStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSSecuredMethod.java
# End Source File
# Begin Source File

SOURCE=.\PSValidatorAdapter.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlObjectStoreHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlObjectStoreLockManager.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlObjectStoreLockerId.java
# End Source File
# End Target
# End Project
