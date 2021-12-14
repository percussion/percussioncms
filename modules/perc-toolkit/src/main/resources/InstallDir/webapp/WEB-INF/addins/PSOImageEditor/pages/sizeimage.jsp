<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="com.percussion.pso.imageedit.services.jexl.ImageEditorTools"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<!--
	Dynamically replace the following:
		- Name of the image in the JSP variable
		- Image width in JSP variable
		- Image height in JSP variable
-->
<%
	//Get the basics first
	Log log = LogFactory.getLog(getClass()); 
	
%>


<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Expires" content="-1" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="cache-control" content="no-cache" />
<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css" type="text/css" />	
<link rel="stylesheet" href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_forms.css" type="text/css" />
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/jquery.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/form.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/rotate.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/iutil.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/iresizable.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/image_editor_general.js"></script>
<script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/psoimageeditor.js"></script>
<script type="text/javascript">

    var dirtyFlag = ${dirtyFlag}; 
    
    definedSize = {
        height : ${definedHeight},
        width  : ${definedWidth}
        };
        
	var warningEditorClose = 'Are you sure you want to close the window?';
	
	var imageHistory = false;
	var warningLostChanges = 'All unsaved changes made to the image will lost, are you sure you wish to continue?';
	var warningReset = 'All unsaved changes made to the image will be lost, are you sure you wish to continue?';
	var warningResetEmpty = 'No changes has been made to the image so far!';
	var warningUndoImage = 'Are you sure to restore the image to previous state?';
	var warningFlipHorizotal = 'Are you sure to flip the image horizotally?';
	var warningFlipVertical = 'Are you sure to flip the image vertically';
	var numSessionHistory = 0;
	var noChangeMadeBeforeSave = 'You have not made any changes to the images.';
	$(document).ready(
		function()
		{
		  
		}		
	);
	
	function initialize_me()
	{
		$('#image_mode').val('');
		$('#angle').val(0);
		$(getImageElement()).clone().appendTo("#hiddenImage");
		enableCrop();
		placeCropBox();
		$("input").bind("change", makeDirty);
	}
	
	 
    
	function placeCropBox()
	{
		var overlay = $('#resizeMe');
		var widthField = $('#width');
        	var heightField = $('#height');
       	 	var topField = $('#y');
       		var leftField = $('#x');
       		var ratioField = $('#ratio');
        	
		//Init Overlay
		var cropBoxWidth = ${cropbox.width};
		var cropBoxHeight = ${cropbox.height};
		var cropBoxTop = ${cropbox.y};
		var cropBoxLeft = ${cropbox.x};

		var imageContainer = $('#imageContainer');
		var imageContainerTop = parseInt($(imageContainer).css('top').replace('px', ''));
		var imageContainerLeft = parseInt($(imageContainer).css('left').replace('px', ''));
		
		overlay.css('width', cropBoxWidth + 'px');
		widthField.val(cropBoxWidth);
		
		overlay.css('height', cropBoxHeight + 'px');
		heightField.val(cropBoxHeight);
		
		var background_position = "";
		
		overlay.css('left', (cropBoxLeft + imageContainerLeft)  + 'px');
	        leftField.val(cropBoxLeft);
	        	
	        background_position = "-" + cropBoxLeft + "px";
		background_position += " -" + cropBoxTop + "px";
	     
	     	overlay.css('top', (cropBoxTop + imageContainerTop) + 'px');
		topField.val(cropBoxTop);

	        if (background_position != "")
	        	overlay.css('background-position', background_position);
	        	
	        ratioField.val(cropBoxHeight / cropBoxWidth);
	}
	
	function fixIE6DragProblem()
	{
	   var bversion = navigator.appVersion;
	   if(bversion.indexOf("MSIE 6") > 1)
	   {
	     document.execCommand("BackgroundImageCache",false,true);
	   }
	}
	
	function handleOnLoad()
	{
	    fixIE6DragProblem();
	    initialize_me();    
	}
	
	
			
</script>
<link href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_general.css" type="text/css" rel="stylesheet" />
<title>Image Editor -- ${sizelabel}</title>

<link href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/highlighter.css" type="text/css" rel="stylesheet" />
</head><body onLoad="handleOnLoad();" >	
<p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>	
<h2>${sizelabel}</h2>
<p>

