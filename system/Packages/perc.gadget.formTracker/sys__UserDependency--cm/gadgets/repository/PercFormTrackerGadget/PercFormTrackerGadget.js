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
 *  PercFormTrackerGadget
 *  @auth Jose Annunziato
 *
 *  (*) Layout:
 *  +---------------------------------------------------------------+
 *  |^Description                           | New | Total |  Action |
 *  +---------------------------------------------------------------+
 *  |Form 1                                 |  3  |   6   |      [V]|
 *  |Description for form 1                 |     |       |         |
 *  +---------------------------------------------------------------+
 *  |Form 2                                 |  4  |   7   |      [V]|
 *  |                                       |     |       |         |
 *  +---------------------------------------------------------------+
 *  |Form 3                                 |  5  |   8   |      [V]|
 *  |Description for form 3                 |     |       ||Open   ||
 *  |                                       |     |       ||Preview||
 *  |                                       |     |       ||Export ||
 *  |                                       |     |       ||Clear  ||
 *  +---------------------------------------------------------------+
 *  |Showing 1 to 3 of 3 total results    First Prev 1 2 3 Next Last|
 *  +---------------------------------------------------------------+
 *
 *  (*) Behavior:
 *  Lists published (live) forms in a table as shown above.
 *  Click titles in table headers to sort ascending/descending.
 *  Click [V] icon to display menu to Open/Preview/Export/Clear the form.
 *  Only CM1 forms can be Open/Preview.
 *  If there is nothing to Export/Clear, selection is ignored.
 *  Select Export to download form submitted data as a CSV.
 *  Form must first be exported before cleared.
 *  Select Clear to clear latest submition data.
 *  Description column shows form title and description properties.
 *  If no title, name is used. Name is required when defining the form.
 *  Description is not required.
 *  New column displays new submissions.
 *  Total column displays total submissions.
 *  Action column is not sortable.
 *  Navigation controls at the bottom are same as datatable navigation controls
 *  
 *  (*) Dependencies:
 *  Widgets: in /cm/widgets/
 *  @see PercDataTable.js - thin wrapper around jquery datatable.js plugin
 *  @see PercSimpleMenu.js - simple menu shown under Action column
 *  @see PercCollapsibleTitle.js - displays form name/description on Description column
 *
 *  Styles: in /cm/gadgets/repository/common/css/
 *  @see PercDataTable.css - datatable styling specific to all gadgets
 *  @see PercSimpleMenu.css - simple menu styling specific to all gadgets
 */
