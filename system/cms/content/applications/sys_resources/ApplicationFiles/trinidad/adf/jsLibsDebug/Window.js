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
 * Note that the only function in this file that is supported is the openWindow
 * function.
 * All other functions may change without notice.
 */

var ADFDialogReturn = new Array();

function _launchDialog(
  srcURL,
  windowName,
  features,
  formName,
  postbackId,
  partial)
{
  // Make sure we're calling submitForm() on the correct document
  var myself = self;
  var returnFromDialog;

  if (partial)
  {
    returnFromDialog = function()
    {
      myself._submitPartialChange(formName, 0, {rtrn:postbackId});
    };
  }
  else
  {
    returnFromDialog = function()
    {
      myself.submitForm(formName, 0, {rtrn:postbackId});
    };
  }

  var index = ADFDialogReturn.length;
  ADFDialogReturn[index] = returnFromDialog;
  srcURL = srcURL + "&_rtrnId=" + index;
  openWindow(window, srcURL, windowName, features, 1);
}


/**
 * Opens a new window, makes it active, and returns the Window
 * object.
 * <p>
 * @param parentWindow The window opening this window.  For modal
 *                     windows, this is the window that the
 *                     new window will be modal to. REQUIRED.
 * @param srcURL       URL for the contents of the new window.
 *                     REQUIRED.
 * @param windowName   Name that identifies the new window for
 *                     URLS. Required if a closeCallback is to be used.
 * @param features     Javascript object containing name/value pairs
 *                     of features to apply to the new window,
 * @param isModal      True if the window should be modal.
 * @param kind         Either 'document' or 'dialog'.  The default is
 *                     dependent on the value of isModal--'document'
 *                     for modeless, 'dialog' for modal.
 * @param closeCallback  Callback method called when the window is closed.
 *                      Note: Under certain adverse conditions (for example,
 *                      if the parentWindow navigates to a new page), it is
 *                      possible that the closeCallback may not be called.
 */
