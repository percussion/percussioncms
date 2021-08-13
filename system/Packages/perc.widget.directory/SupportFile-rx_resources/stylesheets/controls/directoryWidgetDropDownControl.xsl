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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:psxctl="urn:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
<xsl:template match="/" />
<!--
     directoryWidgetDropDownControl
 -->
 
    <psxctl:ControlMeta name="directoryWidgetDropDownControl" dimension="single" choiceset="none">
        <psxctl:Description>a drop down combo box for selecting a single value</psxctl:Description>
        <psxctl:ParamList>
            <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
        </psxctl:Param>
        <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
        </psxctl:Param>
        <psxctl:Param name="size" datatype="Number" paramtype="generic">
            <psxctl:Description>If the element is presented as a scrolled list box, This parameter specifies the number of rows in the list that should be visible at the same time.</psxctl:Description>
        </psxctl:Param>
          <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
           <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
           <psxctl:DefaultValue>400</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
           <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
           <psxctl:DefaultValue>125</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
              <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="organizationxmlurl" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies the url location of a lookup application conforming to the sys_Lookup.dtd.  The current form values are sent to the query and are available for use in filtering the results.</psxctl:Description>
              <psxctl:DefaultValue>../percDirectorySupport/organization</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="departmentparentxmlurl" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies the url location of a lookup application conforming to the sys_Lookup.dtd.  The current form values are sent to the query and are available for use in filtering the results.</psxctl:Description>
              <psxctl:DefaultValue>../percDirectorySupport/departmentParent</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="departmentxmlurl" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies the url location of a lookup application conforming to the sys_Lookup.dtd.  The current form values are sent to the query and are available for use in filtering the results.</psxctl:Description>
              <psxctl:DefaultValue>../percDirectorySupport/department</psxctl:DefaultValue>
        </psxctl:Param>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="directoryDropdown.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/percDirectory/css/directoryDropdown.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="directoryDropdown.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/percDirectory/js/directoryDropdown.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>
    <xsl:template match="Control[@name='directoryWidgetDropDownControl']" mode="psxcontrol">
        <xsl:variable name="organizationxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='organizationxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='organizationxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='organizationxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="departmentparentxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='departmentparentxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='departmentparentxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='departmentparentxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="departmentxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='departmentxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='departmentxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='departmentxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="name">
            <xsl:value-of select="@paramName"/>
        </xsl:variable>
        <xsl:variable name="departmentVal">
            <xsl:value-of select="Value"/>
        </xsl:variable>
        <script >
        // <![CDATA[
        (function($) {
        $(document).ready(function() {
        // ]]>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'readonly'" />
                <xsl:with-param name="value" select="@isReadOnly" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'organizationxmlurl'" />
                <xsl:with-param name="value" select="$organizationxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentparentxmlurl'" />
                <xsl:with-param name="value" select="$departmentparentxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentxmlurl'" />
                <xsl:with-param name="value" select="$departmentxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'paramName'" />
                <xsl:with-param name="value" select="@paramName" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentVal'" />
                <xsl:with-param name="value" select="$departmentVal" />
            </xsl:call-template>
        // <![CDATA[
            var opts = {orgUrl : organizationxmlurl, deptUrl: departmentxmlurl, readonly: readonly, deptParentUrl: departmentparentxmlurl, paramName : paramName, departmentVal : departmentVal };
            $('#perc-content-form').dropDownControl(opts);
        });
        })(jQuery);
        // ]]>
        </script>
        <div id="maindiv-{@paramName}">
            <select id="{@paramName}" class="datadisplay" name="{@paramName}" value="{Value}">
                <xsl:if test="@accessKey!=''">
                    <xsl:attribute name="accesskey">
                        <xsl:call-template name="getaccesskey">
                            <xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/>
                            <xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/>
                            <xsl:with-param name="paramName" select="@paramName"/>
                            <xsl:with-param name="accessKey" select="@accessKey"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="parametersToAttributes">
                    <xsl:with-param name="controlClassName" select="'directoryWidgetDropDownControl'"/>
                    <xsl:with-param name="controlNode" select="."/>
                </xsl:call-template>
            </select>
        </div>
    </xsl:template>
    <xsl:template match="Control[@name='directoryWidgetDropDownControl' and @isReadOnly='yes']" priority = "10" mode="psxcontrol">
        <xsl:variable name="organizationxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='organizationxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='organizationxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='organizationxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="departmentparentxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='departmentparentxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='departmentparentxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='departmentparentxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="departmentxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='departmentxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='departmentxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='directoryWidgetDropDownControl']/psxctl:ParamList/psxctl:Param[@name='departmentxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="name">
            <xsl:value-of select="@paramName"/>
        </xsl:variable>
        <xsl:variable name="departmentVal">
            <xsl:value-of select="Value"/>
        </xsl:variable>
        <script >
        // <![CDATA[
        (function($) {
        $(document).ready(function() {
        // ]]>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'readonly'" />
                <xsl:with-param name="value" select="@isReadOnly" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'organizationxmlurl'" />
                <xsl:with-param name="value" select="$organizationxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentparentxmlurl'" />
                <xsl:with-param name="value" select="$departmentparentxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentxmlurl'" />
                <xsl:with-param name="value" select="$departmentxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'paramName'" />
                <xsl:with-param name="value" select="@paramName" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'departmentVal'" />
                <xsl:with-param name="value" select="$departmentVal" />
            </xsl:call-template>
        // <![CDATA[
            var opts = {orgUrl : organizationxmlurl, deptUrl: departmentxmlurl, readonly: readonly, deptParentUrl: departmentparentxmlurl, paramName : paramName, departmentVal : departmentVal };
            $('#perc-content-form').dropDownControl(opts);
        });
        })(jQuery);
        // ]]>
        </script>
        <div id="maindiv-{@paramName}">
            <div id="{@paramName}" class="datadisplay" name="{@paramName}" value="{Value}">
                <xsl:if test="@accessKey!=''">
                    <xsl:attribute name="accesskey">
                        <xsl:call-template name="getaccesskey">
                            <xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/>
                            <xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/>
                            <xsl:with-param name="paramName" select="@paramName"/>
                            <xsl:with-param name="accessKey" select="@accessKey"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="parametersToAttributes">
                    <xsl:with-param name="controlClassName" select="'directoryWidgetDropDownControl'"/>
                    <xsl:with-param name="controlNode" select="."/>
                </xsl:call-template>
            </div>
        </div>
    </xsl:template>

</xsl:stylesheet>