(function($) {
    
    // constants
    var CM1_FORM_TYPE = 0, DELIVERY_FORM_TYPE = 1;
    var CM1_FORM_MENU_LABELS = ["Open Form", "Preview Form", "Export", "Clear"];
    var DELIVERY_FORM_MENU_LABELS = ["Export", "Clear"];
    var CM1_FORM_CALLBACKS = [editForm, previewForm, exportForm, clearForm];
    var DELIVERY_FORM_CALLBACKS = [exportForm, clearForm];
    var TABLE_HEADER_HEIGHT = 30;
    var TABLE_ROW_HEIGHT = 45;
    var TABLE_STATUS_FOOTER_HEIGHT = 45;
    var itemsPerPage = 5;
    
    // access framework
    var PercServiceUtils = percJQuery.PercServiceUtils;
    var perc_utils       = percJQuery.perc_utils;
    var formService      = percJQuery.PercFormService;
    
    // state variables
    var isLargeColumn = true;
    var oTable;
    var formTrackerTable = undefined;
    var tableDiv;
    var siteName;
    
    // API for this library
    $.fn.PercFormTrackerGadget = function(site,rows) {
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");
        if(site==""){
            var sites = [];
            $(".perc-listing-type-site", window.parent.document).each(function() {
                sites.push($(this).find("div.perc-finder-item-name").html());
            });
            site = sites[0];
        }
        siteName=site;
        tableDiv = $(this);
        gadgets.window.setTitle("FORMS TRACKER");

        loadGadget(site);
    }

    function loadGadget(site) {
        formService.getAllForms(site,function(status, result, message) {
            createFormTrackerTable(result, message)
        });
    }
    
    function createFormTrackerTable(formSummaryJson, message) {
        //If the gadget is in first column then we have to render it as large 
        isLargeColumn = gadgets.window.getDashboardColumn() == 1;
        
        //var formSummaries = formSummaryJson.FormSummary;
        var formSummaries = formSummaryJson.ArrayList;
        if(formSummaries == undefined) {
            formSummaries = [];
        }
        
        var menus = [];
        var percData = [];
        for(s=0; s<formSummaries.length; s++) {
            var formSummary = formSummaries[s];
            
            var description = formSummary.description;
            var id      = formSummary.id;
            var name    = formSummary.name;
            var title   = perc_utils.isBlankString(formSummary.title) ? name : formSummary.title;
            var state   = formSummary.state;
            var type    = formSummary.type;
            var newSubmissions = formSummary.newSubmissions;
            var totalSubmissions = formSummary.totalSubmissions;

            if(id == undefined || id == "") {
                menus.push($.PercFormTrackerDeliveryActions);
            } else {
                menus.push($.PercFormTrackerCM1Actions);
            }
            
            var row = {rowData : {formSummary : formSummary, pageId : formSummary.id, pagePath : formSummary.path}, rowContent : [[title, description], newSubmissions, totalSubmissions]};
            percData.push(row);
        }
        var aoColumns = [
            { sType: "string"},
            { sType: "numeric"},
            { sType: "numeric"}
        ];

        var percVisibleColumns = null;
        if(!isLargeColumn)
            percVisibleColumns = [0,1];

        var percHeaders = ["Description", "New", "Total"];

        var percColumnWidths = ["*","38","40"];

        var config = {percRowDblclickCallback : openForm, percColumnWidths : percColumnWidths, percVisibleColumns : percVisibleColumns, iDisplayLength : itemsPerPage, percData : percData, percHeaders : percHeaders, aoColumns : aoColumns, percMenus : menus};
        config.oLanguage = { sZeroRecords: (message!= null)? message : "No forms found."};
        
        tableDiv.PercActionDataTable(config);
        
        miniMsg.dismissMessage(loadingMsg);

        // Fix height 10 pixels for the iframe so the actions menu from the middle fits to open downwards
        var iframe = this.window.parent.jQuery("div[name='Forms Tracker'] iframe");
        iframe.height(320);        
    }

    function displayErrorMessage(message) {
        tableDiv.append("<div class='perc-gadget-errormessage'>" + message + "</div>");
        miniMsg.dismissMessage(loadingMsg);
    }
    
    $.PercFormTrackerCM1Actions = { title : "", menuItemsAlign : "left", stayInsideOf : ".perc-datatable",
            items : [
                {label : "Open Form",    callback : editForm},
                {label : "Preview Form", callback : previewForm},
                {label : "<a><div>Export</div></a>",  callback : exportForm},
                {label : "Clear",   callback : clearForm}
    ]};
    
    $.PercFormTrackerDeliveryActions = { title : "", menuItemsAlign : "left", stayInsideOf : ".perc-datatable",
            items : [
                {label : "<a><div>Export</div></a>",  callback : exportForm},
                {label : "Clear",   callback : clearForm}
    ]};
    
    function editForm(event) {
        var formSummary = getRowDataFromMenuEvent(event);
        percJQuery.PercNavigationManager.openAsset(formSummary.formSummary);
    }
    
    function openForm(event) {
        var rowData = event.data;
        var formSummary = rowData.formSummary;
        if(formSummary.id == undefined || formSummary.id == "") {
            //nothing to open
            return;
        }
        percJQuery.PercNavigationManager.openAsset(formSummary);
    }
    
    function previewForm(event) {
        var formSummary = getRowDataFromMenuEvent(event);
        percJQuery.perc_finder().launchAssetPreview(formSummary.formSummary.id);
    }
    
    function exportForm(event) {
    	if(!event.data)
    		return;

		var formName = event.data.formSummary.name;
		var totalSubmissions = event.data.formSummary.totalSubmissions;

        if (totalSubmissions == 0)
            return;

        formService.getAllSubmissions(siteName,formName, function(status, result) {
            if(status == PercServiceUtils.STATUS_SUCCESS) {
                //
            } else {
                perc_utils.alert_dialog({title: 'Error', content: result});
            }
            window.location.reload();
        });
    }
    
    function clearForm(event) {
        var formSummary = getRowDataFromMenuEvent(event);
        var formSum = formSummary.formSummary;
        if (formSum.newSubmissions == formSum.totalSubmissions)
        {
            // nothing to clear
            return;
        }
        var formTitle = formSum.title;
        var formName = formSum.name;
        var name = (!perc_utils.isBlankString(formTitle)) ? formTitle : formName;
        perc_utils.confirm_dialog({
           title: 'Confirm Form Clear',
           question: 'Are you sure you want to clear all exported submissions for form \'' + name + '\'?',
           cancel:
               function(){},
           success:
               function(){
                  formService.clearForm(siteName,formName, function(status, result) {
                      if(status == PercServiceUtils.STATUS_SUCCESS) {
                          // noop
                      } else {
                          if(result != undefined && result != "")
                              perc_utils.alert_dialog({title: 'Error', content: result});
                      }
                      window.location.reload();
                  });
               }
        });
    }
    
    function getRowDataFromMenuEvent(event) {
        var menuItem = event.currentTarget;
        var tableRow = $(menuItem).parents(".perc-datatable-row");
        return tableRow.data("percRowData");
    }
})(jQuery);