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
* Schedules Dialog
*/
(function($){
    $.perc_gadgets_search_criteria_dialog = {
            openSearchCriteriaDialog: openSearchCriteriaDialog,
            getSearchConfig: getSearchConfig
    };
    
    function getSearchConfig(metakey, callback)
    {
        percJQuery.PercMetadataService.find(metakey, function(status, data){
            if (status === percJQuery.PercServiceUtils.STATUS_SUCCESS)
            {
                if (data != null)
                {
                    var dataObj = JSON.parse(data.metadata.data);
                    callback(dataObj);
                }
                else
                {
                    callback({});
                }
            }
            else
            {
                callback({});
            }
        });
    }
    /**
     * Opens the schedule dialog.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     */
    function openSearchCriteriaDialog(criteriaData, saveCallback, cancelCallback) 
    {

                // Set the basic markup for the dialog and assign it to the module's dialog variable
                var dialogContent;
                dialogContent =  '<div class="perc-search-criteria-dialog-content" >';
                dialogContent +=             '<form id="perc-search-criteria-dialog-form" action="#">';
                dialogContent +=                 '<div id="perc-search-criteria-dialog-general-container">';
                if (criteriaData.type === "pages") {
                    dialogContent += '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-site-container">';
                    dialogContent += '<label id="perc-search-criteria-dialog-site-label" for="perc-search-criteria-dialog-site"> Site: </label>';
                    dialogContent += '<div class="perc-search-criteria-dialog-select-containers" ><select name="perc-search-criteria-dialog-site" id="perc-search-criteria-dialog-site">';
                    dialogContent += '</select></div>';
                    dialogContent += '</div>';
                    dialogContent += '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-template-container">';
                    dialogContent += '<label class="perc-disabled" id="perc-search-criteria-dialog-template-label" for="perc-search-criteria-dialog-template"> Template: </label>';
                    dialogContent += '<div class="perc-search-criteria-dialog-select-containers"><select name="perc-search-criteria-dialog-template" id="perc-search-criteria-dialog-template" disabled="disabled">';
                    dialogContent += '</select></div>';
                    dialogContent += '</div>';
                }
                else if (criteriaData.type === "assets") {
                    dialogContent += '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-assettype-container">';
                    dialogContent += '<label id="perc-search-criteria-dialog-assettype-label" for="perc-search-criteria-dialog-assettype"> Asset Type: </label>';
                    dialogContent += '<div class="perc-search-criteria-dialog-select-containers"><select name="perc-search-criteria-dialog-assettype" id="perc-search-criteria-dialog-assettype" >';
                    dialogContent += '<option value="@all">All</option>';
                    dialogContent += '</select></div>';
                    dialogContent += '</div>';
                }
                dialogContent += '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-workflow-container">';
                dialogContent += '<label id="perc-search-criteria-dialog-workflow-label" for="perc-search-criteria-dialog-workflow"> Workflow: </label>';
                dialogContent += '<div class="perc-search-criteria-dialog-select-containers"><select name="perc-search-criteria-dialog-workflow" id="perc-search-criteria-dialog-workflow" >';
                dialogContent += '<option value="@all">All</option>';
                dialogContent += '</select></div>';
                dialogContent += '</div>';
                dialogContent +=                     '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-state-container">';
                dialogContent +=                         '<label class="perc-disabled" id="perc-search-criteria-dialog-state-label" for="perc-search-criteria-dialog-state"> State: </label>';
                dialogContent +=                         '<div class="perc-search-criteria-dialog-select-containers"><select name="perc-search-criteria-dialog-state" id="perc-search-criteria-dialog-state" disabled="disabled" >';
                dialogContent +=                             '<option value="@all">All</option>';
                dialogContent +=                         '</select></div>';                
                dialogContent +=                     '</div>';
                dialogContent +=                     '<div class="perc-search-criteria-dialog-fields-containers" id="perc-search-criteria-dialog-created-by-container">';
                dialogContent +=                         '<label id="perc-search-criteria-dialog-created-by-label" for="perc-search-criteria-dialog-created-by-field"> Last Modified By: </label><br/>';
                dialogContent +=                         '<div class="perc-search-criteria-dialog-select-containers" ><input id="perc-search-criteria-dialog-created-by-field" name="perc-search-criteria-dialog-created-by-field"/></div>';
                dialogContent +=                     '</div>';
                dialogContent +=                 '</div>';
                dialogContent +=             '</form>';
                dialogContent += '</div>';

                buttons = {
                    "Search" : {
                        'id': 'perc-search-criteria-dialog-save',
                        'click': function() {
                            
                            var siteValue = dialog.find('#perc-search-criteria-dialog-site').val();
                            var templateName = dialog.find('#perc-search-criteria-dialog-template option:selected').html();
                            var templateValue = dialog.find('#perc-search-criteria-dialog-template').val();
                            var assetTypeName = dialog.find('#perc-search-criteria-dialog-assettype option:selected').html();
                            var assetTypeValue = dialog.find('#perc-search-criteria-dialog-assettype').val();
                            var workflowName = dialog.find('#perc-search-criteria-dialog-workflow option:selected').html();
                            var workflowValue = dialog.find('#perc-search-criteria-dialog-workflow').val();
                            var stateName = dialog.find('#perc-search-criteria-dialog-state option:selected').html();
                            var stateValue = dialog.find('#perc-search-criteria-dialog-state').val();
                            
                            var modifiedValue = dialog.find('#perc-search-criteria-dialog-created-by-field').val();
                            
                            templateValue = (templateValue == null ? "@all" : templateValue);
                            stateValue = (stateValue == null ? "@all" : stateValue);
                            modifiedValue = (modifiedValue == null || modifiedValue.trim() === "" ? "@all" : modifiedValue);
                            
                            var siteName = (siteValue === "@all" ? "All" : siteValue);
                            templateName = (templateValue === "@all" ? "All" : templateName);
                            assetTypeName = (assetTypeValue === "@all" ? "All" : assetTypeName);
                            workflowName = (workflowValue === "@all" ? "All" : workflowName);
                            stateName = (stateValue === "@all" ? "All" : stateName);
                            var modifiedName = (modifiedValue === "@all" ? "All" : modifiedValue);
                            
                            var returnData = {};
                            if (criteriaData.type === "pages") {
                                returnData.site = {"name": siteName,"value": siteValue};
                                returnData.template = {"name": templateName,"value": templateValue};
                            }
                            else if(criteriaData.type === "assets"){
                                returnData.assetType = {"name": assetTypeName, "value" : assetTypeValue};
                            }
                            returnData.workflow = {"name": workflowName, "value" : workflowValue};
                            returnData.state = {"name": stateName, "value" : stateValue};
                            returnData.modifiedby = {"name": modifiedName, "value" : modifiedValue};
                            
                            saveCallback(returnData);
                            dialog.remove();
                        }
                    },
                    'Cancel' : {
                        'id': 'perc-search-criteria-dialog-cancel',
                        'click': function() {
                            cancelCallback();
                            dialog.remove();
                        }
                    }
                };
                dialog = percJQuery(dialogContent).perc_dialog({
                    'id' : "perc-search-criteria-dialog",
                    'title' : 'Filters',
                    'width': 686,
                    'height': 420,
                    'resizable': false,
                    'modal': true,
                    'percButtons': buttons
                });
                

                function loadFields()
                {
                    if (criteriaData.type === "pages") {
                        percJQuery.PercReusableSearchService.getSites(function(status, result){
                            for (var i = 0; i < result.length; i++) {
                                var value = result[i].value;
                                var optionElement = $("<OPTION>").html(value).attr("value", value);
                                if (criteriaData.site != null) {
                                    if (value === criteriaData.site.value) {
                                        optionElement.attr("selected", "selected");
                                    }
                                }
                                dialog.find("#perc-search-criteria-dialog-site").append(optionElement);
                            }
                            if (criteriaData.site.value !== "@all") {
                                dialog.find("#perc-search-criteria-dialog-site").trigger("change");
                            }
                        });
                    }
                    percJQuery.PercReusableSearchService.getWorkflows(function(status, result)
                    {
                        for (var i = 0; i < result.length; i++)
                        {
                            var value = result[i].displayValue;
                            var name = result[i].value;
                            var optionElement = $("<OPTION>")
                                            .html(name)
                                            .attr("value", value);
                            if (criteriaData.workflow != null)
                            {
                                if (value === criteriaData.workflow["value"])
                                {
                                    optionElement.attr("selected", "selected");
                                }                            
                            }
                            dialog.find("#perc-search-criteria-dialog-workflow").append(optionElement);
                        }
                        if (criteriaData.workflow["value"] !== "@all")
                        {
                            dialog.find("#perc-search-criteria-dialog-workflow").trigger("change");
                        }                        
                    });
                     
                    if (criteriaData.type === "assets") {
                        percJQuery.PercAssetService.getAssetTypes("no", function(status, result){
                            
                            for (var i = 0; i < result.length; i++) {
                                var value = result[i].contentTypeId;
                                var name = result[i].widgetLabel;
                                var optionElement = $("<OPTION>").html(name).attr("value", value);
                                if (criteriaData.assetType != null) {
                                    if (value === criteriaData.assetType["value"]) {
                                        optionElement.attr("selected", "selected");
                                    }
                                }
                                dialog.find("#perc-search-criteria-dialog-assettype").append(optionElement);
                            }
                        });
                    }
                     
                }

                dialog.find('#perc-search-criteria-dialog-site').on("change",function() {
                    if ($(this).val() === "@all")
                    {
                        dialog.find("#perc-search-criteria-dialog-template").prop("disabled", true).html("");
                        dialog.find("#perc-search-criteria-dialog-template-label").addClass("perc-disabled");
                    }
                    else
                    {
                        loadTemplatesCombo($(this).val(), function(){
                            dialog.find("#perc-search-criteria-dialog-template").prop("disabled",false);
                            dialog.find("#perc-search-criteria-dialog-template-label").removeClass("perc-disabled");
                        });
                    }
                });

                function loadTemplatesCombo(siteName, callback)
                {
                    percJQuery.PercSiteService.getTemplates(siteName, function(status, result)
                    {
                        if (status === percJQuery.PercServiceUtils.STATUS_SUCCESS)
                        {
                            var data = percJQuery.perc_utils.convertCXFArray(result.TemplateSummary);
                            dialog.find("#perc-search-criteria-dialog-template").html("").append(
                                $("<OPTION>")
                                    .html("All")
                                    .attr("value", "@all")
                            );
                            for (var i = 0; i < data.length; i++)
                            {
                                var id = data[i].id;

                                //CMS-7925 : The filter was not working expected while filtering on templates. Content id extracted and passed to filter results on templates.
                                var contentId = percJQuery.perc_utils.getContentId(id);

                                var name = data[i].name;
                                var optionElement = $("<OPTION>")
                                                .html(name)
                                                .attr("value", id);
                                if (criteriaData.template != null)
                                {
                                    if (id  === criteriaData.template["value"])
                                    {
                                        optionElement.attr("selected", "selected");
                                    }
                                }
                                dialog.find("#perc-search-criteria-dialog-template").append(optionElement).attr("forsite", siteName);
                            }
                            callback();
                        }
                    });  
                }
                
                dialog.find('#perc-search-criteria-dialog-workflow').on("change",function() {
                    if ($(this).val() === "@all")
                    {
                        dialog.find("#perc-search-criteria-dialog-state").prop("disabled", true).html("");
                        dialog.find("#perc-search-criteria-dialog-state-label").addClass("perc-disabled");
                    }
                    else
                    {
                        loadStatesCombo($(this).find('option:selected').html(), function(){
                            dialog.find("#perc-search-criteria-dialog-state").prop("disabled",false);
                            dialog.find("#perc-search-criteria-dialog-state-label").removeClass("perc-disabled");
                        });
                    }
                });            

                function loadStatesCombo(workflow, callback)
                {
                    percJQuery.PercReusableSearchService.getStates(workflow, function(status, result)
                    {
                        if (status === percJQuery.PercServiceUtils.STATUS_SUCCESS)
                        {
                            dialog.find("#perc-search-criteria-dialog-state").html("").append(
                                $("<OPTION>")
                                    .html("All")
                                    .attr("value", "@all")
                            );
                            
                            for (var i = 0; i < result.length; i++)
                            {
                                var value = result[i].displayValue;
                                var name = result[i].value;
                                var optionElement = $("<OPTION>")
                                                .html(name)
                                                .attr("value", value);
                                if (criteriaData.state != null)
                                {
                                    if (value === criteriaData.state["value"])
                                    {
                                        optionElement.attr("selected", "selected");
                                    }
                                }
                                
                                dialog.find("#perc-search-criteria-dialog-state").append(optionElement).attr("forworkflow",workflow);
                            }
                            callback();
                        }
                    });  
                }
              
              // BEGINNING TYPE_AHEAD          
              var inputField = dialog.find("#perc-search-criteria-dialog-created-by-field");
              
              percJQuery.PercReusableSearchService.getUsers("", function(status, results)
              {
                  if (results !== undefined && results != null)
                  {
                    resultItems = results;

                    // add autocomplete to input field
                    inputField.autocomplete(resultItems, {
                        minChars: 0,
                        max: resultItems.length,
                        width: ($.browser.msie) ? 336 : 334,
                        scrollHeight: 101
                    }).result(function(){
                        // callback used after options load
                    });
                  }
              });
                // END TYPE_AHEAD 
              if (criteriaData.modifiedby != null)
              {
                inputField.val((criteriaData.modifiedby["value"] === "@all" ? "" : criteriaData.modifiedby["value"]));
              }
              loadFields();                
    }// End open dialog
})(jQuery);
