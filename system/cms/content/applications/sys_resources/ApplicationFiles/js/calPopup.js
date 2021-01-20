//
// JavaScript Calendar Component
// Author: Robert W. Husted  (robert.husted@iname.com)
// Date:   8/22/1999
// Modified Date: 11/30/1999
// Modified By:   Robert W. Husted
// Notes:  Added frameset support (changed reference for "newWin" to "top.newWin")
//         Also changed Spanish "March" from "Marcha" to "Marzo"
//         Fixed JavaScript Date Anomaly affecting days > 28
// 
// 
// 
// Usage:  Add the following lines of code to your page to enable the Calendar
//         component.
//
//
//         // THIS LINE LOADS THE JS LIBRARY FOR THE CALENDAR COMPONENT 
//
//         <SCRIPT LANGUAGE="JavaScript" SRC="calendar.js"></SCRIPT>
//
//
//
//         // THIS LINE IS USED IN CONJUNCTION WITH A FORM FIELD (myDateField) IN A FORM (myForm).
//         // Replace "myForm" and "myDateField" WITH THE NAME OF YOUR FORM AND INPUT FIELD RESPECTIVELY
//         // WINDOW OPTIONS SET THE WIDTH, HEIGHT, AND X/Y POSITION OF THE CALENDAR WINDOW    
//         // WITH TITLEBAR ON, ALL OTHER OPTIONS (TOOLBARS, ETC) ARE DISABLED BY DEFAULT
//
//         <A HREF="javascript:doNothing()" onClick="setDateField(document.myForm.myDateField);top.newWin = window.open('calendar.html','cal','dependent=yes,width=210,height=230,screenX=200,screenY=300,titlebar=yes')">
//         <IMG SRC="calendar.gif" BORDER=0></A><font size=1>Popup Calendar</font>
//
//
// 
// Required Files:
//
//         calendar.js   - contains all JavaScript functions to make the calendar work
//
//         calendar.html - frameset document (not required if you call the showCalendar()
//                         function.  However, calling showCalendar() directly causes
//                         the Java Virtual Machine (JVM) to start which slows down the
//                         loading of the calendar.)
//
// 
// Files Generally Included:
//
//         calendar.gif  - image that looks like a little calendar
// 
//         yourPage.html - page that contains a form and a date field which implements 
//                         the calendar component
// 
// Modified by Percussion Software 2001


// BEGIN USER-EDITABLE SECTION -----------------------------------------------------



// SPECIFY DATE FORMAT RETURNED BY THIS CALENDAR
// (THIS IS ALSO THE DATE FORMAT RECOGNIZED BY THIS CALENDAR)

// DATE FORMAT OPTIONS:
//
// dd   = 1 or 2-digit Day
// DD   = 2-digit Day
// mm   = 1 or 2-digit Month
// MM   = 2-digit Month
// yy   = 2-digit Year
// YY   = 4-digit Year
// yyyy = 4-digit Year
// month   = Month name in lowercase letters
// Month   = Month name in initial caps
// MONTH   = Month name in captital letters
// mon     = 3-letter month abbreviation in lowercase letters
// Mon     = 3-letter month abbreviation in initial caps
// MON     = 3-letter month abbreviation in uppercase letters
// weekday = name of week in lowercase letters
// Weekday = name of week in initial caps
// WEEKDAY = name of week in uppercase letters
// wkdy    = 3-letter weekday abbreviation in lowercase letters
// Wkdy    = 3-letter weekday abbreviation in initial caps
// WKDY    = 3-letter weekday abbreviation in uppercase letters
//
// Examples:
//
// calDateFormat = "mm/dd/yy";
// calDateFormat = "Weekday, Month dd, yyyy";
// calDateFormat = "wkdy, mon dd, yyyy";
// calDateFormat = "DD.MM.YY";     // FORMAT UNSUPPORTED BY JAVASCRIPT -- REQUIRES CUSTOM PARSING
//

calDateFormat    = "yyyy-MM-DD";


//Set the time flag to 0
var timeflag =0

var tDoc = null;
var bDoc = null;

//Set the time flag to 0
var appendflag =0

// CALENDAR COLORS
topBackground    = "#bec5e7";         // BG COLOR OF THE TOP FRAME
bottomBackground = "#bec5e7";         // BG COLOR OF THE BOTTOM FRAME
tableBGColor     = "black";         // BG COLOR OF THE BOTTOM FRAME'S TABLE
cellColor        = "#ffffff";     // TABLE CELL BG COLOR OF THE DATE CELLS IN THE BOTTOM FRAME
headingCellColor = "#d1d6f0";         // TABLE CELL BG COLOR OF THE WEEKDAY ABBREVIATIONS
headingTextColor = "#646464";         // TEXT COLOR OF THE WEEKDAY ABBREVIATIONS
dateColor        = "#646464";          // TEXT COLOR OF THE LISTED DATES (1-28+)
focusColor       = "#ff0000";       // TEXT COLOR OF THE SELECTED DATE (OR CURRENT DATE)
hoverColor       = "darkred";       // TEXT COLOR OF A LINK WHEN YOU HOVER OVER IT
fontStyle        = "10px verdana, arial, helvetica, sans-serif";           // TEXT STYLE FOR DATES
headingFontStyle = "bold 10px verdana, arial, helvetica, sans-serif";      // TEXT STYLE FOR WEEKDAY ABBREVIATIONS

