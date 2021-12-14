<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2021 Percussion Software, Inc.
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

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
