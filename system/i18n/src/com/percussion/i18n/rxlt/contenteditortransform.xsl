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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!-- This stylesheet extracts all translation keys from the Content Editor System, Shared or Local definitions. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- Main template -->
   <xsl:template match="/">
      <!-- If it is a SystemDef -->
      <xsl:apply-templates select="//ContentEditorSystemDef" mode="root"/>
      <!-- If it is a SharedDef -->
      <xsl:apply-templates select="//PSXContentEditorSharedDef" mode="root"/>
      <!-- If it is a LocalDef -->
      <xsl:apply-templates select="//PSXContentEditor" mode="root"/>
   </xsl:template>
   <!-- ignore any other element in the document -->
   <xsl:template match="*" mode="root"/>
   <!-- ContentEditorSystemDef -->
   <xsl:template match="ContentEditorSystemDef" mode="root" priority="10">
      <keys hint="systemdef">
         <xsl:apply-templates select=".//PSXField" mode="ContentEditorSystemDef"/>
         <xsl:apply-templates select=".//PSXDisplayMapping" mode="ContentEditorSystemDef"/>
         <xsl:apply-templates select=".//PSXField"/>
      </keys>
   </xsl:template>
   <!-- ContentEditorSharedDef -->
   <xsl:template match="PSXContentEditorSharedDef" mode="root" priority="10">
      <keys hint="shareddef">
         <xsl:apply-templates select=".//PSXField" mode="ContentEditorSharedDef"/>
         <xsl:apply-templates select=".//PSXDisplayMapping" mode="ContentEditorSharedDef"/>
         <xsl:apply-templates select=".//PSXField"/>
      </keys>
   </xsl:template>
   <!-- ContentEditorLocalDef -->
   <xsl:template match="PSXContentEditor" mode="root" priority="10">
      <keys hint="localdef">
         <xsl:apply-templates select=".//PSXField" mode="ContentEditorLocalDef">
            <xsl:with-param name="contenttype" select="@contentType"/>
         </xsl:apply-templates>
         <xsl:apply-templates select=".//PSXDisplayMapping" mode="ContentEditorLocalDef">
            <xsl:with-param name="contenttype" select="@contentType"/>
         </xsl:apply-templates>
         <xsl:apply-templates select=".//PSXField"/>
      </keys>
   </xsl:template>
   <!-- Handle systemdef fields -->
   <xsl:template match="PSXField" mode="ContentEditorSystemDef">
      <xsl:if test="@name!='' and @defaultSearchLabel!=''">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.system.', @name, '@',@defaultSearchLabel)"/></xsl:attribute>
            <xsl:text>Content editor system field default label</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <xsl:template match="PSXDisplayMapping" mode="ContentEditorSystemDef">
      <xsl:variable name="fieldref" select="FieldRef"/>
      <xsl:for-each select=".//PSXDisplayText[.!= '']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.system.', $fieldref, '@', .)"/></xsl:attribute>
            <xsl:text>Content editor system field label</xsl:text>
         </key>
         <xsl:variable name="mnemonicKey" select="ancestor::PSXUISet/@accessKey"/>
         <xsl:if test="$mnemonicKey and $mnemonicKey!=''">
	         <key>
	            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.system.', $fieldref, '.mnemonic.', ., '@', $mnemonicKey)"/></xsl:attribute>
	            <xsl:text>Mnemonic key for content editor system field label</xsl:text>
	         </key>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test=".//PSXParam[@name='alt']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.system.', $fieldref, '.alt@', .//PSXParam[@name='alt']//text)"/></xsl:attribute>
            <xsl:text>Content editor system field alt text</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <!-- Handle shareddef fields -->
   <xsl:template match="PSXField" mode="ContentEditorSharedDef">
      <xsl:if test="@name!='' and @defaultSearchLabel!=''">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.shared.', @name, '@',@defaultSearchLabel)"/></xsl:attribute>
            <xsl:text>Content editor shared field default label</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <xsl:template match="PSXDisplayMapping" mode="ContentEditorSharedDef">
      <xsl:variable name="fieldref" select="FieldRef"/>
      <xsl:for-each select=".//PSXDisplayText[.!= '']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.shared.', $fieldref, '@', .)"/></xsl:attribute>
            <xsl:text>Content editor shared field label</xsl:text>
         </key>
         <xsl:variable name="mnemonicKey" select="ancestor::PSXUISet/@accessKey"/>
         <xsl:if test="$mnemonicKey and $mnemonicKey!=''">
	         <key>
	            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.shared.', $fieldref, '.mnemonic.', ., '@', $mnemonicKey)"/></xsl:attribute>
	            <xsl:text>Mnemonic key for content editor system field label</xsl:text>
	         </key>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test=".//PSXParam[@name='alt']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.shared.', $fieldref, '.alt@', .//PSXParam[@name='alt']//text)"/></xsl:attribute>
            <xsl:text>Content editor shared field alt text</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <!-- Handle localdef fields -->
   <xsl:template match="PSXField" mode="ContentEditorLocalDef">
      <xsl:param name="contenttype" select="''"/>
      <xsl:if test="@name!='' and @defaultSearchLabel!=''">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.local.', $contenttype, '.', @name, '@',@defaultSearchLabel)"/></xsl:attribute>
            <xsl:text>Content editor local field default label</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <xsl:template match="PSXDisplayMapping" mode="ContentEditorLocalDef">
      <xsl:param name="contenttype" select="''"/>
      <xsl:variable name="fieldref" select="FieldRef"/>
      <xsl:for-each select=".//PSXDisplayText[.!= '']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.local.', $contenttype, '.', $fieldref, '@', .)"/></xsl:attribute>
            <xsl:text>Content editor local field label</xsl:text>
         </key>
         <xsl:variable name="mnemonicKey" select="ancestor::PSXUISet/@accessKey"/>
         <xsl:if test="$mnemonicKey and $mnemonicKey!=''">
	         <key>
	            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.local.', $contenttype, '.', $fieldref, '.mnemonic.', ., '@', $mnemonicKey)"/></xsl:attribute>
	            <xsl:text>Mnemonic key for content editor system field label</xsl:text>
	         </key>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test=".//PSXParam[@name='alt']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.local.', $contenttype, '.', $fieldref, '.alt@', .//PSXParam[@name='alt']//text)"/></xsl:attribute>
            <xsl:text>Content editor local field alt text</xsl:text>
         </key>
      </xsl:if>
   </xsl:template>
   <!-- Handle error messages-->
   <xsl:template match="PSXField">
      <xsl:for-each select=".//ErrorMessage[PSXDisplayText != '']">
         <key>
            <xsl:attribute name="name"><xsl:value-of select="concat('psx.ce.error@', PSXDisplayText)"/></xsl:attribute>
            <xsl:text>Content editor field error message</xsl:text>
         </key>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>
