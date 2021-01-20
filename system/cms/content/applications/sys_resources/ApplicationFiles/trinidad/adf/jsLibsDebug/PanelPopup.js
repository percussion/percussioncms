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

/**
 * Simple function for opening a popup
 * @param contentId(String) id of the element to pop
 * @param triggerId(String) optional id of the element that launched the popup
 * @param props(Array) array of settings (modal(boolean), center(boolean), width(int), height(int))
 * @param event(Event) the javascript event object (used to position relative popups)
 **/
TrPanelPopup.showPopup = function(contentId, triggerId, props, event)
{
  var popup = TrPanelPopup.POPUP;
  if (!popup)
    popup = TrPanelPopup.POPUP = new TrPanelPopup();
  else
    popup.hide();

  var content = document.getElementById(contentId);
   if (!content)
     return;

  var trigger = document.getElementById(triggerId);
   
  popup.setContent(content);
  popup.setTrigger(trigger);
  popup.setModal(false);
  popup.setCentered(false);

  // Register the callback
  popup.callback = TrPanelPopup._popupCallback;

  if (props)
  {
    if (props['modal'])
      popup.setModal(true);

    if (props['center'])
      popup.setCentered(true);

    if (props['width'] && props['height'])
      popup.setSize(props['width'], props['height']);
  }
  
  popup.show(event);
}

/**
 * Public function for hiding the current popup.
 */  
TrPanelPopup.hidePopup = function()
{
  var popup = TrPanelPopup.POPUP;
  if (popup)
    popup.hide();
}

/**
 * Callback function to ensure we clear
 * the POPUP var.
 */  
TrPanelPopup._popupCallback = function()
{
  TrPanelPopup.POPUP = undefined;
}

/**
 * Class to handle a popup element on a page.
 */
function TrPanelPopup()
{
  //define object properties
  this._content = false;
  this._trigger = false;
  this._centered = false;
  this._modal = false;
  this._visible = false;

  // Store the callback function so we can cleanly cancel it later
  this._eventCallbackFunction = TrUIUtils.createCallback(this, this._handleEvent);
}

TrPanelPopup.prototype.getContent = function()
{
  return this._content;
}

TrPanelPopup.prototype.setContent = function(content)
{ 
  this._content = content;
  
  //Initialize the styles for the content
  if (this._content)
  {
    this._content.style.cssText  = "position: absolute; z-index: 201; top: 0px; left: 0px; visibility:hidden; padding: 0px; overflow:auto;";  
  }
}

/**
 * Get the element being used as the trigger
 **/
TrPanelPopup.prototype.getTrigger = function()
{
  return this._trigger;
}

/**
 * Sets the element to be used as the trigger to show the popup.  We
 * use this to ensure further events on the trigger don't cause a re-popup.
 * @param trigger(Element) The element that will trigger the popup.
 **/
TrPanelPopup.prototype.setTrigger = function(trigger)
{
  this._trigger = trigger;
}


/**
 * Sets the popup to be centered on screen when visible
 * @param centered(boolean) true if popup should be centered
 **/
TrPanelPopup.prototype.setCentered = function(centered)
{
  this._centered = centered;
}

/**
 * Returns true if the popup is set to modal.
 **/
TrPanelPopup.prototype.isModal = function()
{
  return this._modal;
}

/**
 * Sets the popup to be modal when visible
 */
TrPanelPopup.prototype.setModal = function(modal)
{
  this._modal = modal;
}

/**
 * Returns true if the popup is currently visible.
 **/
TrPanelPopup.prototype.isVisible = function()
{
  return this._visible;
}


/**
 * Holds the return value of the dialog.  Check this property after the 
 * popup has closed.
 **/
TrPanelPopup.prototype.returnValue = undefined;

/**
 * Attach a callback function that will be invoked when the popup
 * has been closed.  The callbackProps and returnValue properties will be
 * passed as parameters (e.g. function myCallback(props, value);).
 **/
TrPanelPopup.prototype.callback = undefined;

/**
 * Attach properties to the popup that will be passed to the callback function
 * (e.g. a component target to populate with the returnValue).
 **/
TrPanelPopup.prototype.callbackProps = undefined;

/**
 * Make the popup visible
 **/
TrPanelPopup.prototype.show = function(event)
{
  //we can't show content that isn't there
  if (!this.getContent())
    return;
 
  //don't pop during ppr - safety check
  if (_pprBlocking)
    return;

  //already visible
  if (this.isVisible())
    return;

  this._calcPosition(event);
  
  if (this.isModal())
  {
    TrPanelPopup._showMask();
  }
  else
  {
    // Setup event handler to close the popup if clicked off
    TrPanelPopup._addEvent(document, "click", this._eventCallbackFunction);
  }
  
  TrPanelPopup._showIeIframe();

  this.getContent().style.visibility = "visible"; 
  
  this._visible = true;
}

