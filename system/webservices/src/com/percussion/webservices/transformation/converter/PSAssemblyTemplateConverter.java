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
package com.percussion.webservices.transformation.converter;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateBindingsBinding;
import com.percussion.webservices.assembly.data.PublishType;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts objects between the classes
 * <code>com.percussion.services.assembly.data.PSAssemblyTemplate</code> and
 * <code>com.percussion.webservices.assembly.data.PSAssemblyTemplate</code>.
 */
public class PSAssemblyTemplateConverter extends PSConverter
{

   private static final Logger log = LogManager.getLogger(PSAssemblyTemplateConverter.class);

   public PSAssemblyTemplateConverter(BeanUtilsBean beanUtils) {
      super(beanUtils);

      m_specialProperties.add("id");
      m_specialProperties.add("bindings");
      m_specialProperties.add("globalTemplate");
      m_specialProperties.add("slots");
      m_specialProperties.add("sites");
      m_specialProperties.add("stylesheet");
      m_specialProperties.add("aatype");
      m_specialProperties.add("relationshiptype");
      m_specialProperties.add("publishWhen");
      m_specialProperties.add("whenToPublish");
   }

   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      if (isClientToServer(value))
      {
         com.percussion.webservices.assembly.data.PSAssemblyTemplate orig = (com.percussion.webservices.assembly.data.PSAssemblyTemplate) value;

         PSAssemblyTemplate dest = (PSAssemblyTemplate) result;

         // convert id
         Long id = orig.getId();
         if (id != null)
            dest.setGUID(new PSDesignGuid(id));

         // convert global template
         if (orig.getGlobalTemplate() != 0)
            dest.setGlobalTemplate(new PSDesignGuid(orig.getGlobalTemplate()));

         // Convert bindings
         if (orig.getBindings() != null)
         {
            for (PSAssemblyTemplateBindingsBinding binding : orig.getBindings())
            {
               PSTemplateBinding newbinding = new PSTemplateBinding(binding.getVariable(), binding
                     .getExpression());
               dest.addBinding(newbinding);
            }
         }

         // convert slots
         Reference[] origSlots = orig.getSlots();
         List<IPSGuid> slotIds = new ArrayList<>();
         for (Reference origSlot : origSlots)
            slotIds.add(new PSDesignGuid(origSlot.getId()));
         if (!slotIds.isEmpty()) {
            try {
               dest.setSlots(new HashSet<>(loadSlots(slotIds)));
            } catch (PSAssemblyException e) {
               log.warn(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
         }

         // convert stylesheet
         dest.setStyleSheetPath(orig.getStylesheet());

         // convert relationship type
         dest.setActiveAssemblyType(IPSAssemblyTemplate.AAType
               .valueOf(StringUtils.capitalize(orig.getRelationshipType())));

         // convert publish type
         Converter converter = PSTransformerFactory.getInstance().getConverter(
               IPSAssemblyTemplate.PublishWhen.class);
         dest.setPublishWhen((IPSAssemblyTemplate.PublishWhen) converter
               .convert(IPSAssemblyTemplate.PublishWhen.class, orig
                     .getWhenToPublish()));
      }
      else
      {
         PSAssemblyTemplate orig = (PSAssemblyTemplate) value;

         com.percussion.webservices.assembly.data.PSAssemblyTemplate dest = (com.percussion.webservices.assembly.data.PSAssemblyTemplate) result;

         // convert id
         IPSGuid guid = orig.getGUID();
         if (guid != null)
            dest.setId(new PSDesignGuid(guid).getValue());

         // convert global template
         if (orig.getGlobalTemplate() != null)
         {
            final IPSGuid templateGuid = orig.getGlobalTemplate();
            dest.setGlobalTemplate(new PSDesignGuid(templateGuid).getValue());
         }

         // Convert bindings
         List<PSTemplateBinding> bindings = (List) orig.getBindings();
         if (bindings != null)
         {
            PSAssemblyTemplateBindingsBinding[] barr =
               new PSAssemblyTemplateBindingsBinding[bindings.size()];
            int count = 0;
            for (PSTemplateBinding binding : bindings)
            {
               PSAssemblyTemplateBindingsBinding newb = new PSAssemblyTemplateBindingsBinding(
                     binding.getBindingId(), binding.getVariable(), binding
                           .getExpression(),count+1);
               barr[count++] = newb;
            }
            dest.setBindings(barr);
         }

         // convert slots
         Set<IPSTemplateSlot> origSlots = orig.getSlots();
         Reference[] destSlots = new Reference[origSlots.size()];
         dest.setSlots(destSlots);
         int index = 0;
         for (IPSTemplateSlot origSlot : origSlots)
         {
            Reference destSlot = new Reference(new PSDesignGuid(origSlot
                  .getGUID()).getValue(), origSlot.getName());

            destSlots[index++] = destSlot;
         }

         // convert stylesheet
         dest.setStylesheet(orig.getStyleSheetPath());

         // convert relationship type
         dest.setRelationshipType(orig.getActiveAssemblyType().toString());

         // convert publish type
         Converter converter = PSTransformerFactory.getInstance().getConverter(
               IPSAssemblyTemplate.PublishWhen.class);
         dest.setWhenToPublish((PublishType) converter.convert(
               PublishType.class, orig.getPublishWhen()));
      }

      return result;
   }

   /**
    * Loads the slots for the given slot guids from the server using the
    * assembly service.
    * 
    * @param slotIds soltids for which the slots are to be loaded, must not be
    *           <code>null</code> or empty.
    * @return collection of loaded slots, never <code>null</code> or empty.
    */
   protected Collection<IPSTemplateSlot> loadSlots(List<IPSGuid> slotIds) throws PSAssemblyException {
      if (slotIds == null || slotIds.isEmpty())
      {
         throw new IllegalArgumentException("slotIds must not be null or empty");
      }
      return PSAssemblyServiceLocator.getAssemblyService().loadSlots(slotIds);
   }
}
