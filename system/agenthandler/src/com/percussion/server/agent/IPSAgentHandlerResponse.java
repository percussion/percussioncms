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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This interface defines methods that must be implemented by an agent handle
 * response class. The agent handler response is basically an XML document for
 * which the DTD is defined in the interface
 * <code>IPSDTDAgentHandlerResponse</code>. All these methods are basically to
 * set or get the appropriate values in the dicument.
 */
public interface IPSAgentHandlerResponse extends IPSDTDAgentHandlerResponse
{
   /**
    * Access function for the XML DOM document for the agent request handler
    * response.
    *
    * @return response document DOM Document, never <code>null</code>.
    *
    */
   Document getDocument();

   /**
    * Method to set the result element in the response document.
    *
    * @param response type string for the response. Must not be
    * <code>null</code>.
    *
    * @param message for the response. Must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any of the the arguments
    * is <code>null</code>.
    *
    */
   void setResponse(Node result);

   /**
    * Method to set the response data for the response document.
    *
    * @param response type string for the response. Must not be
    * <code>null</code>.
    *
    * @param message for the response. Must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any of the the arguments
    * is <code>null</code>.
    *
    */
   void setResponse(String type, String msg);

   /**
    * Method to set the response data for the response document.
    *
    * @param response type string for the response. Must not be
    * <code>null</code>.
    *
    * @param message for the response. Must not be <code>null</code>.
    *
    * @param code for the response. May be <code>null</code> or
    * <code>empty</code>.
    *
    * @throws IllegalArgumentException if any of response type or message
    * strings is <code>null</code>.
    *
    */
   void setResponse(String type, String msg, String code);

   /**
    * Access function that returns the full path of the style sheet to be used
    * to render the response document.
    * @return full path of the stylesheet set by the <code>setStyleSheet</code>
    * method. May be <code>null</code> if never set.
    */
   String getStyleSheet();

   /**
    * Set the full path for the style sheet to use to render the response
    * document.
    * @param fullPath - full path name of the style sheet, must not be
    * <code>null</code> or <code>empty</code>.
    * @throws IllegalArgumentException if the argument is <code>null</code> or
    * <code>empty</code>.
    */
   void setStyleSheet(String fullPath);

   /**
    * Helper method that returns the DOM node representing the response.
    */
   Node getResponseNode();
}
