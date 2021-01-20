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

// External variables used:
//  _cfTransIconURL  - the transparent color swatch icon
//  _cfOpaqueIconURL - the opaque color swatch icon
//  _cfBgColor       - the background color of the page

var _cfBus = new Object();
var _cfTransIconURL;
var _cfOpaqueIconURL;
var _cfBgColor;

/*
 * Update the color field swatch.
 */
function _cfsw(
  colorField)
{
  var cff = _getColorFieldFormat(colorField);
  var swatchID = colorField.name + "$sw";
  var value = null;
  var swatch = _getElementById(document, swatchID);

  if (swatch != null)
  {
    var converterError = false;
    if (colorField.value != "")
    {      
      try
      {
        value = cff.getAsObject(colorField.value);
      }
      catch (e)
      {
        // no-op
      }
    }
    if (value != null)
    {
      if (value.alpha == 0)
      {
        swatch.style.backgroundColor = null;
        swatch.src = _cfTransIconURL;
        swatch.alt = _cfTrans;
      }
      else
      {
        swatch.style.backgroundColor = 
          new TrColorConverter("#RRGGBB").getAsString(value);
        swatch.src = _cfOpaqueIconURL;
        swatch.alt = cff.getAsString(value);
      }
    }
    else
    {
      swatch.style.backgroundColor = _cfBgColor;
      swatch.src = _cfOpaqueIconURL;
      swatch.alt = null;
    }

    // make sure tooltips are updated correctly
    // on Mozilla
    if (_agent.isGecko)
      swatch.title = swatch.alt;
  }
}

function _returnColorPickerValue(
  closingWindow,
  event
  )
{
  // extract the return value
  var newColor = closingWindow.returnValue;
  
  var colorField = closingWindow._colorField;

  // See below!
  if (colorField == null)
  {
    colorField = _savedColorField1879034;
  }

  //pu: Do not update if user dismissed color picker 
  // dialog without "Apply" ing the color
  if (closingWindow.isApplicable)
    _cfUpdate(colorField, newColor);
}

/*
 * ColorField Bus Select (used by ColorPalette)
 */
function _cfbs(
  event)
{
  _cfUpdate(_cfBus[event.source], event.params.value);
}

function _cfUpdate(
  colorField,
  newColor)
{
  if (colorField != null)
  {
    //
    // get the format to use to format the result
    //
    var cff = _getColorFieldFormat(colorField);
    
    // update the contents of the text field
    // or update hidden input for compact color field
    var isVisible = (colorField.type != 'hidden');
    var oldValue = colorField.value;
    var newValue = cff.getAsString(newColor);

    //pu: If transparent was not allowed, and the chosen color were to be
    // transparent, do not update the color field.
    if (newValue == _cfTrans && !cff._allowsTransparent)
      return;

    //pu: In case of blank selection from external picker, undefined newValue is 
    // legitimate, but "undefined" should not literally appear in the text area.
    if (newValue == null)
      newValue="";

    if (newValue != colorField.value)
    {
      // bug 2898744
      // also trigger onchange for compact mode
      // with only color swatch displayed
      // bug 2275982
      // trigger onchange programatically
      if (colorField.onchange != null)
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
        // In all cases, the ordering for updates to the color field are as follows
        // 1. input value updated
        // 2. color swatch synchronized with new value (if present)
        // 3. onchange handler fires (if present)
        if (_agent.isIE)
        {
          // attach the value change listener
          colorField.onpropertychange = function()
          {
            var event = window.event;
            if (event.propertyName == 'value')
            {
              // detach the value change listener
              colorField.onpropertychange = function(){};
              
              // update swatch first to make sure it has correct
              // value before propertychange event delivery
              _cfsw(colorField);
       
              colorField.onchange(event);
            }
          }

          colorField.value = newValue;
        }
        else
        {
          colorField.value = newValue;

          // swatch not supported on NS47
          if (!_agent.isNav)
            _cfsw(colorField);
        
          var event = new Object();
          event.type = 'change';
          event.target = colorField;
          colorField.onchange(event);
        }
      }
      else // no onchange handler
      {
        colorField.value = newValue;

        // swatch not supported on NS47
        if (!_agent.isNav)
          _cfsw(colorField);
      }
    }
        
    if (isVisible)
    {
      colorField.select();
      colorField.focus();
    }
  }
}

/**
 * Private function for launching the color picker
 */
function _lcp(
  formName,
  nameInForm,
  destination
  )
{
  var colorField = document.forms[formName][nameInForm];

  // default the destination to the color picker dialog destination
  if (!destination)
  {
    destination = _jspDir + _getQuerySeparator(_jspDir) + "_t=fred&_red=cp";
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

    var newDest = _jspDir + _getQuerySeparator(_jspDir);
    newDest += urlArgs;

    // add the correct first param separator
    newDest += _getQuerySeparator(newDest);
    newDest += "_t=fred";

    var redirect = destination.substring(0, endOfUrl);

    destination = newDest;

    // add in the redirect
    destination += "&redirect=" + escape(redirect);
  }
  
  var patterns = _cfs[nameInForm];
  var pattern = "#RRGGBB"
  if (patterns != null)
  {
    destination += "&pattern=";
    if (typeof patterns == "string")
    {
      pattern = patterns;
      destination += escape(pattern);
    }
    else
    {
      pattern = patterns[0];
      destination += escape(patterns.join(" "));
    }
  }

  // add the current color text
  if (colorField.value != "")
  {
    var format = _getColorFieldFormat(colorField);
    try
    {
      var color = format.getAsObject(colorField.value);
      if (color != null)
      {
        destination += "&value=";

        // escape due to '#' in color code, or possible spaces in _cfTrans
        if (color.alpha == 0)
          destination += escape(_cfTrans);
        else
          destination += escape(new TrColorConverter(pattern).getAsString(color));
      }
    }
    catch (e)
    {
      // no-op 
    }
  }

  var allowsTransparent = _cfts[nameInForm];
  if (allowsTransparent != null)
  {
    destination += "&allowsTransparent=" + allowsTransparent;
  }

  // add the locale
  destination += "&loc=" + _locale;
    
  // and the character set encoding
  if (window["_enc"])
  {
    destination += "&enc=" + _enc;
  }

  var colorDialog = openWindow(self,
                             destination,
                             'colorDialog',
                             {width:430, height:230},
                             true,
                             null,
                             _returnColorPickerValue);
  
  // save the date field on the calendar window for access
  // from event handler
  colorDialog._colorField = colorField;

  // And, for bug 1879034, stash it on a JS variable.  It
  // seems that IE sometimes has already blown away the values
  // on "colorDialog"!
  _savedColorField1879034 = colorField;  
}

var _savedColorField1879034;
