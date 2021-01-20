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

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSFieldConverter} class.
 */
@Category(IntegrationTest.class)
public class PSFieldConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testConversion() throws Exception
   {
      // register the item definition used for testing
      PSItemConverterTest.getTestItemDefManager();

      // create the source object
      PSItemField source = createItemField();
      source.setContentType(ms_contentType);
      
      PSItemField target = (PSItemField) roundTripConversion(
         PSItemField.class, 
         com.percussion.webservices.content.PSField.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSItemField[] sourceArray = new PSItemField[1];
      sourceArray[0] = source;
      
      PSItemField[] targetArray = (PSItemField[]) roundTripConversion(
         PSItemField[].class, 
         com.percussion.webservices.content.PSField[].class, sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
   
   /**
    * Test a list of server object conversion to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      PSItemField source = createItemField();
      source.setContentType(ms_contentType);

      List<PSItemField> sourceList = new ArrayList<PSItemField>();
      sourceList.add(source);
      
      List<PSItemField> targetList = roundTripListConversion(
         com.percussion.webservices.content.PSField[].class, sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /* (non-Javadoc)
    * @see PSConverterTestBase#roundTripConversion(Class, Class, Object)
    */
   @Override
   protected Object roundTripConversion(Class serverType, Class clientType, 
      Object source)
   {
      if (serverType == null)
         throw new IllegalArgumentException("serverType cannot be null");
      
      if (clientType == null)
         throw new IllegalArgumentException("clientType cannot be null");
      
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (!source.getClass().getName().equals(serverType.getName()))
         throw new IllegalArgumentException(
            "source must be of type serverType");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(serverType);
      Object clientObject = converter.convert(clientType, source);
      
      // set content type
      String contentType = ms_contentType;
      if (clientObject instanceof com.percussion.webservices.content.PSField)
      {         
         com.percussion.webservices.content.PSField field = 
            (com.percussion.webservices.content.PSField) clientObject;
         field.setContentType(contentType);
      }
      else if (clientObject instanceof com.percussion.webservices.content.PSField[])
      {
         com.percussion.webservices.content.PSField[] fields = 
            (com.percussion.webservices.content.PSField[]) clientObject;
         for (com.percussion.webservices.content.PSField field : fields)
            field.setContentType(contentType);
      }
      
      // convert client to server object
      converter = factory.getConverter(clientType);
      Object serverObject = converter.convert(serverType, clientObject);
      if (serverObject instanceof PSItemField)
      {         
         PSItemField field = (PSItemField) serverObject;
         field.setContentType(contentType);
      }
      else if (serverObject instanceof PSItemField[])
      {
         PSItemField[] fields = (PSItemField[]) serverObject;
         for (PSItemField field : fields)
            field.setContentType(contentType);
      }

      return serverObject;
   }

   /* (non-Javadoc)
    * @see PSConverterTestBase#roundTripListConversion(Class, List)
    */
   @Override
   protected List roundTripListConversion(Class cz, List srcList) 
      throws Exception
   {
      if (! cz.isArray())
         throw new IllegalArgumentException("cz must be an instance of array.");
      
      if (srcList == null)
         throw new IllegalArgumentException("srcList must not be null.");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert from list to array
      Converter converter = factory.getConverter(cz);
      Object[] array = (Object[]) converter.convert(cz, srcList);
      
      // set content type
      if (array instanceof com.percussion.webservices.content.PSField[])
      {
         com.percussion.webservices.content.PSField[] fields = 
            (com.percussion.webservices.content.PSField[]) array;
         for (com.percussion.webservices.content.PSField field : fields)
            field.setContentType(ms_contentType);
      }
      
      // convert from array to list
      converter = factory.getConverter(List.class);
      List target = (List) converter.convert(List.class, array);
      
      return target;
   }

   /**
    * Create as item field for testing.
    * 
    * @return the new item field, never <code>null</code>.
    */
   private PSItemField createItemField() throws Exception
   {
      String fieldName = "sys_title";
      
      PSField fieldDef = getFieldDef(ms_contentType, fieldName);
      PSUISet uiDef = getUiDef(ms_contentType, fieldName);
      
      PSItemField field = new PSItemField(fieldDef, uiDef, false);
      
      return field;
   }
   
   /**
    * Get the field definition for the specified name.
    * 
    * @param contentType the content type name for which to get the field 
    *    definition, not <code>null</code> or empty.
    * @param name the name for the field to get the definition for, not
    *    <code>null</code> or empty.
    * @return the requested field definition, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static PSField getFieldDef(String contentType, String name)
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      PSItemDefManager itemDefMgr = PSItemConverterTest.getTestItemDefManager();
      
      try
      {
         PSItemDefinition def = itemDefMgr.getItemDef(contentType, 
            PSItemDefManager.COMMUNITY_ANY);
         return def.getFieldByName(name);
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new ConversionException(
            "Unregistered content type : " + contentType);
      }
   }
   
   /**
    * Get the UI definition for the specified field name.
    * 
    * @param contentType the content type name for which to get the UI 
    *    definition, not <code>null</code> or empty.
    * @param name the name of the field for which to get the UI definition,
    *    not <code>null</code> or empty.
    * @return the first UI definition found for the specified field name,
    *    may be <code>null</code> if none was found.
    * @throws Exception for any error.
    */
   public static PSUISet getUiDef(String contentType, String name) 
      throws Exception
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");

      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      PSItemDefManager itemDefMgr = PSItemConverterTest.getTestItemDefManager();
      
      try
      {
         PSItemDefinition def = itemDefMgr.getItemDef(contentType, 
            PSItemDefManager.COMMUNITY_ANY);
         PSContentEditorPipe pipe = 
            (PSContentEditorPipe) def.getContentEditor().getPipe();
         PSDisplayMapping mapping = 
            pipe.getMapper().getUIDefinition().getMapping(name);
         if (mapping == null)
            throw new ConversionException(
               "Unknown field name " + name +
               " in item definition for content type " + contentType);
         
         return mapping.getUISet();
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new ConversionException(
            "Unregistered content type : " + contentType);
      }
   }
   
   /**
    * The content type used for testing.
    */
   private static final String ms_contentType = "Press Release";
}
