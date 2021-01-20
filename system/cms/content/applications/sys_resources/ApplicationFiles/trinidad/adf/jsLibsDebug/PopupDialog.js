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
function TrPopupDialog()
{
  var div = document.createElement("div");
  div.style.cssText = "visibility:hidden; position: absolute; z-index: 201;";
  
  //setup the title bar
  var titlebar = document.createElement("div");
  div.appendChild(titlebar);
  this._titleDiv = titlebar;

  //setup the content iframe
  var iframe = document.createElement("iframe");
  iframe.name = "_blank";
  iframe.frameBorder = "0";
  
  //hold the iframe so we can set the 'src' as needed.
  this._iframe = iframe;
    
  div.appendChild(iframe);
  document.body.appendChild(div);
  
  TrPanelPopup.call(this)
  this.setModal(true);
  this.setCentered(true);
  this.setContent(div);

  var page = TrPage.getInstance();
  div.className = page.getStyleClass("af|panelPopup::container");
  iframe.className = page.getStyleClass("af|panelPopup::content");
  titlebar.className = page.getStyleClass("af|panelPopup::title-text") + 
    ' ' + 
    page.getStyleClass("af|panelPopup::title-bar");
}

// TrPopupDialog inherits from TrPanelPopup
TrPopupDialog.prototype = new TrPanelPopup();

/**
 * Set the title bar text
 **/
TrPopupDialog.prototype.setTitle = function(title)
{
  if (title)
  {
    this._titleDiv.innerHTML = title;
    this._titleDiv.style.display = "block";
  }
  else
  {
    this._titleDiv.innerHTML = "";
    this._titleDiv.style.display = "none";
  }
}

TrPopupDialog.prototype.setDestination = function(url)
{
  this._iframe.src = url;
}

TrPopupDialog.prototype.setSize = function(width, height)
{
  if (width && height)
  {
    this._resizeIFrame(width, height);
  }
}

TrPopupDialog.getInstance = function()
{
  return TrPopupDialog.DIALOG;
}

/**
 * Resize the content area of the dialog
 **/
TrPopupDialog.prototype._resizeIFrame = function(width, height)
{
  this._iframe.height = height;
  this._iframe.width = width;
  this._calcPosition(false);
}

/**
 * Called from dialog page (usually body onload) to set the dialog title to
 * that of the current page, and handle any resizing that may be required.
 **/
TrPopupDialog._initDialogPage = function()
{
  var dialog;

  try
  {
    dialog = parent.TrPopupDialog.DIALOG;

  }
  catch(err)
  {
  }

  if (!dialog)
    return;

  dialog.setTitle(document.title);
    
  // Resize the dialog to the page content
  if (_agent.isIE)
  {
    dialog._resizeIFrame(
      dialog._iframe.Document.body.scrollWidth+40, 
      dialog._iframe.Document.body.scrollHeight+40);
  }
  else
  {
    dialog._resizeIFrame(
      dialog._iframe.contentDocument.body.offsetWidth+40, 
      dialog._iframe.contentDocument.body.offsetHeight+40);
  }

  dialog.show();
}

/*
 * This function handles the closure of the dialog.  Generally, it is
 * called via PPR.
 */
TrPopupDialog._returnFromDialog = function()
{
  var dialog = TrPopupDialog.DIALOG;
  if (dialog)
  {
    dialog.hide();
  }
  else
  {
    alert("returnFromDialog(): Error - Current popup is not a dialog");
  }

  // Clear the dialog instance
  TrPopupDialog.DIALOG = undefined;
}

/*
 * Callback function, invoked on close of dialog.  If necessary, this
 * function will make a submit to cause the return event to fire.
 * TODO - Move this function to another library - TrPage perhaps?
 */
TrPopupDialog._returnFromDialogAndSubmit = function(callbackProps, value)
{
  if (callbackProps)
  {
    var formName = callbackProps['formName'];
    var postbackId = callbackProps['postback'];

    _submitPartialChange(formName, 0, {rtrn:postbackId});
  }
}

TrPopupDialog._launchDialog = function(
  srcURL,
  dialogProps,
  callbackFunction,
  callbackProps)
{
  var dialog = TrPopupDialog.DIALOG;
  if (!dialog)
  {
    dialog = TrPopupDialog.DIALOG = new TrPopupDialog();
  }

  // Register callback on close of dialog
  dialog.callback = callbackFunction;
  dialog.callbackProps = callbackProps;

  // Dialog will auto-size to fit content unless specified
  if (dialogProps && dialogProps['width'] && dialogProps['height'])
    dialog.setSize(dialogProps['width'], dialogProps['height']);

  // Dialog will be opened by _initDialogPage() once dialog page has loaded
  // to prevent lots of up/down sizing when auto-sized.
  dialog.setDestination(srcURL);
}
