<%@ page session="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="str" uri="http://jakarta.apache.org/taglibs/string-1.1" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@page import="com.percussion.pso.imageedit.services.ImageSizeDefinitionManagerLocator"%>
<%@page import="com.percussion.pso.imageedit.services.ImageSizeDefinitionManager"%>
<%@page import="java.util.List"%>
<%@page import="com.percussion.pso.imageedit.data.ImageSizeDefinition"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.percussion.pso.imageedit.data.ImageBean"%>
<%
	ImageUrlBuilder iub = ((ImageUrlBuilder)request.getAttribute("ImageUrlBuilder"));
	UserSessionData usd = (UserSessionData)request.getSession().getAttribute("userData");
	MasterImageMetaData mimd = usd.getMimd();
	
%>
<%@page import="com.percussion.pso.imageedit.web.ImageUrlBuilder"%>
<%@page import="com.percussion.pso.imageedit.data.UserSessionData"%>
<%@page import="com.percussion.pso.imageedit.data.MasterImageMetaData"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.percussion.pso.imageedit.data.ImageMetaData"%>
<%@page import="com.percussion.pso.imageedit.data.SizedImageMetaData"%>

<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="java.util.Enumeration"%>


<% 
    Log log = LogFactory.getLog(getClass()); 
   
    //ImageEditorTools.logRequestAttributes(request,"main.jsp"); 
%>

