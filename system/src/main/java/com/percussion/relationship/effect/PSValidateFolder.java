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
