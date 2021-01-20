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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.proxyconfig.service;

import com.percussion.proxyconfig.data.PSProxyConfig;

import java.util.List;

/**
 * @author LucasPiccoli
 * 
 */
public interface IPSProxyConfigService
{

   /**
    * Finds all proxy configurations in the file.
    * 
    * @return a list containing all of the located proxy configurations, will be
    *         empty if none were found. Never <code>null</code>. The list will
    *         be sorted in order found in the file.
    */
   public List<PSProxyConfig> findAll();

   /**
    * Finds the proxy configuration that supports the protocol.
    * 
    * @param protocol The name of the protocol for which a proxy configuration
    *           needs to be retrieved. Cannot be <code>null</code>, or empty.
    *           Note, if there are more than one servers that run the specified
    *           service, this will only return the 1st one it found, ignore the
    *           rest.
    * 
    * @return The proxy configuration found for the protocol. May be
    *         <code>null</code> if no matches were found.
    */
   public PSProxyConfig findByProtocol(String protocol);
   
   public boolean configFileExists(); 

}
