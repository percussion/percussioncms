/*
	* author: Logan Cai
	* Email: cailongqun (at) yahoo.com.cn
	* Website: www.phpletter.com
	* Created At: 21/April/2007
	* Modified At: 21/April/2007
*/

	/**
	*	get current selected mode value
	*/
	function getModeValue()
	{
		//Nabeel hardcoding crop value only
		return "crop";
	}

	function resetEditor()
	{
		if(isImageHistoryExist())
		{
			changeMode(true);
		}else
		{
			alert(warningResetEmpty);
		}
		return false;
		
	}

	/**
	*	enable to crop function
	*/
	function enableCrop()
	{
	var widthField = $('#width');
        var heightField = $('#height');
        var topField = $('#y');
        var leftField = $('#x');
        var ratioField = $('#ratio');
        var imageToResize = getImageElement();
        var imageWidth = $(imageToResize).attr('width');
        var imageHeight = $(imageToResize).attr('height');

        var overlay = $('#resizeMe');
        var imageContainer = $('#imageContainer');
		var imageContainerTop = parseInt($(imageContainer).css('top').replace('px', ''));
		var imageContainerLeft = parseInt($(imageContainer).css('left').replace('px', ''));
        //Init Container
        $(imageContainer).css('width', imageWidth + 'px');
        $(imageContainer).css('height', imageHeight + 'px');
        $(imageToResize).css('opacity', '.5');
		
        //Init Overlay
        overlay.css('background-image', 'url('+ $(imageToResize).attr('src')+')');
        /*Nabeel modifying initial crop box size to be correct aspect ratio, and 1/5th images size
        overlay.css('width', imageWidth + 'px');
        overlay.css('height', imageHeight + 'px');
        */
        overlay.css('width', Math.floor(imageWidth / 5) + 'px');
        overlay.css('height', Math.floor(imageHeight / 5) + 'px');
        
        //Init Form
        widthField.val(Math.floor(imageWidth / 5));
        heightField.val(Math.floor(imageHeight / 5));
        topField.val(0);
        leftField.val(0);
        ratioField.val(imageHeight / imageWidth);
	    $(overlay).Resizable(
			{
				minWidth: 30,
				minHeight: 30,
				maxWidth: imageWidth,
				maxHeight: imageHeight,
				minTop: imageContainerTop,
				minLeft: imageContainerLeft,
				maxRight: (parseInt(imageWidth) + imageContainerLeft),
				maxBottom: (parseInt(imageHeight) + imageContainerTop),
				dragHandle: true,
				onDrag: function(x, y)
				{
					this.style.backgroundPosition = '-' + (x - imageContainerLeft) + 'px -' + (y - imageContainerTop) + 'px';
					$(topField).val(Math.round(y - imageContainerTop));
					$(leftField).val(Math.round(x - imageContainerLeft));
				},
				handlers: {
					se: '#resizeSE',
					e: '#resizeE',
					ne: '#resizeNE',
					n: '#resizeN',
					nw: '#resizeNW',
					w: '#resizeW',
					sw: '#resizeSW',
					s: '#resizeS'
				},
				onResize : function(size, position) {
					this.style.backgroundPosition = '-' + (position.left - imageContainerLeft) + 'px -' + (position.top - imageContainerTop) + 'px';
					$(widthField).val(Math.round(size.width));
					$(heightField).val(Math.round(size.height));
					$(topField).val(Math.round(position.top - imageContainerTop));
					$(leftField).val(Math.round(position.left - imageContainerLeft));
					$('#ratio').val($(overlay).ResizableRatio() );	
				}
			}
		);
		enableConstraint();
		toggleConstraint();
		
	}
	
	/**
	*	hide all handlers 
	*/	
	function hideHandlers()
	{
		$('#resizeSE').hide();
		$('#resizeE').hide();
		$('#resizeNE').hide();	
		$('#resizeN').hide();
		$('#resizeNW').hide();
		$('#resizeW').hide();	
		$('#resizeSW').hide();
		$('#resizeS').hide();	
	}
	/**
	*
	*	enable to resize the image
	*/
	function enableResize(constraint)
	{
		hideHandlers();
		var imageToResize = getImageElement();	
		var imageContainer = $('#imageContainer');
		var imageContainerTop = parseInt($(imageContainer).css('top').replace('px', ''));
		var imageContainerLeft = parseInt($(imageContainer).css('left').replace('px', ''));		
		var resizeMe = $('#resizeMe');
		var width = $('#width');
		var height = $('#height');
		//ensure the container has same size with the image
		$(imageContainer).css('width', $(imageToResize).attr('width') + 'px');
		$(imageContainer).css('height', $(imageToResize).attr('height') + 'px');
		$(resizeMe).css('width', $(imageToResize).attr('width') + 'px');
		$(resizeMe).css('height', $(imageToResize).attr('height') + 'px');
		$('#width').val($(imageToResize).attr('width'));
		$('#height').val($(imageToResize).attr('height'));
		$('#x').val(0);
		$('#y').val(0);		
		$(resizeMe).Resizable(
			{
				minWidth: 50,
				minHeight: 50,
				maxWidth: 2000,
				maxHeight: 2000,
				minTop: imageContainerTop,
				minLeft: imageContainerLeft,
				maxRight: 2000,
				maxBottom: 2000,
				handlers: {
					s: '#resizeS',
					se: '#resizeSE',
					e: '#resizeE'
				},
				onResize: function(size)
				{
					$(imageToResize).attr('height', Math.round(size.height).toString());
					$(imageToResize).attr('width', Math.round(size.width).toString());
					$(width).val(Math.round(size.width));
					$(height).val(Math.round(size.height));		
					$(imageContainer).css('width', $(imageToResize).attr('width') + 'px');
					$(imageContainer).css('height', $(imageToResize).attr('height') + 'px');	
					$('#ratio').val($(resizeMe).ResizableRatio() );			
				}

			}
		);	
		$(resizeMe).ResizeConstraint(constraint);
		if(typeof(constraint) == 'boolean' && constraint)
		{		
			$('#resizeS').hide();
			$('#resizeE').hide();			
		}else
		{			
			$('#resizeS').show();
			$('#resizeE').show();
		}		
		$('#resizeSE').show();
		$('#ratio').val($(resizeMe).ResizableRatio() );	
			
			
	}
	
	function enableConstraint()
	{
		$('#constraint').removeAttr('disabled');
	}
	
	function disableConstraint()
	{
		$('#constraint').attr('disabled', 'disabled');
	}
	function ShowHandlers()
	{
		$('#resizeSE').show();
		$('#resizeE').show();
		$('#resizeNE').show();	
		$('#resizeN').show();
		$('#resizeNW').show();
		$('#resizeW').show();	
		$('#resizeSW').show();
		$('#resizeS').show();	
	}	
	
	/**
	*	turn constraint on or off
	*/
	function toggleConstraint()
	{
		hideHandlers();	
		var constraintField = document.getElementById("constraint");
		
		if(document.formAction.constraint.checked)
		{
			$('#resizeMe').ResizeConstraint(true);
			switch(getModeValue())
			{
				case "crop":
					$('#resizeSE').show();
					$('#resizeNE').show();	
					$('#resizeNW').show();
					$('#resizeSW').show();
					constraintField.checked = true;
					constraintField.value = 1;
					
					
					break;
			}
						
		}else
		{
			$('#resizeMe').ResizeConstraint(false);
			switch(getModeValue())
			{
				case "crop":
					ShowHandlers();
					constraintField.value = 0;
					break;					
			}			
		}
		
	}
	

	/**
	*	restore to the state the image was
	*/
