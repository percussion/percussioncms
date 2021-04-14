<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="com.percussion.pso.utils.RxRequestUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.percussion.util.IPSHtmlParameters"%>
<%@page import="com.percussion.webservices.content.IPSContentWs"%>
<%@page import="com.percussion.webservices.content.PSContentWsLocator"%>
<%@page import="java.util.List"%>
<%@page import="com.percussion.services.content.data.PSItemStatus"%>
<%@page import="com.percussion.utils.guid.IPSGuid"%>
<%@page import="java.util.Collections"%>
<%@page import="com.percussion.design.objectstore.PSLocator"%>
<%@page import="com.percussion.services.guidmgr.IPSGuidManager"%>
<%@page import="com.percussion.services.guidmgr.PSGuidManagerLocator"%>
<%@page import="com.percussion.cms.objectstore.PSCoreItem"%>
<%@page import="com.percussion.cms.objectstore.PSItemChildEntry"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.percussion.pso.utils.RxItemUtils"%>
<%@page import="com.percussion.webservices.PSErrorsException"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css" type="text/css" />		
<title>Child Test Page</title>
</head>
<body>

<%

	String childName = "sized";
	String field1 = "sized_cropbox_y";
	String field2 = "sized_cropbox_x";
	String field3 = "sized_cropbox_width";
	String field4 = "sized_cropbox_height";
	String field5 = "sized_img_height";
	String field6 = "sized_img_width";
	String field7 = "sized_img_size";
	String field8 = "sized_img_filename";
	String field9 = "sized_img_ext";
	String field10 = "sized_img_type";
	String field11 = "size_code";
	String binaryField = "sized_img";

	IPSContentWs cws = PSContentWsLocator.getContentWebservice(); 
	IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

	String user = RxRequestUtils.getUserName(request);
	String rxsession = RxRequestUtils.getSessionId(request); 
	String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID); 
	if(StringUtils.isNotBlank(contentid))
	{
	   
	       IPSGuid guid = gmgr.makeGuid(new PSLocator(contentid));  
	       List glist = Collections.singletonList(guid);
	       List slist = cws.prepareForEdit(glist, user);
	       List clist = cws.loadItems(glist, true, false, false, false, rxsession, user);

	       List ilist = cws.saveItems(clist, false, false, rxsession, user);
	       IPSGuid itemGuid = (IPSGuid)ilist.get(0);

	       List childrenToSave = new ArrayList();

	       List elist = cws.createChildEntries(itemGuid, childName, 1, rxsession, user); 
	       PSItemChildEntry entry = (PSItemChildEntry) elist.get(0);

	       RxItemUtils.setFieldValue(entry, field1, "Nabeel");
	       RxItemUtils.setFieldValue(entry, field2, "Saad");
	       RxItemUtils.setFieldValue(entry, field3, "width");
	       RxItemUtils.setFieldValue(entry, field4, "height");
	       
	       RxItemUtils.setFieldValue(entry, field5, "img height");
	       RxItemUtils.setFieldValue(entry, field6, "img width");
	       RxItemUtils.setFieldValue(entry, field7, "img size");
	       RxItemUtils.setFieldValue(entry, field8, "img filename");
	       
	       RxItemUtils.setFieldValue(entry, field10, "img type");
	       RxItemUtils.setFieldValue(entry, field9, "img ext");
	       RxItemUtils.setFieldValue(entry, field11, "thumb");
	       //RxItemUtils.setFieldValue(entry, binaryField, "Saad");

	       childrenToSave.add(entry); 

	       if(childrenToSave.size() > 0 )
	       {
		  cws.saveChildEntries(itemGuid, childName, childrenToSave, rxsession, user);	       
%>
		  <p>Saved the child entry</p>
<%
	       }

	       try
	       {
	       	  PSItemStatus itemStatus = new PSItemStatus(Integer.parseInt(contentid));
		  cws.releaseFromEdit(Collections.singletonList(itemStatus), false); 
%>
		  <p>Released from edit...</p>
<%
	       }
	       catch(Exception ee)
	       {
		  cws.checkinItems(Collections.singletonList(itemGuid), null, user);
%>
		  <p>Checked back in...</p>
<%
	       }

	}
	else
	{
%>
		No content id available
<%	}
		

%>
	</body>
</html>