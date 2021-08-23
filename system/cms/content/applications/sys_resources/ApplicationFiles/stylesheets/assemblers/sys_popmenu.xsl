<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<!--Main Template to initiate the Javascript based action menus -->
	<xsl:template match="ActionList" mode="mainmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="siteid"/>
		<xsl:param name="folderid"/>
		<xsl:param name="tiprevision"/>
		<xsl:param name="variantid"/>
		<xsl:param name="showpromote"/>
		<xsl:param name="editauthorized"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="portal"/>
		<xsl:variable name="wfurlintdoc" select="document(concat(//@wfurlint, '&amp;sys_contentid=', $contentid))"/>
		<xsl:variable name="assignmenttype" select="$wfurlintdoc//UserName/@assignmentType"/>
		<xsl:variable name="chkoutuser" select="translate($wfurlintdoc//BasicInfo/@CheckOutUserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
		<xsl:variable name="loginuser" select="translate($wfurlintdoc//UserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
		<xsl:variable name="isLoginUserCheckoutUser">
			<xsl:if test="$chkoutuser=$loginuser">yes</xsl:if>
		</xsl:variable>
		<script>
			PSCreateMenu('<xsl:value-of select="$actionsetid"/>',<xsl:apply-templates select="." mode="popmenu">
				<xsl:with-param name="sessionid" select="$sessionid"/>
				<xsl:with-param name="actionsetid" select="$actionsetid"/>
				<xsl:with-param name="contentid" select="$contentid"/>
				<xsl:with-param name="revision" select="$revision"/>
				<xsl:with-param name="folderid" select="$folderid"/>
				<xsl:with-param name="siteid" select="$siteid"/>
				<xsl:with-param name="tiprevision" select="$tiprevision"/>
				<xsl:with-param name="variantid" select="$variantid"/>
				<xsl:with-param name="showpromote" select="$showpromote"/>
				<xsl:with-param name="assignmenttype" select="$assignmenttype"/>
				<xsl:with-param name="editauthorized" select="$editauthorized"/>
				<xsl:with-param name="contentvalid" select="$contentvalid"/>
				<xsl:with-param name="usercommunity" select="$usercommunity"/>
				<xsl:with-param name="itemcommunity" select="$itemcommunity"/>
				<xsl:with-param name="portal" select="$portal"/>
				<xsl:with-param name="rhythmyxRoot" select="@rxrooturl"/>
				<xsl:with-param name="isLoginUserCheckoutUser" select="$isLoginUserCheckoutUser"/>
			</xsl:apply-templates>,null,null);
		</script>
	</xsl:template>
	<!--Matches on root element and passes on the parameters down to the Action are ActionList elements-->
	<xsl:template match="ActionList" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="siteid"/>
		<xsl:param name="folderid"/>
		<xsl:param name="tiprevision"/>
		<xsl:param name="variantid"/>
		<xsl:param name="showpromote"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="editauthorized"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="portal"/>
		<xsl:param name="rhythmyxRoot"/>
		<xsl:param name="isLoginUserCheckoutUser"/>
		<xsl:choose>
			<xsl:when test="not(@name = 'Arrange' and $editauthorized = 'no')">
		new PSMenu('<xsl:value-of select="@displayname"/>',150,new Array(
		<xsl:if test="$assignmenttype &gt; 1">
					<xsl:apply-templates select="ActionList | Action" mode="popmenu">
						<xsl:with-param name="sessionid" select="$sessionid"/>
						<xsl:with-param name="actionsetid" select="$actionsetid"/>
						<xsl:with-param name="contentid" select="$contentid"/>
						<xsl:with-param name="revision" select="$revision"/>
						<xsl:with-param name="folderid" select="$folderid"/>
						<xsl:with-param name="siteid" select="$siteid"/>
						<xsl:with-param name="variantid" select="$variantid"/>
						<xsl:with-param name="tiprevision" select="$tiprevision"/>
						<xsl:with-param name="showpromote" select="$showpromote"/>
						<xsl:with-param name="assignmenttype" select="$assignmenttype"/>
						<xsl:with-param name="editauthorized" select="$editauthorized"/>
						<xsl:with-param name="contentvalid" select="$contentvalid"/>
						<xsl:with-param name="usercommunity" select="$usercommunity"/>
						<xsl:with-param name="itemcommunity" select="$itemcommunity"/>
						<xsl:with-param name="portal" select="$portal"/>
						<xsl:with-param name="rhythmyxRoot" select="$rhythmyxRoot"/>
						<xsl:with-param name="isLoginUserCheckoutUser" select="$isLoginUserCheckoutUser"/>
					</xsl:apply-templates>
				</xsl:if>))
		</xsl:when>
			<xsl:otherwise>
		new PSMenuItem('~','blank')
		</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!--Dummy Action calls the will be applied to the actions without a specific template match -->
	<xsl:template match="Action" mode="popmenu">
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSBuildDummy('<xsl:value-of select="@url"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!--Template matches on View(View) ActionList -->
	<!--Visibility rule: None-->
	<xsl:template match="ActionList[@name = 'View']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="variantid"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="portal"/>
		new PSMenu('<xsl:value-of select="@displayname"/>',150,new Array(<xsl:apply-templates select="ActionList | Action" mode="popmenu">
			<xsl:with-param name="sessionid" select="$sessionid"/>
			<xsl:with-param name="actionsetid" select="$actionsetid"/>
			<xsl:with-param name="contentid" select="$contentid"/>
			<xsl:with-param name="revision" select="$revision"/>
			<xsl:with-param name="variantid" select="$variantid"/>
			<xsl:with-param name="assignmenttype" select="$assignmenttype"/>
			<xsl:with-param name="portal" select="$portal"/>
		</xsl:apply-templates>))<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Workflow_AuditTrail(Audit Trail) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_AuditTrail']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSAuditTrail(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Workflow_Revisions(Revisions) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_Revisions']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSShowRevisions(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Edit(Edit) Action-->
	<!--Visibility rule: 1) Content should not be in Public state and 2) Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Edit']" mode="popmenu">
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="isLoginUserCheckoutUser"/>
		<xsl:if test="not($contentvalid='y') and $assignmenttype &gt; 2 and $isLoginUserCheckoutUser='yes'">
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSEditContent('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- Template matches on Quick_Edit(Quick Edit) Action-->
	<!--Visibility rule: 1) Content should be in Public state and 2) Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Quick_Edit']" mode="popmenu">
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="assignmenttype"/>
		<xsl:if test="$contentvalid='y'  and $assignmenttype &gt; 2">
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSQuickEditContent('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- Template matches on View_Content(Content) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Content']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSViewContent(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on View_Properties(Properties) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Properties']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSViewMeta(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Workflow_NewVersion(New Copy) Action. It is a subaction under Create action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_NewVersion']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSNewVersion(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Edit_PromotableVersion(Promotable Version) Action. It is a subaction under Create action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Edit_PromotableVersion']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSPromotableVersion(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!--Support template-->
	<xsl:template match="*" mode="edititem">'<xsl:value-of select="@editurl"/>'</xsl:template>
	<!-- Template matches on Slot_Add(Search) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Slot_Add']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSAddItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Arrange_MoveUpLeft(Move UP/Left) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_MoveUpLeft']" mode="popmenu">
		<xsl:param name="actionsetid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSMoveItem('moveup','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Arrange_MoveDownRight(Move Down/Right) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_MoveDownRight']" mode="popmenu">
		<xsl:param name="actionsetid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSMoveItem('movedown','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Arrange_ChangeTemplateSlot'(Change Variant Slot) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_ChangeTemplateSlot']" mode="popmenu">
		<xsl:param name="actionsetid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSModifyItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Slot_Create(Create) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Slot_Create']" mode="popmenu">
		<xsl:param name="actionsetid"/>
		<xsl:param name="folderid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSCreateItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>','<xsl:value-of select="$folderid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Arrange_Remove(Remove) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_Remove']" mode="popmenu">
		<xsl:param name="actionsetid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSMoveItem('delete','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Revision_ViewContent(Content) Action-->
	<!--Visibility rule:None-->
	<xsl:template match="Action[@name='Revision_ViewContent']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSRevisionViewContent('','<xsl:value-of select="$contentid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on Revision_ViewProperties(Properties) Action-->
	<!--Visibility rule:None-->
	<xsl:template match="Action[@name='Revision_ViewProperties']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSRevisionViewProperties('','<xsl:value-of select="$contentid"/>')")
	</xsl:template>
	<!-- Template matches on Revision_Promote(Promote) Action-->
	<!--Visibility rule:Caller takes care of it by passign showpromote  param.-->
	<xsl:template match="Action[@name='Revision_Promote']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="showpromote"/>
		<xsl:if test="$showpromote = 'yes' ">
			,new PSMenuItem('-', "space"),
			new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSRevisionPromote('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>')")<xsl:if test="position() != last()">,</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- Template matches on Translate(Translate) Action-->
	<!--Visibility rule:Appears only when there are more than one language, Action List generator takes care of it. Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Translate']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="assignmenttype"/>
		<xsl:if test="$assignmenttype &gt; 2">
			new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSTranslateItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- Template matches on Translate(Translate) Action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Flush_Cache']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="variantid"/>
			new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSFlushCache('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$variantid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Template matches on View_Compare(Compare) Action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Compare']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="variantid"/>
		<xsl:param name="revision"/>
			new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSCompare('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$variantid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Templete that creates the menu item for the 'Purge' action. -->
	<xsl:template match="Action[@name='Purge']" mode="popmenu">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
  	   new PSMenuItem('<xsl:value-of select="@displayname"/>', "PSPurgeItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$sessionid"/>','<xsl:value-of select="$contentid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
</xsl:stylesheet>
