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
                            var query = "";
                            var anchorYear="";
                            var linkYearText="";
                            var yearParam1 = "";
                            var yearParam2 = "";
                            var encodedQuery ="";
                            var monthsUl ="";
                            var monthIndex = 0;
                            var yearLi = "";
                            var row2="";
                            var stringMonthParam = "";
                            var daysInMonth = "";
                            var dateParam1 = "";
                            var dateParam2 = "";
                            var linkText = "";
                            var a ="";
                            var li = "";
                            var href="";

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

                                for (var j = 0; j < archiveList.years[i].months.length; j++) {
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
                        var flatItemToList=0;
                        if(typeof(numberEntries) !== "undefined" && numberEntries !== "")
                        {
                             flatItemToList =  numberEntries;
                        }
                        var listCounter = 0;
                        var ul = $("<ul>").addClass(".perc-archive-list-wrapper perc-archive-flat");
                        for (var i = 0; i < archiveList.years.length; i++) {

                            var row = archiveList.years[i];
                            //JSON object return the list of months in desc order, starting with December

                            var monthIndex = 12;
                            if(i === 0)
                            {
                                monthIndex = archiveList.years[i].months.length;
                            }

                            for (var j = 0; j < archiveList.years[i].months.length; j++)
                            {

                                var row2 = archiveList.years[i].months[j];


                                if(row2.count < 1)
                                {
                                    monthIndex = monthIndex - 1;
                                    continue;
                                }
                                //Generate the param date to be passed as part of the query criteria
                                var stringMonthParam = "";
                                if (monthIndex >= 10)
                                {
                                    stringMonthParam = monthIndex;
                                }
                                else
                                {
                                    stringMonthParam = "0" + monthIndex;
                                }
                                var daysInMonth = new Date(row.year, monthIndex, 0).getDate();
                                var dateParam1 = row.year + "-"+ stringMonthParam + "-01 00:00:00";
                                var dateParam2 = row.year + "-"+ stringMonthParam + "-" + daysInMonth + " 00:00:00";
                                var a = {};
                                //Generate the link text
                                var linkText = row2.month + " " + row.year + " (" + row2.count + ")";

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
                                    var query = JSON.parse(strJSON );
                                    query.criteria.push("dcterms:created >= '" + dateParam1 + "'");
                                    query.criteria.push("dcterms:created <= '" + dateParam2 + "'");
                                    var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(query));
                                    var href = baseURL + pageResult + "?filter="+ encodeURIComponent(row2.month +" "+ row.year) + encodedQuery;
                                    a = $("<a>")
                                        .attr("href", href)
                                        .text(linkText);
                                }

                                var li = $("<li>")
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
