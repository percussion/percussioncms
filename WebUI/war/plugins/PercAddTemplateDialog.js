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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * "Add Template" dialog implementation. Allows to create a template from a URL or Percussion
 * templates.
 */
(function($)
{
    /**
     * Creates the dialog to create a template.
     * @param function okCallback function invoked after a successful save and before close
     */
    $.PercAddTemplateDialog = function PercNavigationManager(okCallback)
    {
        var CONSTANTS = {
            IDS: {
                MAIN_DIALOG : 'perc_add_template_dialog',
                MAIN_DIALOG_SAVE_BUTTON: 'perc_add_template_dialog_save',
                MAIN_DIALOG_CANCEL_BUTTON: 'perc_add_template_dialog_cancel',
                CREATE_FROM_URL_RADIO: 'perc_add_template_dialog_from_url'
            },
            TEXT: {
                MAIN_DIALOG_TITLE: I18N.message("perc.ui.AddTemplateDialog.title@Add Template"),
                SELECT_SITE_ERROR_TEXT: I18N.message("perc.ui.AddTemplateDialog.text@Please select a site first.")
            }
        };

        // Default dialog options
        var dialogButtons = {
            "Save": {
                id:  CONSTANTS.IDS.MAIN_DIALOG_SAVE_BUTTON,
                click: onSaveClick
            },
            "Cancel": {
                id:  CONSTANTS.IDS.MAIN_DIALOG_CANCEL_BUTTON,
                click: function()
                {
                    dialog.remove();
                }
            }
        };
        var dialogOptions = {
            id: CONSTANTS.IDS.MAIN_DIALOG,
            title: CONSTANTS.TEXT.MAIN_DIALOG_TITLE,
            modal: true,
            resizable: false,
            closeOnEscape: false,
            width: 660,
            height: 'auto',
            percButtons: dialogButtons
        };
        // The dialog basic markup (forms)
        var dialogMarkup = '';
        // Holds the HTML element wrapped with jQuery and the perc_dialog generated
        var dialog;
        // Holds the form object to be validated
        var dialogForm;
        // Will hold the function to be excecuted after a successful template creation (and before
        // closing the dialog)
        var beforeSaveAndCloseCallback = function() {};
        // Will hold the new template name
        var newTemplateName;

        function createAddTemplateDialog(okCallback)
        {
            // Initialize the beforeSaveAndCloseCallback if the parameter is a function
            if (okCallback !== undefined && typeof(okCallback) === 'function')
            {
                beforeSaveAndCloseCallback = okCallback;
            }

            // Initialize the dialog markup and instantiate the perc_dialog plugin
            dialogMarkup += '<div>';
            dialogMarkup +=     '<p class="hint"> ' + I18N.message("perc.ui.addTemplateDialog@Hint") + '</p><br/>';
            dialogMarkup +=     '<form action="">';
            dialogMarkup +=         '<fieldset class="perc_url_field">';
            dialogMarkup +=             '<input class="perc_dialog_input radio-left" type="radio" name="template_from" id="perc_add_template_dialog_from_url" value="from_url" checked="checked">';
            dialogMarkup +=             '<label class="perc_dialog_label" for="perc_add_template_dialog_from_url">Enter page URL from existing site:</label><br>';
            dialogMarkup +=             '<input class="perc_dialog_input perc_dialog_field" id="url" name="url" maxlength="2048" type="text">';
            dialogMarkup +=         '</fieldset>';
            dialogMarkup +=         '<fieldset class="perc_percussion_template_field">';
            dialogMarkup +=             '<input class="perc_dialog_input radio-left" type="radio" name="template_from" id="perc_add_template_dialog_from_percussion_templates" value="from_percussion_templates">';
            dialogMarkup +=             '<label class="perc_dialog_label" for="perc_add_template_dialog_from_percussion_templates">Percussion Templates</label>';
            dialogMarkup +=             '<div class="auto-disable">';
            dialogMarkup +=                 '<select id="perc-templates-filter" name="perc-templates-filter"><option value="base">Base</option><option value="resp">Responsive</option></select>';
            dialogMarkup +=             '</div>';
            dialogMarkup +=             '<div id="perc-template-lib" class="auto-disable">';
            dialogMarkup +=                 '<div class="perc-select" id="perc_templates_filter_block"></div>';
            dialogMarkup +=             '</div>';
            dialogMarkup +=         '</fieldset>';
            dialogMarkup +=     '</form>';
            dialogMarkup += '</div>';
            dialog = $(dialogMarkup).perc_dialog(dialogOptions);

            // Append the template libray widget to the dialog
            dialog.find("#perc-template-lib").template_library();

            configureDialogUI();
        }

        /**
         * Setup of the dialog UI general configuration and events.
         */
        function configureDialogUI()
        {
            // Configures filters and basic validation in the form fields
            function configureValidations()
            {
                // Apply filters to fields
                var urlInputField = dialog.find('input[name="url"]');

                // Initialize validation for form and assign it to the function var
                var rules = {
                    url : {
                        // URL field is required only if the (radio) URL type was selected
                        required: 'input[type=radio]#perc_add_template_dialog_from_url:checked',
                        maxlength: 2000,
                        noBinary : "noBinary"
                    }
                };
                var messages = {
                    url: {
                        required: I18N.message("perc.ui.addTemplateDialog@URL Required"),
                        noBinary: I18N.message("perc.ui.addTemplateDialog@URL error")
                    }
                };
                dialogForm = dialog.find('form').validate(
                {
                    errorClass: "perc_field_error",
                    validClass: "perc_field_success",
                    wrapper: "p",
                    validateHiddenFields: false,
                    debug: false,
                    rules: rules,
                    messages: messages
                });
            }

            // Configure custom events and behavior of the dialog
            function configureCustomEvents()
            {
                // If URL radio button is selected disable the other fieldsets
                dialog.find('input[type=radio]').on("change",function(event)
                {
                    var radioButtonSelected = this;
                    dialog.find('fieldset').each(function()
                    {
                        // If the fieldset (this) doesn't contains the radio button
                        // disable the fieldset. Else enable it.
                        var theFieldset = $(this);
                        if (theFieldset.find(radioButtonSelected).length === 0)
                        {
                            theFieldset.trigger('perc-add-template-dialog-disable-fieldset');
                        }
                        else
                        {
                            theFieldset.trigger('perc-add-template-dialog-enable-fieldset');
                        }
                    });

                    // Re validate the form (if we change to percussion templates the message have to
                    // dissapear)
                    dialogForm.form();
                });
            }

            // Setup handlers for the custom events used before
            function configureCustomHandlers()
            {
                dialog
                    .find('fieldset.perc_url_field')
                    .on('perc-add-template-dialog-disable-fieldset',function(event)
                    {
                        $(this).find('input[name="url"]')
                            .attr('readonly', 'readonly')
                            .attr('disabled', 'true');
                    })
                    .on('perc-add-template-dialog-enable-fieldset',function(event)
                    {
                        $(this).find('input[name="url"]')
                            .removeAttr('readonly')
                            .removeAttr('disabled');
                    });

                // When disabling the URL fieldset:
                // - disable the select (dropdown element)
                // - disable/remove highlight in template selector?
                // When enabling revert what disabling did
                dialog
                    .find('fieldset.perc_percussion_template_field')
                    .on('perc-add-template-dialog-disable-fieldset',function(event)
                    {
                        $(this).addClass('disabled');
                    })
                    .on('perc-add-template-dialog-enable-fieldset',function(event)
                    {
                        $(this).removeClass('disabled');
                    });
            }

            ////////////////////////////////////////////////////////////
            // configureDialogUI function execution starts from here
            ///////////////////////////////////////////////////////////
            configureValidations();
            configureCustomEvents();
            configureCustomHandlers();
            // By default the fielset percussion templates is disabled
            dialog
                .find('fieldset.perc_percussion_template_field')
                .trigger('perc-add-template-dialog-disable-fieldset');
        }

        /**
         * Removes the validation message of the field, if it has one.
         * @param field Object HTML field element wrapped with jQuery.
         */
        function clearValidationErrorMessage(field)
        {
            var errorMsg = field.next();
            if (errorMsg.find('.perc_field_error').length > 0)
            {
                field.removeClass('perc_field_error');
                errorMsg.remove();
            }
        }

        /**
         * Callback invoked after clicking the save button and before creating a template.
         */
        function onSaveClick()
        {
            // Only continue if the form validated ok
            if (dialogForm.form())
            {
            	// close the dialog
            	$("#perc_add_template_dialog .ui-icon-closethick").trigger("click");
            	
                createTemplate();
            }
        }

        /**
         * Creates a template for the selected site (save button callback).
         */
        function createTemplate()
        {
            var site = $.PercNavigationManager.getSiteName();

            // This function will get invoked after creating the new template
            function postCreateTemplate(status)
            {
                $.unblockUI();
                if (! status)
                {
                    perc_utils.alert_dialog({ title: I18N.message("perc.ui.publish.title@Error"), content: result});
                }

                // Invoke the beforeSaveAndCloseCallback passing the new template name
                beforeSaveAndCloseCallback(newTemplateName);
                dialog.remove();
            }

            // 1) If there is no selected site, show an alert. This should not happen unless the
            // Actions menu entry was enabled for some reason
            if(site === '')
            {
                alert(CONSTANTS.TEXT.SELECT_SITE_ERROR_TEXT);
                return;
            }

            // 2) Get the the type of the new template (Percussion or URL) and invoke
            // the corresponding "create template from" function
            var createFrom = dialog.find('input[type=radio]').filter(':checked');
            if (createFrom.attr('id') == CONSTANTS.IDS.CREATE_FROM_URL_RADIO)
            {
                // This option and its function does not have something to executo (a callback)
                // after the template has been created, since a redirection will take place
                createFromUrl();
            }
            else
            {
                $.PercBlockUI();
                createFromPercussionTemplates(site, postCreateTemplate);
            }
        }

        /**
         * Creates a template for the selected site from Percussion templates.
         * @param String site The current selected site
         */
        function createFromPercussionTemplates(site, postSaveCallback)
        {
            // Get the selected item from the template library widget (traybar)
            // Create a copy of the selected template and assign it to the site
            var selectedTemplateFromId = $("#perc-template-lib .perc-selected").attr('id');
            var controller = $.PercSiteTemplatesController(false);
            newTemplateName = controller.copyTemplate(selectedTemplateFromId, site).getTemplateName();
            controller.saveTemplateChanges(postSaveCallback);
        }

        /**
         * Creates a template for the selected site from an URL.
         */
        function createFromUrl()
        {
            // Ask the controller to create the new template from URL and assign it to the
            // corresponding site
            var url = dialog.find('input[name="url"]').val().trim();

            // If the URL entered lacks the 'http(s)://' string prefix, append 'http://'
            if(! (/^(https?):\/\//i).test(url)) {
                url = 'http://' + url;
            }

            $.PercSiteTemplatesController(false).createTemplateFromUrl(
                url,
                // Error callback in case something went wrong: show an errod dialog
                function createFromUrlDialogErrorCallback(result)
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                }
            );
        }

        ////////////////////////////////////////////////////////////
        // PercAddTemplateDialog function execution starts from here
        ////////////////////////////////////////////////////////////
        createAddTemplateDialog(okCallback);
    };
})(jQuery);
