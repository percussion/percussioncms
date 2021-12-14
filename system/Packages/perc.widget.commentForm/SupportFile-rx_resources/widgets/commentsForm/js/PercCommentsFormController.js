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
 * Form controller class.
 */
(function($)
{
    /**
     * An object to hold the form constants.
     */
    $.PercFormConstants = {
        //CSS class names
        FORM_CLASS : 'form-editor',
        FIELD_CLASS : 'field-editor',
        FORM_CONFIG_CLASS : 'form-editor-config',
        FIELD_EDITOR_BASIC_CLASS : 'field-editor-basic',
        FIELD_EDITOR_EXT_CLASS : 'field-editor-ext',
        FIELD_EDITOR_LABEL_CLASS : 'field-label',
        FIELD_ROW_CLASS : 'field-row',
        FIELD_LABEL_CONTAINER_CLASS : 'field-label-container',
        FIELD_INPUT_CONTAINER_CLASS : 'field-input-container',
        LABEL_TOP : 'label-top',
        LABEL_SIDE : 'label-side',
        FORM_LABELLOCATION_SELECT_ID : 'form-labellocation-select',
        FORM_TITLE_CLASS : 'form-title',
        FORM_FIELD_WRAPPER : 'perc-form-field-wrapper',
        FORM_LABEL_CLASS : 'form-widget-label',
        INPUT_CLASS : 'perc-form-datadisplay',
        FORM_TITLE_LABEL_ID : 'perc-form-label-field-name',
        FORM_TITLE_INPUT_ID : 'perc-form-title',
        FORM_NAME_LABEL_ID : 'perc-form-label-description',
        FORM_NAME_INPUT_ID : 'perc-form-name',
        FORM_DND_FIELDS : 'perc-form-dnd-fields',

        //Field Controls
        FIELD_CONTROL_TITLE : 'PercTitleFieldControl',
        FIELD_CONTROL_USER : 'PercUserFieldControl',
        FIELD_CONTROL_EMAIL : 'PercEmailFieldControl',
        FIELD_CONTROL_HONEYPOT : 'PercHoneypotFieldControl',
        FIELD_CONTROL_TEXT : 'PercTextFieldControl',
        FIELD_CONTROL_TEXTAREA : 'PercTextareaFieldControl',
        FIELD_CONTROL_SUBMIT : 'PercSubmitButtonControl',
        FIELD_CONTROL_URL : 'PercURLFieldControl',
        PREFERENCES_KEY: 'preferences'
    };

    $.PercFormVariables = {
        LastRadioName : 'defaultName',
        LastCounterValue : 0,
        IsHoneypotEnabled : 0
    };

    $.PercFormFieldPref = {
        "REQUIRED": {
            "name": "required",
            "addControl": function(container, vals, defaults){
                var checked = "";
                if((typeof(vals) === 'object' && vals['perc-field-required']) || (defaults['DISABLED'] === "TRUE") || (vals === undefined && defaults['CHECKED'] === "TRUE"))
                {
                    checked = " checked='checked'";
                }
                var out = "";
                if(defaults['DISABLED'] === "TRUE"){
                    out = "<div class='perc-pref-entry'>" +
                        "<input type='checkbox' id='perc-field-required'" + checked +" DISABLED/>" + I18N.message("perc.ui.widgets.commentsForm@This Field Is Required") +
                        "</div>";
                } else {
                    out = "<div class='perc-pref-entry'>" +
                        "<input type='checkbox' id='perc-field-required'" + checked +"/>" + I18N.message("perc.ui.widgets.commentsForm@This Field Is Required") +
                        "</div>";
                }
                container.append(out);
            },
            "onApply": function(data, defaults){
                var checked = $('#perc-field-required:checked').val() !== undefined;
                data = {"perc-field-required": checked};
                return data;
            },
            "onRender": function(elem, vals, defaults){
                var name = elem.attr('name');
                if((typeof(vals) === 'object' && vals['perc-field-required']) || (vals === undefined && defaults['CHECKED'] === "TRUE") ||(defaults['DISABLED'] === "TRUE") )
                {
                    elem.addClass("required");
                    var elemParent = elem.parent().parent();
                    if(elem.attr('type') === 'checkbox'){
                        elemParent.parent().parent().find('.field-label-container').prepend("<span class = 'perc-form-error-asterisk'>*</span>");
                    }
                    else{
                        elemParent.find('.field-label-container').prepend("<span class = 'perc-form-error-asterisk'>*</span>");
                    }
                }
            }
        },
        "MAX_CHAR": {
            "name": "max_char",
            "addControl": function(container, vals, defaults){
                var checked = "";
                var max = "";
                if(typeof(vals) === 'object' && typeof(vals['perc-field-max-char-value']) !== 'undefined')
                {
                    max = vals['perc-field-max-char-value'];
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-max-char-value']) !== 'undefined')
                {
                    max = defaults['perc-field-max-char-value'];
                }
                var out = "<div class='perc-pref-entry'>" +
                    "Maximum characters" +
                    "<input type='text' class='perc-pref-text' value='" + max + "' id='perc-field-max-char-value'/>" +
                    "</div>";
                container.append(out);
            },
            "onApply": function(data, defaults){
                var newData = {};
                var checked = $('#perc-field-max-char-enabled:checked').val() !== undefined;
                var max = parseInt($('#perc-field-max-char-value').val());
                if(!isNaN(max) && max > 0)
                    newData = {"perc-field-max-char-enabled": checked, "perc-field-max-char-value": max};
                return newData;
            },
            "onRender": function(elem, vals, defaults){
                if(typeof(vals) == 'object'||typeof(defaults) == 'object')
                {
                    if(typeof(vals) == 'object' && typeof(vals['perc-field-max-char-value']) != 'undefined')
                    {
                        elem.attr("fieldMaxLength", vals['perc-field-max-char-value']);
                    }
                    else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-max-char-value']) != 'undefined')
                    {
                        elem.attr("fieldMaxLength", defaults['perc-field-max-char-value']);
                    }
                }
            }
        },
        "WIDTH": {
            "name": "width",
            "addControl": function(container, vals, defaults){
                var width = "";
                if(typeof(vals) == 'object' && typeof(vals['perc-field-width-value']) != 'undefined')
                {
                    width = vals['perc-field-width-value'];
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-width-value']) != 'undefined')
                {
                    width = defaults['perc-field-width-value'];
                }
                var out = "<div class='perc-pref-entry'>" +
                    "Width" +
                    "<input type='text' class='perc-pref-text' value='" + width + "' id='perc-field-width-value'/>px" +
                    "</div>";
                container.append(out);
            },
            "onApply": function(data, defaults){
                var newData = {};
                var width = parseInt($('#perc-field-width-value').val());
                if(!isNaN(width) && width >= 0)
                {
                    newData = {"perc-field-width-value": width};
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-width-value']) != 'undefined')
                {
                    newData = {"perc-field-width-value":  defaults['perc-field-width-value']};
                }
                return newData;
            },
            "onRender": function(elem, vals, defaults){
                var width = Number.NaN;
                if(typeof(vals) == 'object' &&
                    typeof(vals['perc-field-width-value']) != 'undefined' &&
                    vals['perc-field-width-value'] !== defaults['perc-field-width-value'])
                {
                    width = vals['perc-field-width-value'];
                }
                /*else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-width-value']) != 'undefined')
                {
                    width = defaults['perc-field-width-value'];
                }*/

                if(isNaN(Number(width)))
                {
                    elem.css("width", '');
                }
                else
                {
                    elem.css("width", width + "px");
                }
            }
        },
        "HEIGHT": {
            "name": "height",
            "addControl": function(container, vals, defaults){
                var height = "";
                if(typeof(vals) == 'object' && typeof(vals['perc-field-height-value']) != 'undefined')
                {
                    height = vals['perc-field-height-value'];
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-height-value']) != 'undefined')
                {
                    height = defaults['perc-field-height-value'];
                }
                var out = "<div class='perc-pref-entry'>" +
                    "Height" +
                    "<input type='text' class='perc-pref-text' value='" + height + "' id='perc-field-height-value'/>px" +
                    "</div>";
                container.append(out);
            },
            "onApply": function(data, defaults){
                var newData = {};
                var height = parseInt($('#perc-field-height-value').val());
                if(!isNaN(height) && height >= 0)
                {
                    newData = {"perc-field-height-value": height};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-height-value']) !== 'undefined')
                {
                    newData = {"perc-field-height-value":  defaults['perc-field-height-value']};
                }
                return newData;
            },
            "onRender": function(elem, vals, defaults){
                var height = Number.NaN;
                // If the height is the default value, we prefer to have the Theme package style it.
                // This prevents users from accidentally accepting the default width, and then having problems with custom style-sheets.

                if(typeof(defaults) !== 'object')
                    defaults = {};
                if(typeof(vals) === 'object' &&
                    typeof(vals['perc-field-height-value']) !== 'undefined' &&
                    vals['perc-field-height-value'] !== defaults['perc-field-height-value'])
                {
                    height = vals['perc-field-height-value'];
                }
                /*else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-height-value']) != 'undefined')
                {
                   height = defaults['perc-field-height-value'];
                }*/
                if(isNaN(Number(height)))
                {
                    elem.css("height", '');
                }
                else
                {
                    elem.css("height", height + 'px');
                }
            }
        },

        "CLASS": {
            "name": "class",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var classValue = "";
                if (typeof(vals) == 'object' && typeof(vals['perc-field-class-value']) != 'undefined')
                {
                    classValue = vals['perc-field-class-value'];
                }
                else if (typeof(defaults) == 'object' && typeof(defaults['perc-field-class-value']) != 'undefined')
                {
                    classValue = defaults['perc-field-class-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "Class <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + classValue + "' id='perc-field-class-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);
                $.perc_filterFieldText(container.find('#perc-field-class-value'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var classValue = $('#perc-field-class-value').val().trim();
                // Remove extra spaces between classes
                classValue = classValue.replace( /\s\s+/g, ' ' );

                if (classValue.length !== undefined)
                {
                    newData = {"perc-field-class-value": classValue};
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-class-value']) != 'undefined')
                {
                    newData = {"perc-field-class-value":  defaults['perc-field-class-value']};
                }

                return newData;
            },

            // Function invoked when the form control gets rendered
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var classValue;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' && typeof(vals['perc-field-class-value']) !== 'undefined' && vals['perc-field-class-value'] !== defaults['perc-field-class-value'])
                {
                    classValue = vals['perc-field-class-value'];
                }

                // Apply the class(es) to the field
                elem.addClass(classValue);
            },
            "validate":function(){
                return true;
            }
        },

        "NAME": {
            "name": "name",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var nameValue = "";
                if (typeof(vals) == 'object' && typeof(vals['perc-field-name-value']) != 'undefined')
                {
                    nameValue = vals['perc-field-name-value'];
                }
                else if (typeof(defaults) == 'object' && typeof(defaults['perc-field-name-value']) != 'undefined')
                {
                    nameValue = defaults['perc-field-name-value'];
                }

                var out = "<div class='perc-pref-entry'>" + I18N.message("perc.ui.widgets.commentsForm@Name") +
                    " <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + nameValue + "' id='perc-field-name-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);
                $.perc_filterFieldText(container.find('#perc-field-name-value'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var nameValue = $('#perc-field-name-value').val().trim();
                // Remove extra spaces between classes
                nameValue = nameValue.replace( /\s\s+/g, ' ' );

                if (nameValue.length !== undefined && nameValue.length > 0)
                {
                    newData = {"perc-field-name-value": nameValue};
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-name-value']) != 'undefined')
                {
                    newData = {"perc-field-name-value":  defaults['perc-field-name-value']};
                }

                return newData;
            },

            // Function invoked when the form control gets rendered
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var nameValue;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' && typeof(vals['perc-field-name-value']) != 'undefined' && vals['perc-field-name-value'] !== defaults['perc-field-name-value'])
                {
                    nameValue = vals['perc-field-name-value'];
                }

                // Apply the name to the field
                elem.attr('name', nameValue);
            },
            "validate":function(){
                return true;
            }
        },

        "TITLE": {
            "name": "title",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var titleValue = "";
                if (typeof(vals) == 'object' && typeof(vals['perc-field-title-value']) != 'undefined')
                {
                    titleValue = vals['perc-field-title-value'];
                }
                else if (typeof(defaults) == 'object' && typeof(defaults['perc-field-title-value']) != 'undefined')
                {
                    titleValue = defaults['perc-field-title-value'];
                }

                var out = "<div class='perc-pref-entry'>" + I18N.message("perc.ui.widgets.commentsForm@Title") +
                    " <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + titleValue + "' id='perc-field-title-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);
                $.perc_filterFieldText(container.find('#perc-field-title-value'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var titleValue = $('#perc-field-title-value').val().trim();
                // Remove extra spaces between classes
                titleValue = titleValue.replace( /\s\s+/g, ' ' );

                if (titleValue.length !== undefined && titleValue.length > 0)
                {
                    newData = {"perc-field-title-value": titleValue};
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-title-value']) != 'undefined')
                {
                    newData = {"perc-field-title-value":  defaults['perc-field-title-value']};
                }

                return newData;
            },

            // Function invoked when the form control gets rendered
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var titleValue;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' && typeof(vals['perc-field-title-value']) !== 'undefined' && vals['perc-field-title-value'] !== defaults['perc-field-title-value'])
                {
                    titleValue = vals['perc-field-title-value'];
                }

                elem.attr('title', titleValue);
            },
            "validate":function(){
                return true;
            }
        },

        "ID": {
            "name": "id",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var idValue = "";
                if (vals !== undefined && typeof(vals) === 'object' && typeof(vals['perc-field-id-value']) !== 'undefined')
                {
                    idValue = vals['perc-field-id-value'];
                }
                else if (typeof(defaults) == 'object' && typeof(defaults['perc-field-id-value']) != 'undefined')
                {
                    idValue = defaults['perc-field-id-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "Id <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + idValue + "' id='perc-field-id-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);
                // Filter the ID field to get a valid ID
                $.perc_filterFieldText(container.find('#perc-field-id-value'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var idValue = $('#perc-field-id-value').val().trim();

                if (idValue.length > 0)
                {
                    newData = {"perc-field-id-value": idValue};
                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-id-value']) != 'undefined')
                {
                    // TODO: review this condition
                    newData = {"perc-field-id-value":  defaults['perc-field-id-value']};
                }

                // We will select the form control editors that are basic, this way, the editor for the current controls doesn't get
                // selected and we will be sure that the comparisson will not be include the current (same) control
                var formControls = $('div.perc-form-field-wrapper').children('.field-editor-basic');
                var isUniqueId = true;
                for (var i=0; i < formControls.length; i++)
                {
                    var controlPreferenceVals = $(formControls[i]).data('preferences');

                    // Validate the ID after filtering chars in the input
                    if (typeof(controlPreferenceVals) !== 'undefined'  &&
                        typeof(controlPreferenceVals.id) !== 'undefined' &&
                        typeof(controlPreferenceVals.id['perc-field-id-value']) !== 'undefined' &&
                        idValue.toLowerCase() === controlPreferenceVals.id['perc-field-id-value'].toLowerCase())
                    {
                        isUniqueId = false;
                        break;
                    }
                }

                // If the Id is not unique within the form, shown an alert dialog and return null
                if (!isUniqueId)
                {
                    window.parent.jQuery.perc_utils.alert_dialog({
                        title : I18N.message("perc.ui.labels@Error"),
                        content : I18N.message("perc.ui.widgets.commentsForm@Id Already Assigned")
                    });
                    return null;
                }
                else
                {
                    return newData;
                }
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var idValue;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' && typeof(vals['perc-field-id-value']) !== 'undefined' && vals['perc-field-id-value'] !== defaults['perc-field-id-value'])
                {
                    idValue = vals['perc-field-id-value'];
                }
                /*
                else
                {
                    idValue = getUniqueGeneratedId()

                }
                */
                // If and ID was set, apply it to the current element
                elem.attr('id', idValue);
            },
            "validate":function(){
                return true;
            }
        }

    };


    /**
     * Renders a form editor from a given JSON object and creates the JSON object from the editor.
     * It also creates the rendered HTML from the given JSON Object.
     * Structure of JSON object.
     * {config:{name:"formName", etc...},
     *  fields:[{name:"first-name",type:"PercEntryFieldControl",label:"First Name",defaultValue:"", etc..},{...}]}
     *
     * config object holds the data required for the form configuration like name and other details.
     * See PercCommentsFormController for more info.
     *
     * fields is an array of field objects, form controller loops through these field objects and depending on the type
     * uses the appropriate control for rendering the field editor.
     * All field controls must implement $.PercFieldControlInterface interface and must be added to the PercFieldTypes array.
     * As JavaScript does not allow implementing interfaces, all field controls must have a interface checker.
     */
    $.PercCommentsFormController = function()
    {
        // Makes these local.  Two reasons for this: One, it should create a marginal speed-up in access,
        // but more importantly, using the function now makes semantic sense.
        var generateNameFromLabel = $.perc_autoFillTextFilters.URL;
        var sanitizeTextFill = $.perc_autoFillTextFilters.IDNAMECDATA;
        var autoFillForm = $.perc_textAutoFill;
        var filterField  = $.perc_filterField;
        var sanitizeTextFilter = $.perc_textFilters.IDNAMECDATA;

        var formControlAPI = {
            getControl : getControl,
            getFormData : getFormData,
            getRenderedForm : getRenderedForm,
            getFormEditor : getFormEditor,
            getFieldEditor : getFieldEditor,
            getNewFieldEditor : getNewFieldEditor,
            toggleFieldEditor : toggleFieldEditor,
            generateNameFromLabel : generateNameFromLabel
        };

        /**
         * Creates the JSON data object from the supplied form element. Assumes the form element has been generated
         * as per the structure of the data object.
         * @param $formElem (JQUERY element) assumed not null and a valid form element.
         * @return JSON Object for the form.
         */
        function getFormData($formElem)
        {
            var formData = {config:{},fields:[]};
            //Fill config data
            var $formConfigElem = $formElem.find("."+$.PercFormConstants.FORM_CONFIG_CLASS);
            formData.config.name = $formConfigElem.find("#perc-form-name").val();
            formData.config.title = $formConfigElem.find("#perc-form-title").val();
            //Fill field Elements
            var $formFieldElems = $formElem.find("." + $.PercFormConstants.FIELD_CLASS);
            $.each($formFieldElems, function(){
                var type = $(this).attr("type");
                var fieldControl = $.PercFieldTypes[type];
                if(!fieldControl)
                {
                    throw "Error generating form data as one of the fields is set with invalid form field control";
                }
                formData.fields.push(fieldControl().getFieldData($(this)));
            });
            return formData;
        }

        /**
         * Creates  form editor HTML from the supplied formData.
         * @param formData (JSON Object) assumed not null.
         * @return HTML String form editor HTML.
         * @throws error if no control registered for any of the field types.
         */
        function getFormEditor(formData)
        {
            var formEditorHtml = $("<div class='" + $.PercFormConstants.FORM_CLASS + "'></div>");
            formEditorHtml.append("<div class='" + $.PercFormConstants.FORM_CONFIG_CLASS + "'>" +
                "<div class = 'perc-form-header'>" +
                "<div id = 'perc-form-help-text'>" + I18N.message("perc.ui.widgets.commentsForm@Add Form Fields") + "</div>" +
                "</div><div id = 'perc-form-control-wrapper'><div><div id = 'perc-form-dnd-fields'></div></div>" +
                "</div>");
            var formFieldsDiv = formEditorHtml.find("#perc-form-dnd-fields");

            // Provides auto-fill functionality for the name box, after being sanitized to
            // replace "_" and " " with -, and drop all other non-alphanumerics
            autoFillForm(formEditorHtml.find('#perc-form-title'),formEditorHtml.find('#perc-form-name'),sanitizeTextFill);
            filterField(formEditorHtml.find('#perc-form-name'), sanitizeTextFilter);

            if(formData)
            {
                var formConfig = formData.config;
                formEditorHtml.find("#" + $.PercFormConstants.FORM_TITLE_INPUT_ID).val(formConfig.title);
                formEditorHtml.find("#" + $.PercFormConstants.FORM_NAME_INPUT_ID).val(formConfig.name);
                $.each(formData.fields, function(){
                    var fieldControl = $.PercFieldTypes[this.type];
                    if(!fieldControl)
                    {
                        throw "Invalid form control";
                    }
                    var fieldEditorHtml = fieldControl().getFieldEditor(this);
                    fieldEditorHtml.find("." + $.PercFormConstants.FIELD_CLASS).data($.PercFormConstants.PREFERENCES_KEY,this.prefs);
                    formFieldsDiv.append(fieldEditorHtml);
                });
            }
            return formEditorHtml;
        }

        /**
         * Returns a new form field editor HTML of supplied type finds the control associated with the supplied type
         * and gets the field editor HTML from the control.
         * @see $.PercFieldControlInterface#getFieldEditor for details.
         * @param fieldType (String) assumed not null.
         * @return HTML String form editor HTML.
         * @throws error if no control registered for the supplied fieldType.
         */
        function getNewFieldEditor(fieldType)
        {
            var fieldControl = $.PercFieldTypes[fieldType];
            if(!fieldControl)
            {
                throw "Invalid form control";
            }
            return fieldControl().getFieldEditor();
        }

        /**
         * Returns the form field editor HTML for the supplied fieldData object, finds the control associated with the
         * type of the supplied fieldData and gets the field editor HTML from the control.
         * @see $.PercFieldControlInterface#getFieldEditor for details.
         * @param fieldData (JSON Object) assumed not null.
         * @return HTML String form editor HTML.
         * @throws error if no control registered for the type of supplied fieldData.
         */
        function getFieldEditor(fieldData)
        {
            var fieldControl = $.PercFieldTypes[fieldData.type];
            if(!fieldControl)
            {
                throw "Invalid form control";
            }
            return fieldControl().getFieldEditor(fieldData);
        }

        /**
         * Switches the field editor from basic to extended or extended to basic, gets the type and delegates it to
         * the appropriate control for actual work. @see $.PercFieldControlInterface#toggleFieldEditor for details.
         */
        function toggleFieldEditor($fieldElem)
        {
            var fieldControl = getControl($fieldElem);
            return fieldControl().toggleFieldEditor($fieldElem);
        }

        /**
         * Returns the rendered form for the supplied formData.
         * @param formData (JSON Object) assumed not null and assumes it to be structured as mentioned in class doc.
         */
        function getRenderedForm(formData)
        {
            var formControl = $.PercCommentsFormController();
            var localserver = true;
            var formConfig = formData.config;
            var formRenderedHtml = $("<form name='commentForm' method='post' action='PERC_RV_COMMENT_FORM_ACTION' accept-charset='ISO-8859-1'></form>");
            if ($.PercFormVariables.IsHoneypotEnabled === 1) {
                formRenderedHtml = $("<form name='commentForm' method='post' action='PERC_RV_COMMENT_FORM_ACTION' accept-charset='ISO-8859-1' onsubmit=\'return validatePercForm()\'></form>");
            }
            formRenderedHtml.append(
                $('<span/>').
                addClass($.PercFormConstants.FORM_TITLE_CLASS).
                text(formData.config.title)
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "perc_formName").
                attr("value", formData.config.name)
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "site").
                attr("value", 'PERC_RV_SITE_NAME')
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "pagepath").
                attr("value", 'PERC_RV_PAGE_PATH')
            );

            // This code goes through each field name on the list, and first makes sure that
            // the given label can be converted into a unique field name for that form, and
            // if not, appends _N to it, where N is the next unique number.  It then calls
            // getRenderedField on it actually render the field into html.
            var fieldUniquifier = { };

            $.each(formData.fields, function(){
                var fieldName = formControl.generateNameFromLabel(this.label);
                if(fieldName === '')
                    fieldName = '_blank_';
                var fieldNameUniqueNumber = 0;
                var modifiedFieldName = fieldName;
                while (fieldUniquifier[modifiedFieldName] === true)
                {
                    fieldNameUniqueNumber++;
                    modifiedFieldName = fieldName + "_" + fieldNameUniqueNumber;
                }
                fieldName = modifiedFieldName;
                fieldUniquifier[fieldName] = true;
                this.name = fieldName;

                var fieldControl = $.PercFieldTypes[this.type];
                if(!fieldControl)
                {
                    throw "Invalid form control";
                }
                formRenderedHtml.append(fieldControl().getRenderedField(this));
            });
            return formRenderedHtml;
        }

        /**
         * Helper function to find the field control for the supplied field element.
         * @param fieldElem (HTML String) represents a field editor HTML.
         * @return function The control function corresponding to the supplied HTML element.
         * @throws error if the supplied fieldElem type is not a registered control
         */
        function getControl($fieldElem)
        {
            var fieldType = $fieldElem.attr("type");
            var fieldControl = $.PercFieldTypes[fieldType];
            if(!fieldControl)
            {
                throw "Invalid form control";
            }
            return fieldControl;
        }
        return formControlAPI;
    };

    /**
     * Field control interface, all field controls must implement the methods from this interface.
     * getFieldData and getFieldEditor methods must be implemented to confirm to each other as getFieldEditor uses the
     * fieldData produced by getFieldData method and getFieldData method uses the HTML produced by getFieldEditor method.
     * @see $.PercEntryFieldControl for a reference implementation.
     */
    $.PercFieldControlInterface = {
        /**
         * The display label of the control.
         */
        label:"Field",
        /**
         * The icon url of the control
         */
        icon:"",
        /**
         * Returns the editor HTML for the supplied fieldData JSON object corresponding to the control or new field
         * editor if the fieldData is undefined.
         * Example editor HTML.
         * <div class="field-editor-basic field-editor" type="PercEntryFieldControl">
         *  <div class="field-label">mylabel1</div>
         *  <div><input type="text" value="mydefaultvalue1" name="defaultValue"></div>
         * </div>
         * The root div must have a type attribute with a value as the name of the control, and it must have field
         * editor class and a class for the type of editor. Use constants defined in $.PercFormConstants.
         *
         * @param fieldData
         *            (JSON Object), the data object used to build the editor. If undefined returns basic new field
         *            editor.
         * @param isExtended
         *            (boolean) flag indicating whether to return an extended or basic editor, if it is true returns an
         *            extended editor. Example of basic editor: for text field control the basic editor is a label and
         *            a text box for value and the extended editor is text box for label and text box for value.
         * @return (HTML String), the editor HTML corresponding to this control.
         */
        getFieldEditor : function(fieldData, isExtended){throw "Must be implemented by the concrete class";},

        /**
         * Returns the field data JSON object corresponding to the control.
         * @param $fieldElem JQuery reference to the field element, this is the value returned by the getFieldEditor.
         * @return (JSON Object) created from the field element.
         */
        getFieldData : function($fieldElem){throw "Must be implemented by the concrete class";},

        /**
         * Switches the editor from basic to extended and extended to basic.
         *
         * @param $fieldElem
         *            (JQuery reference to the field element), the editor HTML corresponding to this control.
         * @return (HTML String), the extended editor HTML if supplied fieldElem corresponds to basic editor (determined
         *         by hasClass of FIELD_EDITOR_EXT_CLASS) otherwise basic editor.
         */
        toggleFieldEditor : function($fieldElem){
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            var fieldData = this.getFieldData($fieldElem);
            var editor = this.getFieldEditor(fieldData, !$fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS));
            if(typeof(prefs) == 'object')
            {
                editor.find('.' + $.PercFormConstants.FIELD_CLASS).data($.PercFormConstants.PREFERENCES_KEY, prefs);
            }
            return editor;
        },

        /**
         * Creates the final rendered HTML corresponding to the supplied fieldData object.
         */
        getRenderedField : function(fieldData){throw "Must be implemented by the concrete class";},

        /**
         * Returns an array of all available pref types for this control. Concrete classes should
         * override this class if they need prefs different from the defaults
         * @return array of all available prefs, never <code>null</code> or <code>undefined</code>.
         * @type array
         */
        getAvailablePrefs : function(){return [];}
    };

    //////////////////////////////////////////////////////////
    /******************** TITLE Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercTitleFieldControl = function()
    {
        var titleControlAPI = $.extend({},$.PercFieldControlInterface);
        titleControlAPI.label = "Title";
        var nameValue = "title";

        titleControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
                fieldData.defaultValue = $fieldElem.find("div[name=" + nameValue + "]").text();
                if (fieldData.defaultValue === '' || fieldData.defaultValue === undefined || fieldData.defaultValue === null) {
                    fieldData.defaultValue = $fieldElem.find("div[name=" + nameValue + "]").attr('placeholder');
                }
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name=" + nameValue + "]").val();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_TITLE;
            return fieldData;
        };
        titleControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TITLE).
                    attr('fieldname', titleControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldLabel').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(defValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                var defValue4 = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TITLE).
                    attr('fieldname',  titleControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div class = "perc-form-datadisplay"/>').
                        attr('name', nameValue).
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue4)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TITLE).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    attr('fieldname',  titleControlAPI.label).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        titleControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var labelForValue = "";
            var fieldId = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', labelForValue).
                    text(fieldData.label)
                )
            ).append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<input/>').
                    attr('type', 'text').
                    attr('id', fieldId).
                    attr('name', nameValue).
                    attr('placeholder', defValue)
                )
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            //renderedHtml.find("." + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };
        titleControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED,  defaults:{"DISABLED": "FALSE"}},
                {pref: $.PercFormFieldPref.MAX_CHAR, defaults:{"perc-field-max-char-value": 255}},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.TITLE},
            ];
        };
        return titleControlAPI;
    };

    //////////////////////////////////////////////////////////
    //******************** Honeypot Field ********************
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     * */
    $.PercHoneypotFieldControl = function()
    {
        var honeypotControlAPI = $.extend({},$.PercFieldControlInterface);
        var nameValue = "topyenoh";
        honeypotControlAPI.label = "Honeypot";
        $.PercFormVariables.IsHoneypotEnabled = 1;
        honeypotControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = this.label;
                fieldData.defaultValue = fieldData.label;
            }
            else
            {
                fieldData.label = $fieldElem.find("div[name='fieldLabel']").text();
                fieldData.defaultValue = $fieldElem.find("div[name=" + nameValue + "]").text();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_HONEYPOT;
            return fieldData;
        };
        honeypotControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    attr('fieldname', honeypotControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        attr('name', 'fieldLabel').
                        text(defValue)
                    )
                );
            }
            else if(fieldData)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    attr('fieldname',  honeypotControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    attr('fieldname',  honeypotControlAPI.label).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    )
                );
            }
            return fieldEditorHtml;
        };
        honeypotControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var labelForValue = "";
            var fieldId = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<input/>').
                    attr('type', 'hidden').
                    attr('id', 'topyenoh').
                    attr('name', nameValue)
                )
            );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            return renderedHtml;
        };

        honeypotControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return honeypotControlAPI;
    };

    //////////////////////////////////////////////////////////
    /******************** User Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercUserFieldControl = function()
    {
        var userControlAPI = $.extend({},$.PercFieldControlInterface);
        userControlAPI.label = "User's name";
        var nameValue = "username";

        userControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
                fieldData.defaultValue = $fieldElem.find("div[name=" + nameValue + "]").text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name=" + nameValue + "]").val();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_USER;
            return fieldData;
        };
        userControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_USER).
                    attr('fieldname', userControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldLabel').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(defValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                var defValue2 = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_USER).
                    attr('fieldname',  userControlAPI.label).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div class = "perc-form-datadisplay"/>').
                        attr('name', nameValue).
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue2)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_USER).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    attr('fieldname',  userControlAPI.label).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        userControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();
            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var labelForValue = "";
            var fieldId = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', labelForValue).
                    text(fieldData.label)
                )
            ).append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<input/>').
                    attr('type', 'text').
                    attr('id', fieldId).
                    attr('name', nameValue).
                    val(defValue)
                )
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            renderedHtml.find("." + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };
        userControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED,  defaults:{"DISABLED": "FALSE", "CHECKED" : "TRUE"}},
                {pref: $.PercFormFieldPref.MAX_CHAR,  defaults:{"perc-field-max-char-value": 255}},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.TITLE},
            ];
        };
        return userControlAPI;
    };


    //////////////////////////////////////////////////////////
    /******************** Email Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercEmailFieldControl = function()
    {
        var emailControlAPI = $.extend({},$.PercFieldControlInterface);
        var nameValue = "email";
        emailControlAPI.label = "Email";
        emailControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
                fieldData.defaultValue = $fieldElem.find("div[name='" + nameValue + "']").text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name=" + nameValue + "]").val();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_EMAIL;
            return fieldData;
        };
        emailControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_EMAIL).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldLabel').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            addClass('email').
                            val(fieldData.defaultValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_EMAIL).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div class = "perc-form-datadisplay"/>').
                        attr('name', nameValue).
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_EMAIL).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass('email').
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        emailControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();
            var labelForValue = "";
            var fieldId = "";

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', labelForValue).
                    text(fieldData.label)
                )
            ).append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<input/>').
                    attr('type', 'text').
                    attr('id', fieldId).
                    attr('name', nameValue).
                    addClass('email').
                    attr('placeholder', defValue)
                )
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            //renderedHtml.find("." + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };
        emailControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED,  defaults:{"DISABLED": "FALSE", "CHECKED" : "TRUE"}},
                {pref: $.PercFormFieldPref.MAX_CHAR,  defaults:{"perc-field-max-char-value": 255}},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.TITLE},
            ];
        };
        return emailControlAPI;
    };

    //////////////////////////////////////////////////////////
    /******************* Textarea Field *********************/
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercTextareaFieldControl = function()
    {
        var textareaControlAPI = $.extend({},$.PercFieldControlInterface);
        textareaControlAPI.label = "Comment box";
        var nameValue = "text";
        textareaControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }
            fieldData.defaultValue = $fieldElem.find("textarea[name='text']").val();
            if (fieldData.defaultValue === '' || fieldData.defaultValue === undefined || fieldData.defaultValue === null) {
                fieldData.defaultValue = $fieldElem.find("textarea[name='defaultValue']").attr('placeholder');
            }
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_TEXTAREA;
            return fieldData;
        };
        textareaControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXTAREA).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldLabel').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<textarea/>').
                            attr('name', nameValue).
                            attr('cols', '50').
                            attr('rows', '3').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.defaultValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXTAREA).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<textarea/>').
                            attr('name', nameValue).
                            attr('cols', '50').
                            attr('rows', '3').
                            attr('title', 'Comment Box').
                            attr('aria-labelledby', 'Comment Box').
                            attr('readonly', 'readonly').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.defaultValue)
                        )
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXTAREA).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<textarea/>').
                            attr('name', nameValue).
                            attr('cols', '50').
                            attr('rows', '3').
                            attr('title', 'Comment Box').
                            attr('aria-labelledby', 'Comment Box').
                            attr('background', 'grey').
                            attr('readonly', 'readonly').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        textareaControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();

            var labelForValue = "";
            var fieldId = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', labelForValue).
                    text(fieldData.label)
                )
            ).append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<textarea/>').
                    attr('name', nameValue).
                    attr('class', "required").
                    attr('id', fieldId).
                    attr('placeholder', fieldData.defaultValue)
                )
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('textarea'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            return renderedHtml;
        };
        textareaControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED,  defaults:{"DISABLED": "TRUE"}},
                {pref: $.PercFormFieldPref.MAX_CHAR,  defaults:{"perc-field-max-char-value": 4000}},
                {pref: $.PercFormFieldPref.HEIGHT},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.TITLE},
            ];
        };
        return textareaControlAPI;
    };
    /////////////////////////////////////////////////////////
    /******************** Text Field *********************///
    /////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercTextFieldControl = function()
    {
        var textControlAPI = $.extend({},$.PercFieldControlInterface);
        textControlAPI.label="Text";
        textControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).html();
            }
            else
            {
                fieldData.label = tinymce.EditorManager.get('elm1').getContent();
                tinymce.EditorManager.execCommand('mceRemoveEditor', true, 'elm1');
            }
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_TEXT;
            return fieldData;
        };
        textControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXT).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append($("<textarea id='elm1' name='elm1' rows='14' cols='80' style='width: 100%'>" + fieldData.label +"</textarea>"))
                );
            }
            else if(fieldData)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        html(fieldData.label)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_TEXT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    )
                );
            }

            return fieldEditorHtml;
        };
        textControlAPI.getRenderedField = function(fieldData)
        {
            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<span/>').
                html(fieldData.label)
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('span'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            return renderedHtml;
        };
        textControlAPI.getAvailablePrefs = function()
        {
            return [];
        };
        return textControlAPI;
    };

    //////////////////////////////////////////////////////////
    /******************** URL Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercURLFieldControl = function()
    {
        var urlControlAPI = $.extend({},$.PercFieldControlInterface);
        var nameValue = "url";
        urlControlAPI.label = "URL";
        urlControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
                fieldData.defaultValue = $fieldElem.find("div[name='" + nameValue + "']").text();
                if (fieldData.defaultValue === '' || fieldData.defaultValue === undefined || fieldData.defaultValue === null) {
                    fieldData.defaultValue = $fieldElem.find("div[name=" + nameValue + "]").attr('placeholder');
                }
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name=" + nameValue + "]").val();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_URL;
            return fieldData;
        };
        urlControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_URL).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldLabel').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(defValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                var defValue1 = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_URL).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div class = "perc-form-datadisplay"/>').
                        attr('name', nameValue).
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue1)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_URL).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', nameValue).
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        urlControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercCommentsFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var labelForValue = "";
            var fieldId = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                fieldId = fieldData.prefs.id['perc-field-id-value'];
                labelForValue = fieldId;
            }
            else {
                labelForValue = fieldData.name;
                fieldId = labelForValue;
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', labelForValue).
                    text(fieldData.label)
                )
            ).append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                append(
                    $('<input/>').
                    attr('type', 'text').
                    attr('id', fieldId).
                    addClass('url').
                    attr('name', nameValue).
                    attr('placeholder', defValue)
                )
            );
            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            //renderedHtml.find("." + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };
        urlControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED,  defaults:{"DISABLED": "FALSE"}},
                {pref: $.PercFormFieldPref.MAX_CHAR, defaults:{"perc-field-max-char-value": 2000}},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.TITLE},
            ];
        };
        return urlControlAPI;
    };

    /////////////////////////////////////////////////////////
    /******************** Submit Button ********************/
    /////////////////////////////////////////////////////////

    /**
     * @See $.PercFieldControlInterface
     */
    $.PercSubmitButtonControl = function()
    {
        var submitButtonControlAPI = $.extend({},$.PercFieldControlInterface);
        var formControl = $.PercCommentsFormController();
        submitButtonControlAPI.label="Submit Button";
        submitButtonControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }
            fieldData.label = $fieldElem.find("input[name='label']").val();
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_SUBMIT;
            return fieldData;
        };
        submitButtonControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            if(fieldData && isExtended)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_SUBMIT).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('style', 'float:left; margin-right:25px; margin-top:20px').
                        append(
                            $('<input/>').
                            attr('type', 'button').
                            addClass('defaultFocus').
                            val(fieldData.label)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<div/>').
                            addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                            addClass($.PercFormConstants.FORM_LABEL_CLASS).
                            text('Enter your custom text')
                        ).
                        append(
                            $('<div/>').
                            append(
                                $('<input/>').
                                attr('type', 'text').
                                attr('name', 'label').
                                addClass($.PercFormConstants.INPUT_CLASS).
                                addClass('defaultFocus').
                                attr('maxlength', '255').
                                val(fieldData.label)
                            )
                        )
                    )
                );
            }
            else if(fieldData)
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_SUBMIT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'button').
                            attr('name', 'label').
                            attr('disabled', 'disabled').
                            val(fieldData.label)
                        )
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_SUBMIT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'button').
                            attr('name', 'label').
                            attr('disabled', 'disabled').
                            val('Submit')
                        )
                    )
                );
            }

            return fieldEditorHtml;
        };
        submitButtonControlAPI.getRenderedField = function(fieldData)
        {
            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<input/>').
                attr('type', 'submit').
                val(fieldData.label)
            );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            return renderedHtml;
        };
        submitButtonControlAPI.getAvailableValidation = function()
        {
            return [];
        };
        return submitButtonControlAPI;
    };

    /**
     * An array object to maintain the control type and actual control object relationships.
     */
    $.PercFieldTypes = [];
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_TITLE] = $.PercTitleFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_USER] = $.PercUserFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_EMAIL] = $.PercEmailFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_TEXT] = $.PercTextFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_TEXTAREA] = $.PercTextareaFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_URL] = $.PercURLFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_SUBMIT] = $.PercSubmitButtonControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_HONEYPOT] = $.PercHoneypotFieldControl;

})(jQuery);
