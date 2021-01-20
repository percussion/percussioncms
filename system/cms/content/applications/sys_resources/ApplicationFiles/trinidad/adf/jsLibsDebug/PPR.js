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
 * This callback function gets called each time
 * a library is downloaded. This is for IE only.
 * index - an index which indicates which library is being loaded.
 *     s - the library contents.
 * Store the library scripts in an array. When they are all downloaded
 * execute the contents of each library.
 */
function _pprExecScript(index, s)
{
  if (_pprLibStore && _pprLibStore.allLibraries != (void 0))
  {
    _pprLibStore.allLibraries[index] = s;
    _pprLibStore.loadedStatus[index] = true;

    // Check to see if all the libraries are loaded. Start at the end
    // of the array, since those will most likely be the last loaded, so
    // we can return more quickly.
    for (var index = _pprLibStore.total-1; index >= 0; index--)
    {
      // well, we see that there is a library not loaded, so return. When
      // they are all loaded, then we'll pass through and execute them.
      if (!_pprLibStore.loadedStatus[index])
        return;
    }

    // All the libraries are loaded, so loop through each and evaluate it
    // in the context of the proper target window. This window is usually
    // the iframe, but in the case of a full page refresh, it's the current
    // window.
    for (var i=0; i < _pprLibStore.total; i++)
    {
      var win = parent;

      // This one is fragile... have to hard code the name here.
      if ("_pprIFrame" != window.name)
      {
        win = window;
      }
      win.execScript(_pprLibStore.allLibraries[i]);
    }
    _pprLibStore = null;
  }
}

function _pprCopyObjectElement(sourceDocument, targetDocument)
{

  // uncomment the object tag within script id="_pprObjectScripti"
  // we have to do this because if an object element is within
  // a ppr target, it will not be initialized if it is already
  // initialized in the iframe. To workaround this problem, we put
  // a span and a script around the <media> element's html (which is an
  // html object element), and we comment out the <media>'s html. Here
  // we uncomment it and copy it into the source document.
  var objectSuffix = 0;
  while (true)
  {
    // get script element
    var objectScriptElementId = "_pprObjectScript" + objectSuffix;

    var objectScriptElement = _getElementById(sourceDocument,
                                              objectScriptElementId);
    // if no script element, break
    if (objectScriptElement == null)
      break;
    else
    {
      // if script element, get the uncommented script.
       var scriptText = _getCommentedScript(sourceDocument,
                                            objectScriptElementId);
    }
    if (scriptText != null)
    {
      // copy scriptText into main page.
      var objectSpanElementId = "_pprObjectSpan" + objectSuffix;
      var objectSpanElement = _getElementById(targetDocument,
                                              objectSpanElementId);
      if (objectSpanElement != null)
        objectSpanElement.outerHTML = scriptText;
    }
    objectSuffix++;
  }
}

//
// _pprConsumeFirstClick: Helps implement blocking.
//
// On IE, the capture doesn't allow us to hand off the first click - we can
// only eat it, but attachEvent only allows us to do something with the event
// AFTER it's been delivered to the element. There's no way to make a decision
// whether or not to deliver a particular event. Therefore, since we want to
// deliver the first click, and block everything else, we attachEvent using
// this handler. This handler then just immediately switches over the the
// capture. This function is only used on IE.
//
function _pprConsumeFirstClick(event)
{
  // This is an IE only function
  if (_agent.isIE)
  {
    // switch over to capture
    _pprControlCapture(window, true);
    // and remove this one-time function
    window.document.detachEvent('onclick', _pprConsumeFirstClick);
  }
  return false;
}



//
// _pprConsumeBlockedEvent: Helps implement blocking. This function attached
//                          as the event handler. It just consumes every event
//                          it gets.
//
//                          This function is used on standards based browsers.
//
function _pprConsumeBlockedEvent(evt)
{
  var rv = true;

  if (_pprBlocking)
  {
    var blockTheEvent = true;

    if (window._pprFirstClickPass)
    {
      var newDate = new Date();
      var diff = newDate - _pprBlockStartTime;

      // If we've got a click on a button (or an image within a link that does
      // a submit), less than 150ms after the beginning of a PPR update, assume
      // the user has started an onChange type event with a click on a button.
      // This addresses the problems that people were seeing, but could cause
      // overlapping PPR events in rare cases.
      var delay = 150;
      if ((diff < delay) && (evt.type == 'click'))
      {
        // To try to further limit the overlaps, we only allow clicks on
        // buttons, or images that will cause a submit to go through.
        // get the target of the click
        var orig = evt.explicitOriginalTarget;
        // this function is never called on IE, but if it were, this would be:
        // var orig = (_agent.isIE
        //            ? evt.srcElement
        //            : evt.explicitOriginalTarget);
        blockTheEvent = ! _isSubmittingElement(orig);
      }
    }
    if (blockTheEvent)
    {
      // just swallow the event
      evt.stopPropagation();
      evt.preventDefault();
      rv = false;
    }
  }
  return rv;
}

