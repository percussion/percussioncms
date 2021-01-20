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
package com.percussion.server.command;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.search.PSSearchException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;

import org.w3c.dom.Document;

/**
 * Provides some common functionality for several different commands 
 * @author paulhoward
 */
public abstract class PSConsoleCommandSearchIndex extends PSConsoleCommand
{
   /**
    * Ctor required by framework.
    * 
    * @param args See class description for reqs.
    * 
    * @throws PSIllegalArgumentException If the args don't match allowed 
    * values. Ids are not validated here. 
    * <p>Note: we use PSIllegal... here because that's what the framework uses.
    */
   public PSConsoleCommandSearchIndex(String args)
      throws PSIllegalArgumentException
   {
      super(args);   
   }

   /**
    * Called by the framework, this method then passes the request onto the
    * derived class to perform the real work.
    * 
    * @return A document conforming to the format recommended in the interface. 
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc = PSConsoleCommandShowStatusSearch.getDisabledDoc(request,
            getCommandName());
      if (null != doc)
         return doc;
         
      int code;
      String[] resultArgs;
      
      PSKey[] typeKeys = getIds(m_cmdArgs);
      try
      {
         PSKey[] processedKeys = doExecute(request, typeKeys);
         
         code = getResultCode();
         resultArgs = getResultArgs(processedKeys);
      }
      catch (PSSearchException e)
      {
         throw new PSConsoleCommandException(e.getErrorCode(), 
               e.getErrorArguments());
      }
      
      doc = getResultsDocument(request, getCommandName(), code, 
            resultArgs);
      return doc;
   }

   /**
    * This method figures out which content type ids to
    * process and puts them in an array. If arg is a number, it is returned, 
    * if it is text, it is assumed to be the name of a content type and is
    * converted to a number. Otherwise, all ids for all active content editors 
    * are returned. 
    * 
    * @param type The argument supplied to the command. May be <code>null</code> 
    * or empty.
    * @return An array of at least 1 content type key. All keys identify 
    * currently running editors.
    * 
    * @throws PSConsoleCommandException If the supplied arg is not a number 
    * that represents a running content editor or is not the name of a running
    * content editor if arg is non-empty.
    */
   protected PSKey[] getIds(String type) 
      throws PSConsoleCommandException
   {
      long[] ids;
      try
      {
         if (null == type)
            type = "";
         int typeId = getId(type); 
         if (typeId < 0 && type.length() == 0)
         {
            //need to do all content types
            ids = PSItemDefManager.getInstance().getAllContentTypeIds(
                  PSItemDefManager.COMMUNITY_ANY);
         }
         else
         {
            ids = new long[1];
            if (typeId < 0)
            {
               ids[0] = 
                     PSItemDefManager.getInstance().contentTypeNameToId(type);
            }
            else
            {
               //validate the supplied id
               PSItemDefManager.getInstance().contentTypeIdToName(typeId);
               ids[0] = typeId;
            }
         }
         
         PSKey[] keys = new PSKey[ids.length];
         for (int i=0; i < ids.length; i++)
         {
            keys[i] = PSContentType.createKey((int) ids[i]);
         }
         return keys;
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSConsoleCommandException(e.getErrorCode(), 
               e.getErrorArguments());
      }
   }

   /**
    * Tries to parse a string as an integer.
    * 
    * @param number Assumed not <code>null</code>.
    *  
    * @return If a valid integer > 0, the integer, otherwise, -1.
    */
   protected int getId(String number)
   {
      int result = -1;
      try
      {
         result = Integer.parseInt(number);
      }
      catch (NumberFormatException e)
      {
         //ignore, just return -1
      }
      return result;
   }
   
   /**
    * Called after the {@link #doExecute(PSRequest, int[])} method has finished.
    * 
    * @return The error code to use in the response document. If not 
    * overridden, <code>IPSServerErrors.RCONSOLE_SUCCESS</code> is returned. 
    */
   protected int getResultCode()
   {
      return IPSServerErrors.RCONSOLE_CONTENT_TYPES_PROCESSED;
   }
   
   /**
    * Returns the arguments appropriate for the error code returned by 
    * {@link #getResultCode()}.
    * 
    * @param successes The editors that were actually processed (as opposed 
    * to these that were passed in). Never <code>null</code>, may be empty.
    * 
    * @return If not overridden, a comma separated list of the names of the
    * editors identified in the supplied array. If any id is not valid, the
    * number with '(invalid)' is returned for that name.
    */
   protected String[] getResultArgs(PSKey[] successes)
   {
      if (null == successes)
      {
         throw new IllegalArgumentException("succcesses cannot be null");
      }
      
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      StringBuffer buf = new StringBuffer(100);
      for (int i = 0; i < successes.length; i++)
      {
         String name;
         try
         {
            name = mgr.contentTypeIdToName(successes[i].getPartAsInt());
         }
         catch (PSInvalidContentTypeException e)
         {
            name = String.valueOf(successes[i]) + " (invalid)";
         }
         if (buf.length() > 0)
            buf.append(", ");
         buf.append(name);
      }
      String[] results = 
      {
         buf.toString()
      };
      return results;
   }
   
   /**
    * The derived class must return the fully qualified name (w/o command
    * arguments).
    * 
    * @return Never <code>null</code> or empty.
    */
   protected abstract String getCommandName();
   
   /**
    * The {@link #execute(PSRequest)} method calls this method to do the work.
    * 
    * @param request The request passed into the <code>execute</code>
    * method. May be <code>null</code>.
    *  
    * @param ids A set of ids as interpreted by the derived class.
    *
    * @return The ids that were actually processed. These values are then 
    * passed to the {@link #getResultArgs()} method.
    * 
    * @throws PSSearchException If the command cannot complete successfully.
    */
   protected abstract PSKey[] doExecute(PSRequest request, PSKey[] ids)
      throws PSSearchException;
}
