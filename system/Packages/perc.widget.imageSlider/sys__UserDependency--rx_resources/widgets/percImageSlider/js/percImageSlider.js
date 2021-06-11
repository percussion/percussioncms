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

(function($)
{
    var sliderWarningCount = 0;
    var sliderErrorCount = 0;

    // initializes array that will be used to monitor each image for changes
    previousThumbnailPath = [];

    $(document).ready(function(){

        if($(".perc-image-slider").length > 0)
        {
            $.percImageSliderSetup().renderEditor();
        }
        else if ($(".perc-image-slider-readonly").length > 0)
        {
            $.percImageSliderSetup().renderReadOnly();
        }

        setInterval( updateThumbnail, 500);
        function updateThumbnail() {
            $('.perc-slider-image-path').each( function (i) {

                // If the input has a class of perc-input-slider-error, we know that there is a broken link
                // and we can apply the broken image icons and skip to the next image
                if( $(this).hasClass('perc-input-slider-error') ) {
                    $(this).closest('tr').children("#perc-content-image-size").html('<p class="perc-image-size-text"><i class="fa fa-chain-broken" aria-hidden="true"></i></p>');
                    $(this).closest('tr').children("#perc-content-image-thumbnail").html('<p class="perc-image-size-text"><i class="fa fa-chain-broken" aria-hidden="true"></i></p>');
                    return true;
                }

                thumbnailPath = $(this).attr('title');

                // When an image is loaded into the DOM, the original resolution is overwritten
                // by the image size instantiated into the DOM.  As a result, to get the original
                // image size, we need to create a new image object off-screen and retrieve the size
                // from there.

                // This next section calculates the the native resolution of the selected image
                // Get on screen image thumbnail
                var screenImage = $(this).closest('tr').children("#perc-content-image-thumbnail");
                // Create new offscreen image load and retrieve resolution
                var offscreenImage = new Image();

                //only load image if it has a defined source URL
                if( screenImage.find('img').attr("src") !== undefined ) {
                    offscreenImage.src = screenImage.find('img').attr("src");
                }

                // Get accurate measurements from offsecreen loaded image, we can't get this from the onscreen loaded image
                var imageWidth = offscreenImage.width;
                var imageHeight = offscreenImage.height;

                // updates resolution column or loads appropriate icons for error handling
                if( (imageWidth !== undefined && imageHeight !== undefined) && imageWidth !== 0) {
                    $(this).closest('tr').children("#perc-content-image-size").html('<p class="perc-image-size-text">' + imageHeight + 'x' + imageWidth +'</p>');
                }
                else if ( imageWidth === 0 || imageHeight === 0 ) {
                    $(this).closest('tr').children("#perc-content-image-size").html('<p class="perc-image-size-text"><i class="fa fa-question-circle" aria-hidden="true"></i></p>');
                    $(this).closest('tr').children("#perc-content-image-thumbnail").html('<p class="perc-image-size-text"><i class="fa fa-question-circle" aria-hidden="true"></i></p>');
                }
                else {
                    $(this).closest('tr').children("#perc-content-image-size").html('<p class="perc-image-size-text"><i class="fa fa-exclamation-circle" aria-hidden="true"></i></p>');
                }

                if( thumbnailPath !== null && thumbnailPath !== undefined ) {
                    $(this).closest('tr').children("#perc-content-image-thumbnail").html('<img id="perc-thumbnail-preview" src="' + thumbnailPath + '" height="33" />');
                    if( previousThumbnailPath[i] != thumbnailPath && previousThumbnailPath[i] !== undefined ) {
                        $(this).closest('tr').children("#perc-content-image-size").html('<i class="fa fa-spinner fa-pulse fa-fw"></i><span class="sr-only"></span>');
                    }
                    previousThumbnailPath[i] = thumbnailPath;
                }
                else {
                    previousThumbnailPath[i] = '';
                }

            });

        }

        // When the image or page browse button are clicked we clear the perc managed link ids
        $(".perc-image-field-select-button").on("click",function(){
            var $el = $(this);
            var imgInputName = $el.attr("for");
            $('#' + imgInputName).attr('data-perc-image-path-link-id','');
        });

        $(".perc-page-field-select-button").on("click",function(){
            var $el = $(this);
            var pageInputName = $el.attr("for");
            $('#' + pageInputName).attr('data-perc-page-path-link-id','');
        });

        // Listen for a change event on either the image or external link fields
        // These change events are triggered by PercImageSelectionControl.js and PercPageSelectionControl.js
        $(".perc-slider-image-path, .perc-slider-image-link, .perc-slider-image-link-setting-select").on('change', function(event) {
            validateAssetItems();
        });

    }); // end document ready function


    $.percImageSliderSetup = function()
    {
        var percImageSliderSetupApi = {
            renderEditor : renderEditor,
            renderReadOnly : renderReadOnly
        };

        var self = this;

        // store the table and base IDs for both the image path and link because they need to be updated
        // with incremental values to allow for each browse button to be bound appropriately
        var $table = $('#perc-image-slider-setup-editor');
        var sliderImagePathBaseId = $table.find('tr.hide td input.perc-slider-image-path').attr('id');
        var sliderImageLinkBaseId = $table.find('tr.hide td input.perc-slider-image-link').attr('id');
        imageCount = 0;

        /**
         * Respsonsible for rendering a read-only view of the control.
         */
        function renderReadOnly()
        {
        }

        /***
         * Responsible for rendering the Edit view of the control.
         */
        function renderEditor()
        {
            //Hide help to start
            $('.perc-help').hide();

            //Load data from the data field - this is done in two steps
            //an input field gets the data and the name of the input field is passed to the id of a div that contains
            //our editor class - we get the id from the div and pull the data from the input field.
            var dataFieldName = $(".perc-image-slider").attr("id");
            var dataStr = $("input[name='" + dataFieldName + "']").val();

            if(dataStr.indexOf('config')!==-1){
                dataStr = dataStr.replace('config','percJSONConfig')
            }

            if(dataStr.indexOf('imagePath')!==-1){
                dataStr = dataStr.replace(/imagePath/g,'percImagePath');
            }

            $("input[name='" + dataFieldName + "']").val(dataStr);

            var data = null;
            dataStr = dataStr.trim();
            if(dataStr.length>0){
                data = JSON.parse(dataStr);
            }

            // Populates the rows from the previously stored values
            if(data!==null){
                for(var i =0 ; i < data.percJSONConfig.length ; i++){
                    var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                    $clone.find('.perc-slider-image-path').val(data.percJSONConfig[i].percImagePath);
                    $clone.find('.perc-slider-image-caption').val(data.percJSONConfig[i].imageCaption);
                    $clone.find('.perc-slider-image-caption-2').val(data.percJSONConfig[i].imageCaption2);
                    $clone.find('.perc-slider-image-link').val(data.percJSONConfig[i].percPagePath);
                    $clone.find('.perc-slider-image-external-link').val(data.percJSONConfig[i].externalLink);
                    $clone.find('#perc-slider-image-link-setting').val(data.percJSONConfig[i].linkSetting);
                    $clone.find('.perc-slider-image-path').attr('data-perc-image-content-id', data.percJSONConfig[i].percImageContentId);
                    $clone.find('.perc-slider-image-link').attr('data-perc-page-content-id', data.percJSONConfig[i].percPageContentId);
                    $clone.find('.perc-slider-image-path').attr('data-perc-image-path-link-id', data.percJSONConfig[i].percImagePathLinkId);
                    $clone.find('.perc-slider-image-link').attr('data-perc-page-path-link-id', data.percJSONConfig[i].percPagePathLinkId);
                    $table.find('table').append($clone);

                    // failsafe logic for ensuring that there is always an image path
                    if( data.percJSONConfig[i].percImagePath !== null && data.percJSONConfig[i].percImagePath != 'undefined') {
                        loadThumbnails ($clone, data.percJSONConfig[i].percImagePath, i);
                    }

                    // set incremented image path ids
                    $('.perc-slider-image-path').last().attr({
                        id: sliderImagePathBaseId + imageCount,
                        name: 'imageInputField' + imageCount
                    });
                    $('.perc-image-field-select-button').last().attr({
                        for: sliderImagePathBaseId + imageCount
                    });

                    // set incremented image link ids
                    $('.perc-slider-image-link').last().attr({
                        id: sliderImageLinkBaseId + imageCount,
                        name: 'imageLinkField' + imageCount
                    });
                    $('.perc-page-field-select-button').last().attr({
                        for: sliderImageLinkBaseId + imageCount
                    });

                    imageCount++;
                }
            }
            _attachEvents();

            validateAssetItems();

        }


        // this function is similar to the one above that checks for new title tags every second,
        // but the title tags are not populated on initial load, only when the images are changed,
        // so this function loads the thumbnails when the content editor is first loaded
        function loadThumbnails (clonedTable, percImagePath, i) {
            clonedTable.find("#perc-content-image-thumbnail").html('<i class="fa fa-spinner fa-pulse fa-fw"></i><span class="sr-only"></span>');
            clonedTable.find("#perc-content-image-thumbnail").html('<img id="perc-thumbnail-preview" src="' + percImagePath + '" height="33" />');
            previousThumbnailPath[i] = percImagePath;
        }

        /**
         * Helper method that is responsible for attaching the click events and auto fill functionality.
         * Also attaches the presubmit handler to _preSubmitHandler.
         */

        function _attachEvents()
        {
            var $table = $('#perc-image-slider-setup-editor');

            //Add the pre-submit handler to prepare data for saving.
            window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(_preSubmitHandler);

            //Widget behavioral events
            $('.perc-table-remove').on("click",function () {
                $(this).parents('tr').detach();

                // check validation again so we can remove any warnings if necessary
                validateAssetItems();
            });

            $('.perc-table-add').on("click", function () {
                var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                $table.find('table').append($clone);

                // set incremented image path ids
                $('.perc-slider-image-path').last().attr({
                    id: sliderImagePathBaseId + imageCount,
                    name: 'imageInputField' + imageCount
                });
                $('.perc-image-field-select-button').last().attr({
                    for: sliderImagePathBaseId + imageCount
                });

                // set incremented image link ids
                $('.perc-slider-image-link').last().attr({
                    id: sliderImageLinkBaseId + imageCount,
                    name: 'imageLinkField' + imageCount
                });
                $('.perc-page-field-select-button').last().attr({
                    for: sliderImageLinkBaseId + imageCount
                });

                imageCount++;

            });


            // This section listens for either a click or keypress on the move buttons
            $('.perc-move-button-up').on('keypress click', function(){
                moveRowUp( $(this) );
            });
            $('.perc-move-button-down').on('keypress click', function(){
                moveRowDown( $(this) );
            });

            function moveRowUp( thisObj ) {
                if( (thisObj.parents('.perc-image-slider-row').prev().hasClass('hide')) === false ) {
                    thisObj.parents('.perc-image-slider-row').insertBefore(thisObj.parents('.perc-image-slider-row').prev());
                }
            }
            function moveRowDown(thisObj) {
                if( (thisObj.parents('.perc-image-slider-row').is(':last-child')) === false ) {
                    thisObj.parents('.perc-image-slider-row').insertAfter(thisObj.parents('.perc-image-slider-row').next());
                }
            }

        }

        /***
         * Validates the Widget data.
         * @returns true if everything is valid, false if not
         */
        function validateForm(){

            return true;
        }

        function convertToSlug(Text) {
            return Text
                .toLowerCase()
                .replace(/[^\w ]+/g,'')
                .replace(/ +/g,'-');
        }

        /***
         * Responsible for collecting Widget data into a Json object that can be saved to a field on the backing content type.
         */
        function getData(){
            var percJSONConfig = [];

            //When content is submitted, loop through and build array of objects to push into the percJSONConfig array
            $(".perc-image-slider-row").each(function() {
                if(!  $(this).hasClass("hide") ){
                    var alt = new Object();
                    alt["percImagePath"] =  $(this).find(".perc-slider-image-path").val();
                    alt["imageCaption"] =  $(this).find(".perc-slider-image-caption").val();
                    alt["imageCaption2"] =  $(this).find(".perc-slider-image-caption-2").val();
                    alt["percPagePath"] =  $(this).find(".perc-slider-image-link").val();
                    alt["externalLink"] =  $(this).find(".perc-slider-image-external-link").val();
                    alt["linkSetting"] =  $(this).find("#perc-slider-image-link-setting :selected").val();
                    alt["percImageContentId"] = $(this).find(".perc-slider-image-path").attr('data-perc-image-content-id');
                    alt["percPageContentId"] = $(this).find(".perc-slider-image-link").attr('data-perc-page-content-id');
                    alt["percImagePathLinkId"] = $(this).find(".perc-slider-image-path").attr('data-perc-image-path-link-id');
                    alt["percPagePathLinkId"] = $(this).find(".perc-slider-image-link").attr('data-perc-page-path-link-id');
                    // only push the row into the array if an image path has been selected
                    if ( alt["percImagePath"] !== '' && alt["percImagePath"] !== "#" ) {
                        percJSONConfig.push(alt);
                    }
                }
            });

            return percJSONConfig;
        }
        /**
         * From the editor, reads the values and creates a JSON Object and assigns the JSON object
         * as the value of the hidden input of the Content Types field.
         */
        function _preSubmitHandler()
        {
            //Validate the Widget.
            if(!validateForm()){return false;}

            //Get the Widget data and convert it to a JSON string that can be saved.
            var data = {};
            data.sliderName = convertToSlug(  $("input[name='sliderName']").val());
            data.percJSONConfig=getData();

            //Updates hidden input with JSON object
            var fieldName = $(".perc-image-slider").attr("id");
            $("input[name='" + fieldName + "']").val(JSON.stringify(data));
            return true;

        }

        return percImageSliderSetupApi;
    };

    function validateAssetItems() {

        // This function loops through each slider row and checks the value to find out of it is a valid link
        // and if the asset is archived. If either of these things are true, the field is highlighted in red

        sliderWarningCount = 0;
        sliderErrorCount = 0;

        // This function loops through each slider row and checks the image path
        // and links to ensure they are valid

        $(".perc-image-slider-row").each(function() {

            // skip the template div
            if(!  $(this).hasClass("hide") ){
                currentElement = this;
                currentImagePath = $(currentElement).find(".perc-slider-image-path").val();
                currentInternalPagePath = $(currentElement).find(".perc-slider-image-link").val();
                currentLinkSetting = $(currentElement).find("#perc-slider-image-link-setting :selected").val();

                imageTargetClass = '.perc-slider-image-path';
                internalPageTargetClass = '.perc-slider-image-link';

                // Image asset needs to be validated, and then the internal page link

                // Check if the image path is defined
                if(currentImagePath != '#' && currentImagePath != '') {

                    itemType = 'Asset';

                    // Constructed image asset api request url
                    url = 'http://' + window.location.host + '/Rhythmyx/rest/assets/by-path' + currentImagePath;

                    // We need to check the workflow status of the image to make sure it is not set to Archive
                    checkItemStatus(url, currentElement, imageTargetClass, itemType);

                } // end if current image path is not '#'

                // If the image path is not valid, apply the error class
                else {
                    $(currentElement).find(imageTargetClass).removeClass('perc-input-slider-warning');
                    $(currentElement).find(imageTargetClass).addClass('perc-input-slider-error');
                    sliderErrorCount++;
                }

                // Then validate the internal page link
                if(currentLinkSetting == 'internal') {

                    if(currentInternalPagePath != '#' && currentInternalPagePath != '') {

                        itemType = 'Page';

                        // We need to drop off the '/Sites/' folder at the beginning of the path
                        // for the page api to work
                        url = 'http://' + window.location.host + '/Rhythmyx/rest/pages/by-path' + currentInternalPagePath.replace('/Sites/', '/');

                        checkItemStatus(url, currentElement, internalPageTargetClass, itemType);

                    }
                    else {
                        $(currentElement).find(internalPageTargetClass).removeClass('perc-input-slider-warning');
                        $(currentElement).find(internalPageTargetClass).addClass('perc-input-slider-error');
                        sliderErrorCount++;
                    }

                }

                else {
                    // If we are linking to an external page or have no link, we do not need to validate
                    // any internal links and we can remove the warnings and errors
                    $(currentElement).find(internalPageTargetClass).removeClass('perc-input-slider-warning');
                    $(currentElement).find(internalPageTargetClass).removeClass('perc-input-slider-error');
                }

            } // end if not template object

        }); // end each slider row

        processWarningsAndErrors();

    } // end function validateAssetItems

    function checkItemStatus(url, currentElement, targetClass, itemType) {

        // This function checks the input assets and adds/removes classes as needed
        // This is set to run synchronously so that the warnings can be incremented
        // and tallied once at the end of the validateAssetItems function

        $.ajax({
            contentType: 'application/json',
            async: false,
            url: url,
            dataType: 'json',
            success: function(json) {
                if(itemType=="Asset"){
                    item=json.asset;
                }else{
                    item = json[itemType];
                }
                if( item.workflow.state != 'Live' && item.workflow.state != 'Pending') {
                    $(currentElement).find(targetClass).addClass('perc-input-slider-warning');
                    $(currentElement).find(targetClass).removeClass('perc-input-slider-error');
                    sliderWarningCount++;
                }
                else {
                    $(currentElement).find(targetClass).removeClass('perc-input-slider-warning');
                    $(currentElement).find(targetClass).removeClass('perc-input-slider-error');
                }
            }, // end success
            error: function() {
                console.log('Percussion Slider Widget Error: Could not connect to REST API to retrieve asset content status');
            } // end error
        }); // end ajax

    }

    function processWarningsAndErrors() {

        if( sliderWarningCount > 0 || sliderErrorCount > 0 ) {
            $('#slideConfigurationAlert').removeClass('hidden');
            $('#slideConfigurationAlertDetails').removeClass('hidden');
            if( sliderWarningCount > 0 ) {
                $('#sliderWarningDetails').removeClass('hidden');
            }
            else {
                $('#sliderWarningDetails').addClass('hidden');
            }
            if( sliderErrorCount > 0 ) {
                $('#sliderErrorDetails').removeClass('hidden');
            }
            else {
                $('#sliderErrorDetails').addClass('hidden');
            }
        }
        else {
            $('#slideConfigurationAlert').addClass('hidden');
            $('#slideConfigurationAlertDetails').addClass('hidden');
        }

    }

})(jQuery);
