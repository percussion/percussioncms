<?xml version='1.0'?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) Percussion Software, Inc.  1999-2020
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~      Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match='/'>
		<HTML>
			<TITLE>Benchmarks</TITLE>
			<BODY>
				<xsl:for-each select="HttpBench/Stats">

				<P>Statistics for <xsl:value-of select="numReqs"/> requests.</P>
				<P>Avg. Throughput (connections/sec): 
						<xsl:value-of select="numReqs div ( (finishedTiming - startedTiming) div 1000 )"/>
				</P>

				<TABLE border="1">

					<TR>
						<TH>Stat</TH>
						<TH>Avg</TH>
						<TH>Min</TH>
						<TH>Max</TH>
					</TR>

					<TR>
						<TD>Connect</TD>
						<TD><xsl:value-of select="connectAvgMS/avg"/></TD>
						<TD><xsl:value-of select="connectMinMS"/></TD>
						<TD><xsl:value-of select="connectMaxMS"/></TD>
					</TR>

					<TR>
						<TD>Send Request</TD>
						<TD><xsl:value-of select="reqSendAvgMS/avg"/></TD>
						<TD><xsl:value-of select="reqSendMinMS"/></TD>
						<TD><xsl:value-of select="reqSendMaxMS"/></TD>
					</TR>

					<TR>
						<TD>Read Response Header</TD>
						<TD><xsl:value-of select="hdrReadAvgMS/avg"/></TD>
						<TD><xsl:value-of select="hdrReadMinMS"/></TD>
						<TD><xsl:value-of select="hdrReadMaxMS"/></TD>
					</TR>

					<TR>
						<TD>Read Response Content</TD>
						<TD><xsl:value-of select="cntReadAvgMS/avg"/></TD>
						<TD><xsl:value-of select="cntReadMinMS"/></TD>
						<TD><xsl:value-of select="cntReadMaxMS"/></TD>
					</TR>

					<TR>
						<TD>Round trip time</TD>
						<TD><xsl:value-of select="rndTripAvgMS/avg"/></TD>
						<TD><xsl:value-of select="rndTripMinMS"/></TD>
						<TD><xsl:value-of select="rndTripMaxMS"/></TD>
					</TR>

					<TR>
						<TD><font color="red">Total Time</font></TD>
						<TD><font color="red"><xsl:value-of select="cntReadAvgMS/avg + hdrReadAvgMS/avg + reqSendAvgMS/avg + connectAvgMS/avg"/></font></TD>
						<TD><font color="red"><xsl:value-of select="cntReadMinMS + hdrReadMinMS + reqSendMinMS + connectMinMS"/></font></TD>
						<TD><font color="red"><xsl:value-of select="cntReadMaxMS + hdrReadMaxMS + reqSendMaxMS + connectMaxMS"/></font></TD>
					</TR>

					<TR>
						<TD>Header Size (bytes)</TD>
						<TD><xsl:value-of select="hdrBytesAvg/avg"/></TD>
						<TD><xsl:value-of select="hdrBytesMin"/></TD>
						<TD><xsl:value-of select="hdrBytesMax"/></TD>
					</TR>

					<TR>
						<TD>Content Size (bytes)</TD>
						<TD><xsl:value-of select="cntBytesAvg/avg"/></TD>
						<TD><xsl:value-of select="cntBytesMin"/></TD>
						<TD><xsl:value-of select="cntBytesMax"/></TD>
					</TR>

					<TR>
						<TD><font color="red">Total Size (bytes)</font></TD>
						<TD><font color="red"><xsl:value-of select="cntBytesAvg/avg + hdrBytesAvg/avg"/></font></TD>
						<TD><font color="red"><xsl:value-of select="cntBytesMin + hdrBytesMin"/></font></TD>
						<TD><font color="red"><xsl:value-of select="cntBytesMax + hdrBytesMax"/></font></TD>
					</TR>

				</TABLE>
				</xsl:for-each>
			</BODY>
		</HTML>
	</xsl:template>
</xsl:stylesheet>
