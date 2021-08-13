<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
  <xsl:output method="xml"/>
  <xsl:variable name="this" select="/"/>
  <xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
  <xsl:variable name="componentcontext" select="/*/contexturl"/>
  <xsl:variable name="userroles" select="/*/userrolesurl"/>
  <xsl:variable name="userroles1" select="document(/*/userrolesurl)/UserStatus" /> 
  <xsl:variable name="componentcontext1" select="document(/*/contexturl)/componentcontext/context"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>New Document</title>
      </head>
      <body>        <!--     psx-docalias="newcontent" psx-docref="psx-newcontenturl"     -->
        <!--     psx-docalias="mycontent" psx-docref="psx-mycontenturl"     -->
        <!--     psx-docalias="allcontent" psx-docref="psx-allcontenturl"     -->
        <!--     psx-docalias="userroles" psx-docref="psx-userrolesurl"     -->
        <!--     psx-docalias="componentcontext" psx-docref="psx-contexturl"     -->

        <table height="100%" width="150" cellpadding="4" cellspacing="0" border="0">
          <xsl:attribute name="id">
            <xsl:value-of select="*/x"/>
          </xsl:attribute>
          <xsl:apply-templates select="*" mode="mode0">
               <xsl:with-param name="componentcontext1" select="$componentcontext1"/>
               <xsl:with-param name="userroles1" select="$userroles1"/>
          </xsl:apply-templates>
          <tr class="outerboxcell">
            <td height="100%">&nbsp;</td>
           <!--   Fill down to the bottom   -->
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="*" mode="mode0">
   <xsl:param name="componentcontext1"/>
   <xsl:param name="userroles1"/>
   <xsl:for-each select=".">
      <tr>
        <td valign="top" class="outerboxcell">          <!--   Function Boxes Start   -->
          <table width="100%" cellpadding="0" cellspacing="0" class="outerboxcell" border="0">
            <xsl:attribute name="id">
              <xsl:value-of select="."/>
            </xsl:attribute>
            <tr>
			      <td valign="top">
						<xsl:for-each select="document($relatedlinks)/*/component">
							<xsl:copy-of select="document(url)/*/body/*" />
						</xsl:for-each>
			      </td>
            </tr>
				<xsl:if test="not(workflowid='')">
				<tr class="datacell1">
			      <td valign="middle" class="datacell1" height="21" align="center">
						<a valign="middle">
							<xsl:attribute name="href">
								<xsl:text>javascript:{}</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="onClick">
								<xsl:text>window.open('</xsl:text>
								<xsl:value-of select="previewlink"/>
								<xsl:text>', 'previewworkflow', 'width=860,height=500,location=0;0,toolbar=no,menubar=no,scrollbars=yes,resizable=yes')</xsl:text>
							</xsl:attribute>
							<img src="../sys_resources/images/preview_button.gif" width="145" height="20" border="0" alt="Preview Workflow" title="Preview Workflow"/>
						</a>
					</td>
            </tr>
				</xsl:if>
            <tr class="outerboxcell">
            <td class="datacell1font" align="center">
              <br id="XSpLit"/>(c) Percussion Software 1999-@COPYRIGHTYEAR@
            </td>
            </tr>
          </table>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
