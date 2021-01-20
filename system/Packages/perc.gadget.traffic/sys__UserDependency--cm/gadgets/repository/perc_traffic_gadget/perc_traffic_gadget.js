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
 *  PercTrafficGadget
 *  Graphs Visits and Activity statistics gathered from Google Analytics and local data.
 *  Drill down by selecting a date range in the graph to show details on the activity
 *  such as pages update or created within the selected date range.
 *  @auth Jose Annunziato
 *
 *  +---------------------------------------------------------------+
 *  |   * Loading                                                   |
 *  |   Visits                                          Activity    |
 *  |   +-------------------------------------------------------+   |
 *  |   |   *   |       |       *       |   *   |       |       |   |
 *  |   |*      *       *       |   *   |       |   *   |       |   |
 *  |   |       |   *   |   *   |       *       *       *       |   |
 *  |   |       |       |       |       |       |       |   *   |   |
 *  |   +---+---+---+---+---+---+---+---+---+---+---+---+---+---+   |
 *  |  Nov     Jan     Mar     May     Jun     Aug     Oct     Dec  |
 *  |                                            Show/Hide details  |
 *  |                                                               |
 *  |           Pages updated between <date1> and <date2>           |
 *  |   Page    Change  Views   Template    Modified    Published   |
 *  |   Home1   0       0       Site1       <date>      <date>      |
 *  |   Home2   0       0       Site1       <date>      <date>      |
 *  |   Home3   0       0       Site1       <date>      <date>      |
 *  |                                                               |
 *  |   Showing 1 to 3 of 3 total results   First Prev 1 Next Last  |
 *  +---------------------------------------------------------------+
 */
