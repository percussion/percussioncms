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
        FORM_METADATA_CLASS : 'form-metadata-editor',
        FIELD_CLASS : 'field-editor',
        FORM_CONFIG_CLASS : 'form-editor-config',
        FIELD_EDITOR_BASIC_CLASS : 'field-editor-basic',
        FIELD_EDITOR_EXT_CLASS : 'field-editor-ext',
        FIELD_EDITOR_LABEL_CLASS : 'field-label',
        FIELD_EDITOR_NAME_CLASS : 'field-name',
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
        DEFAULT_LABEL_CONST : 'label',

        //Field Controls
        FIELD_CONTROL_ENTRY : 'PercEntryFieldControl',
        FIELD_CONTROL_TEXT : 'PercTextFieldControl',
        FIELD_CONTROL_DROPDOWN : 'PercDropDownControl',
        FIELD_CONTROL_DATADROPDOWN : 'PercDataDropDownControl',
        FIELD_CONTROL_CHECKBOX : 'PercCheckBoxControl',
        FIELD_CONTROL_RADIO : 'PercRadioControl',
        FIELD_CONTROL_TEXTAREA : 'PercTextareaFieldControl',
        FIELD_CONTROL_HONEYPOT : 'PercHoneypotFieldControl',
        FIELD_CONTROL_RECAPTCHA : 'PercRecaptchaFieldControl',
        FIELD_CONTROL_DATE : 'PercDateControl',
        FIELD_CONTROL_SUBMIT : 'PercSubmitButtonControl',
        FIELD_CONTROL_HIDDEN : 'PercHiddenFieldControl',
        PREFERENCES_KEY: 'preferences'
    };

    $.PercFormVariables = {
        LastRadioName : 'defaultName',
        LastCounterValue : 0,
        IsHoneypotEnabled : 0,
        IsRecaptchaEnabled : 0
    };


    $.PercFormFieldPref = {
        "REQUIRED": {
            "name": "required",
            "addControl": function(container, vals, defaults){
                var checked = "";
                if(typeof(vals) == 'object' && vals['perc-field-required'])
                {
                    checked = " checked='checked'";
                }
                var out = "<div class='perc-pref-entry'>" +
                    "<input type='checkbox' id='perc-field-required'" + checked +"/>This field is required" +
                    "</div>";
                container.append(out);
            },
            "onApply": function(data, defaults){
                var checked = $('#perc-field-required:checked').val() !== undefined;
                data = {"perc-field-required": checked};
                return data;
            },
            "onRender": function(elem, vals, defaults){
                var name = elem.attr('name');
                if(typeof(vals) === 'object' && vals['perc-field-required'])
                {
                    elem.addClass("required");
                    var elemParent = elem.parent().parent();
                    if(elem.attr('type') === 'checkbox' || elem.attr('type') === 'radio'){
                        elemParent.parent().parent().find('.field-label-container').prepend("<span class = 'perc-form-error-asterisk'>*</span>");
                    }
                    else{
                        elemParent.find('.field-label-container').prepend("<span class = 'perc-form-error-asterisk'>*</span>");
                    }
                }
            },
            "validate":function(){
                return true;
            }
        },

        "MAX_CHAR": {
            "name": "max_char",
            "addControl": function(container, vals, defaults){
                var checked = "";
                var max = "";
                if(typeof(vals) == 'object')
                {
                    /* if(vals['perc-field-max-char-enabled'])
                     {
                         checked = " checked='checked'";
                     }*/
                    if(typeof(vals['perc-field-max-char-value']) != 'undefined')
                    {
                        max = vals['perc-field-max-char-value'];
                    }

                }
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-max-char-value']) != 'undefined')
                {
                    max = defaults['perc-field-max-char-value'];
                }
                var out = "<div class='perc-pref-entry'>" +
                    "Maximum characters" +
                    "<input type='text' class='perc-pref-text' value='" + max + "' id='perc-field-max-char-value'/>" +
                    "<label for='perc-field-max-char-value' class='perc_field_error' style='display: none;'>Value must be between 0 and 255.</label></div>";
                container.append(out);
                $.perc_filterField(container.find("#perc-field-max-char-value"), $.perc_textFilters.ONLY_DIGITS);
            },
            "onApply": function(data, defaults){
                var newData = {};
                var checked = $('#perc-field-max-char-enabled:checked').val() !== undefined;
                var max = parseInt($('#perc-field-max-char-value').val());
                if(!isNaN(max) && max > 0)
                    newData = {"perc-field-max-char-enabled": checked, "perc-field-max-char-value": max};
                else if(typeof(defaults) == 'object' && typeof(defaults['perc-field-max-char-value']) != 'undefined')
                {
                    newData = {"perc-field-max-char-value":  defaults['perc-field-max-char-value']};
                }
                return newData;
            },
            "onRender": function(elem, vals, defaults){
                var max = Number.NaN;
                if(typeof(vals) == 'object' &&
                    typeof(vals['perc-field-max-char-value']) != 'undefined' &&
                    vals['perc-field-max-char-value'] !== defaults['perc-field-max-char-value'])
                {
                    max = vals['perc-field-max-char-value'];
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-max-char-value']) !== 'undefined')
                {
                    max = defaults['perc-field-max-char-value'];
                }
                if(isNaN(Number(max)))
                {
                    elem.attr("fieldMaxLength", '');
                }
                else
                {
                    elem.attr("fieldMaxLength", max);
                }

            },
            "validate":function(defaults){
                if(!defaults || defaults["perc-validate-max-char"] !== "yes")
                    return true;
                var isValid = true;
                var val = $('#perc-field-max-char-value').val().trim();
                if(val === "") {
                    return true;
                }
                var max = parseInt(val);
                if(!max || max > 2048)
                {
                    $(".perc_field_error[for='perc-field-max-char-value']").css("display","block").closest(".perc-pref-entry").css("height","40");
                    isValid = false;
                }
                else
                {
                    $(".perc_field_error[for='perc-field-max-char-value']").hide().css("height","");
                }
                return isValid;
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
                $.perc_filterField(container.find("#perc-field-width-value"), $.perc_textFilters.ONLY_DIGITS);
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
                if(typeof(vals) === 'object' &&
                    typeof(vals['perc-field-width-value']) !== 'undefined' &&
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
            },
            "validate":function(){
                return true;
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
                $.perc_filterField(container.find("#perc-field-height-value"), $.perc_textFilters.ONLY_DIGITS);
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
            },
            "validate":function(){
                return true;
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
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-class-value']) !== 'undefined')
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

                var out = "<div class='perc-pref-entry'>" +
                    "Name <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + nameValue + "' id='perc-field-name-value' size=\"50\" maxlength=\"255\"/>" +
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

                if (nameValue.length !== undefined)
                {
                    newData = {"perc-field-name-value": nameValue};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-name-value']) !== 'undefined')
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
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-name-value']) !== 'undefined' &&
                    vals['perc-field-name-value'] !== defaults['perc-field-name-value'] && vals['perc-field-name-value'] !== "")
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

                var out = "<div class='perc-pref-entry'>" +
                    "Title <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + titleValue + "' id='perc-field-title-value' size=\"50\" maxlength=\"255\"/>" +
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

                if (titleValue.length !== undefined)
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
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-title-value']) !== 'undefined' &&
                    vals['perc-field-title-value'] !== defaults['perc-field-title-value'])
                {
                    titleValue = vals['perc-field-title-value'];
                }

                elem.attr('title', titleValue);
            },
            "validate":function(){
                return true;
            }
        },

        "RECAPTCHA_SITEKEY": {
            "name": "recaptcha-sitekey",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var sitekeyValue = "";
                if (vals !== undefined && typeof(vals) === 'object' && typeof(vals['perc-field-recaptcha-sitekey-value']) !== 'undefined')
                {
                    sitekeyValue = vals['perc-field-recaptcha-sitekey-value'];
                }
                else if (typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-sitekey-value']) !== 'undefined')
                {
                    sitekeyValue = defaults['perc-field-recaptcha-sitekey-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "Site Key <input type='text' class='perc-pref-text perc-pref-text-longer' value='" + sitekeyValue + "' id='perc-field-recaptcha-sitekey-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var sitekeyValue = $('#perc-field-recaptcha-sitekey-value').val().trim();

                if (sitekeyValue.length > 0)
                {
                    newData = {"perc-field-recaptcha-sitekey-value": sitekeyValue};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-sitekey-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-sitekey-value":  defaults['perc-field-recaptcha-sitekey-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var sitekeyValue;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-sitekey-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-sitekey-value'] !== defaults['perc-field-recaptcha-sitekey-value'])
                {
                    sitekeyValue = vals['perc-field-recaptcha-sitekey-value'];
                }
                // If key was set, apply it to the current element
                if(sitekeyValue)
                    elem.attr('data-sitekey', sitekeyValue);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_THEME": {
            "name": "recaptcha-theme",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-theme-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-theme-value'];
                }
                else if (typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-theme-value']) !== 'undefined')
                {
                    value = defaults['perc-field-recaptcha-theme-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-theme-value' class='field-label'>Theme (dark/light)</label><input type='text' placeholder='light' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-theme-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-theme-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-theme-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-theme-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-theme-value":  defaults['perc-field-recaptcha-theme-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-theme-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-theme-value'] !== defaults['perc-field-recaptcha-theme-value'])
                {
                    value = vals['perc-field-recaptcha-theme-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-theme', value);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_SIZE": {
            "name": "recaptcha-size",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) == 'object' &&
                    typeof(vals['perc-field-recaptcha-size-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-size-value'];
                }
                else if (typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-size-value']) != 'undefined')
                {
                    value = defaults['perc-field-recaptcha-size-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-size-value'>Size (compact/normal)</label><input type='text' placeholder='normal' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-size-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-size-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-size-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-size-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-size-value":  defaults['perc-field-recaptcha-size-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-size-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-size-value'] !== defaults['perc-field-recaptcha-size-value'])
                {
                    value = vals['perc-field-recaptcha-size-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-size', value);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_TABINDEX": {
            "name": "recaptcha-tabindex",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-tabindex-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-tabindex-value'];
                }
                else if (typeof(defaults) === 'object' &&
                    typeof(defaults['perc-field-recaptcha-tabindex-value']) !== 'undefined')
                {
                    value = defaults['perc-field-recaptcha-tabindex-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-tabindex-value'>Tabindex</label><input type='text' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-tabindex-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-tabindex-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-tabindex-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-tabindex-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-tabindex-value":  defaults['perc-field-recaptcha-tabindex-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-tabindex-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-tabindex-value'] !== defaults['perc-field-recaptcha-tabindex-value'])
                {
                    value = vals['perc-field-recaptcha-tabindex-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-tabindex', value);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_CALLBACK": {
            "name": "recaptcha-callback",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-callback-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-callback-value'];
                }
                else if (typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-callback-value']) != 'undefined')
                {
                    value = defaults['perc-field-recaptcha-callback-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-callback-value'>Callback</label><input type='text' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-callback-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-callback-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-callback-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-callback-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-callback-value":  defaults['perc-field-recaptcha-callback-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-callback-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-callback-value'] !== defaults['perc-field-recaptcha-callback-value'])
                {
                    value = vals['perc-field-recaptcha-callback-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-callback', value);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_EXPIREDCALLBACK": {
            "name": "recaptcha-expired-callback",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-expired-callback-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-expired-callback-value'];
                }
                else if (typeof(defaults) === 'object' &&
                    typeof(defaults['perc-field-recaptcha-expired-callback-value']) !== 'undefined')
                {
                    value = defaults['perc-field-recaptcha-expired-callback-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-expired-callback-value'>Expired Callback</label><input type='text' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-expired-callback-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-expired-callback-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-expired-callback-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-expired-callback-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-expired-callback-value":  defaults['perc-field-recaptcha-expired-callback-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-expired-callback-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-expired-callback-value'] !== defaults['perc-field-recaptcha-expired-callback-value'])
                {
                    value = vals['perc-field-recaptcha-expired-callback-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-expired-callback', value);
            },
            "validate":function(){
                return true;
            }
        },
        "RECAPTCHA_ERRORCALLBACK": {
            "name": "recaptcha-error-callback",

            // Function invoked when the preferences dialog for the form control (field) opens up
            // @param container
            // @param vals
            // @param defaults
            "addControl" : function(container, vals, defaults)
            {
                var value = "";
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-error-callback-value']) !== 'undefined')
                {
                    value = vals['perc-field-recaptcha-error-callback-value'];
                }
                else if (typeof(defaults) === 'object' &&
                    typeof(defaults['perc-field-recaptcha-error-callback-value']) !== 'undefined')
                {
                    value = defaults['perc-field-recaptcha-error-callback-value'];
                }

                var out = "<div class='perc-pref-entry'>" +
                    "<label for='perc-field-recaptcha-error-callback-value'>Error Callback</label><input type='text' class='perc-pref-text perc-pref-text-longer' value='" + value + "' id='perc-field-recaptcha-error-callback-value' size=\"50\" maxlength=\"255\"/>" +
                    "</div>";
                container.append(out);

            },

            // Function invoked when the preferences dialog is closed by clicking the 'Apply' button
            // @param data
            // @param defaults
            "onApply" : function(data, defaults)
            {
                var newData = {};
                var value = $('#perc-field-recaptcha-error-callback-value').val().trim();

                if (value.length > 0)
                {
                    newData = {"perc-field-recaptcha-error-callback-value": value};
                }
                else if(typeof(defaults) === 'object' && typeof(defaults['perc-field-recaptcha-error-callback-value']) != 'undefined')
                {
                    newData = {"perc-field-recaptcha-error-callback-value":  defaults['perc-field-recaptcha-error-callback-value']};
                }

                return newData;
            },

            // Function
            // @para elem
            // @para vals
            // @para defaults
            "onRender" : function(elem, vals, defaults)
            {
                // Perform some checks
                var value;
                if (typeof(defaults) !== 'object')
                {
                    defaults = {};
                }
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-recaptcha-error-callback-value']) !== 'undefined' &&
                    vals['perc-field-recaptcha-error-callback-value'] !== defaults['perc-field-recaptcha-error-callback-value'])
                {
                    value = vals['perc-field-recaptcha-error-callback-value'];
                }
                // If key was set, apply it to the current element
                if(value)
                    elem.attr('data-error-callback', value);
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
                if (vals !== undefined &&
                    typeof(vals) === 'object' &&
                    typeof(vals['perc-field-id-value']) !== 'undefined')
                {
                    idValue = vals['perc-field-id-value'];
                }
                else if (typeof(defaults) === 'object' &&
                    typeof(defaults['perc-field-id-value']) !== 'undefined')
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
                        typeof(controlPreferenceVals.id)!== 'undefined' &&
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
                        title : 'Error',
                        content : 'The Id supplied is already assignated to another form control.'
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
                if (typeof(vals) === 'object' &&
                    typeof(vals['perc-field-id-value']) !== 'undefined' &&
                    vals['perc-field-id-value'] !== defaults['perc-field-id-value'])
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
    /*
    function getUniqueGeneratedId()
    {
        fieldId = 'id_blank';

        var modifiedFieldId = fieldId;
        while (fieldUniquifierId[modifiedFieldId] == true)
        {
            fieldIdUniqueNumber++;
            modifiedFieldId = fieldId + "_" + fieldIdUniqueNumber;
        }
        fieldId = modifiedFieldId;
        fieldUniquifierId[fieldId] = true;
        return fieldId;
    }
    */
    /**
     * Renders a form editor from a given JSON object and creates the JSON object from the editor.
     * It also creates the rendered HTML from the given JSON Object.
     * Structure of JSON object.
     * {config:{name:"formName", etc...},
     *  fields:[{name:"first-name",type:"PercEntryFieldControl",label:"First Name",defaultValue:"", etc..},{...}]}
     *
     * config object holds the data required for the form configuration like name and other details.
     * See PercFormController for more info.
     *
     * fields is an array of field objects, form controller loops through these field objects and depending on the type
     * uses the appropriate control for rendering the field editor.
     * All field controls must implement $.PercFieldControlInterface interface and must be added to the PercFieldTypes array.
     * As JavaScript does not allow implementing interfaces, all field controls must have a interface checker.
     */
    $.PercFormController = function()
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
            getFormMetaDataEditor : getFormMetaDataEditor,
            getFieldEditor : getFieldEditor,
            getNewFieldEditor : getNewFieldEditor,
            toggleFieldEditor : toggleFieldEditor,
            generateNameFromLabel : generateNameFromLabel,
            updateEncryptedFields : updateEncryptedFields
        };

        /**
         * Creates the JSON data object from the supplied form element. Assumes the form element has been generated
         * as per the structure of the data object.
         * @param $formElem (JQUERY element) assumed not null and a valid form element.
         * @return JSON Object for the form.
         */
        function getFormData($formElem, $formMetaElem)
        {
            var formData = {config:{},fields:[]};
            //Fill config data
            var $formConfigElem = $formElem.find("."+$.PercFormConstants.FORM_CONFIG_CLASS);
            formData.config.name = $formConfigElem.find("#perc-form-name").val();
            formData.config.title = $formConfigElem.find("#perc-form-title").val();
            formData.config.description = $formMetaElem.find("textarea[name='perc-form-description']").val();
            formData.config.processorType = $formMetaElem.find("input[name='perc-save-to']:checked").val();
            formData.config.processorURL = $formMetaElem.find("input[name='perc-save-to-url']").val();
            formData.config.successURL = $formMetaElem.find("#perc-formbuild-success-url").val();
            formData.config.unencryptedSuccessURL = $formMetaElem.find("#perc-formbuild-success-url-paired-unencrypted").val();
            formData.config.encryptedSuccessURL = $formMetaElem.find("#perc-formbuild-success-url-paired-encrypted").val();
            formData.config.errorURL = $formMetaElem.find("#perc-formbuild-error-url").val();
            formData.config.unencryptedErrorURL = $formMetaElem.find("#perc-formbuild-error-url-paired-unencrypted").val();
            formData.config.encryptedErrorURL = $formMetaElem.find("#perc-formbuild-error-url-paired-encrypted").val();
            formData.config.emailNotification = $formMetaElem.find("#perc-form-metadata-email-notification").is(':checked');
            formData.config.emailFormNotification = $formMetaElem.find("#perc-form-metadata-email-form").is(':checked');
            formData.config.mailTo = $formMetaElem.find("#perc-formbuild-mail-to").val();
            formData.config.unencryptedMailTo = $formMetaElem.find("#perc-formbuild-mail-to-paired-unencrypted").val();
            formData.config.encryptedMailTo = $formMetaElem.find("#perc-formbuild-mail-to-paired-encrypted").val();
            formData.config.mailSubject = $formMetaElem.find("#perc-formbuild-mail-subject").val();
            formData.config.unencryptedMailSubject = $formMetaElem.find("#perc-formbuild-mail-subject-paired-unencrypted").val();
            formData.config.encryptedMailSubject = $formMetaElem.find("#perc-formbuild-mail-subject-paired-encrypted").val();
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
                "<label id = 'perc-form-label-field-name' class = 'form-widget-label' >Form title:</label> <br />" +
                "<input class = 'perc-form-datadisplay' type = 'text' id = 'perc-form-title' maxlength = '255' size = '50'/><br />" +
                "<label id = 'perc-form-label-description' class = 'form-widget-label perc-required-field'>Form name:</label> <br />" +
                "<input class = 'perc-form-datadisplay' type = 'text' id = 'perc-form-name' maxlength = '255' size = '50'/><br /></div>" +
                "<div id = 'perc-form-help-text'>Add form fields by selecting the \"Form controls menu\" button to the right. Fields can be arranged by dragging and dropping them in the order you would like.</div>" +
                "<div id = 'perc-form-control-wrapper'><div><div id = 'perc-form-dnd-fields'></div></div></div>" +
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
         * Creates  form metadata editor HTML from the supplied formData.
         * @param formData (JSON Object) assumed not null.
         * @return HTML String form metadata editor HTML.
         */
        function getFormMetaDataEditor(formData)
        {
            var formEditorHtml = $("<div class='" + $.PercFormConstants.FORM_METADATA_CLASS + "'></div>");
            formEditorHtml.append(
                "<div id='perc-form-metadata-panel'>" +
                "<div id='perc-form-edit-metadata-link' class='perc-form-spacer'>" +
                "<span id='perc-content-edit-metadata-icon'></span>" +
                "<span>Meta-data</span>" +
                "</div>" +
                "<div id='perc-form-metadata-content' class='table-layout'>" +
                "<div>" +
                "<div>" +
                "<label for='perc-formbuild-success-url' class = 'form-widget-label perc-required-field'>On success redirect to:</label>" +
                "</div>" +
                "<div>" +
                "<div style = 'position:relative'><input type='text' class='perc-encrypted-field' id='perc-formbuild-success-url' />" +
                "<span id='perc-formbuild-success-url-browse'>Browse</span>" +
                "<input type='hidden' id='perc-formbuild-success-url-paired-unencrypted' />" +
                "<input type='hidden' id='perc-formbuild-success-url-paired-encrypted' /></div>" +
                "</div>" +
                "</div>" +
                "<div>" +
                "<div>" +
                "<label for='perc-formbuild-error-url' class = 'form-widget-label perc-required-field'>On error redirect to:</label>" +
                "</div>" +
                "<div>" +
                "<div style = 'position:relative'><input type='text' class='perc-encrypted-field' id='perc-formbuild-error-url' />" +
                "<span id='perc-formbuild-error-url-browse'>Browse</span>" +
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
                "<textarea rows='3' cols='50' id='perc-form-metadata-description' name=" +
                "'perc-form-description' class='perc-form-meta-textarea'></textarea>" +
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
                "<input class='perc-form-datadisplay' name='perc-save-to-url' type='text' " +
                "id='perc-form-metadata-save-to-url-text' />" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div>" +
                "<div>" +
                "<input type='checkbox' name='perc-email-form' id='perc-form-metadata-email-form' " +
                "class='perc-form-metaradio'/>" +
                "<label class='form-widget-label' for='perc-form-metadata-email-form'>Email form</label>" +
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
                "<div style = 'position:relative'><input type='text' class='perc-encrypted-field' id='perc-formbuild-mail-to' disabled='true'/>" +
                "<input type='hidden' id='perc-formbuild-mail-to-paired-unencrypted' />" +
                "<input type='hidden' id='perc-formbuild-mail-to-paired-encrypted' /></div>" +
                "</div>" +
                "</div>" +
                "<div>" +
                "<div>" +
                "<label for='perc-formbuild-mail-subject' class = 'form-widget-label perc-required-field'>Mail subject:</label>" +
                "</div>" +
                "<div>" +
                "<div style = 'position:relative'><textarea rows='3' cols='50'  class='perc-encrypted-field perc-form-meta-textarea' disabled='true' " +
                "id='perc-formbuild-mail-subject'></textarea>" +
                "<input type='hidden' id='perc-formbuild-mail-subject-paired-unencrypted' />" +
                "<input type='hidden' id='perc-formbuild-mail-subject-paired-encrypted' /></div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            );
            if(formData)
            {
                formEditorHtml.find("input[name='perc-save-to']").on("change",function(event){
                    if ($(this).val() === 'LocalServer')
                    {
                        if (this.checked)
                        {
                            formEditorHtml.find('#perc-form-metadata-save-to-url-text').get(0).setAttribute('disabled', 'disabled');
                        }
                        else
                        {
                            formEditorHtml.find('#perc-form-metadata-save-to-url-text').get(0).removeAttribute('disabled');
                        }
                    }
                    else if ($(this).val() === 'ExternalServer')
                    {
                        if (this.checked)
                        {
                            formEditorHtml.find('#perc-form-metadata-save-to-url-text').get(0).removeAttribute('disabled');
                        }
                        else
                        {
                            formEditorHtml.find('#perc-form-metadata-save-to-url-text').get(0).setAttribute('disabled', 'disabled');
                        }
                    }
                    else
                    {
                        throw('Unknown Data Server type');
                    }
                });
                formEditorHtml.find("textarea[name='perc-form-description']").val(formData.config.description);
                formEditorHtml.find("#perc-formbuild-success-url").val(formData.config.unencryptedSuccessURL);
                formEditorHtml.find("#perc-formbuild-success-url-paired-unencrypted").val(formData.config.unencryptedSuccessURL);
                formEditorHtml.find("#perc-formbuild-success-url-paired-encrypted").val(formData.config.encryptedSuccessURL);
                formEditorHtml.find("#perc-formbuild-error-url").val(formData.config.unencryptedErrorURL);
                formEditorHtml.find("#perc-formbuild-error-url-paired-unencrypted").val(formData.config.unencryptedErrorURL);
                formEditorHtml.find("#perc-formbuild-error-url-paired-encrypted").val(formData.config.encryptedErrorURL);
                formEditorHtml.find("#perc-form-metadata-email-notification").prop("checked", formData.config.emailNotification);
                formEditorHtml.find("#perc-form-metadata-email-form").prop("checked", formData.config.emailFormNotification);
                formEditorHtml.find("#perc-formbuild-mail-to").val(formData.config.unencryptedMailTo);
                formEditorHtml.find("#perc-formbuild-mail-to-paired-unencrypted").val(formData.config.unencryptedMailTo);
                formEditorHtml.find("#perc-formbuild-mail-to-paired-encrypted").val(formData.config.encryptedMailTo);
                formEditorHtml.find("#perc-formbuild-mail-subject").val(formData.config.unencryptedMailSubject);
                formEditorHtml.find("#perc-formbuild-mail-subject-paired-unencrypted").val(formData.config.unencryptedMailSubject);
                formEditorHtml.find("#perc-formbuild-mail-subject-paired-encrypted").val(formData.config.encryptedMailSubject);

                formEditorHtml.find('#perc-formbuild-mail-to').prop('disabled', !formData.config.emailNotification);
                formEditorHtml.find('#perc-formbuild-mail-subject').prop('disabled', !formData.config.emailNotification);

                if(formData.config.processorType === "ExternalServer")
                {
                    formEditorHtml.find("input[name='perc-save-to'][value='LocalServer']").removeAttr("checked");
                    formEditorHtml.find("input[name='perc-save-to'][value='ExternalServer']").attr("checked","checked").trigger("click").trigger("change");
                    formEditorHtml.find("input[name='perc-save-to-url']").val(formData.config.processorURL);
                }
                else
                {
                    formEditorHtml.find("input[name='perc-save-to'][value='LocalServer']")
                        .trigger("click")
                        .trigger("change");
                }
            }

            formEditorHtml.find('.perc-encrypted-field').on("blur",function(){
                updateEncryptedFields([$(this).attr("id")], true);
            });

            formEditorHtml.find('#perc-form-metadata-email-notification').on("click",function(){
                var flag = !formEditorHtml.find('#perc-form-metadata-email-notification').is(":checked");
                formEditorHtml.find('#perc-formbuild-mail-to').prop('disabled', flag);
                formEditorHtml.find('#perc-formbuild-mail-subject').prop('disabled', flag);
                formEditorHtml.find("input[name='perc-email-form']").removeAttr("checked");
            });

            formEditorHtml.find('#perc-form-metadata-email-form').on("click",function(){
                var flag = !formEditorHtml.find('#perc-form-metadata-email-form').is(":checked");
                formEditorHtml.find("input[name='perc-email-notification']").removeAttr("checked");
                formEditorHtml.find('#perc-formbuild-mail-to').get(0).setAttribute('disabled', 'disabled');
                formEditorHtml.find('#perc-formbuild-mail-subject').get(0).setAttribute('disabled', 'disabled');
            });

            return formEditorHtml;
        }
        /**
         *
         * @param element
         * @return
         */
        /*function browseDialog(element)
        {
            $.perc_browser({
                on_save: function(spec, closer, show_error)
                {
                    debugger;
                    $(element).val();
                },
                new_asset_option: false,
                selectable_object: "leaf",
                new_folder_opt: false,
                displayed_containers: "Sites",
                //selection_types: ['Folder','site','percPage'],
                asset_name: "Selected Page",
                title: "Select Page",
                save_class: 'perc-save'
            });
        }*/

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
            var formControl = $.PercFormController();
            var localserver = true;
            var formConfig = formData.config;
            var formName = formData.config.name;
            var formAction = "/perc-form-processor/form/";
            var formRenderedHtml;
            if ($.PercFormVariables.IsHoneypotEnabled === 1 || $.PercFormVariables.IsRecaptchaEnabled === 1) {
                formRenderedHtml = $("<form name='" + formData.config.name +"' method='post' action='" +
                    formAction + "' onsubmit=\'return validatePercForm()\'></form>");
            }
            else {
                formRenderedHtml = $("<form name='" + formData.config.name +"' method='post' action='" + formAction + "'></form>");
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
                attr("name", "perc_urlEncrypt").
                attr("value", "true")
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "perc_successUrl").
                attr("value", formData.config.encryptedSuccessURL)
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "perc_errorUrl").
                attr("value", formData.config.encryptedErrorURL)
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "perc_processorType").
                attr("value", formConfig.processorType)
            ).append(
                $('<input/>').
                attr("type", "hidden").
                attr("name", "perc_processorUrl").
                attr("value", formConfig.processorURL)
            );


            if(formData.config.emailNotification)
            {
                formRenderedHtml.append(
                    $('<input/>').
                    attr("type", "hidden").
                    attr("name", "perc_emnt").
                    attr("value", formData.config.encryptedMailTo)
                ).append(
                    $('<input/>').
                    attr("type", "hidden").
                    attr("name", "perc_emns").
                    attr("value", formData.config.encryptedMailSubject));
            }
            if(formData.config.emailFormNotification)
            {
                formRenderedHtml.append(
                    $('<input/>').
                    attr("type", "hidden").
                    attr("name", "emailForm")
                );
            }
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
         * @param $fieldElem (HTML String) represents a field editor HTML.
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

        /**
         * Generates the name of the field from the supplied label. If a name contains spaces,
         * they are converted to underscores.  All unallowed characters are dropped.
         */
        /*function generateNameFromLabel(label)
        {
            label = sanitizeTextFill(label);
            return label;
        }*/


        /**
         * Updates the Success URL for redirects after form submission.
         * Because this url is included in the rendered form, it is encrypted,
         * so this function handles that by making a call to the REST service.
         *
         * @param async Whether the call to the REST service should be made
         *              asyncronously or not.  Defaults to true.
         * @return Object containing up to three values:
         *         <ul>
         *         <li><code>call_made</code>    If a call was made to the REST service or not.  Cannot be null.</li>
         *         <li><code>call_success</code> If the call to the REST service returned a sensible value.  Can be null if
         *                                       <code>call_made</code> is false.<br>
         *                                       <strong>NOTE:</strong> If the call was made asyncronously, the behavior
         *                                       of this value will be undefined.  The call hasn't returned yet, and so
         *                                       whether it was successful or not has no valid definition.</li>
         *         <li><code>call_async</code>   If the call to the REST service was made asyncronously.  Can be null if
         *                                       <code>call_made</code> is false.  <em>Should</em> be the same as the
         *                                       <code>async</code> parameter.</li>
         *         </ul>
         * @see #updateErrorURL
         * @see PercUtilService#encryptString
         * @see PercServiceUtils#makeJsonRequest
         * @see jQuery#ajax
         */
        function updateEncryptedFields(fields, async)
        {
            if (typeof(async) === "undefined")
                async = true;
            var encObjects = [];
            var encStrings = [];
            $(fields).each(function(){
                var fval = $("#" + this).val();
                var uefval = $("#" + this + "-paired-unencrypted").val();
                if(fval !== uefval)
                {
                    var encObject = {"fieldName":"", "fieldValue":"", "encryptedValue":""};
                    encObject.fieldName = "" + this;
                    encObject.fieldValue = fval;
                    encObjects.push(encObject);
                    encStrings.push(fval);
                }
            });

            if(encObjects.length === 0)
                return { "call_made" : false };

            var success = false;
            $.PercUtilService.encryptStrings(encStrings, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    $(encObjects).each(function(){
                        $("#" + this.fieldName + "-paired-unencrypted").val(this.fieldValue);
                        $("#" + this.fieldName + "-paired-encrypted").val(result[this.fieldValue]);
                    });
                    success = true;
                }
                else
                {
                    // Error during the AJAX call. For whatever reason, the data received back was not processable.
                    // @debugging Check to see if server is up, and if returned data is in valid JSON format.
                    success = false;
                    if(!async)
                    {
                        // If the AJAX call was made on blur, we'll allow it to silently fail.
                        // We always get a second shot during save, and no point bothering the
                        // user every time they blur the field.
                        // On the other hand, if we're already doing the syncronous call during
                        // the save operation, we should alert the user that there's a problem.
                        $.perc_utils.alert_dialog({
                            title : 'Error',
                            content : 'Error saving form.  Please try again later.'
                        });
                    }
                }
            }, async);

            return { "call_made" : true, "call_success" : success, "call_async" : async };
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
    /******************** Entry Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercEntryFieldControl = function()
    {
        var entryControlAPI = $.extend({},$.PercFieldControlInterface);
        entryControlAPI.label = "Entry field";
        entryControlAPI.getFieldData = function($fieldElem)
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
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
                if (fieldData.defaultValue === undefined || fieldData.defaultValue === '' || fieldData.defaultValue === null) {
                    fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").attr('placeholder');
                }
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_ENTRY;
            return fieldData;
        };
        entryControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            var defValue;

            if(fieldData && isExtended)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_ENTRY).
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
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(defValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_ENTRY).
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
                        attr('name', 'defaultValue').
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_ENTRY).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(this.label + ' ' + $.PercFormConstants.DEFAULT_LABEL_CONST)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('')
                        )
                    )
                );
            }
            return fieldEditorHtml;

        };
        entryControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var labelForValue = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                labelForValue = fieldData.prefs.id['perc-field-id-value'];
            }
            else {
                labelForValue = 'field-' + fieldData.name + '-input-container';
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
                    attr('name', nameValue).
                    attr('id', labelForValue).
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
        entryControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.MAX_CHAR, defaults:{"perc-validate-max-char":"yes","perc-field-max-char-value":2048}},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}}
            ];
        };
        return entryControlAPI;
    };

    //////////////////////////////////////////////////////////
    /******************** Hidden Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercHiddenFieldControl = function()
    {
        var hiddenControlAPI = $.extend({},$.PercFieldControlInterface);
        hiddenControlAPI.label = "Hidden field";
        hiddenControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            var fieldName = "";
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_NAME_CLASS).text());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }
            else
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("input[name='fieldName']").val());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            }


            fieldData.type = $.PercFormConstants.FIELD_CONTROL_HIDDEN;
            return fieldData;
        };
        hiddenControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            var defValue;
            if(fieldData && isExtended)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HIDDEN).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'fieldName').
                            attr('maxlength', '255').
                            addClass('defaultFocus').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(fieldData.name)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<input/>').
                            attr('type', 'text').
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            val(defValue)
                        )
                    )
                );
                $.perc_filterFieldText(fieldEditorHtml.find("input[name='fieldName']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }
            else if(fieldData)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>')
                        .text("Hidden Field")
                        .addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS)
                );

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HIDDEN).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text(fieldData.name)
                    ).
                    append(
                        $('<div class = "perc-form-datadisplay"/>').
                        attr('name', 'defaultValue').
                        addClass($.PercFormConstants.INPUT_CLASS).
                        text(defValue)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HIDDEN).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text("Hidden field name")
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text('Hidden field value')
                        )
                    )
                );
                $.perc_filterField(fieldEditorHtml.find("input[id='perc-form-label-field-name']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }
            return fieldEditorHtml;

        };
        hiddenControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS)
                .append(
                    $('<div/>').
                    addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                    append(
                        $('<input/>').
                        attr('type', 'hidden').
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
            renderedHtml.find('.' + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };

        hiddenControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return hiddenControlAPI;
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
        honeypotControlAPI.label = "Honeypot field";
        $.PercFormVariables.IsHoneypotEnabled = 1;
        honeypotControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            var fieldName;
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_NAME_CLASS).text());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }
            else
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("div[name='fieldName']").text());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_HONEYPOT;
            return fieldData;
        };
        honeypotControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            var defValue;

            if(fieldData && isExtended)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<div/>').
                            attr('name', 'fieldName').
                            text(fieldData.name)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<div/>').
                            attr('style','display:none;').
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text(defValue)
                        )
                    )
                );
                $.perc_filterFieldText(fieldEditorHtml.find("div[name='fieldName']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }
            else if(fieldData)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>')
                        .text("Honeypot")
                        .addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS)
                );

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('style', 'display:none;').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text(fieldData.name)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_HONEYPOT).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text("Honeypot")
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', 'defaultValue').
                            text("Honeypot")
                        )
                    )
                );
                $.perc_filterField(fieldEditorHtml.find("div[id='perc-form-label-field-name']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }

            return fieldEditorHtml;
        };
        honeypotControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS)
                .append(
                    $('<div/>').
                    addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                    append(
                        $('<input/>').
                        attr('type', 'hidden').
                        attr('name', nameValue).
                        attr('id', 'topyenoh').
                        val(defValue)
                    )
                );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            renderedHtml.find('.' + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;

            return renderedHtml;
        };

        honeypotControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.NAME}
            ];
        };
        return honeypotControlAPI;
    };

//////////////////////////////////////////////////////////
//******************** reCaptcha Field ********************
//////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     * */
    $.PercRecaptchaFieldControl = function()
    {
        var recaptchaControlAPI = $.extend({},$.PercFieldControlInterface);
        recaptchaControlAPI.label = "reCaptcha field";
        $.PercFormVariables.IsRecaptchaEnabled = 1;
        recaptchaControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            var fieldName = "";
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_NAME_CLASS).text());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }
            else
            {
                fieldName = $.perc_autoFillTextFilters.URL($fieldElem.find("div[name='fieldName']").text());
                fieldData.name = fieldName;
                fieldData.label = fieldName;
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }

            fieldData.type = $.PercFormConstants.FIELD_CONTROL_RECAPTCHA;
            return fieldData;
        };
        recaptchaControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            var defValue;
            if(fieldData && isExtended)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_RECAPTCHA).
                    addClass($.PercFormConstants.FIELD_EDITOR_EXT_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    addClass($.PercFormConstants.FORM_LABEL_CLASS).
                    append(
                        $('<div/>').
                        append(
                            $('<div/>').
                            attr('name', 'fieldName').
                            text(fieldData.name)
                        )
                    ).
                    append(
                        $('<div/>').
                        append(
                            $('<div/>').
                            attr('style','display:none;').
                            attr('name', 'defaultValue').
                            addClass($.PercFormConstants.INPUT_CLASS).
                            text(defValue)
                        )
                    )
                );
                $.perc_filterFieldText(fieldEditorHtml.find("div[name='fieldName']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }
            else if(fieldData)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>')
                        .text("reCaptcha")
                        .addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS)
                );

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_RECAPTCHA).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('style', 'display:none;').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text(fieldData.name)
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_RECAPTCHA).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_NAME_CLASS).
                        text("reCaptcha")
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<div class = "perc-form-datadisplay"/>').
                            attr('name', 'defaultValue').
                            text("reCaptcha")
                        )
                    )
                );
                $.perc_filterField(fieldEditorHtml.find("div[id='perc-form-label-field-name']"), $.perc_autoFillTextFilters.IDNAMECDATAALPHA);
            }

            return fieldEditorHtml;
        };
        recaptchaControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }


            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS)
                .append(
                    $('<div/>').
                    addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).
                    addClass("g-recaptcha")
                );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('div'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            return renderedHtml;
        };

        recaptchaControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.RECAPTCHA_SITEKEY},
                {pref: $.PercFormFieldPref.RECAPTCHA_THEME},
                {pref: $.PercFormFieldPref.RECAPTCHA_SIZE},
                {pref: $.PercFormFieldPref.RECAPTCHA_TABINDEX},
                {pref: $.PercFormFieldPref.RECAPTCHA_CALLBACK},
                {pref: $.PercFormFieldPref.RECAPTCHA_EXPIREDCALLBACK},
                {pref: $.PercFormFieldPref.RECAPTCHA_ERRORCALLBACK}
            ];
        };
        return recaptchaControlAPI;
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
        textareaControlAPI.label = "Text box";
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
            fieldData.defaultValue = $fieldElem.find("textarea[name='defaultValue']").val();
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
                            attr('name', 'defaultValue').
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
                            attr('name', 'defaultValue').
                            attr('cols', '50').
                            attr('rows', '3').
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
                        text(this.label + ' ' + $.PercFormConstants.DEFAULT_LABEL_CONST)
                    ).
                    append(
                        $('<div>').
                        append(
                            $('<textarea/>').
                            attr('name', 'defaultValue').
                            attr('cols', '50').
                            attr('rows', '3').
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
            var formControl = $.PercFormController();

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var labelForValue = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                labelForValue = fieldData.prefs.id['perc-field-id-value'];
            }
            else {
                labelForValue = 'field-' + fieldData.name + '-input-container';
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
                    attr('id', labelForValue).
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
                {pref: $.PercFormFieldPref.REQUIRED},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.MAX_CHAR, defaults:{"perc-field-max-char-value": 2048}},
                {pref: $.PercFormFieldPref.HEIGHT},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{"perc-field-width-value": 150}}
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
            /* unlike the other fields, applying the nameValue variable here would not make sense.
             * this is because the fieldData.name is used to derive the nameValue and the fieldData.name
             * variable is applied from the label of the field.  The label in the text area, for example,
             * would be '<p>Text</p>' which does not make sense for the name. */

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<span/>').
                html(fieldData.label).
                text(fieldData.defaultValue)
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
            return [
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return textControlAPI;
    };


