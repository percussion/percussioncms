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
 
// Called by the renderer to register the message box 
// and create an associated TrMessageBox instance
TrMessageBox._registerMessageBox = function(messageBoxId)
{
  if (!TrMessageBox._MESSAGE_BOX)
    TrMessageBox._MESSAGE_BOX = new TrMessageBox(messageBoxId);
}

/**
 * Adds a message to the message box for a given input.  The
 * label is used as the anchor text for rendering a link to
 * the given input.  Also shows the message box if it was previously
 * empty.
 **/
TrMessageBox.addMessage = function(inputId, label, facesMessage)
{
  var messageBox = TrMessageBox._MESSAGE_BOX;
  if (!messageBox)
    return;

  messageBox.addMessage(inputId, label, facesMessage);
}

/**
 * Removes any messages that are shown for the given input.
 * Also hides the message box if there are no.
 **/
TrMessageBox.removeMessages = function(inputId)
{
  var messageBox = TrMessageBox._MESSAGE_BOX;
  if (!messageBox)
    return;

  messageBox.removeMessages(inputId);
}

/**
 * Checks if a tr:messages component is present on-screen.
 **/
TrMessageBox.isPresent = function()
{
  return (TrMessageBox._MESSAGE_BOX) ? true : false;
}

function TrMessageBox(messageBoxId)
{
  if (messageBoxId == undefined)
    return;

  //define object properties
  this._messageBoxId = messageBoxId;
  
  
  // Change 'link' this once skin style is made public
  TrMessageBox._LINK_STYLE = TrPage.getInstance().getStyleClass("OraLink");
  TrMessageBox._LIST_STYLE = TrPage.getInstance().getStyleClass("af|messages::list");
  TrMessageBox._LIST_SINGLE_STYLE = TrPage.getInstance().getStyleClass("af|messages::list-single");
}

/**
 * Adds a message to the MessageBox and makes it visible if hidden.
 **/
TrMessageBox.prototype.addMessage = function(inputId, label, facesMessage)
{
  var listElement = this._getMessageList();
  
  // Create a new line element
  var line = document.createElement("li");
  
  if (inputId)
  {
    // Use summary text if label not available
    if (!label)
      label = facesMessage.getSummary();    

    // Create a clickable anchor
    var anchor = document.createElement("a");
    anchor.className = TrMessageBox._LINK_STYLE;
    anchor.href = "#" + inputId;
    anchor.innerHTML = label;
    line.appendChild(anchor);
  
    // Give it a name we can remember so we can remove it later
    line.name = this._getMessageNameForInput(inputId);
    
    // Populate the text on the line
    var textNode = document.createTextNode(" - " + facesMessage.getSummary());
    line.appendChild(textNode);
  }
  else
  {
    // Treat message as global message, which can't be individually removed

    // Populate the text on the line
    var textNode = document.createTextNode(facesMessage.getSummary() + " - " + facesMessage.getDetail());
    line.appendChild(textNode);
  }

  // Add line element to the list
  listElement.appendChild(line);
  
  if (listElement.hasChildNodes())
  {
    var children = listElement.getElementsByTagName("li");
    
    if (children.length == 1)
      // Set the style for a list of only 1 entry
      listElement.className = TrMessageBox._LIST_SINGLE_STYLE;
    else
      // Set the style for a multi-entry list
      listElement.className = TrMessageBox._LIST_STYLE;
  }
  
  this._showMessageBox();
}

/**
 * Clears all messages from the MessageBox and hides it if visible.
 **/
TrMessageBox.prototype.removeMessages = function(inputId)
{
  var listElement = this._getMessageList();
  
  if (!listElement.hasChildNodes())
    return;
    
  var lineName = this._getMessageNameForInput(inputId);
  
  // Get the child 'li' elements
  var children = listElement.getElementsByTagName("li");
  for (var i = 0; i < children.length; ) 
  {
    var child = children[i];
    
    // if name matches the one we gave it in addMessage()
    if (child.name && child.name == lineName)
    {
      listElement.removeChild(child);
      // List is live, so it will re-index after removeChild
      // so don't increment index
      continue;
    }
    i++;
  }

  // Hide the MessageBox if empty  
  if (children.length == 0)
    this._hideMessageBox();
  else if (children.length == 1)
    // Set the style for a list of only 1 entry
    listElement.className = TrMessageBox._LIST_SINGLE_STYLE;
  else
    // Set the style for a multi-entry list
    listElement.className = TrMessageBox._LIST_STYLE;
}

TrMessageBox.prototype._getMessageBox = function()
{
  if (this._messageBoxId == null)
    return null;
  return _getElementById(document, this._messageBoxId);
}

TrMessageBox.prototype._getMessageList = function()
{
  if (this._messageBoxId == null)
    return null;
  return _getElementById(document, this._messageBoxId + "__LIST__");
}

TrMessageBox.prototype._showMessageBox = function()
{
  var messageBox = this._getMessageBox();
  if (!messageBox)
    return;

  messageBox.style.display = "";        
}

TrMessageBox.prototype._hideMessageBox = function()
{
  var messageBox = this._getMessageBox();
  if (!messageBox)
    return;

  messageBox.style.display = "none";        
}

// Generates a name for this message based on the id of the messagebox
// and the input it relates to.
TrMessageBox.prototype._getMessageNameForInput = function(inputId)
{
  if (!this._messageBoxId || !inputId)
    return null;
  return this._messageBoxId + "__" + inputId + "__";
}
