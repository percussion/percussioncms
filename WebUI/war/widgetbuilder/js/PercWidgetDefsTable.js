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

(function($)
{
    WidgetBuilderApp.WidgetDefSummaryModel = Backbone.Model.extend();

    WidgetBuilderApp.WidgetDefSummariesCollection = Backbone.Collection.extend({
        model: WidgetBuilderApp.WidgetDefSummaryModel
    });
    
    var wdgDefSummaries = new WidgetBuilderApp.WidgetDefSummariesCollection();
    
    
    
    var wdgDefColumns = [{
      name: "widgetId", // The key of the model attribute
      label: "ID", // The name to display in the header
      editable: false,
      renderable:false,
      // By default every cell in a column is editable, but *ID* shouldn't be
      // Defines a cell type, and ID is displayed as an integer without the ',' separating 1000s.
      cell: Backgrid.IntegerCell.extend({
        orderSeparator: ''
      })
    }, {
      name: "label",
      label: "Name",
      editable: false,
      cell: "string"
    }, {
      name: "description",
      label: "Description",
      editable: false,
      cell: "string"
    }, {
      name: "author",
      label: "Author",
      editable: false,
      cell: "string"
    }, {
      name: "publisherUrl",
      label: "Publisher URL",
      editable: false,
      cell: "string",
    }, {
      name: "version",
      editable: false,
      label: "Version",
      cell: "string" // Renders the value in an HTML anchor element
    }, {
        name: "widgetTrayCustomizedIconPath",
        label: "Custom Widget Tray Icon Path",
        editable: false,
        cell: "string"
    }, {
        name: "toolTipMessage",
        label: "ToolTip Message",
        editable: false,
        cell: "string"
    }];
    
    var wdgDefRow = Backgrid.Row.extend({
      events: {
        "click": "selectRow",
      },
      selectRow:function(){
        this.$el.parent().find(".perc-wb-def-selected-row").removeClass("perc-wb-def-selected-row");
        this.$el.addClass("perc-wb-def-selected-row");
        WidgetBuilderApp.selectedModel = this.model.get("widgetId");
        WidgetBuilderApp.updateToolBarButtons();
      }
    });
    // Initialize a new Grid instance
    WidgetBuilderApp.WdgDefGrid = new Backgrid.Grid({
      row: wdgDefRow,
      columns: wdgDefColumns,
      collection: wdgDefSummaries
    });

    //Load the widget defnitions, so that the table gets rendered.
    WidgetBuilderApp.loadDefinitions = function(){
        $.PercWidgetBuilderService.getWidgetDefSummaries(function(status, result){
            if(!status){
                $.perc_utils.alert_dialog({"title":"Widget error", "content":result});
                return;
            }
            var sums = [];
            $.each(result.WidgetBuilderSummaryData, function(){
                var wdgObject = {};
                $.each(this, function(key, value){
                    wdgObject[key] = value;
                });
                sums.push(wdgObject);
            });
            wdgDefSummaries.reset(sums);
            //Select the row if we have selected widget.
            if(WidgetBuilderApp.selectedModel)
            {
                $($('tr:has(td:contains(' + WidgetBuilderApp.selectedModel + '))')).addClass("perc-wb-def-selected-row");
            }
        });
    }
})(jQuery);