//
// _pprConsumeClick: Helps implement blocking. This function just consumes
//                   every click that falls within the body.
//
function _pprConsumeClick(event)
{
  if (_agent.isIE)
  {
    var body = document.body;
    if ((event.x < body.offsetLeft) || (event.y < body.offsetTop)
        || (event.x > body.offsetWidth) || (event.y > body.offsetHeight))
    {
      // OK, we've caught an event outside the body of the document. Assume
      // that the user is clicking somewhere on the menu bar, or another
      // window. At this point, we release the mouse and continue (that's
      // better than keeping the user in limbo).

      _pprStopBlocking(window);
    }
  }
  return false;
}




/**
 * Unload event handler for the partial iframe
 */
function _partialUnload()
{
  // We try to detect back events and jump back to the last
  // fully rendered page.  We consider an unload to be the
  // result of a back or refresh event if the _pprRequestCount
  // has not been incremented.  Note: If the parent window has
  // been unloaded (_pprUnloaded), this must be a refresh, so
  // do nothing.
  if ((parent._pprRequestCount <= 0) && !parent._pprUnloaded)
  {
    // This should be a no-op, but just in case...
    _pprStopBlocking(parent);

    if (!(_agent.isIE) && (parent.document.referrer != null))
    {
      // If we have a referrer, it's safer to jump to it than to try to
      // figure out how deep we are in the history list.
      // Although Microsoft claims that history.go takes either an integer or a
      // string, it appears that only the integer argument works.
      parent.history.go(parent.document.referrer);
    }
    else
    {
      var backOffset = -1;

      if (_agent.isIE)
      {
        // IE's history mechanism never seems to grab the first submit unless
        // some other action has taken place...
        if (parent._pprSomeAction)
        {
          backOffset = -(parent._pprSubmitCount);
        }
      }
      else if (parent._pprSubmitCount && (parent._pprSubmitCount > 0))
      {
        // On Mozilla, the submitCount will be accurate
        backOffset -= parent._pprSubmitCount;
      }

      // we're about to jump back, reset the submit (page) count.
      parent._pprSubmitCount = 0;
      parent._pprSomeAction = false;

      // Only jump back if the offset is actually negative
      if (backOffset < 0)
      {
        parent.history.go(backOffset);
      }
    }
  }
}


// Finds the node under the specified target item
// to which the focus should be restored.
// If the targetItem contains an element with the same
// id or name as the old activeElement, we set
// the focus to this item.  Otherwise we set the focus
// to the first focusable item.
function _getNewActiveElement(targetDocument, targetItem, oldActiveElement)
{
  // First check to see if we have a new element with the same
  // ID as the old active element.
  if (oldActiveElement.id)
  {
    var newActiveElement = _getElementById(targetDocument,
                                           oldActiveElement.id);
    if (_isFocusable(newActiveElement))
      return newActiveElement;
  }

  // =-=ags We should look for nodes with the same name

  return null;
}



/**
 * Onload event handler for the partial iframe
 */
