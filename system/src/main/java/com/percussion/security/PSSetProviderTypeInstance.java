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

package com.percussion.security;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;

/**
 * This class is a Rhythmyx pre-exit that splits the given single security
 * provider information in to provider type and instance and sets to the request
 * with 2 HTML parameters.
 * <br>
 * This is required because request gives the security provider information
 * in a single parameter in the format 'providerType/instance'. But the backend
 * has both of them as seperate fields. So we split this parameter value by '/'
 * and set the values to 2 HTML parameters.
 */
public class PSSetProviderTypeInstance extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /**
    * If the request has security provider information, splits that in to
    * provider type and instance and sets to the request with 2 HTML parameters,
    * otherwise no pre-processing on the request.
    *
    * @param params the array of parameter objects.
    * <p>
    * There are four parameters and all are optional.
    * <br>
    * params[0], specifying the parameter name of the request for security
    * provider information. If not provided, the default name
    * 'sys_securityProvider' will be used. May be <code>null</code> or empty.
    * <br>
    * params[1], specifying the separator between provider type and instance.
    * If not provided, the default '/' will be used. May be <code>null</code> or
    * empty.
    * <br>
    * params[2], specifying the parameter name to set to the request for
    * security provider type. If not provided, the default name 'sys_spType'
    * will be used. May be <code>null</code> or empty.
    * <br>
    * params[3], specifying the parameter name to set to the request for
    * security provider instance. If not provided, the default name
    * 'sys_spInstance' will be used. May be <code>null</code> or empty.
    *
    * @param request the request context for the exit, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if request context for the exit is
    * <code>null</code>.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      if(request == null)
         throw new IllegalArgumentException(
            "Request context for the exit can not be null");

      String securityParamName = DEFAULT_SECURITY_PARAM_NAME;
      String separator = DEFAULT_SECURITY_INFO_SEPARATOR;
      String providerTypeParamName = DEFAULT_PROVIDER_TYPE_PARAM_NAME;
      String securityInstanceParamName = DEFAULT_INSTANCE_PARAM_NAME;

      if(params != null && params.length > 0)
      {
         try {

            if(params[0] != null && params[0].toString().trim().length() > 0)
               securityParamName = params[0].toString().trim();

            if(params[1] != null && params[1].toString().trim().length() > 0)
               separator = params[1].toString().trim();

            if(params[2] != null && params[2].toString().trim().length() > 0)
               providerTypeParamName = params[2].toString().trim();

            if(params[3] != null && params[3].toString().trim().length() > 0)
               securityInstanceParamName = params[3].toString().trim();

         }
         catch (ArrayIndexOutOfBoundsException e)
         {
            //means that parameter doesn't exist, because we set with default
            //values, we can proceed.
         }
      }

      String spName = request.getParameter(securityParamName);
      String spType = "";
      String spInstance = "";
      if(spName != null && spName.length() > 0)
      {
         int index = spName.indexOf(separator);
         if(index != -1)
         {
            spType = spName.substring(0, index);
            spInstance = spName.substring(index+1);
            request.setParameter(securityInstanceParamName, spInstance);
         }
         else
         {
            spType = spName;
         }
         request.setParameter(providerTypeParamName, spType);
      }
   }

   /**
    * The default HTML Parameter name that was set with the security provider
    * information.
    */
   public static final String DEFAULT_SECURITY_PARAM_NAME =
      "sys_securityProvider";

   /**
    * The default HTML Parameter name that should be set with the security
    * provider type information.
    */
   public static final String DEFAULT_PROVIDER_TYPE_PARAM_NAME = "sys_spType";

   /**
    * The default HTML Parameter name that should be set the security provider
    * instance information.
    */
   public static final String DEFAULT_INSTANCE_PARAM_NAME = "sys_spInstance";

   /**
    * The deafault separator between security provider type and instance.
    **/
   public static final String DEFAULT_SECURITY_INFO_SEPARATOR = "/";
}
