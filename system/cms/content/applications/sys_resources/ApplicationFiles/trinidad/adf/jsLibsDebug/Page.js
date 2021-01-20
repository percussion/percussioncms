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
function TrPage()
{
  this._loadedLibraries = TrPage._collectLoadedLibraries();
  this._requestQueue = new TrRequestQueue(window);
}


/**
 * Get the shared instance of the page object.
 */
TrPage.getInstance = function()
{
  if (TrPage._INSTANCE == null)
    TrPage._INSTANCE = new TrPage();

  return TrPage._INSTANCE;
}

/**
 * Return the shared request queue for the page.
 */
TrPage.prototype.getRequestQueue = function()
{
  return this._requestQueue;
}

/**
 * Post the form for partial postback.  Supports both standard AJAX
 * posts and, for multipart/form posts, IFRAME-based transmission.
 * @param actionForm{FormElement} the HTML form to post
 * @param params{Object} additional parameters to send
 * @param headerParams{Object} HTTP headers to include (ignored if 
 *   the request must be a multipart/form post)
 */
TrPage.prototype.sendPartialFormPost = function(
  actionForm,
  params,
  headerParams)
{
  this.getRequestQueue().sendFormPost(
    this, this._requestStatusChanged,
    actionForm, params, headerParams);
}

TrPage.prototype._requestStatusChanged = function(requestEvent)
{
  if (requestEvent.getStatus() == TrXMLRequestEvent.STATUS_COMPLETE)
  {
    var statusCode = requestEvent.getResponseStatusCode();
    
    // The server might not return successfully, for example if an
    // exception is thrown.  When that happens, a non-200 (OK) status
    // code is returned as part of the HTTP prototcol.
    if (statusCode == 200)
    {
      // TODO: clean this up
      if (!_agent.isPIE)
      {
        _pprStopBlocking(window);
      }

      if (requestEvent.isPprResponse())
      {
        var responseDocument = requestEvent.getResponseXML();
        this._handlePprResponse(responseDocument.documentElement);
      }
      else
      {
        // Should log some warning that we got an invalid response
      }
    }
    else if (statusCode >= 400)
    {
      // The RequestQueue logs these for us, so
      // we don't need to take action here.  IMO, that's probably
      // wrong - we should do the handling here

      // TODO: clean this up
      if (!_agent.isPIE)
      {
        _pprStopBlocking(window);
      }

    }
  }                             
}

TrPage.prototype._handlePprResponse = function(documentElement)
{
  var rootNodeName = TrPage._getNodeName(documentElement);
  
  if (rootNodeName == "content")
  {
    // Update the form action
    this._handlePprResponseAction(documentElement);

    var childNodes = documentElement.childNodes;
    var length = childNodes.length;
    
    for (var i = 0; i < length; i++)
    {
      var childNode = childNodes[i];
      var childNodeName = TrPage._getNodeName(childNode);
      
      if (childNodeName == "fragment")
      {     
        this._handlePprResponseFragment(childNode);  
      }
      else if (childNodeName == "script")
      {
        this._handlePprResponseScript(childNode);
      }
      else if (childNodeName == "script-library")
      {
        this._handlePprResponseLibrary(childNode);
      }
    }
  }
  else if (rootNodeName == "redirect")
  {
    var url = TrPage._getTextContent(documentElement);
    // TODO: fix for portlets???
    window.location.href = url;
  }
  else if (rootNodeName == "error")
  {
    var nodeText = TrPage._getTextContent(documentElement);
    // This should not happen - there should always be an error
    // message
    if (nodeText == null)
      nodeText = "Unknown error during PPR";
    alert(nodeText);
  }  
  else if (rootNodeName == "noop")
  {
    // No op
  }
  else
  {
    // FIXME: log an error
  }
}

// TODO move to agent code
TrPage._getNodeName = function(element)
{
  var nodeName = element.nodeName;
  if (!nodeName)
    nodeName = element.tagName;
  return nodeName;
}