function openWindow(
  parentWindow,
  srcURL,
  windowName,
  features,
  isModal,
  kind,
  closeCallback
  )
{
  if (parentWindow)
  {
    // default modality if none specified
    if (isModal == (void 0))
      isModal = false;

    // if the kind isn't specified, default the kind off of the modality
    if (!kind)
    {
      kind = (isModal) ? "dialog" : "document";
    }

    // default the window name to "_blank" if no
    // name is specified, in order to guarantee that a new window is
    // opened
    if (!windowName)
      windowName = "_blank";

    //
    // pick the correct defaults and overrides
    //
    var defaults = _featureDefaults[kind];

    if (defaults == (void 0))
    {
      kind = "document";
      defaults = _featureDefaults[kind];
    }

    var overrides = (isModal)
                      ? _modalFeatureOverrides
                      : _modelessFeatureOverrides;

    // determine the avialable features
    var agentFeatures = (_agent.isIE)
                          ? _ieFeatures
                          : _nnFeatures;

    // we'll be hammering the features object, so make a copy
    var featuresCopy = null;
    if (features)
    {
      featuresCopy = new Object();
      for (var f in features)
      {
        featuresCopy[f] = features[f];
      }
    }

    //
    // build up the feature string
    //
    var featureString = "";

    // loop through the available features for this platform
    for (var featureName in agentFeatures)
    {
      // get the overridden value of the feature
      var featureValue = overrides[featureName];

      if (featureValue == (void 0))
      {
        // get the value of the feature if it isn't overridden
        if (featuresCopy)
        {
          featureValue = featuresCopy[featureName];
          delete featuresCopy[featureName];
        }

        // if no value, get the default value, if any
        if (featureValue == (void 0))
          featureValue = defaults[featureName];
      }

      if (featureValue != (void 0))
      {
        // check if this is a boolean value
        var isBoolean = _booleanFeatures[featureName] != (void 0);

        // output the value
        if (featureValue || !isBoolean)
        {
          // add the feature name
          featureString += featureName;

          // add the value for nonboolean features
          if (!isBoolean)
          {
            featureString += "=" + featureValue;
          }

          // add separator between this and the next
          featureString += ",";
        }
      }
    }

    // Now tack on all the extra features that the user has requested.
    // These may or may not have meaning to the browser's implementation of
    // window.open().
    for (var f in featuresCopy)
    {
      featureString += f;
      if (featuresCopy[f])
        featureString += "=" + featuresCopy[f];

      featureString += ",";
    }

    // trim off last separator
    if (featureString.length != 0)
    {
      featureString = featureString.substring(0, featureString.length - 1);
    }

    // register the closing callback
    if (closeCallback)
    {
      _setDependent(parentWindow, windowName, closeCallback);
    }

    // open an empty window
    var newWindow = parentWindow.open(srcURL, windowName, featureString);

    // Check for popup blockers.
    // This will certainly all change, but now (early 2005) the popup blockers
    // work thusly:
    //
    // Google:  The return value from window.open is null.
    // Safari:  The return value from window.open is null.
    // FireFox: The return value from window.open is a valid object
    //          with no properties.
    // Yahoo!:  The return value from window.open is a valid object which
    //          throws an exception when you try to do anything with it.
    var usableWindow = false;
    if (newWindow != null)
    {
      var propCount = 0;
      try
      {
        for (p in newWindow)
        {
          propCount++;
          break;
        }
        // if (propCount == 0) this is Firefox popup blocker
        if (propCount > 0)
          usableWindow = true;
      }
      catch (e)
      {
        // Yahoo! toolbar throws an exception when you try to access any of the
        // new window's properties.
      }
    }
    // else this is the Safari or Google Toolbar popup blocker

    if (!usableWindow)
    {
      _setDependent(parentWindow, windowName, (void 0));
      if (_AdfWindowOpenError != null)
        alert(_AdfWindowOpenError);
      return;
    }

    // detect the bogus ie4 and turn off modal windows
    var atMostIE4 = _agent.atMost("ie", 4.99);
    var alphaFilter = false;

    // body of the aprent window
    var parentBody = parentWindow.document.body;

    if (isModal && !atMostIE4)
    {
      if (_agent.atLeast("ie", 4))
      {
        parentBody.style.filter = "alpha(opacity=50)";
        alphaFilter = true;
      }

      // Capture mouse events.  Note: we special-case IE/Windows,
      // and only apply the capture after window.open() has been
      // called.  See below for details.
      // XXXSafari: What to do for Safari?
      if (_agent.isGecko)
      {
        // this should work, but doesn't appear to
        if (parentBody != (void 0))
          _addModalCaptureGecko(parentBody);
      }

      parentWindow.onfocus = _onModalFocus;
    }

    // Apply mouse capture for IE/Windows.  Starting in IE 6.0.2800,
    // if we apply the capture before calling window.open(), the capture
    // is lost immediately when the secondary window receives the
    // focus.  To make matters worse, the _onModalFocus handler that
    // we register on the parent window is no longer invoked (not
    // sure why?!).  The end result is that the user can interact
    // with the parent window even when a modal child is displayed.
    // Delaying our setCapture() call until after window.open()
    // seems to work around the problem.
    if (isModal && (_agent.atLeast("ie", 5) && _agent.isWindows))
    {
      _addModalCaptureIE(parentBody);

      // Set up an onlosecapture handler so that we can
      // restore the capture if we unexpectedly lose it.
      parentBody.onlosecapture = _onModalLoseCapture;

      // Popup blockers!
      // BUG 4428033 - ECM: MENUS BECOM DISABLED AFTER RAISING A DIALOG
      //                    FROM A COMMANDMENUITEM
      // When a popup blocker is installed, the onunload event is often
      // not delivered to listeners attached in the scope of the opened
      // modal window.  However, onunload events _are_ delivered to
      // listeners attached in the opener context.
      //
      // However, this onunload event listing is only permitted within the
      // same scripting domain. We should really verify if the domains match
      // or not, but relative URLs are always in the same domain, so just
      // test for absolute URLs instead.
      //
      // A quick check for absolute URL is to test the presence of an
      // unescaped colon.
      //
      var isAbsolute = (srcURL != null && srcURL.indexOf(':') != -1);
      if (!isAbsolute)
      {
        var removeCapture = new Function("e", "_removeModalCaptureIE(window.document.body)");
        newWindow.attachEvent("onunload", removeCapture);
      }
    }

    /*
    // create the content for the window.  We use a frameset so that
    // the content can change without firing the window close event.
    var realSrcURL = "<html>";

    //realSrcURL += "<head><scr";
    //realSrcURL += 'ipt src="/images/jsLibs/Window.js"></head>';

    realSrcURL += '<frameset rows="100%,*" border="0" onunload="_checkUnload(event)"><frame src="' +
          srcURL +
          '"></frameset></html>';

    newWindow.document.write(realSrcURL);
    newWindow.document.close();
        */

    if (isModal && !atMostIE4)
    {
      _setDependent(parentWindow, "modalWindow", newWindow);
    }

    // If there are any poll commands registered, they should be deactivated when the
    //  modal window gets launched. They need to be reactivated upon closing the
    //  modal dependent using _pollWhenModalDependentCloses().
    // Cleaner alternative to _pollWhenModalDependentCloses() could have been to use a
    //  registered callback like _checkUnload(), _onModalFocus(), _onModalLoseCapture().
    //  But none of these were reliable for either of...
    //   1. One of them is to workaround IE grabbing focus on parent
    //       while dialog launch in progress.
    //   2. Does not get called when dialog is dismissed using the close
    //       button on the window, particularly for case of dialogs
    //       launched using openWindow().
    if (isModal && self._pollManager)
    {
      _pollManager.deactivateAll();
      _pollWhenModalDependentCloses();
    }

    // make the active window
    newWindow.focus();


    // Set up a timer to make sure that we reset the alpha filter.
    if (alphaFilter)
    {
      parentWindow.setTimeout("_clearBodyModalEffects('alpha')", 1000);
    }

    return newWindow;
  }
  else
  {
    return null;
  }
}

