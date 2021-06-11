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
 * Form view class.
 */
(function($)
{
    var prefsDialog = null;
    $(document).ready(function(){
        if($(".PercFormWidget").length > 0)
        {
            initializeForm();
        }
        else if ($(".PercFormWidgetReadOnly").length > 0)
        {
            renderReadOnlyForm();
        }
    });
    function initializeForm()
    {
        var formFieldName = $(".PercFormWidget").attr("id");
        var formDataStr = $("input[name='" + formFieldName + "']").val().trim();
        var formData = null;
        if(formDataStr.length>0)
            formData = JSON.parse(formDataStr);

        var editorHtml = $.PercFormController().getFormEditor(formData);
        var metaHtml = $.PercFormController().getFormMetaDataEditor(formData);
        $(".perc-form-fields-col").append(editorHtml);
        $("#perc-metadata-content").html(metaHtml);
        $(".field-editor").append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img src='../rx_resources/widgets/form/images/edit.png' alt='Edit'></span><span class='delete-field'><img src='../rx_resources/widgets/form/images/delete.png' ></span></div>");

        // Position the Form controls menu button
        $("#perc-form-control-wrapper").prepend($("#perc-form-top-row"));

        //Attach event to Edit button
        $(document).on("click", ".toggle-editor",function(){

            // Deactivate old extended editor, since we can only have one active at a time.
            var extEditorElem = $("." + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS);
            if (extEditorElem.length)
                deactivateEditor(extEditorElem);
            $(".defaultFocus").trigger("focus");

            var parent = $(this).parent().parent();
            var newElem = $.PercFormController().toggleFieldEditor(parent);
            addConfig(newElem);
            newElem = parent.parent().replaceWith(newElem);
            $(".defaultFocus").trigger("focus");
            var fieldType = parent.attr('type');
            if(fieldType === 'PercDateControl'){
                attachFormDatepicker();
            }
            if(fieldType === 'PercTextFieldControl'){
                tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
            }


            if($.browser.msie){
                $("input[type = 'text']").css('height', '11px');
            }else{
                $("input[type = 'text']").css('height', 'auto');
            }
        });

        //Attach event to Delete button
        $(document).on("click",".delete-field", function(){
            if(tinymce.EditorManager.get('elm1') && $(this).parent().parent().children('#elm1').length > 0){
                tinymce.EditorManager.execCommand('mceRemoveEditor', true, 'elm1');
            }
            $(this).parent().parent().parent().remove();
        });

        //Attach event to Configure button
        $(document).on("click",".toggle-configure", function(){
            var controlEl = $(this).parent().parent();
            var control = $.PercFormController().getControl(controlEl);
            openPrefsDialog(controlEl, control().getAvailablePrefs());
        });

        //Add Date field            
        $(".form-date-field-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercDateControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            $("iframe").scrollTop(20000);
            newElem.find(".toggle-editor").trigger("click");
            attachFormDatepicker();
        });

        //Add Textarea field
        $(".form-textarea-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercTextareaFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
        });

        //Add Check boxes
        $(".form-check-boxes-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercCheckBoxControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
        });

        //Add Radio buttons
        $(".form-radio-buttons-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercRadioControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
        });

        //Add Submit Button
        $(".form-submit-buttons-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercSubmitButtonControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
        });

        //Add Entry field
        $(".form-entry-field-label").on("click", function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercEntryFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
        });

        // Add Drop down field 
        $(".form-drop-down-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercDropDownControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            $("iframe").scrollTop(20000);
            newElem.find(".toggle-editor").trigger("click");
        });

        // Add Data Drop down field 
        $(".form-data-drop-down-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercDataDropDownControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            $("iframe").scrollTop(20000);
            newElem.find(".toggle-editor").trigger("click");
        });

        //Add Text field
        $(".form-text-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercTextFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
            tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
        });

        //Add Hidden field
        $(".form-hidden-field-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercHiddenFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
            tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
        });

        //Add Honeypot field
        $(".form-perc-honeypot-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercHoneypotFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
            tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
        });

        //Add recaptcha field
        $(".form-perc-recaptcha-label").on("click",function(){
            var newElem = $.PercFormController().getNewFieldEditor("PercRecaptchaFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
            tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
        });

        //Toggle the control menu button on click event 
        $("#perc-control-menu-button").on("click",function(){
            $("#perc-control-menu-wrapper").show();
            $(this).hide();
        });

        //Add mouse out and over event to control menu items        
        $("#perc-control-menu-wrapper").on("mouseenter",function(){})
            .on("mouseleave",function() {
            $("#perc-control-menu-button").show();
            $(this).hide();
        });

        //Attach a Jquery Datepicker  to Date field
        function  attachFormDatepicker(){
            $( ".form-datepicker" ).datepicker({
                showOn: "button",
                buttonImage: "../rx_resources/widgets/form/images/calendar.gif",
                dateFormat: "d M, yy",
                buttonImageOnly: true,
                buttonText: ""
            });
        }
        //Make fields draggable and sortable.
        $( "#perc-form-dnd-fields").sortable({
            placeholder: "ui-state-highlight",
            axis:"y",
            distance: 20,
            opacity: 0.7,
            helper: function(ev,ele) {
                var r = ele.clone();
                r.find('input[type=radio].').attr('name','rename');
                return r;
            },
            snap:true,
            start: function(e, ui) {
                // removes tinymce ok from textarea
                if(tinymce.EditorManager.get('elm1')){
                    tinymce.EditorManager.execCommand('mceRemoveEditor', true, 'elm1');
                }
            },
            beforeStop: function(e,ui) {
                // add tinymce to textarea
                tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
                $(this).sortable( "refresh" );
            },
            containment: $( "#perc-form-dnd-fields").parents('#perc-content-edit-content')
        });

        //Change the control style on mouse over and out
        $(".perc-control-label").on("mouseenter", function() {
            var bgimage = $(this).css('background-image');
            bgimage = bgimage.replace(".png", "-over.png");

            $(this).css({'background-color': '#247297',
                'color':'83A9BB',
                'cursor': 'pointer',
                'background-image':bgimage,
                'border-bottom':'1px solid #99C4D8'});
        }).on("mouseleave",function(){
            var bgimage = $(this).css('background-image');
            bgimage = bgimage.replace("-over.png", ".png");
            $(this).css({'background-color': '#3288B0',
                'opacity':'1',
                'color':'ffffff',
                'cursor': 'pointer',
                'background-image':bgimage,
                'border-bottom':'1px solid #99C4D8'});
        });

        //handle the uniqueness of the form name here.
        if($("div[type='sys_error']").find("input[name='sys_title']").length > 0 || $("div[type='sys_error']").find("input[name='formuniquenamevalidator']").length)
        {
            var errorLabelHtml = '<br/><label style="display: block;" generated="true" for="sys_title" class="perc_field_error">This field cannot be empty, must be unique within the system.</label>';
            $("#perc-form-name").after(errorLabelHtml);
            //As all the form fields rendered using the form editor, simply hiding all errors form fields here.
            $("div[type='sys_error']").hide();
        }
        //Handle the success page browse button click
        $("#perc-formbuild-success-url-browse").on("click",function(){
            var dlgTitle = "Select Success Page";
            var inputElemId = "perc-formbuild-success-url";
            handleBrowseButtonClick(dlgTitle, inputElemId );
        });
        //Handle the error page browse button click
        $("#perc-formbuild-error-url-browse").on("click",function(){
            var dlgTitle = "Select Error Page";
            var inputElemId = "perc-formbuild-error-url";
            handleBrowseButtonClick(dlgTitle, inputElemId );
        });

        //This is a dummy field to validate the form uniquename
        $("input[name='formuniquenamevalidator']").parent("div").hide();
        //Add the Form pre submit handler
        window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(updateFormFields);
        // Meta data
        $("#perc-content-edit-metadata-link").hide();
        $("#perc-form-metadata-content").hide();
        $("#perc-form-edit-metadata-link").on("click",function () {
            $("#perc-form-metadata-content").toggle();
            $("#perc-form-edit-metadata-link").toggleClass('perc-form-spacer');
            $("#perc-form-edit-metadata-link").toggleClass('perc-form-meta-tab-open');
        });

        if(formData === null) {
            var newElem = $.PercFormController().getNewFieldEditor("PercTextFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            $("div.form-widget-label").text('Descriptive text can be added by selecting the edit icon in the upper right.');
            newElem = $.PercFormController().getNewFieldEditor("PercEntryFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            $("div[name='defaultValue']").text('Default text can be added to any text box.');
             newElem = $.PercFormController().getNewFieldEditor("PercSubmitButtonControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
        }
    }

    function deactivateEditor(extEditorElem)
    {
        var checkEditorExt = extEditorElem.parent().children();
        var newElem = $.PercFormController().toggleFieldEditor(checkEditorExt);
        addEvents(newElem);
        checkEditorExt.parent().replaceWith(newElem);
    }

    // Add controls (Delete and Extended Edit Mode)to field
    function addEvents(elem)
    {
        elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img src='../rx_resources/widgets/form/images/edit.png' ></span><span class='delete-field'><img src='../rx_resources/widgets/form/images/delete.png'></span></div>");
    }

    /**
     * Add controls (Delete and Configuration)to field
     */
    function addConfig(elem)
    {
        if ($.PercFormController().getControl($(elem).children().eq(0))().getAvailablePrefs().length > 0)
            configButtonHtml = "<span class='toggle-configure'><img src='../rx_resources/widgets/form/images/configure.png' alt='Configure'></span>";
        else
            configButtonHtml = "<span class='toggle-configure-disabled'><img src='../rx_resources/widgets/form/images/configure2.png' alt='Configure'></span>";
        elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'>" + configButtonHtml + "<span class='delete-field'><img src='../rx_resources/widgets/form/images/delete.png' alt='Delete'></span></div>");
    }

    // If any of the required fields value is empty it will show an inline error message and returns false.
    function _showFieldErrorMessage(reqFieldIds){
        $('.perc_field_error').remove();
        var errorLabelHtml = '<label style="display: block;margin-left: 10px;" generated="true" for="formerrorpage" class="perc_field_error">This is a required field.</label>';
        var isFieldError = false;
        $(reqFieldIds).each(function(){
            var value = $("#" + this).val();
            if(value.trim()==='')
            {
                isFieldError = true;
                $("#" + this).after(errorLabelHtml);
            }
        });
        //Open the Meta-data section if there is any error
        if(isFieldError && $("#perc-form-edit-metadata-link").hasClass('perc-form-spacer')) {
            $("#perc-form-edit-metadata-link").trigger("click");
        }
        return isFieldError;
    }

    function updateFormFields()
    {
        var extEditorElem = $("." + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS);
        if (extEditorElem.length)
            deactivateEditor(extEditorElem);
        $(".defaultFocus").trigger("focus");

        var reqFieldIds = ["perc-formbuild-success-url","perc-formbuild-error-url"];

        if($("#perc-form-metadata-email-notification").is(':checked'))
        {
            reqFieldIds.push("perc-formbuild-mail-to");
            reqFieldIds.push("perc-formbuild-mail-subject");
        }
        else
        {
            $("#perc-formbuild-mail-to").val("");
            $("#perc-formbuild-mail-to-paired-unencrypted").val("");
            $("#perc-formbuild-mail-to-paired-encrypted").val("");
            $("#perc-formbuild-mail-subject").val("");
            $("#perc-formbuild-mail-subject-paired-unencrypted").val("");
            $("#perc-formbuild-mail-subject-paired-encrypted").val("");
        }

        if(_showFieldErrorMessage(reqFieldIds))
            return false;

        var success = false;
        success = $.PercFormController().updateEncryptedFields(reqFieldIds, true);

        var formData = $.PercFormController().getFormData($("."+$.PercFormConstants.FORM_CLASS), $("."+$.PercFormConstants.FORM_METADATA_CLASS) );
        var formConfig = formData.config;
        //Set the title, name and description
        $("#perc-content-edit-sys_title").val(formConfig.name);
        $("#perc-content-edit-formtitle").val(formConfig.title);
        $("#perc-content-edit-description").val(formConfig.description);
        //Set the formData and rendered form.
        $("#perc-content-edit-formdata").val(JSON.stringify(formData));
        var script = "<script id='form-script'>window.addEventListener('DOMContentLoaded', function() {jQuery(document).ready(function(){jQuery('#" + formConfig.name + "').validate()});});</script>";
        if( $.PercFormVariables.IsRecaptchaEnabled === 1){
            script = script +  '<script src="https://www.google.com/recaptcha/api.js" async defer></script>';
        }
        $("#perc-content-edit-renderedform").val(script + $("<div/>").append($.PercFormController().getRenderedForm(formData)).html().replace(/<input\s+([^>]*?)\s*>/ig, "<input $1 />")); // @TODO: This *will* break once we're serving as application/xhtml+xml.  Get rid of the .replace to fix.
        return success;
    }

    //Generate the form for Read-only mode
    function renderReadOnlyForm()
    {
        var formFieldName = $(".PercFormWidgetReadOnly").attr("id");
        var formDataStr = $("input[name='" + formFieldName + "']").val();
        var formData = null;
        formDataStr = formDataStr.trim();
        if(formDataStr.length>0)
            formData = JSON.parse(formDataStr);

        var editorHtml = $.PercFormController().getFormEditor(formData);
        var formHeader = $("<label id = 'perc-form-label-field-name' class = 'form-widget-label' >Form title:</label> <br />" +
            "<div class = 'perc-form-header-datadisplay' type = 'text' id = 'perc-form-title' size = '50'></div><br />" +
            "<label id = 'perc-form-label-description' class = 'form-widget-label perc-required-field'>Form name:</label> <br />" +
            "<div class = 'perc-form-header-datadisplay' type = 'text' id = 'perc-form-name' size = '50'></div><br />");
        var metaHtml = $.PercFormController().getFormMetaDataEditor(formData);

        $(".perc-form-fields-col").append(editorHtml);
        $(".field-editor-basic").parent().toggleClass('perc-form-field-wrapper');
        $("#perc-metadata-content").html(metaHtml).find("input, textarea, radio").attr("readonly","readonly");
        var saveToUrl = $("#perc-form-metadata-save-to-url-text").val();
        var metaDescriptiton = $("#perc-form-metadata-description").val();
        var successUrl = $("#perc-formbuild-success-url").val();
        var errorUrl = $("#perc-formbuild-error-url").val();
        var notificationFlag = $("#perc-form-metadata-email-notification").is(":checked");
        var mailTo = $("#perc-formbuild-mail-to").val();
        var mailSubject = $("#perc-formbuild-mail-subject").val();
        var readOnlyMetadata = $("<div id='perc-form-metadata-panel'>" +
            "<div id='perc-form-edit-metadata-link' class='perc-form-spacer'>" +
            "<span id='perc-content-edit-metadata-icon'></span>" +
            "<span>Meta-data</span>" +
            "</div>" +
            "<div id='perc-form-metadata-content' class='table-layout'>" +
            "<div>" +
            "<div>" +
            "<label for='perc-formbuild-success-url'>On success redirect to:</label>" +
            "</div>" +
            "<div>" +
            "<div style = 'position:relative'><div type='text' id='perc-formbuild-success-url-readonly'>" + successUrl + "</div>" +
            "<input type='hidden' id='perc-formbuild-success-url-paired-unencrypted' />" +
            "<input type='hidden' id='perc-formbuild-success-url-paired-encrypted' /></div>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<label for='perc-formbuild-error-url'>On error redirect to:</label>" +
            "</div>" +
            "<div>" +
            "<div style = 'position:relative'><div type='text' id='perc-formbuild-error-url-readonly'>" + errorUrl + "</div>" +
            "<input type='hidden' id='perc-formbuild-error-url-paired-unencrypted' />" +
            "<input type='hidden' id='perc-formbuild-error-url-paired-encrypted' /></div>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<label class='form-widget-label' id='perc-form-label-description' " +
            "for='perc-form-metadata-description'>Description:</label> " +
            "</div>" +
            "<div>" +
            "<div id='perc-form-meta-readonly-description' name=" +
            "'perc-form-description' class='perc-form-meta-textarea'>" + metaDescriptiton + "</div>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<label class='form-widget-label'>Save form data to:</label>" +
            "</div>" +
            "<div>" +
            "<div class='perc-form-meta-radio-option table-layout'>" +
            "<div>" +
            "<div>" +
            "<input type='radio' name='perc-save-to' id='perc-form-metadata-save-to-local' " +
            "class='perc-form-metaradio' value='LocalServer' checked='checked' />" +
            "</div>" +
            "<div>" +
            "<label class='form-widget-label' for='perc-form-metadata-save-to-local'>Local " +
            "server</label>" +

            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<input type='radio' name='perc-save-to' class='perc-form-metaradio' id=" +
            "'perc-form-metadata-save-to-url' value='ExternalServer' />" +
            "</div>" +
            "<div>" +
            "<label class='form-widget-label' for='perc-form-metadata-save-to-url'>URL:" +
            "</label>" +
            "</div>" +
            "<div id='perc-form-negative-expander'>" +
            "<div class='perc-form-datadisplay' name='perc-save-to-url' type='text' " +
            "id='perc-form-metadata-save-to-url-text-readonly'>" + saveToUrl + "</div>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<input type='checkbox' name='perc-email-notification' id='perc-form-metadata-email-notification' " +
            "class='perc-form-metaradio'/>" +
            "<label class='form-widget-label' for='perc-form-metadata-email-notification'>Notify on form submission</label>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<label for='perc-formbuild-mail-to' class = 'form-widget-label perc-required-field'>Mail to:</label>" +
            "</div>" +
            "<div>" +
            "<div style = 'position:relative'><div type='text' id='perc-formbuild-mail-to-readonly'>" + mailTo + "</div></div>" +
            "</div>" +
            "</div>" +
            "<div>" +
            "<div>" +
            "<label for='perc-formbuild-mail-subject' class = 'form-widget-label perc-required-field'>Mail subject:</label>" +
            "</div>" +
            "<div>" +
            "<div id='perc-form-meta-readonly-mail-subject' name=" +
            "'perc-form-mail-subject' class='perc-form-meta-textarea'>" + mailSubject + "</div></div>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</div>");
        $("#perc-metadata-content").html(readOnlyMetadata).find('input').attr("disabled","disabled");
        $("#perc-form-help-text").hide();
        $("#perc-form-metadata-email-notification").prop("checked", notificationFlag);

        var formTitle = $("#perc-form-title").val();
        var formName = $("#perc-form-name").val();
        $(".perc-form-header").html(formHeader);
        $("#perc-form-title").text(formTitle);
        $("#perc-form-name").text(formName);
        //This is a dummy field to validate the form uniquename
        $("input[name='formuniquenamevalidator']").parent("div").hide();

        // Meta data
        $("#perc-content-edit-metadata-link").hide();
        $("#perc-form-metadata-content").hide();
        $("#perc-form-edit-metadata-link").on("click",function () {
            $("#perc-form-metadata-content").toggle();
            $("#perc-form-edit-metadata-link").toggleClass('perc-form-spacer');
            $("#perc-form-edit-metadata-link").toggleClass('perc-form-meta-tab-open');
        });
    }
    function openPrefsDialog(control, preferences, values)
    {
        if(prefsDialog == null)   // Only create dialog once.
        {
            prefsDialog = $('<div id="perc-prefs-dialog"></div>')
                .html('<div id="perc-prefs-dialog-container"></div>')
                .dialog({
                    autoOpen: false,
                    title: 'Configure Form Field',
                    dialogClass: 'perc-prefs-dialog',
                    width: 500,
                    minWidth: 200,
                    modal: true,
                    zIndex: 50000,
                    buttons:[ {text:"Apply",
                        click:function(){

                        }
                    },
                        {
                            text:"Close",
                            click:function(){
                                $(this).dialog("close");
                            }

                        }]
                });
            $(".ui-button-text-only").attr('id','perc-field-prefs-apply');
            $("#perc-field-prefs-apply").css({"margin-right": "13px"});
        }

        // Get the values that the form control has for all the preferences
        var preferenceVals = control.data('preferences');
        if(typeof(preferenceVals) != 'object')
            preferenceVals = {};

        // Add each preference field to the dialog setting, with the corresponding value
        var container = prefsDialog.find("#perc-prefs-dialog-container");
        container.children().remove();
        for(let i = 0; i < preferences.length; i++)
        {
            preferences[i].pref.addControl(container, preferenceVals[preferences[i].pref.name], preferences[i].defaults);
        }

        // Object that will store the preferences values once the Apply button is clicked
        var preferencesAfterOnApply = {};
        $('#perc-field-prefs-apply').on('click', function() {

            var isValid = true;
            for(i = 0; i < preferences.length; i++)
            {
                isValid = isValid?preferences[i].pref.validate(preferences[i].defaults):isValid;
            }
            if(!isValid)
                return;
            // variable that says if any preference validation on the dialog failed
            var onApplySuccessful = true;
            for(i = 0; i < preferences.length && onApplySuccessful; i++)
            {
                preferencesAfterOnApply[preferences[i].pref.name] = preferences[i].pref.onApply(preferenceVals,  preferences[i].defaults);
                // Check that the onApply function was succesfull comparing with null
                onApplySuccessful = (preferencesAfterOnApply[preferences[i].pref.name] != null);
            }

            // We will save and close the dialog only if every onApply was successfull
            if (onApplySuccessful)
            {
                control.data('preferences', preferencesAfterOnApply);
                prefsDialog.dialog('close');
            }
        });
        prefsDialog.dialog('open');

        // Fix for horizontal scroll bar at bottom of iframe during overlay

        $(".ui-widget-overlay").css('width', '').css('max-width', '98%');

    }

    //Function to handle click on browse button.
    function handleBrowseButtonClick(dlgTitle, inputElemId)
    {
        $.perc_browser
        ({
            on_save: function(spec, closer, show_error)
            {
                var pagePath = spec.path;
                pagePath = pagePath.replace("/Sites/", "");
                var chopStartPosition = pagePath.indexOf("/");
                pagePath = pagePath.substring(chopStartPosition);
                $("#" + inputElemId).val(pagePath).trigger('blur');

                closer();
            },
            new_asset_option: false,
            selectable_object: "leaf",
            new_folder_opt: false,
            displayed_containers: "Sites",
            asset_name: I18N.message( "perc.ui.saveasdialog.label@Selected Page:" ),
            title: dlgTitle,
            save_class: 'perc-save'
        });
    }

})(jQuery);