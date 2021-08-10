<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="java.util.Enumeration"%>
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<!--
	Dynamically replace the following:
		- Name of the image in the JSP variable
		- Image width in JSP variable
		- Image height in JSP variable
-->
<%!
	public int getIntegerParameter(HttpServletRequest req, String paramName)
	{
		int value = 0;
		
		if (req != null && !paramName.equals("") && paramName != null)
		{
			String paramValue = req.getParameter(paramName);
			if (paramValue == null)
				return -1;
			else
			{
				try 
				{
					value = Integer.parseInt(paramValue);
				}
				catch (Exception e)
				{
					//do nothing
					value = -1;
				}
			}
		}
		
		return value;
	}
%>

<% 
    Log log = LogFactory.getLog(getClass()); 
  	Enumeration en = request.getAttributeNames();
while(en.hasMoreElements())
{
   String nm = (String) en.nextElement();
   log.info("Attribute " + nm);
   Object val = request.getAttribute(nm);
   log.info("Type is " + val.getClass().getCanonicalName()); 
}

	
	
	String imageName = "demo.jpg";
	int imageWidth = 600;
	int imageHeight = 451;
	
	
	/*
	String imageName = "landscape.jpg";
	int imageWidth = 2592;
	int imageHeight = 1944;
	*/
	
	int cropWidth = getIntegerParameter(request, "width");
	int cropHeight = getIntegerParameter(request, "height");
	int cropTop = getIntegerParameter(request, "y");
	int cropLeft = getIntegerParameter(request, "x");
%>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="image_editor_files/jquery.js"></script>
<script type="text/javascript" src="image_editor_files/form.js"></script>
<script type="text/javascript" src="image_editor_files/rotate.js"></script>
<script type="text/javascript" src="image_editor_files/iutil.js"></script>
<script type="text/javascript" src="image_editor_files/iresizable.js"></script>
<script type="text/javascript" src="image_editor_files/image_editor_general.js"></script>
<script type="text/javascript">
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
			$('#image_mode').val('');
			$('#angle').val(0);
			$(getImageElement()).clone().appendTo("#hiddenImage");
			changeMode();
			placeCropBox();
		}
	);


	function placeCropBox()
	{
		var overlay = $('#resizeMe');
		var widthField = $('#width');
	        var heightField = $('#height');
	        var topField = $('#y');
        	var leftField = $('#x');
        
		//Init Overlay
		var cropBoxWidth = <%=cropWidth%>;
		var cropBoxHeight = <%=cropHeight%>;
		var cropBoxTop = <%=cropTop%>;
		var cropBoxLeft = <%=cropLeft%>;
		
		var imageContainer = $('#imageContainer');
		var imageContainerTop = parseInt($(imageContainer).css('top').replace('px', ''));
		var imageContainerLeft = parseInt($(imageContainer).css('left').replace('px', ''));
		
		//if (cropBoxLeft < imageContainerLeft && cropBoxLeft != -1)
		//	cropBoxLeft = imageContainerLeft;

		//if (cropBoxTop < imageContainerTop && cropBoxTop != -1)
		//	cropBoxTop = imageContainerTop;

		if (cropBoxWidth != -1)
		{
			overlay.css('width', cropBoxWidth + 'px');
			widthField.val(cropBoxWidth);
		}
		
		if (cropBoxHeight != -1)
		{
			overlay.css('height', cropBoxHeight + 'px');
			heightField.val(cropBoxHeight);
		}
		
		var background_position = "";
		
		if (cropBoxLeft != -1)
	        {
	        	overlay.css('left', (cropBoxLeft + imageContainerLeft)  + 'px');
	        	leftField.val(cropBoxLeft);
	        	
			background_position = "-" + cropBoxLeft + "px";
	        }
	        
	        if (cropBoxTop != -1)
		{
			overlay.css('top', (cropBoxTop + imageContainerTop) + 'px');
			topField.val(cropBoxTop);
				
			if (background_position == "")
				background_position = "0px -" + cropBoxTop + "px";
			else
				background_position += " -" + cropBoxTop + "px";
		}
		
	        if (background_position != "")
	        overlay.css('background-position', background_position);
	}
	
</script>
<link href="image_editor_files/image_editor_general.css" type="text/css" rel="stylesheet"><title>Image Editor</title>

<link href="image_editor_files/highlighter.css" type="text/css" rel="stylesheet">
</head><body>
<div id="controls">
	<fieldset id="modes">
		<legend>Modes</legend>
		<form name="formAction" id="formAction" method="post" action="ajax_image_undo.php">
			<input name="file_path" id="file_path" value="uploaded/ajax_image_editor_demo.jpg" type="hidden">
			<p>
			<label>Retain aspect/ratio?</label> <input name="constraint" id="constraint" value="1" class="input" checked="checked" onclick="return toggleConstraint();" type="checkbox">
			<input value="Select Full Image" onclick="selectFullImage();" type="button" />
			<input value="Close Image Editor" onclick="editorClose();" type="button" />
			</p>
		</form>
	</fieldset>
	<fieldset id="imageInfo">
		<legend id="imageInfoLegend">Image Information</legend>
		<form name="formImageInfo" action="posted_info.jsp" method="post" id="formImageInfo">
			<p><input name="mode" id="image_mode" value="crop" type="hidden">
			<input name="path" id="path" value="<%=imageName%>" type="hidden">
			<label>Width:</label> <input readonly="readonly" name="width" id="width" value="" class="input imageInput" type="text">
			<label>Height:</label> <input readonly="readonly" name="height" id="height" value="" class="input imageInput" type="text">
			<label>X:</label> <input readonly="readonly" name="x" id="x" value="" class="input imageInput" type="text">
			<label>Y:</label> <input readonly="readonly" name="y" id="y" value="" class="input imageInput" type="text">
			<label>Ratio:</label> <input readonly="readonly" name="ratio" id="ratio" value="" class="input imageInput" type="text">
			&nbsp;<input type="submit" value="Done" />
			</p>
		</form>
	</fieldset>
</div>
<div id="imageArea">
    <div style="width: <%=imageWidth%>px; height: <%=imageHeight%>;" id="imageContainer">
    	<img style="opacity: 0.5;" src="<%=imageName%>" name="<%=imageName%>" height="<%=imageHeight%>" width="<%=imageWidth%>">
    </div>
    <div style="width: <%=imageWidth%>px; height: <%=imageHeight%>px; background-position: 0pt; left: 20px; top: 20px; background-image: url(<%=imageName%>);" id="resizeMe">
    	<div style="display: block;" id="resizeSE"></div>
    	<div style="display: none;" id="resizeE"></div>
    	<div style="" id="resizeNE"></div>
    	<div style="display: none;" id="resizeN"></div>
    	<div style="" id="resizeNW"></div>
    	<div style="display: none;" id="resizeW"></div>
    	<div style="" id="resizeSW"></div>
    	<div style="display: none;" id="resizeS"></div>
		<img id="loading" style="display: none;" src="image_editor_files/ajax-loader.gif">
    </div>
</div>

<!-- May not need this
<div id="hiddenImage">
    	<img src="<%=imageName%>.jpg" name="<%=imageName%>" height="<%=imageHeight%>px" width="<%=imageWidth%>px">
    </div>
    -->

</body></html>