// Keeps checking for absence of a modal dependent, when found
//  reactivates all poll, and stops checking any further
function _pollWhenModalDependentCloses()
{
  if (!_getValidModalDependent(self))
  {
    _pollManager.reactivateAll();
  }
  else
  {
    // pu: Call thyself to check again after a second
    //  If more accuracy required, set it to a millisecond
    self.setTimeout("_pollWhenModalDependentCloses()", 1000);
  }
}

// Called on IE when we lose the capture.  As seen in bugs
// 3613614 and 3540569, IE may unexpectedly lose its capture
// while a modal window is being displayed.  When this occurs,
// we attempt to restore the capture so that users won't be
// able to interact with the parent window while a modal child
// is present.
function _onModalLoseCapture()
{
  // We've lost our mouse capture.  Check to see whether
  // we still have any modal child windows.
  var modalDependent = _getValidModalDependent(self);

  if (modalDependent)
  {
    // If we still have a modal child, we need to re-apply
    // our mouse capture.  To do this, we just call _onModalFocus(),
    // which has the necessary code to set the capture.
    //
    // Note, however, that IE doesn't seem to honor the new mouse capture
    // if it is applied from within the onlosecapture handler.  To work
    // around this, we queue the call to _onModalFocus().  That way,
    // the capture will be applied after the onlosecapture event
    // has been handled, and IE seems to be happy.

    window.setTimeout("_onModalFocus()", 1);
  }
}


/**
 * onFocus override when modal windows are up.
 * Handles moving the focus to the top and suppressing IE mouse clicks.
 */
function _onModalFocus()
{
  var theBody = self.document.body;

  // =-=ags Note: This used to call _getValidModalDependent(),
  //        but now instead we call _getModalDependent() and
  //        explicitly check the value of the closed property.
  //        The reason for this is that on Gecko, _onModalFocus
  //        is called before _checkUnload().  This means that by
  //        the time _checkUnload() is called, _getValidModalDependent()
  //        will have already removed the modal window from the
  //        dependents list.  As a result, the capture event handlers
  //        added on Gecko are never removed.
  var modalDependent = _getModalDependent(self);

  var hasCapture = _agent.atLeast("ie", 5) && _agent.isWindows;

  if (modalDependent && !modalDependent.closed)
  {
    modalDependent.focus();

    // reset the capture on IE since it is released whenever
    // the focus moves to another window
    if (hasCapture)
    {
      theBody.setCapture();
    }
  }
  else
  {
    if (hasCapture)
    {
      theBody.onlosecapture = null;
      _removeModalCaptureIE(theBody);
    }
  }
}


// A self-rescheduling function which clears out capture or alpha filtering
// when modal windows are closed.  These effects are normally removed in
// the modal window's onunload() handler.  However, the onunload handler
// may not be called if the modal window is closed by the user before
// loading completes.  This function is used in conjunction with
// Window.setTimeout() to make sure that the effects are cleared
// in this case.
function _clearBodyModalEffects(effect)
{
  if (_getValidModalDependent(self) != null)
  {
    // If the modal window is still shown, re-schedule ourselves
    self.setTimeout("_clearBodyModalEffects('" + effect + "')", 1000);
  }
  else
  {
    if (effect == 'alpha')
    {
      // No modal dependent - clear out the alpha filter and
      // don't bother rescheduling.
      self.document.body.style.filter = null;
    }
  }
}

/**
 * Returns the dependent which is modal and is still open.
 */
