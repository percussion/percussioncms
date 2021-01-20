<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!--
		Default table-based generic style sheet,
		can be used to generate table-centric view of XML data.
		Handles product specific page request nodes.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <HTML>
      <HEADER>
        <TITLE><xsl:value-of select="local-name(./*[1])"/></TITLE>
      </HEADER>
      <BODY>
     		<xsl:for-each select="./*">
				<TABLE BORDER="1">
		 	   	<CAPTION><B><xsl:value-of select="local-name(.)"/></B></CAPTION>
        			<xsl:apply-templates select="./*"/>
        	   </TABLE>
		   </xsl:for-each>

			<P><CENTER><TABLE BORDER="0">
				<TR>
					<TD>
						<xsl:choose>
							<xsl:when test="./*/PSXPrevPage">
								<A><xsl:attribute name="HREF"><xsl:value-of select="./*/PSXPrevPage"/></xsl:attribute>Prev</A>
							</xsl:when>
							<xsl:when test="not(./*/PSXNextPage)"/>
							<xsl:otherwise>Prev</xsl:otherwise>
						</xsl:choose>
					</TD>
					<xsl:for-each select="./*/PSXIndexPage">
							<TD><A><xsl:attribute name="HREF"><xsl:value-of select="."/></xsl:attribute><xsl:value-of select="@pagenum"/></A></TD>
					</xsl:for-each>
					<TD>
						<xsl:choose>
							<xsl:when test="./*/PSXNextPage">
								<A><xsl:attribute name="HREF"><xsl:value-of select="./*/PSXNextPage"/></xsl:attribute>Next</A>
							</xsl:when>
							<xsl:when test="not(./*/PSXPrevPage)"/>
							<xsl:otherwise>Next</xsl:otherwise>
						</xsl:choose>
					</TD>
				</TR>
			</TABLE></CENTER></P>
      </BODY>
    </HTML>
  </xsl:template>

  <xsl:template match="*">
  	 <xsl:choose>
		 <xsl:when test="./*/*">
		 	 <TR>
			 <TH><xsl:value-of select="local-name(.)"/></TH>
			 <TD>
		 	 <TABLE BORDER="1">
				 <TR>
				 	<xsl:apply-templates select="./*"/>
				 </TR>
			 </TABLE>
			 </TD>
		 	 </TR>
		 </xsl:when>
		 <xsl:when test="./*">
		 	 <TR>
			 <TH><xsl:value-of select="local-name(.)"/></TH>
			 <TD>
		 	 <TABLE BORDER="1">
				 <TR>
				 	<xsl:apply-templates select="./*" mode="headers"/>
				 </TR>
				 <TR>
				 	<xsl:apply-templates select="./*" mode="data"/>
				 </TR>
			 </TABLE>
			 </TD>
		 	 </TR>
		 </xsl:when>
		 <xsl:otherwise>
		    <TR><TH><xsl:value-of select="local-name(.)"/></TH><TD><xsl:value-of select="."/></TD></TR>
		 </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*" mode="headers">
	 <TH>
	 	<xsl:value-of select="local-name(.)"/>
	 </TH>
  </xsl:template>

  <xsl:template match="*" mode="data">
 	 <TD>
  	 	<xsl:choose>
		 	<xsl:when test="./*">
		 	 	<TABLE BORDER="1">
			 	 	<CAPTION><B><xsl:value-of select="local-name(.)"/></B></CAPTION>
				 	<TR>
				 		<xsl:apply-templates select="./*" mode="headers"/>
				 	</TR>
				 	<TR>
				 		<xsl:apply-templates select="./*" mode="data"/>
				 	</TR>
			 	</TABLE>
		 	</xsl:when>
		 	<xsl:otherwise>
		    	<xsl:value-of select="."/>
		 	</xsl:otherwise>
    	</xsl:choose>
 	 </TD>
  </xsl:template>

  <xsl:template match="PSXPrevPage">
  </xsl:template>

  <xsl:template match="PSXIndexPage">
  </xsl:template>

  <xsl:template match="PSXNextPage">
  </xsl:template>
</xsl:stylesheet>