// FORMATTING PREFERENCES
bottomBorder  = false;        // TRUE/FALSE (WHETHER TO DISPLAY BOTTOM CALENDAR BORDER)
tableBorder   = 0;            // SIZE OF CALENDAR TABLE BORDER (BOTTOM FRAME) 0=none

// END USER-EDITABLE SECTION -------------------------------------------------------

// browser sniffer code, partially from
// http://www.mozilla.org/docs/web-developer/sniffer/browser_type.html
//
var agt = navigator.userAgent.toLowerCase();
var is_major = parseInt(navigator.appVersion);
var is_minor = parseFloat(navigator.appVersion);

var is_win = ((agt.indexOf("win") != -1) || (agt.indexOf("16bit") != -1));
var is_mac = (agt.indexOf("mac")!= -1);

var is_safari = (is_mac && (agt.indexOf("safari") != -1));
var is_ie = ((agt.indexOf("msie") != -1) && (agt.indexOf("opera") == -1));
var is_nav  = ((agt.indexOf('mozilla') != -1) && (agt.indexOf('spoofer') == -1)
            && (agt.indexOf('compatible') == -1) && (agt.indexOf('opera') == -1)
            && (agt.indexOf('webtv') == -1) && (agt.indexOf('hotjava') == -1) && !is_safari);
var is_mozilla = ((agt.indexOf('mozilla') != -1) && (agt.indexOf('gecko') != -1));
var is_nav4 = (is_nav && (is_major == 4));




// GET CURRENTLY SELECTED LANGUAGE
selectedLanguage = navigator.language;

// PRE-BUILD PORTIONS OF THE CALENDAR WHEN THIS JS LIBRARY LOADS INTO THE BROWSER
buildCalParts();



// CALENDAR FUNCTIONS BEGIN HERE ---------------------------------------------------

// SET THE INITIAL VALUE OF THE GLOBAL DATE FIELD
function setDateField(dateField) {

    // ASSIGN THE INCOMING FIELD OBJECT TO A GLOBAL VARIABLE
    calDateField = dateField;

    // GET THE VALUE OF THE INCOMING FIELD
    inDate = dateField.value;

    // SET calDate TO THE DATE IN THE INCOMING FIELD OR DEFAULT TO TODAY'S DATE
    setInitialDate();

    // THE CALENDAR FRAMESET DOCUMENTS ARE CREATED BY JAVASCRIPT FUNCTIONS
    calDocTop    = buildTopCalFrame();
    calDocBottom = buildBottomCalFrame();
}


// SET THE INITIAL CALENDAR DATE TO TODAY OR TO THE EXISTING VALUE IN dateField
function setInitialDate() {
   
    // CREATE A NEW DATE OBJECT (WILL GENERALLY PARSE CORRECT DATE EXCEPT WHEN "." IS USED AS A DELIMITER)
    // (THIS ROUTINE DOES *NOT* CATCH ALL DATE FORMATS, IF YOU NEED TO PARSE A CUSTOM DATE FORMAT, DO IT HERE)
    calDate = new Date(inDate);

    // IF THE INCOMING DATE IS INVALID, USE THE CURRENT DATE
    if (isNaN(calDate) || inDate == "" || inDate == null) {

        // ADD CUSTOM DATE PARSING HERE
        // IF IT FAILS, SIMPLY CREATE A NEW DATE OBJECT WHICH DEFAULTS TO THE CURRENT DATE
        calDate = new Date();
    }

    // KEEP TRACK OF THE CURRENT DAY VALUE
    calDay  = calDate.getDate();

    // SET DAY VALUE TO 1... TO AVOID JAVASCRIPT DATE CALCULATION ANOMALIES
    // (IF THE MONTH CHANGES TO FEB AND THE DAY IS 30, THE MONTH WOULD CHANGE TO MARCH
    //  AND THE DAY WOULD CHANGE TO 2.  SETTING THE DAY TO 1 WILL PREVENT THAT)
    calDate.setDate(1);
}