(function($) {

    // set to true to generate fairly smooth data points for debugging graph layout
    // false to get from server/service
    // this has nothing to do with the fake data service configurable with Traffic.xml
    // configure from URL: ...&fakeData=true&trafficScale=120&...
    // default fakeData=false, trafficScale=30
    var fakeData = window.parent.fakeData ? window.parent.fakeData : false;
    var trafficScale = window.parent.trafficScale ? window.parent.trafficScale : 30;
    
    // grab necessary Perc APIs
    var PercActivityService = percJQuery.PercActivityService;
    var PercServiceUtils    = percJQuery.PercServiceUtils;
    var perc_utils          = percJQuery.perc_utils;
    var perc_paths          = percJQuery.perc_paths;
    
    // constants
    var DEFAULT_TABLE_ERROR_MESSAGE = "No Pages Found";
    var MAX_RANGES = 6;
    var DAYS_IN_WEEK = 7;
    var HOURS_IN_DAY = 24;
    var SECONDS_IN_HOUR = 3600;
    var MILLISECONDS_IN_1_SECOND = 1000;
    var MAX_POINTS_SHOW_MARKER_RIGHT = 50;
    var MAX_POINTS_SHOW_MARKER_LEFT  = 25;
    var VISITS_SERIES=0, UPDATES_SERIES=1, LIVEPAGES_SERIES=2;
    var INITIAL_FRAME_HEIGHT = 320;
    var INITIAL_COLLAPSED_FRAME_HEIGHT = 340;
    var DETAILS_TABLE_ROW_HEIGHT = 47;
    var TOOLTIP_DATE_FORMAT = "MM d, yy";
    var TOOLTIP_DATE_FORMAT_DAY   = "DD, MM d, yy";
    var TOOLTIP_DATE_FORMAT_WEEK  = "M d, yy";
    var TOOLTIP_DATE_FORMAT_MONTH = "MM yy";
    var TOOLTIP_DATE_FORMAT_YEAR  = "yy";
    var TOOLTIP_DATE_FORMAT_DAY_TICK   = "M d yy";
    var TOOLTIP_DATE_FORMAT_WEEK_TICK  = "M d yy";
    var TOOLTIP_DATE_FORMAT_MONTH_TICK = "M yy";
    var TOOLTIP_DATE_FORMAT_YEAR_TICK  = "yy";
    var SIMPLE_DATE_FORMAT  = "m/d/yy";
    var ONLY_MONTH_SHORT;
    
    var TABLE_HEADER_HEIGHT = 30;
    var TABLE_ROW_HEIGHT = 45;
    var TABLE_STATUS_FOOTER_HEIGHT = 45;
    var itemsPerPage = 5;

    // ui components
    var startDateLabel;
    var endDateLabel;
    var hideDetailsLink;
    var detailsPane;
    var parentFrame;
    var table, tableBody;   // datatable and its body
    var tooltipDateRange;
    var graphLabels;
    var chart1, chart2;
    var tooltip = $('<table class="jqplot-highlighter jqplot-highlighter-tooltip" style="display : none">'
                    +   '<tr id="perc-tooltip-daterange">                               <td colspan=2 style="color : blue"  id="perc-tooltip-daterange-value">Sep 1 - Sep 30</td></tr>'
                    +   '<tr id="perc-tooltip-allvisits"    class="perc-tooltip-row">   <td>Page visits:</td>        <td id="perc-tooltip-allvisits-value"      class="perc-tooltip-value">1234</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-uniquevisits" class="perc-tooltip-row">   <td>Unique page visits:</td> <td id="perc-tooltip-uniquevisits-value"   class="perc-tooltip-value">234</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-newpages"     class="perc-tooltip-row">   <td>New pages:</td>          <td id="perc-tooltip-newpages-value"       class="perc-tooltip-value">2345</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-pageupdates"  class="perc-tooltip-row">   <td>Page updates:</td>       <td id="perc-tooltip-pageupdates-value"    class="perc-tooltip-value">3456</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-takedowns"    class="perc-tooltip-row">   <td>Take downs:</td>         <td id="perc-tooltip-takedowns-value"      class="perc-tooltip-value">456</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-totals"       class="perc-tooltip-row">   <td>Total updates:</td>      <td id="perc-tooltip-totals-value"         class="perc-tooltip-value">65</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '<tr id="perc-tooltip-livepages"    class="perc-tooltip-row">   <td>Live pages:</td>         <td id="perc-tooltip-livepages-value"      class="perc-tooltip-value">85</td><!--td class="perc-tooltip-padding">&nbsp;</td--></tr>'
                    +   '</table>');

    // state variables
    var errorMessage = "";
    var showNewPageUpdates, showUpdates, showTakedownUpdates, showLivePages, showUniqueVisits;
    var selectedGranularity;
    var selectedSite;
    var selectedItemsPerPage = 5;
    var selectedUsage;
    var selectedStartDateDrill;
    var selectedEndDateDrill;
    var startArray=new Array();
    var endArray=new Array();
    var selectedStartDate;
    var selectedEndDate;
    var selectedSkip;
    var selectedTicks;
    var isLargeColumn = true;       // if gadget is on the right side (large column)
    var isDetailsVisible = false;   // if the bottom pane is visible
    var isDetailsAvailable = false;
    var expandedFrameHeight = null;
    var trafficTitle;
    var trafficTitleError;

    var detectClick = true;
    var clickOnCanvas = false;
    
    var trafficTable;
    var tableDiv;
    
    // API for this library
    $.fn.PercTrafficGadget = function(site, dateFromStr, dateToStr, granularity, newPages, updates, takeDowns, livePages, usage, rows) {
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");
        
        tableDiv = $(this);
        
        if(site == null)
            site="";
        loadGadget(site, dateFromStr, dateToStr, granularity, newPages, updates, takeDowns, livePages, usage, rows);
    }

    // API for this library
    $.fn.drawDrilDownChart = function() {
        drillDownChart(selectedStartDateDrill, selectedEndDateDrill);

    }
    
    
    function loadGadget(site, dateFromStr, dateToStr, granularity, newPages, updates, takeDowns, livePages, usage, rowsPerPage) {
        
        // initialize state variables
        selectedStartDate = new Date(dateFromStr);
        selectedEndDate   = new Date(dateToStr);
    
        // make sure the dates are in the right order
        if(selectedStartDate > selectedEndDate) {
            var tmp = selectedStartDate;
            selectedStartDate = selectedEndDate;
            selectedEndDate = tmp;
            
            tmp = dateFromStr;
            dateFromStr = dateToStr;
            dateToStr = tmp;
        }

        var site_title = site == "" ? "All Sites" : site; 
        var title = "TRAFFIC: "+dateFromStr+" - " + dateToStr + " (" + site_title + ")";   
        gadgets.window.setTitle(title);

        selectedGranularity = granularity;
        selectedSite = site;
        selectedItemsPerPage = rowsPerPage;
        selectedUsage = usage;
        isLargeColumn = gadgets.window.getDashboardColumn() == 1; // if the gadget is in first column then we have to render it as large 
        showNewPageUpdates = eval(newPages);
        showUpdates = eval(updates);
        showTakedownUpdates = eval(takeDowns);
        showLivePages = eval(livePages);
        if(usage == "uniquepageviews") {
            showUniqueVisits = true;
        }        
        // initialize ui components
        chart1 = $("#chart1");
        chart2 = $("#chart2");  // dummy chart needed to enable date selection. chart1 acts as a date range remote control that zooms on chart2 but we dont need chart2 zooming so we dont render it
        graphLabels = $("#perc-graph-labels");
        parentFrame = $(window.frameElement);
        tooltipDateRange = tooltip.find("#perc-tooltip-daterange-value");
        detailsPane = $("#perc-traffic-details");
        startDateLabel = $("#perc-traffic-start-date");
        endDateLabel   = $("#perc-traffic-end-date");
        trafficTitle   = $("#perc-traffic-title");
        trafficTitleError   = $("#perc-traffic-error-message");
        // gesture control for hiding and showing the details panel at the bottom
        hideDetailsLink = $("#perc-traffic-show-hide-details")
            .hover(function(){
                if(isDetailsAvailable) {
                    $(this).css("cursor","pointer");
                } else {
                    $(this).css("cursor","default");
                }
            })
            .click(function(){
                if(isDetailsVisible) {
                    hideDetailsPane();
                } else {
                    if(isDetailsAvailable) {
                        showDetailsPane();
                    }
                }
            });
        // panel is hidden by default since there is no date range selected from graph
//        hideDetailsPane();
        
        // Pad dates so that x axis date labels are in synch with the data points.
        // Ask for more data points if necessary so that data points spread across whole date range evenly.
        var selectedDateRange = selectedEndDate - selectedStartDate;
        var selectedDataPointCount;
        var additionalDataPoints;
        var dateTo;
        
        // TODO: move these if statements into calculateAdditionalDatapointCount() or separate function to unclutter code this early on.
        // If necessary, move the end date in the selected date range to ask for more data points so they are evenly spaced
        if(selectedGranularity === "DAY") {
            selectedDataPointCount = 1 + Math.floor(selectedDateRange/MILLISECONDS_IN_1_SECOND/SECONDS_IN_HOUR/HOURS_IN_DAY);
            additionalDataPoints = calculateAdditionalDatapointCount(selectedDataPointCount);
            dateTo = selectedEndDate;
            if(additionalDataPoints !== 0)
                dateTo = getDateAfterRange(dateTo, selectedGranularity, additionalDataPoints);
        } else if(selectedGranularity == "WEEK") {
            selectedDataPointCount = 1 + Math.floor(selectedDateRange/MILLISECONDS_IN_1_SECOND/SECONDS_IN_HOUR/HOURS_IN_DAY/DAYS_IN_WEEK);
            additionalDataPoints = calculateAdditionalDatapointCount(selectedDataPointCount);
            dateFromStr = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, getStartOfWeekDate(selectedStartDate)); // I think I already have this as a string
            dateTo = getEndOfWeekDate(selectedEndDate);
            if(additionalDataPoints != 0)
                dateTo = getDateAfterRange(dateTo, selectedGranularity, additionalDataPoints);
        } else if(selectedGranularity == "MONTH") {
            var months = (selectedEndDate.getFullYear() - selectedStartDate.getFullYear()) * 12;
            selectedDataPointCount = 1 + selectedEndDate.getMonth() - selectedStartDate.getMonth() + months;
            additionalDataPoints = calculateAdditionalDatapointCount(selectedDataPointCount);
            dateFromStr = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, getFirstDateInMonth(selectedStartDate));
            dateTo = getFirstDateInMonth(selectedEndDate);
            if(additionalDataPoints != 0)
                dateTo = getDateAfterRange(dateTo, selectedGranularity, additionalDataPoints);
            dateTo = getLastDateInMonth(dateTo);
        } else if(selectedGranularity == "YEAR") {
            selectedDataPointCount = 1 + selectedEndDate.getFullYear() - selectedStartDate.getFullYear();
            additionalDataPoints = calculateAdditionalDatapointCount(selectedDataPointCount);
            dateFromStr = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, getFirstDateInYear(selectedStartDate));
            dateTo = getFirstDateInMonth(selectedEndDate);//getLastDateInYear(selectedEndDate);
            if(additionalDataPoints != 0)
                dateTo = getDateAfterRange(dateTo, selectedGranularity, additionalDataPoints);
            dateTo = getLastDateInYear(dateTo);
        }
        dateToStr = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, dateTo);
        
        // if not fake, get data from server
        if(!fakeData) {
            PercActivityService
                .getContentTraffic(
                    "//Sites/"+site,
                    dateFromStr,
                    dateToStr,
                    selectedGranularity,
                    ["VISITS","NEW_PAGES", "UPDATED_PAGES", "TAKE_DOWNS", "LIVE_PAGES"],
                    usage,
                    function(status, data) {
                        if(status == PercServiceUtils.STATUS_SUCCESS && data.ContentTraffic != "") {
                            draw(data);
                            graphLabels.show();
                            hideDetailsLink.show();
                        } else {
                            if(data.ContentTraffic === "")
                                data = "No data came back from the server.";
                            chart1.html("<div class='perc-gadget-errormessage'>" + data + "</div>");
                            graphLabels.hide();
                            hideDetailsLink.hide();
                            miniMsg.dismissMessage(loadingMsg);
                            busy = false;
                        }
                    }
            );
        } else {
            graphLabels.show();
            // if we want fake data, create several random/smooth data points for the selected date range
            var visits = createRandomSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            var newPages = createRandomSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            var pageUpdates = createRandomSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            takeDowns = createRandomSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            var updateTotals = perc_utils.addArrays(newPages, pageUpdates);
            updateTotals = perc_utils.addArrays(updateTotals, takeDowns);
            var lives = createRandomSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            var dates = createDatesSeriesForDates(dateFromStr, dateToStr, selectedGranularity);
            var data = {ContentTraffic : {startDate : dateFromStr, endDate : dateToStr, dates : dates, visits : visits, newPages : newPages, pageUpdates : pageUpdates, takeDowns : takeDowns, updateTotals : updateTotals, livePages : lives}};
            draw(data);
            miniMsg.dismissMessage(loadingMsg);
        }
    }

    /**
     *  Draws visits, updates and live pages retrieved from server
     *  @param data contains JSON response from server
     */
    function draw(data) {
        
        var startStr, endStr, dates, visits, newPages, pageUpdates, takeDowns, updates, lives;

        // get data from service
        startStr    = data.ContentTraffic.startDate;
        endStr      = data.ContentTraffic.endDate;
        dates       = data.ContentTraffic.dates;
        visits      = data.ContentTraffic.visits;
        newPages    = data.ContentTraffic.newPages;
        pageUpdates = data.ContentTraffic.pageUpdates;
        takeDowns   = data.ContentTraffic.takeDowns;
        updates     = data.ContentTraffic.updateTotals;
        lives       = data.ContentTraffic.livePages;
  
        // make sure that they are arrays
        // the server returns a string if the array is a single element
        if(typeof dates == "string") {
            dates = [dates];
            visits = [visits];
            newPages = [newPages];
            pageUpdates = [pageUpdates];
            takeDowns = [takeDowns];
            updates = [updates];
            lives = [lives];
        }
        
        // Look for the first future date.
        var futureDateIndex = dates.length;
        var now = new Date();
        
        $.each(dates, function (key, value) {
            if ((new Date(value)).getTime() > now.getTime()) {
                futureDateIndex = key;
                return false;
            }
        });
        
        // Remove future data
        visits.splice(futureDateIndex, visits.length - 1);
        newPages.splice(futureDateIndex, newPages.length - 1);
        pageUpdates.splice(futureDateIndex, pageUpdates.length - 1);
        takeDowns.splice(futureDateIndex, takeDowns.length - 1);
        updates.splice(futureDateIndex, updates.length - 1);
        lives.splice(futureDateIndex, lives.length - 1);
        
        startStr = dates[0];
        endStr = dates[dates.length-1];
        
        // add up the datapoints for the updates, new pages, and take downs
        var totalUpdates = perc_utils.newArray(dates.length);
        if(showNewPageUpdates || showTakedownUpdates || showUpdates) {
            if(showUpdates)
                totalUpdates = pageUpdates;
            
            if(showNewPageUpdates)
                totalUpdates   = perc_utils.addArrays(totalUpdates, newPages);
            
            if(showTakedownUpdates)
                totalUpdates   = perc_utils.addArrays(totalUpdates, takeDowns);
        }
        
        // create series arrays in the format expected by jqPlot:
        // series = [[x, y1, y2, y3, y4], [x, y1, y2, y3, y4], ...]
        // where in our case the x are dates and the y are either visits, updates, new pages, take downs or live pages
        // for all series expect for updatesSeries, there is only one y
        // for the updatesSeries, there 4 values of y: total, new pages, updates, and take downs
        // the first y is used to draw the data point, all the y values are later used for creating the tooltip
        // in total there are 3 series that are drawn
        var visitsSeries  = createSeriesForDates(dates, visits);
        var updatesSeries = createSeriesForDates(dates, totalUpdates, newPages, pageUpdates, takeDowns);
        var livesSeries   = createSeriesForDates(dates, lives);

        // if the gadget is on the right or left, we show (or not) the markers (circles) for each data point
        // we show them if the count is less then some threshold
        var showMarkers;
        if(isLargeColumn) {
            showMarkers = visitsSeries.length < MAX_POINTS_SHOW_MARKER_RIGHT;
        } else {
            showMarkers = visitsSeries.length < MAX_POINTS_SHOW_MARKER_LEFT;
        }

        // by default we always show visits graph and only show the other 2 series if they are selected in the config
        var series = [visitsSeries];
        var activitySeries = [];
        if(showNewPageUpdates || showTakedownUpdates || showUpdates) {
            series.push(updatesSeries);
            activitySeries.push(updatesSeries);
        }
        if(!showNewPageUpdates && !showTakedownUpdates && !showUpdates && showLivePages)
            series.push([]);
        if(showLivePages) {
            series.push(livesSeries);
            activitySeries.push(livesSeries);
        }

        // depending on the granularity configure
        //      the format of the x label
        //      how many data points to skip and number of ticks to avoid crowding labels in the x axis
        var tickOptions = {formatString:'%b %#d %Y'};
        var skip = Math.floor(dates.length / 3);
        if(skip == 0)
            skip = 1;
        var tickInterval = '1 day';
        if(selectedGranularity == "DAY") {
            tickInterval = selectedSkip + ' day';
            numberTicks = undefined;
        } else if(selectedGranularity == "WEEK") {
            tickInterval = undefined;
            numberTicks = selectedTicks;
        } else if(selectedGranularity == "MONTH") {
            tickOptions = {formatString:'%b %Y'};
            tickInterval = selectedSkip + ' month';
            numberTicks = undefined;
        } else if(selectedGranularity == "YEAR") {
            tickOptions = {formatString:'%Y'};
            tickInterval = skip + ' year';
            numberTicks = undefined;
        }
        
        // if it's only one data point, we show 3 x ticks so that the single data point shows in the middle
        var min = startStr;
        var max = endStr;
        if(dates.length == 1) {
            min = undefined;
            max = undefined;
            selectedTicks = 3;
        }
        
        // calculate and fix yaxis max and ticks
        var maxAndTicks = calculateMaxAndTicks(activitySeries);
        y2Max = maxAndTicks[0];
        y2Ticks = maxAndTicks[1];
        
        maxAndTicks = calculateMaxAndTicks([visitsSeries]);
        yMax = maxAndTicks[0];
        yTicks = maxAndTicks[1];
        
        // configure jqPlot events to notify various mouse events
        $.jqplot.eventListenerHooks.push(['jqplotMouseMove' , updateTooltip]);
        $.jqplot.eventListenerHooks.push(['jqplotMouseDown' , startDrilldownSelection]);
        $.jqplot.eventListenerHooks.push(['jqplotMouseUp' , setEndDrilldownSelection]);
        $.jqplot.eventListenerHooks.push(['jqplotMouseLeave', hideToolTip]);
        $.jqplot.eventListenerHooks.push(['jqplotDblClick', clearChartSelection]);
        
        // draw series using jqPlot plugin
        $.jqplot.config.enablePlugins = false;
        var mainChart = $.jqplot('chart1', series, {
            title:'',
            axes:{
                xaxis:{
                    renderer:$.jqplot.DateAxisRenderer, 
                    tickOptions : tickOptions,
                    numberTicks : selectedTicks,
                    min : min,
                    max : max
                },
                yaxis:{
                    min : 0,
                    max : yMax,
                    numberTicks : yTicks,
                    tickOptions:{formatString:'%d'}
                },
                y2axis:{
                    min : 0,
                    max : y2Max,
                    numberTicks : y2Ticks,
                    tickOptions:{formatString:'%d', showGridline : false}
                }
            },
            seriesDefaults:{showMarker:showMarkers, lineWidth:4, markerOptions:{size : 10, style:'filledCircle'}},
            seriesColors : ['#4f99bc', '#95c947', '#95c947'],
            series:[
                {yaxis:'yaxis', markerOptions:{color:'#4f99bc'}},
                {yaxis:'y2axis', markerOptions:{color:'#95c947'}},
                {yaxis:'y2axis', markerOptions:{color:'#95c947'}, lineWidth:1, style:'circle'}
            ],
            cursor:{
                showTooltip: false,
                zoom:true,
                show : true,
                constrainZoomTo: 'x'
            }
        });
        
        // this is a dummy chart to enable the zoom selection on the real chart
        // the real chart controls the zooming on this chart but we dont really
        // need the zooming of this chart so it is not rendered. It's here only
        // to enable the selection of the real chart.
        var dummyPlot = $.jqplot('chart2', series, {
            axes:{
                xaxis:{
                    renderer:$.jqplot.DateAxisRenderer, 
                    tickOptions:{formatString:'%b %#d'}
                }
            },
            cursor:{
                showTooltip: false,
                zoom : true,
                show : true,
                constrainZoomTo : 'x'
            }       
        });
        
        // add the ability to select a range to mainChart
        // controls zoom on dummyPlot but we dont show dummyPlot
        // because we dont really want to zoom
        // we just want to select the range and be notified
        // notify endDrilldownSelection() when zoom is occurs
        $.jqplot.Cursor.zoomProxy(dummyPlot, mainChart, endDrilldownSelection);

        // The jqPlot tool has some sort of bug that calculates the x-axis labels wrong.
        // When adding days across months, it sometimes overshoots by a day depending on
        // whether the current month has 30 or 31 days. This function overrides the labels
        // created by the plugin.
        fixXaxis(dates.length);
        
        miniMsg.dismissMessage(loadingMsg);
        busy = false;
    }

    function clearChartSelection() {
        hideDetailsPane();
        isDetailsAvailable = false;
        $("#perc-traffic-show-hide-details").css('display', 'none');
        parentFrame.height(INITIAL_FRAME_HEIGHT);
    }    
    
    function hideToolTip() {
        tooltip.hide();
    }
    
    /**
     *  Recalculates the max of a series and the number of ticks
     *  so that the vertical values in the series are spread out evenly
     *  @param series an array of arrays of data points
     */
    function calculateMaxAndTicks(series) {
        // search for the max across all series
        var max = -1;
        for(i=0; i<series.length; i++) {
            var serie = series[i];
            for(j=0; j<serie.length; j++) {
                var date = serie[j][0];
                var value = serie[j][1];
                if(value > max)
                    max = value;
            }
        }
        if(max == 0)
            return [1, 2];
        // increment by one to account for 0
        max++;
        var skip = Math.ceil(max/6);
        var ranges = Math.ceil((max-1)/skip);
        var numberOfTicks = ranges + 1;
        max = skip * ranges + 1;
        var maxAndTick = [max-1, numberOfTicks];
        return maxAndTick;
    }
    
    /**
     *  Callback when user starts selecting date range.
     *  Called by jQplot when you click on the graph.
     */
    function startDrilldownSelection(ev, gridpos, datapos, neighbor, plot) {
        if(startArray.length==0){
            selectedStartDateDrill = new Date(datapos.xaxis);
            startArray.push(selectedStartDateDrill);
        }
    }

    /**
     *  Callback when user end selecting date range.
     *
     */
    function  setEndDrilldownSelection(ev, gridpos, datapos, neighbor, plot) {
        if(endArray.length==0){
            selectedEndDateDrill = new Date(datapos.xaxis);
            endArray.push(selectedEndDateDrill);
        }
    }
    
    /**
     *  Callback when user completes selecting date range.
     *  Called by jQplot when you mouse up on the graph.
     */
    function endDrilldownSelection(gridpos, datapos, targetPlot, cursor) {
        selectedEndDateDrill = new Date(datapos.xaxis);
        drillDownChart(selectedStartDateDrill, selectedEndDateDrill);
    }
    
    function drillDownChart(selectedStartDateDrill, selectedEndDateDrill){
        // ignore the request if the dates are the same
        if(selectedStartDateDrill.getTime() == selectedEndDateDrill.getTime())
            return;
        
        if(selectedStartDateDrill > selectedEndDateDrill) {
            var tmp = selectedStartDateDrill;
            selectedStartDateDrill = selectedEndDateDrill;
            selectedEndDateDrill = tmp;
        }

        trafficTitle.show();
        trafficTitleError.hide();
        
        if(selectedGranularity == "DAY") {
            if(isSameDay(selectedStartDateDrill, selectedEndDateDrill)) {
                trafficTitle.hide();
                trafficTitleError.show();
            } else {
                selectedStartDateDrill = getStartOfDayDate(selectedStartDateDrill);
                selectedStartDateDrill = getDateAfterRange(selectedStartDateDrill, "DAY", 1);
                selectedEndDateDrill   = getEndOfDayDate(selectedEndDateDrill);
            }
        } else if(selectedGranularity == "WEEK") {
            if(isSameWeek(selectedStartDateDrill, selectedEndDateDrill)) {
                trafficTitle.hide();
                trafficTitleError.show();
            } else {
                selectedStartDateDrill = getStartOfWeekDate(selectedStartDateDrill);
                selectedStartDateDrill = getDateAfterRange(selectedStartDateDrill, "WEEK", 1);
                selectedEndDateDrill   = getEndOfWeekDate(selectedEndDateDrill);
            }
        } else if(selectedGranularity == "MONTH") {
            if(isSameMonth(selectedStartDateDrill, selectedEndDateDrill)) {
                trafficTitle.hide();
                trafficTitleError.show();
            } else {
                selectedStartDateDrill = getFirstDateInMonth(selectedStartDateDrill);
                selectedStartDateDrill = getDateAfterRange(selectedStartDateDrill, "MONTH", 1);
                selectedEndDateDrill   = getLastDateInMonth(selectedEndDateDrill);
            }
        } else if(selectedGranularity == "YEAR") {
            if(isSameYear(selectedStartDateDrill, selectedEndDateDrill)) {
                trafficTitle.hide();
                trafficTitleError.show();
            } else {
                selectedStartDateDrill = getFirstDateInYear(selectedStartDateDrill);
                selectedStartDateDrill = getDateAfterRange(selectedStartDateDrill, "YEAR", 1);
                selectedEndDateDrill   = getLastDateInYear(selectedEndDateDrill);
            }
        }
        
        startDateLabel.html($.datepicker.formatDate(TOOLTIP_DATE_FORMAT, selectedStartDateDrill));
        endDateLabel.html($.datepicker.formatDate(TOOLTIP_DATE_FORMAT, selectedEndDateDrill));

        selectedStartDateDrill = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, selectedStartDateDrill);
        selectedEndDateDrill   = $.datepicker.formatDate(SIMPLE_DATE_FORMAT, selectedEndDateDrill);

        showDetailsPane();
        getActivityForDateRange(selectedSite, selectedStartDateDrill, selectedEndDateDrill, selectedUsage);
        startArray = new Array();
        endArray = new Array();
    }
    
    function isSameDay(date1, date2) {
        return (date1.getFullYear() == date2.getFullYear()) && (date1.getMonth() == date2.getMonth()) && (date1.getDate() == date2.getDate());
    }
    
    function isSameWeek(date1, date2) {
        var startWeek1 = getStartOfWeekDate(date1);
        var startWeek2 = getStartOfWeekDate(date2);
        return isSameDay(startWeek1, startWeek2);
    }
    
    function isSameMonth(date1, date2) {
        return (date1.getFullYear() == date2.getFullYear()) && (date1.getMonth() == date2.getMonth());
    }
    
    function isSameYear(date1, date2) {
        return date1.getFullYear() == date2.getFullYear();
    }
     
    /**
     *  Client request to get the activity for a given range.
     *  Called at the end of a date range selection.
     */
    function getActivityForDateRange(selectedSite, selectedStartDateDrill, selectedEndDateDrill, selectedUsage) {
        if(busy)
            return;
        loadingMsg = miniMsg.createStaticMessage("Loading...");
        busy = true;
        PercActivityService
            .getActivityForDateRange(
                "//Sites/"+selectedSite,
                selectedStartDateDrill,
                selectedEndDateDrill,
                selectedUsage,
                function(status, data){
                    if(status == PercServiceUtils.STATUS_SUCCESS && data != {}) {
                        // noop
                    } else {
                        errorMessage = data;
                    }
                    createItemsTable(data);
                    miniMsg.dismissMessage(loadingMsg);
                    busy = false;
                    isDetailsAvailable = true;
                }
        );
    }

    /**
     *  Displays the details pane by changing the size of the frame and also makes the link blue and changes the label
     */
    function showDetailsPane() {
        $("#perc-traffic-show-hide-details").css('display', 'block');
        hideDetailsLink
            .html("Hide details")
            .css("color", "#0099CC");
        detailsPane.show();
        if(expandedFrameHeight)
            parentFrame.height(expandedFrameHeight);
        isDetailsVisible = true;
    }
    
    /**
     *  Hides the details pane by changing the size of the frame and also changes the label of the link
     */
    function hideDetailsPane() {
        hideDetailsLink.html("Show details");
        if(isDetailsVisible)
            expandedFrameHeight = parentFrame.height();
        detailsPane.hide();
        parentFrame.height(INITIAL_COLLAPSED_FRAME_HEIGHT);
        isDetailsVisible = false;
    }
    
    /**
     *  Displays and updates the tooltip as user hovers over data points.
     *  Called by jQplot with parameters aboute the event.
     */
    function updateTooltip(ev, gridpos, datapos, neighbor, plot) {
        // TODO: move tooltip html to JSP instead of adding it here
        if(neighbor != null) {
            
            // date
            var date  = neighbor.data[0];
            var value = neighbor.data[1];
            
            date = new Date(date);
            var toolTipDateString = "";
            
            if(selectedGranularity == "DAY") {
                toolTipDateString = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_DAY, date);
            } else if(selectedGranularity == "WEEK") {
                toolTipDateString = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_WEEK, date);
                var endOfWeek = getEndOfWeekDate(date);
                toolTipDateString += " - " + $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_WEEK, endOfWeek);
            } else if(selectedGranularity == "MONTH") {
                toolTipDateString = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_MONTH, date);
            } else if(selectedGranularity == "YEAR") {
                toolTipDateString = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_YEAR, date);
            }
            
            tooltipDateRange = tooltip.find("#perc-tooltip-daterange-value");//
            tooltipDateRange.html(toolTipDateString);
            
            tooltip
                .css("background-color", "white")
                .appendTo("body")
                .css("position", "absolute")
                .hide()
                .hover(function(){$(this).hide();});

            var tWidth  = tooltip.width();
            var tHeight = tooltip.height();
            var tTop    = gridpos.y;
            var tLeft   = gridpos.x - tWidth - 10;
            
            var gTop    = plot.eventCanvas.getTop();
            var gLeft   = plot.eventCanvas.getLeft();
            var gWidth  = plot.eventCanvas.getWidth();
            var gHeight = plot.eventCanvas.getHeight();
            
            seriesIndex = neighbor.seriesIndex;
            
            tooltip.find(".perc-tooltip-row").hide();

            // TODO: do these finds() only once and store them in variables
            if(seriesIndex == VISITS_SERIES) {
                // show unique visits or all visits?
                if(showUniqueVisits) {
                    tooltip.find("#perc-tooltip-uniquevisits").show();
                    tooltip.find("#perc-tooltip-uniquevisits-value").html(value);
                } else {
                    tooltip.find("#perc-tooltip-allvisits").show();
                    tooltip.find("#perc-tooltip-allvisits-value").html(value);
                }
            } else if(seriesIndex == UPDATES_SERIES) {
                // if it's the updates series, update the tooltip to show up to 4 values
                var updateTypesCount = 0;
                var total       = neighbor.data[1];
                var newPages    = neighbor.data[2];
                var pageUpdates = neighbor.data[3];
                var takeDowns   = neighbor.data[4];

                if(showNewPageUpdates) {
                    tooltip.find("#perc-tooltip-newpages-value").html(newPages);
                    tooltip.find("#perc-tooltip-newpages").show();
                    updateTypesCount++;
                }
                if(showUpdates) {
                    tooltip.find("#perc-tooltip-pageupdates-value").html(pageUpdates);
                    tooltip.find("#perc-tooltip-pageupdates").show();
                    updateTypesCount++;
                }
                if(showTakedownUpdates) {
                    tooltip.find("#perc-tooltip-takedowns-value").html(takeDowns);
                    tooltip.find("#perc-tooltip-takedowns").show();
                    updateTypesCount++;
                }
                if(updateTypesCount>1) {
                    tooltip.find("#perc-tooltip-totals-value").html(total);
                    tooltip.find("#perc-tooltip-totals").show();
                }
            } else if(seriesIndex == LIVEPAGES_SERIES) {
                if(showLivePages) {
                    tooltip.find("#perc-tooltip-livepages-value").html(value);
                    tooltip.find("#perc-tooltip-livepages").show();
                }
            }
            
            var pointIndex = neighbor.pointIndex;
            var seriesIndex = neighbor.seriesIndex;

            if(tTop < gTop)
                tTop = gTop;
            if(tTop + tHeight > gTop + gHeight)
                tTop = gTop + gHeight - tHeight;
            
            if(tLeft < gLeft)
                tLeft += tWidth + 50;
            
            tTop  += "px";
            tLeft += "px";
            
            tooltip
                .css("top", tTop)
                .css("left", tLeft)
                .show();
        } else {
            tooltip.hide();
        }
    }
    
    var trafficTable;
    /**
     * Iterates over the JSON data and populates the tbody of the table that shows the status of the pages
     */
    function createItemsTable(data) {
        var tableContainer = $("#perc-traffic-table-container");
        
        // if table already exists, destroy it to repopulate it
        if(typeof trafficTable != "undefined") {
            tableContainer.empty();
        }

        var percHeaders = ["Link Text", "Template", "Modified", "Change", "Views"];
        var percData = [];

        // iterate over the data
        var itemProperties = data.TrafficDetails;
        
        for(i in itemProperties) {
            // get each item property
            var property = itemProperties[i];
            
            var summary = "&nbsp;";
            if(property.summary)
                summary = $(property.summary).text();

            var namePath = [{content : property.name, title : property.path},summary];//"<div class='perc-datatable-firstrow perc-ellipsis'>" + property.name + "</div><div class='perc-datatable-secondrow perc-ellipsis'>" + property.path + "</div>";
            
            var templateName  = {content : property.type, title : property.type};//"<div class='data-cell'><div class='top-line perc-ellipsis' title='"+ property.type +"'>"+property.type+"</div></div>";
            
            // initialize strings used to build the table row
            // parse dates and format them
            var lastModifiedDate      = property.lastModifiedDate;  
            var lastModifiedDateDate  = "";
            var lastModifiedDateTime  = "";
            if(lastModifiedDate && lastModifiedDate != "" && lastModifiedDate != undefined) {
                lastModifiedDate = new Date(property.lastModifiedDate);            
                lastModifiedDateDate = $.datepicker.formatDate('M d, yy', lastModifiedDate);
                lastModifiedDateTime = perc_utils.formatTimeFromDate(lastModifiedDate);
            }       
            var lastModifiedDateTimeAndWho = lastModifiedDateTime + " ("+property.lastModifier+")";
            var lastModified  = [lastModifiedDateDate, lastModifiedDateTimeAndWho];//"<div class='data-cell'><div class='top-line perc-ellipsis' title='"+ lastModifiedDateDate  +"'>"+lastModifiedDateDate +"</div><div class='bottom-line perc-ellipsis' title='"+ lastModifiedDateTime  +" ("  + property.lastModifier + ")'> "+ lastModifiedDateTime +" ("+property.lastModifier+")</div></div>";

            // build the row for each page
            var percRow = {rowContent : [namePath, templateName, lastModified, property.visitsDelta, property.visits], rowData : {pageId : property.id, pagePath : property.path}};
            percData.push(percRow);
        }
        var aoColumns = [
                { sType: "string"  },
                { sType: "string"  },
                { sType: "html"    },
                { sType: "numeric" },
                { sType: "numeric" }
            ];
                
        var percVisibleColumns = null;
        if(!isLargeColumn)
            percVisibleColumns = [0,2];
                
        var sZeroRecords = errorMessage == "" ? DEFAULT_TABLE_ERROR_MESSAGE : errorMessage;
        
        errorMessage = "";
                
        trafficTable = $("<div id='perc-traffic-table'>");
        tableContainer.append(trafficTable);

        var percColumnWidths = ["*","100","104","58","48"];
        tableConfig = {percColumnWidths : percColumnWidths, percExpandParentFrameVertically : true, percStayBelow : "#perc-traffic-heading", percHeaders : percHeaders, aoColumns : aoColumns, percData : percData, percVisibleColumns : percVisibleColumns, iDisplayLength : selectedItemsPerPage};
            
        trafficTable.PercPageDataTable(tableConfig);
        
        miniMsg.dismissMessage(loadingMsg);
        
        // fix the height of the gadget frame so that we dont have a scrollbar
        // calculate the height based on the # of rows per page    
        var gadgetTitlebarHeight = percJQuery(".gadgets-gadget-title-bar").height();
        var gadgetTitlebarShadowHeight = percJQuery(".perc-gadget-titlebar-shadow").height();
        var tableHeaderHeight = $("#perc-traffic-table thead tr").height();
        var trafficTable_infoHeight = $("#perc-traffic-table_info").height();
        
        // get the frame and set its height
        var frame = $(window.frameElement);
//        expandedFrameHeight = (INITIAL_FRAME_HEIGHT + tableHeaderHeight + DETAILS_TABLE_ROW_HEIGHT*selectedItemsPerPage + trafficTable_infoHeight + 20) + 40;
        showDetailsPane();
    }
    /**
     * Creates an array of arrays in the format needed for jQplot to graph the data. The format is as follows:
     * [[<x1>,<y1>,<y2>,<y3>,<y4>],[<x2>,<y1>,<y2>,<y3>,<y4>],...]
     * Where in our case x1, x2, are dates, and y1, y2, ... are visits, updates, take downs, etc...
     * [[<date1>,<value1>,<value2>,<value3>,<value4>],[<date2>,<value1>,<value2>,<value3>,<value4>],...]
     * Example:
     * [["4/5/2010",12,23,34,45],["4/6/2010",13,23,34,45],...]
     * jQplot renders a point with the first 2 values of each sub array, e.g., 1st point is ("4/5/2010",12,23) and 2nd point is ("4/6/2010",13)
     * The extra values are useful as satellite data that may want to be displayed in a tooltip. We use this to implement our tooltip
     */
    function createSeriesForDates(dates, values, values2, values3, values4) {
        var series = [];
        // TODO: optimize this so that you dont have the ifs inside the loop always checking the same thing. Maybe have a loop inside several ifs?
        for(d=0; d<dates.length; d++) {
            var date = dates[d];
            var dataPoint = [date, values[d]];
            if(values2)
                dataPoint.push(values2[d]);
            if(values3)
                dataPoint.push(values3[d]);
            if(values4)
                dataPoint.push(values4[d]);
            series.push(dataPoint);
        }
        return series;
    }
        
    /**
     *  The following are utility methods for testing purposes,
     *  for creating test series for given start and end dates and granularity
     */
    var nex1 = 100;
    var nex2 = 100;
    var nex3 = 100;
    var deltaScale = trafficScale;
    var deltaHalf = deltaScale/2;
    
    function next1() {
        var deltaValue = Math.random() * deltaScale - deltaHalf;
        nex1 += deltaValue;
        return Math.abs(nex1);
    }
    var dateFormat = "m/d/yy";
    function createRandomSeriesForDates(startDate, endDate, selectedGranularity) {

        var s = new Date(startDate);

        if(selectedGranularity == "MONTH") {
            s.setDate(1);
        }

        var e = new Date(endDate);
        var d = s;
        var series = [];
        
        while(d <= e) {
            series.push(parseInt(next1()));
            if(selectedGranularity == "DAY") {
                d.setDate(d.getDate()+1);
            } else if(selectedGranularity == "WEEK") {
                d.setDate(d.getDate()+DAYS_IN_WEEK);
            } else if(selectedGranularity == "MONTH") {
                d.setMonth(d.getMonth()+1);
            } else if(selectedGranularity == "YEAR") {
                d.setFullYear(d.getFullYear()+1);
            }
        }
        return series;
    }
    
    function createDatesSeriesForDates(dateFromStr, dateToStr, selectedGranularity) {
        var s = new Date(dateFromStr);
        
        if(selectedGranularity == "MONTH") {
            s.setDate(1);
        }

        var d = s;
        var e = new Date(dateToStr);
        var series = [];
        while(d <= e) {
            var dStr = $.datepicker.formatDate(dateFormat, d);
            series.push(dStr);
            if(selectedGranularity == "DAY") {
                d.setDate(d.getDate()+1);
            } else if(selectedGranularity == "WEEK") {
                d.setDate(d.getDate()+DAYS_IN_WEEK);
            } else if(selectedGranularity == "MONTH") {
                d.setMonth(d.getMonth()+1);
            } else if(selectedGranularity == "YEAR") {
                d.setFullYear(d.getFullYear()+1);
            }
        }
        return series;
    }
    
    function getLastDateInMonth(forDate) {
        var year = forDate.getFullYear();
        var month = forDate.getMonth()+1;
        var dd = new Date(year, month, 0);
        var daysInMonth = dd.getDate();
        dd.setDate(daysInMonth);
        return dd;
    }
    
    function getFirstDateInMonth(forDate) {
        var year = forDate.getFullYear();
        var month = forDate.getMonth();
        var dd = new Date(year, month, 1);
        return dd;
    }

    function getStartOfDayDate(forDate) {
        var toDate = new Date(forDate);
        toDate.setHours(0);
        toDate.setMinutes(0);
        toDate.setSeconds(0);
        return toDate;
    }
    
    function getEndOfDayDate(forDate) {
        var toDate = new Date(forDate);
        toDate.setHours(23);
        toDate.setMinutes(59);
        toDate.setSeconds(59);
        return toDate;
    }
    
    function getStartOfWeekDate(forDate) {
        var toDate = new Date(forDate);
        var dayOfWeek = toDate.getDay();
        if(dayOfWeek == 0)
            return toDate;
        var date = toDate.getDate();
        toDate.setDate(date-dayOfWeek);
        return toDate;
    }
    
    function getEndOfWeekDate(forDate) {
        var toDate = new Date(forDate);
        var dayOfWeek = toDate.getDay();
        if(dayOfWeek == 6)
            return toDate;
        var date = toDate.getDate();
        toDate.setDate(date+6-dayOfWeek);
        return toDate;
    }
    
    function calculateAdditionalDatapointCount(selectedDataPointCount) {
        selectedSkip = Math.ceil(selectedDataPointCount/MAX_RANGES);
        var rangesCount = Math.ceil((selectedDataPointCount - 1)/selectedSkip);
        selectedTicks = rangesCount + 1;
        var calculatedDataPointCount = selectedSkip * rangesCount + 1;
        var additionalDataPointCount = calculatedDataPointCount - selectedDataPointCount;
        return additionalDataPointCount;
    }
    
    /**
     *  The jqPlot tool has some sort of bug that calculates the x-axis labels wrong.
     *  When adding days across months, it sometimes overshoots by a day depending on
     *  whether the current month has 30 or 31 days. This function overrides the labels
     *  created by the plugin.
     */
    function fixXaxis(datesLength) {
        var chart = $("#chart1");                       // get the chart
        var xAxis = chart.find(".jqplot-xaxis");        // get the xAxis
        var ticks = xAxis.find(".jqplot-xaxis-tick");   // get the ticks
        var ticksLength = ticks.length;
        
        var tickCount = 1;
        var tickStartDate;
        var tickDate;
        var tickDateLbl;
        // initialize and fix the first label
        if(selectedGranularity=="DAY"){
            tickDate = selectedStartDate;
            tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_DAY_TICK, tickDate)
        } else if(selectedGranularity=="WEEK"){
            tickDate = getStartOfWeekDate(selectedStartDate);
            tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_WEEK_TICK, tickDate)
        } else if(selectedGranularity=="MONTH"){
            tickDate = getFirstDateInMonth(selectedStartDate);
            tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_MONTH_TICK, tickDate)
        } else if(selectedGranularity=="YEAR"){
            tickDateLbl = selectedStartDate.getFullYear();
        }
        
        if(datesLength==1){
            $(ticks[0]).html("");
            $(ticks[1]).html(tickDateLbl);
            $(ticks[2]).html("");
            return;
        }
        var firstTick = $(ticks[0]);
        firstTick
            .html(tickDateLbl)
            .css("left", "0");

        // increment and fix the rest of the labels
        $.each(ticks, function(){
            var tick = $(ticks[tickCount]);
            tickCount++;
            if(tickCount>ticksLength)
                return 0;
            
            // fix the label
            if(selectedGranularity=="DAY") {
                tickDate.setDate(tickDate.getDate() + selectedSkip);
                tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_DAY_TICK, tickDate)
                tick.html(tickDateLbl);
            } else if(selectedGranularity=="WEEK") {
                tickDate.setDate(tickDate.getDate() + selectedSkip * DAYS_IN_WEEK);
                tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_WEEK_TICK, tickDate)
                tick.html(tickDateLbl);
            } else if(selectedGranularity=="MONTH") {
                tickDate.setMonth(tickDate.getMonth() + selectedSkip);
                tickDateLbl = $.datepicker.formatDate(TOOLTIP_DATE_FORMAT_MONTH_TICK, tickDate)
                tick.html(tickDateLbl);
            } else if(selectedGranularity=="YEAR") {
                tickDateLbl += selectedSkip;
                tick.html(tickDateLbl);
            }
        });
    }
    
    function getDateAfterRange(afterDate, granularity, additionalRangeCount) {
        if(granularity == "DAY") {
            var additionalDays = additionalRangeCount;
            afterDate.setDate(afterDate.getDate() + additionalDays);
        } else if(granularity == "WEEK") {
            var additionalDays = DAYS_IN_WEEK * additionalRangeCount;
            afterDate.setDate(afterDate.getDate() + additionalDays);
        } else if(granularity == "MONTH") {
            var additionalMonths = additionalRangeCount;
            // this does not work all the time
            // for this to work, the date needs to start at 1st of the month
            // if it starts at the end of a particular month, then adding a month could overshoot the resulting month's day's in the month
            // and spill over to the next month
            afterDate.setMonth(afterDate.getMonth() + additionalMonths);
            /*
            // so we might need to do it by hand:
            var day = afterDate.getDate();
            var months = afterDate.getMonth() + additionalMonths + 1;
            var year = afterDate.getFullYear();
            var newDate = months + "/" + day + "/" + year;
            afterDate = new Date(newDate);
            */
        } else if(granularity == "YEAR") {
            var additionalYears = additionalRangeCount;
            afterDate.setFullYear(afterDate.getFullYear() + additionalYears);
        }
        return afterDate;
    }
    
    function getLastDateInYear(forDate) {
        var year = forDate.getFullYear();
        var lastDateInYear = "12/31/" + year;
        return new Date(lastDateInYear);
    }
    
    function getFirstDateInYear(forDate) {
        var year = forDate.getFullYear();
        var firstDateInYear = "1/1/" + year;
        return new Date(firstDateInYear);
    }

    function newMenu(pageId, pagePath) {
        var menu = $("<div>");
        menu.percSimpleMenu({
            callbacks         : [previewPage, openPage],
            menuTitleExpanded : "<img src='../images/images/perc-menu-dropdown-active.png'>",
            menuTitleCollapsed: "<img src='../images/images/perc-menu-dropdown-inactive.png'>",
            menuLabels        : ["Preview", "Open"],
            callbackData      : [pageId, pagePath],
            optionClasses     : ["perc-preview", "perc-open"]
        });
        return menu;
    }
    
    function previewPage(pageId) {
        percJQuery.perc_finder().launchPagePreview(pageId);
    }
    
    function openPage(pagePath) {
        percJQuery.PercNavigationManager.openPage(pagePath);
    }

})(jQuery);