<div id="controls">
	<form name="formAction" id="formAction" method="post">
	<fieldset id="modes">
		<legend>Commands</legend>
			<input name="file_path" id="file_path" value="" type="hidden">
			<p>
			<input class="hbutton" value="Start Over" name="_target0" type="submit" />
			<input class="hbutton" value="Previous" name="_target${page - 1}" type="submit" />
			<input class="hbutton" value="Select Full Image" onclick="selectFullImage();" type="button" />
			<input class="hbutton" value="Close Image Editor" onclick="close_dirty();" type="button" />
			<input class="hbutton" value="Open New Image" onclick="newimage_dirty();" type="button" /> 
			<input class="hbutton" value="Next" name="_target${page + 1}" type="submit" />
			<input class="hbutton" value="Finish" name="_target${pagecount - 1}" type="submit" /> 
			</p>
	</fieldset>
	<fieldset id="imageInfo">
		<legend id="imageInfoLegend">Image Information</legend>
			<p><input name="mode" id="image_mode" value="crop" type="hidden">
			<input name="path" id="path" value="${sizecode}" type="hidden" >
			<label>Width:</label> 
			<spring:bind path="SizedImage.width">
				<input readonly="readonly" id="${status.expression}" class="input imageInput" name="${status.expression}" type="text" value="${status.value} size="5" />&nbsp;
			</spring:bind>
			<label>Height:</label>
			<spring:bind path="SizedImage.height">
				<input readonly="readonly" id="${status.expression}" class="input imageInput" name="${status.expression}" type="text" value="${status.value}"  size="5"   />&nbsp;
			</spring:bind>
			<label>X:</label>
			<spring:bind path="SizedImage.x">
				<input  readonly="readonly" id="${status.expression}" class="input imageInput" name="${status.expression}" type="text" value="${status.value}"  size="5"  />&nbsp;
			</spring:bind>
			<label>Y:</label>
			<spring:bind path="SizedImage.y">
				<input readonly="readonly" id="${status.expression}" class="input imageInput" name="${status.expression}"  type="text" value="${status.value}"  size="5"   />&nbsp;
			</spring:bind>
			<label>Ratio:</label> <input readonly="readonly" name="ratio" id="ratio" value="" class="input imageInput" type="text"  size="5" >&nbsp;
			
			<label>Retain aspect/ratio?</label>
			<input name="constraint" id="constraint" value="${cropbox.constraint}" <c:if test="${cropbox.constraint == 1}" >checked="checked"</c:if> class="input" onclick="return toggleConstraint();" type="checkbox">
			<input type="hidden" id="openImed" name="openImed" value="" />
			</p>
	</fieldset>	  
	<spring:bind path="image.sizedImages">
		<input type="hidden" id="${status.expression}" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	
	<input type="hidden" id="dirty" name="dirty" value="false" /> 
	<input type="hidden" id="operation" name="" value=""  />		
	<input type="hidden" id="openImed" name="openImed" value="" />
	<input type="hidden" id="pageid" name="_page" value="${page}" />
		   
	</form>
</div>
<div id="imageArea">
    <div style="width: ${displayImage.width}px; height: ${displayImage.height};" id="imageContainer">
    	<img style="opacity: 0.5;" src="${displayImage.url}" name="${displayImage.url}" height="${displayImage.height}" width="${displayImage.width}">
    </div>
    <div style="width: ${displayImage.width}px; height: ${displayImage.height}px; background-position: 0pt; left: 20px; top: 20px; background-image: url(${displayImage.url});" id="resizeMe">
    	<div style="display: block;" id="resizeSE"></div>
    	<div style="display: none;" id="resizeE"></div>
    	<div style="" id="resizeNE"></div>
    	<div style="display: none;" id="resizeN"></div>
    	<div style="" id="resizeNW"></div>
    	<div style="display: none;" id="resizeW"></div>
    	<div style="" id="resizeSW"></div>
    	<div style="display: none;" id="resizeS"></div>
		<img id="loading" style="display: none;" src="/Rhythmyx/rx_resources/addins/psoimageeditor/images/ajax-loader.gif">
    </div>
</div>


</body></html>