/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var MSG_QUEUED_FOR_UPLOAD = ' file(s) queued for upload';
var TOTAL_IN_QUEUE = 0;
var NUM_FAILED = 0;
var NUM_COMPLETED = 0;
var PERC_UTILS = percJQuery.perc_utils;
var IMAGE_FILE_TYPES = ['png', 'jpg', 'jpeg', 'tiff', 'gif', 'webp', 'svg'];
var DID_UPLOAD_FAIL = false;
var XHR_REQUESTS = [];

$(function () {
    $('#perc-selector-wrapper').hide();
    $('#perc-html-selector').val('');
    $('#perc-upload-start').prop('disabled', true).
        on("click",function() {
			triggerSubmit();
        })
		.on("keydown",function(event) {
			if(event.code == "Enter" || event.code == "Space"){
				document.activeElement.click();
			}
        });
    $('#perc-upload-clear').prop('disabled', true);
    $('#perc-upload-cancel').prop('disabled', true);



    $('#fileupload').fileupload({
        url: '/Rhythmyx/uploadAssetFile?folder=/Assets/uploads/test',
        recalculateProgress: false,
        add: function (e, data) {
            TOTAL_IN_QUEUE++;
            $('#perc-added-files').css('display', 'table');
            $('#perc-bulk-status').text(TOTAL_IN_QUEUE + MSG_QUEUED_FOR_UPLOAD);

            var buttonHtml = generateButtonHTML(data);
            data.context = buttonHtml;
            $('#perc-upload-trigger').on("click",function() {
                $('#perc-upload-clear').prop('disabled', true);
                $(this).off();
                if (data.files.length > 0) {
                    $('#perc-upload-cancel').prop('disabled', false);
                    $('#perc-global-progress').removeClass('fade')
                    .show();
                    gadgets.window.adjustHeight();
                    data.url = calculateUrl();
                    jqXHR = data.submit();
                    XHR_REQUESTS.push(jqXHR);
                }
            });
            $('#perc-upload-clear').on("click", function() {
                DID_UPLOAD_FAIL = false;
                if (data.files.length > 0) {
                    data.files = [];
                    TOTAL_IN_QUEUE = 0;
                    $('#perc-bulk-status').text(TOTAL_IN_QUEUE + MSG_QUEUED_FOR_UPLOAD);
                }
                $('#perc-added-files').fadeOut('slow', function() {
                    $('#perc-upload-clear').prop('disabled', true);
                    $(this).empty();
                });
            });
			$('#perc-upload-clear').on("keydown", function(event) {
                 if(event.code == "Enter" || event.code == "Space"){
					document.activeElement.click();
				}
            });
        },
        done: function (e, data) {
            TOTAL_IN_QUEUE--;
            NUM_COMPLETED++;
            $('#perc-bulk-status').text(TOTAL_IN_QUEUE + MSG_QUEUED_FOR_UPLOAD);
            var numActive = $(this).fileupload('active');
            if (numActive === 1) {
                markCompleted();
            }

            data.context.remove();
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .bar').css(
                'width',
                progress + '%'
            );
        },
        fail: function(e, data) {
            TOTAL_IN_QUEUE--;
            NUM_FAILED++;
            DID_UPLOAD_FAIL = true;
            $(data.context).addClass('alert alert-danger');
            $('#perc-bulk-status').text(TOTAL_IN_QUEUE + MSG_QUEUED_FOR_UPLOAD);
            var numActive = $(this).fileupload('active');
            if (numActive === 1) {
                markCompleted();
            }
        }
    });

    // Wire asset type select control
    $('#perc-bulk-asset-type').on("change", function(){
        var v = $('#perc-bulk-asset-type option:selected').val();
        if(v === 'html' || v === 'richtext' || v === 'simpletext')
        {
            $('#perc-selector-wrapper').show();
        }
        else
        {
            $('#perc-selector-wrapper').hide();
        }
    });

    $('#perc-files-upload').on("click", function() {
        if (DID_UPLOAD_FAIL) {
            $('#perc-upload-clear').trigger("click");
            DID_UPLOAD_FAIL = false;
        }
    });

    $('#perc-upload-cancel').on("click", function() {
        cancelAllRequests();
    });
	$('#perc-upload-cancel').on("keydown", function(event) {
        if(event.code == "Enter" || event.code == "Space"){
			document.activeElement.click();
		}
    });
	$('#perc-files-upload').on("keydown", function(event) {
        if(event.code == "Enter" || event.code == "Space"){
			document.activeElement.click();
		}
    });

    $('#perc-upload-cancel').on("keydown", function(event) {
        if(event.code == "Enter" || event.code == "Space"){
			document.activeElement.click();
		}
    });

});

