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



/**
 * The PSHostAddressProviderMetaData class implements cataloging for
 * the Host Address security provider.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSHostAddressProviderMetaData extends Object
{
   /**
    * Construct a meta data object for the specified provider
    * instance.
    *
    * @param      inst            the provider instance
    */
   PSHostAddressProviderMetaData(PSHostAddressProvider inst)
   {
      m_instance = inst;
   }

   /**
    * Default constructor to find connection properties, etc.
    */
   public PSHostAddressProviderMetaData()
   {
      this(null);
   }

   /**
    * Get the name of this security provider.
    *
    * @return      the provider's name
    */
   public String getName()
   {
      return PSHostAddressProvider.SP_NAME;
   }

   /**
    * Get the full name of this security provider.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return "Host Address Security Provider";
   }

   /**
    * Get the descritpion of this security provider.
    *
    * @return      the provider's description
    */
   public String getDescription()
   {
      return "Authentication through host (TCP/IP) addresses.";
   }
   
   /**
    * The host address provider instance, initialized in constructor, may be
    * <code>null<code>.
    */
   private PSHostAddressProvider   m_instance = null;
}

