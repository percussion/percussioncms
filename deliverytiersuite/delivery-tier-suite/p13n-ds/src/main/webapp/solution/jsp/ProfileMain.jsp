<%@ include file="variables.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
	<title>Personalized view of: ${previewPageUrl}</title>
	<link rel="stylesheet" href="${resources}/css/p13nMain.css" type="text/css" media="screen" >
	<link rel="stylesheet" href="${scripts}/jquery-treeview/treeview.css" type="text/css"
		media="screen" >
	<%@ include file="scripts.jsp" %>
	<script type="text/javascript">
		$(function() {
			perc_p13n.bindOnProfileEditor(function(pe){
				pe.insideRhythmyx = ${insideRx};
				pe.profileEditorUrl = '${profileEditorUrl}';
			}, window);
		});	
	</script>
	<style type="text/css">
		div.segmentWeightCloud { padding: 1em }
		.segmentWeightCloud a { text-decoration: none; }
		.segmentWeightCloud li { display: inline; padding: 0.125em}
		.segmentWeightCloud ul { display: inline; padding: 0.125em}
	</style>
</head>
<frameset rows="22,*" framespacing="1" border="1">
	<frame name="frameMenu" id="frameMenu"
		frameborder="0"
		bordercolor="#85aeec"
		src="${profileMenuUrl}${not empty param.FromRx ? '?FromRx=true' : ''}" 
		marginheight="0" marginwidth="0" scrolling="no" 
		noresize="noresize">
	<frameset id="framesetMain" cols="250,*" framespacing="2" border="3" bordercolor="#85aeec">
		<frame
			marginheight="0" marginwidth="0" 
			src="${profileEditorUrl}" 
			name="frameSidebar" id="frameSidebar">
		<frame
			marginheight="0" marginwidth="0" src="${previewPageUrl}" name="frameMain" id="frameMain">
	</frameset>
</frameset>
</html>