// POPUP A WINDOW WITH THE CALENDAR IN IT
function showCalendar(dateField,timevar,appendvar) {
	timeflag = timevar;

   	if(appendvar != null)
		appendflag = appendvar;
     
    var winPrefs = "dependent=yes,width=200,height=235,titlebar=yes"; // Set this with a function based on user agent in the future
    if(is_safari || is_mozilla)
    {
       winPrefs = "dependent=yes,width=220,height=235,titlebar=yes";
    }
    calDateField = dateField;

    // GET THE VALUE OF THE INCOMING FIELD
    inDate = dateField.value;

    // SET calDate TO THE DATE IN THE INCOMING FIELD OR DEFAULT TO TODAY'S DATE
    setInitialDate();

    // THE CALENDAR FRAMESET DOCUMENTS ARE CREATED BY JAVASCRIPT FUNCTIONS
    calDocTop    = buildTopCalFrame();
    calDocBottom = buildBottomCalFrame();

    // SET INITIAL VALUE OF THE DATE FIELD AND CREATE TOP AND BOTTOM FRAMES
    //setDateField(dateField);

    // USE THE JAVASCRIPT-GENERATED DOCUMENTS (calDocTop, calDocBottom) IN THE FRAMESET
    var rowWidth = 70;
	if(timeflag=="1")
		rowWidth = 90;
	calDocFrameset = 
        "<html><head><title>Rhythmyx Calendar Picker</title>\n" +
		"<link type=\"text/css\" src=\"../sys_resources/css/templates.css\"></head>\n" +
        "<frameset rows=\"" + rowWidth + ",*\" frameborder=\"0\">\n" +
        "  <frame name=\"topCalFrame\" src='javascript:parent.opener.calDocTop' scrolling=\"no\">\n" +
        "  <frame name=\"bottomCalFrame\" src='javascript:parent.opener.calDocBottom' scrolling=\"no\">\n" +
        "</frameset>\n";

    // DISPLAY THE CALENDAR IN A NEW POPUP WINDOW
    if(is_safari)
    {
       // if we don't have one, creating a new one, otherwise use it
       if (top.newWin == null || top.newWin.closed)
    	   top.newWin = window.open("", "cal", winPrefs);
       tDoc = top.newWin.document;
       bDoc = tDoc;

       calDoc = 
          "<html><head><title>Rhythmyx Calendar Picker</title>\n" +
          "<link type=\"text/css\" src=\"../sys_resources/css/templates.css\">\n" +
          calStyleSheet + "</head>\n" +
          "<body bgcolor=\""+ topBackground +"\">\n" +
          "  <div id=\"calTop\" style=\"background-color:"+ topBackground +"\">" + buildTopCalFrame() + "<div>\n" 
          + calendarBegin + calendarEnd +
          "</body>\n";
       tDoc.open();
       tDoc.write(calDoc);
       tDoc.close();
       buildBottomCalDOM();
                     
       top.newWin.focus();
    }
    else
    {
       // if we don't have one, creating a new one, otherwise use it
       if (top.newWin == null || top.newWin.closed)
           top.newWin = window.open("javascript:parent.opener.calDocFrameset", "cal", winPrefs);
           
       // First write the frameset
       top.newWin.document.open();
       top.newWin.document.write(calDocFrameset);
       top.newWin.document.close();
       
       tDoc = top.newWin.frames['topCalFrame'].document;
       bDoc = top.newWin.frames['bottomCalFrame'].document;    
       
       tDoc.open();
       tDoc.write(calDocTop);
       tDoc.close();
       bDoc.open();
       bDoc.write(calDocBottom);
       bDoc.close();
       top.newWin.focus();
    }
}

// CREATE THE TOP CALENDAR FRAME
function buildTopCalFrame() {

    
    // CREATE THE TOP FRAME OF THE CALENDAR
    var calDoc = "";
    var _parent = "";
    if(!is_safari)
    {
       _parent = "parent.";
       calDoc += "<html>" +
       "<head>" +
       "</head>" +
       "<body bgcolor=\"" + topBackground + "\">";
    }   
        
    calDoc += "<form name=\"calControl\" onSubmit=\"return false;\">" +
       "<center>" +
       "<table cellpadding=0 cellspacing=1 border=0>" +
       "<tr><td colspan=7>" +
       "<center>" +
       getMonthSelect() +
       "<input name=\"year\" value=\"" + calDate.getFullYear() + "\"type=text size=4 maxlength=4 onChange=\"" + _parent + "opener.setYear()\">" +
       "</center>" +
       "</td>" +
       "</tr>" +
       "<tr>" +
       "<td colspan=7>" +
       "<input " +
       "type=button name=\"previousYear\" value=\"<<\"    onClick=\"" + _parent + "opener.setPreviousYear()\"/><input " +
       "type=button name=\"previousMonth\" value=\" < \"   onClick=\"" + _parent + "opener.setPreviousMonth()\"/><input " +
       "type=button name=\"today\" value=\"Today\" onClick=\"" + _parent + "opener.setToday()\"/><input " +
       "type=button name=\"nextMonth\" value=\" > \"   onClick=\"" + _parent + "opener.setNextMonth()\"/><input " +
       "type=button name=\"nextYear\" value=\">>\"    onClick=\"" + _parent + "opener.setNextYear()\"/>&nbsp;" +
       "</td>" +
       "</tr>" ;
    if(timeflag=="1")
    {
	calDoc +=
    	   "<tr>" +
	   "<td colspan=7>" +
	   "<center>" +
	   getTimeSelect() +
	   "</center>" +
	   "</td>" +
	   "</tr>";
    }
    calDoc +=
       "</table>" +
       "</center>" +
       "</form>";
        
    if(!is_safari)
    {
       calDoc += "</body>" +
          "</html>";
    } 

    return calDoc;
}



