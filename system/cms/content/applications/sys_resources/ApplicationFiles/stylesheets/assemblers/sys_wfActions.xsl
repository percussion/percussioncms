<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<!-- workflow actions -->
	<xsl:template match="Action[@name='Workflow'] | ActionList[@name='Workflow']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
      <xsl:param name="tiprevision"/>
		<xsl:param name="portal"/>
		<xsl:param name="rhythmyxRoot"/>
      <xsl:variable name="revid">
         <xsl:choose>
            <xsl:when test="@modeid=2">
               <xsl:value-of select="$tiprevision"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$revision"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
		<xsl:if test="@urlint">
			<xsl:variable name="tmp" select="document(concat(@urlint, '&amp;sys_contentid=', $contentid))"/>
			<xsl:variable name="chkoutuser" select="translate($tmp//BasicInfo/@CheckOutUserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
			<xsl:variable name="loginuser" select="translate($tmp//UserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
			<xsl:variable name="isAdmin" select="$tmp//UserName/@assignmentType = '4'"/>
			<xsl:if test="$tmp//UserName/@assignmentType > 1">
			new PSMenu('<xsl:value-of select="@displayname"/>',150,new Array(
			<xsl:if test="$portal = 'yes'">
					<xsl:choose>
						<xsl:when test="$tmp//ActionLink[@name = 'checkin']  and $tmp//UserName/@assignmentType > 2 and ($chkoutuser = $loginuser or $isAdmin)">
					new PSMenuItem('<xsl:value-of select="$tmp//ActionLink[@name = 'checkin']/DisplayLabel"/>',"PSCheckinCheckout(<xsl:apply-templates select="document(concat($tmp//workflowactions/@contenteditorurl, '&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revid"/>','checkin','<xsl:value-of select="$actionsetid"/>','<xsl:value-of select="$chkoutuser"/>')"),
					new PSMenuItem('-', "space"),
				</xsl:when>
						<xsl:when test="$tmp//ActionLink[@name = 'checkout'] and $tmp//UserName/@assignmentType > 2">
					new PSMenuItem('<xsl:value-of select="$tmp//ActionLink[@name = 'checkout']/DisplayLabel"/>',"PSCheckinCheckout(<xsl:apply-templates select="document(concat($tmp//workflowactions/@contenteditorurl, '&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revid"/>','checkout','<xsl:value-of select="$actionsetid"/>','<xsl:value-of select="$chkoutuser"/>')"),
					new PSMenuItem('-', "space"),
				</xsl:when>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="($chkoutuser = '' or $chkoutuser = $loginuser or $isAdmin) and $tmp//UserName/@assignmentType > 2">
					<xsl:apply-templates select="$tmp//ActionLink[@isTransition='yes' and @isDisabled='no']" mode="wfactions">
						<xsl:with-param name="sessionid" select="$sessionid"/>
						<xsl:with-param name="actionsetid" select="$actionsetid"/>
						<xsl:with-param name="contentid" select="$contentid"/>
						<xsl:with-param name="revision" select="$revision"/>
						<xsl:with-param name="rhythmyxRoot" select="$rhythmyxRoot"/>
					</xsl:apply-templates>
					<xsl:if test="(count(ActionList | Action) &gt; 0) and $tmp//ActionLink[@isTransition='yes' and @isDisabled='no']">,
					new PSMenuItem('-', "space"),
					</xsl:if>
				</xsl:if>
				<xsl:apply-templates select="ActionList | Action" mode="popmenu">
					<xsl:with-param name="sessionid" select="$sessionid"/>
					<xsl:with-param name="actionsetid" select="$actionsetid"/>
					<xsl:with-param name="contentid" select="$contentid"/>
					<xsl:with-param name="revision" select="$revision"/>
				</xsl:apply-templates>
				<xsl:if test="$portal = 'yes' and $tmp//ActionLink[@name = 'forcecheckin']">
					,new PSMenuItem('-', "space"),
					new PSMenuItem('<xsl:value-of select="$tmp//ActionLink[@name = 'forcecheckin']/DisplayLabel"/>',"PSCheckinCheckout(<xsl:apply-templates select="document(concat($tmp//workflowactions/@contenteditorurl, '&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revid"/>','forcecheckin','<xsl:value-of select="$actionsetid"/>','<xsl:value-of select="$chkoutuser"/>')")
				</xsl:if>
		))<xsl:if test="position() != last()">,</xsl:if>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ActionLink" mode="wfactions">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="rhythmyxRoot"/>
		<xsl:variable name="wfTransition" select="concat($rhythmyxRoot, '/sys_uiSupport/wfTransition.html')"/>
		<xsl:variable name="checkinTransition" select="concat($rhythmyxRoot, '/sys_action/checkintransition.xml')"/>
      <xsl:variable name="tmp">
         <xsl:call-template name="replace-apos">
            <xsl:with-param name="text" select="DisplayLabel"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="tmp1">
         <xsl:call-template name="replace-apos">
            <xsl:with-param name="text" select="Param[@name=&quot;WFAction&quot;]"/>
         </xsl:call-template>
      </xsl:variable>
      new PSMenuItem("<xsl:value-of select='DisplayLabel'/>", "PSBuildWFAction('<xsl:value-of select="$wfTransition"/>', '<xsl:value-of select="$checkinTransition"/>','<xsl:value-of select="$tmp"/>','<xsl:value-of select="@commentRequired"/>','<xsl:value-of select="AssignedRoles/Role[@adhocType != '0']"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="Param[@name=&quot;sys_transitionid&quot;]"/>','<xsl:value-of select="$tmp1"/>','<xsl:value-of select="Param[@name=&quot;sys_command&quot;]"/>','<xsl:apply-templates select="AssignedRoles/Role" mode="buildlist"/>')")
		<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
   <xsl:template match="*" mode="buildlist">
      <xsl:value-of select="."/><xsl:text>:</xsl:text><xsl:value-of select="@adhocType"/><xsl:text>;</xsl:text>
   </xsl:template>
   <xsl:template name="replace-apos">
      <xsl:param name="text"/>
      <xsl:choose>
         <xsl:when test='contains($text, "&apos;" )'>
            <xsl:value-of select='concat(substring-before($text, "&apos;"), "_psxapos_")'/>
            <xsl:call-template name="replace-apos">
               <xsl:with-param name="text" select='substring-after($text, "&apos;")'/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$text"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
