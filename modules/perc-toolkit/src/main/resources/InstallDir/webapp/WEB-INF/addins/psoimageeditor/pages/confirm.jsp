<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"  %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@page import="com.percussion.pso.imageedit.web.ImageUrlBuilder"%>
<%@page import="com.percussion.pso.imageedit.data.*"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.Collection"%>

<%
	ImageUrlBuilder iub = ((ImageUrlBuilder)request.getAttribute("ImageUrlBuilder"));
	UserSessionData usd = (UserSessionData)request.getSession().getAttribute("userData");
	MasterImageMetaData mimd = usd.getMimd();
	String imageUrl = "";
	
%>
<html>
	<head>
		<title>Confirmation</title>
		<script type="text/javascript">
		    var dirtyFlag = ${dirtyFlag}; 
		</script>
		<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css" type="text/css" />	
        <link rel="stylesheet" href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_forms.css" type="text/css" />
        <link href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_general.css" type="text/css" rel="stylesheet" />
        
        <script type="text/javascript" src="/Rhythmyx/sys_resources/js/browser.js"></script>
        <script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/psoimageeditor.js"></script>
        <script type="text/javascript">
            var dirtyFlag=${dirtyFlag}; 
            
         
        </script>
        
	</head>
	<body>
	    <p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>	
		<h2>Confirmation</h2>
		<div>
		<form method="POST">
		<fieldset id="modes">
		   <legend>Commands</legend>
		   <p>		   
			<c:if test="${pagecount > 2}">
			   <input class="hbutton" type="submit" value="Start Over" name="_target0" />&nbsp;
			</c:if>		
			<input class="hbutton" type="submit" value="Previous" name="_target${page - 1}" />&nbsp;
			<input class="hbutton" value="Close Image Editor" onclick="close_dirty();" type="button" />
			<input class="hbutton" value="Open New Image" onclick="newimage_dirty();" type="button" /> 
			<input class="hbutton" value="Save and Exit" type="button" onclick="saveAndExit();"/>
			<input class="hbutton" type="button" value="Save and Continue" onClick="saveAndContinue();" />
			<input type="hidden" id="operation" name="" value=""  />
			<input type="hidden" id="pageid" name="_page" value="${page}" />
		    <spring:bind path="image.sizedImages">
		        <input type="hidden" id="${status.expression}" name="${status.expression}" value="${status.value}" />
	        </spring:bind>
		</p>
		</fieldset>
		<input type="hidden" id="action" name="action" value="" /> 
		</form>
		<p>Look through the details of the image below and decide if you wish to confirm the creation of this item in Rhythmyx.</p>
		<p>
		<div class="imageContainer">
		   <div class="clear">&nbsp;</div> 
			    <c:forEach var="sizedImage" items="${sizedImages}">
			         <div class="imageBox">
			         <h3>${sizedImage.label}</h3>
			         <img src="${sizedImage.url}" alt="${sizedImage.label}" 
			           height="${sizedImage.height}" width="${sizedImage.width}" /> 
                     </div>
                     
			    </c:forEach>
	     </div>
	     <div class="imageContainer">
	         <div class="clear">&nbsp;</div>
			    <c:choose>
			    <c:when test="${displayImage.url != null}">
				     <div class="imageBox">
			         <span class="clear">&nbsp;</span>
					 <h3>Original Image</h3>
				     <img src="${displayImage.url}" height="${displayImage.height}"
					 width="${displayImage.width}"
					 alt="${displayImage.alt}"/>
					 <span class="clear">&nbsp;</span>
				     </div>
					</c:when>
				<c:otherwise>
						<p>No main image was uploaded.</p>
			    </c:otherwise>
			    </c:choose>
	  </div></div>
	</body>
</html>