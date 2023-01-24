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
            
            
            $.extend(this.settings, JSON.parse($(this).attr('data-options')));
			$.extend(this.settings.query, JSON.parse($(this).attr('data-query')));
			var rootElem = $("<div/>");
			
			if(this.settings.eventListLimitOverride > 0 && (this.settings.eventListLimitOverride !== this.settings.maxEvents)){
			this.settings.maxEvents = this.settings.eventListLimitOverride;
			}
		
			
			$.PercEmsEventListService.getPageEntries(this.settings, function(status, result, settings )
            {

				if('object' !== typeof(result)){
					result = JSON.parse(result);
				}

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
		
		
		listItem = $("<li>").addClass("ui-perc-list-element").append('<a href="' + settings.baseEmsEventLink + eventId + '" title="' + eventName + '" target="_blank" rel = "noopener noreferrer" class="perc-emseventlist-eventtitle">' + eventName + '</a>');
        
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
