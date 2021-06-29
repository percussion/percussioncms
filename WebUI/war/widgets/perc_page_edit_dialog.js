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
 * Handles the editing of a page meta-data. Openes a dialog with an Iframe and gets the page edit url from server and
 * sets it as source on the iframe.
 */
(function($) {
    $.perc_page_edit_dialog = function(mcol, content_viewer, pageid) {
        var dialogTitle;
        var dialogButtons;
        var dialogHeight=0;
        var dialogWidth=0;
        var pageSysName = "";
        //We are now loading the form, the clear the content presubmit handlers.
        $.PercContentPreSubmitHandlers.clearHandlers();
        if($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT) {
            //Edit mode
            dialogTitle = I18N.message("perc.ui.page.edit.dialog@Edit Metadata");
            dialogButtons = {
                "Save":    {
                    click: function()    {_saveMetadata();},
                    id: "perc-content-edit-save-button"
                },
                "Cancel":    {
                    click: function()    {
                        $.PercDirtyController.setDirty(false,"page");
                        dialog.remove();
                        return true;},
                    id: "perc-content-edit-cancel-button"
                }
            };
            dialogHeight = 600;
            dialogWidth = 800;
        } else {
            //Read-only mode
            dialogTitle = I18N.message("perc.ui.page.edit.dialog@Metadata")
            dialogButtons = {
                "Ok":    {
                    click: function()    {
                        $.PercDirtyController.setDirty(false,"page");
                        dialog.remove();
                        return true;},
                    id: "perc-content-edit-ok-button"
                }
            };
            dialogHeight = 610;
            dialogWidth = 800;
        }
        var initialLoad = true;
        var dlgHtml = "<div id='edit-page-metadata'>" + "</div>";

        var dialog = $(dlgHtml).perc_dialog( {
            title: dialogTitle,
            modal: true,
            width: dialogWidth,
            height: dialogHeight,
            close: function(event, ui) {
                $.PercDirtyController.setDirty(false,"page");
                dialog.remove();
            },
            open: function(){
                _setPageContent(pageid);
            },
            percButtons: dialogButtons,
            id:"perc-page-edit-dialog"
        } );

        /**
         * Makes an AJAX request to the server and gets the page url and sets it as src attr of the iframe.
         * @param pageid assumed not null.
         */
        function _setPageContent(pageid)
        {
            var pageUrl = "";
            if($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT) {
                //Edit mode
                pageUrl = $.perc_paths.PAGE_EDITURL + "/" + pageid;
            } else {
                //Read-only mode
                pageUrl = $.perc_paths.PAGE_VIEWURL + "/" + pageid;
            }

            $.ajax({
                url: pageUrl,
                dataType: "text",
                contentType: "application/json",
                type: "GET",
                success: function(data, textstatus){
                    var ifrUrl = data + "&nocache=v1";
                    $("#edit-page-metadata").append("<iframe name='edit-page-metadata-frame' id='edit-page-metadata-frame' height='100%' style='border:0;' width='100%' src='" + ifrUrl + "' ></iframe>");
                    $("#edit-page-metadata-frame").on("load",function(){
                        _formatPageContent();
                    });
                },
                error: function(request, textstatus, error){
                    alert(I18N.message("perc.ui.page.edit.dialog@Unable To See Content")+pageid);
                }
            });
        }
        //Binds the frame load and calls the format page content on load of the frame.

        /**
         * Helper function to format the metadata, this must be called in the frame load events functions.
         * As we use the regular content editor for editing the page meta-data, the editor putput needs to be
         * formatted to suit the needs of metadata as the editor was designed for editing assets.
         */
        function _formatPageContent()
        {
            var iframeContainer = $("#edit-page-metadata-frame").contents();
            $.perc_filterField($("#edit-page-metadata-frame").contents().find("[name=sys_title]"), $.perc_textFilters.URL);
            $("#edit-page-metadata-frame").contents().find("#perc-content-edit-metadata-link").hide();
            $("#edit-page-metadata-frame").contents().find("#perc-content-edit-metadata-panel .perc-content-edit-data").show();
            if ($("#edit-page-metadata-frame").contents().find("#perc-content-edit-errors").length > 0)
            {
                $("#edit-page-metadata-frame").contents().find("#perc-content-edit-errors label").html("Error saving the page meta-data.");
            }
            var cbAutoSummary = $("#edit-page-metadata-frame").contents().find("#perc-content-edit-auto_generate_summary");
            var trAutoSummary = cbAutoSummary.closest('tr');
            cbAutoSummary.on("click",_handleAutoSummary);
            if(!pageSysName){
                pageSysName = $("#edit-page-metadata-frame").contents().find("#perc-content-edit-sys_title").val();
            }
            _addFieldGroups();
            $("#edit-page-metadata-frame").contents().find("#perc-content-edit-metadata-link").show();
        }

        // A private helper method to group the fields and create collapsible sections
        function _addFieldGroups()
        {
            var iframeContainer = $("#edit-page-metadata-frame").contents();
            iframeContainer.find("input[id='perc-content-edit-auto_generate_summary']").closest('div[type]').attr("groupName", "perc-pageSum-container");
            iframeContainer.find("input[id='perc-content-edit-page_noindex']").closest('div[type]').attr("groupName", "perc-seo-container").hide();
            iframeContainer.find("label[for='sys_contentpostdate']").before('<div id = "perc-date-override">Override Post Date</div><span class = "perc-date-override-msg">' +I18N.message("perc.ui.page.edit.dialog@Page Will Appear Published") + '<br /></span>');
            var fieldGroups = [{groupName:"perc-pageSum-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@Page Summary"), fieldNames:["resource_link_title","sys_title","page_summary", "sys_contentpostdate","sys_contentpostdatetz"]},
                {groupName:"perc-seo-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@SEO"), fieldNames:["page_title","page_description","page_noindex"]},
                {groupName:"perc-tagCat-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@Tags And Categories"), fieldNames:["page_tags","page_categories_tree"]},
                {groupName:"perc-calendar-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@Calendar"), fieldNames:["page_calendar"]},
                {groupName:"perc-headCode-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@Additional Code"), fieldNames:["additional_head_content","code_insert_after_body_start","code_insert_before_body_close"]}];
            $.each(fieldGroups, function(index){
                var minmaxClass = index===0?"perc-items-minimizer":"perc-items-maximizer";
                var groupHtml = "<div id = '" + this.groupName + "' ><div class = 'perc-section-label' group='" + this.groupName + "'><span  class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel + "</div></div>";
                var ceField = iframeContainer.find("[for='" + this.fieldNames[0] + "']").closest('div[type]').before(groupHtml);
                var fields = this.fieldNames;
                var groupName = this.groupName;
                for(var i=0;i<fields.length;i++)
                {
                    var fieldDiv = iframeContainer.find("[for='" + fields[i] + "']").closest('div[type]').attr("groupName", groupName);
                    if(index!==0)
                        fieldDiv.hide();
                }
            });
            var FileNameField = iframeContainer.find("label[for='sys_title']").closest('div[type]');
            iframeContainer.find("#perc-pageSum-container").after(FileNameField);

            //bind collapsible event
            iframeContainer.find(".perc-section-label").off('click').on('click',function() {
                $(this).find(".perc-min-max").toggleClass('perc-items-minimizer').toggleClass('perc-items-maximizer');
                var groupName = $(this).attr("group");
                iframeContainer.find("div[groupName='" + groupName + "']").toggle();
            });
            //Find all error
            $.each(iframeContainer.find('div[type="sys_error"]'), function(){
                var secGroupName =  $(this).attr("groupName");
                if(secGroupName !== 'perc-pageSum-container')
                    iframeContainer.find("div[group='" + secGroupName + "']").trigger('click');
            });
        }

        // Hide the TinyMCE instead of removing it, and show a div with content like a
        // disabled text area (like read-only view) if auto generate page summary is checked
        function _handleAutoSummary()
        {
            var cbAutoSummary = $("#edit-page-metadata-frame").contents().find("#perc-content-edit-auto_generate_summary");
            var containerArea = $("#edit-page-metadata-frame").contents().find(".mce-tinymce").parent();
            var tinyMCESpan = $("#edit-page-metadata-frame").contents().find(".mce-tinymce");
            if (cbAutoSummary.prop('checked'))
            {
                content = $("#edit-page-metadata-frame").contents().find(".tinymce").val();
                containerArea.append(
                    $('<div/>').
                    attr('id', 'perc_page_autogen_page_summary').
                    css('width', (tinyMCESpan.outerWidth()-1)+'px').
                    css('height', (tinyMCESpan.outerHeight()-7)+'px').
                    css('position', 'relative').
                    css('top', '5px').
                    css('margin', '0px').
                    css('padding', '0px').
                    addClass('datadisplay perc-tinymce-readonly').
                    html(content)
                );
                tinyMCESpan.hide();
            }
            else
            {
                containerArea.find('#perc_page_autogen_page_summary').remove();
                tinyMCESpan.show();
            }
        }

        /**
         * Checks whether there are any errors saving the meta-data, if yes, unblocks the UI and leaves the editor open.
         * If none, reloads the page.
         */
        function _handleSaveResults(evt)
        {
            if ($("#edit-page-metadata-frame").contents().find("#perc-content-edit-errors").length > 0)
            {
                $.unblockUI();
                return;
            }
            else
            {
                if ($("#edit-page-metadata-frame").contents().find("PSXLogErrorSet").length > 0)
                {
                    $.unblockUI();

                    var msg = I18N.message( 'perc.ui.common.error@Content Deleted' );
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: msg, okCallBack: function(){
                            $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_EDITOR);
                        }});
                    dialog.remove();
                }
                else
                {
                    $.PercPageService.savePageMetadata(pageid, function() {
                        $.PercPathService.getPathItemById($.PercNavigationManager.getId(),
                            function(status, result, errorCode){
                                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                                {
                                    $.PercNavigationManager.setName(result.PathItem.name);

                                    var newPath = result.PathItem.folderPaths + "/" + result.PathItem.name;
                                    newPath = newPath.substring(1);
                                    $.PercNavigationManager.setPath(newPath);

                                    $.PercDirtyController.setDirty(false,"page");
                                    var currentUrl = $.PercNavigationManager.getBookmark();
                                    $(window).removeData();
                                    $("#edit-page-metadata-frame").removeData();

                                    var oldPath = result.PathItem.folderPaths + "/" + pageSysName;
                                    var toPath = "/" + newPath;
                                    $.unblockUI();
                                    $.PercRedirectHandler.createRedirect(oldPath, toPath, "page")
                                        .fail(function(errMsg){
                                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.edit.dialog@Redirect Creation Error"), content: errMsg, okCallBack: function(){
                                                    window.location.href = currentUrl;
                                                }});
                                        })
                                        .done(function(){
                                            window.location.href = currentUrl;
                                        });
                                }
                                else
                                {
                                    $.unblockUI();
                                    var msg;
                                    if (errorCode === "cannot.find.item")
                                    {
                                        msg = I18N.message( 'perc.ui.common.error@Content Deleted' );
                                    }
                                    else
                                    {
                                        msg = result;
                                    }

                                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: msg});
                                }
                            });
                    });
                }
            }
        }

        /**
         * Submits the form and binds the onload function of the frame, as frame gets reloaded after it has been submitted.
         */
        function _saveMetadata()
        {
            var fieldValue = $.perc_textFilters.WINDOWS_FILE_NAME($("#edit-page-metadata-frame").contents().find("#perc-content-edit-sys_title").val().trim());
            $("#edit-page-metadata-frame").contents().find("#perc-content-edit-sys_title").val(fieldValue);

            $.PercBlockUI();
            //call all the pre submit handlers if nothing returns flase, submit the form.
            var dosubmit = true;
            $.each($.PercContentPreSubmitHandlers.getHandlers(),function(){
                if(!this()){
                    console.log("Save blocked by pre-submit handler");
                    dosubmit = false;
                }
            });
            if(!dosubmit)
            {
                $.unblockUI();
                return;
            }
            //We are now loading the form, the clear the content presubmit handlers.
            $.PercContentPreSubmitHandlers.clearHandlers();
            $("#edit-page-metadata-frame").contents().find("#perc-content-form").trigger("submit");
            $("#edit-page-metadata-frame").on("load",function(evt) {
                _handleSaveResults();
            } );
        }
    };
})(jQuery);