/**
 * Hide the popup if visible.  Hiding the popup causes the callback
 * handler to be invoked (if configured).
 **/
TrPanelPopup.prototype.hide = function()
{
  //we can't hide content that isn't there
  if (!this.getContent())
    return;

  if (this.isModal())
  {
    TrPanelPopup._hideMask();
  }
  else
  {
    //cancel callbacks
    TrPanelPopup._removeEvent(document, "click", this._eventCallbackFunction);
  }
  
  TrPanelPopup._hideIeIframe();
  
  this.getContent().style.visibility = "hidden";
  //move popup back to top left so it won't affect scroll size if window resized
  this.getContent().style.left = "0px";
  this.getContent().style.top = "0px";
  
  //call the callback function if attached
  if (this.callback)
  {
    this.callback(this.callbackProps, this.returnValue);
  }

  this._visible = false;
}

/**
 * Size the popup to a specific width and height
 */
TrPanelPopup.prototype.setSize = function(width, height)
{
  if (width)
  {
    this.getContent().style.width = width + "px";
  }
  if (height)
  {
    this.getContent().style.height = height + "px";
  }
}

/**
 * Event handler function.  Checks if the event occurred outside
 * of the popup or trigger, and if so causes the popup to hide.
 **/
TrPanelPopup.prototype._handleEvent = function(event)
{
  if (!this.isVisible() || this.isModal())
    return;

  var currElement = false;
  if (_agent.isIE)
  {
    currElement = event.srcElement;
  }
  else
  {
    currElement = event.target;
  }

  //loop through element stack where event occurred
  while (currElement)
  {
    //if clicked on trigger or popup  
    if (currElement == this.getContent() || 
        currElement == this.getTrigger())
    {
      break;
    }
    currElement = currElement.parentNode;
  }

  if (!currElement)
  {
    //if click was on something other than the popupContainer
    this.hide();
  }
}

// The modal mask - shared by all instances
TrPanelPopup._mask = undefined;

/**
 * Show the popup mask that blocks clicks in modal mode.  Initialize it
 * if not already.
 **/
TrPanelPopup._showMask = function()
{
  //initialise mask only once
  if (!TrPanelPopup._mask)
  {
    //create mask for modal popups
    TrPanelPopup._mask = document.createElement('div');
    TrPanelPopup._mask.name = "TrPanelPopup._BlockingModalDiv";
    TrPanelPopup._mask.style.cssText = "display:none;position: absolute; z-index: 200;top: 0px;left: 0px;cursor: not-allowed; background-color: transparent;";
    TrPanelPopup._mask.innerHTML = "&nbsp;";

    //consume all events
    TrPanelPopup._addEvent(TrPanelPopup._mask, "click", TrPanelPopup._consumeMaskEvent);

    //handle window resize events
    TrPanelPopup._addEvent(window, "resize", TrPanelPopup._setMaskSize);

    //set initial mask size
    TrPanelPopup._setMaskSize();

    //add mask to body
    document.body.appendChild(TrPanelPopup._mask);
    
  }

  TrPanelPopup._mask.style.display = "block";
  
}

/**
 * Hide the popup mask that blocks clicks in modal mode.
 **/
TrPanelPopup._hideMask = function()
{
  TrPanelPopup._removeEvent(TrPanelPopup._mask, "click", TrPanelPopup._consumeMaskEvent);
  TrPanelPopup._removeEvent(window, "resize", TrPanelPopup._setMaskSize);
  TrPanelPopup._mask.style.display = "none";
}

/**
 * Position the popup ensuring it doesn't go off-page, and if centered, then 
 * center in the middle of the current window.
 **/
TrPanelPopup.prototype._calcPosition = function(event)
{
  //position the popup
  var left = 0;
  var top = 0;
  
  var isIE = _agent.isIE;
  
  //bring some sanity to the cross browser measurements
  var xOffset = isIE ? document.body.scrollLeft : window.pageXOffset;
  var yOffset = isIE ? document.body.scrollTop : window.pageYOffset;
  var scrollWidth = document.body.scrollWidth;
  var scrollHeight = document.body.scrollHeight;
  var bodyWidth = isIE ? document.body.clientWidth : window.innerWidth;
  var bodyHeight = isIE ? document.body.clientHeight : window.innerHeight;
  var containerWidth = this.getContent().clientWidth;
  var containerHeight = this.getContent().clientHeight;

  if (this._centered)
  {
    left = xOffset + ((bodyWidth - containerWidth) / 2);
    top = yOffset + ((bodyHeight - containerHeight) / 2);
  }
  else
  {
    var eventX = isIE ? window.event.clientX : event.clientX;
    var eventY = isIE ? window.event.clientY : event.clientY;

    //ensure we keep popup within current page width
    if (xOffset + eventX + containerWidth > document.body.scrollWidth)
      left = document.body.scrollWidth - containerWidth;
    else
      left = xOffset + eventX;

    //ensure we keep popup within current page height
    if (yOffset + eventY + containerHeight > document.body.scrollHeight)
      top = document.body.scrollHeight - containerHeight;
    else
      top = yOffset + eventY;
  }  

  this.getContent().style.left = left + "px";
  this.getContent().style.top = top + "px";

  if (!this.isModal())
    TrPanelPopup._resizeIeIframe(left, top, containerWidth, containerHeight);
}

