<?xml version="1.0" encoding="UTF-8"?>
<!--This stylesheet consists of templates match on controls with different names and render the fields.
-->
<!DOCTYPE xsl:stylesheet [
	<!ENTITY nbsp "&#160;">
	<!--  no-break space = non-breaking space, U+00A0 ISOnum -->
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<!--
     sys_HiddenInput: needs a higher priority than the default read-only template
  -->
	<xsl:template match="Control[@name='sys_HiddenInput']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" value="{Value}"/>
	</xsl:template>
	<!-- 
     sys_DropDownSingle
  -->
	<xsl:template match="Control[@name='sys_DropDownSingle']" mode="psxcontrol">
		<div class="datadisplay">
			<select name="{@paramName}">
            <xsl:if test="not(@paramName=//KeywordDependencies/KeywordField/@name)">
               <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownsingle">
                  <xsl:with-param name="controlValue" select="Value"/>
                  <xsl:with-param name="paramName" select="@paramName"/>
               </xsl:apply-templates>
            </xsl:if>
			</select>
		</div>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-sysdropdownsingle">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<!-- local/global and external can both be in the same control -->
		<!-- external is assumed to use a DTD compatible with sys_ContentEditor.dtd (items in <DisplayEntry>s) -->
		<xsl:apply-templates select="DisplayEntry" mode="psxcontrol-sysdropdownsingle">
			<xsl:with-param name="controlValue" select="$controlValue"/>
			<xsl:with-param name="paramName" select="$paramName"/>
		</xsl:apply-templates>
		<xsl:if test="string(@href)">
			<xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-sysdropdownsingle">
				<xsl:with-param name="controlValue" select="$controlValue"/>
				<xsl:with-param name="paramName" select="$paramName"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-sysdropdownsingle">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		
      <option value="{Value}">
			<xsl:if test="Value = $controlValue">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@sourceType">
					<xsl:call-template name="getLocaleDisplayLabel">
						<xsl:with-param name="sourceType" select="@sourceType"/>
						<xsl:with-param name="paramName" select="$paramName"/>
						<xsl:with-param name="displayVal" select="DisplayLabel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="DisplayLabel"/>
				</xsl:otherwise>
			</xsl:choose>
		</option>
	</xsl:template>
	<!-- 
     sys__SingleCheckBox
  -->
	<xsl:template match="Control[@name='sys_SingleCheckBox']" priority="10" mode="psxcontrol">
		<xsl:apply-templates select="DisplayChoices/DisplayEntry[position()=1]" mode="psxcontrol-syssinglecheckbox">
			<xsl:with-param name="Control" select="."/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-syssinglecheckbox">
		<xsl:param name="Control"/>
		<input name="{$Control/@paramName}" type="checkbox" value="{Value}">
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
			</xsl:if>
		</input>
		<xsl:choose>
			<xsl:when test="@sourceType">
				<xsl:call-template name="getLocaleDisplayLabel">
					<xsl:with-param name="sourceType" select="@sourceType"/>
					<xsl:with-param name="paramName" select="$Control/@paramName"/>
					<xsl:with-param name="displayVal" select="DisplayLabel"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="DisplayLabel"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- 
     sys_EditBox
-->
	<xsl:template match="Control[@name='sys_EditBox']" mode="psxcontrol">
		<input type="text" name="{@paramName}" value="{Value}" accesskey="{@accessKey}">
		</input>
	</xsl:template>
	<!-- 
     sys_RadioButtons
-->
	<xsl:template match="Control[@name='sys_RadioButtons']" mode="psxcontrol">
		<div class="datadisplay">
			<xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysradiobuttons">
				<xsl:with-param name="controlValue" select="Value"/>
				<xsl:with-param name="paramName" select="@paramName"/>
				<xsl:with-param name="accessKey" select="@accessKey"/>
			</xsl:apply-templates>
		</div>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-sysradiobuttons">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<xsl:param name="accessKey"/>
		<!-- local/global and external can both be in the same control -->
		<!-- external is assumed to use a DTD compatible with sys_ContentEditor.dtd (items in <DisplayEntry>s) -->
		<xsl:apply-templates select="DisplayEntry" mode="psxcontrol-sysradiobuttons">
			<xsl:with-param name="controlValue" select="$controlValue"/>
			<xsl:with-param name="paramName" select="$paramName"/>
			<xsl:with-param name="accessKey" select="$accessKey"/>
		</xsl:apply-templates>
		<xsl:if test="string(@href)">
			<xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-sysradiobuttons">
				<xsl:with-param name="controlValue" select="$controlValue"/>
				<xsl:with-param name="paramName" select="$paramName"/>
				<xsl:with-param name="accessKey" select="$accessKey"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-sysradiobuttons">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<xsl:param name="accessKey"/>
		<input type="radio" name="{$paramName}" value="{Value}" accesskey="{$accessKey}">
			<xsl:if test="Value = $controlValue">
				<xsl:attribute name="checked"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="checked"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@sourceType">
					<xsl:call-template name="getLocaleDisplayLabel">
						<xsl:with-param name="sourceType" select="@sourceType"/>
						<xsl:with-param name="paramName" select="$paramName"/>
						<xsl:with-param name="displayVal" select="DisplayLabel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="DisplayLabel"/>
				</xsl:otherwise>
			</xsl:choose>
		</input>
	</xsl:template>
	<!-- 
     sys_MaxNumberEditBox
-->
	<xsl:template match="Control[@name='sys_MaxNumberEditBox']" mode="psxcontrol">
		<input type="text" name="{@paramName}" value="{Value}" accesskey="{@accessKey}"/>&nbsp;
		<input type="button" name="{concat(@paramName,'_button')}" onclick="javascript:psSearch.modifyMaxNumber();">
			<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.sys_searchTemplates@Unlimited'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
		</input>
	</xsl:template>
	<!-- 
     sys_TextArea
-->
	<xsl:template match="Control[@name='sys_TextArea']" mode="psxcontrol">
		<textarea name="{@paramName}" value="{Value}" accesskey="{@accessKey}">
		</textarea>
	</xsl:template>
	<!-- 
     sys_searchTextOp
-->
	<xsl:template match="Control[@name='sys_searchTextOp']" mode="psxcontrol">
		<table>
			<tr>
				<xsl:if test="not($isFullTextSearch)">
					<td class="controlname">
						<select name="{concat(@paramName,'_op')}" accesskey="{@accessKey}">
							<xsl:for-each select="DisplayChoices/DisplayEntry">
								<option value="{Value}">
									<xsl:if test="@selected='yes'">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="DisplayLabel"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</xsl:if>
				<td>
					<input type="text" name="{concat(@paramName,'_1')}" value="{Value}" accesskey="{@accessKey}"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- 
     sys_searchNumberOp
-->
	<xsl:template match="Control[@name='sys_searchNumberOp']" mode="psxcontrol">
		<table>
			<tr>
				<xsl:if test="not($isFullTextSearch)">
					<td align="right">
						<select name="{concat(@paramName,'_op')}" accesskey="{@accessKey}">
							<xsl:for-each select="DisplayChoices/DisplayEntry">
								<option value="{Value}">
									<xsl:if test="@selected='yes'">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="DisplayLabel"/>
								</option>
							</xsl:for-each>
						</select>
						<br/>
						<font class="datacell1font">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_searchSupport.sys_searchTemplates@AND'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</font>
					</td>
				</xsl:if>
				<td>
					<input type="text" name="{concat(@paramName,'_1')}" value="{Value[1]}" accesskey="{@accessKey}"/>
					<xsl:if test="not($isFullTextSearch)">
						<br/>
						<input type="text" name="{concat(@paramName,'_2')}" value="{Value[2]}" accesskey="{@accessKey}"/>
					</xsl:if>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- 
     sys_searchDateOp
-->
	<xsl:template match="Control[@name='sys_searchDateOp']" mode="psxcontrol">
		<table>
			<tr>
				<xsl:if test="not($isFullTextSearch)">
					<td align="right">
						<select name="{concat(@paramName,'_op')}" accesskey="{@accessKey}">
							<xsl:for-each select="DisplayChoices/DisplayEntry">
								<option value="{Value}">
									<xsl:if test="@selected='yes'">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="DisplayLabel"/>
								</option>
							</xsl:for-each>
						</select>
						<br/>
						<font class="datacell1font">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_searchSupport.sys_searchTemplates@AND'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</font>
					</td>
				</xsl:if>
				<td>
					<xsl:call-template name="calendarRenderer">
						<xsl:with-param name="paramName" select="concat(@paramName,'_1')"/>
						<xsl:with-param name="value" select="Value[1]"/>
						<xsl:with-param name="accessKey" select="@accessKey"/>
					</xsl:call-template>
					<xsl:if test="not($isFullTextSearch)">
						<br/>
						<xsl:call-template name="calendarRenderer">
							<xsl:with-param name="paramName" select="concat(@paramName,'_2')"/>
							<xsl:with-param name="value" select="Value[2]"/>
							<xsl:with-param name="accessKey" select="@accessKey"/>
						</xsl:call-template>
					</xsl:if>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- 
     sys_ListBoxMulti
   -->
	<xsl:template match="Control[@name='sys_ListBoxMulti']" mode="psxcontrol">
	   <xsl:variable name="isReadOnly" select="Control/@isReadOnly"/>
		<table>
			<tr>
				<td>
					<select multiple="true" name="{concat(@paramName,'_1')}" accesskey="{@accessKey}">
						<xsl:variable name="pname" select="@paramName"/>
						<xsl:if test="@isReadOnly='yes'">
						   <xsl:attribute name="disabled"/>
						</xsl:if>
						<xsl:if test="//KeywordDependencies/KeywordField[ParentField=$pname]">
						   <xsl:attribute name="onchange"><xsl:for-each select="//KeywordDependencies/KeywordField[ParentField=$pname]"><xsl:value-of select="concat('psSearch.',@name)"/><xsl:text>_change();</xsl:text></xsl:for-each></xsl:attribute>
						</xsl:if>
                  <xsl:if test="not(//KeywordDependencies/KeywordField[@name=$pname])">
                     <xsl:for-each select="DisplayChoices/DisplayEntry">
                        <option value="{Value}">
                           <xsl:if test="@selected='yes'">
                              <xsl:attribute name="selected"/>
                           </xsl:if>
                           <xsl:value-of select="DisplayLabel"/>
                        </option>
                     </xsl:for-each>
                  </xsl:if>
					</select>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- 
     sys_ListBoxMulti: readonly
   -->
   <xsl:template match="Control[@name='sys_ListBoxMulti' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <xsl:variable name="paramName" select="@paramName"/>
      <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
         <select disabled="" name="{concat($paramName,'_1')}">
            <xsl:attribute name="value"><xsl:value-of select="Value"/></xsl:attribute>
            <option>
               <xsl:attribute name="value"><xsl:value-of select="Value"/></xsl:attribute>
               <xsl:attribute name="selected"/>
               <xsl:value-of select="DisplayLabel"/>
            </option>
         </select>
      </xsl:for-each>
   </xsl:template>
	<xsl:template name="calendarRenderer">
		<xsl:param name="paramName"/>
		<xsl:param name="value"/>
		<xsl:param name="accessKey"/>
		<input type="text" name="{$paramName}" value="{$value}" accesskey="{$accessKey}"/>&#160;
		<xsl:variable name="appendtemp">
			<xsl:choose>
				<xsl:when test="$isFullTextSearch">1</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a href="javascript:doNothing()">
			<xsl:attribute name="onclick"><xsl:value-of select="concat('showCalendar(document.',$formname,'.',$paramName,',','0,',$appendtemp,');')"/></xsl:attribute>
			<img border="0" src="../sys_resources/images/cal.gif" height="20" width="20">
				<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.sys_searchTemplates@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
			</img>
		</a>
	</xsl:template>
	<xsl:template name="parametersToAttributes"/>
	<xsl:template name="getLocaleDisplayLabel"/>
	<psxi18n:lookupkeys>
		<key name="psx.sys_searchSupport.sys_searchTemplates@AND">Text appears between to value boxes.</key>
		<key name="psx.sys_searchSupport.sys_searchTemplates@Unlimited">Unlimited check box button label.</key>
		<key name="psx.sys_searchSupport.sys_searchTemplates.alt@Calendar Pop-up">Alt text for calendar pop up image.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
