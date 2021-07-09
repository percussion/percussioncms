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

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;

/**
 * An effect focused on folder relationships.
 * {@link #onFolderChange(Object[], IPSRequestContext, IPSExecutionContext, PSEffectResult)}
 * is called when the relationship is a folder relationship.
 * 
 * @author adamgent
 *
 */
public abstract class PSFolderEffect extends PSEffect
{

   @Override
   public void test(Object[] params, IPSRequestContext request,
         IPSExecutionContext context, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if (!context.isPreConstruction() && !context.isPreDestruction()
            && !context.isPreUpdate())
      {
         result.setWarning("Illegal Context, expected to be "
               + "RS_PRE_CONSTRUCTION, RS_PRE_DESTRUCTION or RS_PRE_UPDATE.");
         return;
      }
      if ( ! isFolderRelationship(context, result))
         return;
      onFolderChange(params, request, context, result);
   }
   
   
   private boolean isFolderRelationship(IPSExecutionContext context, PSEffectResult result)
   {
      PSRelationship currentRel = context.getCurrentRelationship();

      if (currentRel == null
            || !currentRel.getConfig().getName().equals(
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT))
      {
         result.setWarning("The current relationship is not of type '"
               + PSRelationshipConfig.TYPE_FOLDER_CONTENT + "'.");
         return false;
      }
      
      return true;
   }
   
   protected boolean isValidOriginatingRelationship(IPSRequestContext reqCtx,
         PSEffectResult result)
   {
      PSRequest request = getRequest(reqCtx);
      PSRelationship relationship = request.getOriginatingRelationship();
      if (relationship == null)
         return true;
      
      if (relationship.getConfig().getName().equals(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT))
      {
         return true;
      }

      result.setWarning("The originating relationship is not of type '"
            + PSRelationshipConfig.TYPE_FOLDER_CONTENT + "'.");

      return false;
   }
   
   protected PSRequest getRequest(IPSRequestContext reqCtx)
   {
      if (!(reqCtx instanceof PSRequestContext))
         throw new IllegalArgumentException("request context must be an instance of PSRequestContext.");
      
      return ((PSRequestContext)reqCtx).getRequest();
   }   

   /**
    * Called if the relationship is a folder relationship. 
    * @param params not null.
    * @param request not null.
    * @param context not null.
    * @param result not null.
    */
   protected abstract void onFolderChange(Object[] params, IPSRequestContext request,
         IPSExecutionContext context, PSEffectResult result);

   @Override
   public void attempt(Object[] prams, IPSRequestContext request,
         IPSExecutionContext context, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      // Folder validation effect does not do any special processing
      result.setSuccess();
   }

   @Override
   public void recover(Object[] params, IPSRequestContext request,
         IPSExecutionContext context, PSExtensionProcessingException e,
         PSEffectResult result) throws PSExtensionProcessingException
   {
      result.setSuccess();

   }

}
