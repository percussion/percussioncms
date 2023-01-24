<%@page import="com.percussion.util.* " 
   errorPage="error.jsp"
   pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
   prefix="rxcomp"%>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>



<%
String requestid = request.getParameter("requestid");
String edition = request.getParameter(IPSHtmlParameters.SYS_EDITIONID);
pageContext.setAttribute("requestid", requestid);
pageContext.setAttribute("edition", edition);
%>
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
   <title>${rxcomp:i18ntext('jsp_publish@Demand Publishing',param.sys_lang)}</title>
   <jsp:include page="/Rhythmyx/ui/header.jsp"/>
   <script src="/Rhythmyx/sys_resources/js/yui/yahoo/yahoo-min.js"></script>
   <script src="/Rhythmyx/sys_resources/js/yui/event/event-min.js"></script>
   <script src="/Rhythmyx/sys_resources/js/yui/connection/connection-min.js"></script>
   <script src="/Rhythmyx/sys_resources/js/publishing.js"></script>
</head>
<body onload="PS.demand.startEditionUpdateTimer()">
   <div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
      <p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>
      <h3>${rxcomp:i18ntext('jsp_publish@Demand Publishing',param.sys_lang)}<h3>
      <p>${rxcomp:i18ntext('jsp_publish@Edition',param.sys_lang)}: ${edition}</p>
   </div>
   <table border="0" cellspacing="0" cellpadding="0">
   <tr>
	   <td class="headercell2font">Progress</td>
	   <td>
		   <div id="status_frame" 
		       style="width: 400px; height 12px; border: 1px solid black">
		       <div id="_status_progress" 
		             style="width: 0px; height: 15px; background-color: blue"></div>
		   </div>
	   </td>
   </tr>
   <tr>
      <td class="headercell2font" style="padding-right: 2em">Start Time</td>
      <td id="_start_time" class="datacell1font"></td>
   </tr>
   <tr>
	   <td class="headercell2font" style="padding-right: 2em">Elapsed</td>
	   <td id="_elapsed_time" class="datacell1font"></td>
   </tr>
   <tr>
      <td class="headercell2font" style="padding-right: 2em">Queued</td>
      <td id="_status_queued" class="datacell1font"></td>
   </tr>
   <tr>
      <td class="headercell2font" style="padding-right: 2em">Prepared For Delivery</td>
      <td id="_status_prepared" class="datacell1font"></td>
   </tr>
   <tr>
      <td class="headercell2font" style="padding-right: 2em">Delivered</td>
      <td id="_status_delivered" class="datacell1font"></td>
   </tr>
   <tr>
      <td class="headercell2font" style="padding-right: 2em">Failed</td>
      <td id="_status_failed" class="datacell1font">&nbsp;</td>
   </tr>
   </table>
   <form>
      <input id="requestid" name="requestid" type="hidden" value="${requestid}" />
   </form>
</body>
</html>
