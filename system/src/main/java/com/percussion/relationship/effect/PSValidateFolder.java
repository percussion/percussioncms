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

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;

/**
 * Do nothing as a place holder in case the registered Exits still have an 
 * entry references this class.
 */
public class PSValidateFolder extends PSEffect
{
   //Implementation of the interface method
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
   {
      //Folder validation effect does not do any special processing
      result.setSuccess();
   }

   //Implementation of the interface method
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      //Folder validation effect does not do any special processing
      result.setSuccess();
   }

   //Implementation of the interface method
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      //Folder validation effect does not need to recover anything
      result.setSuccess();
   }
   
   /**
    * Thread local storage of the processed relationship. This is just to 
    * avoid unnecessary processing of the same relationship for each current 
    * relationship. We need to validate the originating folder relationship 
    * only once not while processing each relationship around the original 
    * owner item.
    */
   private static ThreadLocal m_tlRelationshipsProcessed = new ThreadLocal();
}
