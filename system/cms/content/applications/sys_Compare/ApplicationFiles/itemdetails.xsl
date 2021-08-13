<?xml version="1.0" encoding="UTF-8"?>
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
   <xsl:variable name="variantlist" select="document(//variantlisturl)"/>
   <xsl:template match="/">
      <html>
         <head>
            <title>Rhythmyx - Document Comparison</title>
            <link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css"/>
         </head>
         <body leftmargin="0" topmargin="0">
            <xsl:apply-templates select="itemdetails"/>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="itemdetails">
      <form name="concat('itemdetails',//itemnumber)">
         <input type="hidden" name="revurl" value="concat(//selectrevurl,'&amp;itemnumber=',//itemnumber)"/>
         <table width="220" align="center" border="0" cellpadding="0" cellspacing="1" class="outerboxcell">
            <xsl:for-each select="item">
               <tr class="datacell2">
                  <td align="left" class="datacellfontheader" height="20">Title(ID)</td>
                  <td align="left" class="datacell1font">
                     <xsl:value-of select="title"/>(<xsl:value-of select="../sys_contentid"/>)</td>
               </tr>
               <tr class="datacell1">
                  <td align="left" class="datacellfontheader" height="20">Rev</td>
                  <td align="left" class="datacell1font">
                     <xsl:value-of select="../sys_revision"/>
                  </td>
               </tr>
               <tr class="datacell2">
                  <td align="left" class="datacellfontheader" height="20">Date</td>
                  <td align="left" class="datacell1font">
                     <xsl:value-of select="date"/>
                  </td>
               </tr>
               <tr class="datacell1">
                  <td align="left" class="datacellfontheader" height="20">Who</td>
                  <td align="left" class="datacell1font">
                     <xsl:value-of select="actor"/>
                  </td>
               </tr>
               <tr class="datacell2">
                  <td align="left" class="datacellfontheader" height="20">State</td>
                  <td align="left" class="datacell1font">
                     <xsl:value-of select="state"/>
                  </td>
               </tr>
               <tr class="datacell1">
                  <td align="left" class="datacellfontheader" height="20">Comment</td>
                  <td align="left" class="datacell1font">
                     <img src="../sys_resources/images/singlecomment.gif" alt="{comment}"/>
                  </td>
               </tr>
            </xsl:for-each>
            <xsl:apply-templates select="$variantlist//VariantList" mode="variantlist"/>
            <tr class="datacell2">
               <td width="100%" align="center" valign="middle" colspan="2" class="datacell1font" height="30">
                  <input type="button" name="change" value="Select Revision">
                     <xsl:attribute name="onclick">javascript:openSelectRev("<xsl:value-of select="//selectrevurl"/>");</xsl:attribute>
                  </input>
               </td>
            </tr>
         </table>
      </form>
   </xsl:template>
   <xsl:template match="VariantList" mode="variantlist">
      <tr class="datacell1">
         <td colspan="2" align="left" class="datacell1font" height="20">Select Template</td>
      </tr>
      <tr class="datacell2">
         <td colspan="2" align="center" class="datacell1font" height="20">
            <select>
               <xsl:for-each select="Variant">
                  <option>
                     <xsl:value-of select="DisplayName"/>
                  </option>
               </xsl:for-each>
            </select>
         </td>
      </tr>
   </xsl:template>
</xsl:stylesheet>
