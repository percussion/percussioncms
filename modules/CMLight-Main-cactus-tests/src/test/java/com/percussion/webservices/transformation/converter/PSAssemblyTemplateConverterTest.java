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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSAssemblyTemplateConverter} class.
 */
@Category(IntegrationTest.class)
public class PSAssemblyTemplateConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    */
   public void testConversion() throws Exception
   {
      PSAssemblyTemplateWs source = null;
      
      try
      {
         source = createTemplate("name", getNextId(PSTypeEnum.TEMPLATE));

         PSAssemblyTemplateWs target = (PSAssemblyTemplateWs) roundTripConversion(
               PSAssemblyTemplateWs.class, 
            com.percussion.webservices.assembly.data.PSAssemblyTemplate.class, 
            source);
         
         // verify the the round-trip object is equal to the source object
         assertTrue(source.equals(target));
         
         // create the source array
         PSAssemblyTemplateWs[] sourceArray = new PSAssemblyTemplateWs[1];
         sourceArray[0] = source;
         
         PSAssemblyTemplateWs[] targetArray = (PSAssemblyTemplateWs[]) roundTripConversion(
               PSAssemblyTemplateWs[].class, 
            com.percussion.webservices.assembly.data.PSAssemblyTemplate[].class, 
            sourceArray);
         
         // verify the the round-trip array is equal to the source array
         assertTrue(sourceArray.length == targetArray.length);
         assertTrue(sourceArray[0].equals(targetArray[0]));
      }
      finally
      {
         if (source != null)
         {
            IPSAssemblyService service = 
               PSAssemblyServiceLocator.getAssemblyService();
            
            IPSAssemblyTemplate template = source.getTemplate();
            for (IPSTemplateSlot slot : template.getSlots())
               service.deleteSlot(slot.getGUID());
            service.deleteTemplate(template.getGUID());
         }
      }
   }
   
   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSAssemblyTemplateWs> srcList = new ArrayList<PSAssemblyTemplateWs>();
      
      try
      {
         srcList.add(createTemplate("testTemplate_1", 
            getNextId(PSTypeEnum.TEMPLATE)));
         srcList.add(createTemplate("testTemplate_1", 
            getNextId(PSTypeEnum.TEMPLATE)));
         
         List<PSAssemblyTemplateWs> srcList2 = roundTripListConversion(
            com.percussion.webservices.assembly.data.PSAssemblyTemplate[].class, 
            srcList);
   
         assertTrue(srcList.equals(srcList2));
      }
      finally
      {
         for (PSAssemblyTemplateWs templateWs : srcList)
         {
            IPSAssemblyService service = 
               PSAssemblyServiceLocator.getAssemblyService();

            IPSAssemblyTemplate template = templateWs.getTemplate();
            for (IPSTemplateSlot slot : template.getSlots())
               service.deleteSlot(slot.getGUID());
            service.deleteTemplate(template.getGUID());
         }
      }
   }
   
   /**
    * Creates a template object with the given name.
    * 
    * @param name the name of the object, not <code>null</code> or empty.
    * @return the created object, never <code>null</code>.
    * @throws PSAssemblyException if we cannot save the created template.
    */
   public static PSAssemblyTemplateWs createTemplate(String name, IPSGuid id) 
      throws PSAssemblyException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null");
      
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      PSAssemblyTemplate template = new PSAssemblyTemplate();
      template.setName(name);
      template.setGUID(id);
      template.setLabel(name + "_label");
      template.setLocationPrefix("prefix");
      template.setLocationSuffix("suffix");
      template.setAssembler(name + "_assembler");
      template.setAssemblyUrl(name + "_assemblyUrl");
      template.setStyleSheetPath(name + "_stylesheetPath");
      template.setActiveAssemblyType(IPSAssemblyTemplate.AAType.AutoIndex);
      template.setOutputFormat(IPSAssemblyTemplate.OutputFormat.Snippet);
      template.setPublishWhen(IPSAssemblyTemplate.PublishWhen.Never);
      template.setTemplateType(IPSAssemblyTemplate.TemplateType.Local);
      template.setDescription(name + "_description");
      template.setTemplate(name + "template");
      template.setMimeType(name + "mimeType");
      template.setCharset("UTF8");
      template.setGlobalTemplateUsage(
         IPSAssemblyTemplate.GlobalTemplateUsage.Defined);
      template.setGlobalTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 5555555));

      IPSAssemblyService service = 
         PSAssemblyServiceLocator.getAssemblyService();
      service.saveTemplate(template);
      
      String[] slotNames = 
      {
         "name_1", 
         "name_2", 
         "name_3"
      }; 
      for (int i=0; i<slotNames.length; i++)
      {
         PSTemplateSlot slot = PSTemplateSlotConverterTest.createSlot(
            slotNames[i], getNextId(PSTypeEnum.SLOT));
         template.addSlot(slot);
      }


      // bindings
      template.addBinding(new PSTemplateBinding(1, "$a", "1"));
      template.addBinding(new PSTemplateBinding(2, "$b", "2"));
      template.addBinding(new PSTemplateBinding(3, "$c", "3"));
      
      service.saveTemplate(template);
      
      // make up a list of site references
      Map<IPSGuid,String> sites = new HashMap<IPSGuid,String>();
      sites.put(new PSGuid(PSTypeEnum.SITE, 1), "site_1");
      sites.put(new PSGuid(PSTypeEnum.SITE, 2), "site_2");
      sites.put(new PSGuid(PSTypeEnum.SITE, 3), "site_3");
      sites.put(new PSGuid(PSTypeEnum.SITE, 1000), "site_1000");
      sites.put(new PSGuid(PSTypeEnum.SITE, 1001), "site_1001");
      sites.put(new PSGuid(PSTypeEnum.SITE, 1002), "site_1002");
      sites.put(new PSGuid(PSTypeEnum.SITE, 1003), "site_1003");
      
      
      return new PSAssemblyTemplateWs(template, sites);
   }
}

