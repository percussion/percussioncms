/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function($) {
    $.perc_template_metadata_dialog = function(templateId) {
        var dialog;
        var doctypeHTML5;
        var doctypeXHTML;
        var doctypeCustom;
        var dialogButtons;
        var doctypeSelected;
        var codeMirrorSet = false;
        var cm1,cm2,cm3;

        dialogButtons = {
            "Save":    {
                click: function()    {saveMetadata(templateId);},
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


        var dlgHtml = "<div id='edit-page-metadata'> </div>";

        dialog = $(dlgHtml).perc_dialog( {
            title: "Edit Template MetaData",
            modal: true,
            width: 800,
            height: 610,
            close: function(event, ui) {
                $.PercDirtyController.setDirty(false,"template");
                dialog.remove();
            },
            open: function(){
                _setTemplateContent(templateId);

            },
            percButtons: dialogButtons,
            id:"perc-template-metadata-dialog"
        } );

        /**
         * Makes an AJAX request to the server and gets the page url and sets it as src attr of the iframe.
         * @param templateId assumed not null.
         */
        function _setTemplateContent(templateId)
        {
            var pageUrl = "";
            if($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT) {
                //Edit mode
                pageUrl = $.perc_paths.TEMPLATE_EDITURL + "/" + templateId;
            } else {
                //Read-only mode
                pageUrl = $.perc_paths.TEMPLATE_EDITURL + "/" + templateId;
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
                        _addFieldGroups();
                        setMetaContent(templateId);
                    });
                },
                error: function(request, textstatus, error){
                    alert(I18N.message("perc.ui.page.edit.dialog@Unable To See Content")+templateId);
                }
            });
        }

        function setMetaContent(templateId)
        {
            $.PercSiteTemplatesController(false).loadTemplateMetadata(templateId, function(metadataObj){

                var getDocValue = metadataObj.HtmlMetadata.docType.options;
                doctypeSelected = metadataObj.HtmlMetadata.docType.selected;

                for(i=0; i< getDocValue.length; i++) {
                    if(getDocValue[i].option == "xhtml")
                    {
                        doctypeXHTML = getDocValue[i].value;
                    }
                    else if(getDocValue[i].option == "html5")
                    {
                        doctypeHTML5 = getDocValue[i].value;
                    }
                    else if (getDocValue[i].option == "custom")
                    {
                        doctypeCustom = getDocValue[i].value;
                    }
                }

                var iframeContainer = $("#edit-page-metadata-frame").contents();

                var ceField = iframeContainer.find("input[value='" +doctypeSelected + "']").trigger("click");

            });
        }


        function saveMetadata(templateId)
        {
            var iframeContainer = $("#edit-page-metadata-frame").contents();

            var additionalHeadContent = iframeContainer.find("textarea[id='additional_head_content']");
            var afterBodyStartContent = iframeContainer.find("textarea[id='code_insert_after_body_start']");
            var beforeBodyCloseContent = iframeContainer.find("textarea[id='code_insert_before_body_close']");
            var protectedRegion = iframeContainer.find("input[id='perc-content-edit-protected_region']");
            var protectedRegionText = iframeContainer.find("input[id='perc-content-edit-protected_region_text']");

            var docTypeRadio = iframeContainer.find('input[name="doctype"]:checked');
            var docTypeTxt = iframeContainer.find('textarea[id="perc_template_metadata_custom_doctype"]');

            var metadataObj = {"HtmlMetadata" :{

                    "additionalHeadContent": additionalHeadContent[0].nextSibling.CodeMirror.getValue(),
                    "afterBodyStartContent": afterBodyStartContent[0].nextSibling.CodeMirror.getValue(),
                    "beforeBodyCloseContent": beforeBodyCloseContent[0].nextSibling.CodeMirror.getValue(),
                    "id" : templateId,
                    "protectedRegion" : protectedRegion.val(),
                    "protectedRegionText":protectedRegionText.val(),
                    "docType":{
                        "selected":docTypeRadio.val(),
                        "options":[
                            {"option":"xhtml","value":"PERC_XHTML"},
                            {"option":"html5","value":"PERC_HTML5"},
                            {"option":"custom","value":docTypeTxt.val()}
                        ]
                    }
                }
            };

            $.PercSiteTemplatesController(false).saveTemplateMetadata( metadataObj, function() {});
            dialog.remove();
        }

        // A private helper method to group the fields and create collapsible sections
        function _addFieldGroups()
        {
            var iframeContainer = $("#edit-page-metadata-frame").contents();

            $.perc_filterField($("#edit-page-metadata-frame").contents().find("[name=sys_title]"), $.perc_textFilters.URL);

            var fieldGroups = [
                {groupName:"perc-section-doctype-container", groupLabel:"DocType/HTML Tags", fieldNames:["doctype"]},
                {groupName:"perc-headCode-container", groupLabel:I18N.message("perc.ui.page.edit.dialog@Additional Code"), fieldNames:["additional_head_content","code_insert_after_body_start","code_insert_before_body_close"]},
                {groupName:"perc-section-protected-container", groupLabel:"Protected Regions", fieldNames:["protected_region","protected_region_text"]}];
            $.each(fieldGroups, function(index){
                var minmaxClass = index===0?"perc-items-minimizer":"perc-items-maximizer";
                var groupHtml = "<div style='margin:0 -35px;padding: 15px 15px 15px 35px;cursor:pointer;' id = '" + this.groupName + "' ><div class = 'perc-section-label' group='" + this.groupName + "'><span  class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel + "</div></div>";
                var ceField = iframeContainer.find("[for='" + this.fieldNames[0] + "']").closest('div[type]').before(groupHtml);
                var fields = this.fieldNames;
                var groupName = this.groupName;
                let i =0;
                for(let fld of fields)
                {
                    var fieldDiv = iframeContainer.find("[for='" + fld + "']").closest('div[type]').attr("groupName", groupName);

                    if(index!==0)
                        fieldDiv.hide();
                    i++;
                }
            });

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

            //Bind the click on radio button
            iframeContainer.find("input[name='doctype']").on("click", function() {
                var textarea =  iframeContainer.find("textarea[id='perc_template_metadata_custom_doctype']");
                if($(this).attr('value') === "html5") {
                    textarea.val(doctypeHTML5).attr('readonly', 'readonly').addClass('perc-readOnlyText');
                }
                else if($(this).attr('value') === "xhtml") {
                    textarea.val(doctypeXHTML).attr('readonly', 'readonly').addClass('perc-readOnlyText');
                }
                else if($(this).attr('value') === "custom") {
                    textarea.removeAttr('readonly').removeClass('perc-readOnlyText');
                    if(doctypeCustom !== "" && doctypeCustom != null) {
                        textarea.val(doctypeCustom);
                    }
                }
            });
        }
    };

})(jQuery);
