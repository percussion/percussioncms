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
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSTableLocatorConverter} class.
 */
@Ignore
public class PSTableLocatorConverterTest extends PSBaseConverterTest
{
   /**
    * Test the converter with existing conversions.
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testConversionNoUpdate() throws Exception
   {
      IPSConfigFileLocator locator = initFileLocator();
      
      // create the ctx
      PSConfigurationCtx ctx = new PSConfigurationCtxMock(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      com.percussion.design.objectstore.PSTableLocator convTable;
      boolean didThrow;
      
      // test full match on repository
      convTable = convertTable("full-match", ctx, false);
      assertTrue(convTable.getCredentials().getDataSource() == null);
      
      // test no datasource match
      didThrow = false;
      try
      {
         convertTable("jndi-match", ctx, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      // test no datasource match
      didThrow = false;
      try
      {
         convertTable("driver-match", ctx, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
     
      // test no driver match
      didThrow = false;
      try
      {
         convTable = convertTable("no-match", ctx, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test the converter creating configurations on the fly.
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testConversionWithUpdate() throws Exception
   {
      IPSConfigFileLocator locator = initFileLocator();
      
      // create the ctx
      PSConfigurationCtx ctx = new PSConfigurationCtx(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      com.percussion.design.objectstore.PSTableLocator convTable;
      boolean didThrow;
      
      // now test w/conversions
      List<IPSDatasourceConfig> configs;
      PSLegacyTableLocator srcTable;
      IPSDatasourceConfig config;
      IPSDatasourceResolver resolver;
      
      // test existing
      configs = getDatasourceConfigs(ctx);
      assertEquals(configs.size(), 1);
      convTable = convertTable("full-match", ctx, true);
      assertTrue(convTable.getCredentials().getDataSource() == null);
      // shouldn't have created any
      configs = getDatasourceConfigs(ctx);
      assertEquals(configs.size(), 1);
      
      // test existing jndi ds only
      srcTable = new PSLegacyTableLocator(getTableXml("jndi-match"), 
         null, null);
      convTable = convertTable("jndi-match", ctx, true);
      resolver = getResolver(ctx);
      configs = resolver.getDatasourceConfigurations();
      assertEquals(configs.size(), 2);
      // conversion should always point to default repository ds
      assertTrue(convTable.getCredentials().getDataSource() == null);
      config = configs.get(1);
      assertEquals(resolver.getRepositoryDatasource(), config.getName());
      assertEquals(srcTable.getDatabase(), config.getDatabase());
      assertEquals(srcTable.getOrigin(), config.getOrigin());
      
      // test existing driver config only
      // reset the ctx
      ctx = new PSConfigurationCtx(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      List<IPSJndiDatasource> datasources = ctx.getJndiDatasources();
      assertEquals(datasources.size(), 1);
      srcTable = new PSLegacyTableLocator(getTableXml("driver-match"), 
         null, null);
      convTable = convertTable("driver-match", ctx, true);
      resolver = getResolver(ctx);
      configs = resolver.getDatasourceConfigurations();
      assertEquals(configs.size(), 2);
      // conversion should always point to default repository ds
      assertTrue(convTable.getCredentials().getDataSource() == null);
      config = configs.get(1);
      assertEquals(resolver.getRepositoryDatasource(), config.getName());
      assertEquals(srcTable.getDatabase(), config.getDatabase());
      assertEquals(srcTable.getOrigin(), config.getOrigin());
      assertEquals(datasources.size(), 2);
      IPSJndiDatasource newDS = datasources.get(1);
      assertEquals(newDS.getDriverName(), 
         srcTable.getCredentials().getDriver());
      assertEquals(newDS.getServer(), srcTable.getCredentials().getServer());
      assertEquals(newDS.getUserId(), "sa");
      assertEquals(newDS.getPassword(), "demo");
      
      // test no matching driver in update mode
      didThrow = false;
      try
      {
         convTable = convertTable("no-match", ctx, true);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }
   
   /**
    * Test forced conversion with old, new, and bad xml.
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testForcedConversion() throws Exception
   {
      IPSConfigFileLocator locator = initFileLocator();
      
      // create the ctx
      PSConfigurationCtx ctx = new PSConfigurationCtx(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      com.percussion.design.objectstore.PSTableLocator convTable;
      boolean didThrow;
      
      // test force
      convTable = convertTable("no-match", ctx, false, true);
      assertTrue(convTable.getCredentials().getDataSource() == null);
      
      // test force w/ new table
      assertEquals(ctx.getServerConfig().getBackEndCredentials().size(), 2);
      com.percussion.design.objectstore.PSTableLocator newTable =
         convertTable("driver-match", ctx, true);
      Element newSrc = newTable.toXml(PSXmlDocumentBuilder.createXmlDocument());
      convTable = convertTable(newSrc, ctx, false, true);
      assertTrue(convTable.getCredentials().getDataSource() == null);
      assertEquals(convTable.getAlias(), newTable.getAlias());
      assertEquals(convTable.getId(), newTable.getId());
      assertEquals(convTable.getCredentials().getAlias(), 
         newTable.getCredentials().getAlias());
      assertEquals(convTable.getCredentials().getComment(), 
         newTable.getCredentials().getComment());
      assertEquals(convTable.getCredentials().getConditionals(), 
         newTable.getCredentials().getConditionals());
      assertEquals(convTable.getCredentials().getId(), 
         newTable.getCredentials().getId());
      
      // test invalid xml
      didThrow = false;
      try
      {
         convTable = convertTable("bad-xml", ctx, false, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         convTable = convertTable("bad-xml", ctx, false, true);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);  
   }
   
   
   /**
    * Convenience method calls {@link #convertTable(String, PSConfigurationCtx, 
    * boolean, boolean) 
    * convertTable(name, ctx, updateConfigs, false)}
    */
   private com.percussion.design.objectstore.PSTableLocator convertTable(
      String name, PSConfigurationCtx ctx, boolean updateConfigs) 
         throws Exception
   {
      return convertTable(name, ctx, updateConfigs, false);
   }
   
