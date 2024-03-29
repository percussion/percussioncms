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
package com.percussion.pso.effects;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
 * Adds an item to a folder where its parent resides.  
 * Unlike the FolderFollowerEffect, the item is not removed from any current folders, only added 
 * to new ones.  
 *
 * @author DavidBenua
 *
 */
@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION,PSEffectContext.PRE_UPDATE})
public class PSFolderOwnerSubfolderEffect extends PSAbstractFolderEffect
      implements
         IPSEffect
{
   /**
    * Default constructor. 
    */
   public PSFolderOwnerSubfolderEffect()
   {
      super(); 
      
   }

  
   /**
    * Adds an item to a folder where it does not exit. 
    * @param current the current relationship. 
    * @param subfolderPath the desired subfolder path.  
    * @param userName the user name who caused this event. 
    * @throws Exception
    */
   protected void processRelations(PSRelationship current, String subfolderPath, String userName) throws Exception
   {
      initServices(); 
      Set<String> ownerFolderPaths = getFolderPaths(current.getOwner().getId(), userName);
      Set<String> itemFolderPaths = getFolderPaths(current.getDependent().getId(), userName);
      
      IPSGuid itemGuid = gmgr.makeGuid(current.getDependent());
      
      for(String folderPath : ownerFolderPaths)
      {
         log.debug("owner folder path is " +  folderPath );
         if(StringUtils.isNotBlank(folderPath) && folderPath.startsWith("//Sites/"))
         {           
            String subPath = findOrCreateChildFolder(folderPath, subfolderPath, userName);
            log.debug("checking subfolder path " + subPath); 
            if(!itemFolderPaths.contains(subPath))
            {
               log.debug("adding item to folder " + subPath); 
               cws.addFolderChildren(subPath, 
                     Collections.<IPSGuid>singletonList(itemGuid));
            }
         }
      }   
   }

   /**
    * Gets the current folder paths for an item. 
    * @param contentId the content id of the dependent item. 
    * @param userName the user name 
    * @return the set of folder paths. 
    * @throws Exception
    */
   protected Set<String> getFolderPaths(int contentId, String userName ) throws Exception
   {
      IPSGuid guid = gmgr.makeGuid(new PSLocator(contentId));
      String[] paths = cws.findFolderPaths(guid); 
      Set<String> results = new LinkedHashSet<String>(paths.length); 
      results.addAll(Arrays.<String>asList(paths)); 
      return results;
   }

   /**
    * Find or create the desired subfolder. 
    * @param folderPath the folder path of the parent folder
    * @param subfolderPath the subfolder path. 
    * @param userName the user name
    * @return the path of the subfolder. 
    * @throws Exception
    */
   protected String findOrCreateChildFolder(String folderPath, String subfolderPath, String userName )
   throws Exception
   {
      if(StringUtils.isBlank(subfolderPath))
      {  //no sub path, just return the existing folder. 
         return folderPath; 
      }
      StringBuilder sb = new StringBuilder(folderPath); 
      if(!subfolderPath.startsWith("/"))
      {
         sb.append('/'); 
      }
      sb.append(subfolderPath);
      log.debug("adding folder path " + sb.toString()); 
      cws.addFolderTree(sb.toString()); 

      return sb.toString(); 
   }
   /**
    * @see IPSEffect#attempt(Object[], IPSRequestContext, IPSExecutionContext, PSEffectResult)
    */
   public void attempt(Object[] params, IPSRequestContext req, IPSExecutionContext exCtx, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if(exCtx.isPreConstruction() | exCtx.isPreUpdate())
      {
         String subfolderName = params[0].toString(); 
         log.debug("subfolder name is " + subfolderName); 
         String userName = req.getUserName();
         log.debug("user name is " + userName); 
         String sessionId = req.getUserSessionId();
         log.debug("session id is " + sessionId); 
         
         PSRelationship current = exCtx.getCurrentRelationship(); 
         try {
            processRelations(current, subfolderName, userName); 
         }
         catch (PSErrorsException e)
         {
            Map<IPSGuid, Object> emap = e.getErrors();
            for(Map.Entry<IPSGuid, Object> entry : emap.entrySet())
            {
               log.error("Error for Guid "  + entry.getKey() + " is " + entry.getValue());
            }
            log.error("Errors Exception " + e.getLocalizedMessage(), e); 
         }
         catch (PSErrorException ee)
         {
            log.error(ee.getErrorMessage(), ee);

         }
         catch (Exception ex)
         {
            log.error("Relationship error {}", ex.getMessage());
            log.debug(ex.getMessage(), ex);
         }
      }
      result.setSuccess();
      
   }
   
  
  
}
