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
 * The delivery side archive list view class. Makes a call to the service to get page list entries and renders them in the
 * list.
 * On document ready loops through all perc-archive-list elements on the page and finds the query from the data attribute
 * on them. Passes the query and gets the entries from the service. If service returns an error, logs the error and
 * does nothing.
 */
(function($)
{
    $(function(){
        $.PercArchiveListView.updateArchiveList();
    });
    $.PercArchiveListView = {
        updateArchiveList : updateArchiveList
    };
    function updateArchiveList()
    {
        $(".perc-archive-list").each(function(){
            var currentArchiveList = $(this);
            if ("" === currentArchiveList.attr("data-query")) {return;}
            var queryString = JSON.parse(currentArchiveList.attr("data-query"));


            //Get the render option: hierarchical or flat
            var renderOption = queryString.layout;
            delete queryString.layout;

            //Get the result page to display the pages for each tag
            var pageResult = queryString.archive_page_result;
            delete queryString.archive_page_result;

            //Get the number of entries to be shown
            var numberEntries = queryString.numberEntries;
            delete queryString.numberEntries;

            var isEditMode = queryString.isEditMode;

            var isPreviewMode = queryString.isPreviewMode;

            //Set the base URL to create the href for each item then
            var baseURL = "";
            if(isEditMode==="true" || isPreviewMode == "true"){
                var paths = window.location.pathname.split("/");
                baseURL = "/" + paths[1] + "/" + paths[2];
            }else{
                baseURL = window.location.protocol + '//' + window.location.host;
            }

            var strJSON = JSON.stringify(queryString);

            $.PercArchiveListService.getArchiveEntries(queryString, function(status, archiveList){
                if(status)
                {
                    if ("undefined" !== typeof (renderOption) && "" !== renderOption && "perc-list-hierarchical" === renderOption)
                    {
                        if(typeof (archiveList)!== "undefined") {
                            archiveList = $.PercServiceUtils.toJSON(archiveList);
                        }
                        var itemToList = archiveList.years.length;
                        if("undefined" !== typeof (numberEntries) && "" !== numberEntries)
                        {
                            if(archiveList.years.length < numberEntries)
                            {
                                itemToList = archiveList.years.length;
                            }
                            else
                            {
                                itemToList = numberEntries;
                            }
                        }

                        var ul = $("<ul>").addClass("perc-archive-list-wrapper perc-archive-hierarchical");

                        for (var i = 0; i < itemToList; i++) {
                            var row = archiveList.years[i];
                            let query = "";
                            let anchorYear="";
                            let linkYearText="";
                            let yearParam1 = "";
                            let yearParam2 = "";
                            let encodedQuery ="";
                            let monthsUl ="";
                            let monthIndex = 0;
                            let yearLi = "";
                            let row2="";
                            let stringMonthParam = "";
                            let daysInMonth = "";
                            let dateParam1 = "";
                            let dateParam2 = "";
                            let linkText = "";
                            let a ="";
                            let li = "";
                            let href="";

                            // If year don't have any entry - skip the year
                            if(0 < row.yearCount)
                            {
                                //Create the li element for the year
                                linkYearText = row.year + " (" + row.yearCount + ")";

                                //Set the link for the item Year
                                yearParam1 = row.year + "-01-01 00:00:00";
                                yearParam2 = row.year + "-12-31 23:59:59";
                                if ("undefined" === typeof (pageResult) || "" === pageResult || isEditMode === "true"){
                                    anchorYear = $("<a href='#'>")
                                        .text(linkYearText);
                                }else{
                                    query = JSON.parse( strJSON );
                                    query.criteria.push("dcterms:created >= '" + yearParam1 + "'");
                                    query.criteria.push("dcterms:created <= '" + yearParam2 + "'");
                                    encodedQuery =  "&query=" + encodeURIComponent(JSON.stringify(query));
                                    href =  baseURL + pageResult + "?filter="+ encodeURIComponent(row.year) + encodedQuery;
                                    anchorYear = $("<a>")
                                        .attr("href",href)
                                        .text(linkYearText);

                                }

                                 yearLi = $("<li>")
                                    .addClass("perc-archive-year")
                                    .append(anchorYear);
                                monthsUl = $("<ul class='perc-archive-month-wrapper'/>");
                                //JSON object return the list of months in desc order, starting with December
                                monthIndex = 12;

                                if(0 === i)
                                {
                                    monthIndex = archiveList.years[i].months.length;
                                }

                                for (let j = 0; j < archiveList.years[i].months.length; j++) {
                                    row2 = archiveList.years[i].months[j];

                                    if(1 > row2.count)
                                    {
                                        monthIndex = monthIndex - 1;
                                        continue;
                                    }
                                    //Generate the param date to be passed as part of the query criteria

                                    if (10 <= monthIndex)
                                    {
                                        stringMonthParam = monthIndex;
                                    }
                                    else
                                    {
                                        stringMonthParam = "0" + monthIndex;
                                    }

                                     daysInMonth = new Date(row.year, monthIndex, 0).getDate();
                                     dateParam1 = row.year + "-"+ stringMonthParam + "-01 00:00:00";
                                     dateParam2 = row.year + "-"+ stringMonthParam + "-" + daysInMonth + " 23:59:59";

                                    //Generate the link text
                                    linkText = row2.month + " (" + row2.count + ")";

                                    //Decrease the counter for the monthIndex
                                    if (0 < monthIndex)
                                    {
                                        monthIndex = monthIndex - 1;
                                    }

                                    //Set the link for the item month
                                    if(isEditMode === "true" || "undefined" === typeof (pageResult) || "" === pageResult ){
                                        a = $("<a href='#'>")
                                            .text(linkText);
                                    }else{
                                        query = JSON.parse( strJSON );
                                        query.criteria.push("dcterms:created >= '" + dateParam1 + "'");
                                        query.criteria.push("dcterms:created <= '" + dateParam2 + "'");
                                        encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(query));
                                        href = baseURL + pageResult + "?filter="+  encodeURIComponent(row2.month + " " + row.year )+ encodedQuery;
                                        a = $("<a>")
                                            .attr("href",href )
                                            .text(linkText);
                                    }

                                     li = $("<li>")
                                        .addClass("perc-archive-month")
                                        .append(a);
                                    monthsUl.append(li);
                                }
                                yearLi.append(monthsUl);
                                ul.append(yearLi);
                            }
                            else {
                                console.debug("Not sure how we got here.");
                            }
                        }

                        currentArchiveList.find(".perc-archive-list-container").html("").append(ul);
                    }
                    else {
                        if(typeof (archiveList)!== "undefined") {
                            archiveList = $.PercServiceUtils.toJSON(archiveList);
                        }

                        //Set the variable if max entry value is defined
                        let flatItemToList=0;
                        if(typeof(numberEntries) !== "undefined" && numberEntries !== "")
                        {
                             flatItemToList =  numberEntries;
                        }
                        let listCounter = 0;
                        let ul = $("<ul>").addClass(".perc-archive-list-wrapper perc-archive-flat");
                        for (let i = 0; i < archiveList.years.length; i++) {

                            let row = archiveList.years[i];
                            //JSON object return the list of months in desc order, starting with December

                            let monthIndex = 12;
                            if(i === 0)
                            {
                                monthIndex = archiveList.years[i].months.length;
                            }

                            for (let j = 0; j < archiveList.years[i].months.length; j++)
                            {

                                let row2 = archiveList.years[i].months[j];


                                if(row2.count < 1)
                                {
                                    monthIndex = monthIndex - 1;
                                    continue;
                                }
                                //Generate the param date to be passed as part of the query criteria
                                let stringMonthParam = "";
                                if (monthIndex >= 10)
                                {
                                    stringMonthParam = monthIndex;
                                }
                                else
                                {
                                    stringMonthParam = "0" + monthIndex;
                                }
                                let daysInMonth = new Date(row.year, monthIndex, 0).getDate();
                                let dateParam1 = row.year + "-"+ stringMonthParam + "-01 00:00:00";
                                let dateParam2 = row.year + "-"+ stringMonthParam + "-" + daysInMonth + " 00:00:00";
                                let a = {};
                                //Generate the link text
                                let linkText = row2.month + " " + row.year + " (" + row2.count + ")";

                                //Decrease the counter for the monthIndex
                                if (monthIndex > 0)
                                {
                                    monthIndex = monthIndex - 1;
                                }

                                //Set the link for the item month
                                if (typeof(pageResult) === "undefined" ||  pageResult === "" || isEditMode === "true" ){
                                    a = $("<a href = '#'>")
                                        .text(linkText);
                                }else {
                                    let query = JSON.parse(strJSON );
                                    query.criteria.push("dcterms:created >= '" + dateParam1 + "'");
                                    query.criteria.push("dcterms:created <= '" + dateParam2 + "'");
                                    let encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(query));
                                    let href = baseURL + pageResult + "?filter="+ encodeURIComponent(row2.month +" "+ row.year) + encodedQuery;
                                    a = $("<a>")
                                        .attr("href", href)
                                        .text(linkText);
                                }

                                let li = $("<li>")
                                    .addClass("perc-archive-month")
                                    .append(a);
                                ul.append(li);
                                //Increment the max value counter by 1.                     
                                listCounter = listCounter + 1;

                                //If max value is defined and counter is equal to max value break the month loop
                                if(listCounter === flatItemToList && typeof(numberEntries) !== "undefined" && numberEntries !== "")
                                {
                                    break;
                                }

                            }
                            //If max value is defined and counter is equal to max value break the year loop
                            if(listCounter === flatItemToList && typeof(numberEntries) !== "undefined" && numberEntries !== "")
                            {
                                break;
                            }
                        }
                        currentArchiveList.find(".perc-archive-list-container").html("").append(ul);
                    }
                }

            });

        });
    }
})(jQuery);