/////////////////////////////////////////////////////////
    /****************** Data Drop Down Field ****************/
/////////////////////////////////////////////////////////

    /**
     * @See $.PercFieldControlInterface
     */
    $.PercDataDropDownControl = function()
    {
        var DataDropDownControlAPI = $.extend({},$.PercFieldControlInterface);
        DataDropDownControlAPI.label="Data Drop Down field";
        DataDropDownControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            fieldData.name = $fieldElem.attr("name");
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }

            //fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_DATADROPDOWN;

            //Push the options into the data structure
            var i = 0;
            fieldData.options = [];
            fieldData.defaultValue = 0;
            var opts = $fieldElem.find('.dataDropDownSelect');
            opts.each(function(){
                if($(this).val().trim() !== "")
                {
                    fieldData.options[i] = {
                        value : $(this).val(),
                        label : $(this).val()
                    };
                    if($(this).is('option:selected') || $(this).prev().is('input[type=radio]:checked'))
                    {
                        fieldData.defaultValue = fieldData.options[i].value;
                    }
                    i++;
                }
            });
            return fieldData;
        };
        DataDropDownControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = "";
            var fieldEditorHtmlWrapper = $("<div class = 'perc-form-field-wrapper'></div>");
            if(fieldData && isExtended)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DATADROPDOWN + "' class=' form-widget-label " + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>").attr('name', $.PercFormController().generateNameFromLabel(fieldData.label));
                fieldEditorHtml.append( $("<div>").append( $("<input size = '38' class = 'perc-form-datadisplay defaultFocus' type='text' maxlength='255' name='fieldLabel'/>").val(fieldData.label) ) );
                fieldEditorHtml.append("<div class='form-widget-label' style='padding-right:10px;margin-bottom:-8px;' " + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " >Select a widget</div></br>");
                s = $('<select class="dataDropDownSelect" />');
                var opt = '';
                $.getJSON($.perc_paths.ASSET_TYPES + "?filterDisabledWidgets=yes", function(data) {
                    var len = data.WidgetContentType.length;
                    gData = data;
                    s.append(opt);
                    for (let i = 0; i< len; i++) {
                        opt = $("<option value='" + data.WidgetContentType[i].contentTypeName +"'>" + data.WidgetContentType[i].contentTypeName + "</option>");
                        s.append(opt);
                    }
                });
                fieldEditorHtml.append(s);
                fieldEditorHtml.append('</select>');
                fieldEditorHtml.append("<br />");

                //Add the final div
                fieldEditorHtml.append("<div class='fix' style='clear:both;'></div>");
                // For every button add the corresponding events.
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else if(fieldData)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DATADROPDOWN + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div class=' " + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label' />").text(fieldData.label) );
                var DataDropDown = $('<select style = "margin-top:5px"/>').attr('disabled', 'disabled');
                $.each(fieldData.options,function(i,e) {
                        var o = $('<option/>').val(e.value).text(e.value);
                        o.addClass('dataDropDownSelect');
                        if (fieldData.defaultValue === o.val()) {
                            o.get(0).setAttribute('selected','selected');
                            o.get(0).selected = true;
                        }
                        DataDropDown.append(o);
                    }
                );
                DataDropDown.wrap('<div />');
                fieldEditorHtml.append(DataDropDown);
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DATADROPDOWN + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div id = 'perc-form-label-field-name' class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label' />").text(this.label + ' ' + $.PercFormConstants.DEFAULT_LABEL_CONST) );
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            return fieldEditorHtmlWrapper;
        };
        DataDropDownControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                    attr('for', 'email-to').
                    text(fieldData.label)
                )
            ).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS)
            );
            var DataDropDownOptions = $('<input type="hidden"/>').
            attr('name', fieldData.name).
            attr('value', fieldData.defaultValue).
            attr('data-type', 'option-ddd');

            renderedHtml.find('.' + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).eq(0).append(DataDropDownOptions);

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('select'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            //Made DataDropDown as required by default @see CM-6061
            var vals = [];
            vals["perc-field-required"] = true;
            $.PercFormFieldPref.REQUIRED.onRender(renderedHtml.find('select'), vals, null);

            // Deep magic to deal with Firefox and defaultValue nonsense.  .attr() is ineffective, but .setAttribute() works.

            var checkOptionPresence = renderedHtml.find('#' + 'field-' + fieldData.name + '-data-drop-down-container' + ' ' + 'select option:selected').length;
            if(checkOptionPresence) {
                renderedHtml.find('#' + 'field-' + fieldData.name + '-data-drop-down-container' + ' ' + 'select option:selected').get(0).setAttribute('selected', 'selected');
            }
            return renderedHtml;
        };
        DataDropDownControlAPI.getAvailablePrefs = function()
        {
            return [
                /* Returning nothing here as the data drop down HTML is all configured/generated via PSPageUtils.java
                 * None of the prefs ever worked here to begin with.
                 * In fact, most of the code in the getRenderedField function of the data drop down can be removed.
                 * Needs tested first.
                 * */
            ];
        };
        return DataDropDownControlAPI;
    };

    /////////////////////////////////////////////////////////
    /******************** Drop Down Field ******************/
    /////////////////////////////////////////////////////////

    /**
     * @See $.PercFieldControlInterface
     */
    $.PercDropDownControl = function()
    {
        var dropDownControlAPI = $.extend({},$.PercFieldControlInterface);
        dropDownControlAPI.label="Drop down field";
        dropDownControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            fieldData.name = $fieldElem.attr("name");
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }

            //fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_DROPDOWN;

            //Push the options into the data structure
            var i = 0;
            fieldData.options = [];
            fieldData.defaultValue = 0;
            var opts = $fieldElem.find('.option-dd');
            opts.each(function(){
                if($(this).val().trim() !== "")
                {
                    fieldData.options[i] = {
                        value : $(this).val(),
                        label : $(this).val()
                    };
                    if($(this).is('option:selected') || $(this).prev().is('input[type=radio]:checked'))
                    {
                        fieldData.defaultValue = fieldData.options[i].value;
                    }
                    i++;
                }
            });
            return fieldData;
        };
        dropDownControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = "";
            var fieldEditorHtmlWrapper = $("<div class = 'perc-form-field-wrapper'></div>");
            if(fieldData && isExtended)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DROPDOWN + "' class=' form-widget-label " + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>").attr('name', $.PercFormController().generateNameFromLabel(fieldData.label));
                fieldEditorHtml.append( $("<div>").append( $("<input size = '38' class = 'perc-form-datadisplay defaultFocus' type='text' maxlength='255' name='fieldLabel'/>").val(fieldData.label) ) );
                fieldEditorHtml.append("<div class='form-widget-label' style='color:#E6E6E9;float:left;margin-left:auto;margin-right:auto;padding-right:10px;' " + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " >Def</div>");
                fieldEditorHtml.append("<div class='form-widget-label' style='float:left;padding-right:10px;' " + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " >Enter your custom options</div></br>");
                fieldEditorHtml.append("<br />");
                //Load variables to be used
                var newOption = "<div class='perc-div-rowOption'><input type='radio' name='defaultValue' value='' />" +
                    "<input size='33' class='perc-form-datadisplay option-dd' type='text' name='defaultValue' maxlength='255' value='' />" +
                    "</div>";
                var addControl = "<img src='../rx_resources/widgets/form/images/form-plus.png' class='control-img-button add-control' alt='Add field' title='Add field'/>";
                var deleteControl = "<img src='../rx_resources/widgets/form/images/form-minus.png' class='control-img-button delete-control' alt='Delete field' title='Delete field'/>";

                // Callbacks for minus and add buttons.
                var delete_function = function(event){
                    if ($(this).parent().find(".add-control").is('.add-control')) {
                        $(this).parent().prev().append(addControl);
                        $(this).parent().prev().find(".add-control").on("click",function(evt){
                            add_function(evt);
                        });
                        if (!$(this).parent().prev().prev().is("div")) {
                            $(this).parent().prev().find('.delete-control').remove();
                        }
                    }
                    else{
                        if (!$(this).parent().prev().is("div") && $(this).parent().next().find(".add-control").is('.add-control')) {
                            $(this).parent().next().find('.delete-control').remove();
                        }}
                    $(this).parent().remove();
                };
                var add_function = function(event){
                    var newRow = $(newOption).append(deleteControl);
                    newRow.append(addControl);
                    if (!$(this).parent().prev().is('div')) {
                        $(this).parent().append(deleteControl);
                        $(this).parent().find(".delete-control").on("click",function(evt){
                            delete_function(evt);
                        });
                    }
                    newRow.find(".delete-control").on("click",function(evt){
                        delete_function(evt);
                    });
                    newRow.find(".add-control").on("click",function(evt){
                        add_function(evt);
                    });
                    $(this).parent().parent().find('.fix').before(newRow);
                    $(this).remove();

                    //Fix the height of the input fields
                    $("input[type = 'text']").css('height', 'auto');

                };

                // If there are no options in dropdown, then add just one.
                if (fieldData.options.length===0){
                    fieldEditorHtml.append($(newOption).append(addControl));
                }
                else
                {
                    $.each(fieldData.options,function(i,e) {
                            var divOpt = $(newOption);
                            var o = divOpt.find('.option-dd');
                            o.val(e.value);
                            if (fieldData.defaultValue === o.val()) {
                                divOpt.find('input[type=radio]').get(0).setAttribute('checked','checked');
                            }
                            if ((fieldData.options.length > 1)){
                                divOpt.append(deleteControl);
                            }
                            if (i === (fieldData.options.length - 1)) {
                                divOpt.append(addControl);
                            }
                            fieldEditorHtml.append(divOpt);
                        }
                    );
                }
                //Add the final div
                fieldEditorHtml.append("<div class='fix' style='clear:both;'></div>");
                // For every button add the corresponding events.
                fieldEditorHtml.find(".delete-control").on("click",function(evt){
                    delete_function(evt);
                });
                fieldEditorHtml.find(".add-control").on("click",function(evt){
                    add_function(evt);
                });
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else if(fieldData)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DROPDOWN + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div class=' " + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label' />").text(fieldData.label) );
                var dropDown = $('<select style = "margin-top:5px"/>').attr('disabled', 'disabled');
                $.each(fieldData.options,function(i,e) {
                        var o = $('<option/>').val(e.value).text(e.value);
                        o.addClass('option-dd');
                        if (fieldData.defaultValue === o.val()) {
                            o.get(0).setAttribute('selected','selected');
                            o.get(0).selected = true;
                        }
                        dropDown.append(o);
                    }
                );
                dropDown.wrap('<div />');
                fieldEditorHtml.append(dropDown);
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_DROPDOWN + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div id = 'perc-form-label-field-name' class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label' />").text(this.label + ' ' + $.PercFormConstants.DEFAULT_LABEL_CONST) );
                fieldEditorHtml.append("<div><select disabled='disabled' class = 'perc-form-datadisplay' name='defaultValue'> <option class='option-dd' value='Selection 1'>Selection 1</option> " +
                    "<option class='option-dd' value='Selection 2'>Selection 2</option> <option class='option-dd' value='Selection 3'>Selection 3</option></select></div>");
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            return fieldEditorHtmlWrapper;
        };
        dropDownControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var labelForValue = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                labelForValue = fieldData.prefs.id['perc-field-id-value'];
            }
            else {
                labelForValue = 'field-' + fieldData.name + '-input-container';
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
            ).
            append(
                $('<div/>').
                addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS)
            );
            var dropDownOptions = $('<select/>').
            attr('name', nameValue).
            attr('id', labelForValue);
            for( var index in fieldData.options)
            {
                var option = fieldData.options[index];
                dropDownOptions.append(
                    $('<option/>').
                    val(option.value).
                    text(option.label)
                );
            }
            dropDownOptions.val(fieldData.defaultValue);

            renderedHtml.find('.' + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS).eq(0).append(dropDownOptions);


            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('select'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            //Made dropdown as required by default @see CM-6061
            var vals = [];
            vals["perc-field-required"] = true;
            $.PercFormFieldPref.REQUIRED.onRender(renderedHtml.find('select'), vals, null);

            // Deep magic to deal with Firefox and defaultValue nonsense.  .attr() is ineffective, but .setAttribute() works.

            var checkOptionPresence = renderedHtml.find('#' + 'field-' + fieldData.name + '-drop-down-container' + ' ' + 'select option:selected').length;
            if(checkOptionPresence) {
                renderedHtml.find('#' + 'field-' + fieldData.name + '-drop-down-container' + ' ' + 'select option:selected').get(0).setAttribute('selected', 'selected');
            }
            return renderedHtml;
        };
        dropDownControlAPI.getAvailablePrefs = function()
        {
            return [
                //{pref: $.PercFormFieldPref.REQUIRED},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.CLASS},
                {pref: $.PercFormFieldPref.WIDTH, defaults:{'perc-field-width-value':150}}
            ];
        };
        return dropDownControlAPI;
    };

