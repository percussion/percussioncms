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
