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
