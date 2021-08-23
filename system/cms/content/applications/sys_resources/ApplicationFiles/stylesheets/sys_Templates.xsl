<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: sys_Templates.xsl 1.81 2002/12/07 00:05:27Z bjoginipally Exp $ -->
<!DOCTYPE xsl:stylesheet [
        <!ENTITY nbsp "&#160;">
        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">

   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <!-- write the xml header according to XHTML 1.0 spec -->
   <xsl:output method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="DTD/xhtml1-strict.dtd"/>
   <xsl:variable name="itemLocale" select="/ContentEditor/@itemLocale"/>

   <!--
     provide a shell to test in isolation.
     this template should be ignored when this file is imported

     saxon -o ContentEditor.htm ContentEditor-ph.xml ContentEditor.xsl
  -->
   <xsl:template match="/">
      <html>
         <head>
            <title>Control Library Test</title>
            <script src="/sys_resources/js/textedit.js">;</script>
         </head>
         <body>
            <xsl:for-each select="/ContentEditor/ItemContent/DisplayField">
               <p>
                  <b>
                     <xsl:value-of select="Label"/>
                  </b>
                  <xsl:apply-templates select="Control" mode="psxcontrol"/>
               </p>
            </xsl:for-each>
         </body>
      </html>
   </xsl:template>
   <!--
     default template for read-only controls; just return the value
  -->
   <xsl:template match="Control[@isReadOnly='yes']" priority="5" mode="psxcontrol">
      <div class="datadisplay">
         <xsl:choose>
            <xsl:when test="string-length(Value)">
               <xsl:value-of select="Value"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'&nbsp;'"/>
            </xsl:otherwise>
         </xsl:choose>
      </div>
      <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
   </xsl:template>
   <!--
     generic templates for turning <ParamList> into attributes
  -->
   <xsl:template name="parametersToAttributes">
      <xsl:param name="controlClassName"/>
      <xsl:param name="controlNode"/>
      <xsl:param name="paramType" select="'generic'"/>
      <xsl:param name="source" select="document('')"/>
      <!-- apply any control parameter defaults defined in the metadata -->
      <xsl:apply-templates select="$source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType and psxctl:DefaultValue]" mode="internal">
         <xsl:with-param name="controlClassName" select="$controlClassName"/>
      </xsl:apply-templates>
      <!-- apply control parameters that have been defined in the metadata (will override defaults) -->
      <xsl:apply-templates select="$controlNode/ParamList/Param[@name = $source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType]/@name]" mode="internal">
         <xsl:with-param name="controlname" select="$controlNode/@paramName"/>
      </xsl:apply-templates>
   </xsl:template>
   <xsl:template match="ParamList/Param[@name='alt']" mode="internal" priority="10">
      <xsl:param name="controlname"/>
      <xsl:variable name="keyval">
         <xsl:choose>
            <xsl:when test="@sourceType='sys_system'">
               <xsl:value-of select="concat('psx.ce.system.', $controlname, '.alt@', .)"/>
            </xsl:when>
            <xsl:when test="@sourceType='sys_shared'">
               <xsl:value-of select="concat('psx.ce.shared.', $controlname, '.alt@', .)"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $controlname, '.alt@', .)"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="{@name}"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="$keyval"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
   </xsl:template>
   <xsl:template match="ParamList/Param" mode="internal" priority="5">
      <xsl:attribute name="{@name}"><xsl:value-of select="."/></xsl:attribute>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param[@name='alt']" mode="internal" priority="10">
      <xsl:param name="controlClassName"/>
      <xsl:attribute name="{@name}"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="concat('psx.contenteditor.sys_templates.',$controlClassName,'.alt@',psxctl:DefaultValue)"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param" mode="internal" priority="5">
      <xsl:attribute name="{@name}"><xsl:value-of select="psxctl:DefaultValue"/></xsl:attribute>
   </xsl:template>
   <!--
     generic templates for turning a <ParamList/Param> into a value
  -->
   <xsl:template name="parameterToValue">
      <xsl:param name="controlClassName"/>
      <xsl:param name="controlNode"/>
      <xsl:param name="paramName"/>
      <xsl:param name="source" select="document('')"/>
      <xsl:choose>
         <xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
            <!-- apply control parameters that have been defined in the metadata (will override defaults) -->
            <xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
         </xsl:when>
         <xsl:otherwise>
            <!-- apply any control parameter defaults defined in the metadata -->
            <xsl:apply-templates select="$source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="internal-value"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="ParamList/Param" mode="internal-value">
      <xsl:value-of select="."/>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param" mode="internal-value">
      <xsl:value-of select="psxctl:DefaultValue"/>
   </xsl:template>
   <xsl:template name="getLocaleDisplayLabel">
      <xsl:param name="displayVal"/>
      <xsl:param name="sourceType"/>
      <xsl:param name="paramName"/>
      <xsl:variable name="keyval">
         <xsl:choose>
            <xsl:when test="$sourceType='sys_system'">
               <xsl:value-of select="concat('psx.ce.system.', $paramName, '@', $displayVal)"/>
            </xsl:when>
            <xsl:when test="@sourceType='sys_shared'">
               <xsl:value-of select="concat('psx.ce.shared.', $paramName, '@', $displayVal)"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $paramName,             '@', $displayVal)"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="getLocaleString">
         <xsl:with-param name="key" select="$keyval"/>
         <xsl:with-param name="lang" select="$lang"/>
      </xsl:call-template>
   </xsl:template>
   <!--
    generic templates for turning <ParamList> into a JSON object.
 -->
   <xsl:template name="parametersToJSON">
      <xsl:param name="controlClassName"/>
      <xsl:param name="controlNode"/>
      <xsl:param name="paramType" select="'generic'"/>
      <xsl:param name="source" select="document('')"/>
      <!-- apply any control parameter defaults defined in the metadata -->
      <xsl:text>{</xsl:text>
      <xsl:apply-templates select="$source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType and psxctl:DefaultValue]" mode="internal-json">
         <xsl:with-param name="controlClassName" select="$controlClassName"/>
      </xsl:apply-templates>
      <!-- apply control parameters that have been defined in the metadata (will override defaults) -->
      <xsl:apply-templates select="$controlNode/ParamList/Param[@name = $source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType]/@name]" mode="internal-json">
         <xsl:with-param name="controlname" select="$controlNode/@paramName"/>
      </xsl:apply-templates>
      <xsl:text>dummy : 0</xsl:text>
      <xsl:text>}</xsl:text>
   </xsl:template>
   <xsl:template match="ParamList/Param[@name='alt']" mode="internal-json" priority="10">
      <xsl:param name="controlname"/>
      <xsl:variable name="keyval">
         <xsl:choose>
            <xsl:when test="@sourceType='sys_system'">
               <xsl:value-of select="concat('psx.ce.system.', $controlname, '.alt@', .)"/>
            </xsl:when>
            <xsl:when test="@sourceType='sys_shared'">
               <xsl:value-of select="concat('psx.ce.shared.', $controlname, '.alt@', .)"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $controlname, '.alt@', .)"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="@name"/><xsl:text>:'</xsl:text><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="$keyval"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template><xsl:text>',</xsl:text>
   </xsl:template>
   <xsl:template match="ParamList/Param" mode="internal-json" priority="5">
      <xsl:value-of select="@name" />:'<xsl:value-of select="."/>',
   </xsl:template>
   <!-- Ignore the class parameter as it breaks in IE (reserved js keyword) -->
   <xsl:template match="psxctl:ParamList/psxctl:Param[@name='class']" mode="internal-json" priority="20">
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param[@name='alt']" mode="internal-json" priority="10">
      <xsl:param name="controlClassName"/>
      <xsl:value-of select="@name" /><xsl:text>:'</xsl:text><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="concat('psx.contenteditor.sys_templates.',$controlClassName,'.alt@',psxctl:DefaultValue)"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template><xsl:text>',</xsl:text>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param" mode="internal-json" priority="5">
      <xsl:value-of select="@name" /><xsl:text>:'</xsl:text><xsl:value-of select="psxctl:DefaultValue"/><xsl:text>',</xsl:text>
   </xsl:template>

   <!-- core attributes common to most elements
  id       document-wide unique id
  class    space separated list of classes
  style    associated style info
  title    advisory title/amplification
-->
   <!--
control for radio button
-->
   <!--
     sys_RadioButtons

<!ATTLIST INPUT
  %attrs;                              -%coreattrs, %i18n, %events -
  checked     (checked)      #IMPLIED  -for radio buttons and check boxes -
  disabled    (disabled)     #IMPLIED  -unavailable in this context -
  readonly    (readonly)     #IMPLIED  -for text and passwd -
  size        CDATA          #IMPLIED  -specific to each type of field -
  alt         CDATA          #IMPLIED  -short description -
  tabindex    NUMBER         #IMPLIED  -position in tabbing order -
  accesskey   %Character;    #IMPLIED  -accessibility key character -
  onfocus     %Script;       #IMPLIED  -the element got the focus -
  onblur      %Script;       #IMPLIED  -the element lost the focus -
  onselect    %Script;       #IMPLIED  -some text was selected -
  onchange    %Script;       #IMPLIED  -the element value was changed -
  >
  -->
   <psxctl:ControlMeta name="sys_RadioButtons" dimension="single" choiceset="required">
      <psxctl:Description>A set of radio buttons for selecting a single value</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>200</psxctl:DefaultValue>
         </psxctl:Param>
         <!--     <psxctl:Param name="disabled" datatype="String" paramtype="generic">
            <psxctl:Description>If set, this boolean attribute disables the control for user input.</psxctl:Description>
         </psxctl:Param> -->
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_RadioButtons']" mode="psxcontrol">
      <div class="datadisplay">
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_RadioButtons'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
         <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysradiobuttons">
            <xsl:with-param name="controlValue" select="Value"/>
            <xsl:with-param name="paramName" select="@paramName"/>
            <xsl:with-param name="accessKey">
               <xsl:call-template name="getaccesskey">
                  <xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/>
                  <xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/>
                  <xsl:with-param name="paramName" select="@paramName"/>
                  <xsl:with-param name="accessKey" select="@accessKey"/>
               </xsl:call-template>
            </xsl:with-param>
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
      <input type="radio" name="{$paramName}" id="perc-content-edit-{$paramName}" value="{Value}">
         <xsl:if test="$accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:value-of select="$accessKey"/></xsl:attribute>
         </xsl:if>
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
         <br/>
      </input>
   </xsl:template>
   <!-- read only template for single Radio Button -->
   <xsl:template match="Control[@name='sys_RadioButtons' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <div class="datadisplay">
         <xsl:variable name="Val" select="Value"/>
         <xsl:variable name="paramName" select="@paramName"/>
         <xsl:for-each select="DisplayChoices/DisplayEntry[Value=$Val]">
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
         </xsl:for-each>
      </div>
      <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
   </xsl:template>
   <!--
     sys_EditBox

