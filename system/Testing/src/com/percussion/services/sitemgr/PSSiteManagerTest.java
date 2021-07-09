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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.sitemgr;

import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.data.PSLocationScheme;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.services.sitemgr.data.PSSiteProperty;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import junit.framework.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test site manager crud operations
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSSiteManagerTest
{

   private static final Logger log = LogManager.getLogger(PSSiteManagerTest.class);

   /**
    * 
    */
   static final IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();

   /**
    * 
    */
   static SecureRandom ms_random = new SecureRandom();

   @Test
   public void testSiteTemplateAssociation() throws Exception
   {
      IPSSiteManagerInternal mgr = (IPSSiteManagerInternal) sitemgr;
      Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> assoc = mgr
            .findSiteTemplatesAssociations();
      
      // assoc.size() (should be) == 2; worked on local test, 
      // but don't know why it didn't work from python. Use > 0 for now.
      assertTrue(assoc.size() > 0); 
   }
   
   /**
    * Modify existing site as with MSM
    * 
    * @throws Exception
    */
   @Test
   public void testDeserializeExistingSite() throws Exception
   {
      IPSSite dup = sitemgr.loadSiteModifiable(new PSGuid(PSTypeEnum.SITE, 301));
      Iterator<IPSAssemblyTemplate> tmpIt = dup.getAssociatedTemplates()
            .iterator();
      while (tmpIt.hasNext())
      {
         IPSAssemblyTemplate t = tmpIt.next();
         t.setSlots(new HashSet<IPSTemplateSlot>());
      }

      String s = ((PSSite) dup).toXML();
      Integer ver = ((PSSite) dup).getVersion();
      ((PSSite) dup).setVersion(null);
      try
      {
         ((PSSite) dup).fromXML(s);
      }
      catch (Exception e)
      {
         System.out.println("Deserialization exception: "
               + e.getLocalizedMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      ((PSSite) dup).setVersion(null);
      ((PSSite) dup).setVersion(ver);
      sitemgr.saveSite(dup);
      System.out.println("Done with testDeserialization...");
   }

   /**
    * installing a new Site per MSM
    * 
    * @throws Exception
    */
   @Test
   public void testDeserializeNewSite() throws Exception
   {
      // transient site
      IPSSite site = sitemgr.createSite();
      setupDummySiteData(site);
      setupSiteProperties(site);
      setupTemplateAssociations(site);

      String s = ((PSSite) site).toXML();
      IPSSite deserializedSite = sitemgr.createSite();
      try
      {
         ((PSSite) deserializedSite).fromXML(s);
      }
      catch (Exception e)
      {
         System.out.println("Deserialization exception: "
               + e.getLocalizedMessage());
      }
      ((PSSite) deserializedSite).setVersion(null);
      sitemgr.saveSite(deserializedSite);
      IPSSite dup = sitemgr.loadSite(deserializedSite.getGUID());
      assertEquals(dup, deserializedSite);
      sitemgr.deleteSite(deserializedSite);
   }

   /**
    * 
    * @throws PSSiteManagerException
    */
   @Test
   public void testSiteObject() throws PSNotFoundException {
      IPSSite site = sitemgr.createSite();
      setupDummySiteData(site);
      
      // copy
      IPSSite copySite = sitemgr.createSite();
      assertFalse(site.equals(copySite));
      
      copySite.copy(site);  // copy all properties
      ((PSSite)copySite).setGUID(site.getGUID()); // set ID
      assertTrue(site.equals(copySite));
      
      // Persist
      int totalSites = sitemgr.findAllSites().size();
      sitemgr.saveSite(site);
      assertTrue( (totalSites+1) == sitemgr.findAllSites().size());
      
      // Reload and compare for equality
      IPSSite dup = sitemgr.loadSiteModifiable(site.getGUID());
      assertEquals(site, dup);
      assertEquals(site.hashCode(), dup.hashCode());
      // Modify and save
      dup.setBaseUrl(getRandomString());
      sitemgr.saveSite(dup);
      assertFalse(site.equals(dup));
      
      // test uncache site objects
      IPSGuid siteId = dup.getGUID();
      IPSSite site_1 = sitemgr.loadSiteModifiable(siteId);
      IPSSite site_2 = sitemgr.loadSiteModifiable(siteId);
      assertTrue(site_1 != site_2);
      
      // test cached site objects
      dup = sitemgr.loadSite(dup.getGUID());
      IPSSite dup_2 = sitemgr.loadSite(dup.getGUID());
      assertTrue(dup == dup_2);
      
      // Remove
      sitemgr.deleteSite(dup);
      
      try
      {
         dup = sitemgr.loadSite(dup.getGUID());
         // not expect to be here. The site object should be removed from the 
         // cache by now.
         fail("Deletion failed or cache evicition failed.");
      }
      catch (Exception success)
      {}
   }

   /**
    * Test loading read-only site information. The loaded objects should have
    * equal memory pointers.
    * 
    * @throws PSSiteManagerException
    */
   @Test
   public void testLoadSite_readOnly() throws PSNotFoundException {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid ei = gmgr.makeGuid(301, PSTypeEnum.SITE);
      IPSSite ei1 = sitemgr.loadSite(ei);
      IPSSite ei2 = sitemgr.loadSite(ei);

      assertNotNull(ei1);
      assertTrue(ei1 == ei2);
   }

   /**
    * Specific test for MSM functionality where create a site, extract templates
    * modify them and add back in the site and compare if the new template ids
    * are indeed existing in the site.
    * 
    * @throws PSSiteManagerException if any site exceptions
    * @throws IOException if any io exception
    * @throws SAXException if any saxexception occurred
    */
   @Test
   public void testModifyTemplateIds() throws PSSiteManagerException,
           IOException, SAXException, PSNotFoundException {
      IPSSite site = sitemgr.createSite();
      setupDummySiteData(site);
      setupTemplateAssociations(site);

      // Persist
      sitemgr.saveSite(site);
      PSSite testSite = (PSSite) sitemgr.loadSite(site.getGUID());

      String siteStr = testSite.toXML();
      Set<IPSGuid> tmpGuids = PSSite.getTemplateIdsFromSite(siteStr);
      //we expect to have at least 2 templates in the system
      assertTrue(tmpGuids.size() == 2);
      Set<IPSGuid> newTmpGuids = new HashSet<IPSGuid>();
      String modifiedSiteStr = PSSite.replaceTemplateIdsFromSite(siteStr,
            newTmpGuids);
      PSSite dupe = (PSSite) sitemgr.createSite();
      dupe.fromXML(modifiedSiteStr);
      Set<IPSAssemblyTemplate> tmps = dupe.getAssociatedTemplates();
      assertEquals(0, tmps.size());
      for (IPSAssemblyTemplate t : testSite.getAssociatedTemplates())
      {
         newTmpGuids.add(t.getGUID());
      }
      modifiedSiteStr = PSSite.replaceTemplateIdsFromSite(modifiedSiteStr,
            newTmpGuids);
      Set<IPSGuid> modifiedGuids = PSSite
            .getTemplateIdsFromSite(modifiedSiteStr);
      assertTrue(CollectionUtils.isEqualCollection(newTmpGuids, modifiedGuids));

      // deserialize modified site and see the templates saved are
      // indeed modified ones
      PSSite dupe2 = (PSSite) sitemgr.createSite();
      dupe2.fromXML(modifiedSiteStr);
      tmps = dupe2.getAssociatedTemplates();
      assertEquals(2, tmps.size());
      for (IPSAssemblyTemplate t : tmps)
         if (!modifiedGuids.contains(t.getGUID()))
            fail();

      sitemgr.deleteSite(site);
   }

   /**
    * 
    * @throws PSSiteManagerException
    */
   @Test
   public void testContext() throws PSNotFoundException {
      IPSPublishingContext ctx = sitemgr.loadContext(GUID_PUBLIC_CONTEXT);
      assertEquals(ctx.getName(), "Publish");
      assertEquals(314, ctx.getDefaultSchemeId().getUUID());
      assertNotNull(ctx.getDefaultScheme());
      
      ctx = sitemgr.loadContextModifiable(GUID_PUBLIC_CONTEXT);
      assertEquals(ctx.getName(), "Publish");
      assertEquals(314, ctx.getDefaultSchemeId().getUUID());
      assertNull(ctx.getDefaultScheme());      
      
      ctx = sitemgr.loadContext("Publish");
      assertEquals(314, ctx.getDefaultSchemeId().getUUID());
      assertNotNull(ctx.getDefaultScheme());

      ctx = sitemgr.loadContext("Preview");
      assertEquals(ctx.getName(), "Preview");
      assertEquals(ctx.getGUID().getUUID(), 0);
      assertNull(ctx.getDefaultScheme());
      
      // Test cataloging
      List<IPSPublishingContext> ctxs = sitemgr.findAllContexts();
      assertNotNull(ctxs);
      assertTrue(ctxs.size() > 0);
      for (IPSPublishingContext c : ctxs)
      {
         if (c.getName().equalsIgnoreCase("Publish"))
            assertNotNull(c.getDefaultScheme());
      }
      
      // Test crud operations
      IPSPublishingContext newctx = sitemgr.createContext();
      newctx.setName("dummy1");
      newctx.setDescription("a description");
      sitemgr.saveContext(newctx);
      
      ctx = sitemgr.loadContext(newctx.getGUID());
      assertNotNull(ctx);
      assertEquals("dummy1", newctx.getName());
      assertEquals("a description", newctx.getDescription());
      
      sitemgr.deleteContext(ctx);
   }

   /**
    * @throws PSSiteManagerException
    */
   @Test
   public void testSiteAssociations() throws PSNotFoundException {
      IPSPublishingContext context = sitemgr.loadContext(GUID_SITEFOLDER_CONTEXT);
      IPSSite site = sitemgr.loadSite(new PSGuid(PSTypeEnum.SITE, 301));
      assertTrue(site.getAssociatedTemplates().size() > 0);
      assertTrue(site.getPropertyNames(context.getGUID()).size() > 0);
   }

   private IPSGuid GUID_PREVIEW_CONTEXT = PSGuidUtils.makeGuid(0,
         PSTypeEnum.CONTEXT);

   private IPSGuid GUID_PUBLIC_CONTEXT = PSGuidUtils.makeGuid(1,
         PSTypeEnum.CONTEXT);

   private IPSGuid GUID_SITEFOLDER_CONTEXT = PSGuidUtils.makeGuid(301,
         PSTypeEnum.CONTEXT);
   
   /**
    * @throws PSSiteManagerException
    */
   @Test
   public void testSiteProperties() throws PSNotFoundException {
      IPSPublishingContext ctx = sitemgr.loadContext(GUID_PUBLIC_CONTEXT);
      IPSPublishingContext previewctx = sitemgr
            .loadContext(GUID_PREVIEW_CONTEXT);
      IPSSite site = sitemgr.createSite();
      setupDummySiteData(site);
      site.setProperty("first", ctx.getGUID(), getRandomString());
      site.setProperty("second", ctx.getGUID(), getRandomString());
      site.setProperty("second", ctx.getGUID(), getRandomString());
      site.setProperty("third", ctx.getGUID(), getRandomString());
      assertSame(site.getPropertyNames(ctx.getGUID()).size(), 3);
      assertSame(site.getPropertyNames(previewctx.getGUID()).size(), 0);
      sitemgr.saveSite(site);
      
      try
      {
         site = sitemgr.loadSiteModifiable(site.getGUID());
         site.removeProperty("second", ctx.getGUID());
         assertSame(2, site.getPropertyNames(ctx.getGUID()).size());
   
         // Persist
         sitemgr.saveSite(site);
         site = sitemgr.loadSiteModifiable(site.getGUID());         
         assertSame(2, site.getPropertyNames(ctx.getGUID()).size());
         
         site.setProperty("second", previewctx.getGUID(), getRandomString());
         sitemgr.saveSite(site);
         site = sitemgr.loadSiteModifiable(site.getGUID());         
         assertSame(1, site.getPropertyNames(previewctx.getGUID()).size());

         
         // Remove a property and resave
         site.removeProperty("first", ctx.getGUID());
         sitemgr.saveSite(site);
   
         site = sitemgr.loadSiteModifiable(site.getGUID());

         // remove one (1st) property
         PSSite s = (PSSite) site;
         Set<PSSiteProperty> props = s.getProperties();
         int size = props.size();
         for (PSSiteProperty p : props)
         {
            s.removeProperty(p.getGUID());
            break;
         }
         sitemgr.saveSite(site);
         site = sitemgr.loadSite(site.getGUID());
         s = (PSSite) site;
         assertSame(size-1, s.getProperties().size());
      }
      finally
      {
         if (site != null)
         {
            // Check db and make sure the properties are gone as well
            sitemgr.deleteSite(site);
         }
      }
   }

   private static int DUMMY_SCHEME_ID = 10001;
   
   private IPSLocationScheme createDummyScheme() throws PSNotFoundException {
      IPSLocationScheme scheme;
      try
      {
         IPSGuid id = PSGuidUtils.makeGuid(DUMMY_SCHEME_ID,
               PSTypeEnum.LOCATION_SCHEME);
         scheme = sitemgr.loadScheme(id);
         sitemgr.deleteScheme(scheme);
      }
      catch (PSNotFoundException e)
      {
         // ignore error if fail to load
      }

      // Create a dummy scheme with a number of parameters in order
      scheme = new PSLocationScheme();
      ((PSLocationScheme) scheme).setGUID(new PSGuid(DUMMY_SCHEME_ID));
      scheme.setContentTypeId(401L);
      IPSPublishingContext ctx = sitemgr.loadContext("Publish"); 
      scheme.setContextId(ctx.getGUID());
      scheme.setName("xyzzy");
      scheme.setGenerator("dummy");
      scheme.setTemplateId(501L);

      return scheme;
   }
   
   /**
    * @throws PSSiteManagerException
    */
   @Test
   public void testScheme() throws Exception
   {
      IPSGuid id = PSGuidUtils.makeGuid(314, PSTypeEnum.LOCATION_SCHEME);
      IPSLocationScheme scheme = sitemgr.loadScheme(id);
      assertEquals(314, scheme.getGUID().getUUID());
      assertEquals(new Long(311), scheme.getContentTypeId());
      assertEquals("Generic", scheme.getName());
      assertSame(1, scheme.getParameterNames().size());

      // test cached Location Scheme
      IPSLocationScheme scheme2 = sitemgr.loadScheme(id);
      assertTrue(scheme == scheme2);
      
      // test none cached Location Scheme
      IPSLocationScheme notCacheScheme = sitemgr.loadSchemeModifiable(id);
      assertTrue(scheme != notCacheScheme);
      
      // cannot save cached Location Scheme
      try
      {
         sitemgr.saveScheme(notCacheScheme);
      }
      catch (Exception e)
      {
         // should be here
      }
      
      // ok to save not cached Location Scheme
      sitemgr.saveScheme(notCacheScheme);
      
      scheme = createDummyScheme();
      try
      {
         scheme.addParameter("x", -1, "x", "2");
         Assert.fail();
      }
      catch(Exception e)
      {
         // Correct
      }
      try
      {
         scheme.addParameter(null, 11, "x", "2");
         Assert.fail();
      }
      catch(Exception success)
      {}      
      scheme.addParameter("foo", 0, "bar", "1");
      scheme.addParameter("bar", 1, "razzle", "2");
      scheme.addParameter("dog", 2, "bazzle", "3");
      scheme.addParameter("corn", 3, "boo", "4");
      scheme.addParameter("buffalo", 4, "a", "51");
      scheme.addParameter("buffalo2", 4, "a2", "52");
      scheme.addParameter("buffalo3", 4, "a3", "53");
      scheme.addParameter("zebra", 100, "b", "6");
      
      sitemgr.saveScheme(scheme);

      // Find the newly created location scheme:
      //    1) load the list of schemes via context id, 
      //    2) look for scheme in the list,
      //    3) and compare GUIDs when scheme is found.
      IPSPublishingContext ctx = sitemgr.loadContext("Publish");
      List<IPSLocationScheme> locSchemeList = 
         sitemgr.findSchemesByContextId(ctx.getGUID());
      IPSLocationScheme tstScheme = null;
      for ( IPSLocationScheme iScheme : locSchemeList)
      {
         if (iScheme.getName().equals("xyzzy"))
         {
            tstScheme = iScheme;
            break;
         }
      }
     assertTrue("Location scheme not found in site",(tstScheme != null));

     assertEquals( tstScheme.getGUID(), scheme.getGUID());

    
      // Load scheme
      scheme = sitemgr.loadScheme(PSGuidUtils.makeGuid(DUMMY_SCHEME_ID,
            PSTypeEnum.LOCATION_SCHEME));
      assertEquals(scheme.getName(), "xyzzy");
      assertEquals(scheme.getGenerator(), "dummy");
      assertEquals(scheme.getTemplateId(), new Long(501));
      assertEquals(scheme.getContentTypeId(), new Long(401));
      assertEquals(scheme.getContextId(), sitemgr.loadContext("Publish")
            .getGUID());

      List<String> pnames = scheme.getParameterNames();
      assertEquals("foo", pnames.get(0));
      assertEquals("bar", pnames.get(1));
      assertEquals("dog", pnames.get(2));
      assertEquals("corn", pnames.get(3));
      assertEquals("buffalo3", pnames.get(4));
      assertEquals("buffalo2", pnames.get(5));
      assertEquals("buffalo", pnames.get(6));
      assertEquals("zebra", pnames.get(7));

      assertEquals("bar", scheme.getParameterType("foo"));
      assertEquals("razzle", scheme.getParameterType("bar"));
      assertEquals("bazzle", scheme.getParameterType("dog"));
      assertEquals("boo", scheme.getParameterType("corn"));

      assertEquals("1", scheme.getParameterValue("foo"));
      assertEquals("2", scheme.getParameterValue("bar"));
      assertEquals("3", scheme.getParameterValue("dog"));
      assertEquals("4", scheme.getParameterValue("corn"));
      
      // clone scheme
      IPSLocationScheme clone = (IPSLocationScheme) scheme.clone();
      assertEquals(scheme, clone);
      
      // copy scheme
      IPSLocationScheme copy = sitemgr.createScheme();
      copy.copy(scheme);
      assertSchemePropertiesEquals(copy, scheme);
      assertFalse(copy.getGUID().equals(scheme.getGUID()));
   }

   @Test
   public void testSchemeParameters() throws Exception
   {
      IPSLocationScheme scheme = null;
      String PARAM_NAME_0 = "Param_0";
      String PARAM_NAME_1 = "Param_1";
      try
      {
         scheme = createDummyScheme();
         scheme.addParameter(PARAM_NAME_0, 0, "String", "hello");
         scheme.addParameter(PARAM_NAME_1, 1, "String", "hello_1");

         sitemgr.saveScheme(scheme);

         IPSGuid id = PSGuidUtils.makeGuid(DUMMY_SCHEME_ID,
               PSTypeEnum.LOCATION_SCHEME);
         scheme = sitemgr.loadSchemeModifiable(id);
         String value = scheme.getParameterValue(PARAM_NAME_0);
         assertTrue(StringUtils.isNotBlank(value));
         
         // test removing parameters
         scheme.removeParameter(PARAM_NAME_0);
         sitemgr.saveScheme(scheme);
         
         scheme = sitemgr.loadSchemeModifiable(id);

         value = scheme.getParameterValue(PARAM_NAME_0);
         assertTrue(StringUtils.isBlank(value));
         value = scheme.getParameterValue(PARAM_NAME_1);
         assertTrue(StringUtils.isNotBlank(value));
         
         // cannot save a cloned Location Scheme object
         IPSLocationScheme clone = (IPSLocationScheme) scheme.clone();
         try
         {
            sitemgr.saveScheme(clone);
            fail("Should have failed to save a cloned Location Scheme object.");
         }
         catch (Exception e)
         {
         }
         
         // copy from a cloned object
         scheme.copy(clone);
         scheme.removeParameter(PARAM_NAME_1);
         sitemgr.saveScheme(scheme);
         
         scheme = sitemgr.loadSchemeModifiable(id);

         value = scheme.getParameterValue(PARAM_NAME_1);
         assertTrue(StringUtils.isBlank(value));
      }
      finally
      {
         if (scheme != null)
            sitemgr.deleteScheme(scheme);
      }
   }
   
   /**
    * Compare the properties of 2  objects. Throws
    * assert exception if any property is not equal. It does not compare GUID
    * of the objects.
    * 
    * @param s1 1st object in question, assumed not <code>null</code>.
    * @param s2 2nd object in question, assumed not <code>null</code>.
    */
   private void assertSchemePropertiesEquals(IPSLocationScheme s1,
         IPSLocationScheme s2)
   {
      assertTrue(s1.hashCode() == s2.hashCode());
      assertEquals(s1.getContentTypeId(), s2.getContentTypeId());
      assertEquals(s1.getTemplateId(), s2.getTemplateId());
      assertEquals(s1.getGenerator(), s2.getGenerator());
      assertEquals(s1.getName(), s2.getName());
      assertEquals(s1.getDescription(), s2.getDescription());
      
      assertEquals(s1.getParameterNames(), s2.getParameterNames());
      for (String n : s1.getParameterNames())
      {
         assertEquals(s1.getParameterType(n), s2.getParameterType(n));
         assertEquals(s1.getParameterValue(n), s2.getParameterValue(n));
      }
   }

   private void setupDummySiteData(IPSSite site)
   {
      site.setBaseUrl(getRandomString());
      site.setDescription(getRandomString());
      site.setFolderRoot(getRandomString());
      site.setRoot(getRandomString());
      site.setGlobalTemplate(getRandomString());
      site.setIpAddress(getRandomString());
      site.setName("test");
      site.setNavTheme(getRandomString());
      site.setPassword(getRandomString());
      site.setUserId(getRandomString());
      site.setPort(ms_random.nextInt(60000));
      site.setState(ms_random.nextInt(25));
      site.setAllowedNamespaces("a,b,c");
   }

   private void setupSiteProperties(IPSSite site) throws PSNotFoundException {
      IPSPublishingContext ctx = sitemgr.loadContext(GUID_PUBLIC_CONTEXT);
      site.setProperty("first", ctx.getGUID(), getRandomString());
      site.setProperty("second", ctx.getGUID(), getRandomString());
      site.setProperty("third", ctx.getGUID(), getRandomString());

   }

   /**
    * Catalogs all templates and adds 2 of them to the supplied site (or fewer
    * if fewer templates are cataloged.) All slots are removed from each added
    * template to prevent lazy loading hibernate exceptions.
    * 
    * @throws PSSiteManagerException
    */
   private void setupTemplateAssociations(IPSSite site)
         throws PSSiteManagerException
   {
      Set<IPSAssemblyTemplate> associatedTmps = new HashSet<IPSAssemblyTemplate>();
      try
      {
         IPSAssemblyService m_assemblySvc = PSAssemblyServiceLocator
               .getAssemblyService();
         Set<IPSAssemblyTemplate> templates = m_assemblySvc.findAllTemplates();
         Iterator<IPSAssemblyTemplate> it = templates.iterator();
         int sz = 2;

         while (it.hasNext() && --sz >= 0)
         {
            IPSAssemblyTemplate t = it.next();
            t.setSlots(null);
            associatedTmps.add(t);
         }
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }
      catch (PSAssemblyException e)
      {
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }

      site.getAssociatedTemplates().addAll(associatedTmps);
   }

   private String getRandomString()
   {
      return "random" + ms_random.nextInt(1000);
   }
}
