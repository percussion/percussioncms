# Microsoft Developer Studio Project File - Name="util" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=util - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "util.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "util.mak" CFG="util - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "util - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "util - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "util - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f util.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "util.exe"
# PROP BASE Bsc_Name "util.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.util"
# PROP Rebuild_Opt "clean"
# PROP Target_File "util.exe"
# PROP Bsc_Name "util.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "util - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f util.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "util.exe"
# PROP BASE Bsc_Name "util.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat DEBUG"="1 com.percussion.util"
# PROP Rebuild_Opt "clean"
# PROP Target_File "util.exe"
# PROP Bsc_Name "util.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "util - Java Virtual Machine Release"
# Name "util - Java Virtual Machine Debug"

!IF  "$(CFG)" == "util - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "util - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSUtilErrors.java
# End Source File
# PROP Default_Filter ""

# End Group
# Begin Group "Tests"

# Begin Source File

SOURCE=.\PSBijectionMapTest.java
# End Source File
# Begin Source File

SOURCE=.\PSDateFormatISO8601Test.java
# End Source File
# Begin Source File

SOURCE=.\PSFileFilterTest.java
# End Source File
# Begin Source File

SOURCE=.\PSHashTableFromBundleTest.java
# End Source File
# Begin Source File

SOURCE=.\PSHttpUtilsTest.java
# End Source File
# Begin Source File

SOURCE=.\PSMapClassToObjectTest.java
# End Source File
# Begin Source File

SOURCE=.\PSPatternMatcherTest.java
# End Source File
# Begin Source File

SOURCE=.\PSSortToolTest.java
# End Source File
# PROP Default_Filter ""








# End Group
# Begin Group "Exceptions"

# PROP Default_Filter ""
# End Group

























# Begin Source File

SOURCE=.\PSBase64Decoder.java
# End Source File
# Begin Source File

SOURCE=.\PSBase64Encoder.java
# End Source File
# Begin Source File

SOURCE=.\PSBijectionMap.java
# End Source File
# Begin Source File

SOURCE=.\PSCharSets.java
# End Source File
# Begin Source File

SOURCE=.\PSCollection.java
# End Source File
# Begin Source File

SOURCE=.\PSCountWriter.java
# End Source File
# Begin Source File

SOURCE=.\PSDateFormatHttp.java
# End Source File
# Begin Source File

SOURCE=.\PSDateFormatISO8601.java
# End Source File
# Begin Source File

SOURCE=.\PSDoubleList.java
# End Source File
# Begin Source File

SOURCE=.\PSEntrySet.java
# End Source File
# Begin Source File

SOURCE=.\PSFileFilter.java
# End Source File
# Begin Source File

SOURCE=.\PSFormatVersion.java
# End Source File
# Begin Source File

SOURCE=.\PSHashTableFromBundle.java
# End Source File
# Begin Source File

SOURCE=.\PSHttpUtils.java
# End Source File
# Begin Source File

SOURCE=.\PSInputStreamAdapter.java
# End Source File
# Begin Source File

SOURCE=.\PSInputStreamReader.java
# End Source File
# Begin Source File

SOURCE=.\PSMapClassToObject.java
# End Source File
# Begin Source File

SOURCE=.\PSPatternMatcher.java
# End Source File
# Begin Source File

SOURCE=.\PSProperties.java
# End Source File
# Begin Source File

SOURCE=.\PSPurgableTempFile.java
# End Source File
# Begin Source File

SOURCE=.\PSRandomAccessInputStream.java
# End Source File
# Begin Source File

SOURCE=.\PSRandomAccessOutputStream.java
# End Source File
# Begin Source File

SOURCE=.\PSReaderAdapter.java
# End Source File
# Begin Source File

SOURCE=.\PSSortTool.java
# End Source File
# Begin Source File

SOURCE=.\PSURLEncoder.java
# End Source File
# End Target
# End Project