// CREATE THE BOTTOM CALENDAR FRAME 
// (THE MONTHLY CALENDAR)
function buildBottomCalFrame() {       

    // START CALENDAR DOCUMENT
    var calDoc = calendarBegin;

    // GET MONTH, AND YEAR FROM GLOBAL CALENDAR DATE
    month   = calDate.getMonth();
    year    = calDate.getFullYear();


    // GET GLOBALLY-TRACKED DAY VALUE (PREVENTS JAVASCRIPT DATE ANOMALIES)
    day     = calDay;

    var i   = 0;

    // DETERMINE THE NUMBER OF DAYS IN THE CURRENT MONTH
    var days = getDaysInMonth();

    // IF GLOBAL DAY VALUE IS > THAN DAYS IN MONTH, HIGHLIGHT LAST DAY IN MONTH
    if (day > days) {
        day = days;
    }

    // DETERMINE WHAT DAY OF THE WEEK THE CALENDAR STARTS ON
    var firstOfMonth = new Date (year, month, 1);

    // GET THE DAY OF THE WEEK THE FIRST DAY OF THE MONTH FALLS ON
    var startingPos  = firstOfMonth.getDay();
    days += startingPos;

    // KEEP TRACK OF THE COLUMNS, START A NEW ROW AFTER EVERY 7 COLUMNS
    var columnCount = 0;

    // MAKE BEGINNING NON-DATE CELLS BLANK
    for (i = 0; i < startingPos; i++) {

        calDoc += blankCell;
	columnCount++;
    }

    // SET VALUES FOR DAYS OF THE MONTH
    var currentDay = 0;
    var dayType    = "weekday";

    // DATE CELLS CONTAIN A NUMBER
    for (i = startingPos; i < days; i++) {

	var paddingChar = "&nbsp;";

        // ADJUST SPACING SO THAT ALL LINKS HAVE RELATIVELY EQUAL WIDTHS
        if (i-startingPos+1 < 10) {
            padding = "&nbsp;&nbsp;";
        }
        else {
            padding = "&nbsp;";
        }

        // GET THE DAY CURRENTLY BEING WRITTEN
        currentDay = i-startingPos+1;

        // SET THE TYPE OF DAY, THE focusDay GENERALLY APPEARS AS A DIFFERENT COLOR
        if (currentDay == (day+300)) {// Make this work correctly with added month and year logic
            dayType = "focusDay";
        }
        else {
            dayType = "weekDay";
        }

        // ADD THE DAY TO THE CALENDAR STRING
        calDoc += "<td align=\"center\" bgcolor=\"" + cellColor + "\">" +
                 "<a class=\"" + dayType + "\" href=\"javascript:parent.opener.returnDate(" + 
                 currentDay + ")\">" + padding + currentDay + paddingChar + "</a></td>";

        columnCount++;

        // START A NEW ROW WHEN NECESSARY
        if (columnCount % 7 == 0) {
            calDoc += "</tr><tr>";
        }
    }

    // MAKE REMAINING NON-DATE CELLS BLANK
    for (i=days; i<42; i++)  {

        calDoc += blankCell;
	columnCount++;

        // START A NEW ROW WHEN NECESSARY
        if (columnCount % 7 == 0) {
            calDoc += "</tr>";
            if (i<41) {
                calDoc += "<tr>";
            }
        }
    }

    // FINISH THE NEW CALENDAR PAGE
    calDoc += calendarEnd;

    // RETURN THE COMPLETED CALENDAR PAGE
    return calDoc;
}

// CREATE THE BOTTOM CALENDAR FRAME 
// (THE MONTHLY CALENDAR)
function buildBottomCalDOM() {       

    var tbodyEl = tDoc.getElementById("calTableBody");
    
    cleanTableDOM(tbodyEl);
   
    var rowEl = tDoc.createElement("tr");
    tbodyEl.appendChild(rowEl);
    
    // GET MONTH, AND YEAR FROM GLOBAL CALENDAR DATE
    month   = calDate.getMonth();
    year    = calDate.getFullYear();


    // GET GLOBALLY-TRACKED DAY VALUE (PREVENTS JAVASCRIPT DATE ANOMALIES)
    day     = calDay;

    var i   = 0;

    // DETERMINE THE NUMBER OF DAYS IN THE CURRENT MONTH
    var days = getDaysInMonth();

    // IF GLOBAL DAY VALUE IS > THAN DAYS IN MONTH, HIGHLIGHT LAST DAY IN MONTH
    if (day > days) {
        day = days;
    }

    // DETERMINE WHAT DAY OF THE WEEK THE CALENDAR STARTS ON
    var firstOfMonth = new Date (year, month, 1);

    // GET THE DAY OF THE WEEK THE FIRST DAY OF THE MONTH FALLS ON
    var startingPos  = firstOfMonth.getDay();
    days += startingPos;

    // KEEP TRACK OF THE COLUMNS, START A NEW ROW AFTER EVERY 7 COLUMNS
    var columnCount = 0;

    // MAKE BEGINNING NON-DATE CELLS BLANK
    for (i = 0; i < startingPos; i++) {

        colEl = tDoc.createElement("td");
	colEl.setAttribute("align", "center");
	colEl.setAttribute("bgcolor", cellColor);
	rowEl.appendChild(colEl);
	textNode = tDoc.createTextNode("   ");
        colEl.appendChild(textNode);
	columnCount++;
    }

    // SET VALUES FOR DAYS OF THE MONTH
    var currentDay = 0;
    var dayType    = "weekday";
    var colEl = null;
    var linkEl = null;
    var textNode = null;

    // DATE CELLS CONTAIN A NUMBER
    for (i = startingPos; i < days; i++) {

	var paddingChar = " ";

        // ADJUST SPACING SO THAT ALL LINKS HAVE RELATIVELY EQUAL WIDTHS
        if (i-startingPos+1 < 10) {
            padding = "  ";
        }
        else {
            padding = " ";
        }

        // GET THE DAY CURRENTLY BEING WRITTEN
        currentDay = i-startingPos+1;

        // SET THE TYPE OF DAY, THE focusDay GENERALLY APPEARS AS A DIFFERENT COLOR
        if (currentDay == (day+300)) {// Make this work correctly with added month and year logic
            dayType = "focusDay";
        }
        else {
            dayType = "weekDay";
        }

        // ADD THE DAY TO THE CALENDAR STRING
        colEl = tDoc.createElement("td");
        colEl.setAttribute("align", "center");
        colEl.setAttribute("bgcolor", cellColor);
        rowEl.appendChild(colEl);
               
        
        linkEl = tDoc.createElement("a");
        linkEl.setAttribute("class", dayType);
        linkEl.setAttribute("href", "javascript:parent.opener.returnDate(" + currentDay + ")");
        colEl.appendChild(linkEl);
        
        textNode = tDoc.createTextNode(padding + currentDay + paddingChar);
        linkEl.appendChild(textNode);
        

        columnCount++;

        // START A NEW ROW WHEN NECESSARY
        if (columnCount % 7 == 0)
        {
            rowEl = tDoc.createElement("tr");
            tbodyEl.appendChild(rowEl);
        }
    }

    // MAKE REMAINING NON-DATE CELLS BLANK
    for (i=days; i<42; i++)
    {
       
       colEl = tDoc.createElement("td");
       colEl.setAttribute("align", "center");
       colEl.setAttribute("bgcolor", cellColor);
       rowEl.appendChild(colEl);
       textNode = tDoc.createTextNode("   ");
       colEl.appendChild(textNode);
       columnCount++;

       // START A NEW ROW WHEN NECESSARY
       if (columnCount % 7 == 0)
       {
           rowEl = tDoc.createElement("tr");
           tbodyEl.appendChild(rowEl);
            
       }
    }
    
}

