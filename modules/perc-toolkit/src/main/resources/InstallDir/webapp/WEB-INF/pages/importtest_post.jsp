<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"
		 import="com.percussion.design.objectstore.PSLocator"
		 import="com.percussion.pso.utils.IPSOItemSummaryFinder"
		 import="com.percussion.security.SecureStringUtils"
		 import="com.percussion.services.contentmgr.IPSContentMgr"
		 import="com.percussion.services.contentmgr.IPSNode"
		 import="com.percussion.services.contentmgr.PSContentMgrConfig"
		 import="com.percussion.services.contentmgr.PSContentMgrLocator, com.percussion.services.guidmgr.IPSGuidManager, com.percussion.services.guidmgr.PSGuidManagerLocator, com.percussion.services.guidmgr.PSGuidUtils, com.percussion.services.legacy.IPSCmsObjectMgr, com.percussion.services.legacy.PSCmsObjectMgrLocator, com.percussion.utils.guid.IPSGuid, com.percussion.webservices.content.IPSContentWs, com.percussion.webservices.content.PSContentWsLocator, com.percussion.webservices.security.IPSSecurityWs, com.percussion.webservices.security.PSSecurityWsLocator"
		 import="javax.servlet.jsp.JspWriter"
%>
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

	<%!
	//initialize variables used in the JSP page
	
	IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
	IPSContentMgr mgr = PSContentMgrLocator.getContentMgr(); 
	IPSContentWs contentWs = PSContentWsLocator.getContentWebservice(); 
	IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
	IPSSecurityWs securityWs = PSSecurityWsLocator.getSecurityWebservice();
	IPSOItemSummaryFinder isFinder = null; 
	List myGuids;
	PSContentMgrConfig myConfig = null;
	
	%>
	
	<%
		//Checking for vulnerability
		String str = request.getQueryString();
		if(str != null && str != ""){
			response.sendError(response.SC_FORBIDDEN, "Invalid QueryString!");
		}
	String cid=request.getParameter("sys_contentid");
		//Checking for vulnerability
		if(!SecureStringUtils.isValidPercId(cid)){
			response.sendError(response.SC_FORBIDDEN, "Invalid cid!");
		}
	PSLocator loc = isFinder.getCurrentOrEditLocator(cid);
	IPSGuid contentGUID=gmgr.makeGuid(loc);
	List myGuid = PSGuidUtils.toGuidList(contentGUID);
	List contentList = mgr.findItemsByGUID(myGuid, myConfig);

	IPSNode contentNode = (IPSNode)contentList.get(0);
	String cmsURL = contentNode.getProperty("feedFormat").toString();
	String feedURL = contentNode.getProperty("feedUrl").toString();
	%>

<html><head><title>REST Import Service</title>
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js"></script>
	<script type="text/javascript">
	function htmlEncode(value) {
	    return $('<div/>').text(value).html();
	} 
	  $(function(){
	    $("#form").submit(function(){
	        dataString = request.getParameter("");
			posturl = $("#post_url").val();
			
	        $.ajax({
	        type: "POST",
	        // url: "/Rhythmyx/services/Content/import/cdcSyndicationImport",
			url: posturl,
	        data: dataString,
			contentType: "text/xml",
	        dataType: "xml",
	        complete: function(xhr, status) {
	 	//	alert(data);
		
	            $("#message_ajax").html(htmlEncode(xhr.responseText));
	        }
	 
	        });
	 
	        return false;           
	 
	    });
	});
	</script>
  </head>
<body>

<table width="100%" height="66" background="/Rhythmyx/sys_resources/images/banner_bkgd.jpg" style="background-attachment: fixed; background-repeat: no-repeat;">
<tbody><tr>
<td>
</td>
</tr>
</tbody></table>
<h1>REST Import Service</h1>
	<csrf:form id="form" action="/Rhythmyx/services/Content/import/cvaSyndicationImport" method="post">
		<!--  CMS IMPORT URL:<br/><textarea rows="2" cols="80" NAME="post_url" id="post_url" ></textarea><br>  -->
		FEED URL:<br/>
		<textarea rows="2" cols="80" name="body" id="body"></textarea><br/>
		<input type="submit"></input>
	</csrf:form>
</body>
</html>
