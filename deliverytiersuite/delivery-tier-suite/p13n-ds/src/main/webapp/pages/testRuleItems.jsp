<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="java.util.List"%>
<%@page import="com.percussion.soln.p13n.delivery.data.IDeliveryDataService"%>
<%@page import="com.percussion.soln.p13n.delivery.data.DeliveryListItem"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
</head>
<body>
<%
IDeliveryDataService dataSvc = (IDeliveryDataService)
    WebApplicationContextUtils
    .getRequiredWebApplicationContext(request.getSession().getServletContext())
    .getBean("deliveryDataService");
List listItems = dataSvc.retrieveAllListItems();
%>

<table>
<thead>
<tr><th>List Item</th><th>Filters</th><th>Snippets</th></tr>
</thead>
<tbody>
<%
for ( int i = 0; i < listItems.size(); i++) { 
DeliveryListItem item = (DeliveryListItem) listItems.get(i);
%>
<tr><td><%=item.getId()%></td><td><%=item.getSnippetFilterIds()%></td><td><%=item.getSnippets()%></td></tr>
<%
}
%>
</tbody>
</table>
</body>
</html>