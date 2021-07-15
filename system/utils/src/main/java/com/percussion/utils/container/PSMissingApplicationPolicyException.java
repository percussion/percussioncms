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

