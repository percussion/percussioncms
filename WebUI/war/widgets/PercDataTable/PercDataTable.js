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
 *  Implements an abstraction of datatable plugin.
 *  @author Jose Annunziato
 *
 */
(function($) {

    var MARGIN_PX = 16;
    var PADDING_BOTTOM_PX = 5;
    var tableDom;
    var configo;

    /**
     *  PercDataTable(config)
     *  @param config
        { percData:
            [                                   Array of rows
                {                               Row 1
                    callback : rowclick,        Callback when clicking this row (optional) (future implementation)
                {   rowContent : [              Array of columns per row
                        [{content : "Comment 4", title : "/Site/Site1", callback : open}],      Array of elements in TDs.  Column 1
                        ["7/22/33","22:44:55 PM"]],                                             Elements can be text/html. Column 2
                    rowData : {commentId : 11123, pageId : 1123, pagePath : "/aewq/dsa/cxz"}},  Object associated with row
                {                               Row 2
                    rowContent : [
                        [{content : "Comment 5", title : "/Site/Site2"}],
                        ["8/22/33","22:44:55 PM"]],
                    rowData : {commentId : 21123, pageId : 2123, pagePath : "/sewq/dsa/cxz"}},...
         *
     */
    $.fn.PercDataTable = function(config) {
        // fix aoColumns config based on percVisibleColumns
        // throw out column configs that are not visible
        if(config.percVisibleColumns && config.aoColumns) {
            var newAoColumns = [];
            $.each(config.percVisibleColumns, function(index, visibleColumnIndex){
                newAoColumns.push(config.aoColumns[visibleColumnIndex]);
            });
            config.aoColumns = newAoColumns;
        }

        for(let c=0; c<config.aoColumns.length; c++){
            var aoColumn = config.aoColumns[c];
            aoColumn.sSortDataType = "perc-type-"+aoColumn.sType;
        }

        // build the HTML table and convert it to a dataTable
        config = $.extend(true, {}, defaultConfig, config);
        configo = config;
        tableDom = buildTableDomFromData(config);
        $(this).append(tableDom);
        tableDom
            .dataTable(config)
            .data("config",config);

        // resize parent iframe's height to fit the table
        if(config.percExpandParentFrameVertically) {
            var height = tableDom.parents(".dataTables_wrapper").outerHeight() + MARGIN_PX;
            var width  = tableDom.parents(".dataTables_wrapper").outerWidth()  + MARGIN_PX;
            var parentFrame = getParentFrame();
            var frameHeight = height;
            var additionalHt = config.additionalIframeHeight?config.additionalIframeHeight:0;
            if(config.iDisplayLength) {
                var headHeight = tableDom.find("thead").height();
                var oneRowHeight = $(tableDom.find("tbody tr")[0]).height();
                var paginatorHeight = $(".dataTables_paginate").height();
                var frameMinHeight = headHeight + oneRowHeight * config.iDisplayLength + paginatorHeight + additionalHt;
                frameHeight = frameMinHeight;
            }
            if(config.percStayBelow) {
                var belowElement = $(config.percStayBelow);
                frameHeight += belowElement.offset().top + belowElement.outerHeight() + MARGIN_PX;
            }
            $(parentFrame).height(frameHeight + PADDING_BOTTOM_PX);
        }

        return tableDom;
    };

    function tableRedrawCallback() {
        var dataTable = $(this);
        var config = dataTable.data("config");

        if(config && config.percTableRedrawCallback)
        {
            if(typeof(config.percTableRedrawCallback) === "object")
            {
                $.each(config.percTableRedrawCallback, function(index, value){
                    this(dataTable);
                });
            }
            else if(typeof(config.percTableRedrawCallback) === "function")
            {
                config.percTableRedrawCallback(dataTable);
            }
        }
        setTimeout(function(){
            // prepend Pages label before page numbers
            var paginator = dataTable.parent().children(".dataTables_paginate.paging_full_numbers");
            paginator.find('.perc-datatable-paginator-pages-label').removeClass('paginate_button');
            // add page number attribute to each page for QA
            var pageNumbers = paginator.find("span span.paginate_button, span span.paginate_active");
            $.each(pageNumbers, function(index, element){
                $(element).attr("perc-page", index + 1);
            });
            if(!pageNumbers || pageNumbers.length < 2)
                paginator.hide();
            else
                paginator.show();
            paginator.css("position","absolute");
        }, 1);

    }

    function getParentFrame() {
        var arrFrames = parent.document.getElementsByTagName("IFRAME");
        for (var i = 0; i < arrFrames.length; i++) {
            if (arrFrames[i].contentWindow === window)
                return arrFrames[i];
        }
    }

    function footerRedrawCallback( nFoot, aasData, iStart, iEnd, aiDisplay ) {
        if (configo.bPaginate){
            var config = configo;

            var itemsPerPage = config.iDisplayLength;
            var totalItemsCount = aiDisplay.length;
            var pages = Math.ceil(totalItemsCount / itemsPerPage);
            var currentPageNumber = Math.ceil(iEnd / itemsPerPage);
            var pageOfPages = currentPageNumber + " of " + pages + (pages === 1 ? " Page" : " Pages");

            if(pages === 0)
                pageOfPages = "";

            var pInfo = $(".perc-datatables-info");
            if(pInfo.length > 0){
                pInfo.html(pageOfPages);
            } else {
                $("<div class='datatables_info perc-datatables-info'>"+pageOfPages+"</div>")
                    .appendTo("body");
            }

            var gFooterBar = $(".perc-footer-bar");
            if(gFooterBar.length === 0){
                $("<div class='perc-footer-bar'>&nbsp;</div>")
                    .appendTo("body");
            }
        }
    }

    var defaultConfig = {
        percExpandParentFrameVertically : true,
        additionalIframeHeight : 0, //The Iframe height is calculated based on the rows and other things, if a gadget requires additional height they can specify by this property
        percColumnWidths : ["*","123"],
        percRowDblclickCallback : $.PercOpenPage,
        showPreviewBtnOnHover: false,
        iDisplayLength : 5,
        bFilter: false,
        bAutoWidth : false,
        bPaginate : true,
        bSort: true,
        sPaginationType : "full_numbers",
        bLengthChange : false,
        bInfo : true,
        fnDrawCallback : tableRedrawCallback,
        fnFooterCallback: footerRedrawCallback,
        oLanguage : {sZeroRecords: "No Pages Found", oPaginate : {sFirst : "&lt;&lt;", sPrevious : "&lt;", sNext : "&gt;", sLast : "&gt;&gt;"}, sInfo : " ", sInfoEmpty : " "}
    };

    /**
     *  Builds a Table DOM from the array of arrays in the percData configuration
     *  @param config table configuration containing percData and percHeaders
     *
     *  Generates the following DOM
     *
     *  <pre>
     *  <table cellspacing="0" cellpadding="0" class="perc-datatable">
     *      <thead>
     *          <tr class="perc-datatable-head-row">
     *              <th class="perc-datatable-head-column perc-index-0 perc-first sorting_asc">Page</th>
     *              <th class="perc-datatable-head-column perc-index-1 sorting">Heading 2</th>
     *          </tr>
     *      </thead>
     *      <tbody>
     *          <tr class="perc-datatable-row perc-index-0 perc-first odd">
     *              <td valign="top" class="perc-datatable-column perc-index-0 perc-first sorting_1">
     *                  <div class="perc-datatable-columnrow perc-index-0 perc-first" title="">Comment 11</div>
     *                  <div class="perc-datatable-columnrow perc-index-1 perc-last" title="">/Site/Site22</div>
     *              </td>
     *              <td valign="top" class="perc-datatable-column perc-index-1 ">
     *                  <div class="perc-datatable-columnrow perc-index-0 perc-first" title="">12/22/33</div>
     *                  <div class="perc-datatable-columnrow perc-index-1 perc-last" title="">22:44:55 PM</div>
     *              </td>
     *           </tr>
     *           <tr class="perc-datatable-row perc-index-1 per-last even">
     *              <td valign="top" class="perc-datatable-column perc-index-0 perc-first sorting_1">
     *                  <div class="perc-datatable-columnrow perc-index-0 perc-first" title="">Comment 22</div>
     *                  <div class="perc-datatable-columnrow perc-index-1 perc-last" title="">/Site/Site33</div>
     *               </td>
     *               <td valign="top" class="perc-datatable-column perc-index-1 ">
     *                   <div class="perc-datatable-columnrow perc-index-0 perc-first" title="">13/22/33</div>
     *                   <div class="perc-datatable-columnrow perc-index-1 perc-last" title="">22:44:55 PM</div>
     *              </td>
     *          </tr>
     *      </tbody>
     *  </table>
     *  </pre>
     *
     */
    function buildTableDomFromData(config) {
        var data = config.percData;
        var headers = config.percHeaders;

        // create the table and body
        var table = $("<table class='perc-datatable' style='table-layout : fixed' cellpadding='0' cellspacing='0'>");
        var tbody = $("<tbody>");

        var aoColumns = config.aoColumns;

        // iterate over the data containing rows
        $.each(data, function(rowIndex, element){
            var row = element;

            // mark the first and last table rows
            var firstLast = "";
            if(rowIndex === 0)
                firstLast = "perc-first";
            else if(rowIndex === data.length-1)
                firstLast = "perc-last";

            // create the table row
            var rowTr = $("<tr class='perc-datatable-row perc-index-"+rowIndex+" "+firstLast+"'>");

            if(row.rowData)
                rowTr.data("percRowData", row.rowData);

            // bind click event callbacks
            if(config.percRowClickCallback)
            {
                if(row.rowData)
                {
                    rowTr.on("click",null,row.rowData,
                        function(evt){
                            config.percRowClickCallback(evt);
                        });
                }
                else
                {
                    rowTr.on("click",function(e){
                        config.percRowClickCallback(e);
                    });
                }
            }

            // bind mouseover event callbacks
            if(config.showPreviewBtnOnHover)
            {

                rowTr.on("mouseover",function() {
                    $(this).css('background-color', '#CAF589');
                    $(this).find('.perc-preview-col').show();

                }).on("mouseout",function(){
                    $(this).css('background-color', 'white');
                    $(this).find('.perc-preview-col').hide();
                });
            }

            if(config.percRowDblclickCallback)
            {
                if(row.rowData)
                {
                    rowTr.on("dblclick",row.rowData, function(e){
                        config.percRowDblclickCallback(e);
                    });
                }
                else
                {
                    rowTr.on("dblclick",function(e){
                        config.percRowDblclickCallback(e);
                    });
                }
            }

            // iterate over the columns in each row
            var aoIndex = 0;
            $.each(row.rowContent, function(colIndex, element){

                // skip over non visible columns
                if(config.percVisibleColumns)
                    if($.inArray(colIndex, config.percVisibleColumns)==-1)
                        return true;

                var aoColumn = aoColumns[aoIndex++];
                var sType = aoColumn.sType;
                var percType = "perc-type-"+sType;

                var column = element;
                // mark the first and last column
                var firstLast = "";
                if(colIndex == 0)
                    firstLast = "perc-first";
                else if(colIndex == row.rowContent.length-1)
                    firstLast = "perc-last";
                else
                    firstLast = "perc-middle";

                var headerClass = "";
                //Header classes are auto generated by the element text, if the element text happens to be invalid for a class name,
                //users of this table can pass another array from which the header classes can be created
                if(config.percHeaderClasses && config.percHeaderClasses[colIndex])
                {
                    headerClass = "perc-"+config.percHeaderClasses[colIndex].replace(/ /g,"-").toLowerCase();
                }
                else
                {
                    headerClass = "perc-"+config.percHeaders[colIndex].replace(/ /g,"-").toLowerCase();
                }

                // create the table data
                var columnTd = $("<td class='"+percType+" "+headerClass+" perc-datatable-column perc-ellipsis perc-index-"+colIndex+" perc-cell-"+colIndex+"-"+rowIndex+" "+firstLast+"' valign='top'>");
                var columnRow;
                if(typeof column === "object") {
                    var content = "";
                    var title = "";

                    // iterate over the rows within a table cell
                    if(Array.isArray(column)) {
                        $.each(column, function(colRowIndex, element){
                            if(!element)
                                element = "&nbsp;";
                            var columnRowData = element;
                            var firstLast = "";
                            if(colRowIndex === 0)
                                firstLast = "perc-first";
                            else if(colRowIndex === column.length-1)
                                firstLast = "perc-last";
                            else
                                firstLast = "perc-middle";
                            // if it's just a string, then that's the content, otherwise it's an object with content and maybe a title
                            if(typeof columnRowData == "string") {
                                content = columnRowData;
                            } else {
                                columnRowData = $.extend({ "content" : "", "title" : "" }, columnRowData);
                                content = columnRowData.content;
                                title = columnRowData.title;
                            }

                            if(title === "&nbsp;")
                                title = "";

                            // finally, wrap the content in a div and then add it to the table data
                            columnRow = $("<div style='width:100%' class='perc-datatable-columnrow perc-ellipsis perc-index-"+colRowIndex+" "+firstLast+"'>");
                            if(columnRowData.callback) {
                                var cBack = $("<span>")
                                    .attr("title", title)
                                    .append(content);
                                cBack
                                    .css("cursor", "pointer")
                                    .addClass("perc-callback");
                                columnRow.append(cBack);
                                if(row.rowData)
                                    cBack.on("click",null, row.rowData, function(e){
                                        columnRowData.callback(e);
                                    });
                                else
                                    cBack.on("click",function(e){
                                        columnRowData.callback(e);
                                    });
                            }
                            else {
                                columnRow
                                    .attr("title", title)
                                    .append(content);
                            }

                            columnTd.append(columnRow);
                        });
                    } else {
                        let title = element.title;
                        let content = element.content;
                        columnRow = $("<div title='"+title+"' class='perc-datatable-columnrow perc-ellipsis perc-index-0 perc-first'>")
                            .append(content);
                        columnTd.append(columnRow);
                    }
                } else {
                    columnRow = $("<div class='perc-datatable-columnrow perc-ellipsis perc-index-0 perc-first'>")
                        .append(element);
                    columnTd.append(columnRow);
                }
                // add the table data to the row
                rowTr.append(columnTd);
            });
            // add the row to the table body
            tbody.append(rowTr);
        });
        // add the table body to the table
        table.append(tbody);

        if(headers) {
            var thead = $("<thead>");
            var row = $("<tr class='perc-datatable-head-row'>");
            var aoIndex = 0;
            $.each(headers, function(index, element){
                // skip over non visible columns
                if(config.percVisibleColumns)
                    if($.inArray(index, config.percVisibleColumns)===-1)
                        return true;

                var aoColumn = aoColumns[aoIndex++];
                var sType = aoColumn.sType;
                var percType = "perc-type-"+sType;

                var columnWidth = "";
                if(config.percColumnWidths) {
                    if(index < config.percColumnWidths.length-1) {
                        columnWidth = config.percColumnWidths[index];
                        if(columnWidth === "*")
                            columnWidth = "";
                    } else {
                        columnWidth = config.percColumnWidths[config.percColumnWidths.length-1];
                    }
                }

                // if($.browser.browser.msie || $.browser.webkit)
                //     columnWidth = parseInt(columnWidth) + 20;

                columnWidth += "px";

                var headerClass = "";
                if(config.percHeaderClasses && config.percHeaderClasses[index])
                {
                    headerClass = "perc-"+config.percHeaderClasses[index].replace(/ /g,"-").toLowerCase();
                }
                else
                {
                    headerClass = "perc-"+config.percHeaders[index].replace(/ /g,"-").toLowerCase();
                }


                var firstLast = "";
                if(index === 0)
                    firstLast = "perc-first";
                else if(index === headers.length-1)
                    firstLast = "perc-last";
                else
                    firstLast = "perc-middle";
                var head = $("<th class='"+percType+" "+headerClass+" perc-datatable-head-column perc-index-"+index+" "+firstLast+"'>")
                    .css('width', columnWidth);

                var sortingDirection = $("<span class='perc-sort' style='padding: 0px 10px 0px 0px; border-bottom:none'>&nbsp;</span>");

                //Asign external sort function.
                if (typeof(config.sortFunction) !== "undefined" && !config.bSort){
                    var colName = config.percColNames[index];
                    var data = {};
                    data.colName = colName;
                    data.sortFunction = config.sortFunction;
                    head.on("click",null,data, function(e){
                        sortingHandler(e);
                    });

                    //Avoid select text on double click in the headers.
                    if($.browser.mozilla)
                        head.css('MozUserSelect','none');
                    else if($.browser.msie)
                        head.on('selectstart',function(){return false;});

                    //SortOrder == asc or desc
                    if (colName === config.sortColumn){
                        head.addClass("sorting_" + config.sortOrder);
                    }
                }

                head.append(element);
                head.append(sortingDirection);
                row.append(head);
            });
            thead.append(row);
            table.append(thead);
        }
        if(config.bSort)
            declareCustomSortingFunctions();

        return table;
    }

    function sortingHandler(event){
        var head = $(this);
        var element = event.data.colName;
        var callback = event.data.sortFunction;
        head.siblings().removeClass("sorting_asc").removeClass("sorting_desc");
        var order = "asc";
        if (head.is(".sorting_asc")){
            head.removeClass("sorting_asc").addClass("sorting_desc");
            order = "desc";
        }else if (head.is(".sorting_desc")){
            head.removeClass("sorting_desc").addClass("sorting_asc");
        }else{
            head.addClass("sorting_asc");
        }
        callback(element, order);
    }

    function declareCustomSortingFunctions() {
        // custom column sorting for Change, Views, and Template column
        // checks to see if all data is the same and if so it changes it
        // so that it is unique to force it to sort in reverse order
        /* $.fn.dataTableExt.afnSortData['perc-type-string'] = function  ( oSettings, iColumn ) {
            var aData = [];
            var data;
            $( 'td:eq('+iColumn+')', oSettings.api.rows().nodes() ).each( function () {
                data = $(this).text();
                aData.push( data );
            });
            return aData;
        };

        $.fn.dataTableExt.afnSortData['perc-type-numeric'] = $.fn.dataTableExt.afnSortData['perc-type-string'];

        // custom column sorting for date columns
        // changes the seconds so that if the date, minutes and hours are the same
        // it will be forced to sort in reverse order
        $.fn.dataTableExt.afnSortData['perc-type-date'] = function  ( oSettings, iColumn ) {
            var aData = [];
            $( 'td:eq('+iColumn+')', oSettings.rows(oSettings).nodes() ).each( function () {
                var dateTimeArray = Array("", "");
                var divs = $(this).find('div');

                dateTimeArray[0] = $(divs[0]).text();
                dateTimeArray[1] = $(divs[1]).text();
                var dateString = dateTimeArray.join(' ');
                var date = new Date(dateString);
                aData.push(date);
            });
            return aData;
        };
        */
    }
})(jQuery); 