   /**
    * Converts the table configuration identified by the supplied name and
    * tests that the converted table has the same name, alias, and id.  The
    * params, return, and exceptions are the same as 
    * {@link #convertTable(Element, PSConfigurationCtx, 
    * boolean, boolean)} except that the table config name
    * is supplied instead of the first <code>src</code> parameter.
    *  
    * @param name The configuration name, assumed not <code>null</code> or 
    * empty.
    */
   private com.percussion.design.objectstore.PSTableLocator convertTable(
      String name, PSConfigurationCtx ctx, boolean updateConfigs, 
      boolean forceConversion) throws Exception
   {
      Element src;
      PSLegacyTableLocator srcTable;
      
      src = getTableXml(name);
      srcTable = new PSLegacyTableLocator(src, null, null);
      
      
      com.percussion.design.objectstore.PSTableLocator convTable =
         convertTable(src, ctx, updateConfigs, forceConversion);
       
      assertEquals(convTable.getAlias(), srcTable.getAlias());
      assertEquals(convTable.getId(), srcTable.getId());
      assertEquals(convTable.getCredentials().getAlias(), 
         srcTable.getCredentials().getAlias());
      assertEquals(convTable.getCredentials().getComment(), 
         srcTable.getCredentials().getComment());
      assertEquals(convTable.getCredentials().getConditionals(), 
         srcTable.getCredentials().getConditionals());
      assertEquals(convTable.getCredentials().getId(), 
         srcTable.getCredentials().getId());

       
      return convTable;
   }   
   
   /**
    * Converts the supplied table xml.
    * 
    * @param src The source xml, assumed not <code>null</code>.
    * @param ctx The config ctx to use, assumed not <code>null</code>.
    * @param updateConfigs <code>true</code> to update the configs,
    * <code>false</code> to require that all needed datasource configurations
    * exist.
    * @param forceConversion <code>true</code> to simply convert to the
    * repository, <code>false</code> to perform the normal conversion.
    * @return The resulting table.
    * 
    * @throws Exception If there are any errors.
    */
   private com.percussion.design.objectstore.PSTableLocator convertTable(
      Element src, PSConfigurationCtx ctx, boolean updateConfigs, 
      boolean forceConversion) throws Exception
   {

      PSTableLocatorConverter converter ;

      converter = new PSTableLocatorConverter(ctx, updateConfigs);
      converter.setForcedConversion(forceConversion);
      
      List<IPSComponentConverter> converters = 
         new ArrayList<IPSComponentConverter>(1);
      converters.add(converter);
      PSComponent.setComponentConverters(converters);
      
      return new PSTableLocator(src, null, null);
   }      

   /**
    * Get the specified xml element
    * 
    * @param name Identifies the file, assumed not <code>null</code> or empty.
    * 
    * @return The element, never <code>null</code>.
    * 
    * @throws Exception if there are any errors. 
    */
   private Element getTableXml(String name) throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream("UnitTestResources/com/percussion/design/" +
               "objectstore/legacy/tablelocator-" + name + ".xml"), false);
      return doc.getDocumentElement();
   }
}

