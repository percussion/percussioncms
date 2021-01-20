<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
       <xsl:variable name="data" select="//tabledataset" />
   
   <xsl:template match="datapublisher">
      <xsl:apply-templates select="*" />
   </xsl:template>

   <xsl:template match="//tabledefset">
      <html>
         <head>
            <title>Database Publishing Preview</title>
            <style type="text/css">
               .evenrow {
                  background-color: #DDDDDD;
                  color: black
               }
               .oddrow {
                  background-color: white;
                  color: black
               }
               .headerrow {
                  background-color: #0020a0;
                  color: white
               }
               table {
                  font-family: arial, sans-serif; font-size: 10pt
               }
               th { 
                  margin-left: 5px;
               }
               td  {
                  border-left: 1px solid black;
                  margin: 2px;
               }
               table  {
                  border: 1px solid black
               }
               h3 {
                  font-family: arial, sans-serif; font-size: 12pt; font-weight: bold
               }
            </style>
         </head>
         <body>
            <xsl:for-each select="tabledef">
               <xsl:call-template name="table">
                  <xsl:with-param name="name" select="./@name"/>
                  <xsl:with-param name="keys" select="./primarykey" />
               </xsl:call-template>
            </xsl:for-each>
         </body>
      </html>
   </xsl:template>
   
   <xsl:template match="//tabledataset">
      <!-- ignore -->
   </xsl:template>
   
   <xsl:template name="table">
      <xsl:param name="name"/>
      <xsl:param name="keys"/>
      <xsl:variable name="rowdef" select="rowdef"/>
      <h3>Table <xsl:value-of select="$name"/></h3>
      <table cellpadding="1" cellspacing="0" border="0" >
         <tbody>
            <tr class="headerrow">
               <th align="left">Column Name</th>
               <th align="left">Data Type</th>
               <th align="left">Required</th>
               <th align="left">Primary Key</th>
            </tr>
            <xsl:for-each select="$rowdef/columndef">
               <xsl:choose>
                  <xsl:when test="position() mod 2 = 0">
                     <xsl:call-template name="row">
                        <xsl:with-param name="name" select="@name"/>
                        <xsl:with-param name="keys" select="$keys"/>
                        <xsl:with-param name="rowclass">evenrow</xsl:with-param>
                     </xsl:call-template>                   
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:call-template name="row">
                        <xsl:with-param name="name" select="@name"/>
                        <xsl:with-param name="keys" select="$keys"/>
                        <xsl:with-param name="rowclass">oddrow</xsl:with-param>
                     </xsl:call-template>                   
                  </xsl:otherwise>           
               </xsl:choose>
            </xsl:for-each>
         </tbody>
      </table>
      <br/>
      <table cellpadding="1" cellspacing="0" border="0" >
         <tbody>
            <tr class="headerrow">
               <xsl:for-each select="$rowdef/columndef">
                  <th align="left"><xsl:value-of select="@name"/></th>
               </xsl:for-each>
            </tr>
            <xsl:for-each select="$data/table[@name = $name]/row | $data//childtable[@name = $name]/row">
               <xsl:choose>
                  <xsl:when test="position() mod 2 = 0">
                     <xsl:call-template name="datarow">
                        <xsl:with-param name="name" select="@name"/>
                        <xsl:with-param name="value" select="."/>
                        <xsl:with-param name="rowclass">evenrow</xsl:with-param>
                        <xsl:with-param name="rowdef" select="$rowdef"/>
                     </xsl:call-template>                   
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:call-template name="datarow">
                        <xsl:with-param name="name" select="@name"/>
                        <xsl:with-param name="value" select="."/>
                        <xsl:with-param name="rowclass">oddrow</xsl:with-param>
                        <xsl:with-param name="rowdef" select="$rowdef"/>
                     </xsl:call-template>                   
                  </xsl:otherwise>           
               </xsl:choose>
            </xsl:for-each>
         </tbody>
      </table>
   </xsl:template>
   
   <xsl:template name="row">
      <xsl:param name="name"/>
      <xsl:param name="keys"/>
      <xsl:param name="rowclass"/>
      <tr>
         <xsl:attribute name="class">
            <xsl:value-of select="$rowclass"/>
         </xsl:attribute>
         <th align="left"><xsl:value-of select="@name"/></th>
         <td><xsl:value-of select="jdbctype"/></td>
         <td>
            <xsl:choose>
               <xsl:when test="allowsnull = 'yes'">no</xsl:when>
               <xsl:otherwise>yes</xsl:otherwise>
            </xsl:choose>
         </td>
         <td>
            <xsl:choose>
               <xsl:when test="$keys/name = @name">yes</xsl:when>
               <xsl:otherwise>no</xsl:otherwise>
            </xsl:choose>
         </td>
      </tr>                   
   </xsl:template>
   
   <xsl:template name="datarow">
      <xsl:param name="name"/>
      <xsl:param name="value"/>
      <xsl:param name="rowclass"/>
      <xsl:param name="rowdef"/>
      <tr>
         <xsl:attribute name="class">
            <xsl:value-of select="$rowclass"/>
         </xsl:attribute>
         <xsl:for-each select="$rowdef/columndef">
            <xsl:call-template name="datavalue">
               <xsl:with-param name="value" select="$value"/>
               <xsl:with-param name="name" select="./@name"/>
            </xsl:call-template>
         </xsl:for-each>
      </tr>
   </xsl:template>
   
   <xsl:template name="datavalue">
      <xsl:param name="value"/>
      <xsl:param name="name"/>
      <xsl:variable name="data" select="$value/column[@name = $name]"/>
      <td>
         <xsl:choose>
            <xsl:when test="string-length($data) > 100"><xsl:value-of select="substring($data,0,100)"/>...</xsl:when>
            <xsl:otherwise><xsl:value-of select="$data"/></xsl:otherwise>
         </xsl:choose>
      </td> 
   </xsl:template>
   
   <xsl:template match="*">
      <xsl:copy>
         <xsl:apply-templates select="node()  | @*"/>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="@* | text() | comment() | processing-instruction()">
      <xsl:copy/>
   </xsl:template>
</xsl:stylesheet>