/**
 * Generates the HTML <tr> for each
 * added file to the uploads queue.
 * @param data
 */
generateButtonHTML = function(data) {
    var image = data.files[0];
    var buttonHtml = $('<button class="btn perc-button" aria-label="remove item from queue">' +
         '<span><i class="fa fa-times" aria-hidden="true"></i></span></button>')
        .on("click",function () {
            $(data.context).fadeOut('slow', function() {
                data.context.remove();
                TOTAL_IN_QUEUE--;
                $('#perc-bulk-status').text(TOTAL_IN_QUEUE + MSG_QUEUED_FOR_UPLOAD);
            });
            data.files.pop();
        });

    var html =
        $('<tr/>').css('word-break','break-all')
            .append($('<td/>').text(image.name))
            .append($('<td/>').text(image.size))
            .append($('<td/>').text(image.type))
            .append($('<td/>').append(buttonHtml));

    html.appendTo($('#perc-added-files'))
        .hide()
        .fadeIn('slow');

    return html;
};

markCompleted = function() {
    $('#perc-bulk-status').text('Successful Uploads: ' + NUM_COMPLETED +
         ', Failed uploads: ' + NUM_FAILED);
    $('#progress .bar').css(
        'width',
        0 + '%'
    );

    NUM_COMPLETED = 0;
    NUM_FAILED = 0;
    XHR_REQUESTS = [];

    $('#perc-upload-start').prop('disabled', true);
    if (!DID_UPLOAD_FAIL) {
        // should remain available to clear the failed uploads
        $('#perc-upload-clear').prop('disabled', true);
    }
    $('#perc-upload-cancel').prop('disabled', true);

    $('#perc-global-progress').addClass('fade')
    .hide();
};

calculateUrl = function() {
    var url = '/Rhythmyx/uploadAssetFile?';

    var folderPath = $('#perc-bulk-target-folder').text();

    //encode the folder path string for '&' character that was causing the incorrect folder path in bulk upload gadget upload asset request.
    //encodeURIComponent function encodes the special char in URI if they are used in parameters.
    if( typeof folderPath !== 'undefined' ){
        folderPath = encodeURIComponent(folderPath);
    }

    if (folderPath && folderPath !== '') {
        url += 'folder=' + folderPath;
    }

    var assetType = $('#perc-bulk-asset-type option:selected').val();
    if (assetType && assetType !== '') {
        url += '&assetType=' + assetType;
    } else {
        url += '&assetType=file';
    }

    var cssSelectorValue = $('#perc-html-selector').val();
    if (cssSelectorValue && cssSelectorValue !== '') {
        url += '&cssSelector=' + cssSelectorValue;
    }

    var includeElementValue = $('#perc-selector-options input:checked').val();
    if (includeElementValue && includeElementValue !== '') {
        url += '&includeElement=' + includeElementValue;
    }

    var approveOnUpload = $('#perc-bulk-approve-onupload').is(':checked');
    url += '&approveOnUpload=' + approveOnUpload;

    return url;
};

triggerSubmit = function() {
    if (shouldPromptForFileType()) {
        var title = 'File upload';
        var options = {
            title: title,
            question: 'You have added images for upload but have not selected the \'Asset Type\' of \'image.\'  Are you sure this is correct?',
            cancel: function()
            {
                return;
            },
            success: function()
            {
                $('#perc-upload-trigger').trigger("click");
            }
        };
        PERC_UTILS.confirm_dialog(options);
    } else {
        $('#perc-upload-trigger').trigger("click");
    }
};

shouldPromptForFileType = function() {
    var v = $('#perc-bulk-asset-type option:selected').val();
    var containsImage = false;
    $('#perc-added-files tr').each(function() {
        var fileName = $(this).has('td').children('td :first').text();
        if (fileName && fileName !== '') {
            var extension = getFileExtension(fileName);
            if (IMAGE_FILE_TYPES.indexOf(extension.toLowerCase()) > 0) {
                containsImage = true;
                return false; // to break out of jQuery .each loop
            }
        }
    });
    if (containsImage && v !== 'image') {
        return true;
    }
    return false;
};

getFileExtension = function(fileName) {
    return fileName.split('.').pop();
};

cancelAllRequests = function() {
    console.log(XHR_REQUESTS);
    for (let i = 0; i < XHR_REQUESTS.length; i++) {
        XHR_REQUESTS[i].abort();
    }
    XHR_REQUESTS = [];
};
