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

/* JDL Note (Percussion)

The arrays provided in this file will need to be generated by Rhythmyx.  The rest
of the JavaScript in this file will be static and can sit somewhere on the filesystem.
The arrays (as well as month_name and month_max_days will be generated by Rhythmyx
and published out in a separate file, or in the page that uses the calendar

*/

//******************************************************
//********************************************************************
//Rythmyx needs to set the Month name and max num of days
//********************************************************************
//******************************************************
var events = new Array( );

//********************************************************************
//********************************************************************
//Rythmyx needs to loop through events and populate the events array
//********************************************************************
//********************************************************************
//First remove test data
//events[events.length] = {day:"2", time:"1:00", title:"Grand Rounds: Neurogenetics update", url:"http://my.yahoo.com"};
//events[events.length] = {day:"2", time:"6:00", title:"Gold with the Boss", url:"http://www.golfing.com"};
//events[events.length] = {day:"14", time:"4:00", title:"Staff Meeting", url:"http://www.msn.com"};
//*******************
// END LOOP
//*******************

function moveToNewDay(for_day) {
	var the_back_html = "&nbsp;";
	var the_next_html = "&nbsp;";
	var back_one_day =  0;
	var forward_one_day = 0;


	if ( for_day > 1 ) {
		back_one_day = for_day - 1;
		the_back_html = "<b><a href='#' class='normal_lnk' onClick='displayEvents(" + back_one_day + ");'>&lt;&lt;</a></b>";
	}
	
	if ( for_day < month_max_days ) {
		forward_one_day = for_day + 1;
		the_next_html = "<b><a href='#' class='normal_lnk' onClick='displayEvents(" + forward_one_day + ");'>&gt;&gt;</a></b>";
	}
	
	var the_html = "<table width='100%' border='' cellspacing='0' cellpadding='0'><tr><td height='3px'></td></tr>";
	the_html += "<tr><td class='normal_lnks' align='center' nowrap width='100%'>" + the_back_html;
	the_html += "&nbsp; &nbsp; <b>" + month_name + " " + for_day + "</b> &nbsp; &nbsp; " + the_next_html + "</td></tr></table>";
}




function displayEvents(for_day) {
	var the_new_html = "<ul>"
	var counter = 0;
	var day_link = for_day + "_link";
	
	moveToNewDay(for_day);

	the_new_html = "<table width='90%' border='0' cellspacing='0' cellpadding='0' id='monthevents' style='display:block'>";
        the_new_html += "<tr><td colspan='3' height='5px'></td></tr>";

	for (var i = 0; i < events.length; i++ ) {
		if ( events[i].day == for_day ) {
			the_new_html += "<tr valign='top'>" ;
			the_new_html += "<td width='10' height='20'>&nbsp;</td>";
			// JL: Increased width to 50 to account for longer times (e.g., "12:00pm")
			the_new_html += "<td width='50' align='center' class='months_news' height='20'>" + events[i].time + "</td>";
			the_new_html += "<td class='months_news' height='20'><a href='" + events[i].url + "' class='months_newslnks'>" + events[i].title + "</a></td>";
			the_new_html += "</tr>";
			the_new_html += "<tr>";
			the_new_html += "<td colspan='3' height='2px'></td>";
			the_new_html += "</tr>";

			counter += 1;
		}
	}
	
	
	if ( counter > 0 ) {
		document.getElementById("event_area").innerHTML = the_new_html + "</table>";
	}
	else {
		document.getElementById("event_area").innerHTML = the_new_html + "<tr valign='top'><td width='20' height='20'>&nbsp;</td><td class='months_news' colspan=2>No events for today.&nbsp;</td></tr></table>";
	}
	
	//hilite_date(for_day, day_link);
}


function hilite_date(activedate,activelink)  {
	if (activedate == lastactive) {
		lastactive = activedate;
		lastactivelink = activelink;
	}
	else {
		document.getElementById(activedate).className = "seldate";
		document.getElementById(lastactive).className = "unseldate";
		document.getElementById(activelink).className = "seldate_link";
		document.getElementById(lastactivelink).className = "unseldate_link";
		lastactive = activedate;
		lastactivelink = activelink;
	}
}
