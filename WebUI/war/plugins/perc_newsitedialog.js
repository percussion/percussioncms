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
 * Site creation wizard.
 */
var $perc_newSiteDialogLayout;

(function($)
{
    var dialog;
    $.perc_createNewSiteDialog = function(cbh)
    {
        var v = null;
        dialog = $("<div></div>").perc_wizard(
            {
                steps: 2,
                modal: true,
                resizable: false,
                templateUrl: "/Rhythmyx/cm/app/dialogs/perc_newSiteDialog.jsp",
                title: I18N.message("perc.ui.newsitedialog.title@Create Site"),
                height: 'auto',
                width: 725,
                open: function()
                {
                    var siteNameField = $("#sitename"),
                        templateNameField = $("#templatename"),
                        urlField = $("#url"),
                        defaultSiteOption = $('input#type_url');

                    // This check is performed because the dialog is never removed from the DOM. If the
                    // dialog doesn't exist, initialize it. Else clear its fields.
                    if (!$perc_newSiteDialogLayout)
                    {
                        // Set the "exist dialog" flag
                        $perc_newSiteDialogLayout = true;

                        // Apply filters to fields
                        $.perc_filterField(siteNameField, $.perc_textFilters.HOSTNAME);
                        $.perc_filterField(templateNameField, $.perc_textFilters.URL);

                        // If the URL checkbox is unselected, disable the sitename field
                        $('input[type=radio]').on("change",function()
                        {
                            // If the selected radio button is not the one for the
                            // URL option, make the URL field read only
                            var radioSelected = $(this);
                            if (radioSelected.attr('id') !== "type_url")
                            {
                                urlField
                                    .attr('readonly', 'readonly')
                                    .attr('disabled', 'true');
                                clearValidationErrorMessage(urlField);
                            }
                            else
                            {
                                urlField
                                    .removeAttr('readonly')
                                    .removeAttr('disabled');
                            }
                        });

                        // Create a themplate selector using images: initialize the element
                        var $tempList = $('#perc_templateList').perc_imageselect(
                            {
                                hardSelect: false,
                                onSelect: function(val, url)
                                {
                                    $('#perc_selectedTemplate img').remove();
                                    $('#perc_selectedTemplate').html("<img src='" + url + "'/>" + "<br/><span>" + val.split(".")[2] + "</span>");
                                    $('#selectedtemplate').val(val);
                                }
                            });
                        // We have to get the images for the perc_imageselect plugin
                        _loadTemplateList();
                        $("#perc-select-template-type").on("change",function(){
                            if($(this).val() === "base"){
                                $("#perc-base-template-lib").show();
                                $("#perc-resp-template-lib").hide();
                            }
                            else{
                                $("#perc-base-template-lib").hide();
                                $("#perc-resp-template-lib").show();
                            }
                        });
                        // Initialize validation for form
                        v = $("#perc_newSiteDialogForm").validate(
                            {
                                errorClass: "perc_field_error",
                                validClass: "perc_field_success",
                                wrapper: "p",
                                validateHiddenFields: false,
                                debug: false,
                                rules: _getValidationRules(),
                                messages: _getValidationMessages(),
                                showErrors: function() {
                                    if (this.pendingRequest < 1)
                                    {
                                        this.defaultShowErrors();
                                        var formDialog = $("#perc_newSiteDialogForm");
                                        formDialog.find('.perc_dialog_field.perc_field_success')
                                            .prevAll(".perc_dialog_label").removeClass("perc_dialog_label_error");
                                        formDialog.find('.perc_dialog_field.perc_field_error')
                                            .prevAll(".perc_dialog_label").addClass("perc_dialog_label_error");
                                        $(this.currentElements).removeClass(this.settings.errorClass);
                                    }
                                }
                            });

                    }
                    else
                    {
                        _clearFieldValues();
                    }

                    /////////////////////////////////////////
                    // Common code after the dialog is opened
                    /////////////////////////////////////////

                    // Clear & reset fields validation errors
                    urlField.prop('readonly', false).prop('disabled', false);
                    clearValidationErrorMessage(siteNameField);
                    clearValidationErrorMessage(urlField);

                    // Initialize the radio button groupt ot its default value (URL)
                    defaultSiteOption.attr('checked', true);

                },
                onOk: function()
                {
                    _onOK(cbh);
                },
                onNext: _onNext,
                // We specify a validation function/method that calls the corresponding method from the
                // the validate library
                onValidate: function()
                {
                    return v.form();
                }
            });
        return dialog;
    };

    /**
     * Override default next behavior, because we have to check in the first step the URL
     * option has been selected. If the user selected the URL radio in the first step, call the
     * corresponding ervice to create a site based on the given URL
     * @param function callbackHandlerOnCreate function invoked on successful site creation
     */
    function _onNext(callbackHandlerOnCreate)
    {
        var sitenameDropdown = dialog.find('select[name=sitename-select]');
        if (sitenameDropdown.length > 0){
            dialog.find('input[name=sitename]').val(sitenameDropdown.val());
        }
        var siteNameField = $("#sitename"),
            urlField = $("#url"),

            // Will hold the information needed to redirect to the Design manager
            memento = {
                templateName: undefined,
                templateId: undefined,
                pageId: undefined
            },

            // Wil hold the new site path (needed to invoke services)
            newSitePath,
            continueToNextStep = true;

        /**
         * Shows an error dialog. Will be set as the error callback (behavior) for each of the
         * service calls.
         * @param data String Message that the error dialog will show.
         */
        function errorCallbackFallback(data)
        {
            $.unblockUI();
            $.perc_utils.alert_dialog(
                {
                    title: I18N.message("perc.ui.publish.title@Error"),
                    content: data
                });
        }

        /**
         * Callback for site import/creation service;
         * @param status String PercServiceUtils
         * @param siteData Object data return by the PercSiteService.createSiteFromUrl service
         */
        function siteImportCallback(status, siteData)
        {
            if (status !== $.PercServiceUtils.STATUS_SUCCESS)
            {
                errorCallbackFallback(siteData);
                return;
            }

            // No unblockUI is needed because we are redirecting to a different page, unless we
            // have an error in any service call

            // Betgin setting the memento file used to redirect to the Design view
            memento.templateName = siteData.Site.templateName;
            memento.tabId = "perc-tab-layout";
            newSitePath = $.perc_paths.SITES_ROOT + '/' + siteData.Site.name;

            // We need to call 2 services to get the pageId and templateId
            $.PercSiteService.getTemplates(siteData.Site.name, getTemplateIdCallback);
        }

        /**
         * Callback for site import/creation service;
         * @param status String PercServiceUtils
         * @param siteData Object data return by the PercSiteService.getTemplates service
         */
        function getTemplateIdCallback(status, teamplatesData)
        {
            if (status !== $.PercServiceUtils.STATUS_SUCCESS)
            {
                errorCallbackFallback(teamplatesData);
                return;
            }

            memento.templateId = teamplatesData.TemplateSummary[0].id;

            $.PercPathService.getFolderPathItem(newSitePath, getPageIdCallback);
        }

        /**
         * Callback for site import/creation service;
         * @param status String PercServiceUtils
         * @param siteData Object data return by the PercPathService.getFolderPathItem service
         */
        function getPageIdCallback(status, foldersData)
        {
            if (status !== $.PercServiceUtils.STATUS_SUCCESS)
            {
                errorCallbackFallback(foldersData);
                return;
            }

            var pathItem;

            for (var i = 0; i < foldersData.PathItem.length; i++) {
                if (foldersData.PathItem[i].category === 'LANDING_PAGE')
                {
                    pathItem = foldersData.PathItem[i];
                    i = foldersData.PathItem.length;
                }
            }

            memento.pageId = pathItem.id;

            // Finally we have all the things we want in the memento, retrieve the path (URL param)
            // and invoke the navigation manager
            var querystring = $.deparam.querystring();
            $.PercNavigationManager.goToLocation(
                $.PercNavigationManager.VIEW_DESIGN,
                fields.name,
                null,
                null,
                null,
                pathItem.path,
                null,
                memento
            );
        }

        /////////////////////////////////////////
        // _onNext function execution begins here
        /////////////////////////////////////////
        // We use :checked inside a filter to fix an IE compatibility issue
        if (dialog.find('input[type=radio]').filter(':checked').attr('id') === "type_url")
        {
            var fields = {
                name: siteNameField.val().trim(),
                baseUrl: urlField.val().trim()
            };

            // If the URL entered lacks 'http(s)://' prefix, append 'http://'
            if(! (/^(https?):\/\//i).test(fields.baseUrl)) {
                fields.baseUrl = 'http://' + fields.baseUrl;
            }

            // We don't have to go to the next step, an error dialog could appear
            continueToNextStep = false;

            // Close the dialog to simulate wizard
            $(".ui-dialog-titlebar .ui-icon-closethick").trigger("click");

            // Open the Import Progress dialog
            $.PercImportProgressDialog({
                showVideo: true,
                backgroundRefreshCallback: function()
                {
                    // When the import progress dialog closes and there is an import in progress,
                    // refresh the finder whenever the process finishes
                    $.perc_finderInstance.refresh();
                },
                onSuccessCallback: function(importData)
                {
                    // The new template data will be available in importData, use it to redirect
                    // to the template editor
                    siteImportCallback('success', importData);
                },
                startProgressCallback: function(callbackJobIdHandler)
                {
                    $.PercSiteService.createSiteFromUrlAsync(fields, function(status, jobId) {
                        // callbackJobIdHandler is specified by the Import Progress dialog
                        callbackJobIdHandler(status, jobId);
                    });
                },
                pollingProgressCallback: $.PercSiteService.createSiteFromUrlStatus,
                importResultCallback: $.PercSiteService.createSiteFromUrlResult
            });
        }

        return continueToNextStep;
    }

    // Invoked before submitting the form
    function _onOK(cbh)
    {
        var fields = _getFieldValues();
        fields.selectedtemplate = $("#perc-select-template-type").val()==="base"?fields.perc_selected_basetemplate:fields.perc_selected_resptemplate;
        if (fields.selectedtemplate.length === 0)
        {
            $.perc_utils.alert_dialog(
                {
                    title: I18N.message("perc.ui.publish.title@Error"),
                    content: I18N.message("perc.ui.new.site.dialog@Select Template")
                });
            return;
        }

        if (fields.templatename === null || fields.templatename.trim() === '')
        {
            $.perc_utils.alert_dialog(
                {
                    title: I18N.message("perc.ui.publish.title@Error"),
                    content: I18N.message("perc.ui.new.site.dialog@Template Name")
                });
            return;
        }

        // Set the  basic information we are going to send to the site creation service
        var fielddata = {
            Site: {
                name: fields.sitename,
                label: fields.sitename,
                description: fields.description,
                homePageTitle: I18N.message("perc.ui.new.site.dialog@Home Page"),
                navigationTitle: I18N.message("perc.ui.new.site.dialog@Home Page"),
                baseTemplateName: fields.selectedtemplate,
                templateName: fields.templatename.trim()
            }
        };

        $.PercBlockUI();
        // Force the dialog close while blocking the UI
        $(".ui-dialog-titlebar .ui-icon-closethick").click();

        $.ajax(
            {
                url: $.perc_paths.SITE_CREATE + "/",
                dataType: "json",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify(fielddata),
                success: function(data, textstatus)
                {
                    // Redirect to architecture tab for the new site
                    // $.perc_redirect($.perc_paths.URL_ARCHITECTURE, {site: fields.sitename});

                    // Invoke the callback handler with the sitename as a parameter and unblock UI
                    cbh(fields.sitename);
                    $.unblockUI();
                },
                error: function(request, textstatus, error)
                {
                    // If something went wrong unblock UI and show an error dialog
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                    $.unblockUI();
                    $.perc_utils.alert_dialog(
                        {
                            title: I18N.message("perc.ui.publish.title@Error"),
                            //TODO: Find in Java code for I18N
                            content: defaultMsg
                        });
                }
            });
    }

    // Clear/reset each field in the dialog. It has to be applied because the dialog is
    // never removed from the DOM.
    // Invoked during dialog open/initialization.
    function _clearFieldValues()
    {
        $("#perc_newSiteDialog .perc_dialog_field").each(function()
        {
            $(this).val('');
        });
        $('#perc_selectedTemplate').html('');
        $(".perc_imageselect_selected").removeClass("perc_imageselect_selected");
    }

    function _getFieldValues()
    {
        var results = new Object();
        $("#perc_newSiteDialog .perc_dialog_field").each(function()
        {
            results[this.id] = $(this).val();
        });
        return results;
    }

    function _loadTemplateList()
    {
        var baseTemplates = $("#perc-base-template-lib").PercScrollingTemplateBrowser({isBase:true,width:590, baseType: "base", hiddenFieldId:"perc_selected_basetemplate"});
        var respTemplates  = $("#perc-resp-template-lib").PercScrollingTemplateBrowser({isBase:true,width:590, baseType: "resp", hiddenFieldId:"perc_selected_resptemplate"});
    }
    /**
     * Retrieves the existing sites from the response object and compares the value against
     * each one of those (case insensitive).
     * @param validator - unused
     * @param element - unused
     * @param value {String} - the name of the site to check, must be a non-emtpy string.
     * @param response {} - object containing an array of existing site summaries
     * @return true if the name does not conflict w/ any existing name, false otherwise
     */
    function _validateUniqueSiteNameHandler(validator, element, value, response)
    {
        //we need to add an assertion 'framework' and check them here
        for (i = 0; i < response.SiteSummary.length; i++)
        {
            if ((response.SiteSummary[i].name + "").toLowerCase() === value.toLowerCase())
            {
                return false;
            }
        }
        return true;
    }

    function _getValidationRules()
    {
        var rules = {
            sitename: {
                required: true,
                perc_remote: {
                    url: $.perc_paths.SITES_ALL,
                    contentType: "application/json",
                    type: "GET",
                    dataType: "json",
                    handler: _validateUniqueSiteNameHandler
                }
            },
            url : {
                // URL field is required only in the (radio) URL type was selected
                required: 'input[type=radio]#type_url:checked',
                maxlength: 2000,
                noBinary : "noBinary"
            },
            description: {
                maxlength: 255
            },
            selectedtemplate: {
                required: true
            }
        };
        return rules;
    }

    function _getValidationMessages()
    {
        var messages = {
            sitename: {
                required: I18N.message("perc.ui.new.site.dialog@Site Name Req"),
                perc_remote: I18N.message("perc.ui.new.site.dialog@Unique Name Req")
            },
            url: {
                required: I18N.message("perc.ui.new.site.dialog@URL Req"),
                noBinary: I18N.message("perc.ui.new.site.dialog@URL Format Req")
            },
            templatename: {
                required: I18N.message("perc.ui.new.site.dialog@Template Req"),
                perc_remote: I18N.message("perc.ui.new.site.dialog@Template Unique Req")
            },
            selectedtemplate: {
                required: I18N.message("perc.ui.new.site.dialog@Template Selected Req")
            }
        };
        return messages;
    }

    /**
     * Removes the validation message of the field, if it has one.
     * @param field Object HTML field element wrapped with jQuery.
     */
    function clearValidationErrorMessage(field)
    {
        field.prevAll(".perc_dialog_label").removeClass("perc_dialog_label_error");
        var errorMsg = field.next();
        if (errorMsg.find('.perc_field_error').length > 0)
        {
            field.removeClass('perc_field_error');
            errorMsg.remove();
        }
    }
})(jQuery);
