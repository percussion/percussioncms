/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($) {

    $.fn.imageAssetControl = function(settings) {

        var config = {
            maxDisplayHeight: 400,
            maxDisplayWidth: 600,
            filenameFields: [
                "sys_title",
                "filename"
            ],
            debug: false
        };


        if (settings) $.extend(config, settings);

        var MAIN_IMAGE = 0;
        var THUMB_IMAGE = 1;
        var currentImagePage = MAIN_IMAGE;  // it is either MAIN_IMAGE or THUMB_IMAGE
        var imagePages = [];  // initialized by _initializeForm(),  length = 2; it is either MAIN_IMAGE or THUMB_IMAGE

        var utils = $.perc_utils;
        var uploadedImage;
        var form = $(this);
        var steps = $(this).find(".step");
        var imageUpload = $(this).find("#perc-upload_form");
        var $thumbPrefix = $("#perc-image-thumbprefix");

        var imageFormOptions = {
            //target:    '#response',  // target element(s) to be updated with server response
            success: imageUploadResponse,
            beforeSubmit: function(a,f,o) {
                imageUpload.find('input[type="file"]').removeAttr("disabled");
                $('#image_asset_upload_message').html('Uploading...');
            },
            iframe: true,
            contentType: 'multipart/form-data',
            // other available options:
            url:      '/Rhythmyx/user/apps/imageWidget/upload',        // override for form's 'action' attribute
            type:      'post',        // 'get' or 'post', override for form's 'method' attribute
            dataType:   'json',       // 'xml', 'script', or 'json' (expected server response type)
            resetForm: false        // reset the form after successful submit

            // $.ajax options can be used here too, for example:
            //timeout:   3000
        };

        function imageUploadResponse(data, statusText) {

            // alert('status: ' + statusText + '\n\ndata: \n' + data +
            //  '\n\nThe output div should have already been updated with the responseText.');
            $('#image_asset_upload_message').html('');
            if (config.debug) {
                var $out = $('#uploadOutput');
                $out.html('Form success handler received: <strong>' + typeof data + '</strong>');
                var newData;
                if (typeof data == 'object' && data.nodeType) {
                     newData = _elementToString(data.documentElement, true);
                } else if (typeof data == 'object')  {
                     newData = _objToString(data);
                }
                $out.append('<div><pre>'+ newData +'</pre></div>');
            }
            var image = eval(data[0]);

            // if uploaded invalid or unsupported image ?
            if (image === undefined || image.error !== undefined) {
                var errorMsg = "Failed to upload the image.";
                if (image !== undefined && image.error !== undefined && image.error != null)
                    errorMsg = image.error;
                $('#perc-upload-error-message').show().html(errorMsg);

                $.perc_utils.debug(errorMsg);

                return;
            }

            // succesfull uploaded the image file
            uploadedImage = image;
            $.perc_utils.debug("Uploaded image mimeType: " + image.mimeType);

            for (var i=0; i<config.filenameFields.length;i++) {
                form.find('input[name='+config.filenameFields[i]+']').val(image.filename);
            }

            form.find(".image_asset_orig_height").html(uploadedImage.height);
            form.find(".image_asset_orig_width").html(uploadedImage.width);
            resetImages(image);

            // enable step 2
            imageUpload.hide();
            _showStepImage(2);

            _enableClickStep1(true);
            _enableClickStep2(false);
            _enableClickStep3(true);
        }

        function imageRequest(imageKey,imageInfo) {
            $.get('/Rhythmyx/user/apps/imageWidget/requestImage.do', {
                    imageKey : imageKey
                },
                function ( data, textStatus) {
                    imageInfo.image = data;
                    if (typeof(uploadedImage) == "undefined"  ) {
                        uploadedImage = imageInfo.image;
                        form.find(".image_asset_orig_height").html(uploadedImage.height);
                        form.find(".image_asset_orig_width").html(uploadedImage.width);
                        // setStep(imageInfo.image.page);
                    }
                    setImageIdOnForm(imageInfo);
                    updateImageForDisplay(imageInfo);
                });
        }


        function resetImages(image) {
            //  Create main and thumbnail images
            for (i=0;i<imagePages.length;i++) {

                imagePages[i].image = image;

                setDefaultSizes(imagePages[i]);

                imagePages[i].dirty = true;
                setImageIdOnForm(imagePages[i]);

            }
            updateImageForDisplay(imagePages[MAIN_IMAGE]);
            displayImagePage(MAIN_IMAGE);

        }

        function setDefaultSizes(imageInfo) {
            var defaultWidth = imageInfo.page.find(".image_asset_default_width").val();
            var resize=false;

            if ( defaultWidth !== 'undefined' && defaultWidth > 0) {
                if (imageInfo.image.thumbWidth) {
                    defaultWidth = imageInfo.image.thumbWidth;
                }
                imageInfo.page.find(".image_asset_width").val(defaultWidth);
                calculateHeight(imageInfo);
                resizePage(imageInfo);
            } else {
                updateImageForDisplay(imageInfo);
            }
        }

        function calculateHeight(item) {
            var height = item.page.find('.image_asset_height');
            var width = item.page.find('.image_asset_width');
            height.val( Math.round(( uploadedImage.height / uploadedImage.width ) *  width.val()));
        }

        function calculateWidth(item) {
            var height = item.page.find('.image_asset_width');
            var width = item.page.find('.image_asset_height');
            width.val( Math.round(( uploadedImage.width / uploadedImage.height ) *  width.val()));
        }

        /**
         * Display the MAIN or THUMB image page/panel and hide the other 2 panels/pages (or steps)..
         *
         * @param imageIndex the index for imagePages[]. It is either MAIN_IMAGE or THUMB_IMAGE.
         */
        function displayImagePage(imageIndex) {

            imageUpload.hide();
            var hidePage = imageIndex == MAIN_IMAGE ? THUMB_IMAGE : MAIN_IMAGE;

            imagePages[hidePage].page.hide();
            imagePages[imageIndex].page.fadeIn();

            if ( imageIndex ==  THUMB_IMAGE) {
                if(imagePages[THUMB_IMAGE].image == null) {
                    imagePages[THUMB_IMAGE].image = imagePages[MAIN_IMAGE].image;
                    setDefaultSizes(imagePages[THUMB_IMAGE]);
                    setImageIdOnForm(imagePages[THUMB_IMAGE]);
                } else {
                    updateImageForDisplay(imagePages[THUMB_IMAGE]);
                }
            }
            currentImagePage = imageIndex;
        }


        /**
         * Display the "Upload an Image" step
         */
        function displayUploadStep() {
            steps.hide();
            imageUpload.fadeIn();
            $('#perc-upload-error-message').hide();
            currentImagePage = MAIN_IMAGE;
        }

        function updateImageForDisplay(imageInfo) {

            var scale = imageInfo.page.find(".image_asset_scale");
            if (imageInfo.image === null) {
                alert("Cannot find image");
                displayUploadStep();
                return;
            }
            imageInfo.page.find(".image_asset_id").val(imageInfo.image.imageKey);
            if ( imageInfo.image.width <= config.maxDisplayWidth && imageInfo.image.height <=config.maxDisplayHeight) {

                scale.html("");
                imageInfo.displayImage = imageInfo.image;
                renderMainImage(imageInfo);
            } else {
                //alert ('image too large resizing');
                var height=imageInfo.image.height;
                var width=imageInfo.image.width;
                var displayScale = 100;
                var widthScale = Math.round((config.maxDisplayHeight / imageInfo.image.height ) * 100);
                var heightScale = Math.round((config.maxDisplayWidth / imageInfo.image.width ) * 100);
                if (heightScale < widthScale ) {
                    width = config.maxDisplayWidth;
                    displayScale = heightScale;

                    height = Math.round(( imageInfo.image.height / imageInfo.image.width) * config.maxDisplayWidth);

                } else {
                    height = config.maxDisplayHeight;
                    displayScale = widthScale;
                    width = Math.round(( imageInfo.image.width / imageInfo.image.height) * config.maxDisplayHeight);
                }
                scale.html("Displayed scale is "+displayScale+"%");

                var request = {
                    "imageKey": uploadedImage.imageKey,
                    "height": height,
                    "width": width
                };
                var result = $.post("/Rhythmyx/user/apps/imageWidget/resizeImage.do",
                    request,
                    function(data) {

                        var newData = _objToString(data);
                        //alert ( "resize result is  "+newData );
                        imageInfo.displayImage = eval(data);
                        renderMainImage(imageInfo);
                    },
                    "json"
                );
            }

        }
        function renderMainImage(imageInfo) {
            //alert("rendering image");
            imageInfo.page.find(".image_asset_width").val(imageInfo.image.width);
            imageInfo.page.find(".image_asset_height").val(imageInfo.image.height);
            renderImage(imageInfo.page.find(".image_asset_image"),imageInfo.displayImage);
            //alert("rendered display image");
        }

        function renderImage(selector,j) {
            $(selector).html('<img src="/Rhythmyx/user/apps/imageWidget/image/img'+j.imageKey +'.'+j.ext+'"'+' height="'+j.height+'" width="'+j.width+'"  />');
        }

        function resize() {
            for (i=0;i<imagePages.length;i++) {
                //if (imagePages[i].page.is(":visible"))
                if (imagePages[i].image !== null){
                    resizePage(imagePages[i]);

                }
            }
        }

        // Retrieve and upload the specified image with the modified height and width.
        // Do nothing if height or width has not been modified.
        //
        // @param imageInfo - the info contains info to find image height and width.
        function resizePage(imageInfo) {
            var newWidth = imageInfo.page.find(".image_asset_width").val();
            var newHeight = imageInfo.page.find(".image_asset_height").val();


            if (imageInfo.image.height != newHeight || imageInfo.image.width!=newWidth)  {
                imageInfo.dirty=true;
                var request = {
                    "imageKey": uploadedImage.imageKey,
                    "height": newHeight,
                    "width": newWidth
                };

                var result = $.post("/Rhythmyx/user/apps/imageWidget/resizeImage.do",
                    request,
                    function(data) {
                        var newData = _objToString(data);
                        //alert ( "resize result is  "+newData );
                        imageInfo.image = eval(data);
                        setImageIdOnForm(imageInfo);
                        updateImageForDisplay(imageInfo);
                    },
                    "json"
                );
            }
        }


        function rotate(direction) {
            rotateImages(direction);
            form.find(".image_asset_orig_height").html(uploadedImage.width);
            form.find(".image_asset_orig_width").html(uploadedImage.height);
        }


        function rotateImage(imageInfo,direction) {
            var request = {
                "imageKey": imageInfo.image.imageKey,
                "rotate" : direction
            };

            var result = $.post("/Rhythmyx/user/apps/imageWidget/resizeImage.do",
                request,
                function(data) {

                    var newData = _objToString(data);
                    var result = eval(data);

                    imageInfo.image=result;
                    imageInfo.rotation+=direction;
                    if (imageInfo.rotation===4) {imageInfo.rotation=0;}
                    if (imageInfo.rotation===-1){imageInfo.rotation=3;}
                    updateImageForDisplay(imageInfo);
                    imageInfo.dirty=true;
                    setImageIdOnForm(imageInfo);
                },
                "json"
            );
        }

        function rotateImages(direction) {
            var request = {
                "imageKey": uploadedImage.imageKey,
                "rotate" : direction
            };


            var result = $.post("/Rhythmyx/user/apps/imageWidget/resizeImage.do",
                request,
                function(data) {

                    var newData = _objToString(data);
                    //alert ( "resize result is  "+newData );
                    var result = eval(data);
                    uploadedImage=result;

                    for (i=0;i<imagePages.length;i++) {
                        //if (imagePages[i].page.is(":visible"))
                        if(imagePages[i].image !== null ) {
                            rotateImage(imagePages[i],direction);
                        }
                    }

                },
                "json"
            );
        }

        function setImageIdOnForm(imageInfo) {
            var imageKey="";
            if (imageInfo.image !== null && imageInfo.image !== undefined && imageInfo.image.imageKey!==undefined) {
                imageKey = imageInfo.image.imageKey;
            }

            var id_fieldname = imageInfo.page.find('.image_asset_name').val();

            if (id_fieldname.length > 0 ) {

                var idField = form.find('input[name='+id_fieldname+'_id]');
                idField.val(imageKey);
                var dirtyField = form.find('input[name='+id_fieldname+'_dirty]');
                if (imageInfo.dirty && dirtyField.length==0 ) {
                    idField.after('<input type="hidden" name="'+id_fieldname +'_dirty" value="true" />');
                }

            } else {
                imageInfo.page.find('.image_asset_id').val(imageKey);
            }
        }
        function getImageInfo(element) {
            var  step = $(this).closest(".image_asset_step");
            for (i=0;i<imagePages.length;i++) {
                //if (imagePages[i].page.is(":visible"))
                if(imagePages[i].page == step) {
                    //alert("Found imageInfo");
                    return imagePages[i];
                }
            }
        }
        //Callbacks

        $('.image_asset_rotate_left').on("click",function()  {
            rotate(-1);
        });
        $('.image_asset_rotate_right').on("click", function()  {
            rotate(1);
        });

        /**
         * The handler invoked when selected a different image file in the "Upload" panel
         * The "Upload" INPUT is a pseudo 'img' field, but it cannot name as 'img' on the field;
         * otherwise it will cause some unintended side effect when uploaded invalid an image file.
         */
        function _selectImageHandler(event)
        {
            form.ajaxSubmit(imageFormOptions);
        }

        var validator = function(pathItem) {
            return pathItem && pathItem.type === "Folder" ? null : "Please select a folder.";
        };

        var updateFormActionUrl = function(data) {
            updateFolderPathInfo(data);

            var index = data.id.lastIndexOf('-') + 1;
            var newFolderId = data.id.substring(index);
            var queryUrl = window.location.pathname;
            queryUrl += '?sys_folderid=' + newFolderId + '&sys_asset_folderid=' + newFolderId;

            form.attr('action', queryUrl);
        };

        function _checkUserPermission(data) {
            $.topFrameJQuery.PercFolderHelper().getAccessLevelByPath(
                data.folderPath.substring(data.folderPath.indexOf('/Assets')),
                true,
                function( status, result ) {
                    var error = status === $.topFrameJQuery.PercFolderHelper().PERMISSION_ERROR,
                        onlyWrite = result === $.topFrameJQuery.PercFolderHelper().PERMISSION_READ;
                    if ( error || onlyWrite ) {
                        $.topFrameJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.newassetdialog.title@New Asset"), content: I18N.message("perc.ui.page.path.selection.dialog@Not Authorized to Create")});
                        return;
                    }
                    else {
                        _checkUserWorkflowPermission(data);
                    }
                }
            );
        }

        function _checkUserWorkflowPermission(data) {
            $.topFrameJQuery.PercUserService.getAccessLevel(
                null,
                -1,
                function( status, result) {
                    var error = status === $.PercServiceUtils.STATUS_ERROR,
                        accessRead = result === $.topFrameJQuery.PercUserService.ACCESS_READ,
                        accessNone = result === $.topFrameJQuery.PercUserService.ACCESS_NONE;
                    if (  error || accessRead || accessNone ) {
                        $.topFrameJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.newassetdialog.title@New Asset"), content: I18N.message("perc.ui.page.path.selection.dialog@Not Authorized to Create")});
                    }
                    else {
                        updateFormActionUrl(data);
                    }
                },
                data.folderPath.substring(data.folderPath.indexOf('/Assets'))
            );
        }

        function _displayPathSelection(event) {
            var pathSelectionOptions = {
                okCallback: _checkUserPermission,
                dialogTitle: "Select Destination Folder",
                rootPath:$.topFrameJQuery.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                initialPath: $.topFrameJQuery.cookie("perc-inlineimage-path"),
                selectedItemValidator:validator,
                acceptableTypes:"percImageAsset,site,Folder"
            };

            $.topFrameJQuery.PercPathSelectionDialog.open(pathSelectionOptions);
        }

        $("#perc-select-image").on("change",function(evt){
            _selectImageHandler(evt);
        });

        $(".perc-select-folderpath").on("click", function(evt){
            _displayPathSelection(evt);
        });

        $('.image_asset_constrain').on("click",function()  {

            var item = imagePages[currentImagePage];
            var height = item.page.find('.image_asset_height');
            var width = item.page.find('.image_asset_width');
            var prevHeight = height.val();
            var prevWidth = width.val();
            if(uploadedImage.height <= uploadedImage.width) {
                height.val( Math.round(( uploadedImage.height / uploadedImage.width ) *  width.val()));
            } else {
                width.val( Math.round(( uploadedImage.width / uploadedImage.height ) *  height.val()));
            }
            var currHeight = height.val();
            var currWidth = width.val();
            // resize the image if height or width is automatically modified by the constrain
            if (prevHeight !== currHeight || prevWidth !== currWidth) {
                resize();
            }
        });

        $(this).ajaxError(function(event, request, settings){
            displayUploadStep();
        });

        /**
         * Determines if the current key (contained in the specified key-event) is acceptable.
         * The accept key must be a digit.
         *
         * @param evt - the key event, which contains current key, assumed not NULL.
         * @param maxValue - maximum value of the current input field.
         *
         * @return  TRUE if the key is "Backspace", "arrow" or number, and the final value is less
         *                      than the specified maximum value; otherwise return FALSE.
         */
        function _acceptKey(evt, maxValue)
        {
            var theChar = String.fromCharCode(evt.which);

            // utils.debug("theChar = " + theChar);

            // evt.whcih == 8   for backspace
            // evt.which == 0   for all (left, right, up & down) keys
            if (isNaN(theChar) && evt.which!=8 && evt.which!=0){
                return false;
            }

            var field = evt.target;
            var value = field.value;

            // re-calculate the input value if key is a number, not backspace or arrow key
            if (evt.which !== 8 && evt.which !== 0) {
                var start = $(field).caret().start;
                var end = $(field).caret().end;
                var t1 = field.value.slice(0, start);
                var t2 = field.value.slice(end);
                value = t1 + theChar + t2;
            }

            // don't check against max value for now.
            // if (value > maxValue) {
            //	return false;
            //}

            return true;
        }

        // handle when height input lose focus - always retrieve & display image if there is any height/width changes
        $('.image_asset_height').on("blur",function() {
            resize();
        })
        .on("keypress",function(evt) {
            if (!_acceptKey(evt, uploadedImage.height)) {
                return false;
            }
        })
        .on("keyup",function()  {
            var step = $(this).closest(".image_asset_step");
            var constrain = step.find(".image_asset_constrain:checked").length;

            if(constrain > 0) {
                var newWidth = Math.round(( uploadedImage.width / uploadedImage.height ) *  $(this).val());
                step.find(".image_asset_width").val( newWidth );
            }
        });


        // handle when width input lose focus - always retrieve & display image if there is any height/width changes
        $('.image_asset_width').on("blur",function() {
            resize();
        })
        .on("keypress",function(evt) {

            if (!_acceptKey(evt, uploadedImage.width)) {
                return false;
            }
        })
            .on("keyup",function()  {
            var step = $(this).closest(".image_asset_step");
            var constrain = step.find(".image_asset_constrain:checked").length;
            if(constrain > 0 ) {
                var newHeight = Math.round(( uploadedImage.height / uploadedImage.width ) *  $(this).val());
                step.find(".image_asset_height").val(newHeight);
            }
        });


        // helper method
        function _elementToString(n, useRefs) {
            var attr = "", nest = "", a = n.attributes;
            for (var i=0; a && i < a.length; i++) {
                attr += ' ' + a[i].nodeName + '="' + a[i].nodeValue + '"';
            }

            if (n.hasChildNodes === false) {
                return "<" + n.nodeName + "\/>";
            }

            for (let i=0; i < n.childNodes.length; i++) {
                var c = n.childNodes.item(i);
                if (c.nodeType === 1)       nest += _elementToString(c);
                else if (c.nodeType === 2)  attr += " " + c.nodeName + "=\"" + c.nodeValue + "\" ";
                else if (c.nodeType === 3)  nest += c.nodeValue;
            }
            var s = "<" + n.nodeName + attr + ">" + nest + "<\/" + n.nodeName + ">";
            return useRefs ? s.replace(/</g,'&lt;').replace(/>/g,'&gt;') : s;
        }

        // helper method to convert object to string
        function _objToString(o) {
            var s = '{\n';
            for (var p in o) {
                if (typeof o[p] == 'object')
                    s+=_objToString(o[p]);
                else
                    s += '    ' + p + ': ' + o[p] + '\n';
            }
            return s + '}';
        }

        /**
         * Display the highlighted or dimmed image of step 1 (upload an image) according to the specified parameter.
         *
         * @param isHighLight  -  if it is true, then display the high lighted image of step 1;
         *                                           otherwise display the dim image of step 1.
         */
        function _showStep1Image(isHighLight)
        {
            if (isHighLight)
                $("#perc-image-upload").css("background-position", "0px -38px");
            else
                $("#perc-image-upload").css("background-position", "0px 0px");
        }

        /**
         * Display the highlighted or dimmed image of step 2 (size the image) according to the specified parameter.
         *
         * @param isHighLight  -  if it is true, then display the high lighted image of step 2;
         *                                           otherwise display the dim image of step 2.
         */
        function _showStep2Image(isHighLight)
        {
            if (isHighLight)
                $("#perc-image-resize").css("background-position", "0px -190px");
            else
                $("#perc-image-resize").css("background-position", "0px -152px");
        }


        /**
         * Display the highlighted or dimmed image of step 3 (create a thumbnail) according to the specified parameter.
         *
         * @param isHighLight  -  if it is true, then display the high lighted image of step 3;
         *                                           otherwise display the dim image of step 3.
         */
        function _showStep3Image(isHighLight)
        {
            if (isHighLight)
                $("#perc-image-thumbnail").css("background-position", "0px -114px");
            else
                $("#perc-image-thumbnail").css("background-position", "0px -76px");
        }

        /**
         * The callback function when step "(1)" or "Upload an Image" is clicked.
         */
        function _step1ClickHandler(event)
        {
            displayUploadStep();
            _showStepImage(1);
            _enableClickStep1(false);
            _enableClickStep2(true);
            _enableClickStep3(true);
        }

        /**
         * Enable or disable to click on the step "(1)" (or Upload an Image)
         */
        function _enableClickStep1(enable)
        {
            if (enable)
                $("#perc-image-upload").on("click",function(evt){
                    _step1ClickHandler(evt);
                } ).css("cursor", "pointer");
            else
                $("#perc-image-upload").off('click').css("cursor", "default");
        }

        /**
         * The callback function when step "(2)" or "Size the Image" is clicked.
         */
        function _step2ClickHandler(event)
        {
            displayImagePage(MAIN_IMAGE);
            _showStepImage(2);
            _enableClickStep1(true);
            _enableClickStep2(false);
            _enableClickStep3(true);
        }

        /**
         * Enable or disable to click on the step "(2)" (or Size the Image)
         */
        function _enableClickStep2(enable)
        {
            if (enable)
                $("#perc-image-resize").off('click').on('click',
                    function(evt){
                                _step2ClickHandler(evt);
                            }).css("cursor", "pointer");
            else
                $("#perc-image-resize").off('click').css("cursor", "default");
        }

        /**
         * The callback function when step "(3)" or "Create a Thumbnail" is clicked.
         */
        function _step3ClickHandler(event)
        {
            displayImagePage(THUMB_IMAGE);
            _showStepImage(3);
            _enableClickStep1(true);
            _enableClickStep2(true);
            _enableClickStep3(false);
        }

        /**
         * Enable or disable to click on the step "(3)" (or Create a Thumbnail)
         */
        function _enableClickStep3(enable)
        {
            if (enable)
                $("#perc-image-thumbnail").off('click').on("click",
                    function(evt){
                        _step3ClickHandler(evt);
                    }).css("cursor", "pointer");
            else
                $("#perc-image-thumbnail").off('click').css("cursor", "default");
        }

        /**
         * Highlight the specified step image and dim the rest of the step images.
         * @param step the high lighted step. It must be 1, 2 or 3.
         */
        function _showStepImage(step)
        {
            switch (step) {
                case 1:
                    _showStep1Image(true);
                    _showStep2Image(false);
                    _showStep3Image(false);
                    break;
                case 2:
                    _showStep1Image(false);
                    _showStep2Image(true);
                    _showStep3Image(false);
                    break;
                case 3:
                    _showStep1Image(false);
                    _showStep2Image(false);
                    _showStep3Image(true);
                    break;
            }
        }

        /**
         * The event handler when the "thumbnail prefix" entry is out of focus.
         * This is used to update the current value to the "hidden" field "thumbprefix"
         * and "thumbprefix" is a required field. It retrieves the original value
         * from "thumbprefix" field if current field is empty.
         *
         * @param event the blur event of the handler.
         */
        function _updateThumbprefix(event)
        {
            if ($thumbPrefix.val() === "")
            {
                _initThumbprefix();
                return;
            }
            $("input[name=thumbprefix]").val($thumbPrefix.val());
        }

        $('#perc-image-thumbprefix').on("blur",
            function(e){
                _updateThumbprefix(e);
            });

        /**
         * Initialize the editable "thumbnail" input field, populate the field
         * from the hidden "thumbnail" field.
         */
        function _initThumbprefix()
        {
            var hiddenPrefix = $("input[name=thumbprefix]");
            if (hiddenPrefix.val() === "") {
                alert('"thumbprefix" field cannot be empty.');
            }
            $thumbPrefix.val(hiddenPrefix.val());
        }

        /**
         * If the required "displaytitle" field is blank, then set its value with the uploaded
         * image file name (if it is not blank). This is called by pre-submit mechanizm, where
         * we register this function as the pre-submit handler, so it is called before submit
         * the form to server.
         *
         * Note, pre-populating the "displaytitle" field is only needed when creating an image asset,
         * it is not needed when updating the image asset.
         */
        function _updateDisplayTitleIfNeeded()
        {
            $.perc_utils.debug('updateDisplayTitleIfNeeded() ...');

            var displayTitle = $('#perc-content-edit-displaytitle').val();
            if (displayTitle == null || displayTitle ==='')
            {
                var filename = $('#perc-select-image').val();
                $.perc_utils.debug('filename = ' + filename);
				if(filename.match(/fakepath/)) {
                        // update the file-path text using case-insensitive regex
                        filename = filename.replace(/C:\\fakepath\\/i, '');
                 }
                 $.perc_utils.debug('filename = ' + filename);
                if (filename !== undefined && filename !== null && filename !=='')
                    $('#perc-content-edit-displaytitle').val(filename);
            }
            return true;
        }

        /**
         * This is called when the image editor form is loaded.
         */
        function _initializeForm () {

            $.perc_utils.debug('_initializeForm() ...');

            steps.hide();
            var foundExisting = false;
            // initializing imagePage[2]
            $(".image_asset_step").each(function() {
                var imageInfo = ({
                    page: $(this),
                    image: null,
                    displayImage : null,
                    rotation : 0,
                    dirty : false
                });
                imagePages[imagePages.length] = imageInfo;
                var id_fieldname = $(this).find('.image_asset_name').val();
                var imageKey="";
                if (id_fieldname.length > 0 ) {
                    imageKey = form.find('input[name='+id_fieldname+'_id]').val();
                } else {
                    imageKey = $(this).find('.image_asset_id').val();
                }

                if ( imageKey!== undefined && imageKey.length > 0 ) {
                    foundExisting = true;
                    imageRequest(imageKey,imageInfo);
                }
            });
            if (foundExisting) {
                imagePages[MAIN_IMAGE].page.fadeIn();
                _showStepImage(2);
                _enableClickStep1(true);
                _enableClickStep3(true);
            } else {
                imageUpload.fadeIn();
                _showStepImage(1);
            }

            $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(_updateDisplayTitleIfNeeded);

            _initThumbprefix();
        }

        _initializeForm();
        return $(this);

    };

})(jQuery);

