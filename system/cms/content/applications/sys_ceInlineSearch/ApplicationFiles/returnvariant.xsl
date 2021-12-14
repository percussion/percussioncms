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
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="this" select="/"/>
	<xsl:strip-space elements="*"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.0"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.returnvariant@Rhythmyx - Inline Content Search Variant Output'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('/rx_resources/css/',$lang,'/templates.css')}"/>
				<xsl:if test="//@inlinetype='rxvariant'">
					<xsl:variable name="variantbody" select="/*/body/*"/>
					<xsl:variable name="tmp1">
						<xsl:apply-templates select="$variantbody" mode="copy"/>
					</xsl:variable>
					<xsl:element name="script">
						<xsl:attribute name="language">javascript</xsl:attribute>
               var output = '<xsl:copy-of select="$tmp1"/>';
               <![CDATA[
               // Replace script start and end tokens with the real markup
               output = output.replace(/\@RX\_SCRIPT\_START\@/g, "<script");
	       output = output.replace(/\@RX\_SCRIPT\_END\@/g, "</script");
	       ]]>
            </xsl:element>
				</xsl:if>
				<script><![CDATA[
            var editorname = "";
            
            if(top.opener!=null && top.opener.INLINE_SEARCH_PAGE!=null)
            {
               if(top.opener.isEditLive)
               {
                  editorname = "editlive";
               }
               else
               {
                  editorname = "ektroneditor";
               }
            }
            else
            {
               editorname = "dhtmleditor";
            }
            function returnResult()
            {
               var temp = "";
               var variantAttribs = " contenteditable=\"false\" class=\"rx_ephox_inlinevariant\" style=\"display: inline\"";
               var attrstr  = " rxinlineslot=\"" + inlineslotid + "\" sys_dependentvariantid=\"" + varid + "\" sys_dependentid=\"" + conid + "\" sys_siteid=\"" + siteid + "\" sys_folderid=\"" + folderid + "\"";
               if(inlinetype !="")
                  attrstr += " inlinetype=\"" + inlinetype + "\"";
				if(inlinetype == "rxpopuplink")
				{
				output  = "<a title=\"" + linktitle + "\" href=\"" + encodeAmpersand(urlstring) + "\"" + attrstr + " popup=\"" + "yes" + "\">" + inlinetext + "</a>";
				     returnOutput(output);
				}
            	if(inlinetype == "rxhyperlink")
            	{
            		output  = "<a title=\"" + linktitle + "\" href=\"" + encodeAmpersand(urlstring) + "\"" + attrstr + ">" + inlinetext + "</a>";
            		returnOutput(output);
            	}
            	else if(inlinetype == "rximage")
            	{
                  if(height != "" && width != "")
                     attrstr += " height=\"" + height + "\" width=\"" + width + "\"";
                  output  = "<img title=\"" + linktitle + "\" src=\"" + encodeAmpersand(urlstring) + "\"" + attrstr + " />";
            		returnOutput(output);
            	}
            	else if(inlinetype == "rxvariant")
            	{
                  var isDiv = output.substr(1, 3).toLowerCase() == "div";
                  temp = output.substring(0,output.indexOf(">"));
		  var hasClass = temp.toLowerCase().indexOf("class=") != -1;
		  var noeditvariant = "";
                  if(inlinetext != "")
                     attrstr += " rxselectedtext=\"" + unicode2entity(escape(inlinetext)) + "\"";
		  if(isDiv && !hasClass)
		  {
                     temp += attrstr + variantAttribs;
		  }
		  else
		  {
                     noeditvariant = "<div" + attrstr + variantAttribs + ">";
		  }
                  temp += output.substring(output.indexOf(">"));
            	  if(editorname == "editlive" && noeditvariant != "")
            	     temp = noeditvariant + temp + "</div>";
            	  returnOutput(temp);
               }
            }
	    // Converts unicode chars into the hexidecimal reference
	    // entity
	    function unicode2entity(str)
	    {
	      var sPos = 0;
	      var ePos = -1;
	      var result = "";

	      while((sPos = str.indexOf("%u", sPos)) != -1)
	      {
		 if(ePos == -1)
		 {
		    ePos = 0;
		    result += str.substring(ePos, sPos);
		 }
		 else if(sPos - ePos > 0)
		 {
		    result += str.substring(ePos + 1, sPos);
		 }
		 result += "&#x" + str.substr(sPos + 2, 4) + ";";
		 sPos += 5;
		 ePos = sPos;

	      }
	      result += str.substring(ePos + 1);

	      return result;
	    }


            
            function returnOutput(output)
            {
               closeWindowthrCancel = 0;
               if(top.opener != null)
               {
                  if(editorname == "ektroneditor" || editorname == "editlive")
                  {
                     top.opener.formatOutput(output);
                  }
                  else
                  {
                     window.returnValue=output;
                  }
               }
               setTimeout("self.close()", 500);
            }
            function encodeAmpersand(urlstring)
            {
               var re = new RegExp("&amp;","g");
               urlstring = urlstring.replace(re,"&");
               var re1 = new RegExp("&","g");
               urlstring = urlstring.replace(re1,"&amp;");
               return urlstring;
            }
            ]]></script>
			</head>
			<body onload="javascript:returnResult();">
				<form name="returnvariant">
					<input type="hidden" name="inlinetext" value="{//@inlinetext}"/>
					<input type="hidden" name="dependentvariantid" value="{//@dependentvariantid}"/>
					<input type="hidden" name="dependentid" value="{//@dependentid}"/>
					<input type="hidden" name="sys_siteid" value="{//@sys_siteid}"/>
					<input type="hidden" name="sys_folderid" value="{//@sys_folderid}"/>
					<input type="hidden" name="inlineslotid" value="{//@inlineslotid}"/>
					<input type="hidden" name="inlinetype" value="{//@inlinetype}"/>
					<input type="hidden" name="urlstring" value="{//@urlstring}"/>
					<input type="hidden" name="height" value="{//@height}"/>
					<input type="hidden" name="width" value="{//@width}"/>
					<input type="hidden" name="linktitle" value="{//@linktitle}"/>
				</form>
				<xsl:element name="script">
					<xsl:attribute name="language">javascript</xsl:attribute>
						var varid = 	document.returnvariant.dependentvariantid.value;
						var conid = document.returnvariant.dependentid.value;
						var siteid = document.returnvariant.sys_siteid.value;
						var folderid = document.returnvariant.sys_folderid.value;
						var inlineslotid = document.returnvariant.inlineslotid.value;
						var inlinetype = document.returnvariant.inlinetype.value;
						var inlinetext = document.returnvariant.inlinetext.value;
						var urlstring = document.returnvariant.urlstring.value;
						var height = document.returnvariant.height.value;
					var width = document.returnvariant.width.value;
					        var linktitle = document.returnvariant.linktitle.value;
            		</xsl:element>
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
					<tr class="outerboxcell">
						<td align="center" valign="middle" class="outerboxcellfont">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.generic@Your request is being processed'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>. 
                     <xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.generic@Please wait a moment'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>...
                  </td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="text()">
				<xsl:choose>
					<xsl:when test="@no-escaping">
						<xsl:value-of select="." disable-output-escaping="yes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>&nbsp;</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="attribute::*">
		<xsl:value-of select="."/>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			   <xsl:if test="not(name()='li' or name()='ul' or name()='ol')">
			      <xsl:attribute name="contenteditable">false</xsl:attribute>
			      <xsl:attribute name="unselectable">on</xsl:attribute>
	           </xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="text()" mode="copy">
		<xsl:variable name="temp">
			<xsl:call-template name="replace-string">
				<xsl:with-param name="text" select="." />
				<xsl:with-param name="replace" select='"&#39;"' />
				<xsl:with-param name="with" select='"\&#39;"' />
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="replace-string">
			<xsl:with-param name="text" select="$temp" />
			<xsl:with-param name="replace" select='"&#10;"' />
			<xsl:with-param name="with" select='"\n"' />
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="@*" mode="copy">
		<xsl:attribute name="{name()}">
			<xsl:call-template name="replace-string">
				<xsl:with-param name="text" select="." />
				<xsl:with-param name="replace" select='"&#39;"' />
				<xsl:with-param name="with" select='"\&#39;"' />
			</xsl:call-template>
      </xsl:attribute>
	</xsl:template>
<!-- Generic template that searches for a supplied string  in the supplied text and then replaces with another supplied string -->
<!-- Parameters: -->
<!-- text    - text in which search and replace is needed -->
<!-- replace - string to search for -->
<!-- with    - string to replace with -->
   <xsl:template name="replace-string">
      <xsl:param name="text"/>
      <xsl:param name="replace"/>
      <xsl:param name="with"/>
      <xsl:variable name="stringText" select="string($text)"/>
      <xsl:choose>
         <xsl:when test="contains($stringText,$replace)">
            <xsl:value-of select="substring-before($stringText,$replace)"/>
            <xsl:value-of select="$with"/>
            <xsl:call-template name="replace-string">
               <xsl:with-param name="text" select="substring-after($stringText,$replace)"/>
               <xsl:with-param name="replace" select="$replace"/>
               <xsl:with-param name="with" select="$with"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$stringText"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <psxi18n:lookupkeys>
		<key name="psx.sys_ceInlineSearch.returnvariant@Rhythmyx - Inline Content Search Variant Output">Title for inline content search variant output page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
