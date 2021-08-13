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
   <xsl:variable name="this" select="/"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="Content-Type" content="text/html; UTF-8"/>
            <title>Item Validation Default Error Page</title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
         </head>
         <body>
            <table border="0" width="100%" cellpadding="0" cellspacing="1">
               <tr class="outerboxcell">
                  <td>
                     <table width="100%" cellpadding="0" cellspacing="1" border="0">
                        <tr class="headercell2">
                           <th width="100%" class="datacellnoentriesfound">
                              <b>Item validation errors detected:</b>
                           </th>
                        </tr>
                        <xsl:apply-templates select="*/ErrorSet" mode="mode1"/>
                     </table>
                  </td>
               </tr>
            </table>
            <br/>
            <tr>
               <td class="datacell1font">
                  Please go back to the Rhythmyx content administrator and make necessary corrections.
               </td>
            </tr>
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
   <xsl:template match="*" mode="mode0">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td width="20%" class="datacell1font">
               <xsl:apply-templates select="ErrorFieldSet/ErrorField/@displayName"/>
            </td>
            <td width="20%" class="datacell1font">
               <xsl:apply-templates select="ErrorFieldSet/ErrorField/@submitName"/>
            </td>
            <td width="60%" class="datacell1font">
               <xsl:apply-templates select="ErrorMessage"/>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*/ErrorSet" mode="mode1">
      <xsl:for-each select=".">
         <tr>
            <td width="100%">
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <tr class="headercell2">
                     <th width="20%" class="headercell2font">Field Display Name</th>
                     <th width="20%" class="headercell2font">Field Submit Name</th>
                     <th width="60%" class="headercell2font">Error Message</th>
                  </tr>
                  <xsl:apply-templates select="." mode="mode0"/>
               </table>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>
