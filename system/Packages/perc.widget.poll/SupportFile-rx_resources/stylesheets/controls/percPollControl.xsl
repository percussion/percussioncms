<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
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
				<psxctl:FileLocation>/rx_resources/widgets/percPoll/css/percPoll.min.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- JavaScript -->
			<psxctl:FileDescriptor name="jquery-ui.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/libraries/jquery-ui/jquery-ui.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Poll Control JS-->
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PSJSUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-percutils/jquery.percutils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercPollEditor.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/rx_resources/widgets/percPoll/js/PercPollEditor.min.js</psxctl:FileLocation>
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
