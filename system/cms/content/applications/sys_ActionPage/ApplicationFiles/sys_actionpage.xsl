<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
				xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils"
				extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<!--Main Template to initiate the Javascript based action tables -->
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
		<xsl:param name="assignmenttype"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="portal"/>
		<xsl:param name="rhythmyxRoot"/>
		<xsl:param name="level"/>
		<xsl:apply-templates select="." mode="table">
			<xsl:with-param name="sessionid" select="$sessionid"/>
			<xsl:with-param name="actionsetid" select="$actionsetid"/>
			<xsl:with-param name="contentid" select="$contentid"/>
			<xsl:with-param name="revision" select="$revision"/>
         <xsl:with-param name="siteid" select="$siteid"/>
         <xsl:with-param name="folderid" select="$folderid"/>
			<xsl:with-param name="tiprevision" select="$tiprevision"/>
			<xsl:with-param name="variantid" select="$variantid"/>
			<xsl:with-param name="showpromote" select="$showpromote"/>
			<xsl:with-param name="editauthorized" select="$editauthorized"/>
			<xsl:with-param name="contentvalid" select="$contentvalid"/>
			<xsl:with-param name="assignmenttype" select="$assignmenttype"/>
			<xsl:with-param name="usercommunity" select="$usercommunity"/>
			<xsl:with-param name="itemcommunity" select="$itemcommunity"/>
			<xsl:with-param name="portal" select="$portal"/>
			<xsl:with-param name="rhythmyxRoot" select="$rhythmyxRoot"/>
			<xsl:with-param name="level" select="$level"/>
		</xsl:apply-templates>
	</xsl:template>
	<!--Right Template to initiate the Javascript based action tables -->
	<xsl:template match="ActionList" mode="rightmenu">
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
		<xsl:param name="assignmenttype"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="portal"/>
		<xsl:param name="rhythmyxRoot"/>
		<xsl:param name="level"/>
		<xsl:apply-templates select="ActionList | Action" mode="rightmenu">
			<xsl:with-param name="sessionid" select="$sessionid"/>
			<xsl:with-param name="actionsetid" select="$actionsetid"/>
			<xsl:with-param name="contentid" select="$contentid"/>
			<xsl:with-param name="revision" select="$revision"/>
         <xsl:with-param name="siteid" select="$siteid"/>
         <xsl:with-param name="folderid" select="$folderid"/>
			<xsl:with-param name="tiprevision" select="$tiprevision"/>
			<xsl:with-param name="variantid" select="$variantid"/>
			<xsl:with-param name="showpromote" select="$showpromote"/>
			<xsl:with-param name="editauthorized" select="$editauthorized"/>
			<xsl:with-param name="contentvalid" select="$contentvalid"/>
			<xsl:with-param name="assignmenttype" select="$assignmenttype"/>
			<xsl:with-param name="usercommunity" select="$usercommunity"/>
			<xsl:with-param name="itemcommunity" select="$itemcommunity"/>
			<xsl:with-param name="portal" select="$portal"/>
			<xsl:with-param name="rhythmyxRoot" select="$rhythmyxRoot"/>
			<xsl:with-param name="level" select="$level"/>
		</xsl:apply-templates>
	</xsl:template>
	<!--Matches on root element and passes on the parameters down to the Action are ActionList elements-->
	<xsl:template match="ActionList" mode="table">
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
		<xsl:param name="level"/>
		<xsl:choose>
			<xsl:when test="string-length(@name) = 0">
				<!-- toplevel ActionList has no name -->
				<xsl:apply-templates select="ActionList | Action" mode="table">
					<xsl:with-param name="sessionid" select="$sessionid"/>
					<xsl:with-param name="actionsetid" select="$actionsetid"/>
					<xsl:with-param name="contentid" select="$contentid"/>
					<xsl:with-param name="revision" select="$revision"/>
               <xsl:with-param name="siteid" select="$siteid"/>
               <xsl:with-param name="folderid" select="$folderid"/>
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
					<xsl:with-param name="level" select="$level"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="not(@name = 'Arrange' and $editauthorized = 'no')">
				<xsl:call-template name="subtable">
					<xsl:with-param name="label">
						<xsl:value-of select="@displayname"/>
					</xsl:with-param>
					<xsl:with-param name="icon">
						<xsl:choose>
							<xsl:when test="@name = 'Create'">Create.gif</xsl:when>
							<xsl:when test="@name = 'View'">View.gif</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@name"/>.gif</xsl:otherwise>
						</xsl:choose>
					</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:apply-templates select="ActionList | Action" mode="table">
							<xsl:with-param name="sessionid" select="$sessionid"/>
							<xsl:with-param name="actionsetid" select="$actionsetid"/>
							<xsl:with-param name="contentid" select="$contentid"/>
							<xsl:with-param name="revision" select="$revision"/>
                     <xsl:with-param name="siteid" select="$siteid"/>
                     <xsl:with-param name="folderid" select="$folderid"/>
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
							<xsl:with-param name="level" select="$level+1"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
         </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Build Action calls the will be applied to the actions without a specific template match, this template also handles
   	View_*Compare, which can't be caught by a simple XPath expression -->
	<xsl:template match="Action" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
      <xsl:param name="siteid"/>      
      <xsl:param name="folderid"/>
		<xsl:param name="variantid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:choose>
			<!-- Note that substring is 1 based -->
			<xsl:when test="starts-with(@name,'View_') and substring(@name,string-length(@name) - 6) = 'Compare'">
				<xsl:call-template name="item">
					<xsl:with-param name="label">
						<xsl:value-of select="@displayname"/>
					</xsl:with-param>
					<xsl:with-param name="level" select="$level"/>
					<xsl:with-param name="action">PSCompare('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$variantid"/>','<xsl:value-of select="$folderid"/>','<xsl:value-of select="$siteid"/>')</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="item">
					<xsl:with-param name="label">
						<xsl:value-of select="@displayname"/>
					</xsl:with-param>
					<xsl:with-param name="action">PSBuildAction('<xsl:value-of select="@name"/>','<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$currentrevision"/>')</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Template matches on Workflow_AuditTrail(Audit Trail) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_AuditTrail']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSAuditTrail(<xsl:apply-templates select="document(concat(@urlint,'&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Workflow_Revisions(Revisions) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_Revisions']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSShowRevisions(<xsl:apply-templates select="document(concat(@urlint,'&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Edit(Edit) Action-->
	<!--Visibility rule: 1) Content should not be in Public state and 2) Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Edit']" mode="table">
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="icon">Edit.gif</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSEditContent('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Quick_Edit(Quick Edit) Action-->
	<!--Visibility rule: 1) Content should be in Public state and 2) Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Quick_Edit']" mode="table">
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="contentvalid"/>
		<xsl:param name="usercommunity"/>
		<xsl:param name="itemcommunity"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="icon">Quick_Edit.gif</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSQuickEditContent('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on View_Content(Content) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Content']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSViewContent(<xsl:apply-templates select="document(concat(@urlint,'&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on View_Properties(Properties) Action. It is a subaction under View action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Properties']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSViewMeta(<xsl:apply-templates select="document(concat(@urlint,'&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Workflow_NewVersion(New Copy) Action. It is a subaction under Create action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Workflow_NewVersion']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSNewVersion(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Edit_PromotableVersion(Promotable Version) Action. It is a subaction under Create action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Edit_PromotableVersion']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="action">PSPromotableVersion(<xsl:apply-templates select="document(concat(@urlint, '&amp;fromActiveAssembly=yes&amp;sys_contentid=', $contentid))/*" mode="edititem"/>,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!--Support template-->
	<xsl:template match="*" mode="edititem">'<xsl:value-of select="@editurl"/>'</xsl:template>
	<!-- Template matches on Slot_Add(Search) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Slot_Add']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSAddItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Arrange_MoveUpLeft(Move UP/Left) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_MoveUpLeft']" mode="table">
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="action">PSMoveItem('moveup','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Arrange_MoveDownRight(Move Down/Right) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_MoveDownRight']" mode="table">
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSMoveItem('movedown','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Arrange_ChangeTemplateSlot(Change Variant Slot) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_ChangeTemplateSlot']" mode="table">
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSModifyItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Slot_Create(Create) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Slot_Create']" mode="table">
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
      <xsl:param name="folderid"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSCreateItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>','<xsl:value-of select="$folderid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Arrange_Remove(Remove) Action-->
	<!--Visibility rule:Parent takes care-->
	<xsl:template match="Action[@name='Arrange_Remove']" mode="table">
		<xsl:param name="actionsetid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSMoveItem('delete','<xsl:value-of select="@url"/>','<xsl:value-of select="$actionsetid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Revision_ViewContent(Content) Action-->
	<!--Visibility rule:None-->
	<xsl:template match="Action[@name='Revision_ViewContent']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSRevisionViewContent('','<xsl:value-of select="$contentid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Revision_ViewProperties(Properties) Action-->
	<!--Visibility rule:None-->
	<xsl:template match="Action[@name='Revision_ViewProperties']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSRevisionViewProperties('','<xsl:value-of select="$contentid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Revision_Promote(Promote) Action-->
	<!--Visibility rule:Caller takes care of it by passign showpromote  param.-->
	<xsl:template match="Action[@name='Revision_Promote']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="showpromote"/>
		<xsl:param name="level"/>
		<xsl:if test="$showpromote = 'yes' ">
			<xsl:call-template name="item">
				<xsl:with-param name="label">
					<xsl:value-of select="@displayname"/>
				</xsl:with-param>
				<xsl:with-param name="level" select="$level"/>
				<xsl:with-param name="action">PSRevisionPromote('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>')</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- Template matches on Translate(Translate) Action-->
	<!--Visibility rule:Appears only when there are more than one language, Action List generator takes care of it. Assignment type should be greater than 2(Assignee and Admin only should see this action-->
	<xsl:template match="Action[@name='Translate']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="assignmenttype"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSTranslateItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on Translate(Translate) Action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='Flush_Cache']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="variantid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSFlushCache('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$variantid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Template matches on View_Compare(Compare) Action-->
	<!--Visibility rule: None-->
	<xsl:template match="Action[@name='View_Compare']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="variantid"/>
		<xsl:param name="revision"/>
      <xsl:param name="siteid"/>
      <xsl:param name="folderid"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSCompare('<xsl:value-of select="@url"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$variantid"/>','<xsl:value-of select="$folderid"/>','<xsl:value-of select="$siteid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Templete that creates the menu item for the 'Purge' action. -->
	<xsl:template match="Action[@name='Purge']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="icon">Purge.gif</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSPurgeItem('<xsl:value-of select="@url"/>','<xsl:value-of select="$sessionid"/>','<xsl:value-of select="$contentid"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Templete that creates the menu item for the 'Copy URL' action. -->
	<xsl:template match="Action[@name='Copy_URL_to_Clipboard']" mode="table">
		<xsl:param name="sessionid"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="level"/>
		<xsl:call-template name="item">
			<xsl:with-param name="label">
				<xsl:value-of select="@displayname"/>
			</xsl:with-param>
			<xsl:with-param name="icon">Copy_URL.gif</xsl:with-param>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="action">PSClipboardCopy('<xsl:value-of select="$link"/>')</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
