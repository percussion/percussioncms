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
package com.percussion.rx.design.impl;

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.server.PSServer;
import com.percussion.util.IOTools;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

public class PSSharedGroupModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException("load(IPSGuid) is not currently "
            + "implemented for design objects of type " + getType().name());
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public Long getVersion(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      Long version = null;
    
      PSContentEditorSharedDef sharedDef = 
         PSServer.getContentEditorSharedDef();
      if (sharedDef != null)
      {
         PSSharedFieldGroup group = sharedDef.getSharedGroup(name);
         if (group != null)
         {
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            version = IOTools.getChecksum(PSXmlDocumentBuilder.toString(
                  group.toXml(doc)));
         }
      }
    
      return version;
   }
   
   @Override
   public void delete(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      try
      {
      PSContentEditorSharedDef sharedDef = 
         PSServer.getContentEditorSharedDef();
      if (sharedDef != null)
      {
         PSSharedFieldGroup group = sharedDef.getSharedGroup(name);
         if (group != null)
         {
            sharedDef.removeFieldGroup(group);
            PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
            os.saveContentEditorSharedDefFile(sharedDef);
         }
      }
      }
      catch(Exception e)
      {
         String msg = "Failed to delete the shared group with name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }
}
