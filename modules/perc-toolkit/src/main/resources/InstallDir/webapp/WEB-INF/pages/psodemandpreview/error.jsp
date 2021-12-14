<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
<html>
   <head>
     <meta content="Percussion Rhythmyx" name="generator"/>
	 <title>Publish for Preview Errors</title>
	 <link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css"
			type="text/css" />		
   </head>
   <body>
   <p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>
   <h3>Errors occurred while publishing for preview</h3>
	  <p>${errorMessage}</p>
   <p><a onclick="javascript: Window.close();">Close</a></p>
   </body>
</html>