function cleanTableDOM(tbodyEl)
{
   var rows = tbodyEl.getElementsByTagName("tr");      
   
   for(i = 1; i < rows.length; i++)
   {
     tbodyEl.removeChild(rows[i]);
     i--;
   }
   

}





// WRITE THE MONTHLY CALENDAR TO THE BOTTOM CALENDAR FRAME
function writeCalendar() {

    if(is_safari)
    {
       buildBottomCalDOM();    
    }
    else
    {
       
       // WRITE THE NEW CALENDAR TO THE BOTTOM FRAME
       bDoc.open();
       bDoc.write(buildBottomCalFrame());
       bDoc.close();
    }   
}


// SET THE CALENDAR TO TODAY'S DATE AND DISPLAY THE NEW CALENDAR
function setToday() {

    
    // SET GLOBAL DATE TO TODAY'S DATE
    calDate = new Date();

    // SET DAY MONTH AND YEAR TO TODAY'S DATE
    var month = calDate.getMonth();
    var year  = calDate.getFullYear();

    // SET MONTH IN DROP-DOWN LIST
    tDoc.calControl.month.selectedIndex = month;

    // SET YEAR VALUE
    tDoc.calControl.year.value = year;

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
}


// SET THE GLOBAL DATE TO THE NEWLY ENTERED YEAR AND REDRAW THE CALENDAR
function setYear() {

    // GET THE NEW YEAR VALUE
    var year  = tDoc.calControl.year.value;

    // IF IT'S A FOUR-DIGIT YEAR THEN CHANGE THE CALENDAR
    if (isFourDigitYear(year)) {
        calDate.setFullYear(year);

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
    }
    else {
        // HIGHLIGHT THE YEAR IF THE YEAR IS NOT FOUR DIGITS IN LENGTH
        tDoc.calControl.year.focus();
        tDoc.calControl.year.select();
    }
}


// SET THE GLOBAL DATE TO THE SELECTED MONTH AND REDRAW THE CALENDAR
function setCurrentMonth() {

    // GET THE NEWLY SELECTED MONTH AND CHANGE THE CALENDAR ACCORDINGLY
    var month = tDoc.calControl.month.selectedIndex;

    calDate.setMonth(month);

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
}


// SET THE GLOBAL DATE TO THE PREVIOUS YEAR AND REDRAW THE CALENDAR
function setPreviousYear() {

    var year  = tDoc.calControl.year.value;

    if (isFourDigitYear(year) && year > 1000) {
        year--;
        calDate.setFullYear(year);
        tDoc.calControl.year.value = year;

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
    }
}


// SET THE GLOBAL DATE TO THE PREVIOUS MONTH AND REDRAW THE CALENDAR
function setPreviousMonth() {

    var year  = tDoc.calControl.year.value;
    if (isFourDigitYear(year)) {
        var month = tDoc.calControl.month.selectedIndex;

        // IF MONTH IS JANUARY, SET MONTH TO DECEMBER AND DECREMENT THE YEAR
        if (month == 0) {
            month = 11;
            if (year > 1000) {
                year--;
                calDate.setFullYear(year);
                tDoc.calControl.year.value = year;
            }
        }
        else {
            month--;
        }
        calDate.setMonth(month);
        tDoc.calControl.month.selectedIndex = month;

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
    }
}


// SET THE GLOBAL DATE TO THE NEXT MONTH AND REDRAW THE CALENDAR
function setNextMonth() {

    var year = tDoc.calControl.year.value;

    if (isFourDigitYear(year)) {
        var month = tDoc.calControl.month.selectedIndex;

        // IF MONTH IS DECEMBER, SET MONTH TO JANUARY AND INCREMENT THE YEAR
        if (month == 11) {
            month = 0;
            year++;
            calDate.setFullYear(year);
            tDoc.calControl.year.value = year;
        }
        else {
            month++;
        }
        calDate.setMonth(month);
        tDoc.calControl.month.selectedIndex = month;

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
    }
}


