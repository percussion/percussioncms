<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
       <html>
          <head>
             <meta http-equiv="Content-Type" content="text/html;      charset=UTF-8"/>
             <title>Person.htm</title>
          </head>

          <body bgcolor="#666699" text="#FFFFCC" alink="#66CCFF" vlink="#99CCFF" link="#FFCC66">
             <p>
                <font face="Verdana, Arial, Helvetica, sans-serif">
                   <b>
                      <font size="4">Global Enterprises</font>
                   </b>

                   <a href="Welcome.htm">
                      <font size="4">Employee Contact Directory</font>
                   </a>

                </font>

             </p>

             <table width="100%" border="0" height="143">
                <xsl:apply-templates select="Person" mode="mode2"/>
             </table>

          </body>

       </html>

    </xsl:template>

    <xsl:template match="*">
       <xsl:choose>
          <xsl:when test="text()">
             <xsl:value-of select="."/>
          </xsl:when>

          <xsl:otherwise>&nbsp;
</xsl:otherwise>

       </xsl:choose>

       <xsl:if test="not(position()=last())">
          <br id="XSpLit"/>
       </xsl:if>

    </xsl:template>

    <xsl:template match="Telephone" mode="mode0">
       <xsl:for-each select=".">
          <tr>
             <td align="right">
                <b>
                   <font size="3" color="#000000">Telephone:</font>
                </b>

             </td>

             <td>
                <b>
                   <font size="3" color="#000000">
                      <xsl:apply-templates select="."/>
                   </font>

                </b>

             </td>

          </tr>

       </xsl:for-each>

    </xsl:template>

    <xsl:template match="Email" mode="mode1">
       <xsl:for-each select=".">
          <tr>
             <td align="right">
                <b>
                   <font size="3" color="#000000">Email:</font>
                </b>

             </td>

             <td>
                <b>
                   <font size="3" color="#000000">
                      <xsl:apply-templates select="."/>
                   </font>

                </b>

             </td>

          </tr>

       </xsl:for-each>

    </xsl:template>

    <xsl:template match="Person" mode="mode2">
       <xsl:for-each select=".">
          <tr bgcolor="#666699">
             <td width="30%">
                <img src="Corplogo.gif" width="211" height="113"/>
                <br id="XSpLit"/>
             </td>

             <td>
                <font size="6">
                   <xsl:apply-templates select="Name/First"/>
                   <xsl:apply-templates select="Name/Last"/>
                </font>

                <br id="XSpLit"/>
                <font size="4">
                   <xsl:apply-templates select="Title"/>
                </font>

                <hr id="XSpLit"/>
                <table width="100%" bgcolor="#9999CC">
                   <xsl:apply-templates select="Telephone" mode="mode0"/>
                   <xsl:apply-templates select="Email" mode="mode1"/>
                </table>

             </td>

          </tr>

       </xsl:for-each>

    </xsl:template>

 </xsl:stylesheet>