// Update the form with the new action provided in the response
TrPage.prototype._handlePprResponseAction = function(contentNode)
{
  var action = contentNode.getAttribute("action");

  if (action)
  {
    var doc = window.document;    

    // Replace the form action used by the next postback
    // Particularly important for PageFlowScope which might
    // change value of the pageflow scope token url parameter.    
    // TODO: track submitted form name at client, instead of
    // just updating the first form
    doc.forms[0].action = action;
  }

  // TODO: support Portal
}

// Handles a single fragment node in a ppr response.
TrPage.prototype._handlePprResponseFragment = function(fragmentNode)
{
  // Convert the content of the fragment node into an HTML node that
  // we can insert into the document
  var sourceNode = this._getFirstElementFromFragment(fragmentNode);

  // In theory, all fragments should have one element with an ID.
  // Unfortunately, the PPRResponseWriter isn't that smart.  If
  // someone calls startElement() with the write component, but never
  // passed an ID, we get an element with no ID.  And, even
  // worse, if someone calls startElement() with a <span> that
  // never gets any attributes on it, we actually strip that
  // span, so we can get something that has no elements at all!
  if (!sourceNode)
     return;

  // Grab the id of the source node - we need this to locate the
  // target node that will be replaced
  var id = sourceNode.getAttribute("id");
  // As above, we might get a node with no ID.  So don't crash
  // and burn, just return.
  if (!id)
    return;

  // assert((id != null), "null id in response fragment"); 

  // Find the target node
  var doc = window.document;
  var targetNode = doc.getElementById(id);
  var activeNode = _getActiveElement();
  var refocusId = null;
  if (activeNode && TrPage._isDomAncestorOf(activeNode, targetNode))
    refocusId = activeNode.id;
  
  if (targetNode == null)
  {
    // log.severe("unable to locate target node: " + id);
  }
  else
  {
    // replace the target node with the new source node
    targetNode.parentNode.replaceChild(sourceNode, targetNode);
  }  

  // TODO: handle nodes that don't have ID, but do take the focus?
  if (refocusId)
  {
    activeNode = doc.getElementById(refocusId);
    if (activeNode && activeNode.focus)
    {
      activeNode.focus();
      window._trActiveElement = activeNode;
    }
  }
}


/**
 * Return true if "parent" is an ancestor of (or equal to) "child"
 */
TrPage._isDomAncestorOf = function(child, parent)
{
  while (child)
  {
    if (child == parent)
      return true;
    var parentOfChild = child.parentNode;
    // FIXME: in DOM, are there ever components whose
    // parentNode is themselves (true for window objects at times)
    if (parentOfChild == child)
      break;
    child = parentOfChild;
  }
  
  return false;
}


/**
 * Replaces the a dom element contained in a peer. 
 * 
 * @param newElement{DOMElement} the new dom element
 * @param oldElement{DOMElement} the old dom element
 */
TrPage.prototype.__replaceDomElement = function(newElement, oldElement)
{
  oldElement.parentNode.replaceChild(newElement, oldElement);
}
  
// Extracts the text contents from a rich response fragment node and 
// creates an HTML element for the first element that is found.
TrPage.prototype._getFirstElementFromFragment = function(fragmentNode)
{
  // Fragment nodes contain a single CDATA section
  var fragmentChildNodes = fragmentNode.childNodes;
  // assert((fragmentChildNodes.length == 1), "invalid fragment child count");

  var cdataNode = fragmentNode.childNodes[0];
  // assert((cdataNode.nodeType == 4), "invalid fragment content");
  // assert(cdataNode.data, "null fragment content");

  // The new HTML content is in the CDATA section.
  // TODO: Is CDATA content ever split across multiple nodes?
  var outerHTML = cdataNode.data;

  // We get our html node by slamming the fragment contents into a div.
  var doc = window.document;  
  var div = doc.createElement("div");

  // Slam the new HTML content into the div to create DOM
  div.innerHTML = outerHTML;
  
  return TrPage._getFirstElementWithId(div);
  
}

// Returns the first element underneath the specified dom node
// which has an id.
TrPage._getFirstElementWithId = function(domNode)
{

  var childNodes = domNode.childNodes;
  var length = childNodes.length;
  
  for (var i = 0; i < length; i++)
  {
    var childNode = childNodes[i];
 
    // Check for ELEMENT nodes (nodeType == 1)   
    if (childNode.nodeType == 1)
    {
      if (childNode.id)
        return childNode;
        
      return TrPage._getFirstElementWithId(childNode);
    }
  }
  
  return null;
}

