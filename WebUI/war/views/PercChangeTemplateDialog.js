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
 * Change template dialog, see API for the available methods and behavior.
 */
(function($)
{

    $.PercChangeTemplateDialog = function()
    {
        var changeTemplateDialogApi = {
            /**
             * Opens the new page dialog and creates a new page on clicking save button.
             * Validates the input and provides the inline validation errors. Shows an alert dialog if there is an
             * error creating the page on server side.
             * @param finderPath,
             * @param templateId, the string representation of the template guid (EG: 16777215-101-705), if not blank, then uses this
             * template id to create the page. If blank shows the template picker.
             */
            openDialog: _openDialog
        };
        var pageId = null;
        var templateId = null;
        var sitename = null;
        var currentTemplateName = null;
        var currentTemplateId = null;
        var sourceType = null;
        var templateSummaries = null;
        var successCallBack = function()
        {
        };
        
        //See API for doc.
        function _openDialog(pId, tId, sname, scb)
        {
            pageId = pId;
            templateId = tId;
            sitename = sname;
            successCallBack = scb;
            var queryPath = $.perc_paths.TEMPLATES_BY_SITE + '/' + sitename;
            $.getJSON(queryPath, function(spec)
            {
                templateSummaries = spec['TemplateSummary'];
                if (templateSummaries.length <= 1 && templateId != "") 
                {
                    _openAlertDialog();
                }
                else 
                {
                    _openTemplateDialog()
                }
            });
            
            
        }
        
        function _openAlertDialog()
        {
            $.perc_utils.alert_dialog({
                title: I18N.message("perc.ui.page.general@Warning"),
                content: I18N.message("perc.ui.change.template.dialog@Only One Template"),
                id: 'perc-no-template-message'
            });
        }
        
        //See API for doc.
        function _openTemplateDialog()
        {
            var taborder = 30;
            var dialogHtml = "<div>" +
            "<form action='' method='GET'> " +
            "<a class='prevPage browse left'></a>" +
            "<div class='perc-scrollable'><input type='hidden' id='perc-select-template' name='template'/>" +
            "<div class='perc-items'>" +
            "</div></div>" +
            "<a class='nextPage browse right' ></a>" +
            //"<div style = 'display:block' id = 'perc-template-label'><span class='perc-label-left'><label class ='perc-static-label'>Current Template:</label><label class = 'perc-dynamic-label'></label></span><span class = 'perc-label-right'><label class = 'perc-static-label'>Selected Template:</label><label class = 'perc-dynamic-label'></label></span></div>" +
            "<div style = 'display:block' id = 'perc-template-label'><span class='perc-label-left'></span><span class = 'perc-label-right'></span></div>" +
            "</form></div>";
            
            // if we are in the new blog post dialog, the width is 
            var dialogWidth = 800;
            var dialog = $(dialogHtml).perc_dialog({
                title: I18N.message("perc.ui.change.template.dialog@Change Template"),
                buttons: {},
                percButtons: {
                    "Save": {
                        click: function()
                        {
                            _submit();
                        },
                        id: "perc-page-save"
                    },
                    "Cancel": {
                        click: function()
                        {
                            _remove();
                        },
                        id: "perc-page-cancel"
                    }
                },
                id: "perc-change-template-dialog",
                width: dialogWidth,
                modal: true
            });
            scrollableTemplateSelector();
            /**
             * The call back used when recieved validation or internal errors.
             * It will set the focus on the page name input entry if received
             * an validation error and the error code is "page.alreadyExists".
             *
             * @param request the request object contains the error message in the
             * response.
             */
            function errorHandler(request)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                var code = $.PercServiceUtils.extractFieldErrorCode(request);
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.publish.title@Error"),
                    id: 'perc-error-dialog-confirm',
                    content: defaultMsg
                });
            }
            
            /**
             * Removes the dialog.
             */
            function _remove()
            {
                dialog.remove();
            }
            
            function _submit()
            {
                // If the selected template is same as current template - abort the ajax call and return
                if(dialog.find("#perc-select-template").val() ==  currentTemplateId) {
                       dialog.remove();
                       return;
                }
                
                var pageIds = [pageId];
                var migrateContentRestData =  {"MigrateContentRestData":{"siteName":sitename,"templateId":dialog.find("#perc-select-template").val(),"refPageId":"","pageIds":pageIds,"sourceType":sourceType}};
                $.PercTemplateService().assignTemplateAndMigrateContent(migrateContentRestData, function(status, result)
                { 
                    if (status == $.PercServiceUtils.STATUS_SUCCESS) 
                    {
                        dialog.remove();
                        successCallBack();
                    }
                    else 
                    {
                        errorHandler(result);
                    }
                    
                });
            }
            
            
            /**
             * Builds the scrollable template selector, this needs to be replaced by PercScrollingTemplateBrowser.
             */
            function scrollableTemplateSelector()
            {
                var itemContainer = dialog.find('div.perc-scrollable div.perc-items');
                var selectLocalStyle = "height: 160px; width: 410px; overflow-x: scroll; overflow-y: hidden;";
                $('#perc-select-template_perc_is').attr("style", selectLocalStyle);
                
                $.each(templateSummaries, function()
                {
                    itemContainer.append(createTemplateEntry(this, templateId));
                    $("div.perc-scrollable").scrollable({
                        items: ".perc-items",
                        size: 4,
                        keyboard: false
                    });
                    $(".perc-items .item .item-id").hide();
                    // bind click event to each item to handle selection
                    $(".perc-items .item").bind('click', function()
                    {
                        var itemId = $(this).find(".item-id").text();
                        var itemName = $(this).find(".perc-text-overflow").text();
                        $("#perc-select-template").val(itemId);
                        $(".perc-label-right").html("Selected Template:&nbsp;" + itemName);
                        $(".perc-items .item").removeClass("perc-selected-item");
                        $(this).addClass("perc-selected-item");
                    });
                    
                    //Capture the name of current template for a given page                    
                    if (this.id == templateId)                   
                    {
                        currentTemplateName = this.name;
                        currentTemplateId = this.id;
                       
                    }                                                                           
                });
                
                var selectedTemplateName = null;
                if (!currentTemplateName)
                {
                    currentTemplateName = "Unassigned";
                    selectedTemplateName = "None";
                    sourceType = "UNASSIGNED";
                }
                else
                {
                    selectedTemplateName = currentTemplateName;
                    sourceType = "TEMPLATE";
                }
                
                $(".perc-label-left").html("Current Template:&nbsp;" + currentTemplateName);
                $(".perc-label-right").html("Selected Template:&nbsp;" + selectedTemplateName);
                $("#perc-select-template").val(currentTemplateName);
                
                // after adding all the template entries, truncate the labels if they dont fit
                $.PercTextOverflow($("div.perc-text-overflow"), 122); 
                
                
                /**
                 * Creates and returns an entry for the template selection field.
                 */
                function createTemplateEntry(data, templateId)
                {
                    var tempDiv = "<div class=\"item\">";
                    if(data.id == templateId) {
                        tempDiv = "<div class=\"item perc-selected-item\">";
                    }
                    var temp = tempDiv +
                    "<div class=\"item-id\">@ITEM_ID@</div>" +
                    "    <table>" +
                    "        <tr><td align='left'>" +
                    "            <img style='border:1px solid #E6E6E9' height = '86px' width = '122px' src=\"@IMG_SRC@\"/>" +
                    "        </td></tr>" +
                    "        <tr><td>" +
                    "            <div class='perc-text-overflow-container' style='text-overflow:ellipsis;width:122px;overflow:hidden;white-space:nowrap'>" +
                    "                <div class='perc-text-overflow' style='float:none' title='@ITEM_TT@' alt='@ITEM_TT@'>@ITEM_LABEL@</div>" +
                    "        </td></tr>" +
                    "    </table>" +
                    "</div>";
                    return temp.replace(/@IMG_SRC@/, data.imageThumbPath).replace(/@ITEM_ID@/, data.id).replace(/@ITEM_LABEL@/, data.name).replace(/@ITEM_TT@/g, data.name);
                }
                
            }
            
        }// End open dialog
        return changeTemplateDialogApi;
    }
    
})(jQuery);
