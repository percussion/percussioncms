<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
       <html>
          <head>
             <meta http-equiv="Content-Type" content="text/html;      charset=UTF-8"/>
             <title>PersonList.htm</title>
          </head>

          <body bgcolor="#666699" text="#FFFFCC">
             <table>
                <tr>
                   <td>
                      <img src="Corplogo.gif" width="211" height="113"/>
                   </td>

                   <td align="center">
                      <b>
                         <font size="5">Employee Contact Directory</font>
                      </b>

                      <p>
                         <b>
                            <font size="4">Search Results</font>
                         </b>

                      </p>

                   </td>

                </tr>

             </table>

             <table width="100%" border="1" align="left" bgcolor="#9999CC" bordercolorlight="#000000" bordercolordark="#FFFFFF">
                <tr bgcolor="#000000" align="center">
                   <td width="15%">
                      <b>
                         <font color="#FFFFFF" size="2">Employee No</font>
                      </b>

                   </td>

                   <td>
                      <b>
                         <font color="#FFFFFF" size="2">First Name</font>
                      </b>

                   </td>

                   <td>
                      <b>
                         <font color="#FFFFFF" size="2">Last Name</font>
                      </b>

                   </td>

                   <td>
                      <b>
                         <font face="Arial, Helvetica, sans-serif" color="#FFFFFF" size="2">Telephone</font>
                      </b>

                   </td>

                </tr>

                <xsl:apply-templates select="PersonList/Person" mode="mode0"/>
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

    <xsl:template match="PersonList/Person" mode="mode0">
       <xsl:for-each select=".">
          <tr bgcolor="#C1C1F4">
             <td>
                <a>
                   <xsl:attribute name="href">
                      <xsl:value-of select="Link"/>
                   </xsl:attribute>

                   <xsl:apply-templates select="EmployeeID"/>
                </a>

             </td>

             <td>
                <b>
                   <font color="#000000" size="2">
                      <xsl:apply-templates select="Name/First"/>
                   </font>

                </b>

             </td>

             <td>
                <b>
                   <font color="#000000" size="2">
                      <xsl:apply-templates select="Name/Last"/>
                   </font>

                </b>

             </td>

             <td>
                <b>
                   <font color="#000000" size="2">
                      <xsl:apply-templates select="Telephone"/>
                   </font>

                </b>

             </td>

          </tr>

       </xsl:for-each>

    </xsl:template>

 </xsl:stylesheet>
