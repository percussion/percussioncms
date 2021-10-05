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

import com.percussion.error.PSException;


/**
 * PSUnsupportedProviderException is thrown to indicate that the specified
 * provider type is not supported by provider handed the request.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUnsupportedProviderException extends PSException
{
   /**
    * Constructs an unsupported provider exception with the default
    * message.
    *
    * @param   providerClass   the class of the security provider
    *
    * @param   provider         the provider the class was asked to handle
    */
   public PSUnsupportedProviderException( java.lang.String providerClass,
                                          java.lang.String provider)
   {
      super(IPSSecurityErrors.PROVIDER_NOT_SUPPORTED_BY_CLASS,
            new Object[] { providerClass, provider });
   }
}

