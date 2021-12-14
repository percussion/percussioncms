<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxlink="urn:www.percussion.com/com.percussion.services.utils.jspel.PSLinkUtilities"
                exclude-result-prefixes="psxi18n,psxlink"
                extension-element-prefixes="psxi18n">
   <xsl:template match="Action[@name='Item_ViewDependents']" mode="table">
      <xsl:param name="contentid"/>
      <xsl:param name="revision"/>
      <xsl:param name="level"/>
      <xsl:call-template name="item">
         <xsl:with-param name="level" select="$level"/>
         <xsl:with-param name="label">
            <xsl:value-of select="@displayname"/>
         </xsl:with-param>
         <xsl:with-param name="action">PSEditSimpleItem('<xsl:value-of select="@url"/>?sys_contentid=<xsl:value-of select="$contentid"/>&amp;sys_revision=<xsl:value-of select="$revision"/>')</xsl:with-param>
      </xsl:call-template>
   </xsl:template>
   <!-- Preview  and 	Active Assembly -->
   <xsl:template match="Action[contains(@name,'_Preview') or contains(@name,'_ActiveAssembly')]" mode="rightmenu">
      <xsl:param name="contentid"/>
      <xsl:param name="sessionid"/>
      <xsl:param name="revision"/>
      <xsl:param name="siteid"/>
      <xsl:param name="folderid" />
      <xsl:param name="assignmenttype"/>
      <xsl:param name="rhythmyxRoot"/>
      <xsl:param name="level"/>
         <xsl:variable name="jsfunction">
            <xsl:choose>
               <xsl:when test="contains(@name,'_Preview')">PSPreviewItem</xsl:when>
               <xsl:otherwise>PSActiveAssemblyItem</xsl:otherwise>
            </xsl:choose>
         </xsl:variable>
         <xsl:call-template name="subtable">
            <xsl:with-param name="label">
               <xsl:value-of select="@displayname"/>
            </xsl:with-param>
            <xsl:with-param name="icon">
               <xsl:choose>
               <xsl:when test="contains(@name,'_ActiveAssembly')">Active_Assembly.gif</xsl:when>
               <xsl:when test="contains(@name,'_Preview')">Prev_Intra_and_Pub.gif</xsl:when>
               </xsl:choose>
            </xsl:with-param>
            <xsl:with-param name="content">
               <xsl:apply-templates select="document(concat(substring-before(//@wfurlint, 'Rhythmyx'), 'Rhythmyx/sys_rcSupport/variantlist.xml?sys_contentid=', $contentid, '&amp;pssessionid=',$sessionid,'&amp;sys_folderid=',$folderid,'&amp;sys_siteid=',$siteid))/*" mode="list">
                  <xsl:with-param name="urlParams" select="document(concat(substring-before(//@wfurlint, 'Rhythmyx'), 'Rhythmyx/sys_cxSupport/Params.xml?sys_actionid=', @actionid, '&amp;pssessionid=',$sessionid))//Params"/>
                  <xsl:with-param name="contentid" select="$contentid"/>
                  <xsl:with-param name="revision" select="$revision"/>
                  <xsl:with-param name="jsfunction" select="$jsfunction"/>
                  <xsl:with-param name="sessionid" select="$sessionid"/>
                   <xsl:with-param name="siteid" select="$siteid"/>
                  <xsl:with-param name="folderid" select="$folderid"/>
                  <xsl:with-param name="level" select="$level+1"/>
               </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="level" select="$level"/>
         </xsl:call-template>
   </xsl:template>
   <xsl:template match="Action" mode="rightmenu">
      <!-- swallow other right menu actions -->
   </xsl:template>
   <!-- Preview  and 	Active Assembly -->
   <xsl:template match="Action[contains(@name,'_Preview') or contains(@name,'_ActiveAssembly')]" mode="table">
      <!-- swallow in table mode -->
   </xsl:template>
   <xsl:template match="*" mode="list">
      <xsl:param name="urlParams"/>
      <xsl:param name="contentid"/>
      <xsl:param name="revision"/>
      <xsl:param name="folderid" />
      <xsl:param name="siteid" />
      <xsl:param name="jsfunction"/>
      <xsl:param name="sessionid"/>
      <xsl:param name="level"/>
      <xsl:if test="Variant/@variantId != ''">
         <xsl:apply-templates select="Variant" mode="variant-copy">
            <xsl:with-param name="contentid" select="$contentid"/>
            <xsl:with-param name="revision" select="$revision"/>
            <xsl:with-param name="jsfunction" select="$jsfunction"/>
            <xsl:with-param name="sessionid" select="$sessionid"/>
             <xsl:with-param name="siteid" select="$siteid"/>
             <xsl:with-param name="folderid" select="$folderid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="urlParams" select="$urlParams"/>
             <xsl:sort select="DisplayName"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>
   <xsl:template match="*" mode="variant-copy">
      <xsl:param name="urlParams"/>
      <xsl:param name="contentid"/>
      <xsl:param name="revision"/>
      <xsl:param name="jsfunction"/>
      <xsl:param name="sessionid"/>
      <xsl:param name="folderid" />
      <xsl:param name="level"/>
      <xsl:param name="siteid" select="$urlParams/Param[@name='sys_siteid']"/> 

      <xsl:variable name="url">
         <xsl:choose>
            <xsl:when test="$jsfunction = 'PSActiveAssemblyItem'">../sys_action/checkoutaapage.xml</xsl:when>
            <xsl:otherwise><xsl:value-of select="AssemblyUrlPlain"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

		<!-- In AA is passed as a parameter to the checkout aa url. -->
		<!-- Otherwise empty. -->
      <xsl:variable name="assemblyUrl">
         <xsl:choose>
            <xsl:when test="$jsfunction = 'PSActiveAssemblyItem'"><xsl:value-of select="AssemblyUrlPlain"/></xsl:when>
            <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="absurl" select="psxlink:get-abs-link($url)"/>

      <xsl:call-template name="item">
         <xsl:with-param name="label">
            <xsl:value-of select="DisplayName"/>
         </xsl:with-param>
         <xsl:with-param name="level" select="$level"/>
         <xsl:with-param name="action">
            <!-- use a different assembly url which is a relative URL of the variant as registered -->
            <xsl:value-of select="$jsfunction"/>('<xsl:value-of select="$absurl"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="@variantId"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$siteid"/>','<xsl:value-of select="$folderid"/>','<xsl:value-of select="$sessionid"/>','<xsl:value-of select="$assemblyUrl"/>')</xsl:with-param>
      </xsl:call-template>
   </xsl:template>
</xsl:stylesheet>
