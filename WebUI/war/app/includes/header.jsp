<%@page import="com.percussion.server.PSServer" %>
<%@page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@page import="com.percussion.utils.PSSpringBeanProvider" %>
<%@page import="com.percussion.utils.service.impl.PSUtilityService" %>
<%@page import="java.text.MessageFormat" %>
<%@page import="java.util.Calendar" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
String locale= PSRoleUtilities.getUserCurrentLocale();
String lang="en";
if(locale==null){
	locale="en-us";
}else{
	if(locale.contains("-"))
		lang=locale.split("-")[0];
	else
		lang=locale;
}
    PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");
    boolean isSaaS = utilityService.isSaaSEnvironment();

    String mainNavTab = request.getParameter("mainNavTab");
    if (request.getAttribute("dispatched") == null) {
        response.sendRedirect("/cm/");
    }

    String debug = request.getParameter("debug");
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    if (debug == null)
        debug = "false";

    String view = request.getParameter("view");
    String className = "perc-ui-view-ready";
    if (view.equals("editor")) {
        className = "perc-ui-view-processing";
    }

    final String versionAttrName = "CMS_ABOUT_MESSAGE";
	Calendar cal = Calendar.getInstance();
	int year = cal.get(Calendar.YEAR);
    String aboutMsg = "<div class=\"perc-about\">"
            + "<div class=\"perc-logo\"></div>"
            + "<p><a class=\"perc-company-link\" target=\"_blank\" href=\"https://www.percussion.com\">https://www.percussion.com</a></p>"
            + "<p>{0}</p>"
            + "<p>Copyright &copy; " + year + " by Percussion Software Inc.</p>"
            + "</div>";
    Object o = request.getSession().getAttribute(versionAttrName);
    if (o == null || o.toString().trim().length() == 0) {

        String ver = PSServer.getVersionString();

        aboutMsg = MessageFormat.format(aboutMsg, ver);
        request.getSession().setAttribute(versionAttrName, aboutMsg);
    } else {
        aboutMsg = o.toString();
    }
%>
<script  >
    var gIsSaaSEnvironment = <%=isSaaS%>;
    $j(document).ready(function () {
        $j('.perc-header #perc-help-about').click(function (e) {
            $j.perc_utils.confirm_dialog({
                title: "About Percussion CMS",
                type: "OK",
                width: 400,
                question: '<%= aboutMsg %>'
            });
        });
        $j('.perc-header #perc-changepw').click(function (e) {
            $j.perc_ChangePwDialog.open();
        });
    });
</script>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div class="perc-header">
    <div class="perc-logo" role="img" aria-label="<i18n:message key="perc.ui.header@Percussion Logo"/>"></div>
    <%-- logout must be after help because they both float and we want logout to left of help --%>
    <div class="perc-logout"><a href="javascript:void(0)" id="perc-changepw" title="<i18n:message key="perc.ui.change.pw@Change Password"/>"><i18n:message key="perc.ui.common.label@Welcome"/>
        <span><%= request.getAttribute("currentUserName") %></span></a> | <a href="https://help.percussion.com/"
                                                                             target="_blank" title="<i18n:message key="perc.ui.common.label@Help"/>"><i18n:message key="perc.ui.common.label@Help"/></a> | <a href="/rest/api-docs?url=/rest/swagger.json" target="_blank" title="REST API Documentation">API</a> | <a
            href="https://community.percussion.com/" target="_blank" title="<i18n:message key="perc.ui.common.label@Percussion Community"/>"><i18n:message key="perc.ui.common.label@Percussion Community"/></a> | <a
            href="javascript:void(0)" id="perc-help-about" title="<i18n:message key="perc.ui.common.label@About"/>"><i18n:message key="perc.ui.common.label@About"/></a> | <a href="/Rhythmyx/logout" title="<i18n:message
            key="perc.ui.common.label@Log Out"/>"><i18n:message
            key="perc.ui.common.label@Log Out"/></a><span id="perc-ui-view-indicator" class="<%=className%>"></span>
    </div>

    <div class="perc-header-topnav">
        <jsp:include page="mainnav.jsp" flush="true">
            <jsp:param name="mainNavTab" value="<%= mainNavTab %>"/>
        </jsp:include>
    </div>
</div>
<div class="perc-visible perc-dropshadow"></div>
