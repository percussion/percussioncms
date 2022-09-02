<%@ include file="header.jsp" %>
<body id="SidebarFrame">
	<script type="text/javascript">
		$(function() {
			perc_p13n.bindOnProfileEditor(function(pe){
				pe.profileEditorUrl = '${profileEditorUrl}';
				pe.setSidebarDocument(document);
			}, window);
		});	
	</script>
<div id="ProfileSidebar" style="height:100%; width:100%">
<div id="ProfileEditPane">
<div id="p13nUserData" class="p13nUserData">
	<div class="p13nUserDataLabel">User Id:</div>
	<div><input type="text" id="p13nUserDataInputID" size="5em;" class="p13nUserDataInput" /></div>
	<div class="p13nUserDataLabel">Label:</div>
	<div><input type="text" id="p13nUserDataInputLabel" size="5em;" class="p13nUserDataInput" /></div>
	<div>
		<input type="button" id="p13nUserDataSaveButton" value="Save" class="p13nUserDataButton" />
		<span style="padding-left:1em"> </span>
		<input type="button" id="p13nUserDataCancelButton" value="Cancel" class="p13nUserDataButton" />
	</div>
</div>
<form:form commandName="profile" cssClass="p13nProfileData" action="${profileEditorUrl}">
	<div class="p13nProfileSelector">
		<span class="p13nLabelText">Profile: </span>
		<spring:bind path="profile.id">
			<select id="selectedId" class="p13nProfileSelect" name="selectedId">
				<option value="0">-- Switch Profile -- </option>
				<c:forEach items="${profiles}" var="p">
					<c:choose>
						<c:when test="${p.id == status.value}">
							<option value="${p.id}" selected="selected">${p.label}</option>
						</c:when>
						<c:otherwise>
							<option value="${p.id}">${p.label}</option>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</select>
		</spring:bind>
	</div>
	<div class="p13nProfileMeta">
		<label for="p13nLockProfile" class="p13nLabelText">Disable Tracking:</label>
		<form:checkbox id="p13nLockProfile" path="lockProfile" /> 
		<div class="trackingMessage" style="display:none">
			<div class="message">No tracking in page.</div>
		</div>
	</div>
	
	<div class="segmentWeightCloud">
	${cloud}
	</div>
	<div class="segmentWeightTree">
	${tree}
	</div>
	<div id="hiddenFields" style="display:none"> 
		<form:hidden path="id"/>
		<form:hidden path="userId"/>
		<form:hidden path="label"/>
	</div>
</form:form>
</div>
</div>
</body>
</html>