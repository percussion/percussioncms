<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.0"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>History Bar lookup</title>
      </head>

      <body>
        <table border="0" cellspacing="1" cellpadding="1" bgcolor="#ffffff" align="center">
          <tr>
            <td colspan="16" bgcolor="FFFFFF" class="largeheadline">ContentStatus History for contentid:</td>
          </tr>

          <xsl:apply-templates select="*/contentid" mode="mode0"/>
          <tr bgcolor="#eeeeee">
            <td class="table1">Id</td>
            <td class="table1">ContentId</td>
            <td class="table1">Actor</td>
            <td class="table1">Valid</td>
            <td class="table1">State</td>
            <td class="table1">TransitionID</td>
            <td class="table1">TransitionDesc</td>
            <td class="table1">RoleName</td>
            <td class="table1">CheckOutUserName</td>
            <td class="table1">LastModifierName</td>
            <td class="table1">LastModifiedDate</td>
            <td class="table1">EventTime</td>
            <td class="table1">WorkflowAppID</td>
            <td class="table1">Title</td>
            <td class="table1">Revision</td>
            <td class="table1">Valid</td>
          </tr>

          <xsl:apply-templates select="*/historyentry" mode="mode1"/>
          <tr>
            <td id="XSpLit"/>
            <td id="XSpLit"/>
            <td id="XSpLit"/>
            <td id="XSpLit"/>
            <td id="XSpLit"/>
            <td id="XSpLit"/>
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

  <xsl:template match="*/contentid" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
          <xsl:apply-templates select="."/>
        </td>

        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
        <td id="XSpLit"/>
      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*/historyentry" mode="mode1">
    <xsl:for-each select=".">
      <tr>
        <td>
          <xsl:apply-templates select="historyid"/>
        </td>

        <td>
          <xsl:apply-templates select="historycontentid"/>
        </td>

        <td>
          <xsl:apply-templates select="historyactor"/>
        </td>

        <td>
          <xsl:apply-templates select="historyvalid"/>
        </td>

        <td>
          <xsl:apply-templates select="historystate"/>
        </td>

        <td>
          <xsl:apply-templates select="historytransitionid"/>
        </td>

        <td>
          <xsl:apply-templates select="historytransitiondesc"/>
        </td>

        <td>
          <xsl:apply-templates select="historyrolename"/>
        </td>

        <td>
          <xsl:apply-templates select="historycheckoutusername"/>
        </td>

        <td>
          <xsl:apply-templates select="historylastmodifiername"/>
        </td>

        <td>
          <xsl:apply-templates select="historylastmodifieddate"/>
        </td>

        <td>
          <xsl:apply-templates select="historyeventtime"/>
        </td>

        <td>
          <xsl:apply-templates select="historyworkflowappid"/>
        </td>

        <td>
          <xsl:apply-templates select="historytitle"/>
        </td>

        <td>
          <xsl:apply-templates select="historyrevision"/>
        </td>

        <td>
          <xsl:apply-templates select="historyvalid"/>
        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
