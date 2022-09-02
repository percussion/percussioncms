<%@ include file="header.jsp" %>
<body style="margin-top: 0px; margin-right: 0px;  margin-bottom: 0px;  margin-left: 0px">
	<script type="text/javascript">
	$(function() {
		perc_p13n.bindOnProfileEditor(function(pe){
			pe.setMenubarDocument(document);
		}, window);
	});	
	</script>
	<div class="p13nMenuBar">
		<span class="p13nMenuItem" id="p13nMenuNew">New</span>
		<span class="p13nMenuItem" id="p13nMenuSave">Save</span>
		<span class="p13nMenuItem" id="p13nMenuSaveAs">Save As</span>
		<span class="p13nMenuItem" id="p13nMenuDelete">Delete</span>
		<span class="p13nMenuItem" id="p13nMenuRefresh">Refresh</span>
		<span class="p13nMenuItem" id="p13nMenuHideorShow">Hide Outline</span>
		<c:if test="${not empty param.FromRx}">
			<span class="p13nMenuItem" id="p13nMenuAA">Active Assembly</span>
		</c:if>
	</div>
</body>
</html>