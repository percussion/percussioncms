/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.effects;
import java.util.ArrayList;
import java.util.List;


import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;



/**
 * Folder Follower Effect.  Moves the dependent item into
 * all of the folders occupied by the owner item, and removes
 * it from any other folders. Operates only at relationship 
 * construction time. At all other times, the item is not moved.    
 * 
 *
 * @author DavidBenua
 *
 * Copyright 2007, Percussion Software, Inc.  
 */
@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION,PSEffectContext.PRE_DESTRUCTION,PSEffectContext.PRE_UPDATE})
public class PSFolderFollowerEffect extends PSAbstractFolderEffect implements IPSEffect
      
{
   /**
    * Default constructor.
    */
   public PSFolderFollowerEffect()
   {
      
   }

   
   /**
    * Gets the parent folders of this item.  
    * @param loc the item locator
    * @return the List of relationships to parent folders.  Never <code>null</code> but may
    * be <code>empty</code>
    * @throws PSErrorException
    */
   protected List<PSRelationship> getFolderParents(PSLocator loc) throws PSErrorException
   {
      initServices(); 
      PSRelationshipFilter filter = new PSRelationshipFilter(); 
      IPSGuid guid = gmgr.makeGuid(loc); 
      filter.setDependent(loc);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_FOLDER); 
      filter.limitToEditOrCurrentOwnerRevision(true);       
      List<PSRelationship> parRels = sws.loadRelationships(filter);
      return parRels;
   }

   
   /**
    * Process a new relations.  This method compares the folder relationships of the relationship 
    * owner with the folder relationships of the dependent.  The dependent is added to any folders
    * where the owner resides (and removed from folders the owner does not reside).   
    * @param current the current relationship.  
    * @throws Exception
    */
   public void processRelations(PSRelationship current) throws Exception
   {
      List<PSRelationship> ownerFolders = getFolderParents(current.getOwner()); 
      List<PSRelationship> depFolders = getFolderParents(current.getDependent());
      
      List<PSRelationship> toDelete = subtractRels(depFolders, ownerFolders); 
      List<PSRelationship> toAdd = subtractRels(ownerFolders, depFolders);
      addMissing(current.getDependent(), toAdd);
      removeExtra(toDelete); 
   }
   
   /**
    * Adds a missing item to a folder.  
    * @param item the locator for the item.  
    * @param missingList the list of relationships where the item is missing from the folder.  
    * @throws PSErrorException 
    */
   protected void addMissing(PSLocator item, List<PSRelationship> missingList) throws PSErrorException
   {
      initServices(); 
      IPSGuid itemGuid = gmgr.makeGuid(item); 
      for(PSRelationship missing : missingList)
      {
         IPSGuid folderGuid = gmgr.makeGuid(missing.getOwner()); 
         sws.createRelationship(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT, 
               folderGuid, itemGuid);
      }
   }
   
   /**
    * Removes extra items from a folder 
    * @param extras the extra relationships 
    * @throws PSErrorsException
    * @throws PSErrorException
    */
   protected void removeExtra(List<PSRelationship> extras) throws PSErrorsException, PSErrorException
   {
      List<IPSGuid> relGuids = new ArrayList<IPSGuid>(); 
      for(PSRelationship rel : extras)
      {
         relGuids.add(rel.getGuid()); 
      }
      sws.deleteRelationships(relGuids);
   }
   
   
   /**
    * Subtract a list of relationships from another list of relationships. The relationships are subtracted if a 
    * relationship exists with the same owner content id. 
    * @param mainList the main list to subtract <em>from</em>
    * @param subList the list to be subtracted.
    * @return the remainder list. Never <code>null</code> but may be <code>empty</code> 
    */
   protected List<PSRelationship> subtractRels(List<PSRelationship> mainList, List<PSRelationship> subList)
   {
      List<PSRelationship> remains = new ArrayList<PSRelationship>();
      for(PSRelationship mainRel : mainList)
      {
         boolean found = false;
         for(PSRelationship subRel : subList)
         {
            if(mainRel.getOwner().getId() == subRel.getOwner().getId())
            {
               found = true;
               break;
            }
         }
         if(!found)
         {
            remains.add(mainRel); 
         }
      }
      
      return remains; 
   }

   /**
    * @see IPSEffect#attempt(Object[], IPSRequestContext, IPSExecutionContext, PSEffectResult)
    */
   public void attempt(Object[] params, IPSRequestContext req, IPSExecutionContext exCtx, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if(exCtx.isConstruction())
      {
         PSRelationship current = exCtx.getCurrentRelationship(); 
         try {
            processRelations(current); 
         }
         catch (Exception ex)
         {
            log.error("Relationship error " + ex, ex); 
         }
      }
      result.setSuccess();
      
   }
}