<%@page import="com.percussion.pso.imageedit.services.jexl.ImageEditorTools"%>
<html>
	<head>
		<title>Image Editor</title>
		<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css" type="text/css" />
		<link rel="stylesheet" href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_forms.css" type="text/css" />
		<link href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_general.css" type="text/css" rel="stylesheet" />
		 		
		<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/jquery.js"></script>
		<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/form.js"></script>
		<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/psoimageeditor.js"></script>
		<script>
		    
		    var dirtyFlag = ${dirtyFlag}; 
		    
		    $(document).ready(
		        function()
				{
				    $("#dirty").val(${dirtyFlag});
				    $("input").bind("change",makeDirty);
				    $("textarea").bind("change",makeDirty);  
				}
		    );
                       
			function concatCheckBoxGroup(fieldName)
			{
				var underscoreAt = fieldName.search("_");
				var hiddenFieldName = fieldName.substring(0, underscoreAt);
				var hiddenField = document.getElementById(hiddenFieldName);
				
				var fields = document.getElementsByName(fieldName);
				//alert("field: " + fields + " == fieldName " + fieldName + " -- numoffields: " + fields.length);
				var fieldValue ="";
				if (fields.length > 1) 
				{
					for (var i = 0; i < fields.length; i++) 
					{
						var t = fields[i].value;
						//alert("is " + t + " it checked? " + fields[i].checked);
						if (fields[i].checked == true) 
							if (fieldValue == "") 
								fieldValue = fields[i].value;
							else
								fieldValue += "," + fields[i].value;
					}
				}
				
				hiddenField.value = fieldValue;
			}
			
			function setSizedImageVisibility(visibleFlag)
			{
				var sizedDiv = document.getElementById("sized_images_div");
				sizedDiv.style.display = visibleFlag;
			}
			
			function toggleSizedImages(fieldVal)
			{
				if (fieldVal != "" && fieldVal != null)
					setSizedImageVisibility("block");
					
				document.getElementById("action").value = "";
			}
			
			function clearFileUpload()
			{
				document.getElementById("action").value = "cleared";
				document.getElementById("fileUpload").value = "";
				
				document.getElementById("filename").value = "";
				document.getElementById("ext").value = "";
				document.getElementById("size").value = "";
				document.getElementById("mimetype").value = "";
				document.getElementById("imgwidth").value = "";
				document.getElementById("imgheight").value = "";
				
				setSizedImageVisibility("none");
				setPreviewLinkVisibility("none");
				
				var checkboxes = document.getElementsByName("sizedImages_cbg");
				for (var i = 0; i < checkboxes.length; i++)
				{
					checkboxes[i].checked = false;
				}
				dirtyFlag = true; 
			}
			
			function setPreviewLinkVisibility(visibleFlag)
			{
				var previewSpan = document.getElementById("previewlink");
				if (visibleFlag == "block") visibleFlag = "inline";
				previewSpan.style.display = visibleFlag;
			}
			
			function handleNext()
			{
			   concatCheckBoxGroup('sizedImages_cbg');
			}
			
			function handleOnLoad()
			{
			    setSizedImageVisibility("${sizedDivStyle}");
			}
			
		</script>
	</head>
	<body onload="handleOnLoad();">
	<img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg">
		<h3>Image Editor</h3>
		<p>Please fill in all the information,
		upload a high resolution image and choose what other
		image types you would like to create.</p>
		
		<spring:hasBindErrors name="image">
		<p>
		   <font color="red">
               	<c:forEach items="${errors.allErrors}" var="errMsgObj">
			    	<spring:message message="${errMsgObj}"/><br />                                   
                </c:forEach>
           </font>
        </p>
        <p>&nbsp;</p>
        </spring:hasBindErrors>
        
               
		<form method="POST" enctype="multipart/form-data">
		<fieldset id="modes">
		<legend>Commands</legend>
			<input name="file_path" id="file_path" value="" type="hidden">
			<p>
			<input class="hbutton" value="Close Image Editor" onclick="close_dirty();" type="button" />
			<input class="hbutton" value="Open New Image" onclick="newimage_dirty();" type="button" /> 
			<input class="hbutton" type="submit" value="Next" name="_target<c:out value="${page + 1}"/>" onclick="javascript:handleNext();" />
			<input class="hbutton" value="Finish" name="_target${pagecount - 1}" type="submit" onclick="javascript:handleNext();"/> 
			</p>
	 </fieldset>
	    <fieldset id="imageInfo">
	    <legend id="imageInfoLegend">Image Information</legend>
			<table>
				<tr>
					<td>* System Title:</td>
					<td>
						<spring:bind path="image.sysTitle">
							<input name="${status.expression}" type="text" value="${status.value}"  />
						</spring:bind>
					</td>
				</tr>
				<tr>
					<td>* Display Title:</td>
					<td>
						<spring:bind path="image.displayTitle">
							<input name="${status.expression}" type="text" value="${status.value}"  />
						</spring:bind>
					</td>
				</tr>
				<tr>
					<td>Image Alt Text:</td>
					<td>
						<spring:bind path="image.alt">
							<input name="${status.expression}" type="text" value="${status.value}"  />
						</spring:bind>
					</td>
				</tr>
				<tr>
					<td>Image Description:</td>
					<td>
						<spring:bind path="image.description">
							<textarea name="${status.expression}"><c:out value="${status.value}" /></textarea>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<td>Upload Image:</td>
					<td>
						<input id="fileUpload" type="file" name="binary" onchange="toggleSizedImages('this.value')" />
						<c:if test="${metadata.size > 0}">
						   <input class="hbutton" type="button" value="Clear" onclick="clearFileUpload();" />
						   <span id="previewlink">&nbsp;<a href="${metadata.url}" target="_new">Preview</a></span>
						</c:if>
					</td>
				</tr>
				<c:if test="${metadata.size > 0}"> 
				<tr>
					<td>Image filename</td>
					<td>
					    <input type="text" id="filename"  name="filename" value="${metadata.filename}" readonly="readonly" 
					      class="plaintext"/>
					</td>
				</tr>				
				<tr>
					<td>Image Extension</td>
					<td>
					    <input type="text" id="ext"  name="ext" value="${metadata.ext}" readonly="readonly" 
					     class="plaintext"/>				
					</td>
				</tr>				
				<tr>
					<td>Image Mimetype</td>
					<td>
					    <input type="text" id="mimetype"  name="mimetype" value="${metadata.mimetype}" readonly="readonly" 
					     class="plaintext"/>			
					</td>
				</tr>	
				<tr>
					<td>Image Size</td>
					<td>
					    <input type="text" id="size"  name="size" value="${metadata.size}" readonly="readonly" 
					     class="plaintext"/>					
					</td>
				</tr>				
				<tr>
					<td>Image Width</td>
					<td>
					    <input type="text" id="imgwidth"  name="imgwidth" value="${metadata.width}" readonly="readonly" 
					     class="plaintext"/>
					</td>
				</tr>								
				<tr>
					<td>Image Height</td>
					<td>
					     <input type="text" id="imgheight"  name="imgheight" value="${metadata.height}" readonly="readonly" 
					     class="plaintext"/>
					</td>
				</tr>
				</c:if>
			</table>
			</fieldset>
			
			<div id="sized_images_div">
			
	        <fieldset id="imageTypes">
	        <legend id="imageTypesLegend">Image Types</legend>
			<table cellpadding="5">
			<tr>
			<c:forEach var="sizeDef"  items="${allDisplaySizes}" varStatus="loop">
			<td>
			<input type="checkbox" name="sizedImages_cbg" value="${sizeDef.code}" 
    		<c:if test="${sizeDef.checked == 'checked'}">
			checked="checked" 			
			</c:if>			
			/>
			<strong><c:out value="${sizeDef.label}" /></strong> 
			</td>	
			<c:if test="${loop.index % 2 == 1}">
			   </tr><tr>
			</c:if> 
			</c:forEach>
			</tr>
			</table>
			
			<spring:bind path="image.sizedImages">
				<input type="hidden" id="${status.expression}" name="${status.expression}" value="${status.value}" />
			</spring:bind>
			<input type="hidden" id="action" name="action" value="" />
			<input type="hidden" id="openImed" name="openImed" value="" />
			<input type="hidden" id="dirty" name="dirty" value="false" />
			<input type="hidden" id="operation" name="" value=""  /> 
			<input type="hidden" id="pageid" name="_page" value="${page}" />
		   
			</div>
			</fieldset>
			<p></p>
			
		</form>
	</body>
</html>