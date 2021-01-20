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

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.cache.PSFolderRelationshipCache;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the execution of 'dump folderrelationshipcache' console
 * command and dumps the results as an XML document to the console.
 */
public class PSConsoleCommandDumpFolderRelationshipCache extends PSConsoleCommandCache
{
   /**
    * The constructor for this class. The command arguments is optional.
    *
    * @param cmdArgs   the argument string to use when executing   this command, may
    * be <code>null</code> or empty. If it is not empty, it is expected to be
    * the id of a folder relationship.
    */
   public PSConsoleCommandDumpFolderRelationshipCache(String cmdArgs)
   {
      super(cmdArgs);
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    *    &lt;!--
    *       The cache statistics element with each attribute referring to
    *       each kind of statistics. See {@link
    *       PSFolderRelationshipCache#getCacheStatistics(Document)} for
    *       description of <code>FolderRelationshipCacheStatistics</code> 
    *       element.
    * 
    *       If the folder cache is off, then the result will not contain 
    *       'PSXRelationship' or 'FolderRelationshipCacheStatistics'.
    * 
    *       If the folder cache is on and no arguments, then the result will
    *       contain 'FolderRelationshipCacheStatistics'.
    * 
    *       If there is a argument, which will be used as a folder relationship,
    *       then the result will contain 'PSXRelationship' 
    *      --&gt;
    *      &lt;!ELEMENT PSXConsoleCommandResults   (command, 
    *       (FolderRelationshipCacheStatistics | PSXRelationship)?)&gt;
    *
    *      &lt;!--
    *         the command that was executed
    *      --&gt;
    *      &lt;!ELEMENT command (#PCDATA)&gt;
    *
    * </CODE></PRE>
    *
    * @param request
    *           the requestor object, may be <code>null</code>
    * 
    * @return the result document, never <code>null</code>
    * 
    * @throws PSConsoleCommandException
    *            if an error occurs during execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      Document respDoc = getResultsDocument();
      Element root = respDoc.getDocumentElement();

      Element cacheElm = getFolderRelationshipCacheDoc(respDoc);
      if (cacheElm != null)
         root.appendChild(cacheElm);

      return respDoc;
   }

   /**
    * Creates the cache statistics element. See {@link #execute(PSRequest)}for
    * the structure of the element.
    * 
    * @param doc
    *           the document to use to create the element, may not be <code>
    * null</code>
    * 
    * @return the xml element representing cache statistics or a folder
    *         relationship, never <code>null</code>
    * 
    * @throws PSConsoleCommandException
    *            failed to pass argument.
    */
   Element getFolderRelationshipCacheDoc(Document doc)
         throws PSConsoleCommandException
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");

      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      if (cache == null)
         return null;
      
      Element cacheElm = null;
      if (m_cmdArgs != null && m_cmdArgs.trim().length() != 0)
      {
         int id = -1;
         String errorMsg = null;
         try
         {
            id = Integer.parseInt(m_cmdArgs);
         }
         catch (NumberFormatException e)
         {
            Object[] args = {getCommandName(), m_cmdArgs};
            throw new PSConsoleCommandException(
                  IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
         }
         
         PSRelationship rel = cache.getRelationship(id);
         if (rel != null)
         {
            cacheElm = rel.toXml(doc);
         }
         else
         {
            throw new PSConsoleCommandException(
                  IPSServerErrors.CANNOT_FIND_CACHED_FOLDER_RELATIONSHIP,
                  new Object[] { new Integer(id) });
         }
      }
      else
      {
         cacheElm = cache.getCacheStatistics(doc);
      }
      
      return cacheElm;
   }


   //see super class method for description.
   public String getCommandName()
   {
      return ms_cmdName;
   }

   /**
    * The command executed by this class.
    */
   public final static String ms_cmdName = "dump folderrelationshipcache";
}
