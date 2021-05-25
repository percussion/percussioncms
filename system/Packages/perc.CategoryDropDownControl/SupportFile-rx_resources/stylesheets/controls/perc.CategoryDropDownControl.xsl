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
     perc.CategoryDropDownControl
 -->
 
 
    <psxctl:ControlMeta name="perc.CategoryDropDownControl" dimension="single" choiceset="none">
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
        <psxctl:Param name="multiple" datatype="String" paramtype="generic">
            <psxctl:Description>If set, this boolean attribute allows multiple selections. If not set, the element only permits single selections.</psxctl:Description>
        </psxctl:Param>
        <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
        </psxctl:Param>
        <psxctl:Param name="disabled" datatype="String" paramtype="generic">
            <psxctl:Description>If set, this boolean attribute disables the control for user input.</psxctl:Description>
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
        <psxctl:Param name="parentCategory" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies a parent category name.  Every field that specifies this group name will be updated when any of the other fields in the group are modified.</psxctl:Description>
            <psxctl:DefaultValue>root</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="autoGenerate" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies if the cascaded drop downs are to be auto generated or not. Value will be yes to auto generate no to not auto generate.</psxctl:Description>
            <psxctl:DefaultValue>yes</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="categoryxmlurl" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies the url location of a lookup application conforming to the sys_Lookup.dtd.  The current form values are sent to the query and are available for use in filtering the results.</psxctl:Description>
              <psxctl:DefaultValue>../percCategoryDropDown/sys_Lookup</psxctl:DefaultValue>
        </psxctl:Param>
        <psxctl:Param name="sortOrder" datatype="String" paramtype="generic">
              <psxctl:Description>This parameter specifies the if the options are to be displayed by a specific sort order or not. Values to this parameter can be ascending or descending.</psxctl:Description>
        </psxctl:Param>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="CategoryDropdown.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/categoryDropDown/css/CategoryDropdown.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="categoryDropdown.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/categoryDropDown/js/categoryDropdown.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>
    <!-- Template to display editable and read-only category drop down. -->
    <xsl:template match="Control[@name='perc.CategoryDropDownControl']" mode="psxcontrol">
        <xsl:variable name="parentCategory">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='parentCategory']">
                    <xsl:value-of select="ParamList/Param[@name='parentCategory']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='perc.CategoryDropDownControl']/psxctl:ParamList/psxctl:Param[@name='parentCategory']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="categoryxmlurl">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='categoryxmlurl']">
                    <xsl:value-of select="ParamList/Param[@name='categoryxmlurl']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='perc.CategoryDropDownControl']/psxctl:ParamList/psxctl:Param[@name='categoryxmlurl']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="autoGenerate">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='autoGenerate']">
                    <xsl:value-of select="ParamList/Param[@name='autoGenerate']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='perc.CategoryDropDownControl']/psxctl:ParamList/psxctl:Param[@name='autoGenerate']/psxctl:DefaultValue"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="multiple">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='multiple']">
                    <xsl:value-of select="ParamList/Param[@name='multiple']"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="sortOrder">
            <xsl:choose>
                <xsl:when test="ParamList/Param[@name='sortOrder']">
                    <xsl:value-of select="ParamList/Param[@name='sortOrder']"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="name">
            <xsl:value-of select="@paramName"/>
        </xsl:variable>
        <xsl:variable name="val">
            <xsl:value-of select="Value"/>
        </xsl:variable>
        <script >
        // <![CDATA[
        (function($) {
        $(document).ready(function() {
        // ]]>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'parentCategory'" />
                <xsl:with-param name="value" select="$parentCategory" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'categoryxmlurl'" />
                <xsl:with-param name="value" select="$categoryxmlurl" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'paramName'" />
                <xsl:with-param name="value" select="@paramName" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'readonly'" />
                <xsl:with-param name="value" select="@isReadOnly" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'autoGenerate'" />
                <xsl:with-param name="value" select="$autoGenerate" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'val'" />
                <xsl:with-param name="value" select="$val" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'sortorder'" />
                <xsl:with-param name="value" select="$sortOrder" />
            </xsl:call-template>
        // <![CDATA[

            var siteName = parent.$.PercNavigationManager.getSiteName();

            if (parentCategory.indexOf("{sitename}") !== -1)
            {
                var siteName = parent.$.PercNavigationManager.getSiteName();
                parentCategory = parentCategory.replace("{sitename}",siteName);
            }
           
            var opts = {url : categoryxmlurl, parent : parentCategory, paramName : paramName, autoGenerate : autoGenerate, val : val, readonly : readonly, sortorder : sortorder, sitename : siteName };
            $('#perc-content-form').dropDownControl(opts);
        });
        })(jQuery);
        // ]]>
        </script>
            <div id="maindiv-{@paramName}">
            <input type="hidden" name="{@paramName}" id="perc-categories-json-{@paramName}" value="{Value}" />
            <select id="parent-categories-{@paramName}" name="{@paramName}-Categories" >
                <!-- following attribute required to trigger update -->
                <xsl:if test="$autoGenerate!='yes'">
                    <xsl:attribute name="multiple">1</xsl:attribute>
                </xsl:if>
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
                    <xsl:with-param name="controlClassName" select="'perc.CategoryDropDownControl'"/>
                    <xsl:with-param name="controlNode" select="."/>
                </xsl:call-template>
            </select>
        </div>
    </xsl:template>
    

</xsl:stylesheet>
