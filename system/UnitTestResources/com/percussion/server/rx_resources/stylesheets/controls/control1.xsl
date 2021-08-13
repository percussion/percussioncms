<?xml version="1.0" encoding="UTF-8" ?>
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
	<psxctl:ControlMeta name="control1" dimension="single" choiceset="none">
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
	<xsl:template match="Control[@name='control1']" mode="psxcontrol">
		<input type="text" name="{@paramName}" value="{Value}">
			<xsl:if test="@accessKey!=''">
				<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
			</xsl:if>
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'control1'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
		</input>
	</xsl:template>
</xsl:stylesheet>