<!ATTLIST input
%attrs;
type        %InputType;    "text"
name        CDATA          #IMPLIED
value       CDATA          #IMPLIED
checked     (checked)      #IMPLIED
disabled    (disabled)     #IMPLIED
readonly    (readonly)     #IMPLIED
size        CDATA          #IMPLIED
maxlength   %Number;       #IMPLIED
src         %URI;          #IMPLIED
alt         CDATA          #IMPLIED
usemap      %URI;          #IMPLIED
tabindex    %Number;       #IMPLIED
accesskey   %Character;    #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onselect    %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
accept      %ContentTypes; #IMPLIED
>
 -->
   <psxctl:ControlMeta name="sys_EditBox" dimension="single" choiceset="none">
      <psxctl:Description>The standard control for editing text:  an &lt;input type="text"> tag.</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in number of characters.  The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="maxlength" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for This parameter is an unlimited number.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>450</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>160</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP.</psxctl:Description>
            <psxctl:DefaultValue>INPLACE_TEXT</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_EditBox']" mode="psxcontrol">
      <input type="text" name="{@paramName}"  id="perc-content-edit-{@paramName}"  value="{Value}">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
   </xsl:template>
   <!--
     sys_File
 -->
   <psxctl:ControlMeta name="sys_File" dimension="single" choiceset="none">
      <psxctl:Description>The standard control for uploading files:  an &lt;input type="file"> tag.</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="cleartext" datatype="String" paramtype="custom">
            <psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear'.</psxctl:Description>
            <psxctl:DefaultValue>Clear</psxctl:DefaultValue>
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
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="readyToGo" occurrence="single">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/generic/sys_FileInfo</name>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_File']" mode="psxcontrol">
      <input type="hidden" name="psxmldoc" value="treatAsText"/>
      <input type="file" id="perc-content-edit-{@paramName}" name="{@paramName}">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_File'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
      <xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
         &nbsp;&nbsp;&nbsp;
         <xsl:call-template name="sys_filereadonly"/>
         <xsl:if test="boolean(@clearBinaryParam)">
            &nbsp;&nbsp;
            <xsl:call-template name="sys_fileclear"/>
         </xsl:if>
      </xsl:if>
   </xsl:template>
   <!-- when the file control is used in read-only mode, provide a binary preview -->
   <xsl:template name="sys_filereadonly" match="Control[@name='sys_File' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <xsl:variable name="childkey">
         <xsl:choose>
            <xsl:when test="boolean(../../@childkey)">
               <xsl:value-of select="../../@childkey"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="/*/ItemContent/@childkey"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="fileName">
         <xsl:for-each select="/*/ItemContent/DisplayField">
            <xsl:if test="Control/@paramName='item_file_attachment_filename'">
               <xsl:value-of select="concat('&amp;fileName=',Control/Value)"/>
            </xsl:if>
         </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="url">
         <xsl:choose>
            <xsl:when test="contains(/ContentEditor/@submitHref, '.html')">
               <!-- exclude the extension, as it causes IE to ignore the content-type header -->
               <xsl:value-of select="substring-before(/ContentEditor/@submitHref, '.html')"/>
               <xsl:value-of select="substring-after(/ContentEditor/@submitHref,'.html')"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="/ContentEditor/@submitHref"/>
            </xsl:otherwise>
         </xsl:choose>
         <!-- ? or & depending on the Href already having CGI vars -->
         <xsl:choose>
            <xsl:when test="contains(/ContentEditor/@submitHref, '?')">&amp;</xsl:when>
            <xsl:otherwise>?</xsl:otherwise>
         </xsl:choose>
         <xsl:variable name="filerev" select="/*/Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
         <xsl:value-of select="concat('sys_command=binary&amp;sys_contentid=',/*/Workflow/@contentId,
         '&amp;sys_revision=',$filerev,
         '&amp;sys_submitname=',@paramName,'&amp;sys_childrowid=',$childkey)"/>
         <!-- childid if it exists -->
      </xsl:variable>
      <a href="{$url}{$fileName}" target="_blank"  rel = "noopener noreferrer" class="perc-preview-file-link" id="perc-content-edit-{@paramName}">
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="'psx.contenteditor.sys_templates@Preview File'"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </a>
   </xsl:template>
   <!-- when the file control is used in edit mode, provide a clear checkbox -->
   <xsl:template name="sys_fileclear" match="Control[@name='sys_File' and @isReadOnly='no']" priority="10" mode="psxcontrol-sys_fileclear">
      <span class="datadisplay">
         <input name="{@clearBinaryParam}" type="checkbox" value="yes">
            <xsl:if test="@accessKey!=''">
               <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
            </xsl:if>
         </input>
         <xsl:call-template name="parameterToValuefileclear">
            <xsl:with-param name="controlClassName" select="'sys_File'"/>
            <xsl:with-param name="controlNode" select="."/>
            <xsl:with-param name="paramName" select="'cleartext'"/>
         </xsl:call-template>
         <br/>
      </span>
   </xsl:template>
   <xsl:template name="parameterToValuefileclear">
      <xsl:param name="controlClassName"/>
      <xsl:param name="controlNode"/>
      <xsl:param name="paramName"/>
      <xsl:param name="source" select="document('')"/>
      <xsl:choose>
         <xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
            <!-- apply control parameters that have been defined in the metadata (will override defaults) -->
            <xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
         </xsl:when>
         <xsl:otherwise>
            <!-- apply any control parameter defaults defined in the metadata -->
            <xsl:apply-templates select="$source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="fileclear-internal-value"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param" mode="fileclear-internal-value">
      <xsl:call-template name="getLocaleString">
         <xsl:with-param name="key" select="concat('psx.ce.',../../@name, '@', psxctl:DefaultValue)"/>
         <xsl:with-param name="lang" select="$lang"/>
      </xsl:call-template>
   </xsl:template>
   <!--
   sys_webImageFX
   -->
   <psxctl:ControlMeta name="sys_webImageFX" dimension="single" choiceset="none">
      <psxctl:Description>Custom Ektron control for editing and uploading image files:  an &lt;input type="file"> tag.</psxctl:Description>
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
         <psxctl:Param name="width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>800</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="config_src_url" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the location of the config.xml that will the control will use for configuration. The default value is "/rx_resources/webimagefx/ImageEditConfig.xml".</psxctl:Description>
            <psxctl:DefaultValue>/rx_resources/webimagefx/ImageEditConfig.xml</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cleartext" datatype="String" paramtype="custom">
            <psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear'.</psxctl:Description>
            <psxctl:DefaultValue>Clear</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>840</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>500</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="webimagefx.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/rx_resources/webimagefx/webimagefx.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="readyToGo" occurrence="single">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/generic/sys_FileInfo</name>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_webImageFX' and @isReadOnly='no']" priority="10" mode="psxcontrol">
      <xsl:choose>
         <!-- Check for IE on Windows-->
         <xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE')">
            <!-- set up the variables that will be used in the javascript -->
            <xsl:variable name="name">
               <xsl:value-of select="@paramName"/>
            </xsl:variable>
            <xsl:variable name="width">
               <xsl:choose>
                  <xsl:when test="ParamList/Param[@name='width']">
                     <xsl:value-of select="ParamList/Param[@name='width']"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='width']/psxctl:DefaultValue"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="height">
               <xsl:choose>
                  <xsl:when test="ParamList/Param[@name='height']">
                     <xsl:value-of select="ParamList/Param[@name='height']"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='height']/psxctl:DefaultValue"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="config_src_url">
               <xsl:choose>
                  <xsl:when test="ParamList/Param[@name='config_src_url']">
                     <xsl:value-of select="ParamList/Param[@name='config_src_url']"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='config_src_url']/psxctl:DefaultValue"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <input type="hidden" name="uploadfilephoto" value="{Value}"/>
            <xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
               <xsl:if test="boolean(@clearBinaryParam)">
                  <br/>
                  <xsl:call-template name="sys_imageclear"/>
               </xsl:if>
            </xsl:if>
            <script><![CDATA[
      <!--

         if(WifxLicenseKeys != "")
         {
            WebImageFX.Config = "]]><xsl:value-of select="$config_src_url"/>";
               WebImageFX.create("uploadfilephoto", <xsl:value-of select="$width"/>, <xsl:value-of select="$height"/>);
               <![CDATA[
             ps_hasWifx = true;
         }
         else
         {
            var doc = window.document;
            doc.open();
            doc.writeln("<table bgcolor='#ffffff' width='60%' height='50' border='1' cellpadding='0' cellspacing='0'>");
            doc.writeln("<tr><td align='center' valign='middle'>");
            doc.writeln("No license for WebImageFX control.<br>Please contact Percussion technical support.");
            doc.writeln("</td></tr></table>");
            doc.close();

         }
      //-->
      ]]></script>
         </xsl:when>
         <xsl:otherwise>
            <input type="file" name="uploadfilephoto">
               <xsl:call-template name="parametersToAttributes">
                  <xsl:with-param name="controlClassName" select="'sys_File'"/>
                  <xsl:with-param name="controlNode" select="."/>
               </xsl:call-template>
            </input>
            <xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
               <xsl:if test="boolean(@clearBinaryParam)">
                  <br/>
                  <xsl:call-template name="sys_filereadonly"/>
               </xsl:if>
            </xsl:if>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!-- when the file control is used in edit mode, provide a clear checkbox -->
   <xsl:template name="sys_imageclear" match="Control[@name='sys_webImageFX' and @isReadOnly='no']" priority="10" mode="psxcontrol-sys_webimagefxclear">
      <span class="datadisplay">
         <input name="{@clearBinaryParam}" type="checkbox" value="yes"/>
         <xsl:call-template name="parameterToValueimageclear">
            <xsl:with-param name="controlClassName" select="'sys_webImageFX'"/>
            <xsl:with-param name="controlNode" select="."/>
            <xsl:with-param name="paramName" select="'cleartext'"/>
         </xsl:call-template>
         <br/>
      </span>
   </xsl:template>
   <xsl:template name="parameterToValueimageclear">
      <xsl:param name="controlClassName"/>
      <xsl:param name="controlNode"/>
      <xsl:param name="paramName"/>
      <xsl:param name="source" select="document('')" />
      <xsl:choose>
         <xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
            <!-- apply control parameters that have been defined in the metadata (will override defaults) -->
            <xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
         </xsl:when>
         <xsl:otherwise>
            <!-- apply any control parameter defaults defined in the metadata -->
            <xsl:apply-templates select="$source/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="imageclear-internal-value"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="psxctl:ParamList/psxctl:Param" mode="imageclear-internal-value">
      <xsl:call-template name="getLocaleString">
         <xsl:with-param name="key" select="concat('psx.ce.',../../@name, '@', psxctl:DefaultValue)"/>
         <xsl:with-param name="lang" select="$lang"/>
      </xsl:call-template>
   </xsl:template>
   <!--
         End of sys_webImageFX
       -->
   <!--
     sys_HiddenInput: needs a higher priority than the default read-only template
  -->
   <psxctl:ControlMeta name="sys_HiddenInput" dimension="single" choiceset="none">
      <psxctl:Description>a hidden input field</psxctl:Description>
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
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup or none. Applicable values are MODAL, POPUP, INPLACE_TEXT and NONE any other value is treated as POPUP. The recommended values for this control is NONE.</psxctl:Description>
            <psxctl:DefaultValue>NONE</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_HiddenInput']" priority="10" mode="psxcontrol">
      <input type="hidden" name="{@paramName}" value="{Value}">
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
   </xsl:template>
   <!--
     when the mode is "psxcontrol-hidden", visibility rules have suppressed
     the control; render all controls as if they were sys_HiddenInput.
-->
   <xsl:template match="Control" mode="psxcontrol-hidden">
      <xsl:variable name="paramname" select="@paramName"/>
      <xsl:variable name="controlnode" select="."/>
      <xsl:choose>
         <!--In case of DisplayChoices like check boxes, we need hidden input elements wherever @selected=yes -->
         <xsl:when test="DisplayChoices[not(boolean(preceding-sibling::Value))]">
            <xsl:choose>
               <xsl:when test="@name='sys_DropDownSingle'">
                  <xsl:choose>
                     <xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']">
                        <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{DisplayChoices/DisplayEntry[@selected='yes']/Value}">
                           <xsl:call-template name="parametersToAttributes">
                              <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                              <xsl:with-param name="controlNode" select="$controlnode"/>
                           </xsl:call-template>
                        </input>
                     </xsl:when>
                     <xsl:when test="string(DisplayChoices/@href) and document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']">
                        <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']/Value}">
                           <xsl:call-template name="parametersToAttributes">
                              <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                              <xsl:with-param name="controlNode" select="$controlnode"/>
                           </xsl:call-template>
                        </input>
                     </xsl:when>
                     <xsl:otherwise>
                        <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{DisplayChoices/DisplayEntry/Value}">
                           <xsl:call-template name="parametersToAttributes">
                              <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                              <xsl:with-param name="controlNode" select="$controlnode"/>
                           </xsl:call-template>
                        </input>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:when>
               <xsl:otherwise>
                  <!-- Loop through all display entries with @selected=yes -->
                  <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
                     <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{Value}">
                        <xsl:call-template name="parametersToAttributes">
                           <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                           <xsl:with-param name="controlNode" select="$controlnode"/>
                        </xsl:call-template>
                     </input>
                  </xsl:for-each>
                  <!-- Loop through all display entries with @selected=yes if entries come from a external document -->
                  <xsl:if test="string(DisplayChoices/@href)">
                     <xsl:for-each select="document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']">
                        <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{Value}">
                           <xsl:call-template name="parametersToAttributes">
                              <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                              <xsl:with-param name="controlNode" select="$controlnode"/>
                           </xsl:call-template>
                        </input>
                     </xsl:for-each>
                  </xsl:if>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <input type="hidden" name="{$paramname}" id="perc-content-edit-{$paramname}" value="{Value}">
               <xsl:call-template name="parametersToAttributes">
                  <xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
                  <xsl:with-param name="controlNode" select="$controlnode"/>
               </xsl:call-template>
            </input>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!--
     sys_DropDownSingle

<!ATTLIST select
%attrs;
name        CDATA          #IMPLIED
size        %Number;       #IMPLIED
multiple    (multiple)     #IMPLIED
disabled    (disabled)     #IMPLIED
tabindex    %Number;       #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
>
  -->
   <psxctl:ControlMeta name="sys_DropDownSingle" dimension="single" choiceset="required">
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
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_DropDownSingle']" mode="psxcontrol">
      <div>
         <select name="{@paramName}">
            <xsl:if test="@accessKey!=''">
               <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
            </xsl:if>
            <xsl:call-template name="parametersToAttributes">
               <xsl:with-param name="controlClassName" select="'sys_DropDownSingle'"/>
               <xsl:with-param name="controlNode" select="."/>
            </xsl:call-template>
            <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownsingle">
               <xsl:with-param name="controlValue" select="Value"/>
               <xsl:with-param name="paramName" select="@paramName"/>
            </xsl:apply-templates>
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
   <!-- read only template for dropdown single -->
   <xsl:template match="Control[@name='sys_DropDownSingle' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <div class="datadisplay">
         <xsl:variable name="Val" select="Value"/>
         <xsl:variable name="paramName" select="@paramName"/>
         <xsl:choose>
            <xsl:when test="not($Val)">
               <xsl:variable name="displayValue">
                  <xsl:choose>
                     <xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']">
                        <xsl:value-of select="DisplayChoices/DisplayEntry[@selected='yes']/DisplayLabel"/>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="DisplayChoices/DisplayEntry/DisplayLabel"/>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:variable>
               <xsl:choose>
                  <xsl:when test="@sourceType">
                     <xsl:call-template name="getLocaleDisplayLabel">
                        <xsl:with-param name="sourceType" select="@sourceType"/>
                        <xsl:with-param name="paramName" select="$paramName"/>
                        <xsl:with-param name="displayVal" select="$displayValue"/>
                     </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="$displayValue"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
               <xsl:for-each select="DisplayChoices/DisplayEntry[Value=$Val]">
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
               </xsl:for-each>
            </xsl:otherwise>
         </xsl:choose>
      </div>
      <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}">
         <xsl:attribute name="value"><xsl:choose><xsl:when test="Value!=''"><xsl:value-of select="Value"/></xsl:when><xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="DisplayChoices/DisplayEntry[@selected='yes']/Value"/></xsl:when><xsl:otherwise><xsl:value-of select="DisplayChoices/DisplayEntry/Value"/></xsl:otherwise></xsl:choose></xsl:attribute>
      </input>
   </xsl:template>
   <!--
     sys_CheckBoxGroup
  -->
   <psxctl:ControlMeta name="sys_CheckBoxGroup" dimension="array" choiceset="required">
      <psxctl:Description>a group of check boxes with the same HTML param name</psxctl:Description>
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
         <psxctl:Param name="columncount" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the number of column(s) displayed.</psxctl:Description>
            <psxctl:DefaultValue>1</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="columnwidth" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the column in pixels or percentage.</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>200</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="selectall.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/selectall.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_CheckBoxGroup']" priority="10" mode="psxcontrol">
      <!-- both a local/global <DisplayChoices> and an external <DisplayChoices> can be in the same control
        (in case you want to hardcode a few, and have the rest dynamic) -->
      <xsl:variable name="columncount">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='columncount']">
               <xsl:value-of select="ParamList/Param[@name='columncount']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxGroup']/psxctl:ParamList/psxctl:Param[@name='columncount']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="columnwidth">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='columnwidth']">
               <xsl:value-of select="ParamList/Param[@name='columnwidth']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxGroup']/psxctl:ParamList/psxctl:Param[@name='columnwidth']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:choose>
         <xsl:when test="$columncount > 1">
            <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
               <xsl:with-param name="Control" select="."/>
               <xsl:with-param name="columncount" select="$columncount"/>
               <xsl:with-param name="columnwidth" select="$columnwidth"/>
            </xsl:apply-templates>
            <xsl:for-each select="DisplayChoices">
               <xsl:if test="string(@href)">
                  <xsl:apply-templates select="document(@href)/*/DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
                     <xsl:with-param name="Control" select="."/>
                     <xsl:with-param name="columncount" select="$columncount"/>
                     <xsl:with-param name="columnwidth" select="$columnwidth"/>
                  </xsl:apply-templates>
               </xsl:if>
            </xsl:for-each>
         </xsl:when>
         <xsl:otherwise>
            <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-syscheckboxgroup-onecolumn">
               <xsl:with-param name="Control" select="."/>
            </xsl:apply-templates>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="@isReadOnly !='yes'">
         <!-- syntax checkAll(document.EditForm.$fieldnanme) -->
         <a href="#">
            <xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOcheckAll(document.forms['perc-content-form'].elements[']]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[']);]]><xsl:text/></xsl:attribute>Check All</a>
         <xsl:text>&#160; &#160;</xsl:text>
         <a href="#">
            <xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOuncheckAll(document.forms['perc-content-form'].elements[']]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[']);]]><xsl:text/></xsl:attribute>Uncheck All</a>
      </xsl:if>
   </xsl:template>
   <xsl:template match="DisplayChoices" mode="psxcontrol-syscheckboxgroup-onecolumn">
      <xsl:param name="Control"/>
      <xsl:apply-templates select="DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
         <xsl:with-param name="Control" select="$Control"/>
      </xsl:apply-templates>
      <xsl:if test="string(@href)">
         <!-- external xml is assumed to use a DTD compatible with rx_ContentEditor.dtd
           (namely that items are in <DisplayEntry>s) -->
         <xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
            <xsl:with-param name="Control" select="$Control"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>
   <xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
      <!-- Control is a reference to the parent node
        (supplied in case we are processing nodes from external source) -->
      <xsl:param name="Control"/>
      <div class="datadisplay">
         <xsl:choose>
            <xsl:when test="$Control/@isReadOnly = 'yes'">
               <xsl:choose>
                  <xsl:when test="@selected = 'yes'">
                     <img src="/sys_resources/images/checked.gif" height="16" width="16"/>
                     <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <img src="/sys_resources/images/unchecked.gif" height="16" width="16"/>
                  </xsl:otherwise>
               </xsl:choose>
               &nbsp;<xsl:value-of select="DisplayLabel"/>
            </xsl:when>
            <xsl:otherwise>
               <input name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" type="checkbox" value="{Value}">
                  <xsl:if test="@accessKey!=''">
                     <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
                  </xsl:if>
                  <xsl:call-template name="parametersToAttributes">
                     <xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
                     <xsl:with-param name="controlNode" select="$Control"/>
                  </xsl:call-template>
                  <xsl:if test="@selected = 'yes'">
                     <xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="DisplayLabel"/>
               </input>
               <br/>
            </xsl:otherwise>
         </xsl:choose>
      </div>
   </xsl:template>
   <xsl:template match="DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
      <xsl:param name="Control"/>
      <xsl:param name="columncount"/>
      <xsl:param name="columnwidth"/>
      <xsl:variable name="DisplayEntrycount" select="number(count(DisplayEntry))"/>
      <xsl:variable name="DisplayEntrycountreadonly" select="number(count(DisplayEntry[@selected = 'yes']))"/>
      <xsl:choose>
         <!-- READ ONLY MODE -->
         <xsl:when test="$Control/@isReadOnly = 'yes'">
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr class="headercell2">
                  <td valign="top">
                     <table border="0">
                        <xsl:apply-templates select="DisplayEntry[position() mod $columncount = 1]" mode="psxcontrol-syscheckboxgroup-newrow">
                           <xsl:with-param name="Control" select="$Control"/>
                           <xsl:with-param name="columncount" select="$columncount"/>
                           <xsl:with-param name="readonly" select="'yes'"/>
                           <xsl:with-param name="columnwidth" select="$columnwidth"/>
                        </xsl:apply-templates>
                     </table>
                  </td>
               </tr>
            </table>
         </xsl:when>
         <!-- EDIT ONLY MODE -->
         <xsl:otherwise>
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr class="headercell2">
                  <td valign="top">
                     <table border="0">
                        <xsl:apply-templates select="DisplayEntry[position() mod $columncount = 1]" mode="psxcontrol-syscheckboxgroup-newrow">
                           <xsl:with-param name="Control" select="$Control"/>
                           <xsl:with-param name="columncount" select="$columncount"/>
                           <xsl:with-param name="columnwidth" select="$columnwidth"/>
                        </xsl:apply-templates>
                     </table>
                  </td>
               </tr>
            </table>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-newrow">
      <xsl:param name="Control"/>
      <xsl:param name="columncount"/>
      <xsl:param name="readonly"/>
      <xsl:param name="columnwidth"/>
      <tr>
         <td class="datadisplay" valign="top" align="left" width="{$columnwidth}">
            <xsl:choose>
               <xsl:when test="$readonly='yes' and @selected = 'yes'">
                  <img src="/sys_resources/images/checked.gif" height="16" width="16"/>
                  <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
               </xsl:when>
               <xsl:when test="$readonly='yes' and @selected != 'yes'">
                  <img src="/sys_resources/images/unchecked.gif" height="16" width="16"/>
                  <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
               </xsl:when>
               <xsl:otherwise>
                  <input name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" type="checkbox" value="{Value}">
                     <xsl:if test="@accessKey!=''">
                        <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
                     </xsl:if>
                     <xsl:call-template name="parametersToAttributes">
                        <xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
                        <xsl:with-param name="controlNode" select="$Control"/>
                     </xsl:call-template>
                     <xsl:if test="@selected = 'yes'">
                        <xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
                     </xsl:if>
                     <xsl:value-of select="DisplayLabel"/>
                  </input>
               </xsl:otherwise>
            </xsl:choose>
         </td>
         <xsl:apply-templates select="following-sibling::DisplayEntry" mode="psxcontrol-syscheckboxgroup-ncolumns">
            <xsl:with-param name="Control" select="$Control"/>
            <xsl:with-param name="columncount" select="$columncount"/>
            <xsl:with-param name="readonly" select="$readonly"/>
            <xsl:with-param name="columnwidth" select="$columnwidth"/>
         </xsl:apply-templates>
      </tr>
   </xsl:template>
   <xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-ncolumns">
      <xsl:param name="Control"/>
      <xsl:param name="columncount"/>
      <xsl:param name="readonly"/>
      <xsl:param name="columnwidth"/>
      <xsl:if test="position() &lt; $columncount">
         <td class="datadisplay" valign="top" align="left" width="{$columnwidth}">
            <xsl:choose>
               <xsl:when test="$readonly='yes' and @selected = 'yes'">
                  <img src="/sys_resources/images/checked.gif" height="16" width="16"/>
                  <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
               </xsl:when>
               <xsl:when test="$readonly='yes' and @selected != 'yes'">
                  <img src="/sys_resources/images/unchecked.gif" height="16" width="16"/>
                  <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
               </xsl:when>
               <xsl:otherwise>
                  <input name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" type="checkbox" value="{Value}">
                     <xsl:if test="@accessKey!=''">
                        <xsl:attribute name="accesskey"><xsl:value-of select="@accessKey"/></xsl:attribute>
                     </xsl:if>
                     <xsl:call-template name="parametersToAttributes">
                        <xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
                        <xsl:with-param name="controlNode" select="$Control"/>
                     </xsl:call-template>
                     <xsl:if test="@selected = 'yes'">
                        <xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
                     </xsl:if>
                     <xsl:value-of select="DisplayLabel"/>
                  </input>
               </xsl:otherwise>
            </xsl:choose>
         </td>
      </xsl:if>
   </xsl:template>
   <!--
     sys_TextArea