function _partialChange(navigationFormName)
{
  // If we don't have an outstanding request, don't do
  // anything.  Technically we shouldn't see this case,
  // but this might come up if our back button/history
  // handling code fails.
  if (parent._pprRequestCount <= 0)
    return;

  // Update the request count
  parent._pprRequestCount--;
  parent._pprSomeAction = true;

  // We need to do some pre-processing... Fix up links
  if (navigationFormName)
    _fixAllLinks(navigationFormName, parent);

  // use source document, since Mozilla claims that
  // the document object no longer exists
  // when we call write and close at the end
  // of this method
  var sourceDocument = document;
  var targetDocument = parent.document;

  var oldActiveElement = _getParentActiveElement();
  var newActiveElement = null;

  // We want to keep track of whether we are restoring the focus
  // to the actual component which should have the focus, or just
  // to the first focusable component that we can find.
  var isFirstFocusable = false;

  for (var i = 0; i < _pprTargets.length; i++)
  {

    var targetID = _pprTargets[i];

    //
    // copy over the content
    //
    var sourceItem = _getElementById(sourceDocument, targetID);
    var targetItem = _getElementById(targetDocument, targetID);

    if (sourceItem && targetItem)
    {
      // If the old activeElement is a descendent of the node that
      // is being replaced, we'll need to restore the focus after
      // we are done replacing content.
      var restoreFocus = _isDescendent(oldActiveElement, targetItem);

      _setOuterHTML(targetDocument, targetItem, sourceItem);

      // If that activeElement was in the subtree that was just
      // replaced, figure out what the new activeElement should be.
      if ((restoreFocus) && (newActiveElement == null))
      {
        // Make sure we have a current reference to the target item.
        // It seems that the call _setOuterHTML() can cause our
        // targetItem reference to become invalid.  The end result
        // is that we are not able to successfully traverse the
        // subtree under targetItem.  Getting the targetItem from
        // the document after _setOuterHTML() has been called fixes
        // this problem.
        targetItem = _getElementById(targetDocument, targetItem.id);

        // Get the new element to set the focus to
        newActiveElement = _getNewActiveElement(targetDocument,
                                                targetItem,
                                                oldActiveElement);

        if (newActiveElement == null)
        {
          newActiveElement = _getFirstFocusable(targetItem);
          if (newActiveElement != null)
            isFirstFocusable = true;
        }
        // if we're here, we'll take care of resetting the focus,
        // no need for the unblock to do it.
        parent._pprEventElement = null;
      }
    }
  }

  _pprCopyObjectElement(sourceDocument, targetDocument);

  // Load the script libraries into target document
  _loadScriptLibraries(targetDocument);

  _saveScripts(targetDocument);

  // for BIBeans PPRBack support (bug 3650018) save the forms action.
  var lastFormAction = _getElementById(targetDocument,"_pprSaveFormAction");
  if(lastFormAction)
    lastFormAction.value = document.forms[0].action;


  // unblock
  _pprStopBlocking(parent);

  // Restore focus to new active element
  var req = _getRequestedFocusNode(parent);
  if (req != null)
    newActiveElement = req;

  _restoreFocus(newActiveElement, isFirstFocusable, targetDocument);
  _setRequestedFocusNode(null, null, false, parent);

  // Make sure form actions are up to date
  _updateFormActions(sourceDocument, targetDocument);

  // clear out the iframe content
// =-=ags For now, we avoid overwriting the iframe content, as the
//        additional write essentially breaks the history list on IE.
//        We should, however, investigate whether there is some way
//        to free up the memory used by the iframe without breaking
//        back button behavior
//  sourceDocument.write();
//  sourceDocument.close();

  if (_pprFirstClickPass || parent._pprFirstClickPass)
  {
    _eval(parent, "_submitFormCheck();");
  }
}

/**
 * Equivalent of targetItem.outerHTML = sourceItem.outerHTML for platforms
 * that don't support outerHTML, but do support innerHTML, or platforms that
 * don't support outerHTML on all elements
 */
