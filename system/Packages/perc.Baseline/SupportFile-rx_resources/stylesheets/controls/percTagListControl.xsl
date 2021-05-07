<?xml version="1.0" encoding="UTF-8" ?>
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
        <!ENTITY % HTMLlat1 SYSTEM "../../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template match="/" />
<!--
     percTagListControl
 -->
	<psxctl:ControlMeta name="percTagListControl" dimension="array" choiceset="required">
		<psxctl:Description>The tag list control.</psxctl:Description>
		<psxctl:ParamList>
         <psxctl:Param name="width" datatype="Number" paramtype="generic">
            <psxctl:Description>Defines the width of the input field</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="maxchars" datatype="Number" paramtype="generic">
            <psxctl:Description>The field defines the maximum number of characters that are allowed</psxctl:Description>
         </psxctl:Param>
		</psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="percTagListControl.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/css/percTagListControl.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="jquery.autocomplete.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/jquery.autocomplete.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="percTagListControl.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/js/percTagListControl.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>            
        </psxctl:AssociatedFileList>       
	</psxctl:ControlMeta>
	
	<xsl:template match="Control[@name='percTagListControl']" mode="psxcontrol">

	<script >
            percTagListSource = [
                <xsl:choose>
                    <xsl:when test="DisplayChoices/DisplayEntry/Value = ''">
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each select="DisplayChoices/DisplayEntry">&quot;<xsl:value-of select="Value"/>&quot;<xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            ];
            
    </script>
      <input type="text" name="{concat(@paramName,'-display')}" id="{concat(@paramName,'-display')}" class="percTagListControl">
        <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
      <select type="text" style="display:none" name="{@paramName}"  id="perc-content-edit-{@paramName}" multiple="1">
      </select>
	</xsl:template>
    
    <xsl:template match="Control[@name='percTagListControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <div class="datadisplay">
        <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each>
         <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
            <xsl:if test="@accessKey!=''">
                <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
            </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
        <input type="hidden" name="{concat(@paramName,'-display')}" id="{concat(@paramName,'-display')}" class="percTagListControl">
            <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
             <xsl:if test="@accessKey!=''">
                <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
             </xsl:if>
             <xsl:call-template name="parametersToAttributes">
                <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
                <xsl:with-param name="controlNode" select="."/>
             </xsl:call-template>
        </input>
      </div>
   </xsl:template>
</xsl:stylesheet>