TrPage.prototype._loadScript = function(source)
{
  // Make sure we only load each library once
  var loadedLibraries = this._loadedLibraries;
  if (loadedLibraries[source])
    return;
    
  loadedLibraries[source] = true;
  var xmlHttp = new TrXMLRequest();
  xmlHttp.setSynchronous(true);
  xmlHttp.send(source, null);
  if (xmlHttp.getStatus() == 200)
  {
    var responseText = xmlHttp.getResponseText();
    if (responseText)
    {
      if (_agent.isIE)
        window.execScript(responseText);
      else
        window.eval(responseText);
    }
  }

  // Clean to prevent memory leak
  xmlHttp.cleanup();
}

// Handles a single script node in a rich response
TrPage.prototype._handlePprResponseScript = function(scriptNode)
{
  var source = scriptNode.getAttribute("src");
  if (source)
  {
    this._loadScript(source);
  }
  else
  {
    var nodeText = TrPage._getTextContent(scriptNode);
    if (nodeText)
    {
      if (_agent.isIE)
        window.execScript(nodeText);
      else
        window.eval(nodeText);
    }
  }
}

TrPage.prototype._handlePprResponseLibrary = function(scriptNode)
{
  var nodeText = TrPage._getTextContent(scriptNode);
  this._loadScript(nodeText);
}

// TODO: move to agent API
TrPage._getTextContent = function(element)
{
  if (_agent.isIE)
  {
    // NOTE: this only works if it is an element, not some other DOM node
    var textContent = element.innerText;
    if (textContent == undefined)
      textContent = element.text;
        
    return textContent;
  }

  // Safari doesn't have "innerText", "text" or "textContent" - 
  // (at least not for XML nodes).  So sum up all the text children
  if (_agent.isSafari)
  {
    var text = "";
    var currChild = element.firstChild;
    while (currChild)
    {
      var nodeType = currChild.nodeType;
      if ((nodeType == 3) || (nodeType == 4))
        text = text + currChild.data;
      currChild = currChild.nextSibling;
    }

    return text;
  }

  return element.textContent;
}

TrPage._collectLoadedLibraries = function()
{
  var loadedLibraries = new Object();

  // We use document.getElementsByTagName() to locate all scripts
  // in the page.  In theory this could be slow if the DOM is huge,
  // but so far seems extremely efficient.
  var domDocument = window.document;
  var scripts = domDocument.getElementsByTagName("script");

  for (var i = 0; i < scripts.length; i++)
  {
    // Note: we use node.getAttribute("src") instead of node.src as
    // FF returns a fully-resolved URI for node.src.  In theory we could
    // fully resolve/normalize all script src values (both here and 
    // in rich responses), but this seems like overkill.  Instead, we
    // just use whatever value happens to show up in the HTML src attribute,
    // whether it is fully resolved or not.  In theory this could mean that
    // we could evalute a library an extra time (if it appears once fully
    // resolved and another time as a relative URI), but this seems like 
    // an unlikely case which does not warrant extra code.
    var src = scripts[i].getAttribute("src");

    if (src)
      loadedLibraries[src] = true;
  }
  
  return loadedLibraries;  
}

 
/**
 * Adds the styleClassMap entries to the existing internal
 * styleClassMap. Styles can then be accessed via the 
 * getStyleClass function.
 * @param styleClassMap() {key: styleClass, ...}
 */
TrPage.prototype.addStyleClassMap = function(styleClassMap)
{
  if (!styleClassMap)
    return;

  if (!this._styleClassMap)
    this._styleClassMap = new Object();

  // Copy key:styleClass pairs to internal map
  for (var key in styleClassMap)
    this._styleClassMap[key] = styleClassMap[key];
}
 
/**
 * Return the styleClass for the given key.
 * @param key(String) Unique key to retrieve the styleClass
 * @return (String) The styleClass, or undefined if not exist
 */
TrPage.prototype.getStyleClass = function(key)
{
  if (key && this._styleClassMap)
  {
    var mapped = this._styleClassMap[key];
    if (mapped)
      return mapped;
  }

  return key;
}