function _setOuterHTML(
  targetDocument,
  targetItem,
  sourceItem
  )
{
  var sourceTagName = sourceItem.tagName;

  if (_agent.isIE || _agent.isSafari)
  {
    var useOuter = true;

    // is this a special IE leaf item that doesn't support outerHTML
    var isLeafItem = ((sourceTagName == "TD")      ||
                      (sourceTagName == "TH")      ||
                      (sourceTagName == "CAPTION"));

    // is this a special IE container item, that doesn't support
    // inner or outerHTML
    var isContainerItem = !isLeafItem &&
      ((sourceTagName == "COL")      ||
       (sourceTagName == "COLGROUP") ||
       (sourceTagName == "TR")       ||
       (sourceTagName == "TFOOT")    ||
       (sourceTagName == "THEAD")    ||
       (sourceTagName == "TBODY"));

    if (isLeafItem || isContainerItem)
    {
      // create an element with the correct attributes
      var newTargetItem = targetDocument.createElement(sourceTagName);

      // Safari has problems with some elements...
      if ((_agent.isSafari)
          && ((sourceTagName == "TR") || (sourceTagName == "TD")))
      {
        if (sourceTagName == "TD")
          newTargetItem.innerHTML = sourceItem.innerHTML;
        else
          targetItem.outerHTML = sourceItem.outerHTML;
      }
      else
        // mergeAttributes, including the id
        newTargetItem.mergeAttributes(sourceItem, false);

      // copy over the content
      if (isLeafItem)
      {
        newTargetItem.innerHTML = sourceItem.innerHTML;
      }
      else
      {
        if (isContainerItem)
        {
          //
          // add all of the cloned child elements
          // firstChild gets the comments as elements. we will
          // need to filter those out.
          //
          var currCell = sourceItem.firstChild;

          while (currCell != null)
          {
            // add the cloned cell.
            // filter out the comment tag.
            while(currCell != null && currCell.tagName == "!")
            {
              currCell = currCell.nextSibling;
            }

            if (currCell != null)
            {
              newTargetItem.appendChild(_setOuterHTML(targetDocument,
                                                      null,
                                                      currCell));
            }

            currCell = currCell.nextSibling;
          }
        }
      }

      // replace the current child with the new child
      if (targetItem)
      {
        if (targetItem["parentNode"])
          targetItem.parentNode.replaceChild(newTargetItem, targetItem);
      }
      else
      {
        targetItem = newTargetItem;
      }
      useOuter = false;
    }

    if (useOuter)
    {
      targetItem.outerHTML = sourceItem.outerHTML;
    }
  }
  else
  {
    // create the new Target item using a namespace to create this item
    // did not work correctly on Mozilla!
    var newTargetItem;

    // treat tr differently, since there is a Mozilla bug regarding innerHTML
    // and tr. It seems to strip out all the tds.
    if (sourceTagName != 'TR')
    {
      newTargetItem = targetDocument.createElement(sourceTagName);

      // Mozilla has a bug with copying innerHTML for the select element (#100175)
      // and it also has a problem when we use cloneNode (the scripts that
      // are in the iframe that get eval'd sometimes don't seem to get eval'd
      // correctly, and we get a missing function error). So, this is what
      // we came up with to work around that.
      if (sourceTagName == 'SELECT')
      {
        // the multiple attribute does not get copied over when we copy over
        // the source attributes, so do it here. This way when we set the
        // selected options below, multiple selections will be allowed.
        if (sourceItem.multiple)
        {
          newTargetItem.multiple = sourceItem.multiple;
        }
        for (var i = 0; i< sourceItem.options.length; i++)
        {
          var sourceOption = sourceItem.options[i];
          var newOption = new Option();

          newOption.value = sourceOption.value;
          newOption.text = sourceOption.text;
          newOption.selected = sourceOption.selected;


          newTargetItem.options[i] = newOption;
        }


      }
      else
      {
        // copy over the inner html
        var sourceInnerHTML = sourceItem.innerHTML;

        if ((sourceInnerHTML != null) && (sourceInnerHTML.length > 0))
        {

          newTargetItem.innerHTML = sourceItem.innerHTML;

        }

      }

      //
      // set the attributes of the source node
      //
      var sourceAttrs = sourceItem.attributes;

      for (var i = 0; i < sourceAttrs.length; i++)
      {
        newTargetItem.setAttribute(sourceAttrs[i].name, sourceAttrs[i].value);
      }


    }
    else
    {
      // for tr, use the importNode function. I'm limiting its
      // use to tr in case it is buggy, and to reduce the cases
      // where it is used. It might be a good thing to use in
      // the future. =-=jmw
      newTargetItem = targetDocument.importNode(sourceItem, true);

    }



    // Replace the current child with the new child.
    // We would like replaceChild(), but this causes problems on
    // Mozilla.  For example, in the PPR validation demo, if we use
    // replaceChild(), the validation demo contents disappear!  Using
    // insertBefore() followed by removeChild() seems to work just
    // fine though...
    // VG - replaceChild seems to work correctly with Gecko 1.5
     targetItem.parentNode.replaceChild(newTargetItem, targetItem);

    //targetItem.parentNode.insertBefore(newTargetItem, targetItem);
    //targetItem.parentNode.removeChild(targetItem);


  }

  return targetItem;
}


