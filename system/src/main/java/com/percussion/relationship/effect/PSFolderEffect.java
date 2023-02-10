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