<!ATTLIST textarea
  %attrs;
  name        CDATA          #IMPLIED
  rows        %Number;       #REQUIRED
  cols        %Number;       #REQUIRED
  disabled    (disabled)     #IMPLIED
  readonly    (readonly)     #IMPLIED
  tabindex    %Number;       #IMPLIED
  accesskey   %Character;    #IMPLIED
  onfocus     %Script;       #IMPLIED
  onblur      %Script;       #IMPLIED
  onselect    %Script;       #IMPLIED
  onchange    %Script;       #IMPLIED
  >
  -->
   <psxctl:ControlMeta name="sys_TextArea" dimension="single" choiceset="none">
      <psxctl:Description>A simple text area</psxctl:Description>
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
         <psxctl:Param name="rows" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the number of visible text lines. The default value is 4.</psxctl:Description>
            <psxctl:DefaultValue>4</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cols" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the visible width in average character widths. The default value is 80.</psxctl:Description>
            <psxctl:DefaultValue>80</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>620</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>240</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="requirescleanup" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the text area needs to be checked for critical classes.</psxctl:Description>
            <psxctl:DefaultValue>no</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="PercContentChecker.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/PercContentChecker.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_TextArea']" mode="psxcontrol">
      <textarea name="{@paramName}" wrap="soft">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_TextArea'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
         <xsl:value-of select="Value"/>
      </textarea>
      <xsl:variable name="requirescleanup">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='requirescleanup']">
               <xsl:value-of select="ParamList/Param[@name='requirescleanup']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_TextArea']/psxctl:ParamList/psxctl:Param[@name='requirescleanup']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:if test="$requirescleanup = 'yes'">
         <script >
            PercHtmlFieldsContentCheckerArray.push('<xsl:value-of select="@paramName" />');
         </script>
      </xsl:if>
   </xsl:template>


   <!--
     sys_CalendarSimple
  -->
   <psxctl:ControlMeta name="sys_CalendarSimple" dimension="single" choiceset="none">
      <psxctl:Description>A input box with icon to pop-up calendar picker</psxctl:Description>
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
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="alt" datatype="String" paramtype="img">
            <psxctl:Description>This parameter specifies alternate text the calendar picker icon, for user agents that cannot display images. The default value is "Calendar Pop-up"</psxctl:Description>
            <psxctl:DefaultValue>Calendar Pop-up</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="src" datatype="String" paramtype="img">
            <psxctl:Description>This parameter specifies the location of the image resource used for the calendar picker icon. The default value is "/sys_resources/images/cal.gif"</psxctl:Description>
            <psxctl:DefaultValue>/sys_resources/images/cal.gif</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="String" paramtype="img">
            <psxctl:Description>This parameter specifies the height of the calendar picker icon. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 20.</psxctl:Description>
            <psxctl:DefaultValue>20</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="width" datatype="String" paramtype="img">
            <psxctl:Description>This parameter specifies the width of the calendar picker icon. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is 20.</psxctl:Description>
            <psxctl:DefaultValue>20</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="formname" datatype="String" paramtype="jscript">
            <psxctl:Description>This parameter specifies the name of the form that contains this control. It is used by the calendar's JavaScript. The default value is "EditForm"</psxctl:Description>
            <psxctl:DefaultValue>perc-content-form</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="time" datatype="String" paramtype="jscript">
            <psxctl:Description>This parameter specifies whether time is to be displayed or not.If 0 then no time.</psxctl:Description>
            <psxctl:DefaultValue>0</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>450</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>200</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>

         <psxctl:FileDescriptor name="timepicker.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/timepicker.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>

         <psxctl:FileDescriptor name="jquery-ui-1.8.9.custom.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>/cm/themes/smoothness/jquery-ui-1.8.9.custom.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_CalendarSimple']" mode="psxcontrol">
      <xsl:variable name="calendar_id" select="concat('perc-content-edit-', @paramName)" />
      <xsl:variable name="value" select="Value" />

      <input id="{$calendar_id}" name="{@paramName}" style="display:none;" type="text" value="{Value}" />
      <input class="perc-datetime-picker" id="{concat($calendar_id,'-display')}" name="{concat(@paramName,'-display')}" type="text" value="" />

      <script >
         (function($) {
         $(function() {
         var options = <xsl:call-template name="parametersToJSON"><xsl:with-param name="controlClassName" select="'sys_CalendarSimple'"/><xsl:with-param name="controlNode" select="."/></xsl:call-template>;
         options.inputName = '<xsl:value-of select="@paramName" />';
         options.inputId = '<xsl:value-of select="$calendar_id" />';
         options.display = '<xsl:value-of select="concat($calendar_id,'-display')" />';
         var v = '<xsl:value-of select="$value" />';
         var vdate = '<xsl:value-of select="$value" />'.split(' ');
         var formatDate = "";
         <xsl:text disable-output-escaping="yes">
         if (vdate.length > 1){
         var vtime = vdate[1].split(':');
         vtime = ((vtime[0]==0 || vtime[0]==12)? '12' : vtime[0]%12) + ":" + vtime[1] + ((vtime[0]>11)? " pm" : " am" );
         formatDate = vdate[0] + " " + vtime;
         }
         </xsl:text>
         $('#' + options.display).datepicker({
         altTimeField: '',
         buttonImage: '/rx_resources/controls/percQueryControl/images/calendar.gif',
         buttonImageOnly: true,
         buttonText: '',
         changeMonth:true,
         changeYear:true,
         constrainInput: true,
         dateFormat: 'yy-mm-dd',
         duration: '',
         showOn: 'button',
         showTime: true,
         stepHours: 1,
         stepMinutes: 1,
         time24h: false,
         // hook called when date is selected
         // mark asset as dirty when date is selected
         onSelect : function(dateText, inst) {
         // if the top most jquery is defined
         if(typeof $.topFrameJQuery !== 'undefined')
         // mark the asset as dirty
         $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
         },
         onClose : function(dateText)
         {
         var newDate = dateText.split(' ');
         var newFormatDate = "";
         <xsl:text disable-output-escaping="yes">
         if (newDate.length > 1){
         var newTime = newDate[1].split(':');
         newTime = (((newDate[2].toUpperCase()=='PM' &amp;&amp; newTime[0]!=12)  || (newDate[2].toUpperCase()=='AM' &amp;&amp; newTime[0]==12)) ? (((parseInt(newTime[0])+12)%24) + ':' + newTime[1]) : newTime.join(':'));
         newFormatDate = newDate[0] + " " + newTime;
         }
         </xsl:text>
         $('#' + options.inputId).val(newFormatDate);
         }
         })
         .on('paste', function(evt){evt.preventDefault();})
         .on('keypress keydown', function(evt)
         {
         if(evt.keyCode === 46 || evt.keyCode === 8 )
         {
         var field = evt.target;
         field.value = "";
         $('#' + options.inputId).val("");
         evt.preventDefault();
         return;
         }
         if(evt.charCode === 0 || typeof(evt.charCode) === 'undefined')
         return;
         evt.preventDefault();
         })
         .val(formatDate);
         });
         })(jQuery);
      </script>
   </xsl:template>

   <!--
     sys_HtmlEditor
  -->
   <psxctl:ControlMeta name="sys_HtmlEditor" dimension="single" choiceset="none" deprecate="yes" replacewith="sys_eWebEditPro">
      <psxctl:Description>WYSIWYG HTML Editor</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="NAME" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to the inline frame. The default value is "dynamsg".</psxctl:Description>
            <psxctl:DefaultValue>dynamsg</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the inline frame. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the inline frame. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 250.</psxctl:Description>
            <psxctl:DefaultValue>250</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="SCROLLING" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies scrolling information for the inline frame.  The default value is "auto".</psxctl:Description>
            <psxctl:DefaultValue>auto</psxctl:DefaultValue>
            <psxctl:ChoiceList>
               <psxctl:Entry>auto</psxctl:Entry>
               <psxctl:Entry>yes</psxctl:Entry>
               <psxctl:Entry>no</psxctl:Entry>
            </psxctl:ChoiceList>
         </psxctl:Param>
         <psxctl:Param name="SRC" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the location of the HTML that will populate the inline frame. The default value is "/sys_resources/texteditor/deditor.html".</psxctl:Description>
            <psxctl:DefaultValue>/sys_resources/texteditor/deditor.html</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="formname" datatype="String" paramtype="jscript">
            <psxctl:Description>This parameter specifies the name of the form that contains this control. It is used by the editor's JavaScript. The default value is "EditForm"</psxctl:Description>
            <psxctl:DefaultValue>perc-content-form</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
            <psxctl:DefaultValue>103</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
            <psxctl:DefaultValue>104</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineVariantSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inlinevariant slot. The inline search dialog box shows the content types that have at least one variant added to the inline variant slot. The default value is system inline variant slotid 105.</psxctl:Description>
            <psxctl:DefaultValue>105</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>800</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>350</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="textedit.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/textedit.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="setupOptional" occurrence="multiple">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/xmldom/sys_xdTextCleanup</name>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(fieldName)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rxW2Ktidy.properties</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rxW2KserverPageTags.xml</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text/>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text/>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>yes</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <!-- form-onsubmit functions must return true or the submit will be cancelled -->
   <xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol-form-onsubmit">
      <!-- Check for IE first -->
      <xsl:choose>
         <xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE') and ../@displayType!='sys_hidden'">
            <xsl:value-of select="concat(' &amp;&amp; set',@paramName,'()')"/>
         </xsl:when>
         <!-- If not IE -->
         <xsl:otherwise/>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol-body-onload">
      <!-- Check for IE first -->
      <xsl:choose>
         <xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE') and ../@displayType!='sys_hidden'">
            <xsl:value-of select="concat('get',@paramName,'();')"/>
         </xsl:when>
         <!-- If not IE -->
         <xsl:otherwise/>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol">
      <!-- Check for IE first -->
      <!-- set up the variables that will be used in the javascript -->
      <xsl:variable name="name">
         <xsl:value-of select="@paramName"/>
      </xsl:variable>
      <xsl:variable name="formname">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='formname']">
               <xsl:value-of select="ParamList/Param[@name='formname']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_HtmlEditor']/psxctl:ParamList/psxctl:Param[@name='formname']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="id">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='NAME']">
               <xsl:value-of select="ParamList/Param[@name='NAME']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_HtmlEditor']/psxctl:ParamList/psxctl:Param[@name='NAME']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineLinkSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineImageSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineImageSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineVariantSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineVariantSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineVariantSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineVariantSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <script >
         function set<xsl:value-of select="$name"/>() {
         if(<xsl:value-of select="$id"/>.document.all.switchMode.checked){
         <xsl:value-of select="$id"/>.document.all.switchMode.checked = false;
         <xsl:value-of select="$id"/>.setMode(false);
         }
         <xsl:value-of select="$formname"/>.<xsl:value-of select="$name"/>.value = <xsl:value-of select="$id"/>.window.Composition.document.body.innerHTML;
         return true;
         }
         function get<xsl:value-of select="$name"/>() {
         <xsl:value-of select="$id"/>.window.Composition.document.InlineLinkSlot = "<xsl:value-of select="$InlineLinkSlot"/>";
         <xsl:value-of select="$id"/>.window.Composition.document.InlineImageSlot = "<xsl:value-of select="$InlineImageSlot"/>";
         <xsl:value-of select="$id"/>.window.Composition.document.InlineVariantSlot = "<xsl:value-of select="$InlineVariantSlot"/>";
         <xsl:value-of select="$id"/>.window.Composition.document.editorName = "<xsl:value-of select="@paramName"/>";
         <xsl:value-of select="$id"/>.window.Composition.document.body.innerHTML = <xsl:value-of select="$formname"/>.<xsl:value-of select="$name"/>.value;
         }
      </script>
      <xsl:choose>
         <xsl:when test="contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')">
            <!-- This is only a temporary arrangment. Permanent fix should use sys_Textarea control -->
            <textarea cols="50" rows="15" wrap="soft">
               <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
               <xsl:value-of select="Value"/>
            </textarea>
         </xsl:when>
         <xsl:when test="contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE')">
            <!-- If not a Mac or NN -->
            <IFrame>
               <xsl:call-template name="parametersToAttributes">
                  <xsl:with-param name="controlClassName" select="'sys_HtmlEditor'"/>
                  <xsl:with-param name="controlNode" select="."/>
               </xsl:call-template>
            </IFrame>
            <input type="hidden" name="{$name}" value="{Value}"/>
         </xsl:when>
         <xsl:otherwise>
            <!-- This is only a temporary arrangment. Permanent fix should use sys_Textarea control -->
            <textarea cols="50" rows="15" wrap="soft">
               <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
               <xsl:value-of select="Value"/>
            </textarea>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     sys_EditLive
     -->
   <psxctl:ControlMeta name="sys_EditLive" dimension="single" choiceset="none">
      <psxctl:Description>A simple text area</psxctl:Description>
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
         <psxctl:Param name="rows" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the number of visible text lines. The default value is 4.</psxctl:Description>
            <psxctl:DefaultValue>4</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cols" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the visible width in average character widths. The default value is 80.</psxctl:Description>
            <psxctl:DefaultValue>80</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>620</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>240</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_EditLive']" mode="psxcontrol">
      <textarea name="{@paramName}" wrap="soft">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_EditLive'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
         <xsl:value-of select="Value"/>
      </textarea>
   </xsl:template>
   <!--
     sys_Table: needs a higher priority than the default read-only template