/*	function restoreToOriginal(warning)
	{
			if(typeof(warning) == "boolean" && warning)
			{
					if(!window.confirm(warningReset))
					{
						return false;	
					}
			}
			
			$("#imageContainer").empty();
			$("#hiddenImage img").clone().appendTo("#imageContainer");		
			return true;
			
	
	}
*/

	function getImageElement()
	{
		var imageElement = null;
		var imageContainer = document.getElementById('imageContainer');
		for(var i = 0; i < imageContainer.childNodes.length; i++)
		{
			if((typeof(imageContainer.childNodes[i].name) != "undefined" && imageContainer.childNodes[i].name.toLowerCase() == 'image') || (typeof(imageContainer.childNodes[i].tagName) != "undefined" && (imageContainer.childNodes[i].tagName.toLowerCase() == 'canvas' ||  imageContainer.childNodes[i].tagName.toLowerCase() == 'img'))  )
			{
				imageElement = 	imageContainer.childNodes[i];
			}
		}
		return imageElement;
	}
	
	
function processImage(formId, operator)
{
			$("#loading")
			   .ajaxStart(function(){
				   $(this).show();
			   })
			   .ajaxComplete(function(){
				   $(this).hide();
			   });	
			var options = 
			{ 
				dataType: 'json',
				error: function (data, status, e) 
				{
					alert(e);
				},				
				success: function(data) 
				{ 
	if(typeof(data.error) == 'undefined')
	{
		alert('Unexpected information ');
	}
	else if(data.error != '')
	{

		alert(data.error);
	}else
	{
		$("#loading").show();

			numSessionHistory = parseInt(data.history);
	var preImage = new Image();
				preImage.width = data.width;
				preImage.height = data.height;													
	preImage.onload = function()
	{				
									          
														$('#hiddenImage').empty();														
														$(preImage).appendTo('#hiddenImage');
														
														changeMode(false, true);		
/*														switch(operator)
														{
															case '+':
																numSessionHistory++;
																break;
															case '-':
																numSessionHistory--;
																break;
														}*/
														$('#loading').hide(); 
														//alert(numSessionHistory);
														//return true;
														
									             
									        };
						var now = new Date();
			preImage.src = data.path + "?" + now.getTime();



			}
				} 
			}; 
			$('#' + formId).ajaxSubmit(options); 		
			return false;
}

function editorClose()
{
	if(window.confirm(warningEditorClose))
	{
		self.close();
	}
	return false;
}

function selectFullImage()
{
	var imageToResize = getImageElement();
        var imageWidth = $(imageToResize).attr('width');
        var imageHeight = $(imageToResize).attr('height');
        
        var overlay = $('#resizeMe');
	overlay.css('width', imageWidth + 'px');
        overlay.css('height', imageHeight + 'px');
        overlay.css('top', '20px');
        overlay.css('left', '20px');
        overlay.css('background-position', '0px');
        
        var widthField = $('#width');
	var heightField = $('#height');
	var topField = $('#y');
	var leftField = $('#x');
	widthField.val(imageWidth);
	heightField.val(imageHeight);
	topField.val(0);
        leftField.val(0);
}