/////////////////////////////////////////////////////////
    /******************** CheckBox Field ******************/
/////////////////////////////////////////////////////////

    /**
     * @See $.PercCheckBoxControlInterface
     */
    $.PercCheckBoxControl = function()
    {
        var checkboxControlAPI = $.extend({},$.PercFieldControlInterface);
        checkboxControlAPI.label="Checkboxes field";
        checkboxControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) == 'object')
            {
                fieldData.prefs = prefs;
            }
            fieldData.name = $fieldElem.attr("name");
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }
            fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_CHECKBOX;

            //Push the options into the data structure
            fieldData.options = [];
            fieldData.defaultValue = 0;
            var opts = $fieldElem.find('.option-dd');
            opts.each(function(i){
                    if($(this).val().trim() !== "")
                    {
                        fieldData.options[i] = {
                            value : $(this).val(),
                            checked : $(this).val()
                        };
                        if($(this).is(':checked') || $(this).prev().is('input[type=checkbox]:checked'))
                        {
                            fieldData.options[i].checked = 'true';
                        }
                        else
                        {
                            fieldData.options[i].checked = 'false';
                        }
                    }
                }
            );


            return fieldData;
        };
        checkboxControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = "";
            var fieldEditorHtmlWrapper = $("<div class = 'perc-form-field-wrapper'></div>");
            if(fieldData && isExtended)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_CHECKBOX + "' class=' form-widget-label " + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS + " " + $.PercFormConstants.FIELD_CLASS + "'></div>").attr('name', $.PercFormController().generateNameFromLabel(fieldData.label));
                fieldEditorHtml.append( $("<div/>").append( $("<input size = '38' class = 'defaultFocus perc-form-datadisplay' type='text' maxlength='255' name='fieldLabel' />").val(fieldData.label) ) );
                fieldEditorHtml.append("<span></span>");
                //Load variables to be used
                var newOption = "<div class='perc-div-rowOption'><input type='checkbox' name='defaultValue' value='' />" +
                    "<input size='33' class='perc-form-datadisplay option-dd' type='text' name='defaultValue' maxlength='255' value='' />" +
                    "</div>";
                var addControl = "<img src='../rx_resources/widgets/form/images/form-plus.png' class='control-img-button add-control' alt='Add field' title='Add field'/>";
                var deleteControl = "<img src='../rx_resources/widgets/form/images/form-minus.png' class='control-img-button delete-control' alt='Delete field' title='Delete field'/>";

                // Callbacks for minus and add buttons.
                var delete_function = function(event){
                    if ($(this).parent().find(".add-control").is('.add-control')) {
                        $(this).parent().prev().append(addControl);
                        $(this).parent().prev().find(".add-control").on("click",
                            function(evt){
                                add_function(evt);
                            });
                        if (!$(this).parent().prev().prev().is("div")) {
                            $(this).parent().prev().find('.delete-control').remove();
                        }
                    }
                    else{
                        if (!$(this).parent().prev().is("div") && $(this).parent().next().find(".add-control").is('.add-control')) {
                            $(this).parent().next().find('.delete-control').remove();
                        }}
                    $(this).parent().remove();
                };
                var add_function = function(event){
                    var newRow = $(newOption).append(deleteControl);
                    newRow.append(addControl);
                    if (!$(this).parent().prev().is('div')) {
                        $(this).parent().append(deleteControl);
                        $(this).parent().find(".delete-control").on("click",
                            function(evt){
                                delete_function(evt);
                            });
                    }
                    newRow.find(".delete-control").on("click",
                        function(evt){
                            delete_function(evt);
                        });

                    newRow.find(".add-control").on("click", function(evt){
                        add_function(evt);
                    });
                    $(this).parent().parent().find('.fix').before(newRow);
                    $(this).remove();

                    //Fix the height of the input fields
                    if($.browser.msie){
                        $("input[type = 'text']").css('height', '11px');
                    }else{
                        $("input[type = 'text']").css('height', 'auto');
                    }
                };

                // If there are no options in checkbox, then add just one.
                if (fieldData.options.length===0){
                    fieldEditorHtml.append($(newOption).append(addControl));
                }
                else
                {
                    $.each(fieldData.options,function(i,e) {
                            var divOpt = $(newOption);
                            var o = divOpt.find('.option-dd');
                            o.val(e.value);
                            if (fieldData.options[i].checked === 'true') {
                                divOpt.find('input[type=checkbox]').attr('checked','checked');
                            }
                            if ((fieldData.options.length > 1)){
                                divOpt.append(deleteControl);
                            }
                            if (i === (fieldData.options.length - 1)) {
                                divOpt.append(addControl);
                            }
                            fieldEditorHtml.append(divOpt);
                        }
                    );
                }
                //Add the final div
                fieldEditorHtml.append("<div class='fix' style='clear:both;'></div>");
                // For every button add the corresponding events.
                fieldEditorHtml.find(".delete-control").on("click",function(evt){
                    delete_function(evt);
                });
                fieldEditorHtml.find(".add-control").on("click", function(evt){
                    add_function(evt);
                });
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else if(fieldData)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_CHECKBOX + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label'/>").text(fieldData.label) );

                $.each(fieldData.options,function(i,e) {
                        //fieldEditorHtml.append("<div class='perc-form-datadisplay'> </div>");
                        var newRow = $("<div class='perc-div-rowOption'>");
                        var check = $('<input type="checkbox"/>');
                        // Create and then set any other attributes
                        check.attr('class', 'option-dd');
                        check.attr('disabled', 'disabled');
                        check.attr('value', e.value);
                        check.attr('name', 'DefaultValue');
                        check.attr('id', e.value);
                        //Check if the checkbox must be checked
                        if (fieldData.options[i].checked === 'true') {
                            check.attr('checked', 'checked');
                        }
                        //Define variable for label
                        var labelRadio = $('<div/>').append( $("<label/>").attr('for', e.value).text(e.value) ).html(); // $('<div/>').append().html() hack to get source.
                        newRow.append(check);
                        newRow.append(labelRadio);
                        newRow.append("</div>");
                        fieldEditorHtml.append(newRow);
                    }
                );
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_CHECKBOX + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append("<div id = 'perc-form-label-field-name' class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label'>Checkboxes field label</div>");
                fieldEditorHtml.append("<div><input disabled = 'disabled' size = '33' class = 'option-dd' type='checkbox' name='defaultValue' value='Item 1' id='option1'/> " +
                    "<label for='option1'>Item 1</label></div>");
                fieldEditorHtml.append("<div><input disabled = 'disabled' size = '33' class = 'option-dd' type='checkbox' name='defaultValue' value='Item 2' id='option2'/> " +
                    "<label for='option2'>Item 2</label></div>");
                fieldEditorHtml.append("<div><input disabled = 'disabled' size = '33' class = 'option-dd' type='checkbox' name='defaultValue' value='Item 3' id='option2'/> " +
                    "<label for='option2'>Item 3</label></div>");
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            return fieldEditorHtmlWrapper;
        };
        checkboxControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var renderedHtml = $('<fieldset/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<legend/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                        //attr('for', fieldData.prefs && fieldData.prefs.name ? fieldData.prefs.name['perc-field-name-value'] :'field-' + fieldData.name + '-input-container').
                        text(fieldData.label)
                )
            );
            var checkBoxesHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS);
            var thisRow = {};
            for (var index in fieldData.options) // Array of Objects
            {
                var option = fieldData.options[index];
                var labelForValue = 'field-' + fieldData.name + '-option-' + formControl.generateNameFromLabel(option.value);
                thisRow = $('<div/>').
                append(
                    $('<div/>').
                    append(
                        $('<input/>').
                        attr('type', 'checkbox').
                        attr('id', labelForValue).
                        attr('name', nameValue).
                        val(option.value)
                    )
                ).
                append(
                    $('<div/>').
                    append(
                        $('<label/>').
                        attr('for', labelForValue).
                        text(option.value)
                    )
                );
                if(option.checked === "true" || option.checked === true) // It can be provided as either a string or a bool :(
                {
                    // Fix Firefox Bug involving rendering html and defaultValue/value
                    thisRow.find('input').attr('checked', 'checked').get(0).defaultChecked=true;
                }
                checkBoxesHtml.append(thisRow);
            }

            renderedHtml.append(checkBoxesHtml);

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            return renderedHtml;
        };
        checkboxControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return checkboxControlAPI;
    };


    /////////////////////////////////////////////////////////
    /******************** Radio Button Field ***************/
    /////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */

    $.PercRadioControl = function()
    {
        var radioControlAPI = $.extend({},$.PercFieldControlInterface);
        radioControlAPI.label = "Radio button field";
        var uid = $.PercFormVariables.LastCounterValue;

        radioControlAPI.generateUID = function()
        {
            uid++;
            //Add control to check if the name already exists in the form
            var uniqueName = 'defaultName-'+uid;
            var nameElements = $('#' + $.PercFormConstants.FORM_DND_FIELDS + '  input[name='+ uniqueName +']');
            if(nameElements.length >= 1 || $.PercFormVariables.LastRadioName === uniqueName)
            {
                return this.generateUID();
            }
            $.PercFormVariables.LastRadioName = uniqueName;
            $.PercFormVariables.LastCounterValue = uid;
            return uniqueName;
        };

        radioControlAPI.getFieldData = function($fieldElem)
        {
            var fieldData = {};
            var prefs = $fieldElem.data($.PercFormConstants.PREFERENCES_KEY);
            if(typeof(prefs) === 'object')
            {
                fieldData.prefs = prefs;
            }
            fieldData.name = $fieldElem.attr("name");
            if($fieldElem.hasClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS))
            {
                fieldData.label = $fieldElem.find("." + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
            }
            fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_RADIO;

            //Push the options into the data structure
            var i = 0;
            fieldData.options = [];
            fieldData.defaultValue = '';
            var formWrapper = '';
            fieldData.defaultName = this.generateUID();
            var opts = $fieldElem.find('.option-dd');
            opts.each(function(){
                    if($(this).val().trim() !== "")
                    {
                        fieldData.options[i] = {
                            value : $(this).val(),
                            label : $(this).val()
                        };
                        if($(this).is(':checked') || $(this).prev().is('input[type=radio]:checked'))
                        {
                            fieldData.defaultValue = $(this).val();
                        }
                        i++;
                    }
                }
            );
            return fieldData;
        };
        radioControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = "";
            var fieldEditorHtmlWrapper = $("<div class = 'perc-form-field-wrapper'></div>");
            if(fieldData && isExtended)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_RADIO + "' class='form-widget-label " + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS + " " + $.PercFormConstants.FIELD_CLASS + "'></div>").attr('name',  $.PercFormController().generateNameFromLabel(fieldData.label));
                fieldEditorHtml.append( $("<div/>").append( $("<input size = '38' class = 'defaultFocus perc-form-datadisplay' type='text' maxlength='255' name='fieldLabel' />").val(fieldData.label) ) );
                fieldEditorHtml.append("<span></span>");

                //Load variables to be used
                var newOption = "<div class='perc-div-rowOption'><input type='radio' name='"+ fieldData.defaultName  +"' value='' />" +
                    "<input size='34' class='perc-form-datadisplay option-dd' type='text' maxlength='255' value='' />" +
                    "</div>";
                $(newOption).find('input[type=text]').attr('name', fieldData.defaultName);
                newOption = $('<div/>').append(newOption).html(); // $('<div/>').append().html() hack to get source.
                var addControl = "<img src='../rx_resources/widgets/form/images/form-plus.png' class='control-img-button add-control' alt='Add field' title='Add field'/>";
                var deleteControl = "<img src='../rx_resources/widgets/form/images/form-minus.png' class='control-img-button delete-control' alt='Delete field' title='Delete field'/>";

                // Callbacks for minus and add buttons.
                var delete_function = function(event){
                    if ($(this).parent().find(".add-control").is('.add-control')) {
                        $(this).parent().prev().append(addControl);
                        $(this).parent().prev().find(".add-control").on("click",
                            function(evt){
                                add_function(evt);
                            });
                        if (!$(this).parent().prev().prev().is("div")) {
                            $(this).parent().prev().find('.delete-control').remove();
                        }
                    }
                    else{
                        if (!$(this).parent().prev().is("div") && $(this).parent().next().find(".add-control").is('.add-control')) {
                            $(this).parent().next().find('.delete-control').remove();
                        }}
                    $(this).parent().remove();
                };
                var add_function = function(event){
                    var newRow = $(newOption).append(deleteControl);
                    newRow.append(addControl);
                    if (!$(this).parent().prev().is('div')) {
                        $(this).parent().append(deleteControl);
                        $(this).parent().find(".delete-control").on("click",function(evt){
                            delete_function(evt);
                        });
                    }
                    newRow.find(".delete-control").on("click",function(evt){
                        delete_function(evt);
                    });
                    newRow.find(".add-control").on("click",function(evt){
                        add_function(evt);
                    });
                    $(this).parent().parent().find('.fix').before(newRow);
                    $(this).remove();

                    //Fix the height of the input fields

                    if($.browser.msie){
                        $("input[type = 'text']").css('height', '11px');
                    }else{
                        $("input[type = 'text']").css('height', 'auto');
                    }
                };

                // If there are no options in dropdown, then add just one.
                if (fieldData.options.length===0){
                    fieldEditorHtml.append($(newOption).append(addControl));
                }
                else
                {
                    $.each(fieldData.options,function(i,e) {
                            var divOpt = $(newOption);
                            var o = divOpt.find('.option-dd');
                            o.val(e.value);
                            if (fieldData.defaultValue === o.val()) {
                                divOpt.find('input[type=radio]').attr('checked','checked');
                            }
                            if ((fieldData.options.length > 1)){
                                divOpt.append(deleteControl);
                            }
                            if (i === (fieldData.options.length - 1)) {
                                divOpt.append(addControl);
                            }
                            fieldEditorHtml.append(divOpt);
                        }
                    );
                }
                //Add the final div
                fieldEditorHtml.append("<div class='fix' style='clear:both;'></div>");
                // For every button add the corresponding events.
                fieldEditorHtml.find(".delete-control").on("click",function(evt){
                    delete_function(evt);
                });
                fieldEditorHtml.find(".add-control").on("click",function(evt){
                    add_function(evt);
                });
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else if(fieldData)
            {
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_RADIO + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append( $("<div class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label' />").text(fieldData.label));
                fieldData.defaultName = this.generateUID();
                //If there is none selected value, select the first option by default
                if(fieldData.defaultValue === '' && fieldData.options.length>=1)
                {
                    fieldData.defaultValue  = fieldData.options[0].value;
                }
                $.each(fieldData.options,function(i,e) {
                        var newRow = $("<div class='perc-div-rowOption'>");
                        var radio = $('<input type="radio"/>');
                        // Create and then set any other attributes
                        radio.attr('class', 'option-dd');
                        radio.attr('disabled', 'disabled');
                        radio.attr('value', e.value);
                        radio.attr('name', fieldData.defaultName);
                        radio.attr('id', e.value);
                        //Check if the radio must be checked
                        if (fieldData.defaultValue === $(radio).val()) {
                            radio.attr('checked', 'checked');
                        }
                        //Define variable for label
                        var labelRadio = $("<label/>").attr('for', e.value).text(e.value);
                        newRow.append(radio);
                        newRow.append(labelRadio);
                        newRow.append("</div>");
                        newRow.wrap('<div />');
                        fieldEditorHtml.append(newRow);
                    }
                );
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            else
            {
                var inputName = this.generateUID();
                fieldEditorHtml = $("<div type='" + $.PercFormConstants.FIELD_CONTROL_RADIO + "' class='" + $.PercFormConstants.FIELD_EDITOR_BASIC_CLASS + " " + $.PercFormConstants.FIELD_CLASS  + "'></div>");
                fieldEditorHtml.append("<div id = 'perc-form-label-field-name' class='" + $.PercFormConstants.FIELD_EDITOR_LABEL_CLASS + " form-widget-label defaultFocus'>Radio button field label</div>");
                fieldEditorHtml.append("<div><input size = '34' disabled = 'disabled' class = 'option-dd' type='radio' name='"+ inputName  +"' value='Item 1' id='option1'/> " +
                    "<label for='option1'>Item 1</label></div>");
                fieldEditorHtml.append("<div><input size = '34' disabled = 'disabled' class = 'option-dd' type='radio' name='"+ inputName  +"' value='Item 2' id='option2'/> " +
                    "<label for='option2'>Item 2</label></div>");
                fieldEditorHtml.append("<div><input size = '34' disabled = 'disabled' class = 'option-dd' type='radio' name='"+ inputName  +"' value='Item 3' id='option3'/> " +
                    "<label for='option3'>Item 3</label></div>");
                fieldEditorHtml.find('input[type=radio]').map(function(index,DOMElement){ $(DOMElement).attr('name', inputName); });
                fieldEditorHtmlWrapper.append(fieldEditorHtml);
            }
            return fieldEditorHtmlWrapper;

        };
        radioControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var renderedHtml = $('<fieldset/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<legend/>').
                addClass($.PercFormConstants.FIELD_LABEL_CONTAINER_CLASS).
                append(
                    $('<label/>').
                        //attr('for', 'field-' + fieldData.name + '-input-container').
                        text(fieldData.label)
                )
            );
            var radioButtonsHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS);
            var thisRow = {};
            for (var index in fieldData.options) // Array of Objects
            {
                var option = fieldData.options[index];
                var labelForValue = 'field-' + fieldData.name + '-option-' + formControl.generateNameFromLabel(option.value);
                thisRow = $('<div/>').
                append(
                    $('<div/>').
                    append(
                        $('<input/>').
                        attr('type', 'radio').
                        attr('id', labelForValue).
                        attr('name', nameValue).
                        val(option.value)
                    )
                ).
                append(
                    $('<div/>').
                    append(
                        $('<label/>').
                        attr('for', labelForValue).
                        text(option.value)
                    )
                );
                if(fieldData.defaultValue === option.value)
                {
                    // Fix Firefox Bug involving rendering html and defaultValue/value
                    thisRow.find('input').attr('checked', 'checked').get(0).defaultChecked=true;
                }
                radioButtonsHtml.append(thisRow);
            }

            renderedHtml.append(radioButtonsHtml);

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            //Made radio buttons as required by default @see CM-6061
            var vals = [];
            vals["perc-field-required"] = true;
            $.PercFormFieldPref.REQUIRED.onRender(renderedHtml.find('input'), vals, null);
            return renderedHtml;
        };
        radioControlAPI.getAvailablePrefs = function()
        {
            return [
                //{pref: $.PercFormFieldPref.REQUIRED}
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return radioControlAPI;
    };

    //////////////////////////////////////////////////////////
    /******************** Date Field *********************///
    //////////////////////////////////////////////////////////
    /**
     * @See $.PercFieldControlInterface
     */
    $.PercDateControl = function()
    {
        var dateControlAPI = $.extend({},$.PercFieldControlInterface);
        dateControlAPI.label = "Date field";
        dateControlAPI.getFieldData = function($fieldElem)
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
                fieldData.defaultValue = $fieldElem.find("div[name='defaultValue']").text();
            }
            else
            {
                fieldData.label = $fieldElem.find("input[name='fieldLabel']").val();
                fieldData.defaultValue = $fieldElem.find("input[name='defaultValue']").val();
            }
            fieldData.type = $.PercFormConstants.FIELD_CONTROL_DATE;
            return fieldData;
        };
        dateControlAPI.getFieldEditor = function(fieldData, isExtended)
        {
            var fieldEditorHtml = $('<div/>').
            addClass($.PercFormConstants.FORM_FIELD_WRAPPER);
            var defValue;
            if(fieldData && isExtended)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_DATE).
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
                        $('<div class = "perc-form-datepicker" />').
                        append(
                            $('<input />').
                            attr('type', 'text').
                            attr('readonly', 'readonly').
                            attr('name', 'defaultValue').
                            addClass('perc-form-date-datadisplay').
                            addClass('form-datepicker').
                            attr('aria-label', 'Date Field').
                            attr('title', 'Date Field').
                            val(defValue)
                        )
                    )
                );
            }
            else if(fieldData)
            {
                defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_DATE).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text(fieldData.label)
                    ).
                    append(
                        $('<div class = "perc-form-datepicker" />').
                        append(
                            $('<div class = "perc-form-date-datadisplay" />').
                            attr('name', 'defaultValue').
                            attr('aria-label', 'Date Field').
                            attr('title', 'Date Field').
                            text(defValue)
                        ).
                        append(
                            $('<img src="../rx_resources/widgets/form/images/calendar.gif" alt="Date Picker" title="Date Picker">')
                        )
                    )
                );
            }
            else
            {
                fieldEditorHtml.append(
                    $('<div/>').
                    attr('type', $.PercFormConstants.FIELD_CONTROL_DATE).
                    addClass($.PercFormConstants.FIELD_EDITOR_BASIC_CLASS).
                    addClass($.PercFormConstants.FIELD_CLASS).
                    append(
                        $('<div/>').
                        attr('id', 'perc-form-label-field-name').
                        addClass($.PercFormConstants.FIELD_EDITOR_LABEL_CLASS).
                        addClass($.PercFormConstants.FORM_LABEL_CLASS).
                        text('Date field label')
                    ).
                    append(
                        $('<div class = "perc-form-datepicker">').
                        append(
                            $('<div class = "perc-form-date-datadisplay" />').
                            attr('name', 'defaultValue').
                            attr('aria-label', 'Date Field').
                            attr('title', 'Date Field').
                            text('')
                        )
                    )
                );
            }

            return fieldEditorHtml;

        };
        dateControlAPI.getRenderedField = function(fieldData)
        {
            var formControl = $.PercFormController();

            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var labelForValue = "";

            // see @CMS-3475
            if(fieldData.prefs && !$.isEmptyObject(fieldData.prefs.id)) {
                labelForValue = fieldData.prefs.id['perc-field-id-value'];
            }
            else {
                labelForValue = 'field-' + fieldData.name + '-input-container';
            }

            var defValue = fieldData.defaultValue === 'null' ? '' : fieldData.defaultValue;

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
                    addClass('form-datepicker').
                    attr('name', nameValue).
                    attr('id', labelForValue).
                    attr('readonly', 'readonly').
                    attr('aria-label', 'Date Field').
                    attr('title', 'Date Field').
                    val(defValue)
                )
            );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }

            // Fix Firefox Bug involving rendering html and defaultValue/value
            renderedHtml.find('.' + $.PercFormConstants.FIELD_INPUT_CONTAINER_CLASS + ' ' + 'input').get(0).defaultValue = fieldData.defaultValue;
            return renderedHtml;
        };
        dateControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.REQUIRED},
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
        };
        return dateControlAPI;
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
        var formControl = $.PercFormController();
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
            var nameValue = fieldData.name;

            if(fieldData.prefs && fieldData.prefs.name) {
                nameValue = fieldData.prefs.name["perc-field-name-value"] === "" ? fieldData.name : fieldData.prefs.name["perc-field-name-value"];
            }

            var renderedHtml = $('<div/>').
            addClass($.PercFormConstants.FIELD_ROW_CLASS).
            append(
                $('<input/>').
                attr('type', 'submit').
                attr('name', nameValue).
                val(fieldData.label)
            );

            var prefs = this.getAvailablePrefs();
            for(var i = 0; i < prefs.length; i++)
            {
                prefs[i].pref.onRender(renderedHtml.find('input'), (fieldData.prefs ? fieldData.prefs[prefs[i].pref.name] : undefined), prefs[i].defaults);
            }
            return renderedHtml;
        };

        submitButtonControlAPI.getAvailablePrefs = function()
        {
            return [
                {pref: $.PercFormFieldPref.ID},
                {pref: $.PercFormFieldPref.NAME},
                {pref: $.PercFormFieldPref.TITLE},
                {pref: $.PercFormFieldPref.CLASS}
            ];
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
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_ENTRY] = $.PercEntryFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_TEXT] = $.PercTextFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_RADIO] = $.PercRadioControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_TEXTAREA] = $.PercTextareaFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_DROPDOWN] = $.PercDropDownControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_DATADROPDOWN] = $.PercDataDropDownControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_CHECKBOX] = $.PercCheckBoxControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_DATE] = $.PercDateControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_SUBMIT] = $.PercSubmitButtonControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_HIDDEN] = $.PercHiddenFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_HONEYPOT] = $.PercHoneypotFieldControl;
    $.PercFieldTypes[$.PercFormConstants.FIELD_CONTROL_RECAPTCHA] = $.PercRecaptchaFieldControl;


})(jQuery);