<!ATTLIST table
  %attrs;
  summary     %Text;         #IMPLIED
  width       %Length;       #IMPLIED
  border      %Pixels;       #IMPLIED
  frame       %TFrame;       #IMPLIED
  rules       %TRules;       #IMPLIED
  cellspacing %Length;       #IMPLIED
  cellpadding %Length;       #IMPLIED
  >
  -->
   <psxctl:ControlMeta name="sys_Table" dimension="table" choiceset="none">
      <psxctl:Description>A simple table</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="summary" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter provides a summary of the table's purpose and structure for user agents rendering to non-visual media such as speech and Braille.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the desired width of the entire table.  This parameter may be either a number of pixels or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cellspacing" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies how much space the user agent should leave between the left side of the table and the left-hand side of the leftmost column, the top of the table and the top side of the topmost row, and so on for the right and bottom of the table. The attribute also specifies the amount of space to leave between cells.  The default value is 0.</psxctl:Description>
            <psxctl:DefaultValue>0</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cellpadding" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the amount of space between the border of the cell and its contents. If the value of this attribute is a pixel length, all four margins should be this distance from the contents. If the value of the attribute is a percentage length, the top and bottom margins should be equally separated from the content based on a percentage of the available vertical space, and the left and right margins should be equally separated from the content based on a percentage of the available horizontal space. The default value is 5.</psxctl:Description>
            <psxctl:DefaultValue>5</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="border" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width (in pixels only) of the frame around the table. The default value is 1.</psxctl:Description>
            <psxctl:DefaultValue>1</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_Table']" priority="10" mode="psxcontrol">
      <table border="0" cellpadding="0" cellspacing="0">
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_Table'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
         <thead>
            <xsl:apply-templates select="Table/Header" mode="inside"/>
         </thead>
         <tbody>
            <xsl:apply-templates select="Table/RowData/Row" mode="inside"/>
         </tbody>
      </table>
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
         <tr>
            <td align="center">
               <xsl:apply-templates select="Table/ActionLinkList" mode="actionlist"/>
            </td>
         </tr>
      </table>
   </xsl:template>
   <xsl:template match="Header" mode="inside">
      <tr>
         <xsl:for-each select="HeaderColumn">
            <th class="headercell">
               <span class="datadisplay"><xsl:value-of select="."/></span>
            </th>
         </xsl:for-each>
         <xsl:if test="../RowData/Row/ActionLinkList">
            <th class="headercell"><span class="datadisplay">Action</span></th>
         </xsl:if>
      </tr>
   </xsl:template>
   <xsl:template match="Row" mode="inside">
      <tr>
         <xsl:for-each select="Column">
            <td>
               <xsl:apply-templates select="Control" mode="psxcontrol"/>
            </td>
         </xsl:for-each>
         <xsl:if test="ActionLinkList">
            <td>
               <xsl:apply-templates select="ActionLinkList" mode="actionlist"/>
            </td>
         </xsl:if>
      </tr>
   </xsl:template>
   <!-- OLD CODE
   apply any control parameter defaults defined in the metadata
   <xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name='sys_EditBox']/psxctl:ParamList/psxctl:Param[psxctl:DefaultValue]" mode="internal"/>
   apply control parameters that have been defined in the metadata (will override defaults)
   <xsl:apply-templates select="ParamList/Param[@name = document('')/*/psxctl:ControlMeta[@name='sys_EditBox']/psxctl:ParamList/psxctl:Param/@name]" mode="internal"/>
   -->
   <!-- suppress text nodes in these modes, but keep walking element nodes -->
   <xsl:template match="*" mode="psxcontrol-body-onload">
      <xsl:apply-templates select="*" mode="psxcontrol-body-onload"/>
   </xsl:template>
   <xsl:template match="*" mode="psxcontrol-form-onsubmit">
      <xsl:apply-templates select="*" mode="psxcontrol-form-onsubmit"/>
   </xsl:template>
   <xsl:template match="*" mode="psxcontrol-customcontrol-isdirty">
      <xsl:apply-templates select="*" mode="psxcontrol-customcontrol-isdirty"/>
   </xsl:template>
   <!--
     sys_RelatedContentTable

