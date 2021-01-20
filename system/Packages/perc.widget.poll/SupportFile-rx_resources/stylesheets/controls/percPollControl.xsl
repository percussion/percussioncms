<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:template match="/"/>
	<!--
     percPollControl 
 -->
	<psxctl:ControlMeta name="percPollControl" dimension="single" choiceset="none">
		<psxctl:Description>Provides UI for creating the polls.</psxctl:Description>
		<psxctl:ParamList>
        </psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<!-- CSS -->
			<psxctl:FileDescriptor name="percPolls.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/widgets/percPoll/css/percPoll.min.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- JavaScript -->
			<psxctl:FileDescriptor name="jquery-ui.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/jquery-ui.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Poll Control JS-->
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/Jeditable.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>                     
			<psxctl:FileDescriptor name="PSJSUtils.js" type="script" mimetype="text/javascript">
                            <psxctl:FileLocation>../../cm/jslib/PSJSUtils.js</psxctl:FileLocation>
                            <psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercPollEditor.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/widgets/percPoll/js/PercPollEditor.min.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='percPollControl']" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-poll-answer" id="{@paramName}">
			<div class="perc-poll-question">
				<label for="perc-poll-question" accesskey="" class="perc-required-field">Poll question:</label><br/>
				<input type="text" class="datadisplay" value="" id="perc-poll-question" name="perc-poll-question" size="50"/>
			</div>
			<div class="perc-poll-answer-type">
				<label for="perc-poll-answer-type" accesskey="">Poll question type:</label><br/>
				<input type="radio" class="datadisplay" value="single" id="perc-poll-answer-type-single" name="perc-poll-answer-type"/><span class="perc-poll-label">Single selection</span><span style="display:inline-block;width:20px"/>
				<input type="radio" class="datadisplay" value="multi" id="perc-poll-answer-type-multi" name="perc-poll-answer-type"/><span class="perc-poll-label">Multiple selections</span><br/>
			</div>
			<div class="perc-poll-answers-container">
				<label for="perc-poll-answer-choices" accesskey="">Poll answers:</label><br/>
				<div class="perc-poll-answer-choices">
				</div>
			</div>
		</div>
	</xsl:template>
	<xsl:template match="Control[@name='percPollControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-poll-answer-readonly" id="{@paramName}">
			<div class="perc-poll-question">
				<label for="perc-poll-question" accesskey="">Poll question:</label><br/>
				<div class="datadisplay" value="" id="perc-poll-question" name="perc-poll-question"/>
			</div>
			<div class="perc-poll-answer-type">
				<label for="perc-poll-answer-type" accesskey="">Poll question type:</label><br/>
				<input type="radio" class="datadisplay" value="single" id="perc-poll-answer-type-single" name="perc-poll-answer-type"/><span class="perc-poll-label">Single selection</span><span style="display:inline-block;width:20px"/>
				<input type="radio" class="datadisplay" value="multi" id="perc-poll-answer-type-multi" name="perc-poll-answer-type"/><span class="perc-poll-label">Multiple selections</span><br/>
			</div>
			<div class="perc-poll-answers-container">
				<label for="perc-poll-answer-choices" accesskey="">Poll answers:</label><br/>
				<div class="perc-poll-answer-choices">
				</div>
			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
