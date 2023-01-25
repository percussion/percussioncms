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