-->
   <psxctl:ControlMeta name="sys_RelatedContentTable" dimension="table" choiceset="none">
      <psxctl:Description>A related content table</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="summary" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter provides a summary of the table's purpose and structure for user agents rendering to non-visual media such as speech and Braille.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the desired width of the entire table.  This parameter may be either a number of pixels or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cellspacing" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies how much space the user agent should leave between the left side of the table and the left-hand side of the leftmost column, the top of the table and the top side of the topmost row, and so on for the right and bottom of the table. The attribute also specifies the amount of space to leave between cells.  The default value is 0.</psxctl:Description>
            <psxctl:DefaultValue>0</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="cellpadding" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the amount of space between the border of the cell and its contents. If the value of this attribute is a pixel length, all four margins should be this distance from the contents. If the value of the attribute is a percentage length, the top and bottom margins should be equally separated from the content based on a percentage of the available vertical space, and the left and right margins should be equally separated from the content based on a percentage of the available horizontal space. The default value is 5.</psxctl:Description>
            <psxctl:DefaultValue>5</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="border" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width (in pixels only) of the frame around the table. The default value is 1.</psxctl:Description>
            <psxctl:DefaultValue>1</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_RelatedContentTable']" priority="10" mode="psxcontrol">
      <!-- if the following condition is true old related item table is put -->
      <xsl:if test="not(contains(/ContentEditor/SectionLinkList/SectionLink[@name='RelatedLookupURL'], 'sys_rcSupport'))">
         <table width="100%" cellpadding="4" cellspacing="0" border="0">
            <tr>
               <td class="headercell2">
                  <table width="100%" cellpadding="0" cellspacing="0" border="0">
                     <tr>
                        <td class="backgroundcolor">
                           <table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
                              <tr class="headercell">
                                 <td class="headercell2font" align="center">Title</td>
                                 <td class="headercell2font" align="center">Type</td>
                                 <td class="headercell2font" align="center">Variant</td>
                                 <td class="headercell2font" align="center">Slot</td>
                                 <td class="headercell2font" align="center">&#160;</td>
                                 <xsl:if test="Table/RowData/Row/ActionLinkList">
                                    <td class="headercellfont" align="center">Action</td>
                                 </xsl:if>
                              </tr>
                              <xsl:apply-templates select="Table/RowData/Row" mode="Row"/>
                           </table>
                           <table width="100%" border="0" cellpadding="0" cellspacing="0">
                              <tr class="headercell2">
                                 <td align="center">
                                    <xsl:apply-templates select="Table/ActionLinkList" mode="actionlist"/>
                                 </td>
                              </tr>
                           </table>
                        </td>
                     </tr>
                  </table>
               </td>
            </tr>
         </table>
      </xsl:if>
   </xsl:template>
   <xsl:template match="Row" mode="Row">
      <xsl:variable name="RelatedURL">
         <!--<xsl:value-of select="concat('http://38.164.160.65:9992/Rhythmyx/rx_ceRelatedContentSearch/relatedcontentlookup.xml','?sysid=',Column/Control[@paramName='sysid']/Value)" />-->
         <xsl:value-of select="concat(/ContentEditor/SectionLinkList/SectionLink[@name='RelatedLookupURL'],'&amp;sysid=',Column/Control[@paramName='sysid']/Value)"/>
      </xsl:variable>
      <tr class="datacell1">
         <xsl:apply-templates select="document($RelatedURL)" mode="RowData"/>
         <xsl:if test="ActionLinkList">
            <td class="datacell2">
               <xsl:apply-templates select="ActionLinkList" mode="actionlist">
                  <xsl:with-param name="separator" select="'&nbsp;'"/>
               </xsl:apply-templates>
            </td>
         </xsl:if>
      </tr>
   </xsl:template>
   <xsl:template match="/RelatedContentPrevew/item" mode="RowData">
      <td class="datacell1font" valign="top" align="left">
         <xsl:value-of select="concat(titles,'(',titles/@id,')')"/>&#160;</td>
      <td class="datacell1font" valign="top" align="left">
         <xsl:value-of select="type"/>&#160;</td>
      <td class="datacell1font" valign="top" align="left">
         <xsl:value-of select="variant"/>&#160;</td>
      <td class="datacell1font" valign="top" align="left">
         <xsl:value-of select="slot"/>&#160;</td>
      <td class="datacell1font" valign="top" align="center">
         <a href="{previewurl}" target="_blank" rel = "noopener noreferrer">
            <img src="/sys_resources/images/eye.gif" alt="Preview" align="top" width="16" height="16" border="0"/>
         </a>
      </td>
   </xsl:template>
   <!--
     sys_VariantDropDown control

-->
   <!--

<!ATTLIST select
%attrs;
name        CDATA          #IMPLIED
size        %Number;       #IMPLIED
multiple    (multiple)     #IMPLIED
disabled    (disabled)     #IMPLIED
tabindex    %Number;       #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
>
  -->
   <psxctl:ControlMeta name="sys_VariantDropDown" dimension="single" choiceset="optional" deprecated="yes">
      <psxctl:Description>a drop down combo box for selecting a single variant</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
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
         <psxctl:Param name="OutputFormat" datatype="String" paramtype="custom">
            <psxctl:Description>This parameter selects the output format (snippet or page).</psxctl:Description>
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
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_VariantDropDown']" mode="psxcontrol">
      <xsl:variable name="OutputFormat">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='OutputFormat']">
               <xsl:value-of select="ParamList/Param[@name='OutputFormat']"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="LookupURL">
         <xsl:value-of select="concat(DisplayChoices/@href,'&amp;outputformat=',$OutputFormat,'&amp;contenttypeid=',/ContentEditor/@contentTypeId)"/>
      </xsl:variable>
      <div>
         <select name="{@paramName}">
            <xsl:call-template name="parametersToAttributes">
               <xsl:with-param name="controlClassName" select="'sys_VariantDropDown'"/>
               <xsl:with-param name="controlNode" select="."/>
            </xsl:call-template>
            <xsl:apply-templates select="document($LookupURL)/*/item" mode="psxcontrol-variantdropdownsingle">
               <xsl:with-param name="controlValue" select="Value"/>
            </xsl:apply-templates>
         </select>
      </div>
   </xsl:template>
   <xsl:template match="item" mode="psxcontrol-variantdropdownsingle">
      <xsl:param name="controlValue"/>
      <option value="{value}">
         <xsl:if test="value = $controlValue">
            <xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
         </xsl:if>
         <xsl:if test="@selected='yes'">
            <xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
         </xsl:if>
         <xsl:value-of select="display"/>
      </option>
   </xsl:template>
   <xsl:template match="Control[@name='sys_VariantDropDown' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <xsl:variable name="OutputFormat">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='OutputFormat']">
               <xsl:value-of select="ParamList/Param[@name='OutputFormat']"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="LookupURL">
         <xsl:value-of select="concat(DisplayChoices/@href,'&amp;outputformat=',$OutputFormat,'&amp;contenttypeid=',/ContentEditor/@contentTypeId)"/>
      </xsl:variable>
      <xsl:variable name="Val" select="Value"/>
      <div class="datadisplay">
         <xsl:value-of select="document($LookupURL)/*/item[value=$Val]/display"/>
      </div>
   </xsl:template>
   <!--
     sys_GenericPageError

     Displays the generic page validation error message on top of the form.
 -->
   <xsl:template name="sys_GenericPageError">
      <xsl:for-each select="DisplayError">
         <tr>
            <td class="headercell2">
               <table width="100%" cellpadding="4" cellspacing="0" border="0">
                  <td>
                     <table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
                        <tr class="headercell">
                           <td class="headercell2errorfont">
                              <xsl:value-of select="GenericMessage"/>
                           </td>
                        </tr>
                        <xsl:for-each select="Details/FieldError">
                           <tr>
                              <td>
                                 <xsl:if test="@submitName!=''">
                                    <xsl:variable name="subName" select="@submitName"/>
                                    <xsl:variable name="displayName" select="@displayName"/>
                                    <xsl:variable name="dfield" select="//ItemContent/DisplayField[Control/@paramName=$subName]"/>
                                    <table width="100%" cellpadding="0" cellspacing="0" border="0" class="backgroundcolor">
                                       <tr>
                                          <td width="20%" class="headererrorcell">
                                             <xsl:variable name="keyval">
                                                <xsl:choose>
                                                   <xsl:when test="$dfield/DisplayLabel/@sourceType='sys_system'">
                                                      <xsl:value-of select="concat('psx.ce.system.', $dfield/Control/@paramName, '@', $displayName)"/>
                                                   </xsl:when>
                                                   <xsl:when test="$dfield/DisplayLabel/@sourceType='sys_shared'">
                                                      <xsl:value-of select="concat('psx.ce.shared.', $dfield/Control/@paramName, '@', $displayName)"/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $dfield/Control/@paramName, '@', $displayName)"/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:variable>
                                             <xsl:call-template name="getLocaleString">
                                                <xsl:with-param name="key" select="$keyval"/>
                                                <xsl:with-param name="lang" select="$lang"/>
                                             </xsl:call-template>
                                             &#160;
                                          </td>
                                          <td class="headererrorcell">
                                             <xsl:if test=".!=''">
                                                <xsl:call-template name="getLocaleString">
                                                   <xsl:with-param name="key" select="concat('psx.ce.error@',.)"/>
                                                   <xsl:with-param name="lang" select="$lang"/>
                                                </xsl:call-template>
                                             </xsl:if>
                                          </td>
                                       </tr>
                                    </table>
                                 </xsl:if>
                              </td>
                           </tr>
                        </xsl:for-each>
                     </table>
                  </td>
               </table>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <!--
     sys_FileWord
 -->
   <psxctl:ControlMeta name="sys_FileWord" dimension="single" choiceset="none">
      <psxctl:Description>a file upload input control with MS Word launcher</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="cleartext" datatype="String" paramtype="custom">
            <psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear Word'.</psxctl:Description>
            <psxctl:DefaultValue>Clear Word</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="RxContentEditorURL" datatype="String" paramtype="msword">
            <psxctl:Description>This parameter specifies the absolute URL to the content editor of the current content item.  The value is passed to the OCX control, which uses it to obtain the metadata fields of the current content item.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="WordTemplateURL" datatype="String" paramtype="msword">
            <psxctl:Description>This parameter specifies the absolute URL to retrieve the Microsoft Word template document which provides the macros used to edit content items within Word.  The value is passed to the OCX control.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="ContentBodyURL" datatype="String" paramtype="msword">
            <psxctl:Description>This parameter specifies the absolute URL to retrieve the Microsoft Word document associated with the current content item.  The value is passed to the OCX control.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
            <psxctl:DefaultValue>103</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
            <psxctl:DefaultValue>104</psxctl:DefaultValue>
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
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="readyToGo" occurrence="single">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/generic/sys_FileInfo</name>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_FileWord']" mode="psxcontrol">
      <xsl:call-template name="rx_filereadonly"/>
      <xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
         <xsl:if test="boolean(@clearBinaryParam)">
            &nbsp;&nbsp;
            <xsl:call-template name="sys_fileclear"/>
         </xsl:if>
      </xsl:if>
   </xsl:template>
   <!-- when the file control is used in read-only mode, provide a binary preview -->
   <xsl:template name="rx_filereadonly" match="Control[@name='sys_FileWord' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <xsl:variable name="childkey">
         <xsl:choose>
            <xsl:when test="boolean(../../@childkey)">
               <xsl:value-of select="../../@childkey"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="/*/ItemContent/@childkey"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="contenteditorurl">
         <xsl:value-of select="ParamList/Param[@name='RxContentEditorURL']"/>
      </xsl:variable>
      <xsl:variable name="wordtemplateurl">
         <xsl:value-of select="ParamList/Param[@name='WordTemplateURL']"/>
      </xsl:variable>
      <xsl:variable name="contentbodyurl">
         <xsl:value-of select="ParamList/Param[@name='ContentBodyURL']"/>
      </xsl:variable>
      <xsl:variable name="bodysourcename">
         <xsl:value-of select="@paramName"/>
      </xsl:variable>
      <xsl:variable name="firsttimeuse">
         <xsl:value-of select="/*/ItemContent/@newDocument"/>
      </xsl:variable>
      <xsl:variable name="encodingfieldname">
         <xsl:value-of select="concat(@paramName,'_encoding')"/>
      </xsl:variable>
      <xsl:variable name="encoding">
         <xsl:value-of select="/ContentEditor/ItemContent/DisplayField/Control[@paramName=$encodingfieldname]/Value"/>
      </xsl:variable>
      <xsl:variable name="InlineLinkSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('/rx_resources/stylesheets/rx_Templates.xsl')/*/psxctl:ControlMeta[@name='sys_FileWord']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineImageSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineImageSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('/rx_resources/stylesheets/rx_Templates.xsl')/*/psxctl:ControlMeta[@name='sys_FileWord']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <script><![CDATA[
         function launchWordandPreview(contentid,revision) {
            //call ephox pre submit to update the hidden parameter
            if(hasEditLiveControls)
            {
               rxEphoxPreSubmit();
            }
            var paramstr = "";
            var edform = document.EditForm;
            var flen = edform.length;
            var selfirst = true;
            var parvalsep = "|_|_";
            var valparsep = "#|#|";
            var folderid = parseParam("sys_folderid", document.location.href);
            if(contentid!=""){
               paramstr += "sys_contentid" + parvalsep + contentid + valparsep;
            }
            if(revision !=""){
               paramstr += "sys_revision" + parvalsep + revision + valparsep;
            }
            if(folderid !=""){
               paramstr += "sys_folderid" + parvalsep + folderid + valparsep;
            }
            for(i=0;i<flen;i++){
               var parValue = edform[i].value;
               if (edform[i].type == "checkbox")
               {
                  if (edform[i].checked) {
                     paramstr += edform[i].name + parvalsep + parValue;
                     if(i<flen-1)
                        paramstr += valparsep;
                  }
               }
               else if(edform[i].type == "select-multiple")
               {
                  selfirst=true;
                  for(j=0;j<edform[i].options.length;j++)
                  {
                     if(edform[i].options[j].selected)
                     {
                        if(selfirst)
                           selfirst = false;
                        else
                           paramstr += valparsep;
                        paramstr += edform[i].name + parvalsep + edform[i].options[j].value;
                     }
                  }
                  if(i<flen-1)
                     paramstr += valparsep;
               }
               else {
                  paramstr += edform[i].name + parvalsep + parValue;
                  if(i<flen-1)
                     paramstr += valparsep;
               }


            }
            if(document.URL.indexOf("debugrxword=yes")!=-1){
               document.all.word.setAttribute("DebugMode","yes");
            }
            else{
               document.all.word.setAttribute("DebugMode","no");
            }

            document.all.word.setAttribute("ParamString",paramstr);

            document.all.word.Fire();

            if (document.all.word.Success == true && document.all.sys_currentview.value == "sys_All") {
               self.close();

            }
            else {
               if (contentid != "" && revision != "") {
                  docurl = document.location.href;
                  docurl  = docurl.split("?")[0] + "?sys_command=preview&sys_contentid=" + contentid + "&sys_revision=" + revision
                  document.location.href = docurl;
               }
            }
         }
         ]]></script>
      <a>
         <xsl:attribute name="href">javascript:launchWordandPreview("<xsl:value-of select="/*/Workflow/@contentId"/>","<xsl:value-of select="/*/Workflow/ContentStatus/@thisRevision"/>");</xsl:attribute>
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="'psx.contenteditor.sys_templates@Launch Word'"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </a>
      <object id="MSXML4" classid="clsid:88d969c0-f192-11d4-a65f-0040963251e5" codebase="/sys_resources/word/msxml4.cab#version=4,00,9004,0" type="application/x-oleobject" style="display: none"/>
      <object id="word" classid="clsid:DA87CB4F-8EDF-4087-8F04-87EC3C938202" codebase="/rx_resources/word/rxwordocx.cab#version=6,0,0,5" type="application/x-oleobject" style="display: none">
         <param name="ContentEditorURL" value="{$contenteditorurl}"/>
         <param name="WordTemplateURL" value="{$wordtemplateurl}"/>
         <param name="EncodingParam" value="{$encoding}"/>
         <param name="ContentBodyURL" value="{$contentbodyurl}"/>
         <param name="BodySourceName" value="{$bodysourcename}"/>
         <param name="FirstTimeUse" value="{$firsttimeuse}"/>
         <param name="InlineSlots" value="{concat('InlineLinkSlot#',$InlineLinkSlot,'##InlineImageSlot#',$InlineImageSlot)}"/>
      </object>
   </xsl:template>
   <!--    SingleCheckBox
     (eventually this control should support rows/cols for layout)
