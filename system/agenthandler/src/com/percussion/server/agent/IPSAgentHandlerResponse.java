/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
