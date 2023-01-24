/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.server.agent;

/**
 * This interface defines the DTD for the agent handler response document 
 * that a user will get when he initiates an agent action. 
 * Also defined in this class are all string constants that are required by the 
 * implementin of the class.
 * 
 * <P>
 * &lt;?xml version="1.0" encoding="UTF-8"&gt;<br>
 * &lt;!DOCTYPE psxagent:agentmanagerresponse[<br>
 * &lt;!ELEMENT psxagent:agentmanagerresponse (response, status?) &gt;<br>
 * &lt;!ATTLIST psxagent:agentmanagerresponse editionid #REQUIRED &gt;<br>
 * &lt;!ATTLIST psxagent:agentmanagerresponse type NMTOKEN error|info|status &gt;<br>
 * &lt;!ATTLIST psxagent:agentmanagerresponse xmlns:psxagent CDATA #FIXED 
 * "urn:www.percussion.com/agentmanager" &gt;<br>
 * &lt;!ELEMENT response (#PCDATA) &gt;<br>
 * &lt;!ATTLIST response code #REQUIRED type NMTOKEN 
 * noPage|noAction|noEditionId|inProgress|notInProgress|publish
 * |stop|unknownAction &gt;<br>
 * &lt;!ELEMENT status (#PCDATA) &gt;<br>
 * &lt;!ATTLIST status clistindex #REQUIRED &gt;<br>
 * &lt;!ATTLIST status clistcount #REQUIRED &gt;<br>
 * ]&gt;<br>
 * &lt;psxagent:agentmanagerresponse xmlns:psxagent="urn:www.percussion.com/agentmanager" 
 * editionid="4" type="status"&gt;<br>
 * &lt;response code="notInProgress"&gt;Edition is not in progress
 * &lt;/response&gt;<br>
 * &lt;status clistindex="" clistcount="" /&gt;<br>
 * &lt;/psxagent:agentmanagerresponse&gt;<br>
 */
public interface IPSDTDAgentHandlerResponse
{
   /**
    * DOM elements
    */
   static public final String ELEM_ROOT = "psxagent:agentmanagerresponse";
   static public final String ELEM_RESPONSE = "response";
   static public final String ELEM_STATUS = "status";

   /**
    * DOM element attributes
    */
   static public final String ATTR_NS = "xmlns:psxagent";
   static public final String ATTR_TYPE = "type";
   static public final String ATTR_CODE = "code";
   static public final String ATTR_ACTION = "action";


   /**
    * Response types. ATTR_TYPE above will have one of these values .
    */
   static public final String RESPONSE_TYPE_ERROR = "error";
   static public final String RESPONSE_TYPE_INFO = "info";
   static public final String RESPONSE_TYPE_STATUS = "status";
   static public final String RESPONSE_TYPE_RESULT = "result";

   /**
    * Response code values possible. ATTR_CODE of the ELEM_RESPONSE element 
    * will  get one of these values.
    */
   static public final String RESPONSE_CODE_NOPAGE = "noPage";
   static public final String RESPONSE_CODE_NOACTION = "noAction";
   static public final String RESPONSE_CODE_INPROGRESS = "inProgress";
   static public final String RESPONSE_CODE_NOTINPROGRESS = "notInProgress";
   static public final String RESPONSE_CODE_INVALIDACTION =
      "unknownAction";

  /**
   * List f Publisher Handler actions supported. One of these actions is 
   * supplied as an HTML parameter in handler request.
   */
   static public final String HANDLER_ACTION_STOP = "stop";
   static public final String HANDLER_ACTION_STATUS = "status";

   /**
    * Some other string constants.
    */
   static public final String HANDLER_PAGE = "agenthandler";
   static public final String MANAGER_PAGE = "agentmanager";
   static public final String HANDLER_PARAM_ACTION = "rxagentaction";
   static public final String HANDLER_PARAM_AGENT_NAME = "rxagentname";
}
