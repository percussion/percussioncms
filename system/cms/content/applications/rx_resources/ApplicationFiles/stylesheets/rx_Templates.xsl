<?xml version="1.0" encoding="UTF-8" ?>
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
	<xsl:template match="/" />
	<!--
     sys_FileWord. 
     Do not modify this control directly. This control, word template file and cab files need to be modified together.
     Please see read me or help for upgrading the word controls. 
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
</xsl:stylesheet>