/**
 * Simple event handler that consumes any clicks when modal popup is shown
 */
TrPanelPopup._consumeMaskEvent = function(event)
{
  return false;
}

//useful event registration function
TrPanelPopup._addEvent = function(obj, evType, fn)
{
  // TODO: abstract onto Agent object
  if (obj.addEventListener)
  {
    obj.addEventListener(evType, fn, false);
    return true;
  }
  else if (obj.attachEvent)
  {
    var r = obj.attachEvent("on"+evType, fn);
    return r;
  }
  else
  {
    return false;
  }
}

//useful event deregistration function
TrPanelPopup._removeEvent = function(obj, evType, fn)
{
  // TODO: abstract onto Agent object
  if (obj.removeEventListener)
  {
    obj.removeEventListener(evType, fn, false);
    return true;
  }
  else if (obj.detachEvent)
  {
    var r = obj.detachEvent("on"+evType, fn);
    return r;
  }
  else
  {
    return false;
  }
}

/*
 * Sizes/resizes the modal mask if the window size changes
 */
TrPanelPopup._setMaskSize = function()
{
  //only bother if mask is inited
  if (!TrPanelPopup._mask)
    return;

  if (window.innerHeight!=window.undefined)
    fullHeight = window.innerHeight;
  else if (document.compatMode=='CSS1Compat')
    fullHeight = document.documentElement.clientHeight;
  else if (document.body)
    fullHeight = document.body.clientHeight;

  if (window.innerWidth!=window.undefined) 
    fullWidth = window.innerWidth;
  else if (document.compatMode=='CSS1Compat')
    fullWidth = document.documentElement.clientWidth;
  else if (document.body)
    fullWidth = document.body.clientWidth;
    
  // Determine what's bigger, scrollHeight or fullHeight / width
  if (fullHeight > document.body.scrollHeight)
  {
    popHeight = fullHeight;
  }
  else
  {
    popHeight = document.body.scrollHeight
  }
  
  TrPanelPopup._mask.style.height = popHeight + "px";
  TrPanelPopup._mask.style.width = document.body.scrollWidth + "px";
  
  TrPanelPopup._resizeIeIframe(0, 0, document.body.scrollWidth, popHeight);
}

/**
 * FUNCTIONS BELOW IMPLEMENT CSS/IFRAME WORKAROUND FOR THE INFAMOUS IE 6.x SELECT ZINDEX BUG
 * More info here: http://dotnetjunkies.com/WebLog/jking/archive/2003/07/21/488.aspx
 **/
TrPanelPopup._showIeIframe = function()
{
  // FIXME: only bother doing this for IE 6 - bypass the code for IE 7
  if (_agent.isIE)
  {
    TrPanelPopup._initIeIframe();
    TrPanelPopup._maskIframe.style.display = "block";      
  }
}

TrPanelPopup._hideIeIframe = function()
{
  if (_agent.isIE)
  {
    TrPanelPopup._initIeIframe();
    TrPanelPopup._maskIframe.style.display = "none";      
  }
}

TrPanelPopup._resizeIeIframe = function(left, top, width, height)
{
  if (_agent.isIE)
  {
    TrPanelPopup._initIeIframe();
    TrPanelPopup._maskIframe.style.left = left;
    TrPanelPopup._maskIframe.style.top = top;
    TrPanelPopup._maskIframe.style.width = width;
    TrPanelPopup._maskIframe.style.height = height;
  }
}

TrPanelPopup._initIeIframe = function()
{
  if (!TrPanelPopup._maskIframe)
  {
    //create single reusable iframe if not already inited
    TrPanelPopup._maskIframe = document.createElement('iframe');
    TrPanelPopup._maskIframe.name = "TrPanelPopup._ieOnlyZIndexIframe";
    TrPanelPopup._maskIframe.style.cssText = "display: none; left: 0px; position: absolute; top: 0px; z-index: 199;";
    TrPanelPopup._maskIframe.style.filter = "progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0)";
    // FIXME: should this be set to avoid SSL warnings?
    //TrPanelPopup._maskIframe.src = "javascript:false;"
    document.body.appendChild(TrPanelPopup._maskIframe);
  }
}
