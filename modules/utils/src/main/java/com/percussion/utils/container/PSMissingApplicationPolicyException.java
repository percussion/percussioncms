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
package com.percussion.utils.container;

import com.percussion.utils.container.jboss.IPSJBossErrors;
import com.percussion.utils.exceptions.PSBaseException;
import org.apache.commons.lang.StringUtils;

/**
 * Indicates a specified application policy is not found.
 */
public class PSMissingApplicationPolicyException extends PSBaseException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct the exception with the policy name and file name.
    * 
    * @param policyName The name of the policy that was not found, may not be
    * <code>null</code> or empty.
    *  
    * @param fileName The name of the file in which the policy was expected to
    * be found, may not be <code>null</code> or empty.
    * 
    */
   public PSMissingApplicationPolicyException(String policyName, 
      String fileName)
   {
      super(IPSJBossErrors.APP_POLICY_ELEMENT_MISSING, policyName, 
         fileName);
      
      if (StringUtils.isBlank(policyName))
         throw new IllegalArgumentException(
            "policyName may not be null or empty");
      
      if (StringUtils.isBlank(fileName))
         throw new IllegalArgumentException(
            "fileName may not be null or empty");
   }



   /* (non-Javadoc)
    * @see com.percussion.utils.exceptions.PSBaseException#getResourceBundleBaseName()
    */
   @Override
   protected String getResourceBundleBaseName()
   {
      // TODO Auto-generated method stub
      return "com.percussion.utils.jboss.PSJBossErrorStringBundle";
   }

}

