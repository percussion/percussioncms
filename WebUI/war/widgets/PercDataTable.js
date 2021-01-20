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
 *  PercDataTable.js
 *  @author Jose Annunziato
 *
 *  Description:
 *  Thin plugin layer over jQuery's datatable plugin http://www.datatables.net/
 *  to implement Percussion specific custom behavior.
 *  
 *  @param percHeaders   (string[]) Labels for the headers
 *  @param percColsLeft  (int[]) indices of columns to show if gadget is on the left
 *  @param percColsRight (int[]) indices of columns to show if gadget is on the right
 *  @param percData      (string[][]) 2 dimentional array of data. rows of columns
 *  @param percHeaderClasses (string[]) classes to add to each of the headers
 *
 */
(function($) {
    
    // constants
    var S_INFO_LEFT  = "_START_ to _END_ of _TOTAL_";
    var S_INFO_RIGHT = "Showing _START_ to _END_ of _TOTAL_ total results";
    
    var defaultConfig = {
        percHeaderClasses : [],
        iDisplayLength : 5,
        bFilter: false,
        bAutoWidth : false,
        bPaginate : true,
        sPaginationType : "full_numbers",
        bLengthChange : false,
        bInfo : true,
        fnDrawCallback  : function() {
            // fix the ellipsis on every draw
            if ($.browser.msie) {
                $(".perc-ellipsis").each(function(){
                    handleOverflow($(this));
                });
            }
        },
        // if on first or last page, update the disabled color of the sequential pagination controls
        fnFooterCallback: function( nFoot, aasData, iStart, iEnd, aiDisplay ) {
            // set them all to their default active color
            $(".first").addClass("perc-percdatatable-active").removeClass("perc-percdatatable-disabled");
            $(".previous").addClass("perc-percdatatable-active").removeClass("perc-percdatatable-disabled");
            $(".next").addClass("perc-percdatatable-active").removeClass("perc-percdatatable-disabled");
            $(".last").addClass("perc-percdatatable-active").removeClass("perc-percdatatable-disabled");
            // if on the first page disable First and Previous controls
            if(iStart == 0) {
                $(".first").addClass("perc-percdatatable-disabled").removeClass("perc-percdatatable-active");
                $(".previous").addClass("perc-percdatatable-disabled").removeClass("perc-percdatatable-active");
            }
            // if on the last page disable Last and Next controls
            if(iEnd == aasData.length) {
                $(".next").addClass("perc-percdatatable-disabled").removeClass("perc-percdatatable-active");
                $(".last").addClass("perc-percdatatable-disabled").removeClass("perc-percdatatable-active");
            }
        }
    };
    
    $.fn.percDataTable = function (config) {
        
        var sInfo = S_INFO_RIGHT;
        var columnWidths = [];
        if(config.percColumnWidths && config.percColumnWidths.length > 0)
            columnWidths = config.percColumnWidths[0];
        
        // TODO: do we really need this dependency with gadgets? could be a configuration
        var isRightColumn = true;
        if(gadgets) {
            // if the gadget is in first column then we have to render it as large 
            isRightColumn = gadgets.window.getDashboardColumn() == 1;
            sInfo = isRightColumn ? S_INFO_RIGHT : S_INFO_LEFT;
            if(columnWidths.length > 1)
                columnWidths = isRightColumn ? config.percColumnWidths[0] : config.percColumnWidths[1];
        }
        
        fixPercColsLeftAndRightIfUndefined(config);
        
        // merge and override default and custom configurations
        config = $.extend({}, defaultConfig, config);

        config.oLanguage.sInfo = sInfo;

        var table = $(this);
        
        table.addClass("perc-datatable");
        
        // create headers
        var headers = config.percHeaders;
        table.append("<thead><tr>");
        var tableHeaderRow = table.find("thead tr");
            
        var indices = config.percColsLeft;
        if(isRightColumn)
            indices = config.percColsRight;
            
        var headerClasses = config.percHeaderClasses;
        for(i=0; i<(indices.length); i++) {
            var h = indices[i];
            var header = headers[h];
            var headerClass = "";
            var headerWidth = "";
            if(headerClasses && headerClasses.length > 0)
                headerClass = headerClasses[h];
            if(columnWidths && columnWidths.length > 0)
                headerWidth = columnWidths[i];
            // put header text in a SPAN so we move the sorting arrow with the header
            tableHeaderRow.append("<th class='perc-datatable-header perc-col-" + i + " " + headerClass + "' style='width:"+headerWidth+"; max-width:"+headerWidth+"'><span>"+header+"</span></th>");
        }

        // create rows
        table.append("<tbody>");
        var percRows  = config.percData;
        var tableBody = table.find("tbody");
        tableBody.html("");
        for(r=0; r<percRows.length; r++) {
            var tableRow = $("<tr class='perc-row-"+r+"'>");
            var percRow = percRows[r];
            for(i=0; i<indices.length; i++) {
                var d = indices[i];
                var tableData = $("<td class='perc-cell-"+r+"-"+i+"'>");
                var data = percRow[d];
                if(data==="" || data===undefined)
                    data = "&nbsp;";
                tableData.append(data);
                tableRow.append(tableData);
            }
            tableBody.append(tableRow);
        }

        declareCustomSortingFunctions();
        
        var oTable = table.dataTable(config);
            
        // only fix ellipsis in IE
        if ($.browser.msie) {
            // fix text overflow at first
            $(".perc-ellipsis").each(function() {
                handleOverflow($(this));
            });
            
            // fix text overflow when window resizes
            $(window).bind('resize', function(){
                $(".perc-ellipsis").each(function(){
                    handleOverflow($(this));
                });
            });
        }
                
        return oTable;
    }

    function handleOverflow(element) {
        var title = element.attr("title");
        if(title === '')
            return true;
        var width = element.parents("td").width();
        element.css("width",width);
    }

    /**
     *  if percColsLeft is undefined then we want all columns to show on the left column
     *  same for percColsRight for the right column
     */
    function fixPercColsLeftAndRightIfUndefined(config) {
        if(config.percColsLeft == undefined) {
            config.percColsLeft = [];
            var headers = config.percHeaders;
            for(h=0;h<headers.length;h++) {
                config.percColsLeft.push(h);
            }
        }
        if(config.percColsRight == undefined) {
            config.percColsRight = [];
            var headers = config.percHeaders;
            for(h=0;h<headers.length;h++) {
                config.percColsRight.push(h);
            }
        }
    }

    function declareCustomSortingFunctions() {
        // custom column sorting for Change, Views, and Template column
        // checks to see if all data is the same and if so it changes it
        // so that it is unique to force it to sort in reverse order
        $.fn.dataTableExt.afnSortData['dom-ptext'] = function  ( oSettings, iColumn ) {
            var aData = [];
            var different = false;
            var previous = null;
            var data;
            $( 'td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings) ).each( function () {
                data = $(this).text();
                if(previous != null && previous != data)
                    different = true;
                previous = data;
                aData.push( data );
            });
            if(!different) {
                for(i=0; i<aData.length; i++) {
                    aData[i] = aData[i] + i;
                };
            }
            return aData;
        }

        // custom column sorting for Page Link column
        $.fn.dataTableExt.afnSortData['dom-page'] = function  ( oSettings, iColumn ) {
            var aData = [];
            $( 'td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings) ).each( function () {
                var data = $(this).text();
                data = data.substring(0,data.indexOf("/"));
                aData.push( data );
            });
            return aData;
        }

        // custom column sorting for date columns
        // changes the seconds so that if the date, minutes and hours are the same
        // it will be forced to sort in reverse order
        $.fn.dataTableExt.afnSortData['dom-date'] = function  ( oSettings, iColumn ) {
            var aData = [];
            var seconds = 0;
            $( 'td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings) ).each( function () {
                var dateTimeArray = Array("", "");
                dateTimeArray[0] = $(this).find('.top-line').text();
                dateTimeArray[1] = (new String($(this).find('.bottom-line').text())).replace(/^(.*?)\s*\(.+$/, "$1"); // Get rid of username.
                var dateString = dateTimeArray.join(' ');
                var date = new Date(dateString);
                aData.push(new Date(date.setSeconds(seconds)));
                seconds++;
            });
            return aData;
        }
    }
})(jQuery); 
