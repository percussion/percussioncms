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

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This class encapsulates the agent handler response XML document that is
 * defined in the interface <code>IPSDTDAgentHandlerResponse</code>. The
 * document is created in the constructor itself.
 */
public class PSAgentHandlerResponse implements IPSAgentHandlerResponse
{
   /**
    * Default constructor. Creates the empty XML document with DTD defined.
    */
   public PSAgentHandlerResponse()
   {
      initXMLDocument();
   }

   /**
    * This method creates the XML document as per the DTD defined.
    */
   private void initXMLDocument()
   {
      m_Document = PSXmlDocumentBuilder.createXmlDocument();

      m_Root = (Element)m_Document.appendChild(
                  m_Document.createElement(ELEM_ROOT));
      m_Root.setAttribute(ATTR_NS, PSUtils.NS_URI_PERCUSSION_AGENT);
      m_Response = PSXmlDocumentBuilder.addElement(m_Document, m_Root,
         ELEM_RESPONSE, "");
   }

   /**
    * Access function for the XML DOM document for the agent request handler
    * response.
    *
    * @return response document DOM Document, never <code>null</code>.
    *
    */
   public Document getDocument()
   {
      return m_Document;
   }

   /*
    * see IPSAgentHandlerResponse for documentation
    */
   public Node getResponseNode()
   {
      return m_Response;
   }

   /**
    * Method to set the action for the response.
    *
    * @param action name for the response, must not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is <code>null</code>.
    */
   public void setAction(String action)
   {
      if(null == action || action.trim().length() < 1)
      {
         throw new IllegalArgumentException(
            "Action cannot be empty or null");
      }
      m_Root.setAttribute(ATTR_ACTION, action);
   }

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
   public void setResponse(Node result)
   {
      if(null == result)
         return;

      if(result instanceof Element ||
         result instanceof Text)
      {
         m_Response.appendChild(result);
      }
      else
      {
         throw new IllegalArgumentException(
            "Result node must be either a Text or Element type");
      }
   }

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
   public void setResponse(String type, String msg)
   {
      if(null == type)
      {
         throw new IllegalArgumentException(
            "Response type string cannot be null in setResponse");
      }
      if(null == msg)
      {
         throw new IllegalArgumentException(
            "Response message string cannot be null in setResponse");
      }
      setResponse(type, msg, null);
   }

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
   public void setResponse(String type, String msg, String code)
   {
      if(null == type)
      {
         throw new IllegalArgumentException(
            "Response type string cannot be null in setResponse");
      }
      if(null == msg)
      {
         throw new IllegalArgumentException(
            "Response message string cannot be null in setResponse");
      }

      if(null != code && code.trim().length() > 0)
      {
         m_Response.setAttribute(ATTR_CODE, code);
      }
      m_Root.setAttribute(ATTR_TYPE, type);
      Node node = m_Response.getFirstChild();
      if(null == node)
      {
         Text text = m_Document.createTextNode(msg);
         m_Response.appendChild(text);
      }
      else
      {
         ((Text)node).setData(msg);
      }
   }

   /**
    * Set the full path for the style sheet to use to render the response
    * document.
    * @param fullPath - full path name of the style sheet, must not be
    * <code>null</code> or <code>empty</code>.
    * @throws IllegalArgumentException if the argument is <code>null</code> or
    * <code>empty</code>.
    */
   public void setStyleSheet(String fullPath)
   {
      if(fullPath == null || fullPath.trim().length() < 1)
      {
         throw new IllegalArgumentException(
            "Stylesheet path must not be null or empty");
      }
      m_StyleSheetPath = fullPath;
   }

   /**
    * Access function that returns the full path of the style sheet to be used
    * to render the response document.
    * @return full path of the stylesheet set by the <code>setStyleSheet</code>
    * method. May be <code>null</code> if never set.
    */
   public String getStyleSheet()
   {
      return m_StyleSheetPath;
   }

   /**
    * The response XML document this class encapsulates. Method initXMLDocument
    * creates the basic template required.
    */
   Document m_Document = null;

   /**
    * Full path name of the response XSL style sheet to render the document.
    */
   String m_StyleSheetPath = null;

   /**
    * The root element of the response document for quick access.
    */
   Element m_Root = null;

   /**
    * The response element of the response document for quick access.
    */
   Element m_Response = null;

   /**
    * The status element of the response document for quick access.
    */
   Element m_Status = null;
}
