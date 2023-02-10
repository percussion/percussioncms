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
package com.percussion.cms.objectstore.server.util;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUIDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to find fields in the content types.
 * 
 * @author stephenbolton
 * 
 */
public class PSFieldFinderUtil
{

   /**
    * Finds fields in a {@link PSDisplayMapper} based upon the existence of a specific
    * property name and value in the control properties. This does not test
    * against the dynamic generated value but the display value of the
    * configuration as seen in the properties dialog of workbench
    * 
    * @param mapper the {@link PSDisplayMapper}
    * @param propertyName not <code>null</code>
    * @param propertyValue not <code>null</code>
    * @return List of String field names
    */
   private static List<String> getFields(PSDisplayMapper mapper, String propertyName, String propertyValue)
   {
      List<String> fieldNames = new ArrayList<>();
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String name = mapping.getFieldRef();
         PSControlRef controlMeta = mapping.getUISet().getControl();
         if (controlMeta!=null) {
         Iterator params = controlMeta.getParameters();
         while (params.hasNext())
         {
            PSParam param = (PSParam) params.next();
            if (param.getName().equals(propertyName) && param.getValue().getValueDisplayText().equals(propertyValue))
            {
               fieldNames.add(name);
            }
         }
         }
         PSDisplayMapper childMapper = mapping.getDisplayMapper();

         if (childMapper != null)
         {
            fieldNames.addAll(getFields(childMapper, propertyName, propertyValue));
         }
      }
      return fieldNames;
   }

   /**
    * Finds fields in a {@link PSItemDefinition} based upon the existence of a specific
    * property name and value in the control properties. This does not test
    * against the dynamic generated value but the display value of the
    * configuration as seen in the properties dialog of workbench
    * 
    * @param def the {@link PSItemDefinition}
    * @param propertyName not <code>null</code>
    * @param propertyValue not <code>null</code>
    * @return List of String field names
    */
   public static List<String> getFields(PSItemDefinition def, String propertyName, String propertyValue)
   {

      PSContentEditorPipe pipe = (PSContentEditorPipe) def.getContentEditor().getPipe();
      if (null == pipe)
         throw new RuntimeException("Missing pipe on content editor.");

      PSContentEditorMapper mapper = pipe.getMapper();
      PSUIDefinition uidef = mapper.getUIDefinition();
      PSDisplayMapper parent = uidef.getDisplayMapper();
      return getFields(parent, propertyName, propertyValue);

   }
}
