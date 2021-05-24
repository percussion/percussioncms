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

    /**
     *Initialize the form
     */
    function initializeForm()
    {
        var formFieldName = $(".PercFormWidget").attr("id");
        var formDataStr = $("input[name='" + formFieldName + "']").val();
        var formData = null;
        if($.trim(formDataStr).length>0)
            formData = JSON.parse(formDataStr);
            
        var editorHtml = $.PercCommentsFormController().getFormEditor(formData);
        $(".perc-form-fields-col").append(editorHtml);
        
        // Attach the action buttons to the fields
           attachActionButtons(editorHtml);
          
        // Position the Form controls menu button
        $("#perc-form-control-wrapper").prepend($("#perc-form-top-row"));
        
        // Initailize all the action buttons
        initiActionButtons();
        
        //Make form fields draggable and sortable
        dragSortFields();
        
        //Bind the click event on Menu items
        onClickMenu();
        
        //Attach the mouse over and out 
        mouseActionOnMenu();
        
        //If editor is empty load it with basic fields
        if(formData === null) {
           loadDefaultFields();   
        }
        
        //Add the Form pre submit handler
        window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(updateFormFields);
    }
    
    /**
     *Make form fields draggable and sortable
     */
    function dragSortFields() {
    
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
    }
    
    /**    
    *Attach action buttons to each field in the form editor
    */    
    function attachActionButtons(editorHtml) {

        editorHtml.find('.field-editor').each( function() {
            var fieldType = $(this).attr('type');
            if(fieldType === 'PercTextareaFieldControl' || fieldType === 'PercSubmitButtonControl' ) {
                $(this).append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img src='../rx_resources/widgets/commentsForm/images/edit.png' ></span><span class='disabled-delete-field'><img src='../rx_resources/widgets/commentsForm/images/deleteInactive.png'></span></div>");
            }
            
            else {
                var currentField = $(this);
                $(this).append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img alt='' src='../rx_resources/widgets/commentsForm/images/edit.png' ></span><span class='delete-field'><img alt='' src='../rx_resources/widgets/commentsForm/images/delete.png'></span></div>");
                getClassName(currentField);                     
            }
        });
    }
    
    function getClassName(currentField) {
        var className = "";
        var fieldType = currentField.attr('type');
        if(fieldType === "PercTextFieldControl") {
            return;
        }
        
        else if(fieldType === "PercEmailFieldControl") {
            className = "form-email-label";
        }

        else if(fieldType === "PercURLFieldControl") {
            className = "form-website-label";
        }
        
        else if(fieldType === "PercTitleFieldControl") {
                className = "form-title-label";
        }

        else if(fieldType === "PercHoneypotFieldControl") {
            className = "form-honeypot-label";
        }

        else if(fieldType === "PercUserFieldControl") {
                className = "form-username-label";
        }
        
        $("." + className).addClass(className + '-disable');
        var bgimage = $("." + className).css('background-image');
        bgimage = bgimage.replace("-over.png", "-disable.png");
        $("." + className).css('background-image', bgimage);
    }
    
    /**    
    *Initialize the Action buttons - Edit(pencil), Delete(cross) and Configure(wrench)
    */
    function initiActionButtons() {
    
            //Attach event to Edit button
        $(document).on("click", ".toggle-editor",function(){
        
            // Deactivate old extended editor, since we can only have one active at a time.
            var extEditorElem = $("." + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS);
            if (extEditorElem.length)
                deactivateEditor(extEditorElem);
            $(".defaultFocus").trigger("focus");
            
            var parent = $(this).parent().parent();
            var newElem = $.PercCommentsFormController().toggleFieldEditor(parent);
            var fieldType = parent.attr('type');
            var isComment = false;
            if(fieldType === 'PercTextareaFieldControl' || fieldType === 'PercSubmitButtonControl') {
                isComment = true;
            }
            addConfig(newElem, isComment);        
            newElem = parent.parent().replaceWith(newElem);
            $(".defaultFocus").trigger("focus");
            fieldType = parent.attr('type');
            if(fieldType === 'PercTextFieldControl') {
                tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
            }
            $("input[type = 'text']").css('height', 'auto');
        });
        
        //Attch event to Delete button
        $(document).on("click",".delete-field", function() {
            if(tinymce.EditorManager.get('elm1') && $(this).parent().parent().children('#elm1').length > 0){                
                tinymce.EditorManager.execCommand('mceRemoveEditor', true, 'elm1');
            } 
            var fieldType = $(this).parent().parent().attr('type');
            var className = "";
            if(fieldType === "PercEmailFieldControl") {
                className = "form-email-label";
                activateMenuItem(className);
            }

            else if(fieldType === "PercURLFieldControl") {
                className = "form-website-label";
                activateMenuItem(className);
            }

            else if(fieldType === "PercHoneypotFieldControl") {
                className = "form-honeypot-label";
                activateMenuItem(className);
            }
            
            else if(fieldType === "PercTitleFieldControl") {
                    className = "form-title-label";
                    activateMenuItem(className);
            }

            else if(fieldType === "PercUserFieldControl") {
                    className = "form-username-label";
                    activateMenuItem(className);
            }
            
            $(this).parent().parent().parent().remove();
        });
   
        //Attach event to Configure button
        $(document).on("click",".toggle-configure", function(){
            var controlWrapper = $(this).parent().parent().parent();
            var controlEl = $(this).parent().parent();
            var control = $.PercCommentsFormController().getControl(controlEl);           
            openPrefsDialog(controlEl, control().getAvailablePrefs());
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
    
    }
    
   /**
    *Bind the mouseover and mouseout event to menu items
    */
    function mouseActionOnMenu() {
    
        $(".perc-control-label").on("mouseenter",function(evt) {
            var bgimage = $(this).css('background-image');
            var fieldClass = $(this).attr('class');
            if(fieldClass.indexOf('disable') !== -1) {
                bgimage = bgimage.replace(".png", ".png");
                return;
            }
            else {
                   bgimage = bgimage.replace(".png", "-over.png");
            }
            
            $(this).css({'background-color': '#247297', 
                        'color':'83A9BB',
                        'background-image':bgimage,
                        'border-bottom':'1px solid #99C4D8'});       
        }).on("mouseleave",function(){
            var bgimage = $(this).css('background-image');
             bgimage = bgimage.replace(".png", ".png");
            var fieldClass = $(this).attr('class');
            if(fieldClass.indexOf('disable') !== -1) {
            $(this).css({'background-color': '#3288B0',
                        'color':'#1a5f7f',
                        'background-image':bgimage,                        
                        'border-bottom':'1px solid #99C4D8'});
            return;
            }
            else {
                 bgimage = bgimage.replace("-over.png", ".png");
                    $(this).css({'background-color': '#3288B0',
                    'color':'#ffffff',
                    'cursor': 'pointer',
                    'background-image':bgimage,                        
                    'border-bottom':'1px solid #99C4D8'});
            }    

        });
    }
    
   /**
    *Disable the menu item after user adds it to the form editor.
    */     
    function deactivateMenuItem(className , controlType) {
    
            if($("." + className).hasClass(className +'-disable')) {
                return;
            }    
            else {
                $("." + className).addClass(className + '-disable');
                var bgimage = $("." + className).css('background-image');
                bgimage = bgimage.replace("-over.png", "-disable.png");
                $("." + className).css('background-image', bgimage);
            }
            var newElem = $.PercCommentsFormController().getNewFieldEditor(controlType);
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);                
    }
    
    /**
     *Bind the click event on the menu items.
     */
    function onClickMenu() {
        //Add Text field
        $(".form-text-label").on("click",function(){
            var newElem = $.PercCommentsFormController().getNewFieldEditor("PercTextFieldControl");
            addEvents(newElem);
            $("#perc-form-dnd-fields").append(newElem);
            newElem.find(".toggle-editor").trigger("click");
            $("iframe").scrollTop(20000);
            tinymce.EditorManager.execCommand('mceAddEditor', true, 'elm1');
        });
        
        //Add URL Field
        $(".form-website-label").on("click",function(){
            deactivateMenuItem("form-website-label" , "PercURLFieldControl");
        });
        
        //Add Email field
        $(".form-email-label").on("click",function(){
            deactivateMenuItem("form-email-label" , "PercEmailFieldControl");
        });
        
        //Add Title field
        $(".form-title-label").on("click",function(){
            deactivateMenuItem("form-title-label" , "PercTitleFieldControl");
        });
        
        //Add Username field
        $(".form-username-label").on("click",function(){
            deactivateMenuItem("form-username-label" , "PercUserFieldControl");
        });

        //Add Honeypot field
        $(".form-honeypot-label").on("click",function(){
            deactivateMenuItem("form-honeypot-label" , "PercHoneypotFieldControl");
        });

    }
    /**
     * Load the form-editor with default fields
     */  
    function loadDefaultFields() {
        var newElem = $.PercCommentsFormController().getNewFieldEditor("PercTitleFieldControl");
        addEvents(newElem);
        $("#perc-form-dnd-fields").append(newElem);
        var newElem2 = $.PercCommentsFormController().getNewFieldEditor("PercHoneypotFieldControl");
        addEvents(newElem2);
        $("#perc-form-dnd-fields").append(newElem2);
        var newElem3 = $.PercCommentsFormController().getNewFieldEditor("PercURLFieldControl");
        addEvents(newElem3);
        $("#perc-form-dnd-fields").append(newElem3);
        var newElem4 = $.PercCommentsFormController().getNewFieldEditor("PercUserFieldControl");
        addEvents(newElem4);
        $("#perc-form-dnd-fields").append(newElem4);
        var newElem5 = $.PercCommentsFormController().getNewFieldEditor("PercTextareaFieldControl");
        addCommentboxEvents(newElem5);
        $("#perc-form-dnd-fields").append(newElem5);
        var newElem6 = $.PercCommentsFormController().getNewFieldEditor("PercEmailFieldControl");
        addEvents(newElem6);
        $("#perc-form-dnd-fields").append(newElem6);
        var newElem7 = $.PercCommentsFormController().getNewFieldEditor("PercSubmitButtonControl");
        addCommentboxEvents(newElem7);
        $("#perc-form-dnd-fields").append(newElem7);
        
        // Disable the menu items on load
        $(".form-title-label").addClass('form-title-label-disable');
        $(".form-email-label").addClass('form-email-label-disable');
        $(".form-username-label").addClass('form-username-label-disable');
        $(".form-honeypot-label").addClass('form-honeypot-label-disable');
        $(".form-website-label").addClass('form-website-label-disable');
    }
     
    // Activate menu item once the field is deleted from the form editor
    function activateMenuItem(className) {
        $("." + className).removeClass(className + '-disable');
        var bgimage = $("." + className).css('background-image');
        bgimage = bgimage.replace("-disable.png", ".png");
        $("." + className).css('background-image', bgimage);
    }
    
    // Decativate the Editor 
    function deactivateEditor(extEditorElem) {
        var checkEditorExt = extEditorElem.parent().children();
        var fieldType = $(checkEditorExt).attr('type');
        var newElem = $.PercCommentsFormController().toggleFieldEditor(checkEditorExt);
        if(fieldType === 'PercTextareaFieldControl' || fieldType === 'PercSubmitButtonControl' ) {
            addCommentboxEvents(newElem);
        }
        else {
            addEvents(newElem);
        }    
        checkEditorExt.parent().replaceWith(newElem);
    }

    // Add controls (Delete and Extended Edit Mode)to field
    function addEvents(elem) {
        elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img alt='' src='../rx_resources/widgets/commentsForm/images/edit.png' ></span><span class='delete-field'><img alt='' src='../rx_resources/widgets/commentsForm/images/delete.png'></span></div>");
    }
    
    function addCommentboxEvents(elem) {
       elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'><span class='toggle-editor'><img alt=''  src='../rx_resources/widgets/commentsForm/images/edit.png' ></span><span class='disabled-delete-field'><img alt='' src='../rx_resources/widgets/commentsForm/images/deleteInactive.png'></span></div>");
    }
    
     
    /**
     * Add controls (Delete and Configuration)to field
     */
    function addConfig(elem, isComment) {
        if ($.PercCommentsFormController().getControl($(elem).children().eq(0))().getAvailablePrefs().length > 0)
            configButtonHtml = "<span class='toggle-configure'><img alt=''  src='../rx_resources/widgets/commentsForm/images/configure.png' ></span>";
        else
            configButtonHtml = "<span class='toggle-configure-disabled'><img alt='' src='../rx_resources/widgets/commentsForm/images/configure2.png' ></span>";
        if(isComment) {    
            elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'>" + configButtonHtml + "<span class='disabled-delete-field'><img alt='' src='../rx_resources/widgets/commentsForm/images/deleteInactive.png'></span></div>");
        }
        else {
            elem.find(".field-editor").append("<div class = 'perc-form-ui-menu'>" + configButtonHtml + "<span class='delete-field'><img alt=''  src='../rx_resources/widgets/commentsForm/images/delete.png'></span></div>");
        }    
    }
    
    /**
     * Update the form fields
     */
 
    function updateFormFields() {
        var extEditorElem = $("." + $.PercFormConstants.FIELD_EDITOR_EXT_CLASS);
        if (extEditorElem.length)
            deactivateEditor(extEditorElem);
        $(".defaultFocus").trigger("focus");
        var success = true;

        var formData = $.PercCommentsFormController().getFormData($("."+$.PercFormConstants.FORM_CLASS));
        var formConfig = formData.config;
        //Set the formData and rendered form.
        $("#perc-content-edit-formdata").val(JSON.stringify(formData));
        var script = "<script id='form-script' type='text/javascript'>window.addEventListener('DOMContentLoaded', function() {jQuery(document).ready(function(){jQuery('form[name=commentForm]').validate({errorClass:'form-error-msg'})});});</script>";
        $("#perc-content-edit-renderedform").val(script + $("<div/>").append($.PercCommentsFormController().getRenderedForm(formData)).html().replace(/<input\s+([^>]*?)\s*>/ig, "<input $1 />")); // @TODO: This *will* break once we're serving as application/xhtml+xml.  Get rid of the .replace to fix.
        return success;
    }
   
    /**
     * Generate the form for Read-only mode
     */
       
    function renderReadOnlyForm() {
        var formFieldName = $(".PercFormWidgetReadOnly").attr("id");
        var formDataStr = $("input[name='" + formFieldName + "']").val();
        var formData = null;
        if($.trim(formDataStr).length>0)
            formData = JSON.parse(formDataStr);
        
        var editorHtml = $.PercCommentsFormController().getFormEditor(formData);
        
        $(".perc-form-fields-col").append(editorHtml);
        $(".field-editor-basic").parent().toggleClass('perc-form-field-wrapper');
        $("#perc-form-help-text").hide();

   }
   
    /**
     * Setup the preference dialog
     */   
   
    function openPrefsDialog(control, preferences, values) {        
        if(prefsDialog == null)   // Only create dialog once.
        {
            prefsDialog = $('<div id="perc-prefs-dialog"></div>')
                .html('<div id="perc-prefs-dialog-container"></div>')
                .dialog({
                    autoOpen: false,
                    title: 'Configure Comments Form Field',
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
        var preferenceVals = control.data('preferences');
        if(typeof(preferenceVals) != 'object')
            preferenceVals = {};
        var container = prefsDialog.find("#perc-prefs-dialog-container");
        container.children().remove();
        for(i = 0; i < preferences.length; i++)
        {         
            preferences[i].pref.addControl(container, preferenceVals[preferences[i].pref.name], preferences[i].defaults);         
        }
        $("#perc-field-prefs-apply").off("click").on('click', function(){
            for(i = 0; i < preferences.length; i++)
            {
                preferenceVals[preferences[i].pref.name] = preferences[i].pref.onApply(preferenceVals,  preferences[i].defaults);         
            }
            control.data('preferences', preferenceVals);
            prefsDialog.dialog('close');      
        });
        prefsDialog.dialog('open');
        
        // Fix for horizontal scroll bar at bottom of iframe during overlay

        $(".ui-widget-overlay").css('width', '').css('max-width', '98%');
   
    }    
})(jQuery);
