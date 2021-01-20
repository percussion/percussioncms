/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

// _dfsv(): Date Field Set Value function.  
function _dfsv(
  dateField,
  newValue
  )
{
  // Make sure we have valid values
  if ((dateField == (void 0)) || (newValue == (void 0)))
    return;
    
  // adjust day light saving, if any
  newValue += _getDayLightSavOffset(newValue);

  // Add back in the time
  // offset,since we don't want to overwrite the user's time when 
  // they pick a new date from the calendar. 
  newValue += _getTimePortion(dateField);
  
  // compare the time zone that is on the client with the time zone that
  // came from the localeContext on the server and adjust if necessary.
  // bug 3167883
  newValue += _getLocaleTimeZoneDifference();

  var newDate = new Date(newValue);

  //
  // get the format to use to format the result
  //
  var format = _getDateFieldFormat(dateField);
    
  // update the contents of the text field
  var oldValue = dateField.value;
  var newValue = format.getAsString(newDate);

  if (dateField.value != newValue)
  {
    // bug 2275982
    // trigger onchange programatically
    if (dateField.onchange != (void 0))
    {
      // The IE event delivery mechanism for built-in events like onchange
      // automatically sets the window.event property for inspection by
      // event handlers.
      // When we synthesize this onchange event here, we must ensure that
      // window.event is as close as possible to the value of window.event
      // that would be visible to the onchange handler if it had been triggered 
      // by user interaction, like moving focus away from the field.
      // Updating window.event directly triggers a JS error in IE, so we must
      // use the built-in event delivery mechanism instead.
      // Here, we attach a handler to the IE-specific propertychange event,
      // which will fire when any property is changed, not just the value.
      // In our handler, we only respond to a change in the value property,
      // by detaching the handler and delivering the onchange event.
      // Now, the value for window.event during the onchange handler execution
      // is the property change event but it has the right values for source,
      // target, etc.
      if (_agent.isIE)
      {
        // attach the value change listener
        dateField.onpropertychange = function()
        {
          var event = window.event;
          if (event.propertyName == 'value')
          {
            // detach the value change listener
            dateField.onpropertychange = function(){};
              
            dateField.onchange(event);
          }
        }

        dateField.value = newValue;
      }
      else
      {
        dateField.value = newValue;

        var event = new Object();
        event.type = 'change';
        event.target = dateField;
        dateField.onchange(event);
      }
    }
    else // no onchange handler
    {
      dateField.value = newValue;
    }
  }

  dateField.select();
  dateField.focus();
}

/**
 * The long 'date' value rendered by the server doesn't have the client's 
 * daylight saving information. It is just wrt the current timeZone offset 
 * of the client. So add the daylight saving offset before calculating the 
 * actual date.
 */
function _getDayLightSavOffset(newValue)
{
  var currDate = new Date();
  var dstDate = new Date(newValue);
  
  var dlsOffset = dstDate.getTimezoneOffset() - currDate.getTimezoneOffset();
  return (dlsOffset * 60 * 1000);
}

function _returnCalendarValue(
  closingWindow,
  event
  )
{
  // extract the return value
  var newValue = closingWindow.returnValue;
  
  if (newValue != (void 0))
  {
    var dateField = closingWindow._dateField; 
    // See below!
    if (dateField == (void 0))
    {
      dateField = _savedField1879034;
    }

    _dfsv(dateField, newValue);
  }
}

function _returnPopupCalendarValue(
  props,
  value
  )
{
  // Callback method registered with the popup
  // 'props' contains the name of the target form & field to populate
  if (value != (void 0))
  {
    var formName = props['formName'];
    var fieldName = props['fieldName'];
    var dateField = document.forms[formName][fieldName];
    _dfsv(dateField, value);
  }
}


/**
 * Private function for launching the date picker
 */
function _ldp(
  formName,
  nameInForm,
  usePopup,
  minValue,
  maxValue,
  destination
  )
{
  var dateField = document.forms[formName][nameInForm];
  var oldValue = _dfgv(dateField);

  if (!oldValue)
  {
    // if the parse failed, then create a new Date object.
    oldValue = new Date();
  }

  // default the destination to the calendar dialog destination
  if (!destination)
  {
    destination = _jspDir + _getQuerySeparator(_jspDir);

    if (usePopup)
      //don't use frame redirect
      destination += "_t=cd";
    else
      destination += "_t=fred&_red=cd";
  }
  else
  {
    // since we need to redirect, replace the last portion of the URL with
    // the redirect JSP
    var endOfUrl = destination.lastIndexOf('?');
    var urlArgs  = "";
  
    if (endOfUrl == -1)
    {
      endOfUrl = destination.length;
    }
    else
    {
      urlArgs = destination.substr(endOfUrl + 1);
    }

    var startOfLastPart = destination.lastIndexOf('/', endOfUrl);
  
    var newDest = destination.substring(0, startOfLastPart + 1);
    newDest += _jspDir + _getQuerySeparator(_jspDir);
    newDest += urlArgs;

    // add the correct first param separator
    newDest += _getQuerySeparator(newDest);
    newDest += "_t=fred";

    var redirect = destination.substring(startOfLastPart + 1, endOfUrl);

    destination = newDest;

    // add in the redirect
    destination += "&redirect=" + escape(redirect);
  }
  
  // add the current time in Millis since 1970
  destination += "&value=" + oldValue.getTime();
  
  // add the locale
  destination += "&loc=" + _locale;
    
  // and the character set encoding
  if (window["_enc"])
  {
    destination += "&enc=" + _enc;
  }

  //
  // add on min and max value attributes
  //
  if (minValue != (void 0))
  {
    destination += "&minValue=" + minValue; 
  }
  
  if (maxValue != (void 0))
  {
    destination += "&maxValue=" + maxValue; 
  }

  if (usePopup)
  {
    // Open the dialog passing callback details
    TrPopupDialog._launchDialog(
      destination,
      {},
      _returnPopupCalendarValue,
      { 'formName':formName, 'fieldName':nameInForm });
  }
  else
  {
    // Open the window;  we used to name it "calendar", but
    // that's a common enough name that we hit bug 2807778
    var calWindow = openWindow(self,
                               destination,
                               'uix_2807778',
                               {width:350, height:370},
                               true,
                               void 0,
                               _returnCalendarValue);
    
    // save the date field on the calendar window for access
    // from event handler
    calWindow._dateField = dateField;
  
    // And, for bug 1879034, stash it on a JS variable.  It
    // seems that IE sometimes has already blown away the values
    // on "calWindow"!
    _savedField1879034 = dateField;  
  }
}

