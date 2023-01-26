<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
		   prefix="rxcomp"%>


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
	<script src="/JavaScriptServlet"></script>
	<script src="<%= root %>/sys_resources/dojo/dojo.js">;</script>

	<!--
    The following JavaScript files need to be included explicitly (instead of
    include them in the compressed dojo.js); otherwise, the UI may not work.
    Have not found the cause of the problem yet.
    -->
	<script src="<%= root %>/sys_resources/ps/content/History.js">;</script>
	<script src="<%= root %>/sys_resources/ps/content/SelectTemplates.js">;</script>


	<script >
        var __rxroot = "<%= root %>";
        //Global flag to indicate that the dojo initialization is required for active assembly
        var __isAa = false;

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
