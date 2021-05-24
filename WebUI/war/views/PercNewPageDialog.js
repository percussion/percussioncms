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
 * New page dialog, see API for the available methods and behavior.
 */

(function($){
    
    $.PercNewPageDialog = function()
    {
        var newPageDialogApi = {
                /**
                 * Opens the new page dialog and creates a new page on clicking save button.
                 * Validates the input and provides the inline validation errors. Shows an alert dialog if there is an
                 * error creating the page on server side.
                 * @param finderPath, 
                 * @param templateId, the string representation of the template guid (EG: 16777215-101-705), if not blank, then uses this 
                 * template id to create the page. If blank shows the template picker.
                 */
                openDialog : _openDialog
        };
        //See API for doc.
        function _openDialog(finderPath, templateId) 
        {
            var siteName = "";
            if(finderPath) {
                finderPath = finderPath.split("/");
	            if (finderPath[1] === $.perc_paths.SITES_ROOT_NO_SLASH)
	            {
	            	siteName = finderPath[2];
	            }
            }
            var taborder = 30;
            var dialogHtml = "<div>" +
                        "<p class='perc-field-error' id='perc-save-error'></p><br/>" +
                        "<span style='position: relative; float: right; margin-top: -44px; margin-right: -2px;'><label>* - denotes required field</label></span>" +
                        "<form action='' method='GET'> ";
                       if(!templateId)
                       {
                           dialogHtml = dialogHtml + "<label for='perc-select-template'>" +I18N.message("perc.ui.new.page.dialog@Select A Template") + "</label><br/>" +
                            "<a class='prevPage browse left'></a>" +
                            "<div class='perc-scrollable'><input type='hidden' id='perc-select-template' name='template'/>" +
                            "<div class='perc-items'>" +
                            "</div></div>" +
                            "<a class='nextPage browse right' ></a>";
                       }
                       else
                       {
                           dialogHtml = dialogHtml + "<input type='hidden' id='perc-select-template' name='template' value='" + templateId + "'/>";
                       
                       }
                       dialogHtml = dialogHtml + "<div style='float:left;>" +
                        "<fieldset>" +
                        "<label for='perc-page-linktext' class='perc-required-field'>" + (!templateId ? I18N.message( "perc.ui.newpagedialog.label@Page link text" ) : I18N.message( "perc.ui.newblogpostdialog.label@Post title" )) + ":</label> <br/> " +
                        "<input type='text' tabindex='" + taborder + "' class='required' id='perc-page-linktext' name='page_linktext' maxlength='512'/> <br/> ";

                    if(!templateId)
                    {
                        dialogHtml = dialogHtml +
                             "<input type='text' style = 'display:none' tabindex='" + taborder + "' id='perc-page-title' class='required' name='page_title' maxlength='512'/> ";
                    }
                    else
                    {
                        /*
                         * if the template id is set, we are creating a dialog for the blog post gadget
                         * so, for story 353, we do not show the page title field
                         */
                        dialogHtml = dialogHtml +
                             "<label for='perc-page-title' class='perc-required-field' style='display: none;'>" + I18N.message( "perc.ui.newblogpostdialog.label@Hidden Post title" ) + ":</label> <br style='display: none;'/> " +
                             "<input type='hidden' tabindex='" + taborder + "' id='perc-page-title' class='required' name='page_title' maxlength='512'/> <br style='display: none;'/>";
                    }
                    
                    // render the rest of the dialog
                    dialogHtml = dialogHtml +
                        "<label for='perc-page-name' class='perc-required-field'>" + (!templateId ? I18N.message( "perc.ui.newpagedialog.label@Page name" ) : I18N.message( "perc.ui.newblogpostdialog.label@Post name" )) + ":</label> <br/> " +
                        "<input type='text' tabindex='" + taborder + "' class='required' id='perc-page-name' name='page_name' maxlength='255'/><br/> " +
                      "</fieldset>" +
                      "</div>" +
                        "<div class='ui-layout-south'>" +
                        "<div id='perc_buttons' style='z-index: 100;'></div>" +
                         "</div>" +
                        "</form> </div>";
                       
            // if we are in the new blog post dialog, the width is 
            var dialogWidth = !templateId ?  800 : 420;
            var dialog = $(dialogHtml).perc_dialog( {
                     title: (!templateId ? I18N.message( "perc.ui.newpagedialog.title@New Page" ) : I18N.message( "perc.ui.newblogpostdialog.title@New Post" )), 
                     buttons: {},
                     percButtons:   {
                        "Save": {
                            click: function()   {
								 $.PercBlockUI();
								_submit(siteName);
								$.unblockUI();
								},
                            id:"perc-page-save"
                        },
                        "Cancel":   {
                            click: function()   {_remove();},
                            id:"perc-page-cancel"
                        }
                     },
                    id: "perc-new-page-dialog",
                    width: dialogWidth,
                    resizable: false,
                    modal: true
            });
            //add the template selector if the template id is not defined.
            if(!templateId)
            {
                scrollableTemplateSelector();
            }
            /**
             * The call back used when recieved validation or internal errors.
             * It will set the focus on the page name input entry if received 
             * an validation error and the error code is "page.alreadyExists".
             *
             * @param request the request object contains the error message in the
             * response. 
             */               
            function errorHandler( request ) {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                var code = $.PercServiceUtils.extractFieldErrorCode(request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), id:'perc-error-dialog-confirm', content: defaultMsg, okCallBack:function(){
                        if (code === 'page.alreadyExists') {
                            $('#perc-page-name').focus();
                        }
                     }
                });
            }
            
            function _remove()  {
                dialog.remove();
            }
            
            function _submit(site)  {
            	$.PercSiteService.getSiteProperties(site, function(status, result) {
					if(status === $.PercServiceUtils.STATUS_SUCCESS) {
						var fileName = $(dialog.find('#perc-page-name')[0]).val();
						var fileExt = result.SiteProperties.defaultFileExtention;
						if (fileExt && fileName.lastIndexOf(".") < 0) {
                            if (fileName.length + fileExt.length < 255) { //consider a dot as one more char
                                fileName += "." + fileExt;
                            } else {
                                fileName = fileName.substring(0, 254 - fileExt.length) + "." + fileExt; //consider a dot as one more char
                            }
                        }
                            $(dialog.find('#perc-page-name')[0]).val(fileName);
                            //below checking for the special characters should not be entered in file name.
                            var regex = /[\\\/~`|<>?":*\[\]{}#;%]/;
                            if (regex.test(fileName)) {
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: '<span style="color:red" > The FileName cannot be empty and must not exceed 255 characters, must be unique within the folder and cannot contain any of the following characters: \\ / | &lt; &gt; ? " : \[ \] { } * # ; % </span>'});
                                return;
                            }

            		} else {
						$.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
					}
					dialog.find('form').submit();
	            });
            }
        
            var validation = dialog.find('form').validate({
                    errorClass: "perc_field_error",
                            validClass: "perc_field_success",
                            wrapper: "p",
                            validateHiddenFields: false,
                            messages: _getValidationMessages(),
                            debug: true,                    
                    submitHandler: function(form) {
                    var page_name = $(form).find('[name=page_name]').val( );
                    page_name = $.trim(page_name);
                    page_name = $.perc_textFilters.WINDOWS_FILE_NAME(page_name);
                    $(form).find('[name=page_name]').val(page_name);
                    $.perc_pathmanager.open_containing_folder( finderPath,
                        function( fspec, path ) {
                            $.perc_pagemanager.create_new_page( path, $(form).serializeArray(), function(page) {  
                                    dialog.remove(); 
                                    loadPage(path.join("/"), page_name, page.Page.id);}, errorHandler );
                        } );
                    }
            });  
        
            
            /**
             * Builds the scrollable template selector, this needs to be replaced by PercScrollingTemplateBrowser. 
             */
            function scrollableTemplateSelector()
            {
                var itemContainer = dialog.find('div.perc-scrollable div.perc-items');
                
                var selectLocalStyle = "height: 160px; width: 410px; overflow-x: scroll; overflow-y: hidden;";
                $('#perc-select-template_perc_is').attr("style", selectLocalStyle);
                
                var queryPath;
                if (finderPath[1] === $.perc_paths.SITES_ROOT_NO_SLASH)
                {
                   queryPath = $.perc_paths.TEMPLATES_BY_SITE + '/' + finderPath[2];
                }
                else
                {
                   queryPath = $.perc_paths.TEMPLATES_USER;
                }
               
                $.getJSON( queryPath, function( spec ) {
                    //Load template selector
                    $.each( spec['TemplateSummary'], function() {
                        itemContainer.append(createTemplateEntry(this));
                        $("div.perc-scrollable").scrollable({
                            items: ".perc-items",
                            size: 4,
                            keyboard: false
                        });
                        $(".perc-items .item .item-id").hide();
                        // bind click event to each item to handle selection
                        $(".perc-items .item").on('click', function(){
                            var itemId = $(this).find(".item-id").text(); 
                            $("#perc-select-template").val(itemId);
                            $(".perc-items .item").removeClass("perc-selected-item");
                            $(this).addClass("perc-selected-item");
                        });
                        // select first item by default
                        $firstItem = $(".perc-items .item:first");
                        $("#perc-select-template").val($firstItem.find(".item-id").text());
                        $firstItem.addClass("perc-selected-item");
                           
                    });
                    
                    // after adding all the template entries, truncate the labels if they dont fit
                    $.PercTextOverflow($("div.perc-text-overflow"), 122);
                });
                
                
                /**
                 * Creates and returns an entry for the template selection field.
                 */
                function createTemplateEntry(data)
                {
                    var temp = "<div class=\"item\">" +
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
                       return temp.replace(/@IMG_SRC@/, data.imageThumbPath)
                          .replace(/@ITEM_ID@/, data.id)
                          .replace(/@ITEM_LABEL@/, data.name)
                          .replace(/@ITEM_TT@/g, data.name);
                }

            }
            
           //Text auto fill and filter settings for form fields
           {
                var linkTextField = $('#perc-page-linktext');
                var titleField = $('#perc-page-title');
                var pageNameField = $('#perc-page-name');   
                $.perc_textAutoFill(linkTextField, titleField);
                $.perc_textAutoFill(linkTextField, pageNameField, $.perc_autoFillTextFilters.URL, null, 255);
                $.perc_filterField(pageNameField, $.perc_textFilters.URL);
            }
        
            /**
             * Builds and returns an object that has the validation messages for each field.
             */
            function _getValidationMessages()
            {
                var messages = {
                   "page_linktext": {
                      required: (!templateId ? "Page link text" : "Post Title") + "  is a required field."          
                   },
                   "page_title": {
                      required: (!templateId ? "Page" : "Hidden Post") + " title is a required field."
                   },
                   "page_name": {
                      required: (!templateId ? "Page" : "Post") + " name is a required field."
                   }     
                };
                return messages;
            }
        
            /**
             * Helper method to load the page with the given parameters. This is a browser reload.
             * @param folderPath assumed not null.
             * @param pageName assumed not null.
             * @param pageId assumed not null.
             * 
             */
            function loadPage(folderPath, pageName, pageId)
            {
              $.PercNavigationManager.goToLocation(
                $.PercNavigationManager.VIEW_EDITOR,
                $.PercNavigationManager.parseSiteFromPath(folderPath),
                $.PercNavigationManager.MODE_EDIT,
                pageId,pageName,folderPath + "/" + pageName, $.PercNavigationManager.PATH_TYPE_PAGE);
            }
            
        
        }// End open dialog
        return newPageDialogApi;
    };

})(jQuery);