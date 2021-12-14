<?xml version="1.0" encoding="UTF-8"?>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- The purpose of this XSL file is to add NavFolderEffect to Relationship Configuration XML file-->
	<xml:output method="xml" encoding="UTF-8"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Match on PSXRelationshipConfig with category name as rs_folder and add the NavFolderEffect if it does not exist-->
	<xsl:template match="PSXRelationshipConfig[@category='rs_folder']/EffectSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXConditionalEffect/PSXExtensionCall/name='Java/global/percussion/fastforward/managednav/rxs_NavFolderEffect')">
         <PSXConditionalEffect activationEndpoint="either">
            <PSXExtensionCall id="0">
               <name>Java/global/percussion/fastforward/managednav/rxs_NavFolderEffect</name>
            </PSXExtensionCall>
            <Conditions>
               <PSXRule boolean="and">
                  <PSXConditional id="0">
                     <variable>
                        <PSXSingleHtmlParameter id="0">
                           <name>rxs_disableNavFolderEffect</name>
                        </PSXSingleHtmlParameter>
                     </variable>
                     <operator>&lt;&gt;</operator>
                     <value>
                        <PSXTextLiteral id="0">
                           <text>y</text>
                        </PSXTextLiteral>
                     </value>
                     <boolean>AND</boolean>
                  </PSXConditional>
               </PSXRule>
               <PSXRule boolean="and">
                  <PSXConditional id="0">
                     <variable>
                        <PSXOriginatingRelationshipProperty id="0">
                           <name>category</name>
                        </PSXOriginatingRelationshipProperty>
                     </variable>
                     <operator>&lt;&gt;</operator>
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rs_translation</text>
                        </PSXTextLiteral>
                     </value>
                     <boolean>AND</boolean>
                  </PSXConditional>
               </PSXRule>
            </Conditions>
            <ExecutionContextSet>
               <ExecutionContext type="PreConstruction"/>
               <ExecutionContext type="PreDestruction"/>
            </ExecutionContextSet>
         </PSXConditionalEffect>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