function _getValidModalDependent(
  parentWindow
  )
{
  var modalDependent = _getModalDependent(parentWindow);

  if (modalDependent)
  {
    if (modalDependent.closed)
    {
      _setDependent(parentWindow, "modalWindow", (void 0));
      modalDependent = (void 0);
    }
  }

  return modalDependent;
}


/**
 * Sizes the window to its preferred size.
 */
function _sizeWin(
  theWindow,
  extraWidth,
  extraHeight,
  params
  )
{
  var isGecko    = _agent.isGecko;
  var isIE       = _agent.isIE;
  var isSafari   = _agent.isSafari;
  var isStandard = (isGecko || isSafari);

  if (!(isStandard || (isIE && _agent.isWindows)))
    return;

  /*
  // =-= bts theoretically, this would be all we need to do for Mozilla,
  //         but the implementation appears to be sub-optimal for our case
  if (isGecko)
  {
    theWindow.sizeToContent();
    return;
  }
  */
  var body =  theWindow.document.body;

  if (body)
  {
    // width of the inside
    var newWidth = (!isIE && (body.scrollWidth > body.clientWidth))
                     ? body.scrollWidth
                     : _getBodyWidth(body, body.offsetWidth, body.offsetLeft);
    var newHeight = 0;

    if (isStandard)
    {
      newHeight = body.offsetHeight + (window.outerHeight - window.innerHeight);

      // random extra space added in to make this work
      newHeight += 30;

      // add in the window frame plus padding
      // =-=AEW For some bizarre reason, I'm seeing cases
      // where the window's outerWidth is much, much smaller than
      // the body's offsetWidth, which results in the Gecko
      // window shrinking.  Block this, though it'd be nice
      // to know what's really going on!
      if (window.outerWidth > body.offsetWidth)
        newWidth  += (window.outerWidth - body.offsetWidth);
    }
    else
    {
      newHeight = body.scrollHeight + (body.offsetHeight - body.clientHeight);
      // if this method supported windows with toolbars, we would need to
      // add another 67 here, rather than the aribitrary 8 pixels
      newHeight += 21;

      // add in the padding plus bogus extra size
      newWidth += body.offsetWidth - body.clientWidth + 16;
      // add in the margins (MS bogusly uses Strings for these)

      newHeight += parseInt(body.topMargin) + parseInt(body.bottomMargin);
      newWidth  += parseInt(body.leftMargin) + parseInt(body.rightMargin);
    }
    //
    // allow the size to be a little bigger than currently necessary.
    // This is useful when we will be paging through multiple items and
    // later pages  might need a slightly larger window than the initial
    // page.
    if (extraWidth)
      newWidth += extraWidth;

    if (extraHeight)
      newHeight += extraHeight;

    // Make sure that width and height are at least as big as minimum requested
    if (params != (void 0))
    {
      if (params['W'])
      {
        var minWidth = 0 + params['W'];
        if (newWidth < minWidth)
          newWidth = minWidth;
      }

      if (params['H'])
      {
        var minHeight = 0 + params['H'];
        if (newHeight < minHeight)
          newHeight = minHeight;
      }
    }

    var newWin = _getTop(theWindow);

    // keep a bottom/right pad of at least 5% of the available screen
    var avLeft = isIE ? 0 : newWin.screen.availLeft;
    var avTop = isIE ? 0 : newWin.screen.availTop;
    var maxSHeight = newWin.screen.availHeight * 0.95;
    var maxSWidth = newWin.screen.availWidth * 0.95;
    // adjust if necessary
    if (newHeight > maxSHeight)
      newHeight = maxSHeight;
    if (newWidth > maxSWidth)
      newWidth = maxSWidth;

    // Finally, we can resize the window.
    // theWindow.parent.resizeTo(newWidth, newHeight);
    newWin.resizeTo(newWidth, newHeight);

    // Check to make sure that our resize hasn't put the
    // window partially off screen.
    var wLeft = isIE ? newWin.screenLeft: newWin.screenX;
    var wTop = isIE ? newWin.screenTop: newWin.screenY;
    var moveit = false;

    // If we are off screen horizontal or vertical, center in that direction
    if ((wLeft + newWidth) > (avLeft + maxSWidth))
    {
      wLeft = (newWin.screen.availWidth - newWidth)/2;
      moveit = true;
    }

    if ((wTop + newHeight) > (avTop + maxSHeight))
    {
      wTop = (newWin.screen.availHeight - newHeight)/2;
      moveit = true;
    }

    if (moveit)
    {
      newWin.moveTo(wLeft, wTop);
    }
  }
}




