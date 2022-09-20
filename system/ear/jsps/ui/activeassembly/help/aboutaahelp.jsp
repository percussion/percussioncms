<jsp:directive.page
import="com.percussion.i18n.ui.PSI18NTranslationKeyValues" />
<jsp:directive.page import="com.percussion.util.PSFormatVersion" />
<jsp:directive.page import="com.percussion.util.PSStopwatch" />
<jsp:directive.page import="com.percussion.guitools.ResourceHelper"/>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
prefix="rxcomp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%
//Assume the Version.properties file always resides with the same
//package as PSStop class
String appPackage = PSStopwatch.class.getPackage().getName();
PSFormatVersion version = new PSFormatVersion(appPackage);
String versionString = version.getVersionString();
String copyRightString = "Copyright \u00a9 1999-"
+ ResourceHelper.getResources().getString("copyrightyear")
+ " by Percussion Software, Inc.";
%>
<div class="PsAaAboutDlg">
	<table align="center" width="480" height="332">
		<tr>
			<td height="50%">&nbsp;</td>
		</tr>
		<tr>
			<td valign="top" height="50%">
				<a href="https://www.percussion.com">https://www.percussion.com</a><br /><br />
				<p><%=versionString%></p>
				<p><%=copyRightString%></p>
			</td>
		</tr>
	</table>
</div>
