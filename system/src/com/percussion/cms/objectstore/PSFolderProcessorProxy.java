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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.error.PSNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * Proxy class that should be used to get the correct folder processor 
 * depending on the callers location.
 */
public class PSFolderProcessorProxy extends PSProcessorProxy 
   implements IPSFolderProcessor
{
   //see base class method for details
   public PSComponentSummary getSummary(String path) throws PSCmsException
   {
      return getProcessor().getSummary(path);
   }
   /**
    * Create the proxy.
    */
   public PSFolderProcessorProxy(String location, Object ctx)
      throws PSCmsException
   {
      super(location, ctx);
   }

   //see IPSFolderProcessor interface
   @Override
   public String[] getFolderPaths(PSLocator objectId)
      throws PSCmsException
   {
      return getFolderPaths(objectId, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

    @Override
    public String[] getFolderPaths(PSLocator objectId, String relationshipTypeName) throws PSCmsException {
        return getProcessor().getFolderPaths(objectId, relationshipTypeName);
    }


    //see IPSFolderProcessor interface
   public void copyChildren(List children, PSLocator targetFolderId) 
      throws PSCmsException
   {
      getProcessor().copyChildren(children, targetFolderId);
   }

   //see IPSFolderProcessor interface
   public PSComponentSummary[] getChildSummaries(PSLocator sourceFolderId)
         throws PSCmsException
   {
      return getProcessor().getChildSummaries(sourceFolderId);
   }
   
   //see IPSFolderProcessor interface
   public void moveChildren(PSLocator sourceFolderId, List children,
         PSLocator targetFolderId) throws PSCmsException
   {
      getProcessor().moveChildren(sourceFolderId, children, targetFolderId);
   }

   //see IPSFolderProcessor interface
   public void removeChildren(PSLocator sourceFolderId, List children)
           throws PSCmsException, PSNotFoundException {
      getProcessor().removeChildren(sourceFolderId, children);
   }

   //see IPSFolderProcessor interface
   public void addChildren(List children, PSLocator targetFolderId)
      throws PSCmsException
   {
      getProcessor().addChildren(children, targetFolderId);
   }

   //see IPSFolderProcessor interface
   public String copyFolder(PSLocator source, PSLocator target,
      PSCloningOptions options) throws PSCmsException
   {
      return getProcessor().copyFolder(source, target, options);
   }
   
   //see IPSFolderProcessor interface
   public void copyFolderSecurity(PSLocator source, PSLocator target)
      throws PSCmsException
   {
      getProcessor().copyFolderSecurity(source, target);
   }
   
   //see IPSFolderProcessor interface
   public Set getFolderCommunities(PSLocator source) throws PSCmsException
   {
      return getProcessor().getFolderCommunities(source);
   }
   
   /**
    * Get the processor from the base class and cast to correct type.
    * 
    * @return never <code>null</code>.
    */
   private IPSFolderProcessor getProcessor()
   {
      String type = "PSFolder";
      try
      {
         return (IPSFolderProcessor) m_processorConfig.getProcessor(type);
      }
      catch (PSCmsException e)
      {
         //should never happen unless a bug
         throw new RuntimeException("Invalid configuration for '" + type + "':"
               + e.getLocalizedMessage());
      }
   }
   //see base class method for details
   public PSComponentSummary[] getParentSummaries(PSLocator objectId)
         throws PSCmsException
   {
      return getProcessor().getParentSummaries(objectId);
   }
   //see base class method for details
   public PSLocator[] getDescendentFolderLocators(PSLocator folderId)
         throws PSCmsException
   {
      return getProcessor().getDescendentFolderLocators(folderId);
   }
   
   //see base class method for details
   public PSLocator[] getDescendentFolderLocatorsWithoutFilter(PSLocator folderId)
         throws PSCmsException
   {
      return getProcessor().getDescendentFolderLocatorsWithoutFilter(folderId);
   }
      
   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSFolderProcessor#getAncestorLocators(com.percussion.design.objectstore.PSLocator)
    */
   public List<PSLocator> getAncestorLocators(PSLocator folderId) 
      throws PSCmsException
   {
      return getProcessor().getAncestorLocators(folderId);
   }

   // implement the interface method
   public void removeChildren(PSLocator sourceFolderId, List children,
      boolean force) throws PSCmsException, PSNotFoundException {
      getProcessor().removeChildren(sourceFolderId, children, force);
   }

   // implement the interface method
   public void moveChildren(PSLocator sourceFolderId, List children,
      PSLocator targetFolderId, boolean force) throws PSCmsException
   {
      getProcessor().moveChildren(sourceFolderId, children, targetFolderId,
         force);
   }
   
   // implement the interface method
   public void purgeFolderAndChildItems(List<PSLocator> items) throws PSCmsException 
   {
       getProcessor().purgeFolderAndChildItems(items);
   }

   // implement the interface method
   public void purgeFolderNavigation(PSLocator folder) throws PSCmsException
   {
       getProcessor().purgeFolderNavigation(folder);
   }
   // implement the interface method
   public void purgeFolderAndChildItems(PSLocator sourceFolderId,
           List<PSLocator> items) throws PSCmsException {
       getProcessor().purgeFolderAndChildItems(sourceFolderId, items);
   }

}