// SET THE GLOBAL DATE TO THE NEXT YEAR AND REDRAW THE CALENDAR
function setNextYear() {

    var year  = tDoc.calControl.year.value;
    if (isFourDigitYear(year)) {
        year++;
        calDate.setFullYear(year);
        tDoc.calControl.year.value = year;

	// GENERATE THE CALENDAR SRC
	calDocBottom = buildBottomCalFrame();

    // DISPLAY THE NEW CALENDAR
    writeCalendar(calDocBottom);
    }
}


// GET NUMBER OF DAYS IN MONTH
function getDaysInMonth()  {

    var days;
    var month = calDate.getMonth()+1;
    var year  = calDate.getFullYear();

    // RETURN 31 DAYS
    if (month==1 || month==3 || month==5 || month==7 || month==8 ||
        month==10 || month==12)  {
        days=31;
    }
    // RETURN 30 DAYS
    else if (month==4 || month==6 || month==9 || month==11) {
        days=30;
    }
    // RETURN 29 DAYS
    else if (month==2)  {
        if (isLeapYear(year)) {
            days=29;
        }
        // RETURN 28 DAYS
        else {
            days=28;
        }
    }
    return (days);
}


// CHECK TO SEE IF YEAR IS A LEAP YEAR
function isLeapYear (Year) {

    if (((Year % 4)==0) && ((Year % 100)!=0) || ((Year % 400)==0)) {
        return (true);
    }
    else {
        return (false);
    }
}


// ENSURE THAT THE YEAR IS FOUR DIGITS IN LENGTH
function isFourDigitYear(year) {

    if (year.length != 4) {
        tDoc.calControl.year.value = calDate.getFullYear();
        tDoc.calControl.year.select();
        tDoc.calControl.year.focus();
    }
    else {
        return true;
    }
}


