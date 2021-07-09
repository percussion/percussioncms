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
package com.percussion.relationship.effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

/**
 * This effect attaches a new clone based on a relationship list from the 
 * predefined list of relationships to the folder specified via an HTML 
 * parameter in the request.
 * <p>
 * The effect will be active for the situations meeting the following 
 * conditions:
 * <ol>
 * <li>The context must be relationship creation</li>
 * <li>Non <code>null</code> and non empty HTML parameter {@link 
 * com.percussion.util.IPSHtmlParameters#SYS_FOLDERID} must exist in the 
 * request. If more than one value exists the clone will be addedd to all 
 * target folders</li>
 * <li>The originating and current relationship must be the same. This assures
 * that the effect is processed only once in the request life time</li>
 * </ol>
 * <p>
 * This effect typically goes on to the relationships which allow creation of 
 * clones from the user interface such as "New Copy", "Promotable Version" etc.
 * Note that it does not check if the clone is already attached to a requested 
 * target folder.
 *
 */
public class PSAttachCloneToFolder extends PSEffect
{

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.relationship.PSEffectResult)
    */
   public void test(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      //context must be construction
      if(!context.isPreConstruction())
      {
         result.setWarning(
            "This effect is active only during relationship construction");
         return;
      }
      //A valid sys_folderid parameter must be present
      Object obj = request.getParameterObject(IPSHtmlParameters.SYS_FOLDERID);
      List tgtFolders = new ArrayList();
      if (obj == null || StringUtils.isBlank(obj.toString()))
      {
          result.setWarning("No folder to attach");
          return;
      }
      
      if (obj instanceof List)
      {
          tgtFolders = (List)obj;
      }
      else
      {
          tgtFolders.add(obj.toString());
      }
      if(tgtFolders.size()<1)
      {
         result.setWarning("No folder to attach");
         return;
      }
      PSRelationship originatingRel = context.getOriginatingRelationship();
      PSRelationship currentRel = context.getCurrentRelationship();
      //Originating and current relationships must be the same
      //We cannot compare the dependent since it might not be avaialble in the 
      //originating relationship yet
      if(!originatingRel.getConfig().getName().equals(
            currentRel.getConfig().getName()) ||
         !originatingRel.getOwner().equals(currentRel.getOwner()))
      {
         result.setWarning(
            "This effect is active only if the originating relationship and "
               + "current relationship are the same");
         return;
      }
      result.setSuccess();
   }

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.relationship.PSEffectResult)
    */
   public void attempt(
      Object[] prams,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      PSRelationship currentRel = context.getCurrentRelationship();
      Object orig = request.getParameterObject(
            IPSHtmlParameters.SYS_ORIGINALFOLDERID);
      Object obj = request.getParameterObject(IPSHtmlParameters.SYS_FOLDERID);
      
      // Restore the original if available
      if (orig != null) obj = orig;
      
      List tgtFolders = new ArrayList();
      if (obj instanceof List)
      {
          tgtFolders = (List)obj;
      }
      else
      {
          tgtFolders.add(obj.toString());
      }

       try
       {
          PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
          
          List children = new ArrayList();
          children.add(currentRel.getDependent());
          for (int i = 0; i < tgtFolders.size(); i++)
          {
             String folderid = tgtFolders.get(i).toString();
             PSLocator tgtFolder = new PSLocator(folderid, "-1");
             proc.add(
                PSDbComponent.getComponentType(PSFolder.class),
                PSRelationshipConfig.TYPE_FOLDER_CONTENT,
                children,
                tgtFolder);
          }
       }
       catch (PSCmsException e)
       {
          result.setError(e);
          return;
       }
       result.setSuccess();
   }

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.extension.PSExtensionProcessingException, 
    * com.percussion.relationship.PSEffectResult)
    */
   public void recover(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      // TODO Auto-generated method stub

   }
   
   /**
    * Predefined list of categories for which the new clone needs to be 
    * attached to the supplied folder. This list cuurently contains three 
    * categories:
    * <ol>
    * <li>{@link PSRelationshipConfig.CATEGORY_COPY New Copy}</li>
    * <li>{@link PSRelationshipConfig.CATEGORY_PROMOTABLE Promotable Version}</li>
    * <li>{@link PSRelationshipConfig.CATEGORY_TRANSLATION Translation}</li>
    * </ol>
    */
   static Set ms_cloneRelCategories = new HashSet();
   static
   {
      ms_cloneRelCategories.add(PSRelationshipConfig.CATEGORY_COPY);
      ms_cloneRelCategories.add(PSRelationshipConfig.CATEGORY_PROMOTABLE);
      ms_cloneRelCategories.add(PSRelationshipConfig.CATEGORY_TRANSLATION);
   }
}