// _dfgv(): Date Field Get Value function
// Returns the value of the dateField as a Date object
// or null if there was an error.
function _dfgv(dateField)
{
  if (dateField.value != "")
  {
    try{
      var value = _getDateFieldFormat(dateField).getAsObject(dateField.value);      
      return value;
    }
    catch (e)
    {
      // no-op
    }    
  }

  return null;
}


/* 
 * Returns the time-only portion of the dateField in ms.
 */
function _getTimePortion(dateField)
{
  var oldValue = _dfgv(dateField);

  // If the date field doesn't have a value, use the
  // current time.
  if (!oldValue)
    oldValue = new Date();

  // get just the time portion in milliseconds from the date field.
  // First, get just the date portion of the oldValue 
  // (the value in the date field). 
  // Then subtract the date-only date from the date-time date to get
  // the time only portion. We'll add this time back in after the
  // user picks a date from the calendar. This way the time 
  // in the field, if any, will be preserved, and empty fields
  // will default to the current time.
  var oldValueDateOnly = new Date ( oldValue.getFullYear(), 
                                    oldValue.getMonth(), 
                                    oldValue.getDate());
      
  // get only the time portion of the date in the field.

  return oldValue-oldValueDateOnly;
}

/**
 * compare the time zone that is on the client with the time zone that
 * came from the localeContext on the server, and return the difference.
 * This can be used to adjust the date/time value that will be displayed in
 * the date field to use the timezone set on the locale context on the 
 * server instead of the timezone we get from javascript's getTimezoneOffset.
 * see bug 3167883
 */
function _getLocaleTimeZoneDifference()
{
  var currentDate = new Date();
  // timeZoneOffset in javascript appears to give
  // the wrong sign, so I am switching it.
  // the timeZoneOffset is in minutes.
  var currentDateTzOffset = currentDate.getTimezoneOffset() * -1;
  var tzOffsetDiff = 0;
  if (_uixLocaleTZ)
    tzOffsetDiff = (_uixLocaleTZ - currentDateTzOffset)*60*1000;
  
  return tzOffsetDiff;
}

/**
 * _dbb(): Date Field Blur handler
 *
 * Parameters:
 * - dateField is the date field object
 * - calendarID is the ID of the inline calendar 
 *     associated with dateField
 */
function _dfb(dateField, calendarID)
{
  
   _fixDFF(dateField);

//  if (calendarID != (void 0))
//    _calActiveDateFields[calendarID] = null;

}

/**
 * _dbf(): Date Field Focus handler
 *
 * Parameters:
 * - dateField is the date field object
 * - calendarID is the ID of the inline calendar 
 *     associated with dateField
 */
function _dff(dateField, calendarID)
{
  _dfa(dateField, calendarID);
}

/**
 * _dba(): Date Field Activate
 * 
 * Makes the specified dateField the "active" dateField
 * for the calendar
 *
 * Parameters:
 * - dateField is the date field object or id
 * - calendarID is the ID of the inline calendar 
 *     associated with dateField
 */
function _dfa(dateField, calendarID)
{
  if (calendarID != (void 0))
  {
    if (window._calActiveDateFields == (void 0))
      window._calActiveDateFields = new Object();

    if (typeof(dateField) == "string")
    {
      dateField = _getElementById(document, dateField);
    }

    window._calActiveDateFields[calendarID] = dateField;
  }
}

/**
 * _calsd(): Calendar Select Date function.
 *
 * Parameters:
 * - source is the id of the calendar
 * - value is the date value to select
 */
function _calsd(source, value)
{

  if (window._calActiveDateFields != (void 0))
  {
    var dateField = window._calActiveDateFields[source];

    if (dateField)
      _dfsv(dateField, value);
  }

  return false;
}

function _updateCal(choice,url,p)
{

  url += ('&scrolledValue='+choice.options[choice.selectedIndex].value);
  if (p) 
    _firePartialChange(url);
  else 
    document.location.href=url;
}


function _doCancel()
{
  var dialog = parent.TrPopupDialog.getInstance();
  if (dialog)
  {
    dialog.returnValue = (void 0);
    //TODO - Need Cleaner way to close dialogs using via getInstance()
    parent.TrPopupDialog._returnFromDialog();
  }
  else
  {
    top.returnValue = (void 0);
    top.close();
  }
  return false;
}

function _selectDate(dateTime)
{
  var dialog = parent.TrPopupDialog.getInstance();
  if (dialog)
  {
    dialog.returnValue = dateTime;
    //TODO - Need Cleaner way to close dialogs using via getInstance()
    parent.TrPopupDialog._returnFromDialog();
  }
  else
  {
    top.returnValue = dateTime;
    top._unloadADFDialog(window.event);
    top.close();
  }
  return false;
}

// Holds the date dialog box when open
var _DATE_DIALOG;

var _savedField1879034;
