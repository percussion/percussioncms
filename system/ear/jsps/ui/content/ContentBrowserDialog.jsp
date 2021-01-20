<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
		   prefix="rxcomp"%>
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

<%
	String locale = PSI18nUtils.getSystemLanguage();
	String root = request.getContextPath();
%>
<html>
<head>
	<title>Content Browser</title>
	<link rel="stylesheet" type="text/css" href="<%= root %>/sys_resources/css/templates.css"/>
	<link rel="stylesheet" type="text/css" href="<%= root %>/sys_resources/css/aa/styles.css"/>
	<script src="<%= root %>/sys_resources/js/browser.js">;</script>
	<script src="<%= root %>/sys_resources/js/globalErrorMessages.js">;</script>
	<script src="<%= root %>/sys_resources/js/<%= locale %>/globalErrorMessages.js">;</script>
	<script src="<%= root %>/tmx/tmx.jsp?sys_lang=<%= locale %>">;</script>
	<script src="<%= root %>/sys_resources/dojo/dojo.js">;</script>

	<!--
    The following JavaScript files need to be included explicitly (instead of
    include them in the compressed dojo.js); otherwise, the UI may not work.
    Have not found the cause of the problem yet.
    -->
	<script src="<%= root %>/sys_resources/ps/content/History.js">;</script>
	<script src="<%= root %>/sys_resources/ps/content/SelectTemplates.js">;</script>


	<!-- Required only to debug IE/Safari. To be removed for production. Hit F12 to show debug console on Windows (IE)
<script language="javascript" src="<%= root %>/sys_resources/firebug/firebug.js"></script>
 -->

	<script >

        var __rxroot = "<%= root %>";
        //Global flag to indicate that the dojo initialization is required for active assembly
        var __isAa = false;

        // Turn on console for dojo.debug.
        dojo.require("dojo.debug.console");

        dojo.hostenv.writeIncludes();

        var contentBrowser = null;
        var ___selectedContent = null;

        function __initBrowserDialog()
        {
            ___selectedContent = window.opener.___selectedContent;
            contentBrowser = new ps.content.Browse(window.opener.___bwsMode);
            contentBrowser.init(__rxroot);
            slotId = new ps.aa.ObjectId(window.opener.___slotId);
            contentBrowser.open(window.opener.___cBackFunction, slotId);
        }

	</script>

</head>
<BODY onresize="contentBrowser.fillInParentWindow()" onload="__initBrowserDialog()">
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
	<tr class="outerboxcell">
		<td align="center" valign="middle" class="outerboxcellfont">
			${rxcomp:i18ntext('psx.generic@Your request is being processed',locale)}
			${rxcomp:i18ntext('psx.generic@Please wait a moment',locale)}
		</td>
	</tr>
</table>
</BODY>
</HTML>
