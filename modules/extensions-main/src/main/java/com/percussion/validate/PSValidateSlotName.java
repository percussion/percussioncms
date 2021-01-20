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

package com.percussion.validate;

import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This exit is specifically used to validate the uniqueness
 * of slotnames when a user tries to insert or update a slotname.
 * To do this we make an internal request to get a list of existing
 * slots and then validate what the user is trying to save against
 * the list.
 *
 * A slotname is considered unique if the name is not already in
 * the database or the name is in the database and has the
 * same slotid passed in by this request.
 */
public class PSValidateSlotName implements IPSRequestPreProcessor
{
   /*
    * Implementation of the method in the interface IPSRequestPreProcessor
    * Empty in this case as it is not needed
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {

   }

   /**
    * This method makes an internal request to get a list of all
    * slots that exist in the system. We then validate against the
    * slotname and slotid for slotname uniqueness.
    *
    * @param params array of parameters sent in by exit caller, in this case
    *  no params are expected. May be <code>null</code>.
    * @param request the request context for the exit, assumed not
    *    <code>null</code>.
    *
    * @throws PSExtensionProcessingException thrown if there is an extension error
    * and when a slotname is not unique
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if(request == null)
      {
         return; //should never happen
      }

      try
      {
         m_slotname = request.getParameter(PARAM_SLOTNAME);
         m_slotid = request.getParameter(PARAM_SLOTID);


         // Make internal request to get existing slot info
         String slotListRequest = SLOTLIST_REQUEST;
         IPSInternalRequest iReq =
                     request.getInternalRequest(slotListRequest);

         Document doc = iReq.getResultDoc();
         iReq.cleanUp();

         // Load the results into a HashMap

         NodeList slotlist = doc.getElementsByTagName(XML_TAG_SLOT);
         Element slotElem = null;
         String sname = null;
         String sid = null;
         for(int i=0; i<slotlist.getLength(); i++)
         {
            slotElem = (Element)slotlist.item(i);

            sname = slotElem.getElementsByTagName(XML_TAG_SLOTNAME).
               item(0).getFirstChild().getNodeValue();

            sid = slotElem.getElementsByTagName(XML_TAG_SLOTID).
               item(0).getFirstChild().getNodeValue();

            m_slotMap.put(sid,sname);

         }

         // Check validity of slotname
         if(!isValid())
         {
            Object[] args = new Object[0];
            throw new PSExtensionException(
               IPSExtensionErrors.VALIDATE_SLOTNAME_NOT_UNIQUE, args);
         }



      }
      catch(PSException e)
      {
         throw new PSExtensionProcessingException(
         e.getErrorCode(), e.getErrorArguments());
      }
      catch (Throwable e)
      {
         PSConsole.printMsg("Extension", e);
      }

  }


  /**
   * Validate the slotname as being unique.
   * Helper function.
   *
   * A slotname is considered unique if the name is not already in
   * the database or the name is in the database and has the
   * same slotid passed in by this request.
   *
   * @returns true if this slotname is unique, else
   * false if it is not unique.
   */
   private boolean isValid()
   {

      // No slots currently exist
      // so we must be valid
      if(m_slotMap.isEmpty())
         return true;

         Iterator iter = m_slotMap.keySet().iterator();
         String key = null;
         String value = null;
         while(iter.hasNext())
         {
            key = (String)iter.next();
            value = (String)m_slotMap.get(key);

            // Not valid if slotname exists with
            // a different slotid
            if(m_slotname.equalsIgnoreCase(value) &&
               !m_slotid.equals(key))
               return false;

         }
         return true;

   }

  /**
   * The HashMap of all slot names and ids. Never <code>null</code>,
   * but may be empty.
   */
  private Map<String,String> m_slotMap = new HashMap<String,String>();

  /**
   * The slot name passed in the request. Never <code>null</code>.
   */
  private String m_slotname;

  /**
   * The slot id passed in the request. Never <code>null</code>.
   */
  private String m_slotid;

  /**
   * The name of the slot tag element
   */
  private static final String XML_TAG_SLOT = "slot";

  /**
   * The name of the slot id tag element
   */
  private static final String XML_TAG_SLOTID = "slotid";

  /**
   * The name of the slot name tag element
   */
  private static final String XML_TAG_SLOTNAME = "slotname";

  /**
   * The name of the slot id  request parameter
   */
  private static final String PARAM_SLOTID = "slotid";

  /**
   * The name of the slot name request parameter element
   */
  private static final String PARAM_SLOTNAME = "slotname";

  /**
   * The internal request to get the slot list
   */
  private static final String SLOTLIST_REQUEST = "sys_Slots/slotlist";

}

