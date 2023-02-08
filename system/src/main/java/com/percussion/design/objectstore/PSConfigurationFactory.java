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
package com.percussion.design.objectstore;

import org.w3c.dom.Document;

/**
 * The factory class that constructs appropriate <code>IPSConfig</code>
 * instance based on the requested configuration names.
 */
public class PSConfigurationFactory 
{   
   /**
    * Gets the appropriate configuration instance from the supplied 
    * configuration document for the supplied name.
    * 
    * @param configName The name of the configuration to get, may not be <code>
    * null</code> or empty and must be one of the existing configurations (data
    * in PSX_RXCONFIURATIONS). Currently supports {@link #RELATIONSHIPS_CFG} and
    * {@link #CLONE_HANDLERS_CFG}.
    * @param configDoc the configuration document, must confirm to the dtd 
    * required by <code>PSConfig</code>, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSUnknownNodeTypeException if the supplied document does not 
    * represent <code>PSConfig</code> or the rx configuration in the config 
    * document does not represent appropriate configuration object.
    */
   public static IPSConfig getConfiguration(
      String configName, Document configDoc) throws PSUnknownNodeTypeException
   {
      if(configName == null || configName.trim().length() == 0)
         throw new IllegalArgumentException(
            "configName may not be null or empty.");
            
      if(configDoc == null)
         throw new IllegalArgumentException("configDoc may not be null.");
         
      if( configDoc.getDocumentElement() == null)
         throw new IllegalArgumentException(
            "the document element may not be null.");
            
      PSConfig config = new PSConfig(configDoc.getDocumentElement(), null, null);
      configDoc = (Document)config.getConfig();
      
      IPSConfig rxConfig;
      if(configName.equals(RELATIONSHIPS_CFG))
      {
         rxConfig = new PSRelationshipConfigSet(
            configDoc.getDocumentElement(), null, null);
      }
      else if(configName.equals(CLONE_HANDLERS_CFG))
      {
         rxConfig = new PSCloneHandlerConfigSet(
            configDoc.getDocumentElement(), null, null);
      }
      else
         throw new IllegalArgumentException(
            "configName must be one of the <" + RELATIONSHIPS_CFG + ">, <" +
            CLONE_HANDLERS_CFG + ">");
            
      return rxConfig;
   }

   /**
    * The name referencing to the 'relationships' configuration.
    */
   public static final String RELATIONSHIPS_CFG = "relationships";
   
   /**
    * The name referencing to the 'clonehandlers' configuration.
    */
   public static final String CLONE_HANDLERS_CFG = "clonehandlers";   
   
   /**
    * The name of the clone handler configuration that contains system defined
    * process checks.
    */
   public static final String SYS_CLONE_CFG_NAME = "standard";       
}
