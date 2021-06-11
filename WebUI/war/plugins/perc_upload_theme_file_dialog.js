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

/**
 * perc_upload_theme_file_dialog
 *
 * Show a dialog to upload theme files.
 */
(function($){
    /**
     * Public API
     */
    $.perc_upload_theme_file_dialog = {
            open: openUploadDialog
    };
    
    /**
     * Constructs the dialog
     * @param finder The finder object.
     */
    function openUploadDialog(finder)
    {
        // The current finder path could be ending with a file, and we don't want that
        var path = initializePath();
        
        // Build an absolute URL as a workaround for IE issue (popup security warning)
        // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
        var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";
        
        var buttons = {};
        buttonSave = {
                id : "perc-addItem-dialog-save"
        };
        buttonCancel =   {
                id : "perc-addItem-dialog-cancel",
                click : function() {
                    dialog.remove();
                }
        };
        buttons.Save = buttonSave;
        buttons.Cancel = buttonCancel;
        
        var dialog = createDialog();
        
        
        /**
         * Builds the dialog (and its form) invoking perc_dialog()
         * @returns jQuery element wrapping the dialog and form created with perc_dialog()
         */
        function createDialog()
        {
            var actionUrl = $.perc_paths.WEBRESOURCESMGT_FILE_UPLOAD;
            // Basic dialog content HTML markup
            var dialogContent = "<div id='perc-design-file-upload'>" +
                     '<form id="perc-theme-file-upload-form" name="perc-theme-file-upload-form" enctype="multipart/form-data" method="post" action="' + actionUrl +'">' +
//                    + '<span class="perc-required-legend"><label>* - ' + I18N.message("perc.ui.uploadtheme.form.text@denotes required field") + '</label></span>'
//                        + '<label class="perc-required-field" for="upload-theme-file-attachment" accesskey="F"><u>F</u>ile name:</label><br>'
                     '<label for="upload-theme-file-attachment">File name:</label><br>' +
                         '<input type="hidden" name="upload-theme-file-path" />' +
                         '<input type="file" size="50" name="upload-theme-file-attachment" />' +
                     '</form>' +
                    // hidden div that will hold the server response
                     '<div id="perc-theme-file-upload-response" style="display:none"></div>' +
                 "</div>";
            
            // Create the upload dialog
            var d = $(dialogContent).perc_dialog( {
                title: I18N.message( "perc.ui.uploadtheme.dialog.title@Upload File"),
                id: "perc-upload-theme-file-dialog",
                width: 686,
                resizable : false,
                modal: true,
                closeOnEscape : true,
                percButtons: buttons,
                open: function() {
                    // Initialize the Save button, biding its submit behavior and styling
                    initializeSaveButton();
                    
                    // Initialize the jQuery form plugin
                    $('#perc-theme-file-upload-form').ajaxForm({
                        // Element that will hold the server response after submitting the
                        target: '#perc-theme-file-upload-response',
                        // Function that must be triggered to close the dialog on success, or show an error dialog
                        success: handleResponse,
                        // Function to be called after an error occured (specially for timeouts, as the file upload
                        // service returns 200 always
                        error: handleError,
                        // Since we are submitting a file upload form, we must use an iframe as a target of the
                        // form. The jQuery form plugin will handle it automatically
                        iframe: true
                    }); 
                }
            });
            return $(d);
        }

        /**
         * Styles the Save button and binds the save logic to the button Save.
         */
        function initializeSaveButton()
        {
            // Build an absolute URL as a workaround for IE issue (popup security warning)
            // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
            var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";
            var buttonSave = $("#perc-addItem-dialog-save");
            
            buttonSave
                .off('click')
                // Bind function to the click event in the button
                .on("click",saveLogic);
        }
        
        /**
         * Logic involved after clicking the Save button
         */
        function saveLogic()
        {   
            if (checkFileFieldCompleted() === false)
            {
                return false;
            }
            checkElementWithSameNameOrUpload();
            return false;
        }
              
        /**
         * Checks that the field file was completed after clicking the Save button.
         * If not, it shows an error label below the field.
         * @return boolean true if the input file filed has been completed
         */
        function checkFileFieldCompleted()
        {
            var fileField = dialog.find('input:file');
            var errorLabel = dialog.find('label.perc_field_error');
            if (fileField.val() === '')
            {
                // Show the error message, if not shown previously
                if (errorLabel.length === 0)
                {
                    fileField.after('<label class="perc_field_error" for="upload-theme-file-attachment" style="display: block;">' + I18N.message("perc.ui.uploadtheme.form.text@Please select a file.") + '</label>');
                }
                return false;
            }
            else
            {
                errorLabel.remove();
                return true;
            }
        }
        
        /**
         * Checks if a file with the same name of the exists in the current finder Path (under Design node).
         */
        function checkElementWithSameNameOrUpload()
        {
            // There are differences with IE, FF and Chrome when getting the value from file input field
            var fileName = dialog.find('input:file').val().replace(/.+[\\\/]/, "");
                        
            // manually encode the filename for non-Ascii characters
            fileName = $.perc_utils.encodeURL(fileName);
            var encodedPath = $.perc_utils.encodePathArray(path);
            
            $.PercWebResourcesService.validateFileUpload(encodedPath, fileName, function(status, result)
                {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        if (result.data === $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            // No element with same name found in the path, proceed with the upload
                            uploadFile();
                        }
                        else
                        {
                            // An element with the same name has been found, confirm overwrite
                            var options = {
                                    id       : "perc-design-file-upload-file-exist-warning",
                                    title    : I18N.message( "perc.ui.uploadtheme.dialog.title@Warning"),
                                    question : result.data,
                                    yes      : "OK",
                                    // The user chose to overwrite the file
                                    success  : uploadFile 
                            };
                            $.perc_utils.confirm_dialog(options);
                        }
                    }
                    else
                    {
                        // There is a folder with the same name, this is an erroneous situation and that is why
                        var options = {
                                id      : "perc-design-file-upload-file-exist-error",
                                title   : I18N.message( "perc.ui.uploadtheme.dialog.title@Error"),
                                content : $.PercServiceUtils.extractDefaultErrorMessage(result.request)
                        };
                        $.perc_utils.alert_dialog(options);
                    }
                }
            );
        }
        
        /**
         * Uploads the file into the corresponding finder path.
         */
        function uploadFile()
        {
            $.PercBlockUI();
            
            // Upload the path hidden field and submit the form
            // There are differences with IE, FF and Chrome when getting the value from file input field
            var fileName = dialog.find('input:file').val().replace(/.+[\\\/]/, "");
            
            // manually encode the filename for non-Ascii characters
            fileName = $.perc_utils.encodeURL(fileName);
            var encodedPath = $.perc_utils.encodePathArray(path);

            dialog.find('input[name="upload-theme-file-path"]').val('/' + encodedPath.slice(3).join('/') + '/' + fileName);
            dialog.find('#perc-theme-file-upload-form').submit();
        }
        
        /**
         * Checks the response from the server. If there is any, show it with a dialog.
         */
        function handleResponse()
        {
            // We need the text from the response
            var textResponse = $('#perc-theme-file-upload-response').text();

            // If there is a message in the response, open an alert with the error message and
            // reopen the upload dialog
            if (textResponse === "")
            {
                $.unblockUI();
                dialog.remove();
                finder.refresh(function() {});
            }
            else
            {   
                var options = {
                    title: I18N.message("perc.ui.uploadtheme.dialog.title@Error"), 
                    content: textResponse
                };
                $.perc_utils.alert_dialog(options);
                $.unblockUI();
            }
        }
        
        /**
         * Checks the response from the server. If there is any, show it with a dialog.
         */
        function handleError(jqXHR, textStatus, errorThrown)
        {
            var textResponse = I18N.message("perc.ui.upload.theme.file.dialog@Unkown Error");
            
            if (textStatus === "timeout")
                textResponse = I18N.message("perc.ui.upload.theme.file.dialog@Operation time");
            
            var options = {
                title: I18N.message("perc.ui.uploadtheme.dialog.title@Error"), 
                content: textResponse
            };
            $.perc_utils.alert_dialog(options);
            $.unblockUI();
        }
        
        /**
         * Process the current finder path in order to determine if the last element is
         * a file, folder, etc. and set
         * @return String path Returns the intended path where the file will be uploaded
         */
        function initializePath()
        {
            var current_path = finder.getCurrentPath();
            
            // Get the selected item from Column or List mode with the class FSFile
            selectedItemSpec = $("#perc-finder-listview .perc-datatable-row-highlighted").data("percRowData");
            if (selectedItemSpec === undefined)
            {
                var selectedItemSpec = $(".mcol-listing.perc-listing-type-FSFile.mcol-opened").data("spec");
            }
            
            // If we selected a file, pop out the last element
            if (selectedItemSpec !== undefined && selectedItemSpec.type === 'FSFile' && selectedItemSpec.leaf)
            {
                current_path.pop();
            }
            
            return current_path;
        }

    }// End of function: openUploadDialog
})(jQuery);