-->
   <psxctl:ControlMeta name="sys_SingleCheckBox" dimension="single" choiceset="required">
      <psxctl:Description>a group of check boxes with the same HTML param name</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
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
      </psxctl:ParamList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_SingleCheckBox']" priority="10" mode="psxcontrol">
      <xsl:variable name="ISselected">
         <xsl:value-of select="Value"/>
      </xsl:variable>
      <xsl:variable name="chkBoxAccKey">
         <xsl:if test="@accessKey!=''">
            <xsl:call-template name="getaccesskey">
               <xsl:with-param name="label" select="preceding-sibling::DisplayLabel" />
               <xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType" />
               <xsl:with-param name="paramName" select="@paramName" />
               <xsl:with-param name="accessKey" select="@accessKey" />
            </xsl:call-template>
         </xsl:if>
      </xsl:variable>
      <xsl:apply-templates select="DisplayChoices/DisplayEntry[position()=1]" mode="psxcontrol-syssinglecheckbox">
         <xsl:with-param name="Control" select="."/>
         <xsl:with-param name="ISselected" select="$ISselected"/>
         <xsl:with-param name="chkBoxAccKey" select="$chkBoxAccKey"/>
      </xsl:apply-templates>
   </xsl:template>
   <xsl:template match="DisplayEntry" mode="psxcontrol-syssinglecheckbox">
      <xsl:param name="Control"/>
      <xsl:param name="chkBoxAccKey"/>
      <xsl:param name="ISselected"/>
      <table class="perc-singlecheckbox" width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="headercell2">
            <td>
               <xsl:choose>
                  <xsl:when test="$Control/@isReadOnly = 'yes'">
                     <xsl:choose>
                        <xsl:when test="Value = $ISselected">
                           <table border="0">
                              <tr>
                                 <td class="datadisplay" align="left">
                                    <img class="perc-readonly-checkbox" src="/sys_resources/images/checked.gif" height="16" width="16"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
                                    <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>
                                 </td>
                              </tr>
                           </table>
                        </xsl:when>
                        <xsl:otherwise>
                           <table border="0">
                              <tr>
                                 <td class="datadisplay" align="left">
                                    <img class="perc-readonly-checkbox" src="/sys_resources/images/unchecked.gif" height="16" width="16"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
                                    <input type="hidden" name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" value="{Value}"/>
                                 </td>
                              </tr>
                           </table>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise>
                     <table border="0">
                        <tr>
                           <td class="datadisplay" valign="top" align="left">
                              <input name="{$Control/@paramName}" id="perc-content-edit-{$Control/@paramName}" type="checkbox" value="{Value}">
                                 <xsl:if test="$chkBoxAccKey!=''">
                                    <xsl:attribute name="accesskey"><xsl:value-of select="$chkBoxAccKey"/></xsl:attribute>
                                 </xsl:if>
                                 <xsl:call-template name="parametersToAttributes">
                                    <xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
                                    <xsl:with-param name="controlNode" select="$Control"/>
                                 </xsl:call-template>
                                 <xsl:if test="Value = $ISselected">
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
                              <br/>
                           </td>
                        </tr>
                     </table>
                  </xsl:otherwise>
               </xsl:choose>
            </td>
         </tr>
      </table>
   </xsl:template>
   <xsl:template name="getaccesskey">
      <xsl:param name="label"/>
      <xsl:param name="sourceType"/>
      <xsl:param name="paramName"/>
      <xsl:param name="accessKey"/>
      <xsl:if test="$label!='' and $accessKey != '' and $paramName != ''">
         <xsl:variable name="keyval">
            <xsl:choose>
               <xsl:when test="$sourceType='sys_system'">
                  <xsl:value-of select="concat('psx.ce.system.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
               </xsl:when>
               <xsl:when test="DisplayLabel/@sourceType='sys_shared'">
                  <xsl:value-of select="concat('psx.ce.shared.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:variable>
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="$keyval"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </xsl:if>
   </xsl:template>
   <!--  END Single Check Box -->
   <!--
       sys_CheckBoxTree
    -->
   <psxctl:ControlMeta name="sys_CheckBoxTree" dimension="array" choiceset="required">
      <psxctl:Description>Tree of check boxes</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the applet. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is 400.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the applet. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 300.</psxctl:Description>
            <psxctl:DefaultValue>300</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tree_src_url" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the relative location of the xml that will defines the tree</psxctl:Description>
            <psxctl:DefaultValue>/rx_resources/treedef.xml</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="formname" datatype="String" paramtype="jscript">
            <psxctl:Description>This parameter specifies the name of the form that contains this control, defaults to EditForm.</psxctl:Description>
            <psxctl:DefaultValue>perc-content-form</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>250</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
      </psxctl:AssociatedFileList>

      <psxctl:Dependencies>
         <psxctl:Dependency status="setupOptional" occurrence="multiple">
            <psxctl:Default>

               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/exit/sys_paramStringListToParamArray</name>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(simpleChildField)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>;</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(simpleChildField)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>yes</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>

   </psxctl:ControlMeta>

   <xsl:template match="Control[@name='sys_CheckBoxTree']" mode="psxcontrol-customcontrol-isdirty">
      <xsl:variable name="parentEl" select="name(parent::node())"/>
      <xsl:if test="$parentEl != 'Column'">
         <xsl:value-of select="concat(' || CustomControlIsDirty_',@paramName,'()')"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="Control[@name='sys_CheckBoxTree']" mode="psxcontrol">
      <!-- set up the variables that will be used in the javascript -->
      <xsl:variable name="name">
         <xsl:value-of select="@paramName" />
      </xsl:variable>

      <xsl:variable name="width">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='width']">
               <xsl:value-of select="ParamList/Param[@name='width']" />
            </xsl:when>

            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxTree']/psxctl:ParamList/psxctl:Param[@name='width']/psxctl:DefaultValue" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="height">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='height']">
               <xsl:value-of select="ParamList/Param[@name='height']" />
            </xsl:when>

            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxTree']/psxctl:ParamList/psxctl:Param[@name='height']/psxctl:DefaultValue" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="tree_src_url">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='tree_src_url']">
               <xsl:value-of select="ParamList/Param[@name='tree_src_url']" />
            </xsl:when>

            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxTree']/psxctl:ParamList/psxctl:Param[@name='tree_src_url']/psxctl:DefaultValue" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="formname">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='formname']">
               <xsl:value-of select="ParamList/Param[@name='formname']" />
            </xsl:when>

            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxTree']/psxctl:ParamList/psxctl:Param[@name='formname']/psxctl:DefaultValue" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="selected_values">
         <xsl:apply-templates select="DisplayChoices/DisplayEntry[@selected='yes']" mode="list-of-values" />
      </xsl:variable>

      <xsl:variable name="applet_name"><xsl:value-of select="@paramName" />_CheckBoxTreeApplet</xsl:variable>

      <input type="hidden" name="{@paramName}" id="{@paramName}" value="{Value}" />

      <script language="JavaScript1.2"><![CDATA[
            <!--
            var ]]><xsl:value-of select="$name"/><![CDATA[_name = "]]><xsl:value-of select="$applet_name"/>"<![CDATA[;
            var ]]><xsl:value-of select="$name"/><![CDATA[_height = "]]><xsl:value-of select="$height"/>"<![CDATA[;
            var ]]><xsl:value-of select="$name"/><![CDATA[_width = "]]><xsl:value-of select="$width"/>"<![CDATA[;
            var ]]><xsl:value-of select="$name"/><![CDATA[_selectedItems = "]]><xsl:value-of select="$selected_values"/>"<![CDATA[;
            var ]]><xsl:value-of select="$name"/><![CDATA[_treeXML = "]]><xsl:value-of select="$tree_src_url"/>"<![CDATA[;
            var ]]><xsl:value-of select="$name"/><![CDATA[_readOnly = "]]><xsl:value-of select="@isReadOnly"/>"<![CDATA[;

            var ]]><xsl:value-of select="$name"/><![CDATA[_appletCaller = ]]>new AppletCaller();

         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("name", <xsl:value-of select="$name"/><![CDATA[_name]]>);
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("id", <xsl:value-of select="$name"/><![CDATA[_name]]>);
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("width", <xsl:value-of select="$name"/><![CDATA[_width]]>);
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("height", <xsl:value-of select="$name"/><![CDATA[_height]]>);
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("codebase", "/sys_resources/AppletJars");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("archive", "/sys_resources/AppletJars/rxCheckboxTree.jar");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("code", "com.percussion.controls.contenteditor.checkboxtree.PSCheckboxTreeApplet");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("classid", "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("codebaseattr", "http://java.sun.com/products/plugin/autodl/jinstall-1_4-windows-i586.cab#Version=1,4,0,0");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("type", "application/x-java-applet;version=1.4.0_03");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("cache_archive", "rxCheckboxTree.jar");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("cache_option", "Plugin");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("selectedItems", <xsl:value-of select="$name"/><![CDATA[_selectedItems]]>);
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("treeXML", <xsl:value-of select="$name"/><![CDATA[_treeXML]]>);
         if (<xsl:value-of select="$name"/><![CDATA[_readOnly]]> == "yes")
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.addParam("readOnly", "yes");
         <xsl:value-of select="$name"/><![CDATA[_appletCaller]]>.show();
         //-->

         function set<xsl:value-of select="$name" />()
         {
         _applet = PSGetApplet(window, <xsl:value-of select="$name"/><![CDATA[_name]]>);
         _hiddenField = document.getElementById('<xsl:value-of select="$name" />');
         _hiddenField.value = _applet.getSelected();

         return true;
         }

         function CustomControlIsDirty_<xsl:value-of select="$name"/>()
         {
         _applet = PSGetApplet(window, <xsl:value-of select="$name"/><![CDATA[_name]]>);
         return _applet.isDirty();
         }
      </script>
   </xsl:template>

   <xsl:template match="Control[@name='sys_CheckBoxTree']" mode="psxcontrol-form-onsubmit">
      <xsl:if test="../@displayType!='sys_hidden'">
         <xsl:value-of select="concat(' &amp;&amp; set',@paramName,'()')" />
      </xsl:if>
   </xsl:template>

   <xsl:template match="DisplayEntry[1]" priority="5" mode="list-of-values">
      <xsl:value-of select="Value" />
   </xsl:template>

   <xsl:template match="DisplayEntry" mode="list-of-values">;<xsl:value-of select="Value" />
   </xsl:template>

   <!--
       default template for hidden sys_CheckBoxTree control; just return the value
    -->
   <xsl:template match="Control[@name='sys_CheckBoxTree']" priority="6" mode="psxcontrol-hidden">
      <input type="hidden" name="{@paramName}">
         <xsl:attribute name="value">
            <xsl:apply-templates select="DisplayChoices/DisplayEntry[@selected='yes']" mode="list-of-values" />
         </xsl:attribute>
      </input>
   </xsl:template>

   <!-- END sys_CheckBoxTree -->
   <!--
   sys_CheckBoxTreeJS
   -->
   <psxctl:ControlMeta name="sys_CheckBoxTreeJS" dimension="array" choiceset="required">
      <psxctl:Description>A simple text area</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>tree-datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the applet. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is 400.</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the applet. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 300.</psxctl:Description>
            <psxctl:DefaultValue>250</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>620</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>240</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="tree_src_url" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the relative location of the xml that will defines the tree</psxctl:Description>
            <psxctl:DefaultValue>/rx_resources/treedef.xml</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="rootpath" datatype="String" paramtype="generic">
            <psxctl:Description>The path to the root category to select from</psxctl:Description>
            <psxctl:DefaultValue></psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="ui.dynatree.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>/web_resources/cm/css/dynatree/skin/ui.dynatree.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="tree.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>/sys_resources/css/checkboxTree/tree.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="ui.core.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-dynatree/jquery.dynatree.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="checkboxTree.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/checkboxTree.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="checkboxTreeReadonly.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/checkboxTreeReadonly.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="setupOptional" occurrence="multiple">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/exit/sys_paramStringListToParamArray</name>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(simpleChildField)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>;</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(simpleChildField)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>yes</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>
      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_CheckBoxTreeJS']" priority="31" mode="psxcontrol">

      <xsl:variable name="controlName" select="'sys_CheckBoxTreeJS'" />
      <xsl:variable name="tree_src_url">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'tree_src_url'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="rootpath">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'rootpath'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="height">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'height'" />
            <xsl:with-param name="default" select="'300'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="width">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'width'" />
            <xsl:with-param name="default" select="'400'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="style">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'style'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="tabindex">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'tabindex'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="class">
         <xsl:call-template name="getParam">
            <xsl:with-param name="controlName" select="$controlName" />
            <xsl:with-param name="paramName" select="'class'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="selected_values">
         <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
            <xsl:value-of select="Value" /><xsl:if test="position() != last()">;</xsl:if>
         </xsl:for-each>
      </xsl:variable>
      <script >
         // <![CDATA[
        (function($) {
        $(function() {
        // ]]>
         <xsl:call-template name="jsVar">
            <xsl:with-param name="name" select="'treeSrcUrl'" />
            <xsl:with-param name="value" select="$tree_src_url" />
         </xsl:call-template>
         <xsl:call-template name="jsVar">
            <xsl:with-param name="name" select="'rootpath'" />
            <xsl:with-param name="value" select="$rootpath" />
         </xsl:call-template>
         <xsl:call-template name="jsVar">
            <xsl:with-param name="name" select="'selectedValues'" />
            <xsl:with-param name="value" select="$selected_values" />
         </xsl:call-template>
         <xsl:call-template name="jsVar">
            <xsl:with-param name="name" select="'paramName'" />
            <xsl:with-param name="value" select="@paramName" />
         </xsl:call-template>
         <xsl:call-template name="jsVar">
            <xsl:with-param name="name" select="'readonly'" />
            <xsl:with-param name="value" select="@isReadOnly='yes'" />
         </xsl:call-template>
         // <![CDATA[
            readonly = readonly == 'true' ? true : false;
            var siteName = parent.$.PercNavigationManager.getSiteName();
            siteurl = treeSrcUrl + "?" + "sitename="+ siteName + "&rootpath="+rootpath;

            var opts = {url : siteurl, selected : selectedValues, paramName : paramName, readonly : readonly};
            $('#' + paramName + '-tree').perc_checkboxTree(opts);
        });
        })(jQuery);
        // ]]>
      </script>
      <div id="{@paramName}-tree">
         <xsl:attribute name="style">
            <xsl:choose>
               <xsl:when test="$style != ''">
                  <xsl:value-of select="$style" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:variable name="newWidth">
                     <xsl:choose>
                        <xsl:when test="contains($width,'px') or contains($width,'%')">
                           <xsl:value-of select="$width"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="$width"/><xsl:text>px</xsl:text>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="newHeight">
                     <xsl:choose>
                        <xsl:when test="contains($width,'px') or contains($width,'%')">
                           <xsl:value-of select="$height"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="$height"/><xsl:text>px</xsl:text>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:value-of select="concat('width:', $newWidth, 'px; height:', $newHeight, 'px; overflow:auto;') " />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:attribute>

         <xsl:if test="$tabindex != ''">
            <xsl:attribute name="tabindex">
               <xsl:value-of select="$tabindex" />
            </xsl:attribute>
         </xsl:if>
         <xsl:if test="$class != ''">
            <xsl:attribute name="class">
               <xsl:value-of select="$class" />
            </xsl:attribute>
         </xsl:if>

      </div>
      <input type="hidden" name="{@paramName}" id="{@paramName}" value="{$selected_values}" />
   </xsl:template>

   <xsl:template name="jsVar">
      <xsl:param name="name" />
      <xsl:param name="value" />
      var <xsl:value-of select="$name" /> = '<xsl:value-of select="$value" />';
   </xsl:template>

   <xsl:template name="getParam">
      <xsl:param name="paramName" />
      <xsl:param name="controlName"/>
      <xsl:param name="default" select="''" />
      <xsl:choose>
         <xsl:when test="ParamList/Param[@name=$paramName]">
            <xsl:value-of select="ParamList/Param[@name=$paramName]" />
         </xsl:when>
         <xsl:when test="document('')/*/psxctl:ControlMeta[@name=$controlName]/psxctl:ParamList/psxctl:Param[@name=$paramName]/psxctl:DefaultValue">
            <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name=$controlName]/psxctl:ParamList/psxctl:Param[@name=$paramName]/psxctl:DefaultValue" />
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$default" /></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
   END sys_CheckBoxTreeJS
   -->
   <!--
    sys_DropDownMultiple

<!ATTLIST select
%attrs;
name        CDATA          #IMPLIED
size        %Number;       #IMPLIED
multiple    (multiple)     #IMPLIED
disabled    (disabled)     #IMPLIED
tabindex    %Number;       #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
>
  -->
   <psxctl:ControlMeta name="sys_DropDownMultiple" dimension="array" choiceset="required">
      <psxctl:Description>a drop down combo box for selecting multiple values</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
            <psxctl:DefaultValue>1</psxctl:DefaultValue>
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
            <psxctl:DefaultValue>250</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended values are MODAL and POPUP.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="selectall.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/selectall.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_DropDownMultiple']" mode="psxcontrol">
      <xsl:variable name="control_id">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='id']">
               <xsl:value-of select="ParamList/Param[@name='id']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_DropDownMultiple']/psxctl:ParamList/psxctl:Param[@name='id']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="name"><xsl:value-of select="$control_id"/></xsl:attribute>
      <select multiple="1" name="{@paramName}">
         <xsl:if test="ParamList/Param[@name='size']">
            <xsl:attribute name="size"><xsl:value-of select="ParamList/Param[@name='size']"/></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_DropDownMultiple'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
         <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownmultiple">
            <xsl:with-param name="controlValue" select="Value"/>
            <xsl:with-param name="paramName" select="@paramName"/>
         </xsl:apply-templates>
      </select>
      <br/>
      <xsl:if test="@isReadOnly !='yes'">
         <!-- syntax checkAll(document.EditForm.$fieldnanme) -->
         <a href="#">
            <xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOselectAll(document.forms['perc-content-form'].elements[']]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[']);]]><xsl:text/></xsl:attribute>Select All</a>
         <xsl:text>&#160; &#160;</xsl:text>
         <a href="#">
            <xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOunselectAll(document.forms['perc-content-form'].elements[']]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[']);]]><xsl:text/></xsl:attribute>Unselect All</a>
      </xsl:if>
   </xsl:template>
   <xsl:template match="DisplayChoices" mode="psxcontrol-sysdropdownmultiple">
      <xsl:param name="controlValue"/>
      <xsl:param name="paramName"/>
      <!-- local/global and external can both be in the same control -->
      <!-- external is assumed to use a DTD compatible with sys_ContentEditor.dtd (items in <DisplayEntry>s) -->
      <xsl:apply-templates select="DisplayEntry" mode="psxcontrol-sysdropdownmultiple">
         <xsl:with-param name="controlValue" select="$controlValue"/>
         <xsl:with-param name="paramName" select="$paramName"/>
      </xsl:apply-templates>
      <xsl:if test="string(@href)">
         <xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-sysdropdownmultiple">
            <xsl:with-param name="controlValue" select="$controlValue"/>
            <xsl:with-param name="paramName" select="$paramName"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>
   <xsl:template match="DisplayEntry" mode="psxcontrol-sysdropdownmultiple">
      <xsl:param name="controlValue"/>
      <xsl:param name="paramName"/>
      <div class="datadisplay">
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
      </div>
   </xsl:template>
   <!-- read only template for dropdown multiple -->
   <xsl:template match="Control[@name='sys_DropDownMultiple' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
      <div class="datadisplay">
         <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']/DisplayLabel">
            <xsl:if test="position()=last()">
               <xsl:value-of select="."/>
            </xsl:if>
            <xsl:if test="not(position()=last())">
               <xsl:value-of select="."/>,
            </xsl:if>
         </xsl:for-each>
      </div>
      <!-- hidden values -->
      <xsl:variable name="paramName" select="@paramName"/>
      <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
         <input type="hidden">
            <xsl:attribute name="name"><xsl:value-of select="$paramName"/></xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="Value"/></xsl:attribute>
         </input>
      </xsl:for-each>
   </xsl:template>
   <!--
        sys_ImagePath

   <!ATTLIST input
   %attrs;
   type        %InputType;    "text"
   name        CDATA          #IMPLIED
   value       CDATA          #IMPLIED
   checked     (checked)      #IMPLIED
   disabled    (disabled)     #IMPLIED
   readonly    (readonly)     #IMPLIED
   size        CDATA          #IMPLIED
   maxlength   %Number;       #IMPLIED
   src         %URI;          #IMPLIED
   alt         CDATA          #IMPLIED
   usemap      %URI;          #IMPLIED
   tabindex    %Number;       #IMPLIED
   accesskey   %Character;    #IMPLIED
   onfocus     %Script;       #IMPLIED
   onblur      %Script;       #IMPLIED
   onselect    %Script;       #IMPLIED
   onchange    %Script;       #IMPLIED
   accept      %ContentTypes; #IMPLIED
   >
    -->
   <psxctl:ControlMeta name="sys_ImagePath" dimension="single" choiceset="none">
      <psxctl:Description>Control for image link.</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in number of characters.  The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="maxlength" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for This parameter is an unlimited number.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>450</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>160</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP.</psxctl:Description>
            <psxctl:DefaultValue>INPLACE_TEXT</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="PercImageSelectionControl.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/PercImageSelectionControl.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_ImagePath']" mode="psxcontrol">
      <input type="text" name="{@paramName}"  id="perc-content-edit-{@paramName}"  title="{Value}" value="{Value}" readonly="readonly" style="background-color:#E6E6E9;overflow: hidden;text-overflow: ellipsis;">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_ImagePath'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-image-field-select-button" value="Browse"/>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-image-field-clear-button" value="Clear"/>
   </xsl:template>
   <!--
        sys_FilePath

   <!ATTLIST input
   %attrs;
   type        %InputType;    "text"
   name        CDATA          #IMPLIED
   value       CDATA          #IMPLIED
   checked     (checked)      #IMPLIED
   disabled    (disabled)     #IMPLIED
   readonly    (readonly)     #IMPLIED
   size        CDATA          #IMPLIED
   maxlength   %Number;       #IMPLIED
   src         %URI;          #IMPLIED
   alt         CDATA          #IMPLIED
   usemap      %URI;          #IMPLIED
   tabindex    %Number;       #IMPLIED
   accesskey   %Character;    #IMPLIED
   onfocus     %Script;       #IMPLIED
   onblur      %Script;       #IMPLIED
   onselect    %Script;       #IMPLIED
   onchange    %Script;       #IMPLIED
   accept      %ContentTypes; #IMPLIED
   >
    -->
   <psxctl:ControlMeta name="sys_FilePath" dimension="single" choiceset="none">
      <psxctl:Description>Control for image link.</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in number of characters.  The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="maxlength" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for This parameter is an unlimited number.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>450</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>160</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP.</psxctl:Description>
            <psxctl:DefaultValue>INPLACE_TEXT</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="PercFileSelectionControl.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/PercFileSelectionControl.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>

   <!--
      sys_PagePath

 <!ATTLIST input
 %attrs;
 type        %InputType;    "text"
 name        CDATA          #IMPLIED
 value       CDATA          #IMPLIED
 checked     (checked)      #IMPLIED
 disabled    (disabled)     #IMPLIED
 readonly    (readonly)     #IMPLIED
 size        CDATA          #IMPLIED
 maxlength   %Number;       #IMPLIED
 src         %URI;          #IMPLIED
 alt         CDATA          #IMPLIED
 usemap      %URI;          #IMPLIED
 tabindex    %Number;       #IMPLIED
 accesskey   %Character;    #IMPLIED
 onfocus     %Script;       #IMPLIED
 onblur      %Script;       #IMPLIED
 onselect    %Script;       #IMPLIED
 onchange    %Script;       #IMPLIED
 accept      %ContentTypes; #IMPLIED
 >
  -->
   <psxctl:ControlMeta name="sys_PagePath" dimension="single" choiceset="none">
      <psxctl:Description>Control for page link.</psxctl:Description>
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
            <psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in number of characters.  The default value is 50.</psxctl:Description>
            <psxctl:DefaultValue>50</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="maxlength" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for This parameter is an unlimited number.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>450</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>160</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP.</psxctl:Description>
            <psxctl:DefaultValue>INPLACE_TEXT</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="PercPageSelectionControl.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/PercPageSelectionControl.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_PagePath']" mode="psxcontrol">
      <input type="text" name="{@paramName}"  id="perc-content-edit-{@paramName}"  title="{Value}" value="{Value}" readonly="readonly" style="background-color:#E6E6E9;overflow: hidden;text-overflow: ellipsis;">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_PagePath'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-page-field-select-button" value="Browse"/>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-page-field-clear-button" value="Clear"/>
   </xsl:template>

   <xsl:template match="Control[@name='sys_FilePath']" mode="psxcontrol">
      <input type="text" name="{@paramName}"  id="perc-content-edit-{@paramName}"  title="{Value}" value="{Value}" readonly="readonly" style="background-color:#E6E6E9;overflow: hidden;text-overflow: ellipsis;">
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'sys_FilePath'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-file-field-select-button" value="Browse"/>
      <input type="button" for="perc-content-edit-{@paramName}" class="perc-file-field-clear-button" value="Clear"/>
   </xsl:template>

   <!--
    sys_tinymce
    -->
   <psxctl:ControlMeta name="sys_tinymce" dimension="single" choiceset="none">
      <psxctl:Description>Ephox EditLive HTML Editor</psxctl:Description>
      <psxctl:ParamList>
         <psxctl:Param name="width" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the inline frame. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is "960".</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="height" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the inline frame. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 250.</psxctl:Description>
            <psxctl:DefaultValue>250</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="config_src_url" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the location of the config.xml that will the control will use for configuration. This file must be in the /rx_resources/ephox folder.  The default value is "config.xml".</psxctl:Description>
            <psxctl:DefaultValue>/sys_resources/tinymce/config/default_config.json</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="css_file" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the location of the customer defined css file. This file must be in the /rx_resources/tinymce folder.</psxctl:Description>
            <psxctl:DefaultValue></psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
            <psxctl:DefaultValue>103</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
            <psxctl:DefaultValue>104</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="InlineVariantSlot" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the id of inline variant slot. The inline search dialog box shows the content types that have at least one variant added to the inline variant slot. The default value is system inline variant slotid 105.</psxctl:Description>
            <psxctl:DefaultValue>105</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>90%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>300</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
            <psxctl:DefaultValue>MODAL</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="helptext" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the help text to be used on a control</psxctl:Description>
            <psxctl:DefaultValue>This is the default helptext</psxctl:DefaultValue>
         </psxctl:Param>
      </psxctl:ParamList>
      <psxctl:AssociatedFileList>
         <psxctl:FileDescriptor name="bluebird.min.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>https://cdnjs.cloudflare.com/ajax/libs/bluebird/3.3.5/bluebird.min.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="tinymce.min.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/tinymce/js/tinymce/tinymce.min.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="tinymce_init.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/tinymce/js/tinymce_init.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="editorinline.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/editorinline.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="timepicker.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/timepicker.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="jquery.tinymce.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/tinymce/js/PercCustomStylesService.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
         <psxctl:FileDescriptor name="PercContentChecker.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>/sys_resources/js/PercContentChecker.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
         </psxctl:FileDescriptor>
      </psxctl:AssociatedFileList>
      <psxctl:Dependencies>
         <psxctl:Dependency status="setupOptional" occurrence="multiple">
            <psxctl:Default>
               <PSXExtensionCall id="0">
                  <name>Java/global/percussion/xmldom/sys_xdTextCleanup</name>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>$(fieldName)</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rxW2Ktidy.properties</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rxW2KserverPageTags.xml</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text/>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text/>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
                  <PSXExtensionParamValue id="0">
                     <value>
                        <PSXTextLiteral id="0">
                           <text>yes</text>
                        </PSXTextLiteral>
                     </value>
                  </PSXExtensionParamValue>
               </PSXExtensionCall>
            </psxctl:Default>
         </psxctl:Dependency>

      </psxctl:Dependencies>
   </psxctl:ControlMeta>
   <xsl:template match="Control[@name='sys_tinymce']" mode="psxcontrol-customcontrol-isdirty">
      <xsl:variable name="parentEl" select="name(parent::node())"/>
      <xsl:if test="$parentEl != 'Column'">
         <xsl:value-of select="concat(' || TinyMCEControlIsDirty_',@paramName,'()')"/>
      </xsl:if>
   </xsl:template>
   <xsl:template match="Control[@name='sys_tinymce']" priority="10" mode="psxcontrol">
      <xsl:variable name="name">
         <xsl:value-of select="@paramName"/>
      </xsl:variable>
      <xsl:variable name="width">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='width']">
               <xsl:value-of select="ParamList/Param[@name='width']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='width']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="height">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='height']">
               <xsl:value-of select="ParamList/Param[@name='height']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='height']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="config_src_url">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='config_src_url']">
               <xsl:value-of select="ParamList/Param[@name='config_src_url']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='config_src_url']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="css_file">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='css_file']">
               <xsl:value-of select="ParamList/Param[@name='css_file']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='css_file']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineLinkSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineImageSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineImageSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="InlineVariantSlot">
         <xsl:choose>
            <xsl:when test="ParamList/Param[@name='InlineVariantSlot']">
               <xsl:value-of select="ParamList/Param[@name='InlineVariantSlot']"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_tinymce']/psxctl:ParamList/psxctl:Param[@name='InlineVariantSlot']/psxctl:DefaultValue"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="validItemLocale">
         <xsl:choose>
            <xsl:when test="not($itemLocale = '')">
               <xsl:value-of select="$itemLocale"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$lang"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="communityName">
         <xsl:value-of select="//ItemContent/DisplayField/Control[@paramName='sys_communityid']/DisplayChoices/DisplayEntry[Value = ../../Value]/DisplayLabel"/>
      </xsl:variable>
      <xsl:variable name="typeName">
         <xsl:value-of select="substring-before(/ContentEditor/@submitHref,'.')"/>
      </xsl:variable>
      <xsl:variable name="childkeyid" select="../../@childkey"/>
      <xsl:variable name="childrow" select="../../../ActionLinkList/ActionLink/Param[@name='sys_childrowid']"/>
      <xsl:variable name="uniqueName">
         <xsl:choose>
            <xsl:when test="$childrow">
               <xsl:value-of select="$name"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat($name, $childkeyid)"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="readOnlyBoolean">
         <xsl:choose>
            <xsl:when test="@isReadOnly='yes'">true</xsl:when>
            <xsl:otherwise>false</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <script>
     <xsl:text disable-output-escaping="yes">
     //&lt;![CDATA[<![CDATA[

       function TinyMCEControlIsDirty_]]></xsl:text><xsl:value-of select="$uniqueName"/><xsl:text disable-output-escaping="yes"><![CDATA[()
       {
            for(i=0; i < tinyMCE.editors.length; i++)
            {
                if(tinyMCE.editors[i].isDirty())
                    return tinyMCE.editors[i].isDirty();
            }

            return false;
       }
      //  ]]>]]&gt;</xsl:text>

         <![CDATA[

       perc_tinymce_init({
       perc_config : "]]><xsl:value-of select="$config_src_url"/><![CDATA[",
       control_css : "]]><xsl:value-of select="$css_file"/><![CDATA[",
       height : "]]><xsl:value-of select="$height"/><![CDATA[",
       width : "]]><xsl:value-of select="$width"/><![CDATA[",
       mode : "textareas",
       readonly : ]]><xsl:value-of select="$readOnlyBoolean"/><![CDATA[,
       inlineLinkSlot : "]]><xsl:value-of select="$InlineLinkSlot"/><![CDATA[",
       inlineImageSlot : "]]><xsl:value-of select="$InlineImageSlot"/><![CDATA[",
       inlineVariantSlot : "]]><xsl:value-of select="$InlineVariantSlot"/><![CDATA[",
       editor_selector : "tinymce_]]><xsl:value-of select="$uniqueName"/><![CDATA[",
       perc_locale  : "]]><xsl:value-of select="$validItemLocale"/><![CDATA[",
       community  : "]]><xsl:value-of select="$communityName"/><![CDATA[",
       fieldName  : "]]><xsl:value-of select="$uniqueName"/><![CDATA[",
       typeName  : "]]><xsl:value-of select="$typeName"/><![CDATA[",
       userRoles : ]]><xsl:call-template name="rolejsarray"/><![CDATA[

    });

   ]]></script>
      <xsl:variable name="tinymcestyle" select="concat('width: ', $width, 'px; height: ', $height, 'px;', ' visibility:hidden;')" />

      <!-- <div>-->
      <textarea style="visibility:hidden;" name="{@paramName}" id="ce_{@paramName}{$uniqueName}" class="tinymce_{$uniqueName} tinymce" cols="10" row="10">
         <!--<xsl:attribute name="style">
                    <xsl:value-of select="$tinymcestyle" />
                 </xsl:attribute>-->
         <xsl:if test="@accessKey!=''">
            <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName+$uniqueName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
         </xsl:if>
         <xsl:value-of select="Value"/>
      </textarea>
      <!--</div>-->
   </xsl:template>

   <xsl:template name="rolejsarray"><xsl:text>[</xsl:text>
      <xsl:for-each select="//UserStatus/Roles/Role">
         <xsl:text>"</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
         <xsl:if test="position() != last()">
            <xsl:text>,</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
   </xsl:template>

   <psxi18n:lookupkeys>
      <key name="psx.contenteditor.sys_templates@Launch Word">Launch word link label</key>
      <key name="psx.contenteditor.sys_templates.sys_CalendarSimple.alt@Calendar Pop-up">Alt text for calendar image.</key>
      <key name="psx.contenteditor.sys_templates@Preview File">Preview file link label</key>
      <key name="psx.ce.sys_File@Clear">File clear check box label.</key>
      <key name="psx.contenteditor.sys_templates@Edit Content">Button label for editing the content.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
