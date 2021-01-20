# Microsoft Developer Studio Project File - Name="data" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Java Virtual Machine External Target" 0x0806

CFG=data - Java Virtual Machine Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "data.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "data.mak" CFG="data - Java Virtual Machine Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "data - Java Virtual Machine Release" (based on\
 "Java Virtual Machine External Target")
!MESSAGE "data - Java Virtual Machine Debug" (based on\
 "Java Virtual Machine External Target")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""

!IF  "$(CFG)" == "data - Java Virtual Machine Release"

# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f data.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "data.exe"
# PROP BASE Bsc_Name "data.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "makeit.bat com.percussion.data"
# PROP Rebuild_Opt "clean"
# PROP Bsc_Name "data.bsc"
# PROP Target_Dir ""

!ELSEIF  "$(CFG)" == "data - Java Virtual Machine Debug"

# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ""
# PROP BASE Intermediate_Dir ""
# PROP BASE Cmd_Line "NMAKE /f data.mak"
# PROP BASE Rebuild_Opt "/a"
# PROP BASE Target_File "data.exe"
# PROP BASE Bsc_Name "data.bsc"
# PROP BASE Target_Dir ""
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ""
# PROP Intermediate_Dir ""
# PROP Cmd_Line "E:\E2\Tools\makeit.bat DEBUG"="1 com.percussion.data"
# PROP Rebuild_Opt "clean"
# PROP Bsc_Name "data.bsc"
# PROP Target_Dir ""

!ENDIF 

# Begin Target

# Name "data - Java Virtual Machine Release"
# Name "data - Java Virtual Machine Debug"

!IF  "$(CFG)" == "data - Java Virtual Machine Release"

!ELSEIF  "$(CFG)" == "data - Java Virtual Machine Debug"

!ENDIF 

# Begin Group "Interfaces"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\IPSBackEndErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSDataErrors.java
# End Source File
# Begin Source File

SOURCE=.\IPSDataExtractor.java
# End Source File
# Begin Source File

SOURCE=.\IPSExecutionStep.java
# End Source File
# Begin Source File

SOURCE=.\IPSResultGenerator.java
# End Source File
# Begin Source File

SOURCE=.\IPSResultSetConverter.java
# End Source File
# Begin Source File

SOURCE=.\IPSStatementBlock.java
# End Source File
# Begin Source File

SOURCE=.\IPSStyleSheetMerger.java
# End Source File
# PROP Default_Filter ""








# PROP Default_Filter ""








# PROP Default_Filter ""








# PROP Default_Filter ""








# PROP Default_Filter ""








# End Group
# Begin Group "Tests"

# Begin Source File

SOURCE=.\PSDataComparisonTest.java
# End Source File
# Begin Source File

SOURCE=.\PSDataConverterTest.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryJoinerTest.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetTest.java
# End Source File
















# PROP Default_Filter ""





# End Group
# Begin Group "Exceptions"

# Begin Source File

SOURCE=.\PSConversionException.java
# End Source File
# Begin Source File

SOURCE=.\PSDataExtractionException.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlException.java
# End Source File
# Begin Source File

SOURCE=.\PSUnsupportedConversionException.java
# End Source File
















# PROP Default_Filter ""




# End Group


















































































































































































































































































































































































# Begin Source File

SOURCE=.\PSBackEndColumnExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSBackEndLogin.java
# End Source File
# Begin Source File

SOURCE=.\PSBinaryData.java
# End Source File
# Begin Source File

SOURCE=.\PSCgiVariableExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSCookieExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSCssStyleSheetMerger.java
# End Source File
# Begin Source File

SOURCE=.\PSDataConverter.java
# End Source File
# Begin Source File

SOURCE=.\PSDataExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSDataExtractorFactory.java
# End Source File
# Begin Source File

SOURCE=.\PSDataHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSDataTypeInfo.java
# End Source File
# Begin Source File

SOURCE=.\PSDatabaseMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSDatabasePoolConnection.java
# End Source File
# Begin Source File

SOURCE=.\PSDatabasePoolManager.java
# End Source File
# Begin Source File

SOURCE=.\PSDateLiteralExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSDtdRelationalMapper.java
# End Source File
# Begin Source File

SOURCE=.\PSExecutionBlock.java
# End Source File
# Begin Source File

SOURCE=.\PSExecutionData.java
# End Source File
# Begin Source File

SOURCE=.\PSHtmlParameterExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSHtmlParameterTree.java
# End Source File
# Begin Source File

SOURCE=.\PSIndexStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSIndexedLookupJoiner.java
# End Source File
# Begin Source File

SOURCE=.\PSJoinTree.java
# End Source File
# Begin Source File

SOURCE=.\PSJoinedRowDataBuffer.java
# End Source File
# Begin Source File

SOURCE=.\PSLiteralExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSLiteralSetExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSLockedUpdateStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSNativeStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSNumericLiteralExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSOptimizer.java
# End Source File
# Begin Source File

SOURCE=.\PSPagedRequestLinkGenerator.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryCacher.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryJoiner.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryOptimizer.java
# End Source File
# Begin Source File

SOURCE=.\PSQueryStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestLinkGenerator.java
# End Source File
# Begin Source File

SOURCE=.\PSRequestRedirector.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSet.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetColumnMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetHtmlConverter.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetMimeConverter.java
# End Source File
# Begin Source File

SOURCE=.\PSResultSetXmlConverter.java
# End Source File
# Begin Source File

SOURCE=.\PSRowDataBuffer.java
# End Source File
# Begin Source File

SOURCE=.\PSSortedResultJoiner.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlBuilderContext.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlDeleteBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlInsertBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlLockedUpdateBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlParser.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlQueryBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlUpdateBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSSqlUpdateInsertBuilder.java
# End Source File
# Begin Source File

SOURCE=.\PSStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSStatementBlock.java
# End Source File
# Begin Source File

SOURCE=.\PSStatementColumn.java
# End Source File
# Begin Source File

SOURCE=.\PSStatementColumnMapper.java
# End Source File
# Begin Source File

SOURCE=.\PSStatementGroup.java
# End Source File
# Begin Source File

SOURCE=.\PSStyleSheet.java
# End Source File
# Begin Source File

SOURCE=.\PSStyleSheetMerger.java
# End Source File
# Begin Source File

SOURCE=.\PSTableMetaData.java
# End Source File
# Begin Source File

SOURCE=.\PSTableStatistics.java
# End Source File
# Begin Source File

SOURCE=.\PSTextLiteralExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSTransactionSet.java
# End Source File
# Begin Source File

SOURCE=.\PSUdfCallExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSUpdateHandler.java
# End Source File
# Begin Source File

SOURCE=.\PSUpdateInsertStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSUpdateOptimizer.java
# End Source File
# Begin Source File

SOURCE=.\PSUpdateStatement.java
# End Source File
# Begin Source File

SOURCE=.\PSUserContextExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSXmlFieldExtractor.java
# End Source File
# Begin Source File

SOURCE=.\PSXslStyleSheetMerger.java
# End Source File
# End Target
# End Project
