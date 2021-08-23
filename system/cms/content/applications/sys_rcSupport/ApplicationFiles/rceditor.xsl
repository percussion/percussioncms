<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html [
   <!ENTITY % HTMLlat1 SYSTEM "./../../DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "./../../DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "./../../DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:include href="file:sys_resources/stylesheets/relatedcontentctrl.xsl"/>
   <xsl:output method="html"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_rcSupport.rceditor@Related Content Control'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			<script language="javascript" src="../tmx/tmx.jsp?sys_lang={$lang}">;</script>
			<script src="../sys_resources/dojo/dojo.js">;</script>
			<script src="../sys_resources/ps/content/History.js">;</script>
			<script src="../sys_resources/ps/content/SelectTemplates.js">;</script>
            <script language="javascript"><![CDATA[
			    /**
                 * Global variables needed for the browse dialog
                 */
               var ___cBackFunction = null;
               var ___slotId = null;
               var ___bws = null;
               var ___bwsMode = ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR;
               var __rxroot = "/Rhythmyx";

               function onClose()
               {
                  self.close();
               }
               function onClickEdit(url)
               {
                  if(window.opener && !window.opener.closed)
                  {
                     if(window.opener.contentEditor)
                     {
                        window.opener.location.href = url;
                        self.close(); 
                      }
                      else
                      {
                         var cmd = "setTimeout(\"changeWindowUrl('" + url + "')\", 500)";
                         eval(cmd);
                      }
                  }
                  
               }
               function changeWindowUrl(url)
               {
                  window.location.href = url;
               }       
               function go(url)
               {
                  httpcaller = escape(window.location.href);
                  window.location.href = url + "&httpcaller=" + httpcaller; 
               }
			   function ___openSearch(slotid, url)
			   {
                   var cId = ]]><xsl:value-of select="//@contentid"/>
				   <xsl:choose>
				      <xsl:when test="//@inlineSearchUsesContentBrowser = 'false'">
                                         var useContentBrowser = false;
				      </xsl:when>
				      <xsl:otherwise>
                                         var useContentBrowser = true;
				      </xsl:otherwise>
				   </xsl:choose>

				   <![CDATA[;
				   if(useContentBrowser)
				   {
					   var response = ps.io.Actions.getContentTypeByContentId(cId);
					   if(response.isSuccess())
					  {
						 var cType = response.getValue().sys_contenttypeid;
						 ___slotId ='["1","'
						   + cId + '",null,null,null,null,null,"' 
	                       + cType + '",null,"' 
	                       + slotid + '",null,null,null,null,null]';
                         ___cBackFunction = function()
						     {
							    window.location.reload();
							 };
                         ___bws = window.open(ps.util.CONTENT_BROWSE_URL, "contentBrowerDialog",
		                    "resizable=1;status=0,toolbar=0,scrollbars=0,menubar=0,location=0,directories=0,width=750,height=500"); 
					   }
					  else
					  {
						 ps.io.Actions.maybeReportActionError(response);
						 return;
					  }
				   }
				   else
				   {
                       // Open legacy search
					   window.open(url, "searchitems","toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=400,z-lock=1");
				   }
			   }			   
            ]]></script>
         </head>
         <body onload="self.focus()">
            <xsl:apply-templates select="document(/*/@ContentSlotLookupURL)/*" mode="relatedcontentctrl">
               <xsl:with-param name="relateddoc" select="document(/*/@RelatedLookupURL)"/>
               <xsl:with-param name="mode" select="@mode"/>
               <xsl:with-param name="editable" select="'yes'"/>
            </xsl:apply-templates>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_rcSupport.rceditor@Related Content Control">Title for Related Content edit table.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
