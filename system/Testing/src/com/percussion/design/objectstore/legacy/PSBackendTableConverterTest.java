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

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSBackendTableConverter} class.
 */

@Ignore
public class PSBackendTableConverterTest extends PSBaseConverterTest
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
      PSConfigurationCtx ctx = (PSConfigurationCtx) new PSConfigurationCtxMock(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      PSBackEndTable convTable;
      boolean didThrow;
      
      // create repository info
      IPSRepositoryInfo repInfo = new PSTableRepositoryInfo(
         new PSLegacyBackEndTable(getTableXml("full-match"), null, null));

      
      // test full match on repository
      convTable = convertTable("full-match", ctx, repInfo, false);
      assertTrue(convTable.getDataSource() == null);
      
      // test no datasource match
      didThrow = false;
      try
      {
         convertTable("jndi-match", ctx, repInfo, false);
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
         convertTable("driver-match", ctx, repInfo, false);
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
         convTable = convertTable("no-match", ctx, repInfo, false);
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
      PSConfigurationCtx ctx = (PSConfigurationCtx) new PSConfigurationCtxMock(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      PSBackEndTable convTable;
      boolean didThrow;
      
      // create repository info
      IPSRepositoryInfo repInfo = new PSTableRepositoryInfo(new PSLegacyBackEndTable(
         getTableXml("full-match"), null, null));
      
      // now test w/conversions
      List<IPSDatasourceConfig> configs;
      PSLegacyBackEndTable srcTable;
      IPSDatasourceConfig config;
      
      // test existing
      configs = getDatasourceConfigs(ctx);
      assertTrue(configs.size() == 1);
      convTable = convertTable("full-match", ctx, repInfo, true);
      assertTrue(convTable.getDataSource() == null);
      // shouldn't have created any
      configs = getDatasourceConfigs(ctx);
      assertTrue(configs.size() == 1);
      
      // test existing jndi ds only
      srcTable = new PSLegacyBackEndTable(getTableXml("jndi-match"), 
         null, null);
      convTable = convertTable("jndi-match", ctx, repInfo, true);
      configs = getDatasourceConfigs(ctx);
      assertTrue(configs.size() == 2);
      assertEquals(convTable.getDataSource(), configs.get(1).getName());
      config = configs.get(1);
      assertEquals(srcTable.getDatabase(), config.getDatabase());
      assertEquals(srcTable.getOrigin(), config.getOrigin());
      
      // test existing driver config only
      // reset the ctx
      ctx = new PSConfigurationCtx(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      List<IPSJndiDatasource> datasources = ctx.getJndiDatasources();
      assertTrue(datasources.size() == 1);
      srcTable = new PSLegacyBackEndTable(getTableXml("driver-match"), 
         null, null);
      convTable = convertTable("driver-match", ctx, repInfo, true);
      configs = getDatasourceConfigs(ctx);
      assertTrue(configs.size() == 2);
      config = configs.get(1);
      assertEquals(convTable.getDataSource(), config.getName());
      assertEquals(srcTable.getDatabase(), config.getDatabase());
      assertEquals(srcTable.getOrigin(), config.getOrigin());
      assertTrue(datasources.size() == 2);
      IPSJndiDatasource newDS = datasources.get(1);
      assertEquals(newDS.getDriverName(), srcTable.getDriver());
      assertEquals(newDS.getServer(), srcTable.getServer());
      assertEquals(newDS.getUserId(), "sa");
      assertEquals(newDS.getPassword(), "demo");
      
      // test no matching driver in update mode
      didThrow = false;
      try
      {
         convTable = convertTable("no-match", ctx, repInfo, true);
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
      PSConfigurationCtx ctx =  new PSConfigurationCtxMock(locator, PSLegacyEncrypter.OLD_SECURITY_KEY());
      PSBackEndTable convTable;
      boolean didThrow;
      
      // create repository info
      IPSRepositoryInfo repInfo = new PSTableRepositoryInfo(
         new PSLegacyBackEndTable(getTableXml("full-match"), null, null));
      
      // test force
      convTable = convertTable("no-match", ctx, repInfo, false, true);
      assertTrue(convTable.getDataSource() == null);
      
      // test force w/ new table
      PSBackEndTable newTable = convertTable("driver-match", ctx, repInfo, 
         true);
      Element newSrc = newTable.toXml(PSXmlDocumentBuilder.createXmlDocument());
      convTable = convertTable(newSrc, ctx, repInfo, false, true);
      assertTrue(convTable.getDataSource() == null);
      assertEquals(convTable.getAlias(), newTable.getAlias());
      assertEquals(convTable.getTable(), newTable.getTable());
      assertEquals(convTable.getId(), newTable.getId());
      
      // test invalid xml
      didThrow = false;
      try
      {
         convTable = convertTable("bad-xml", ctx, repInfo, false, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         convTable = convertTable("bad-xml", ctx, repInfo, false, true);
      }
      catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);  
   }
   
   /**
    * Convenience method calls {@link #convertTable(String, PSConfigurationCtx, 
    * IPSRepositoryInfo, boolean, boolean) 
    * convertTable(name, ctx, repInfo, updateConfigs, false)}
    */
   private PSBackEndTable convertTable(String name, PSConfigurationCtx ctx,
      IPSRepositoryInfo repInfo, boolean updateConfigs) throws Exception
   {
      return convertTable(name, ctx, repInfo, updateConfigs, false);
   }
   
   /**
    * Converts the table configuration identified by the supplied name and
    * tests that the converted table has the same name, alias, and id.  The
    * params, return, and exceptions are the same as 
    * {@link #convertTable(Element, PSConfigurationCtx, 
    * IPSRepositoryInfo, boolean, boolean)} except that the table config name
    * is supplied instead of the first <code>src</code> parameter.
    *  
    * @param name The configuration name, assumed not <code>null</code> or 
    * empty.
    */
   private PSBackEndTable convertTable(String name, PSConfigurationCtx ctx,
      IPSRepositoryInfo repInfo, boolean updateConfigs, boolean forceConversion) 
      throws Exception
   {
      Element src;
      PSLegacyBackEndTable srcTable;
      
      src = getTableXml(name);
      Assert.assertNotNull("Table xml is null", src);
      srcTable = new PSLegacyBackEndTable(src, null, null);
      
      
      PSBackEndTable convTable = convertTable(src, ctx, repInfo, updateConfigs,
         forceConversion);
       
      assertEquals(convTable.getAlias(), srcTable.getAlias());
      assertEquals(convTable.getTable(), srcTable.getTable());
      assertEquals(convTable.getId(), srcTable.getId());

       
      return convTable;
   }   
   
   /**
    * Converts the supplied table xml.
    * 
    * @param src The source xml, assumed not <code>null</code>.
    * @param ctx The config ctx to use, assumed not <code>null</code>.
    * @param repInfo The repository info to use, assumed not <code>null</code>.
    * @param updateConfigs <code>true</code> to update the configs,
    * <code>false</code> to require that all needed datasource configurations
    * exist.
    * @param forceConversion <code>true</code> to simply convert to the
    * repository, <code>false</code> to perform the normal conversion.
    * 
    * @return The resulting table.
    * 
    * @throws Exception If there are any errors.
    */
   private PSBackEndTable convertTable(Element src, PSConfigurationCtx ctx,
      IPSRepositoryInfo repInfo, boolean updateConfigs, boolean forceConversion)
      throws Exception
   {

      PSBackendTableConverter converter = new PSBackendTableConverter(ctx, 
         repInfo, updateConfigs);
      converter.setForcedConversion(forceConversion);
      
      List<IPSComponentConverter> converters = 
         new ArrayList<IPSComponentConverter>(1);
      converters.add(converter);
      PSComponent.setComponentConverters(converters);
      
      return new PSBackEndTable(src, null, null);
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
        PSBackendTableConverterTest.class.getResourceAsStream(
            "/com/percussion/design/objectstore/legacy/table-"
            + name + ".xml"), false);
      return doc.getDocumentElement();
   }

   /**
    * Basic implementation of the repository info inteface
    */
   private class PSTableRepositoryInfo implements IPSRepositoryInfo
   {
      PSLegacyBackEndTable mi_table;
      
      private PSTableRepositoryInfo(PSLegacyBackEndTable table)
      {
         mi_table = table;
      }
      public String getDriver()
      {
         return mi_table.getDriver();
      }

      public String getServer()
      {
         return mi_table.getServer();
      }

      public String getDatabase()
      {
         return mi_table.getDatabase();
      }

      public String getOrigin()
      {
         return mi_table.getOrigin();
      }
      
   }
}

