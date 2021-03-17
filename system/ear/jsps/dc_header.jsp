<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
           prefix="rxcomp"%>
<%
    String locale = PSI18nUtils.getSystemLanguage();
    String root = request.getContextPath();
%>
<html>
<head>
    <title>Desktop Content Explorer Header</title>
    <link rel="stylesheet" type="text/css" href="<%=root%>/sys_resources/css/templates.css"/>
    <link rel="stylesheet" type="text/css" href="<%=root%>/rx_resources/css/templates.css"/>
    <link rel="stylesheet" type="text/css" href="{concat('<%=root%>/rx_resources/css/',$lang,'/templates.css')}"/>
</head>
<body>
<div id="RhythmyxBanner"><!--Background image from templates.css--></div>
</body>
</html>