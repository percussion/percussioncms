<%@page import="com.percussion.server.PSServer" %>
<%@page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@page import="com.percussion.utils.PSSpringBeanProvider" %>
<%@page import="com.percussion.utils.service.impl.PSUtilityService" %>
<%@page import="java.text.MessageFormat" %>
<%@page import="java.util.Calendar" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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
            + "<p><a class=\"perc-company-link\" target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://www.percussion.com\">https://www.percussion.com</a></p>"
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

    String fullrolestr = PSRoleUtilities.getUserRoles();
%>
<script>
        var gIsSaaSEnvironment = <%=isSaaS%>;
        $(document).ready(function () {
            $('.perc-header #perc-help-about').on("click",function (e) {
                $.perc_utils.confirm_dialog({
                    title: "About Percussion CMS",
                    type: "OK",
                    width: 400,
                    question: '<%= aboutMsg %>'
                });
            });
            $('.perc-header #perc-changepw').on("click",function (e) {
                $.perc_ChangePwDialog.open();
            });
        });
</script>
<script>
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");
                    var csrfToken = $("meta[name='_csrf']").attr("content");
                    var headers = {};
                    headers[csrfHeader] = csrfToken;
    $.ajaxSetup({
  headers: headers
  });
</script>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div class="perc-header">
    <div class="perc-logo" role="img" aria-label="<i18n:message key="perc.ui.header@Percussion Logo"/>"></div>
    <%-- logout must be after help because they both float and we want logout to left of help --%>
    <div class="perc-logout"><a href="javascript:void(0)" id="perc-changepw" title="<i18n:message key="perc.ui.change.pw@Change Password"/>"><i18n:message key="perc.ui.common.label@Welcome"/>
        <span><%= request.getAttribute("currentUserName") %></span></a> | <a href="https://help.percussion.com/"
                                                                             target="_blank" title="<i18n:message key="perc.ui.common.label@Help" />" rel="noopener noreferrer"><i18n:message key="perc.ui.common.label@Help"/></a> <%
        if (fullrolestr.contains("Admin")){
                                                                             %>| <a href="/rest/api-docs?url=/rest/openapi.json&docExpansion=none&deepLinking=true&filter=true&tagsSorter=alpha" target="_blank" rel="noopener noreferrer" title="REST API Documentation">API</a> <%}%>| <a
            href="https://community.percussion.com/" target="_blank" rel="noopener noreferrer" title="<i18n:message key="perc.ui.common.label@Percussion Community"/>"><i18n:message key="perc.ui.common.label@Percussion Community"/></a> | <a
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
