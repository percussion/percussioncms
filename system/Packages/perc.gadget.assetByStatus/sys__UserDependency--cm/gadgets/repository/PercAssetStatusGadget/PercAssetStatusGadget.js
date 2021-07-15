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
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($){
    var itemsPerPage = 5;

    // grab necessary Perc APIs
    var PercSearchService  = percJQuery.PercSearchService;
    var PercServiceUtils = percJQuery.PercServiceUtils;
    var perc_utils       = percJQuery.perc_utils;
    var percWorkflowService = percJQuery.PercWorkflowService();

    var isLargeColumn = true;       // if gadget is on the right side (large column)
    var tableDiv;
    var tableData;
    var jobId;
    var criteria = {"type":"assets","assetType":"","workflow":"","state":"", lasteditedby:""};
    var moduleID;
    var selectFiltersCallback;
    var WidgetLabelByContentTypeId = [];
    var WidgetLabelByContentTypeName = [];
    // API for this library
    $.fn.PercWorkflowGadget = function(assetType, workflow, state, lasteditedby, rows, gadgetModuleID, _selectFiltersCallback) {
        moduleID = gadgetModuleID;
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");

        // resize gadget to fit the rows
        itemsPerPage = rows;

        tableDiv = $(this);

        if(assetType == null)
            assetType="";
        criteria.assetType = assetType;
        criteria.workflow = workflow;
        criteria.state = state;
        criteria.lasteditedby = lasteditedby;
        criteria.type="assets";
        selectFiltersCallback = _selectFiltersCallback;
        //Fill the widget content type association arrays
        percJQuery.PercAssetService.getAssetTypes("no", function(status, result){
            for (var i = 0; i < result.length; i++) {
                var res = result[i];
                WidgetLabelByContentTypeId[res.contentTypeId] = res.widgetLabel;
                WidgetLabelByContentTypeName[res.contentTypeName] = res.widgetLabel;
            }
            loadGadget(criteria);
        });
    };

    /**
     * Retrieves assets from a site that are in a particular status and then renders them as a table
     * @param criteria gadget criteria
     */
    function loadGadget(criteria) {
        if(tableDiv.find(".dataTables_wrapper").length > 0)
        {
            tableDiv.find(".dataTables_wrapper").remove();
        }
        // get the data and then pass it to createStatusTable to create the table
        let searchCriteriaObj = {"query": "", "folderPath": "//Folders/$System$/Assets", "sortColumn":"sys_title","sortOrder":"asc", "formatId":-1};

        var searchFields = [];
        if (criteria.assetType !== "@all")
            searchFields.push({"key": "sys_contenttypeid", "value" : criteria.assetType});
        if (criteria.workflow !== "@all")
            searchFields.push({"key": "sys_workflowid", "value" : criteria.workflow});
        if (criteria.state !== "@all")
            searchFields.push({"key": "sys_contentstateid", "value" : criteria.state});
        if (criteria.lasteditedby !== "@all")
            searchFields.push({"key": "sys_contentlastmodifier", "value" : criteria.lasteditedby});

        searchCriteriaObj.searchFields = {"entry":searchFields};

        PercSearchService
            .getAsyncSearchResult({"SearchCriteria":searchCriteriaObj},
                function(status, data){
                    if(status === true) {
                        tableData = perc_utils.convertCXFArray(data.PagedItemList.childrenInPage);
                        createStatusTable(tableData);
                    } else {
                        displayErrorMessage(data);
                    }
                    // on success or error be sure to re-attach events
                    addClickEvents();

                });
    }

    function createStatusTable(data){
        isLargeColumn = gadgets.window.getDashboardColumn() === 1; // if the gadget is in first column then we have to render it as large

        var percData = [];
        var excludeActionMenu = true;
        var itemProperties = data;

        function nameValueObjectArrayToMap(dataRow, nameKey, valueKey)
        {
            var d;
            var map = {};
            for(d=0;d<dataRow.length;d++)
            {
                let data  = dataRow[d];
                let name  = data[nameKey];
                let value = data[valueKey];
                map[name] = value;
            }
            return map;
        }

        $.each(itemProperties, function(index, item){

            var valuesMap = nameValueObjectArrayToMap(item.columnData.column, "name", "value");

            var lastModifiedDateParts = perc_utils.splitDateTime(valuesMap.sys_contentlastmodifieddate);

            var lastModifiedDateDate  = valuesMap.sys_contentlastmodifieddate === "" ? "" : lastModifiedDateParts.date;
            var lastModifiedDateTime  = valuesMap.sys_contentlastmodifieddate === "" ? "" : lastModifiedDateParts.time;

            var lastModifier = valuesMap.sys_contentlastmodifier;

            var lastModifiedTimeAndWho = lastModifiedDateTime === "" ? "" : (lastModifiedDateTime + " ("+lastModifier+")");
            var lastModifiedDateAndTime = lastModifiedDateDate +' ' + lastModifiedDateTime; //Combine the last modified date & time
            //var lastPublishedDateParts = perc_utils.splitDateTime(item.lastPublishedDate);

            var lastPublishedDateParts = typeof(valuesMap.sys_postdate) == "undefined" ? "" : perc_utils.splitDateTime(valuesMap.sys_postdate);
            var lastPublishedDateDate = typeof(valuesMap.sys_postdate) == "undefined" || valuesMap.sys_postdate === "" ? "" : lastPublishedDateParts.date;
            var lastPublishedDateTime = typeof(valuesMap.sys_postdate) == "undefined" || valuesMap.sys_postdate === "" ? "" : lastPublishedDateParts.time;
            var lastPublishedDateAndTime = lastPublishedDateDate +' '+ lastPublishedDateTime;  //Combine the published date & time
            var pageName = (valuesMap.linkText != null ? valuesMap.linkText : valuesMap.sys_title);
            var assetId = item.id;

            var assetPath = (!Array.isArray(item.folderPaths) ? item.folderPaths.replace("//Folders/$System$","") + "/" + item.name : "");
            var itemStatus = valuesMap.sys_statename; //sys_statename
            var type = WidgetLabelByContentTypeName[item.type];

            var summary;

            if(item.summary)
                summary = $(item.summary).text();
            else
                summary = "&nbsp;";

            var checkBoxContent = "<input class='perc-table-row-checkbox' type = 'checkbox' data-perc-id='" + assetId + "'/>";
            var previewColContent = "<div class ='perc-preview-col perc-datatable-columnrow' style = 'display:none;'><img src='/cm/gadgets/repository/perc_workflow_status_gadget/images/previewIcon.png'/></div><span></span>";
            var previewTitle = "Preview '" + pageName + "' page";
            var statusContent = "<span class='perc-table-row-status' data-perc-id='" + assetId + "'>" + itemStatus + "</span><span class='perc-table-row-status-icon'></span>";
            var row = {rowData : {assetId : assetId, assetPath : assetPath}, rowContent : [[{content : checkBoxContent, callback:function(){}}],
                    [{content : pageName, title : assetPath, callback:$.PercOpenAsset}],
                    [{content : previewColContent, title: previewTitle, callback: $.PercPreviewAsset}],
                    [{content : statusContent, title : itemStatus}],
                    [{content : lastModifier, title : lastModifier}],
                    [{content : type, title : type}],
                    [lastModifiedDateAndTime],
                    [lastPublishedDateAndTime]]};

            percData.push(row);
        });
        var chkBoxContent = "<input class = 'perc-header-checkbox' type = 'checkbox' />";

        var headers = [chkBoxContent, "Title", "&nbsp;", "Status", "Last Modified By", "Asset Type","Modified","Published"];
        var headerClasses = ["CheckBox", "Title", "Preview", "Status", "Last Modified By", "AssetType","Modified","Published"];

        var aoColumns = [
            { "bSortable": false },
            { sType: "string"},
            { "bSortable": false },
            { sType: "string"},
            { sType: "string"},
            { sType: "string"},
            { sType: "date"},
            { sType: "date"}
        ];

        var percVisibleColumns = null;
        if(!isLargeColumn)
            percVisibleColumns = [0,1,2,3,4];

        var percColumnWidths = ["30", "*", "30","110","110","80","135","135"];
        if($.browser.chrome || $.browser.safari)
            percColumnWidths = ["10", "*", "10","90","90","60","115","115"];
        var oLanguage = {sZeroRecords: "No Assets Found", oPaginate : {sFirst : "&lt;&lt;", sPrevious : "&lt;", sNext : "&gt;", sLast : "&gt;&gt;"}, sInfo : " ", sInfoEmpty : " "};

        var showPreviewBtnOnHover = true;
        var config = {percColumnWidths : percColumnWidths, percVisibleColumns : percVisibleColumns, iDisplayLength : itemsPerPage, percData : percData, percHeaders : headers, percHeaderClasses: headerClasses, aoColumns : aoColumns, showPreviewBtnOnHover:showPreviewBtnOnHover, additionalIframeHeight : 45, oLanguage : oLanguage};
        miniMsg.dismissMessage(loadingMsg);

        tableDiv.PercPageDataTable(config, excludeActionMenu);
        tableDiv.find(".perc-table-row-checkbox").on("dblclick", function(e) {
            e.stopPropagation();
        });
        tableDiv.find(".perc-header-checkbox").on("click", function(evt){
            var allChecked = true;
            tableDiv.find(".perc-table-row-checkbox").each(function(){
                if(!$(this).is(':checked'))
                {
                    allChecked = false;
                    return true;
                }
            });
            if(allChecked)
            {
                tableDiv.find(".perc-table-row-checkbox").prop("checked",false);
            }
            else
            {
                tableDiv.find(".perc-header-checkbox").prop("checked", true);
                tableDiv.find(".perc-table-row-checkbox").prop("checked", true);
            }
        });
        tableDiv.find(".paginate_button,.paginate_active").on("click", function(){
            tableDiv.find(".perc-header-checkbox,.perc-table-row-checkbox").prop("checked",false);
        });
        tableDiv.find("[perc-page]").on("click", function(){
            tableDiv.find(".perc-header-checkbox,.perc-table-row-checkbox").prop("checked",false);
        });

        var criteriaContentFields = tableDiv.parents("body").find("#perc-search-criteria-panel-content-fields");
        var iframe = $(percJQuery.find("#remote_iframe_" + moduleID));
        iframe.height(iframe.height() + (criteriaContentFields.length > 0 ? criteriaContentFields.height() + 15 : 0));
    }

    function displayErrorMessage(message) {
        tableDiv.append("<div class='perc-gadget-errormessage'>" + message + "</div>");
        miniMsg.dismissMessage(loadingMsg);
    }

    function clearErrorMessage(){
        tableDiv.find(".perc-gadget-errormessage").remove();
    }

    function addClickEvents()
    {
        bindApproveEvent();
        bindRefreshEvent();
        bindSelectFiltersEvent();
    }

    function bindRefreshEvent()
    {
        tableDiv.parents("body").find("#perc-search-criteria-panel-content-refresh-search").removeClass("perc-disabled").off("click").on("click",function(evt){
            clearErrorMessage();
            loadingMsg = miniMsg.createStaticMessage("Loading...");
            unbindRefreshEvent();
            unbindApproveEvent();
            unbindSelectFiltersEvent();
            loadGadget(criteria);});
    }

    function unbindRefreshEvent()
    {
        tableDiv.parents("body").find("#perc-search-criteria-panel-content-refresh-search").addClass("perc-disabled").off();
    }

    function bindApproveEvent()
    {
        tableDiv.parents("body").find("#perc-bulk-approve-button").removeClass("perc-disabled").off("click").on("click",function(evt){
            approveItems(evt);
        });
    }

    function unbindApproveEvent()
    {
        tableDiv.parents("body").find("#perc-bulk-approve-button").off().addClass("perc-disabled");
    }

    function bindSelectFiltersEvent()
    {
        tableDiv.parents("body").find("#perc-search-criteria-panel-content-select-filters").off("click").css("color","#0099CC").on("click", function(evt){
            clearErrorMessage();
            selectFiltersCallback();
        });
    }

    function unbindSelectFiltersEvent()
    {
        tableDiv.parents("body").find("#perc-search-criteria-panel-content-select-filters").off().css("color","#FFFFFF");
    }
    /**
     * Approve button click handler function, collects all the selected
     */
    function approveItems(evt)
    {
        var selectedRows = tableDiv.find(".perc-table-row-checkbox:checked");
        if(selectedRows.length < 1)
        {
            perc_utils.alert_dialog({
                title: "Warning",
                content: "Please select at least one asset to approve.",
                id: 'perc-no-items-selected-message'
            });
            return;
        }

        //Unbind the refresh event while we are processing.
        unbindRefreshEvent();
        unbindApproveEvent();
        unbindSelectFiltersEvent();
        var dataObject = {"ApprovableItems":{"approvableItems":[]}};
        var aitems = dataObject.ApprovableItems.approvableItems;
        $.each(tableData, function(){
            var approvableItem = $.extend({}, this);
            var isItemChecked = tableDiv.find(".perc-table-row-checkbox[data-perc-id='" + this.id + "']").is(":checked");
            if(isItemChecked)
                tableDiv.find(".perc-table-row-status[data-perc-id='" + this.id + "']").next(".perc-table-row-status-icon").addClass("perc-table-row-status-tobeprocessed").html(statusImgHtml_tobe);
            $.extend(approvableItem, {"approve" : isItemChecked});
            aitems.push(approvableItem);
        });
        tableDiv.find(".perc-table-row-status-tobeprocessed").first().addClass("perc-table-row-status-processing").removeClass("perc-table-row-status-tobeprocessed").html(statusImgHtml_proc);
        percWorkflowService.bulkApproveItems(dataObject, function(status, results){
            if(status == PercServiceUtils.STATUS_ERROR){
                var defMsg = PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"), content: defMsg});
                return;
            }
            else{
                jobId = results[0];
                getStatus(false);
            }
        });
    }

    /**
     * Gets the approval status by calling the service, if isFull is true then returns all the items, if not returns items that are selected.
     */
    function getStatus(isFull)
    {
        percWorkflowService.getBulkApproveStatus(jobId, isFull, function(status, results){
            if(status == PercServiceUtils.STATUS_ERROR){
                var defMsg = PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"), content: defMsg});
                return;
            }
            else{
                var approvalJob = results[0];
                if(isFull)
                {
                    updateTable(approvalJob.BulkApprovalJob);
                }
                else
                {
                    updateStatus(approvalJob.BulkApprovalJob);
                }
            }
        });
    }

    /**
     * Updates the status of the selected assets, loops through the processed items and updates the rows and if status is processing then calls the getStatus with a sec wait time.
     */
    function updateStatus(approvalStatus)
    {
        var processedItems = approvalStatus.items.processedItems;

        if(processedItems)
        {
            if(!Array.isArray(processedItems))
            {
                var tempArr = [];
                tempArr.push(processedItems);
                processedItems = tempArr;
            }

            //Update the status of the each processed page and update the status img
            $.each(processedItems, function(){
                var itemId = this.id;
                var statusClass = "";
                var temp = tableDiv.find(".perc-table-row-status[data-perc-id='" + itemId + "']").css("font-weight","bold").text(this.status);
                if(this.approvalStatus == "Failed")
                {
                    var imgHtml = $(statusImgHtml_error).attr("title",this.approvalMessage);
                    temp.next(".perc-table-row-status-icon").addClass("perc-table-row-status-error").removeClass("perc-table-row-status-tobeprocessed").html(imgHtml);
                }
                else
                {
                    temp.next(".perc-table-row-status-icon").removeClass("perc-table-row-status-tobeprocessed").html("");
                }
            });
            if(tableDiv.find(".perc-table-row-status-tobeprocessed").length > 0)
                tableDiv.find(".perc-table-row-status-tobeprocessed").first().addClass("perc-table-row-status-processing").removeClass("perc-table-row-status-tobeprocessed").html(statusImgHtml_proc);

        }

        //If status is still processing calls getStatus with a time interval of 1 sec, if status is Failed show the error message to the user, if the status is completed, show the error dialog if there are any failed items.
        switch(approvalStatus.status)
        {
            case "PROCESSING":
                setTimeout(function(){getStatus(false);}, 1000);
                break;
            case "FAILED":
                perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"), content: "Unexpected error occured while approving the selected items.", id: 'perc-no-items-selected-message'});
                //Bind the refresh and approve events back
                bindRefreshEvent();
                bindApproveEvent();
                bindSelectFiltersEvent();
                break;
            case "COMPLETED":
                if(!isEmpty(approvalStatus.items.errors))
                {
                    showErrorDialog(approvalStatus.items.errors);
                }
                //Bind the refresh and approve events back
                bindRefreshEvent();
                bindApproveEvent();
                bindSelectFiltersEvent();
                break;
        }
    }

    function isEmpty(obj) {
        for(var key in obj) {
            if(obj.hasOwnProperty(key))
                return false;
        }
        return true;
    }

    function updateTable(approvalStatus)
    {
        //@TODO Update the whole table with all the results, this is needed to cover the navigate away case, but this work has been defferred as approving 25 assets is reasonably fast enough.
    }

    /**
     * Shows error dialog, expects the errors to be a result of Java Map entries.
     */
    function showErrorDialog(errors)
    {
        var mapEntries = [];
        if(!Array.isArray(errors.entry))
            mapEntries.push(errors.entry);
        else
            mapEntries = errors.entry;

        var tableRows = "";
        $.each(mapEntries, function(){
            tableRows += "<tr><td width='20%'>" + this.key + "</td><td width='*'>" + this.value + "</td></tr>";
        });

        var dialogHTML = "<div id='perc-ba-error-main-wrapper'>" +
            "<div>The following assets were not approved.</div>" +
            "<div id='perc-ba-error-table-wrapper'>" +
            "<table class='perc-ba-error-table'>" +
            tableRows +
            "</table>" +
            "</div>" +
            "</div>";
        var dialog = percJQuery(dialogHTML).perc_dialog( {
            resizable : false,
            title: "Approval errors",
            modal: true,
            closeOnEscape : true,
            percButtons:{
                "Close":{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-ba-error-dialog-close"
                }
            },
            id: "perc-ba-error-dialog",
            width: 700,
            height:490
        });
    }

    // workflow status images
    var statusImgHtml_proc = "<img style='margin-left:5px;margin-bottom:-1px;' src='/cm/gadgets/repository/perc_workflow_status_gadget/images/processing.gif'/>";
    var statusImgHtml_tobe = "<img style='margin-left:5px;margin-bottom:-1px;' src='/cm/gadgets/repository/perc_workflow_status_gadget/images/tobeprocessed.png'/>";
    var statusImgHtml_error = "<img style='margin-left:5px;margin-bottom:-1px;' src='/cm/gadgets/repository/perc_workflow_status_gadget/images/errorIcon.gif'/>";

    //Search filter code
    $(document).ready(function() {
        var searchConfig;
        var labels = {"assetType":"Asset Type", "workflow": "Workflow", "state": "Status", "modifiedby": "Last Edited by"};
        var defaultWorkflow;
        function getDefaultConfig()
        {
            var searchConfig = {
                "assetType": {"name": "All", "value":"@all"},
                "workflow": (defaultWorkflow != null ? defaultWorkflow : {"name": "Default Workflow", "value":"6"}),
                "state": {"name": "Pending", "value":"4"},
                "modifiedby":{"name": "All", "value":"@all"}};
            return searchConfig;
        }

        function setDefaultWorkflow(callback)
        {
            percJQuery.PercWorkflowService().getDefaultWorkflow(function(status, result)
            {
                if (status)
                {
                    var value = result.EnumVals.entries[0].displayValue;
                    var name = result.EnumVals.entries[0].value;
                    defaultWorkflow = {"name": name, "value": value};
                }
                callback();
            });
        }

        function openSearchCriteriaDialog()
        {
            $.extend(searchConfig,{"type":"assets"});
            $.perc_gadgets_search_criteria_dialog.openSearchCriteriaDialog(searchConfig, function(resultObj)
                {
                    percJQuery.PercMetadataService.save("perc.user." + percJQuery.PercNavigationManager.getUserName() + ".dash.page.0.mid." + moduleId + ".prefs.search_criteria", JSON.stringify(resultObj), function(){
                        searchConfig = resultObj;
                        if ($.isEmptyObject(searchConfig))
                            searchConfig = getDefaultConfig();
                        var config = {
                            "container": $("#perc-filter-section"),
                            "criteriaObj" : searchConfig,
                            "labels": labels,
                            "selectFiltersCallback": openSearchCriteriaDialog,
                            "refreshSearchCallback": function(){loadTable(searchConfig);}
                        };
                        $.perc_gadgets_search_criteria_panel.buildSearchInfoPanel(config);
                        var criteriaContentFields = $("#perc-search-criteria-panel-content-fields");
                        var iframe = $(percJQuery.find("#remote_iframe_" + moduleId));
                        iframe.height(iframe.height() + (criteriaContentFields.length > 0 ? criteriaContentFields.height() + 15 : 0));
                        loadTable(searchConfig);
                    });
                },
                function(){});
        }

        function searchConfigCallback(resultObj)
        {
            searchConfig = resultObj;
            if ($.isEmptyObject(searchConfig))
                searchConfig = getDefaultConfig();

            var config = {
                "container": $("#perc-filter-section"),
                "criteriaObj" : searchConfig,
                "labels": labels,
                "selectFiltersCallback": openSearchCriteriaDialog,
                "refreshSearchCallback": function(){loadTable(searchConfig);}
            };

            $.perc_gadgets_search_criteria_panel.buildSearchInfoPanel(config);
            loadTable(searchConfig);
        }

        setDefaultWorkflow(function(){
            $.perc_gadgets_search_criteria_dialog.getSearchConfig("perc.user." + percJQuery.PercNavigationManager.getUserName() + ".dash.page.0.mid." + moduleId + ".prefs.search_criteria", function(resultObj)
            {
                var dataObj;
                if(typeof resultObj === "string"){
                    dataObj = JSON.parse(resultObj);
                }

                searchConfig = resultObj;

                if ($.isEmptyObject(searchConfig))
                {
                    percJQuery.PercMetadataService.find("perc.user." + percJQuery.PercNavigationManager.getUserName() + ".dash.page.0.prefs", function(status, data){
                        if (status === percJQuery.PercServiceUtils.STATUS_SUCCESS)
                        {
                            if (data != null && typeof data !== "undefined")
                            {
                                var dataObj;
                                if(typeof data.metadata.data === "string")
                                    dataObj = JSON.parse(data.metadata.data);

                                if (dataObj.userprefs["mid_" + moduleId] != null)
                                {
                                    var metaPrefsObj = dataObj.userprefs["mid_" + moduleId];
                                    var newSearchCriteria = getDefaultConfig();
                                    if (metaPrefsObj.assetType != null)
                                    {
                                        newSearchCriteria.assetType = {"name": metaPrefsObj.assetType, "value": metaPrefsObj.assetType};
                                    }
                                    percJQuery.PercReusableSearchService.getWorkflows(function(status, result)
                                    {
                                        if (metaPrefsObj.ssworkflow != null && metaPrefsObj.ssworkflow === "@all")
                                        {
                                            newSearchCriteria.workflow = {"name": "All", "value": "@all"};
                                        }
                                        else
                                        {
                                            for (var i = 0; i < result.length; i++)
                                            {
                                                var value = result[i].displayValue;
                                                var name = result[i].value;
                                                if (metaPrefsObj.ssworkflow != null && metaPrefsObj.ssworkflow === name)
                                                {
                                                    newSearchCriteria.workflow = {"name": name, "value": value};
                                                }
                                            }
                                        }
                                        if (newSearchCriteria.workflow != null && metaPrefsObj.status != null && metaPrefsObj.status !== "@all" )
                                        {
                                            percJQuery.PercReusableSearchService.getStates(newSearchCriteria.workflow.name, function(status, result)
                                            {
                                                for (var i = 0; i < result.length; i++)
                                                {
                                                    var value = result[i].displayValue;
                                                    var name = result[i].value;
                                                    if (metaPrefsObj.status != null && metaPrefsObj.status == name)
                                                    {
                                                        newSearchCriteria.state = {"name": name, "value": value};
                                                    }
                                                }
                                                searchConfigCallback(newSearchCriteria);
                                            });
                                        }
                                        else
                                        {
                                            newSearchCriteria.state = {"name": "All", "value": "@all"};
                                            searchConfigCallback(newSearchCriteria);
                                        }
                                    });
                                }
                                else
                                {
                                    searchConfigCallback({});
                                }
                            }
                            else
                            {
                                searchConfigCallback({});
                            }
                        }
                        else
                        {
                            searchConfigCallback({});
                        }
                    });
                }
                else
                {
                    searchConfigCallback(searchConfig);
                }
            });
        });

        var prefs = new gadgets.Prefs();
        var assetType = prefs.getString("assetType");
        var PercServiceUtils = percJQuery.PercServiceUtils;
        var pathService         = percJQuery.PercPathService;
        var wfService           = percJQuery.PercWorkflowService();
        var percUtils           = percJQuery.perc_utils;

        function loadTable(searchConfig)
        {
            $("#perc-search-criteria-panel-content-refresh-search").addClass("perc-disabled").off();
            $("#perc-bulk-approve-button").off().addClass("perc-disabled");
            $("#perc-search-criteria-panel-content-select-filters").off().css("color","#FFFFFF");
            loadingMsg = miniMsg.createStaticMessage("Loading...");
            var assetType = searchConfig.assetType.value;
            var workflow = searchConfig.workflow.value;
            var state = searchConfig.state.value;
            var lasteditedby = searchConfig.modifiedby.value;

            var assetName = assetType === "" || assetType === "@all"? "(All Assets)" : WidgetLabelByContentTypeId[assetType]?"(" + WidgetLabelByContentTypeId[assetType] + ")":"";
            var workflowName = searchConfig.workflow.name === "" || searchConfig.workflow.name === "All" ? "All Workflows" : searchConfig.workflow.name;
            var stateName = searchConfig.state.name === "" || searchConfig.state.name === "All" ? "All States" : searchConfig.state.name;

            var title = "ASSETS BY STATUS: " + workflowName + " - " + stateName + " " + assetName;
            gadgets.window.setTitle(title);

            var rows = parseInt(prefs.getString("zrows"));
            if(isNaN(rows))
                rows = 10;

            $("#perc-pagesbystatus-gadget").PercWorkflowGadget(assetType, workflow, state, lasteditedby, rows, moduleId, openSearchCriteriaDialog);
        }

    });


})(jQuery);
