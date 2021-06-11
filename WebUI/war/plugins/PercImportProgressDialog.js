/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 * "Import progress" dialog. Shows the implementation progress with a progress bar.
 */
(function($)
{
    /**
     * Creates the dialog.
     * @param Object config this object should have the following structure:
     * <pre>
     *    {
     *        showVideo: boolean, true if we want to include the video in the dialog (OPTIONAL)
     *        backgroundRefreshCallback: function() UI callback function to refresh after import
     *        onSuccessCallback: function(importData) callback function to handle the import result
     *        startProgressCallback: function(callbackJobIdHandler) status polling function
     *    }
     * </pre>
     */
    $.PercImportProgressDialog = function PercImportProgressDialog(config)
    {
        var CONSTANTS = {
            IDS: {
                MAIN_DIALOG: 'perc_import_progress_dialog',
                MAIN_DIALOG_CLOSE_BUTTON: 'perc_import_progress_dialog_close',
                MAIN_DIALOG_CLOSE_DISABLED_BUTTON: 'perc_import_progress_dialog_close_disabled',
                MAIN_DIALOG_DONE_BUTTON: 'perc_import_progress_dialog_done',
                MAIN_DIALOG_DONE_DISABLED_BUTTON: 'perc_import_progress_dialog_done_disabled'
            },
            TEXT: {
                MAIN_DIALOG_TITLE: I18N.message("perc.ui.ImportProgressDialog.title@Import"),
                IMPORT_ERROR_MESSAGE: I18N.message('perc.ui.ImportProgressDialog.message@Import failed message'),
                IMPORT_SUCCESS_MESSAGE: I18N.message('perc.ui.ImportProgressDialog.message@Import succeded message')
            },
            OTHER: {
                EVENTS_ENABLE_DONE_BUTTON: 'enableDoneButton',
                POLLING_PERIOD_DURATION: 100,
                IMPORT_LOG_URL: $.perc_paths.VIEW_IMPORT_LOG + '?templateId='
            }
        };

        // Default dialog options
        var dialogButtons = {
            "Done": {
                id: CONSTANTS.IDS.MAIN_DIALOG_DONE_BUTTON,
                click: function(event)
                {
                    // Bind the enabled DONE button to the onSuccess callback function with the
                    // importData returned when the import process finish successfuly
                    config.onSuccessCallback(importData);
                }
            },
            "Done Disabled": {
                id: CONSTANTS.IDS.MAIN_DIALOG_DONE_DISABLED_BUTTON
            },
            "Close Normal": {
                id: CONSTANTS.IDS.MAIN_DIALOG_CLOSE_BUTTON,
                click: function()
                {
                    // If the dialog closes, set to true the UI refresh, to enque the refresh
                    invokeBackgroundRefreshCallback = true;
                    // If the import progress finished, we have to also refrsh in background
                    if (config.backgroundRefreshCallback !== undefined && importFinished === true)
                    {
                        config.backgroundRefreshCallback();
                    }
                    dialog.remove();
                }
            }
        };
        var percDialogOptions = {
            id: CONSTANTS.IDS.MAIN_DIALOG,
            title: CONSTANTS.TEXT.MAIN_DIALOG_TITLE,
            modal: true,
            resizable: false,
            closeOnEscape: true,
            width: 686,
            height: 'auto',
            percButtons: dialogButtons
        };
        // The dialog basic markup
        var dialogMarkup = $('<div/>');
        // Holds the HTML element wrapped with jQuery and the perc_dialog generated
        var dialog;
        // Holds the import result data
        var importJobId;
        // The result data from the import progress
        var importData;
        // Flag needed to ignore unnecesary function calls
        var importFinished = false;
        // Will hold the % of the import progress
        var importPercentage = -1;
        // Will hold the the progress section elements wrapped with jQuery
        var progressSection;
        // Flag that will tell the import progress to perform a refresh in the UI if the import
        // dialog has been closed and an import progress was in progress
        var invokeBackgroundRefreshCallback = false;

        function createImportProgressDialog(config)
        {
            // Initialize the dialog markup and instantiate the perc_dialog plugin
            progressSection = '<div class="progress">';
            progressSection +=     '<p class="progress-message">&nbsp;</p>';
            progressSection +=     '<div class="progress-bar-container">';
            progressSection +=         '<div class="progress-bar"></div>';
            progressSection +=     '</div>';
            progressSection +=     '<div class="error-message">';
            progressSection +=         CONSTANTS.TEXT.IMPORT_ERROR_MESSAGE;
            progressSection +=     '</div>';
            progressSection +=     '<div class="success-message">';
            progressSection +=         CONSTANTS.TEXT.IMPORT_SUCCESS_MESSAGE;
            progressSection +=     '</div>';
            progressSection += '</div>';
            progressSection = $(progressSection);

            dialogMarkup.append($('<div>').append(progressSection));

            if (config.showVideo === true)
            {
                dialogMarkup.append(
                $('<div align="center" style="margin-top:24px;height: 315px; overflow:hidden">')
                    .append(
                        $('<iframe id="perc_import_video" src="//help.percussion.com/in-product/single2" scrolling="no" marginheight="0" marginwidth="0" frameborder="0" width="560px" height="0px" style="display: block; overflow: hidden">')
                    )
                    .append(
                        $('<img id="perc_import_image" src="../images/images/ImportVideoNotFound.png" width="560px" height="0px">')
                    )
                );
            }

             dialogMarkup.append(
                $('<div>')
                    .append('<p class="hint">Ask questions. Get answers. Visit the <a target="_blank" href="https://community.percussion.com" title="Percussion Community">Percussion Community</a> to access Video Tutorials, Forums, and more.</p>')
            );

            if (config.showVideo === true)
            {
                //Used a random dummy parameter to avoid cache
                dialogMarkup.append(
                    $('<img height="0px" width="0px" src="https://help.percussion.com/Assets/Help/header/images/PercussionSwoosh.png?dummy=' + Math.random() + '">')
                        .error(handleUnreachableURL)
                        .load(showVideoIframe)
                );
            }

            function handleUnreachableURL()
            {
                $("#" + CONSTANTS.IDS.MAIN_DIALOG).find("#perc_import_image").attr("height", "315px");
            }

            function showVideoIframe()
            {
                $("#" + CONSTANTS.IDS.MAIN_DIALOG).find("#perc_import_video").attr("height", "315px");
            }

            dialog = $(dialogMarkup).perc_dialog(percDialogOptions);

            configureDialogUI();
            startImportProgress();
        }

        /**
         * Setup of the dialog UI general configuration and events.
         */
        function configureDialogUI()
        {
            // Fix the disabled DONE button right margin is incorrect
            var doneButtonDisabled = $('#' + dialogButtons['Done Disabled'].id);

            // By default the DONE button is disabled (the enabled version is hidden)
            var doneButton = $('#' + dialogButtons.Done.id);
            doneButton.hide();
            // Set a custom event for enabling/disabling the DONE button
            $('#' + CONSTANTS.IDS.MAIN_DIALOG).on(
                CONSTANTS.OTHER.EVENTS_ENABLE_DONE_BUTTON,
                function(event, flag)
                {
                    enableDoneButton(flag);
                }
            );
        }

        /**
         * Enables or disables the DONE button according a flag parameter.
         * @param boolean flag If true, will enable the button (otherwise disable it)
         */
        function enableDoneButton(flag)
        {
            var doneButton = $('#' + dialogButtons.Done.id),
                doneButtonDisabled = $('#' + dialogButtons['Done Disabled'].id);
            if (flag === true)
            {
                doneButton.show();
                doneButtonDisabled.hide();
            }
            else
            {
                doneButton.hide();
                doneButtonDisabled.show();
            }
        }

        /**
         * Checks the status of the import job.
         * TODO:
         * - the whole process should have a timeout near 10mins
         */
        function startImportProgress()
        {
            config.startProgressCallback(function(status, jobId)
            {
                // If something went wrong during the job creation, show the error status status.
                // Else, invoke the polling of the job status
                if (status !== $.PercServiceUtils.STATUS_SUCCESS)
                {
                    setErrorState();
                }
                else
                {
                    importJobId = jobId;
                    pollStatus();
                }
            });
        }

        /**
         * Checks the status of the import job.
         */
        function pollStatus()
        {
            // We could have one or more pollStatus() invokations pending and, previosly got the
            // completed status. This check prevent from making an unnecesary request if the
            // process finished
            if (importFinished === true)
            {
                return;
            }

            config.pollingProgressCallback(importJobId, function(status, asyncJobStatus) {
                if (status !== $.PercServiceUtils.STATUS_SUCCESS || asyncJobStatus.status < 0)
                {
                    setErrorState();
                }
                else
                {
                    var progressMessage = progressSection.find('.progress-message');
                    var progressBar = progressSection.find('.progress-bar');

                    // Update the progress info only when the job has done some % of advance
                    if (asyncJobStatus.status > importPercentage)
                    {
                        // If there is an import status message, make it look good by making the
                        // first letter of the sentence uppercase and appending "..."
                        if (asyncJobStatus.message !== undefined &&
                            asyncJobStatus.message.length > 0)
                        {
                            asyncJobStatus.message = asyncJobStatus.message.charAt(0).toUpperCase() +
                                asyncJobStatus.message.slice(1) + '...';
                            progressMessage.html(asyncJobStatus.message);
                        }

                        // Update progress representation and store the % of progress
                        progressBar.css('width', asyncJobStatus.status + '%');
                        importPercentage = asyncJobStatus.status;
                    }

                    if (asyncJobStatus.status === 100)
                    {
                        // The job reached its end successfully, set the corresponding flag to true
                        importFinished = true;

                        // If the dialog has been closed before the import finishes, invoke the
                        // background refresh callback
                        if (config.backgroundRefreshCallback !== undefined &&
                            invokeBackgroundRefreshCallback === true)
                        {
                            config.backgroundRefreshCallback();
                            return;
                        }

                        // We put a delay of 1,5 seg after the 100% is reached, so the user can see
                        // it before showing the success state of the dialog
                        setSuccessState();
                        return;
                    }

                    // Reached this point, we are in the middle of the progress, call the
                    // pollStatus function again
                    setTimeout(pollStatus, CONSTANTS.OTHER.POLLING_PERIOD_DURATION);
                }
            });
        }

        /**
         * Makes the progress message and progress bar hidden, so the layout stays umodified.
         */
        function hideProgressMessageAndBar()
        {
            progressSection.find('.progress-message').css('visibility', 'hidden');
            progressSection.find('.progress-bar-container').css('visibility', 'hidden');
        }

        /**
         * Shows the error state tha the dialog shows when something went wrong during the whole
         * process.
         */
        function setErrorState()
        {
            hideProgressMessageAndBar();
            progressSection.find('.error-message').show();
        }

        /**
         * Sets the dialog appearance when the import finished successfuly
         */
        function setSuccessState()
        {
            // We have to retrieve the import result before proceeding
            config.importResultCallback(importJobId, function(status, importResult)
            {
                importData = importResult;

                // Set the corresponding link for the import log
                setTemplateIdForLink(importData);

                hideProgressMessageAndBar();
                progressSection.find('.success-message').show();

                // Enable the DONE button
                enableDoneButton(true);
            });
        }

        /**
         * Retrieves the id of the template needed in the import log link
         */
        function setTemplateIdForLink()
        {
            /**
             * TODO: There is a little delay after completing the href attrib for the import log
             * link when using Chrome as the web browser. Don't know if it can be fixed via JS
             */
            function modifyLink(templateId)
            {
                var importLogLink = progressSection.find('a[title="Download the import log here"]');
                importLogLink.attr('href', CONSTANTS.OTHER.IMPORT_LOG_URL + templateId);
            }

            // We cheat a little here, we are not supposed to know importResult structure
            if (importData.id !== undefined)
            {
                modifyLink(importData.id);
            }
            else
            {
                // We don't have the ID for the template, so we have to make an extra service call
                $.PercSiteService.getTemplates(importData.Site.name, function(status, templatesData)
                {
                    modifyLink(templatesData.TemplateSummary[0].id);
                });
            }
        }

        // //////////////////////////////////////////////////////////
        // PercImportProgressDialog function execution starts from here
        // //////////////////////////////////////////////////////////
        createImportProgressDialog(config);
    };
})(jQuery);