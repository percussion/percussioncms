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
 * Iframe base Data Transfer Request Event class. This object is passed back to the listeners
 * of a Data Transfer Service Request. This object and TrXMLRequestEvent 
 * support ITrXMLRequestEvent pseudo-interface
 * @ see TrXMLRequestEvent
 */
function TrIFrameXMLRequestEvent(
  iframeDoc)
{
  this._iframeDoc = iframeDoc;
}


TrIFrameXMLRequestEvent.prototype.getStatus = function()
{
  // If we go this far the transfer is complete
  return TrXMLRequestEvent.STATUS_COMPLETE;
}


/**
* Returns the response of the Data Transfer Request as an XML document
* NOTE: this method is valid only for TrXMLRequestEvent.STATUS_COMPLETE
**/
TrIFrameXMLRequestEvent.prototype.getResponseXML = function()
{
  var agentIsIE = _agent.isIE;
  var iframeDoc = this._iframeDoc;
  if(agentIsIE && iframeDoc.XMLDocument)
    return iframeDoc.XMLDocument;
  else
    return iframeDoc;
}


/**
* Returns the response of the Data Transfer Request as text.
* NOTE: this method is valid only for TrXMLRequestEvent.STATUS_COMPLETE
**/
TrIFrameXMLRequestEvent.prototype.getResponseText = function()
{  
  var agentIsIE = _agent.isIE;
  var iframeDoc = this._iframeDoc, xmlDocument = null;

  if(agentIsIE && iframeDoc.XMLDocument)
    xmlDocument = iframeDoc.XMLDocument;
  else if(window.XMLDocument && (iframeDoc instanceof XMLDocument))
    xmlDocument = iframeDoc;
    
  if(xmlDocument)
    return AdfAgent.AGENT.getNodeXml(xmlDocument);
  else
    return iframeDoc.documentElement.innerHTML;
}

TrIFrameXMLRequestEvent.prototype._isResponseValidXML = function()
{
  var agentIsIE = _agent.isIE;
  var iframeDoc = this._iframeDoc;

  if(agentIsIE && iframeDoc.XMLDocument)
    return true;
  else if(window.XMLDocument && (iframeDoc instanceof XMLDocument))
    return true;
  else
    return false;
}

/**
* Returns the status code of the xml http Data Transfer Request.
* NOTE: this method is valid only for TrXMLRequestEvent.STATUS_COMPLETE
**/
TrIFrameXMLRequestEvent.prototype.getResponseStatusCode = function()
{
  // if we got till here using Iframe we must have document so return 200
  return 200;
}

/**
* Returns if whether if is a rich response
* NOTE: this method is valid only for TrXMLRequestEvent.STATUS_COMPLETE
**/
TrIFrameXMLRequestEvent.prototype.isPprResponse = function()
{
  var agentIsIE = _agent.isIE;
  var iframeDoc = this._iframeDoc;
  var isRichReponse = false;
  
  // Look for "Adf-Rich-Response-Type" PI
  if(agentIsIE && iframeDoc.XMLDocument)
  {
    var xmlDocument = iframeDoc.XMLDocument, childNodes = xmlDocument.childNodes;
    // In IE the xml PI is the first node
    if(childNodes.length >= 2 && childNodes[1].nodeName ==  "Tr-XHR-Response-Type")
      isRichReponse = true;
  }
  else
  {
    if(iframeDoc.firstChild && iframeDoc.firstChild.nodeName ==  "Tr-XHR-Response-Type")
      isRichReponse = true;
  }
  
  return isRichReponse;
}

/**
* Returns if whether if is a rich response
* NOTE: this method is valid only for TrXMLRequestEvent.STATUS_COMPLETE
**/
TrIFrameXMLRequestEvent.prototype.getResponseContentType = function()
{
  if(this._isResponseValidXML())
    return "text/xml";
    
  return "text/html";
}