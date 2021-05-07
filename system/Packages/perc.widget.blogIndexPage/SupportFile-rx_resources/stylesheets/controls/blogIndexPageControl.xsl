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
	<psxctl:ControlMeta name="blogIndexPageControl" dimension="array" choiceset="required">
		<psxctl:Description>The Blog Index Page Control.</psxctl:Description>
        <psxctl:AssociatedFileList>
        				<psxctl:FileDescriptor name="jquery-ui.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/jquery-ui.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>		
			<psxctl:FileDescriptor name="PSJSUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/PSJSUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>						
			<psxctl:FileDescriptor name="jquery.validate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/jquery.validate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_extend_jQueryValidate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_extend_jQueryValidate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>					
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/Jeditable.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/PSJSUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>	
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>						
            <psxctl:FileDescriptor name="jquery.blogIndexPage.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/blogIndexPage/js/jquery.blogIndexPage.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            
        </psxctl:AssociatedFileList>       
	</psxctl:ControlMeta>
   <xsl:template match="Control[@name='blogIndexPageControl']" priority="10" mode="psxcontrol">
      <input type="hidden" name="{@paramName}" value="{Value}">
         <xsl:call-template name="parametersToAttributes">
            <xsl:with-param name="controlClassName" select="'blogIndexPageControl'"/>
            <xsl:with-param name="controlNode" select="."/>
         </xsl:call-template>
      </input>
   </xsl:template>	
</xsl:stylesheet>
