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
 *  PercInlineEditDataTable.js
 *  @author federicoromanelli
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
    var nEditing = null;

    // Exposed API/interface
    $.PercInlineEditDataTable = {
        init                : init,
        enableTable			: enableTable
    };

    var defaultConfig = {
        percHeaderClasses : [],
        percDeleteRow: true,
        percEditableCols : [],
        bFilter: false,
        bAutoWidth : false,
        bPaginate : false,
        bInfo : false,
        bSort: false,
        percNoTableHeaders : false,
        percAddRowElementId : "perc-inline-edit-datatable-add-row",
        percNewRowDefaultValues : [],
        percShowValuesPlaceholders : false,
        percPlaceHolderValues : [],
        fnDrawCallback  : function() {
            // fix the ellipsis on every draw
            if ($.browser.msie) {
                $(".perc-ellipsis").each(function(){
                    handleOverflow($(this));
                });
            }
        }
    };

    function enableTable (container, enabled)
    {
        var table = $(container);
        if (enabled)
        {
            table.find("td").removeClass("perc-disabled");
        }
        else
        {
            table.find("td").addClass("perc-disabled");
        }
    }

    function init (container, config, dlgCallbackFunc) {
        nEditing = null;

        var table = $(container);
        $('#' + config.percAddRowElementId).off("click");
        $(table).find('td span').off("click");
        $(table).find('td input').off("click").off('focusout');
        $(table).find('a.perc-inline-edit-datatable-delete-row').off('click');
        table.html("");

        var columnWidths = [];
        if(config.percColumnWidths && config.percColumnWidths.length > 0)
            columnWidths = config.percColumnWidths;

        // merge and override default and custom configurations
        config = $.extend({}, defaultConfig, config);



        table.addClass("perc-inline-edit-datatable");

        // create headers
        var headers = config.percHeaders;


        if (config.percDeleteRow)
        {
            headers.push ("");
            columnWidths.push("10");
            config.percNewRowDefaultValues.push("");
        }

        var indices = headers.length;

        table.append("<thead><tr>");
        var tableHeaderRow = table.find("thead tr");

        var headerClasses = config.percHeaderClasses;
        for(i=0; i < indices; i++) {
            var h = i;
            var header = headers[h];
            var headerClass = "";
            var headerWidth = "";
            if(headerClasses && headerClasses.length > 0)
                headerClass = headerClasses[h];
            if(columnWidths && columnWidths.length > 0)
                headerWidth = columnWidths[i];
            tableHeaderRow.append("<th class='perc-datatable-header perc-col-" + i + " " + headerClass + "' style='width:"+headerWidth+"px; max-width:"+headerWidth+"px'><span>"+header+"</span></th>");
        }

        // create rows
        table.append("<tbody>");
        var percRows  = config.percData;
        var tableBody = table.find("tbody");
        tableBody.html("");
        for(r=0; r<percRows.length; r++) {
            var tableRow = $("<tr class='perc-row-"+r+"'>");
            var percRow = percRows[r];
            if (config.percDeleteRow)
            {
                percRow.rowContent.push('<a class="perc-inline-edit-datatable-delete-row" href=""></a>');
            }
            for(i=0; i<indices; i++) {
                var d = i;
                var columnWidth = "";
                if(columnWidths && columnWidths.length > 0)
                    columnWidth = columnWidths[i];
                var tableData = $("<td style='width:"+columnWidth+"px; max-width:"+columnWidth+"px' id='perc-cell-"+r+"-"+i+"' class='perc-cell-"+r+"-"+i+"'>");
                var data = percRow.rowContent[d];
                if(data==="" || data===undefined)
                    data = "&nbsp;";

                var placeHolderClass = "";
                if (config.percShowValuesPlaceholders)
                {
                    var placeHolderValue = "";

                    if ($.type(config.percPlaceHolderValues[i]) === "string")
                    {
                        placeHolderValue = config.percPlaceHolderValues[i];
                    }
                    else
                    {
                        placeHolderValue = config.percPlaceHolderValues[r][i];
                    }
                    if (data.trim() === placeHolderValue)
                    {
                        placeHolderClass = "perc-placeholder";
                    }
                }

                tableData.append("<span class='" + placeHolderClass + "'>" + data + "</span>");
                tableRow.append(tableData);
            }

            tableBody.append(tableRow);
        }
        if (config.percNoTableHeaders)
        {
            table.find("thead").css("display", "none");
        }
        if(oTable){
            oTable.fnDestroy();
        }
        var oTable = table.dataTable(config);

        if (percRows.length == 0)
        {
            addRow();
        }

        function editCol ( nRow, nCol )
        {
            var aData = oTable.fnGetData(nRow);
            var jqTds = $('>td', nRow);
            $(nRow).addClass("perc-edit-mode");
            var idParts = nCol.id.split("-");
            var colNumber = parseInt(idParts.pop());
            if (!config.percEditableCols[colNumber])
                return;
            var placeHolderValue = "";
            if ($.type(config.percPlaceHolderValues[colNumber]) === "string")
            {
                placeHolderValue = config.percPlaceHolderValues[colNumber];
            }
            else
            {
                var rowNumber = oTable.fnGetPosition( nRow );
                placeHolderValue = config.percPlaceHolderValues[rowNumber][colNumber];
            }

            var value = "";

            if ($(aData[colNumber]).length > 0)
            {
                value = $(aData[colNumber]).text().trim();
                if (config.percShowValuesPlaceholders && value === placeHolderValue)
                    value = "";
                nCol.innerHTML = '<input id="inputEdition'+ colNumber +'" placeholder="' + placeHolderValue + '" type="text" value="'+ value +'" />';
            }
            else
            {
                value = aData[colNumber].trim();
                if (config.percShowValuesPlaceholders && value === placeHolderValue)
                    value = "";
                nCol.innerHTML = '<input id="inputEdition'+ colNumber +'" placeholder="' + placeHolderValue + '" type="text" value="'+ value +'" />';

            }
            $(table).find('td input').on('focusout', tableCellFocusOut);
            var labelHtml = '<label class="visuallyhidden" for="inputEdition'+ colNumber +'">Search:</label>';
            table.parent().append(labelHtml);
            addPlaceHolder();

            $(nCol).find("input").trigger("focus");
        }

        // Edit Cells
        function editRow ( nRow, isOnlyRow )
        {
            var aData = oTable.fnGetData(nRow);
            var jqTds = $('>td', nRow);
            $(nRow).addClass("perc-edit-mode");
            for (var i = 0; i < jqTds.length; i++)
            {
                //because third column is for delete button.
                if(i>1){
                    break;
                }
                var placeHolderValue = "";
                if ($.type(config.percPlaceHolderValues[i]) === "string")
                {
                    placeHolderValue = config.percPlaceHolderValues[i];
                }
                else
                {
                    var rowNumber = oTable.fnGetPosition( nRow );
                    placeHolderValue = config.percPlaceHolderValues[rowNumber][i];
                }
                $(table).parent().append('<label class="visuallyhidden" for="inputEdition'+ i +'">Search:</label>');
                var value = "";
                if ($(aData[i]).length > 0)
                {
                    value = $(aData[i]).text().trim();
                    if (config.percShowValuesPlaceholders && value === placeHolderValue)
                        value = "";

                    jqTds[i].innerHTML = '<input id="inputEdition'+ i +'" placeholder="' + placeHolderValue + '" type="text" value="'+ value +'" />';
                }
                else
                {
                    value= "";
                    if(typeof aData[i] != 'undefined'){
                        value = aData[i].trim();
                    }

                    if (config.percShowValuesPlaceholders && value === placeHolderValue)
                        value = "";
                    jqTds[i].innerHTML = '<input id="inputEdition'+ i +'" placeholder="' + placeHolderValue + '" type="text" value="'+ value +'" />';
                }
            }
            addPlaceHolder();

            if (!isOnlyRow)
            {
                $(jqTds[0]).find("input").trigger("focus");
            }
        }

        function restoreRow ( nRow )
        {
            var aData = oTable.fnGetData(nRow);
            var jqTds = $('>td', nRow);
            var length = jqTds.length;
            if (config.percDeleteRow)
                length--;
            for ( var i=0, iLen = length ; i<iLen ; i++ ) {

                oTable.fnUpdate( "<span>" + aData[i] + "</span>", nRow, i, false );
            }

            oTable.fnDraw();
            $(nRow).append('<td><a class="perc-inline-edit-datatable-delete-row" href=""></a></td>');
        }

        function saveRow (nRow)
        {
            var jqInputs = $('input', nRow);
            var jqTds = $('>td', nRow);
            $(nRow).removeClass("perc-edit-mode");
            var value1 = jqInputs[0].value;
            var placeHolderClass1 = "";
            var value2 = jqInputs[1].value;
            var placeHolderClass2 = "";
            if (config.percShowValuesPlaceholders && value1.trim() === "")
            {
                var placeHolderValue = "";
                if ($.type(config.percPlaceHolderValues[0]) === "string")
                {
                    placeHolderValue = config.percPlaceHolderValues[0];
                }
                else
                {
                    var rowNumber = oTable.fnGetPosition( nRow );
                    placeHolderValue = config.percPlaceHolderValues[rowNumber][0];
                }
                placeHolderClass1 = "perc-placeholder";
                value1 = placeHolderValue;
            }

            if (config.percShowValuesPlaceholders && value2.trim() === "")
            {
                var placeHolderValue2 = "";
                if ($.type(config.percPlaceHolderValues[1]) === "string")
                {
                    placeHolderValue2 = config.percPlaceHolderValues[1];
                }
                else
                {
                    var rowNumber2 =  oTable.fnGetPosition( nRow );
                    placeHolderValue2 = config.percPlaceHolderValues[rowNumber2][1];
                }
                placeHolderClass2 = "perc-placeholder";
                value2 = placeHolderValue2;
            }

            oTable.fnUpdate( "<span class='" + placeHolderClass1 + "'>" + value1 + "</span>", nRow, 0, false );
            oTable.fnUpdate( "<span class='" + placeHolderClass2 + "'>" + value2 + "</span>", nRow, 1, false );
            oTable.fnDraw();
            if (config.percDeleteRow)
            {
                $(nRow).append('<td style="width:10px; max-width:10px"><a style="display:none;" class="perc-inline-edit-datatable-delete-row" href=""></a></td>');
            }
            var newRowsDefaultValues = defaultConfig.percNewRowDefaultValues;
            if (jqInputs[0].value.trim() === newRowsDefaultValues[0])
            {
                deleteRow(nRow);
            }
        }

        function saveCol ( nRow, nCol )
        {
            var jqInputs = $('input', nCol);
            $(nRow).removeClass("perc-edit-mode");
            var idParts = nCol.id.split("-");
            var colNumber = parseInt(idParts.pop());
            var value = jqInputs[0].value;
            var placeHolderClass = "";
            if (config.percShowValuesPlaceholders && value.trim() === "")
            {
                var placeHolderValue = "";
                if ($.type(config.percPlaceHolderValues[colNumber]) === "string")
                {
                    placeHolderValue = config.percPlaceHolderValues[colNumber];
                }
                else
                {
                    var rowNumber = oTable.fnGetPosition( nRow );
                    placeHolderValue = config.percPlaceHolderValues[rowNumber][colNumber];
                }
                placeHolderClass = "perc-placeholder";
                value = placeHolderValue;
            }

            oTable.fnUpdate( "<span class='" + placeHolderClass + "'>" + value + "</span>", nRow, colNumber, false );
            oTable.fnDraw();
            var newRowsDefaultValues = defaultConfig.percNewRowDefaultValues;
            if (jqInputs[0].value.trim() === newRowsDefaultValues[0])
            {
                deleteRow(nRow);
            }

            if (colNumber === 1)
            {
                displayDeleteButton(nRow);
            }
        }

        function displayDeleteButton(nRow)
        {
            var jqTds = $('td', nRow);
            if ($(jqTds[1]).find("span").text().trim() !== ""){
                $(nRow).find(".perc-inline-edit-datatable-delete-row").css("display", "block");
                $(nRow).find(".perc-inline-edit-datatable-delete-row").on('click', deleteRowButtonAction);
            }
        }

        function addColIds (nRow)
        {
            var jqTds = $('>td', nRow);
            var rowNumber = oTable.fnGetPosition( nRow );
            for ( var i = 0, iLen=jqTds.length ; i<iLen ; i++ ) {
                $(jqTds[i]).attr("id", "perc-cell-" + rowNumber + "-" + i);
            }
        }

        if (config.percAddRowElementId && config.percAddRowElementId != "")
        {
            // Add row action
            $('#' + config.percAddRowElementId).on("click", function (e) {
                e.preventDefault();
                var data = oTable.fnGetData();
                var value1 = "";
                var value2 = "";
                if ($(data[0][0]).length > 0)
                {
                    value1 = $(data[0][0]).text().trim();
                }
                else
                {
                    value1 = data[0][0].trim();
                }
                if ($(data[0][1]).length > 0)
                {
                    value2 = $(data[0][1]).text().trim();
                }
                else
                {
                    value2 = data[0][1].trim();
                }
                if (data.length > 1 || (data.length === 1 && value1 !== "" && value2 !== ""))
                {
                    addRow();
                    $(table).find('td input').on('focusout', tableCellFocusOut);
                }
            } );
        }

        function tableCellFocusOut(e){

            $(table).parent().find("label.visuallyhidden-with-placeholder").remove();
            $(table).parent().find("label.visuallyhidden").remove();
            e.stopPropagation();
            e.preventDefault();

            var nRow = $(this).parents('tr')[0];
            var nCol = $(this).parents('td')[0];
            if (oTable.fnGetData().length === 1)
            {
                nEditing = nRow;
            }
            if ( nEditing === nRow) {

                var jqInputs = $('input', nRow);
                if (jqInputs.length > 1)
                {
                    saveRow(nEditing);
                }
                else
                {
                    saveCol(nEditing, nCol);
                }
                nEditing = null;

                if (jqInputs.length > 1)
                {
                    nEditing = nRow;
                    var jqTds = $('td', nRow);
                    var jqSpans = $('span', nRow);
                    if ($(jqTds[0]).find("span").text().trim() !== "")
                    {
                        if ($(jqTds[1]).find("span").text().trim() === "")
                        {
                            editCol(nRow, jqTds[1]);
                        }
                        else
                        {
                            $(nRow).find(".perc-inline-edit-datatable-delete-row").css("display", "block");
                        }
                    }

                }
            }
        }


        $(table).find('td input').on('focusout', tableCellFocusOut);

        $(table).find('td span').on('click', tableCellOnClick);

        function tableCellOnClick(e){
            e.stopPropagation();
            e.preventDefault();
            if (!$(this).parents('td').hasClass("perc-disabled"))
            {
                /* Get the row as a parent of the link that was clicked on */
                var nRow = $(this).parents('tr')[0];
                var nCol = $(this).parents('td')[0];

                if ( nEditing !== null && nEditing !== nRow ) {
                    /* Currently editing - but not this row - restore the old before continuing to edit mode */
                    try
                    {
                        restoreRow( nEditing );
                    } catch(ex){
                        //console.error(ex);
                    }
                    editCol( nRow, nCol );
                    nEditing = nRow;
                }
                else {
                    /* No edit in progress - let's start one */
                    editCol( nRow, nCol );
                    nEditing = nRow;
                }
            }

        }

        function addRow(isOnlyRow)
        {

            var newRowsDefaultValues = Array.from(config.percNewRowDefaultValues);

            var aiNew = oTable.fnAddData( newRowsDefaultValues );

            if ( nEditing !== null && nEditing !== nRow ) {
                try
                {
                    restoreRow( nEditing );
                } catch(e){}
            }
            var nRow = oTable.fnGetNodes( aiNew[0] );
            addColIds(nRow);
            editRow( nRow, isOnlyRow );
            nEditing = nRow;
        }
        // Delete row action
        $(table).find('a.perc-inline-edit-datatable-delete-row').on('click', deleteRowButtonAction);

        function deleteRowButtonAction(e){
            e.stopPropagation();
            e.preventDefault();
            var nRow = $(e.currentTarget).parents('tr')[0];
            deleteRow(nRow);
        }

        function deleteRow( nRow)
        {
            oTable.fnDeleteRow( nRow );
            if (oTable.fnGetData().length === 0)
            {
                addRow(true);
            }
        }
        function addPlaceHolder()
        {
            if(!('placeholder' in $('<input>')[0] || 'placeHolder' in $('<input>')[0]))
            {
                table.find('input[placeholder]').placeHolder({hideOnFocus: false});
            }
        }
        // only fix ellipsis in IE
        if ($.browser.msie) {
            // fix text overflow at first
            $(".perc-ellipsis").each(function() {
                handleOverflow($(table));
            });

            // fix text overflow when window resizes
            $(window).on('resize', function(evt){
                $(".perc-ellipsis").each(function(){
                    handleOverflow($(table));
                });
            });
        }
        table.css("width", "inherit");
        addPlaceHolder();
        return oTable;
    }

    function handleOverflow(element) {
        var title = element.attr("title");
        if(title === '')
            return true;
        var width = element.parents("td").width();
        element.css("width",width);
    }
})(jQuery);
