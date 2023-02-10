<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>



<html>
<head>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&prefix=perc.ui.&sys_lang="en-us"></script>
    <script src="/JavaScriptServlet"></script>

<%@include file="../app/includes/common_js.jsp"%>

<script src="../plugins/perc_path_manager.js"></script>
<script src="../widgets/PercContentBrowserWidget.js"></script>

<script>
	$(function()
	{
		$.PercAssetBrowserWidget
		({
		    // callback when user selects a leaf
			on_click : function(spec)
			{
			    console.log(spec.id + ", " + spec.path);
			},
			
			// the type of selectable object
			// valid options: "leaf", "folder", "all"
			selectable_object: "folder",
			 
            // whether we want a new folder button next to the dropdown
			new_folder_opt: false,
			 
  //          displayed_containers: "Sites",
            // what type of folders are displayed
            // valid options: "Sites", "Assets", "All"
            // these are based on the content type

            displayed_containers: "Sites",

            // which types of items show up and can be selected in the directory listing
            // these are based on content type
            
			selection_types: ['Folder','site'],
			
			// Div where you want the browser to appear
			placeHolder : 'perc-asset-browser-placeholder'
		});
	});
</script>

<%@include file="../app/includes/common_css.jsp"%>
<!--link rel="stylesheet" type="text/css" href="../css/perc_mcol.css"-->
<link rel="stylesheet" type="text/css" href="../css/PercContentBrowserWidget.css">
<link rel="stylesheet" type="text/css" href="../css/jquery.jmodal.css">
<link rel="stylesheet" type="text/css" href="../css/layout.css">
<link rel="stylesheet" type="text/css" href="../css/styles.css">

</head>
<body>

<div id="perc-asset-browser-placeholder">
</div>

</body>
</html>