// Called by _partialChange() to ensure that the form action
// attributes are update to date.
function _updateFormActions(sourceDocument, targetDocument)
{
  var sourceForms = sourceDocument.forms;

  // Loop through all of the forms in the iframe, checking
  // the action attribute against the corresponding forms
  // in the parent window.  If any action attributes are
  // out of date, set the new action on the form in the
  // parent window.
  for (var i = 0; i < sourceForms.length; i++)
  {
    var sourceForm = sourceForms[i];

    // The partial page response may include forms which
    // have not had their contents updated.  We only care
    // about forms that contain partial targets...
    if (sourceForm.hasChildNodes())
    {
      // Get the source form's name and action
      var sourceName = sourceForm.name;
      var sourceAction = sourceForm.action;

      // Locate the target form
      var targetForm = targetDocument.forms[sourceName];

      if (targetForm)
      {
        var targetAction = targetForm.action;

        // If the target form's action is out of date,
        // set it to the new action.
        if (targetAction != sourceAction)
          targetForm.action = sourceAction;
      }
    }
  }
}

// This function is executed by _getParentActiveElement() to
// save away the activeElement in a well known location where
// it can be accessed by the PPR iframe
function _saveActiveElement()
{
  if (window._pprEventElement)
    window._pprActiveElement = window._pprEventElement;
  else if (document.activeElement)
    window._pprActiveElement = document.activeElement;
  else
    window._pprActiveElement = null;
}



// Called by _partialChange to get the parent window's
// active element.
function _getParentActiveElement()
{
  // This is more complicated then it should be.  We should be
  // able to access the activeElement through parent.activeElement,
  // but when we do that IE gives us a non-readable node - and we
  // we can't get at any properties that we need, like id, parentNode,
  // etc...  This seems to be some sort of security restriction - it
  // is only a problem when we try to access the parent window's
  // active element from a child iframe.  This isn't a problem
  // if we access the local window's activeElement.
  //
  // Luckily we have a work around.  Since the parent
  // window can get at its own activeElement, we execute a script
  // in the parent window which saves away the active element in
  // a well known variable.  Then, we can access the activeElement
  // and all of its properties through this variable.
  //
  // This, obviously, only works on platforms where activeElement is supported.

  if (parent.document.activeElement)
  {
    _eval(parent, "_saveActiveElement()");
    return parent._pprActiveElement;
  }

  return null;
}

// For PPR Back issue:
// save the scripts and libraries with every partial page response
// so that the scripts and libraries can be re-loaded when the user
// comes back to the page after navigating off of it. (this is called
// from _partialChange())
function _saveScripts(targetDocument)
{
  // the PPR Back Button support only works for IE.
  if (!_agent.isIE)
    return;

  // save inline scripts by appending them to the previous saved scripts.
  var saveScriptElement= _getElementById(targetDocument, "_pprSaveScript");

  if (saveScriptElement != null)
  {
    var _pprScripts = _getCommentedScript(document, "_pprScripts");
    saveScriptElement.value =
      saveScriptElement.value + " " + _pprScripts;
  }

  // save the library by appending to the already saved libraries
  // if it isn't already there.
  var saveLibrariesElement = _getElementById(targetDocument, "_pprSaveLib");

  if (saveLibrariesElement != null && (window["_pprLibraries"] != (void 0)))
  {

    for (var i = 0; (i < _pprLibraries.length); i++)
    {

      if (saveLibrariesElement.value.indexOf(_pprLibraries[i]) == -1)
      {
        if (saveLibrariesElement.value != "")
         saveLibrariesElement.value += "," + _pprLibraries[i];
        else
          saveLibrariesElement.value += _pprLibraries[i];
      }
    }
  }
}

// Fires a partial change event to the specified URL
function _firePartialChange(baseURL)
{
    var srcURL = _addParameter(baseURL,
                               _getPartialParameter(),
                               "true");

    // For now, we just use a single shared iframe "_pprIFrame".
    var iframe = _getElementById(document, _pprIframeName);

    // Before we fire, update the request count
    _pprRequestCount++;

    // and block all mouse clicks until the update is done
    _pprStartBlocking(window);

    if (_agent.isIE)
    {
      // iframe.src = srcURL;
      //iframe.ownerDocument.location.replace(srcURL);
      //iframe.document.location.replace(srcURL);

      // ie 5.5 way of doing this
      iframe.contentWindow.location.replace(srcURL);
    }
    else
    {
      // do this a slightly more confusing way for Mozilla
      //_dump(iframe.contentDocument.location);

      iframe.contentDocument.location.replace(srcURL);
    }
}
