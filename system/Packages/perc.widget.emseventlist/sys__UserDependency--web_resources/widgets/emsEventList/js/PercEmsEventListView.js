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
**/
(function($)
{
    $(document).ready(function(){
        $.PercEmsEventListView.updatePageLists();
    });
	
    $.PercEmsEventListView = {
        updatePageLists : updatePageLists
    };


    function updatePageLists()
    {
        $(".perc-ems-eventlist-container").each(function()
        {
		    var currentElem = $(this);
            if (currentElem.attr("data-query") === "")
            {
                return true; 
            }
         
            // Sane defaults
            this.settings = {
				eventListTitleTag: "h3",
				showDescription: true,
				showPastEvents: "week",
				showFutureEvents: "week",
				maxEvents: 3,
				showLocation: true,
				showEventDate: true,
				showEventTime: true,
                eventDateFormat: "dddd, MMMM Do YYYY", 
				eventTimeFormat: 'h:mmA',
				event2412Format: 'hh:mmt',
                eventDateLocale: 'en-us',
                enableCalenderIcon: false,
				listTitle: "",
				hideEventListOverride: false,
				eventListLimitOverride: 0,
                query: {}
            };

            var callbackOptions = {};
            
            
            $.extend(this.settings, $.parseJSON($(this).attr('data-options')));
			$.extend(this.settings.query, $.parseJSON($(this).attr('data-query')));
			var rootElem = $("<div/>");
			
			if(this.settings.eventListLimitOverride > 0 && (this.settings.eventListLimitOverride != this.settings.maxEvents)){
			this.settings.maxEvents = this.settings.eventListLimitOverride;
			}
		
			
			$.PercEmsEventListService.getPageEntries(this.settings, function(status, result, settings )
            {
                if (result && result.length)
                {
					var titleElem = createTitleHtml(settings);
					var titleId = titleElem.attr("id");		
                    rootElem.append(titleElem);
					
					var listContainer = $("<div role='navigation' aria-labelledby='" + titleId + "'>").addClass("perc-emseventlist-list-container");
					
                    var listElem = $("<ul>").addClass("perc-list-main perc-emsevent-list");
					var count = 1;
					$.each(result, function(index, entry){
                        if(count<=settings.maxEvents){
							listElem.append(createEntryHtml(settings, entry));
							count++;
						}else{
							return false;
						}
						
                    });
					listContainer.append(listElem);
                    rootElem.append(listContainer);
                    currentElem.append(rootElem);
                }
            });
			
        });
    } // end updatePageLists
	
	function createTitleHtml(settings)
    {
        var title = "";
		var titleId = "emseventlist-title-" + Math.floor(Math.random() * 100);
        var elemName = settings.eventListTitleTag || "h2";
        if (settings.listTitle) {
            title = $("<div id='" + titleId + "'>")
                    .append($("<" + elemName + "/>")
                    		.addClass("perc-emseventlist-title")
                            .text( settings.listTitle)
                    ); 
        }
        return title;
    }
   
    /**
    * Create the page item html from the pageData returned from
    * the server.
	Sample JSON:
[{"eventDetailID":1375,"eventID":148,"title":"TRIO Upward Bound Fall Academies","description":"","location":"Lacorte Hall LaCorte Hall A221","locationUrl":null,"cancelled":false,"noEndTime":false,"priority":2,"eventDate":"2018-10-13T00:00:00","eventStartTime":"2018-10-13T08:30:00","eventEndTime":null,"isAllDayEvent":false,"isTimedEvent":true,"eventTypeId":15,"eventTypeName":"Meeting","contactName":null,"contactEmail":"upwardbound@csudh.edu","isReOccuring":true,"isOnMultipleCalendars":false,"bookingID":65285,"reservationID":18932,"connectorID":0,"hideContactName":true,"hideContactEmail":null,"hideContactPhone":null,"customLabelField1":null,"customFieldDescription1":null,"customUrl1":null,"customLabelField2":null,"customFieldDescription2":null,"customUrl2":null,"eventUpdatedBy":"Connector Service","eventUpdatedDate":"2018-09-27T18:08:17.98","eventDetailUpdatedBy":"dbo","eventDetailUpdatedDate":"2018-09-13T09:25:32.2"},{"eventDetailID":1376,"eventID":148,"title":"TRIO Upward Bound Fall Academies","description":"","location":"Lacorte Hall LaCorte Hall A230","locationUrl":null,"cancelled":false,"noEndTime":false,"priority":2,"eventDate":"2018-10-13T00:00:00","eventStartTime":"2018-10-13T08:30:00","eventEndTime":null,"isAllDayEvent":false,"isTimedEvent":true,"eventTypeId":15,"eventTypeName":"Meeting","contactName":null,"contactEmail":"upwardbound@csudh.edu","isReOccuring":true,"isOnMultipleCalendars":false,"bookingID":65286,"reservationID":18932,"connectorID":0,"hideContactName":true,"hideContactEmail":null,"hideContactPhone":null,"customLabelField1":null,"customFieldDescription1":null,"customUrl1":null,"customLabelField2":null,"customFieldDescription2":null,"customUrl2":null,"eventUpdatedBy":"Connector Service","eventUpdatedDate":"2018-09-27T18:08:17.98","eventDetailUpdatedBy":"dbo","eventDetailUpdatedDate":"2018-09-13T09:25:32.2"},{"eventDetailID":1377,"eventID":148,"title":"TRIO Upward Bound Fall Academies","description":"","location":"Lacorte Hall LaCorte Hall A227","locationUrl":null,"cancelled":false,"noEndTime":false,"priority":2,"eventDate":"2018-10-13T00:00:00","eventStartTime":"2018-10-13T08:30:00","eventEndTime":null,"isAllDayEvent":false,"isTimedEvent":true,"eventTypeId":15,"eventTypeName":"Meeting","contactName":null,"contactEmail":"upwardbound@csudh.edu","isReOccuring":true,"isOnMultipleCalendars":false,"bookingID":65287,"reservationID":18932,"connectorID":0,"hideContactName":true,"hideContactEmail":null,"hideContactPhone":null,"customLabelField1":null,"customFieldDescription1":null,"customUrl1":null,"customLabelField2":null,"customFieldDescription2":null,"customUrl2":null,"eventUpdatedBy":"Connector Service","eventUpdatedDate":"2018-09-27T18:08:17.98","eventDetailUpdatedBy":"dbo","eventDetailUpdatedDate":"2018-09-13T09:25:32.2"},{"eventDetailID":1378,"eventID":148,"title":"TRIO Upward Bound Fall Academies","description":"","location":"Lacorte Hall LaCorte Hall A229","locationUrl":null,"cancelled":false,"noEndTime":false,"priority":2,"eventDate":"2018-10-13T00:00:00","eventStartTime":"2018-10-13T08:30:00","eventEndTime":null,"isAllDayEvent":false,"isTimedEvent":true,"eventTypeId":15,"eventTypeName":"Meeting","contactName":null,"contactEmail":"upwardbound@csudh.edu","isReOccuring":true,"isOnMultipleCalendars":false,"bookingID":65288,"reservationID":18932,"connectorID":0,"hideContactName":true,"hideContactEmail":null,"hideContactPhone":null,"customLabelField1":null,"customFieldDescription1":null,"customUrl1":null,"customLabelField2":null,"customFieldDescription2":null,"customUrl2":null,"eventUpdatedBy":"Connector Service","eventUpdatedDate":"2018-09-27T18:08:17.98","eventDetailUpdatedBy":"dbo","eventDetailUpdatedDate":"2018-09-13T09:25:32.2"},{"eventDetailID":1379,"eventID":148,"title":"TRIO Upward Bound Fall Academies Wknd","description":"","location":"James L. Welch Hall Welch Hall 400","locationUrl":null,"cancelled":false,"noEndTime":false,"priority":2,"eventDate":"2018-10-13T00:00:00","eventStartTime":"2018-10-13T08:30:00","eventEndTime":null,"isAllDayEvent":false,"isTimedEvent":true,"eventTypeId":15,"eventTypeName":"Meeting","contactName":null,"contactEmail":"upwardbound@csudh.edu","isReOccuring":true,"isOnMultipleCalendars":false,"bookingID":65303,"reservationID":18932,"connectorID":0,"hideContactName":true,"hideContactEmail":null,"hideContactPhone":null,"customLabelField1":null,"customFieldDescription1":null,"customUrl1":null,"customLabelField2":null,"customFieldDescription2":null,"customUrl2":null,"eventUpdatedBy":"Connector Service","eventUpdatedDate":"2018-09-27T18:08:17.98","eventDetailUpdatedBy":"dbo","eventDetailUpdatedDate":"2018-09-13T09:25:32.2"}]
    */                               
    function createEntryHtml(settings, entry)
    {
		
		var eventName = entry.title;
		var eventDate = entry.eventDate;
		var eventTime = entry.eventStartTime;
        var locale = settings.eventDateLocale;
		var eventEndTime =  entry.eventEndTime;
		var eventIsTimedEvent = entry.isTimedEvent;
		var eventType = entry.eventTypeName;
        var dateFormat = settings.eventDateFormat;
		var eventId = entry.eventDetailID;
		var description = entry.description;
		var eventLoc = entry.location;
        var isAllDayEvent = entry.isAllDayEvent;
		
var dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
var calOptions = {month:'short'};
var dEventDate  = new Date(eventDate);
var timeOptions = { hour: 'numeric', minute: 'numeric'}		
		
		
		listItem = $("<li>").addClass("ui-perc-list-element").append('<a href="' + settings.baseEmsEventLink + eventId + '" title="' + eventName + '" target="_blank" class="perc-emseventlist-eventtitle">' + eventName + '</a>');
        
		if(settings.enableCalenderIcon){
			listItem.append('<div class="perc-emseventlist-calicon"> <div class="perc-emseventlist-calicon-month">'+ dEventDate.toLocaleDateString(settings.locale, calOptions) + '</div><div class="perc-emseventlist-calicon-day">' +dEventDate.getDate() + '</div></div>');
		}
		
		if(settings.showDescription){
			listItem.append('<div class="perc-emseventlist-description-container">' + description + '</div>');
		}
		if(settings.showEventDate){
			listItem.append('<div class="perc-emseventlist-date-container">' + dEventDate.toLocaleDateString(settings.locale, dateOptions) + '</div>');	
		}
		
		if(settings.showEventTime){
			listItem.append('<div class="perc-emseventlist-time-container">' + dEventDate.toLocaleTimeString(settings.locale, timeOptions) + '</div>');
		}
		if(settings.showLocation){
			listItem.append('<div class="perc-emseventlist-location-container">' + eventLoc+ '</div>'); 
		}
		
        return listItem;
    }
})(jQuery);
