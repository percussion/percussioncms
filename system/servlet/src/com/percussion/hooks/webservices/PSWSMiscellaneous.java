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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.hooks.webservices;

import java.util.Vector;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.SOAPContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class defines the actions associated with the Miscellaneous group
 * of web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSMiscellaneous extends PSWebServices
{
   public PSWSMiscellaneous()
      throws SOAPException
   {}

   /**
    * This operation is used to checkin a specific piece of content without
    * updating it’s data. Effectively an Undo Checkout, although there were
    * no changes to the content, the audit and history trails will include both
    * the checkout and checkin actions.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>CheckInRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void checkIn(Envelope env,
                       SOAPContext reqCtx,
                       SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("checkIn", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to checkout a specific piece of content.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>CheckOutRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void checkOut(Envelope env,
                        SOAPContext reqCtx,
                        SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("checkOut", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to set the revisino lock for the current revision.
    * By doing this any update to this content item will create a new revision.
    * The system sets this lock automatically when the item has gone public.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>LockRevisionRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void lockRevision(Envelope env,
                            SOAPContext reqCtx,
                            SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("lockRevision", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to log a user into the system and returns a
    * valid session.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>LoginRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void login(Envelope env,
                     SOAPContext reqCtx,
                     SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("login", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to log the current user out of the current session.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>LogoutRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void logout(Envelope env,
                      SOAPContext reqCtx,
                      SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("logout", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to call a custom application within the Rhythmyx
    * server, the application location is an attribute within the action element
    * that defines the application/resouce to be called.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>CallDirectRequest</code> element defined in
    *                     the sys_MiscellaneousParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    *  sent when the call direct action does not have the required
    *  customAppLocation attribute to tell the Rx server which
    *  application / resource to be called
    */
   public void callDirect(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      Body body = env.getBody();
      if (body != null)
      {
         Vector bodyEntries = body.getBodyEntries();
         if (bodyEntries.size() > 0)
         {
            // element 0 is the method to be called
            Element bodyEl = (Element)bodyEntries.elementAt(0);
            Element reqEl = getFirstElementChild(bodyEl);
            Element appEl = getFirstElementChild(reqEl);
            String custom = getElementData(appEl);
            if (custom.equals("") || custom.trim().length() == 0)
            {
               throw new SOAPException(Constants.FAULT_CODE_CLIENT,
                  "Missing 'AppLocation' element data, for callDirect action.");
            }
            // add the required parameter
            m_optionMap.put("custom", custom);

            sendToServer("callDirect", env, reqCtx, resCtx);
         }
      }
   }

   private Element getFirstElementChild(Node node)
   {
      if (node == null)
         return null;

      Node child = node.getFirstChild();
      while (child != null)
      {
         if (child.getNodeType() == Node.ELEMENT_NODE)
            return (Element)child;

         child = child.getNextSibling();
      }
      return null;
   }

   private String getElementData(Node node)
   {
      StringBuffer ret = new StringBuffer();

      if (node != null)
      {
         Node text;
         for (text = node.getFirstChild();
              text != null;
              text = text.getNextSibling())
         {
            /**
            * the item's value is in one or more text nodes which are
            * its immediate children
            */
            if (text.getNodeType() == Node.TEXT_NODE)
            {
               ret.append(text.getNodeValue());
            }
            else
            {
               if (text.getNodeType() == Node.ENTITY_REFERENCE_NODE)
               {
                  ret.append(getElementData(text));
               }
            }
         }
      }
      return ret.toString();
   }
}