// BUILD THE MONTH SELECT LIST
function getMonthSelect() {

    // BROWSER LANGUAGE CHECK DONE PREVIOUSLY (navigator.language())
    // FIRST TWO CHARACTERS OF LANGUAGE STRING SPECIFIES THE LANGUAGE
    // (THE LAST THREE OPTIONAL CHARACTERS SPECIFY THE LANGUAGE SUBTYPE)
    // SET THE NAMES OF THE MONTH TO THE PROPER LANGUAGE (DEFAULT TO ENGLISH)

    // IF FRENCH
    if (selectedLanguage == "fr") {
        monthArray = new Array('Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
                               'Juillet', 'Aout', 'Septembre', 'Octobre', 'Novembre', 'Décembre');
    }
    // IF GERMAN
    else if (selectedLanguage == "de") {
        monthArray = new Array('Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                               'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember');
    }
    // IF SPANISH
    else if (selectedLanguage == "es") {
        monthArray = new Array('Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                               'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre');
    }
    // DEFAULT TO ENGLISH
    else {
        monthArray = new Array('January', 'February', 'March', 'April', 'May', 'June',
                               'July', 'August', 'September', 'October', 'November', 'December');
    }

    // DETERMINE MONTH TO SET AS DEFAULT
    var activeMonth = calDate.getMonth();

    // START HTML SELECT LIST ELEMENT
    monthSelect = "<select name=\"month\" onChange=\"parent.opener.setCurrentMonth()\">";

    // LOOP THROUGH MONTH ARRAY
    for (i=0;i<monthArray.length;i++) {
        
        // SHOW THE CORRECT MONTH IN THE SELECT LIST
        if (i == activeMonth) {
            monthSelect += "<option selected>" + monthArray[i] + "</option>\n";
        }
        else {
            monthSelect += "<option>" + monthArray[i] + "</option>\n";
        }
    }
    monthSelect += "</select>";

    // RETURN A STRING VALUE WHICH CONTAINS A SELECT LIST OF ALL 12 MONTHS
    return monthSelect;
}

function getTimeSelect() 
{

TimeArray = new Array('12am','1am', '2am', '3am', '4am', '5am', '6am',
                               '7am', '8am', '9am', '10am', '11am', '12pm',
							   '1pm', '2pm', '3pm', '4pm', '5pm', '6pm',
                               '7pm', '8pm', '9pm', '10pm', '11pm' );

ValueTimeArray = new Array('24:00:00','1:00:00', '2:00:00', '3:00:00', '4:00:00', '5:00:00', '6:00:00',
                               '7:00:00', '8:00:00', '9:00:00', '10:00:00', '11:00:00', '12:00:00',
							   '13:00:00', '14:00:00', '15:00:00', '16:00:00', '17:00:00', '18:00:00',
                               '19:00:00', '20:00:00', '21:00:00', '22:00:00', '23:00:00' );

 TimeSelect = "<select  name=\"time\">"
 TimeSelect += "<option selected value = >" + "Set Time" + "</option>\n";
	for (i=0;i<TimeArray.length;i++) {
        TimeSelect += "<option value = " + ValueTimeArray[i] + ">" + TimeArray[i] + "</option>\n";
    }
    TimeSelect += "</select>";

   
    return TimeSelect;
}



// SET DAYS OF THE WEEK DEPENDING ON LANGUAGE
function createWeekdayList() {

    // IF FRENCH
    if (selectedLanguage == "fr") {
        weekdayList  = new Array('Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi');
        weekdayArray = new Array('Di', 'Lu', 'Ma', 'Me', 'Je', 'Ve', 'Sa');
    }
    // IF GERMAN
    else if (selectedLanguage == "de") {
        weekdayList  = new Array('Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag');
        weekdayArray = new Array('So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa');
    }
    // IF SPANISH
    else if (selectedLanguage == "es") {
        weekdayList  = new Array('Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado')
        weekdayArray = new Array('Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa');
    }
    else {
        weekdayList  = new Array('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday');
        weekdayArray = new Array('Su','Mo','Tu','We','Th','Fr','Sa');
    }

    // START HTML TO HOLD WEEKDAY NAMES IN TABLE FORMAT
    var weekdays = "<tr bgcolor=\"" + headingCellColor + "\">";

    // LOOP THROUGH WEEKDAY ARRAY
    for (i=0;i<weekdayArray.length;i++) {

        weekdays += "<td width=\"20\" class=\"heading\" align=\"center\">" + weekdayArray[i] + "</td>";
    }
    weekdays += "</tr>";

    // RETURN TABLE ROW OF WEEKDAY ABBREVIATIONS TO DISPLAY ABOVE THE CALENDAR
    return weekdays;
}


// PRE-BUILD PORTIONS OF THE CALENDAR (FOR PERFORMANCE REASONS)
function buildCalParts() {

    calendarBegin = "";
    
    // GENERATE WEEKDAY HEADERS FOR THE CALENDAR
    weekdays = createWeekdayList();

    // BUILD THE BLANK CELL ROWS
    blankCell = "<td align=\"center\" bgcolor=\"" + cellColor + "\">&nbsp;&nbsp;&nbsp;</td>";

    // BUILD THE TOP PORTION OF THE CALENDAR PAGE USING CSS TO CONTROL SOME DISPLAY ELEMENTS
    if(!is_safari)
    {
       calendarBegin =
           "<html>" +
           "<head>";
    }
    // STYLESHEET DEFINES APPEARANCE OF CALENDAR
    calStyleSheet = "<style type=\"text/css\">" +
        "<!--" +
        "td.heading { text-decoration: none; color:" + headingTextColor + "; font: " + headingFontStyle + "; }" +
        "a.focusDay:link { color: " + focusColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.focusDay:hover { color: " + focusColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.focusDay:visited { color: " + focusColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.focusDay:active { color: " + focusColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.weekday:link { color: " + dateColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.weekday:hover { color: " + hoverColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.weekday:visited { color: " + hoverColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "a.weekday:active { color: " + hoverColor + "; text-decoration: none; font: " + fontStyle + "; }" +
        "-->" +
        "</style>";
        
     if(!is_safari)
     {
        calendarBegin += calStyleSheet + "</head>" +
           "<body bgcolor=\"" + bottomBackground + "\">" +
           "<center>";
     }     

        // NAVIGATOR NEEDS A TABLE CONTAINER TO DISPLAY THE TABLE OUTLINES PROPERLY
        if (is_nav) {
            calendarBegin += 
                "<table cellpadding=\"0\" cellspacing=\"1\" border=\"" + tableBorder + "\" align=\"center\" bgcolor=\"" + tableBGColor + "\"><tr class=\"headercell\"><td class=\"headercellfont\">";
        }

        // BUILD WEEKDAY HEADINGS
        calendarBegin +=
            "<table cellpadding=\"0\" cellspacing=\"1\" border=\"" + tableBorder + "\" align=\"center\" bgcolor=\"" + tableBGColor + "\">" +
            "<tbody id=\"calTableBody\">" +
            weekdays;
        
        if(!is_safari)
        {
           calendarBegin += "<tr>";
        }   


    // BUILD THE BOTTOM PORTION OF THE CALENDAR PAGE
    calendarEnd = "";

        // WHETHER OR NOT TO DISPLAY A THICK LINE BELOW THE CALENDAR
        if (bottomBorder) {
            calendarEnd += "<tr></tr>";
        }

        // NAVIGATOR NEEDS A TABLE CONTAINER TO DISPLAY THE BORDERS PROPERLY
        if (is_nav) {
            calendarEnd += "</td></tr></table>";
        }

        // END THE TABLE AND HTML DOCUMENT
        calendarEnd +=
            "</tbody></table>" +
            "</center>" +
            "</body>" +
            "</html>";
}


// REPLACE ALL INSTANCES OF find WITH replace
// inString: the string you want to convert
// find:     the value to search for
// replace:  the value to substitute
//
// usage:    jsReplace(inString, find, replace);
// example:  jsReplace("To be or not to be", "be", "ski");
//           result: "To ski or not to ski"
//
function jsReplace(inString, find, replace) {

    var outString = "";

    if (!inString) {
        return "";
    }

    // REPLACE ALL INSTANCES OF find WITH replace
    if (inString.indexOf(find) != -1) {
        // SEPARATE THE STRING INTO AN ARRAY OF STRINGS USING THE VALUE IN find
        t = inString.split(find);

        // JOIN ALL ELEMENTS OF THE ARRAY, SEPARATED BY THE VALUE IN replace
		
        return (t.join(replace));
    }
    else {
        return inString;
    }
}


// JAVASCRIPT FUNCTION -- DOES NOTHING (USED FOR THE HREF IN THE CALENDAR CALL)
function doNothing() {
}


// ENSURE THAT VALUE IS TWO DIGITS IN LENGTH
function makeTwoDigit(inValue) {

    var numVal = parseInt(inValue, 10);

    // VALUE IS LESS THAN TWO DIGITS IN LENGTH
    if (numVal < 10) {

        // ADD A LEADING ZERO TO THE VALUE AND RETURN IT
        return("0" + numVal);
    }
    else {
        return numVal;
    }
}


// SET FIELD VALUE TO THE DATE SELECTED AND CLOSE THE CALENDAR WINDOW
function returnDate(inDay)
{

    // inDay = THE DAY THE USER CLICKED ON
    calDate.setDate(inDay);

    // SET THE DATE RETURNED TO THE USER
    var day           = calDate.getDate();
    var month         = calDate.getMonth()+1;
    var year          = calDate.getFullYear();
    var monthString   = monthArray[calDate.getMonth()];
    var monthAbbrev   = monthString.substring(0,3);
    var weekday       = weekdayList[calDate.getDay()];
    var weekdayAbbrev = weekday.substring(0,3);
    var time
    outDate = calDateFormat;

    // RETURN TWO DIGIT DAY
    if (calDateFormat.indexOf("DD") != -1) {
        day = makeTwoDigit(day);
        outDate = jsReplace(outDate, "DD", day);
    }
    // RETURN ONE OR TWO DIGIT DAY
    else if (calDateFormat.indexOf("dd") != -1) {
        outDate = jsReplace(outDate, "dd", day);
    }

    // RETURN TWO DIGIT MONTH
    if (calDateFormat.indexOf("MM") != -1) {
	   
        month = makeTwoDigit(month);
        outDate = jsReplace(outDate, "MM", month);
    }
    // RETURN ONE OR TWO DIGIT MONTH
    else if (calDateFormat.indexOf("mm") != -1) {
        outDate = jsReplace(outDate, "mm", month);
    }

    // RETURN FOUR-DIGIT YEAR
    if (calDateFormat.indexOf("yyyy") != -1) {
        outDate = jsReplace(outDate, "yyyy", year);
    }
    // RETURN TWO-DIGIT YEAR
    else if (calDateFormat.indexOf("yy") != -1) {
        var yearString = "" + year;
        var yearString = yearString.substring(2,4);
        outDate = jsReplace(outDate, "yy", yearString);
    }
    // RETURN FOUR-DIGIT YEAR
    else if (calDateFormat.indexOf("YY") != -1) {
        outDate = jsReplace(outDate, "YY", year);
    }

    // RETURN DAY OF MONTH (Initial Caps)
    if (calDateFormat.indexOf("Month") != -1) {
        outDate = jsReplace(outDate, "Month", monthString);
    }
    // RETURN DAY OF MONTH (lowercase letters)
    else if (calDateFormat.indexOf("month") != -1) {
        outDate = jsReplace(outDate, "month", monthString.toLowerCase());
    }
    // RETURN DAY OF MONTH (UPPERCASE LETTERS)
    else if (calDateFormat.indexOf("MONTH") != -1) {
        outDate = jsReplace(outDate, "MONTH", monthString.toUpperCase());
    }

    // RETURN DAY OF MONTH 3-DAY ABBREVIATION (Initial Caps)
    if (calDateFormat.indexOf("Mon") != -1) {
        outDate = jsReplace(outDate, "Mon", monthAbbrev);
    }
    // RETURN DAY OF MONTH 3-DAY ABBREVIATION (lowercase letters)
    else if (calDateFormat.indexOf("mon") != -1) {
        outDate = jsReplace(outDate, "mon", monthAbbrev.toLowerCase());
    }
    // RETURN DAY OF MONTH 3-DAY ABBREVIATION (UPPERCASE LETTERS)
    else if (calDateFormat.indexOf("MON") != -1) {
        outDate = jsReplace(outDate, "MON", monthAbbrev.toUpperCase());
    }

    // RETURN WEEKDAY (Initial Caps)
    if (calDateFormat.indexOf("Weekday") != -1) {
        outDate = jsReplace(outDate, "Weekday", weekday);
    }
    // RETURN WEEKDAY (lowercase letters)
    else if (calDateFormat.indexOf("weekday") != -1) {
        outDate = jsReplace(outDate, "weekday", weekday.toLowerCase());
    }
    // RETURN WEEKDAY (UPPERCASE LETTERS)
    else if (calDateFormat.indexOf("WEEKDAY") != -1) {
        outDate = jsReplace(outDate, "WEEKDAY", weekday.toUpperCase());
    }

    // RETURN WEEKDAY 3-DAY ABBREVIATION (Initial Caps)
    if (calDateFormat.indexOf("Wkdy") != -1) {
        outDate = jsReplace(outDate, "Wkdy", weekdayAbbrev);
    }
    // RETURN WEEKDAY 3-DAY ABBREVIATION (lowercase letters)
    else if (calDateFormat.indexOf("wkdy") != -1) {
        outDate = jsReplace(outDate, "wkdy", weekdayAbbrev.toLowerCase());
    }
    // RETURN WEEKDAY 3-DAY ABBREVIATION (UPPERCASE LETTERS)
    else if (calDateFormat.indexOf("WKDY") != -1) {
        outDate = jsReplace(outDate, "WKDY", weekdayAbbrev.toUpperCase());
    }

    // SET THE VALUE OF THE FIELD THAT WAS PASSED TO THE CALENDAR
	if(timeflag=="1")
	{
		time = tDoc.calControl.time.value;
		outDate+=" " + time;
	}
	if(appendflag == "1")
	    calDateField.value += outDate;
	else
	    calDateField.value = outDate;


    // GIVE FOCUS BACK TO THE DATE FIELD
    calDateField.focus();

    // CLOSE THE CALENDAR WINDOW
    top.newWin.close()
}
