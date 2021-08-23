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
        <title>Dependency</title>
      </head>

      <body>
        <table>
          <tr>
            <td>contentid</td>
            <td>revision</td>
            <td>Title</td>
            <td>ContentType</td>
            <td>Contenttypeid</td>
            <td>Workflow ID</td>
            <td>Workflow State</td>
            <td>Workflow Stateid</td>
            <td>StateValid</td>
            <td>Link to Children</td>
            <td>Preview Link</td>
          </tr>

          <xsl:apply-templates select="*/Item" mode="mode0"/>
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

  <xsl:template match="*/Item" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
          <xsl:apply-templates select="@contentid"/>
        </td>

        <td>
          <xsl:apply-templates select="@revision"/>
        </td>

        <td>
          <xsl:apply-templates select="Title"/>
        </td>

        <td>
          <xsl:apply-templates select="ContentType"/>
        </td>

        <td>
          <xsl:apply-templates select="ContentType/@contenttypeid"/>
        </td>

        <td>
          <xsl:apply-templates select="Workflow/@Appid"/>
        </td>

        <td>
          <xsl:apply-templates select="Workflow/State"/>
        </td>

        <td>
          <xsl:apply-templates select="Workflow/@stateid"/>
        </td>

        <td>
          <xsl:apply-templates select="Workflow/@stateValid"/>
        </td>

        <td>
          <xsl:apply-templates select="ChildLink"/>
        </td>

        <td>
          <xsl:apply-templates select="PreviewLink"/>
        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
