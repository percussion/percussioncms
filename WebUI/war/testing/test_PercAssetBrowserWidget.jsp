<%--
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
  --%>

<html>
<head>
<script src="/Rhythmyx/tmx/tmx.jsp?mode=js&prefix=perc.ui.&sys_lang="en-us"></script>

<%@include file="../app/includes/common_js.jsp"%>
<!--script src="../jslib/jquery-1.3.2.js"></script-->

<script src="../plugins/perc_path_manager.js"></script>
<script src="../widgets/PercContentBrowserWidget.js"></script>

<script>
	$j(document).ready(function()
	{
		$j.PercAssetBrowserWidget
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