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

(function($)
{
    var defaultConfig =
        {

        };
    var _folderDblClickCallback = function(){};
    $.fn.PercFinderListView = function(config, serviceContent)
    {
        var self = $(this);
        var serviceData;
        if(typeof config.folderDblClickCallback === "function") {
            _folderDblClickCallback = config.folderDblClickCallback;
        }
        if (serviceContent.PagedItemList !== undefined)
        {
            serviceData = $.perc_utils.convertCXFArray(serviceContent.PagedItemList.childrenInPage);
            self.data('totalResult', serviceContent.PagedItemList.childrenCount);
            self.data('startIndex', serviceContent.PagedItemList.startIndex);
        }
        else
        {
            serviceData = $.perc_utils.convertCXFArray(serviceContent.PathItem);
        }

        var c = parseContenIntoConfig(config.displayFormat, serviceData);

        var percData     = c.percData;
        var aoColumns    = c.percTypes;
        if(percData.length > 0)
        {
            var arrayRowContent = [];
            for(let s=0; s<aoColumns.length; s++)
            {
                arrayRowContent.push("&nbsp;");
            }

            percData.push({rowContent : arrayRowContent});
        }
        var percHeaders  = c.percHeaders;
        var percColNames = c.percColNames;
        var percWidths   = c.percWidths;
        var percColumnWidths = percWidths;

        var configDT = {percRowClickCallback : rowClickCallback, percRowDblclickCallback : rowDblclickCallback, percColumnWidths : percColumnWidths, percData : percData, percHeaders : percHeaders, aoColumns : aoColumns};
        configDT.oLanguage = {"sZeroRecords": "No Files Found"};
        configDT.bPaginate = false;
        configDT.bInfo = false;
        configDT.bSort = false;
        configDT.oLanguage.sZeroRecords = "No Items Found";
        configDT.sortFunction = config.sortFunction;
        configDT.sortColumn = config.sortColumn;
        configDT.sortOrder = config.sortOrder;
        configDT.percColNames = percColNames;

        self.empty();
        self.PercDataTable(configDT);

        var table = $(self.find("table"));
        var container = $(table.parents(".mcol-direc"));
        table.PercFixedTableHeader({"resizable":$(".perc-finder-body.ui-resizable"), "remove":$("#perc-finder-choose-columnview"), "container":container});

        if (typeof(config.callback) != "undefined")
            config.callback(self);

        createItemsDragAndDrop(table);

        $(".perc-datatable-row:last").off();
    };

    function createItemsDragAndDrop (table)
    {
        var allRows = table.find(".perc-datatable-row");
        $.map( allRows, function(val, i) {
            if ($(val).data("percRowData") !== undefined && $(val).data("percRowData").category === "ASSET")
            {
                $(val).css("cursor", "default");
                $(val).draggable( {
                    helper: function() {
                        return $('<div />')
                            .html($(this).find(".perc-datatable-columnrow").html())
                            .addClass("dataTables_wrapper")
                            .css('color', "white");
                    },
                    appendTo: 'body',
                    refreshPositions: true,
                    zIndex: 9990,
                    revert: true,
                    revertDuration: 0,
                    start: $.perc_finder().onDragStart,
                    stop: $.perc_finder().onDragStop,
                    delay: $.perc_finder().dragDelay
                });
            }
        });
        // This droppable is temporal to gain the effect of disablement like the column view
        // (it will be updated to be functional with the other list finder drag and dropping functionality
        $(table).droppable( {
            tolerance: 'pointer',
            accept: false,
            over: function(){},
            out: function(){},
            drop: function(){} } );
    }

    /**
     * Callback function invoked after clicking a row in the list view.
     * @param rowData jQuery.Event
     */
    function rowClickCallback(rowData)
    {
        $(".perc-datatable-row").removeClass("perc-datatable-row-highlighted");
        $(this).addClass("perc-datatable-row-highlighted");
        var newPath = $.merge([""], getItemFolderPath(rowData));

        // Reflect the path change in the input (pathbar) on top of the finder and after that,
        // and invoke the  "change path" listeners with the new one
        $("#mcol-path-summary").val(newPath.join('/'));

        //Set the current item in the Finder.
        if (typeof(rowData.data.id) != "undefined") {
            $.perc_finder().setCurrentItem(rowData.data);
        }
        $.perc_finder().flagChangeView = false;
        $.perc_finder().executePathChangedListeners(newPath);
        $.perc_finder().flagChangeView = true;
    }

    function getItemFolderPath(rowData)
    {
        if(!rowData.data)
            return "";
        var folderPath;
        if (rowData.data.type === "site") // if click on a site don't need to include the item name
        {
            folderPath = rowData.data.folderPaths[0];
        }
        else
        {
            folderPath = rowData.data.folderPaths[0] + "/" + rowData.data.name;
        }

        return folderPath.replace("Folders/$System$/", "").substring(2,folderPath.length).split("/");
    }

    function rowDblclickCallback(rowData)
    {
        if(rowData.data.type==="Folder" || rowData.data.type==="FSFolder")
        {
            _folderDblClickCallback("/" + getItemFolderPath(rowData).join("/"));
        }
        else
        {
            $.PercNavigationManager.openPathItem("/" + getItemFolderPath(rowData).join("/"));
        }
    }

    function parseContenIntoConfig(displayFormat, serviceData)
    {
        var percData    = [];
        var percTypes   = [];
        var percColNames = [];
        var percHeaders = [];
        var percWidths = [];

        var columns = displayFormat.SimpleDisplayFormat.columns;

        var c, s;
        for(s=0; s<serviceData.length; s++)
        {
            var dataRow = $.perc_utils.convertCXFArray(serviceData[s].columnData.column);
            var nameValueMap = nameValueObjectArrayToMap(dataRow, "name", "value");
            var percRow = [];

            var icon = $.perc_utils.choose_icon( serviceData[s].type, serviceData[s].icon, getItemFolderPath({data: serviceData[s]}) );
            var iconHtml = "<img style=\"float:left;\" src=\"" + icon.src + "\" alt=\""+ icon.alt + "\" title=\"" + icon.title +"\" aria-hidden=\"" + icon.decorative + "\" />";

            for(c=0; c<columns.length; c++)
            {
                var column = columns[c];
                var colName = column.name;
                var colLabel = column.label;
                var data = nameValueMap[colName];

                // format date with no seconds
                if (column.type.toLowerCase() === "date" && data !== null  && data !== "")
                {
                    var dateParts = $.perc_utils.splitDateTime(data);
                    var dateAndTime = dateParts.date + ", " + dateParts.time;
                    data = "<div title='"+dateAndTime+"'>"+ dateParts.date + "</div>";
                }

                if (column.type.toLowerCase() === "number" && data != null  && data !== "")
                {
                    data = '<div title = "' + data + ' Bytes" style="text-align:right;">' + $.perc_utils.formatFileSize(data) + "</div>";
                }
                // The first column should have this kind of tooltip (that's why c != 0)
                if (column.type.toLowerCase() === "text" && data != null  && data !== "" && c !== 0)
                {
                    data = '<span title = "' + data + '" style="font-weight: normal;">' + data + "</span>";
                }

                if(data == null || data == "")
                    data = "&nbsp;";

                if (c == 0 && iconHtml != "")
                {
                    var itemPath = "", itemPathRaw, serviceDataItem = serviceData[s];
                    // DANGER: folderPaths may contain an array
                    // What should we do if it is an array? For now, just using the fist one!
                    // HACK: CM-4488 search fails to parse results correctly
                    if (Array.isArray(serviceDataItem.folderPaths)) {
                        itemPathRaw = serviceDataItem.folderPaths[0];
                    } else {
                        itemPathRaw = serviceDataItem.folderPaths;
                    }
                    // to fix CMS-6402	.
                    if(itemPathRaw == undefined){
                        itemPathRaw ="" ;
                    }
                    if(serviceData[s].type == 'site') {
                        itemPath = itemPathRaw.replace('/' + $.perc_paths.SITES_ROOT, '');
                    }
                    else {
                        itemPath = itemPathRaw.replace('/' + $.perc_paths.SITES_ROOT, '')
                            .replace('/' + $.perc_paths.DESIGN_ROOT, '')
                            .replace('//Folders/$System$' + $.perc_paths.ASSETS_ROOT, '') + '/' + serviceData[s].name;
                    }
                    data = iconHtml + '<span title = "'+ itemPath + '" style="padding-left:4px;">' + data + "</span>";
                }

                percRow.push(data);
            }

            var percContent = {"rowContent" : percRow, "rowData" : serviceData[s] };
            percData.push(percContent);
        }


        for(c=0; c<columns.length; c++)
        {
            let column  = columns[c];
            percColNames.push(column.name);
            percHeaders.push(column.label);
            percWidths.push((column.width == -1 ? "*" : ($.browser.msie ? column.width - 20 : column.width ) ));
            var type = column.type.toLowerCase();
            if(type === "text") {
                type = "string";
            }

            if (c === 0)
            {
                type = "html";
            }
            percTypes.push({"sType" : type});
        }

        var config = {"percData" : percData, "percColNames":percColNames, "percHeaders":percHeaders, "percTypes":percTypes, "percWidths": percWidths};
        return config;
    }

    function nameValueObjectArrayToMap(dataRow, nameKey, valueKey)
    {
        var d;
        var map = {};
        for(d=0;d<dataRow.length;d++)
        {
            var data  = dataRow[d];
            var name  = data[nameKey];
            var value = data[valueKey];
            map[name] = value;
        }
        return map;
    }
})(jQuery);
