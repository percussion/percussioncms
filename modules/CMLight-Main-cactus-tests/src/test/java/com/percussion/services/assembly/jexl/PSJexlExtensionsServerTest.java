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
package com.percussion.services.assembly.jexl;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.experimental.categories.Category;

/**
 * Test jexl extensions that require the server
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSJexlExtensionsServerTest extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSJexlExtensionsServerTest.class);

   /**
    * Velocity engine instance, statically initialized
    */
   public static VelocityEngine ms_engine = null;

   static
   {
      ms_engine = new VelocityEngine();
      try
      {
         ms_engine.init();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Get a velocity context for the tests
    * 
    * @return the velocity context, never <code>null</code>
    */
   public VelocityContext getContext()
   {
      VelocityContext rval = new VelocityContext();
      Map<String, Object> sys = new HashMap<String, Object>();
      rval.put("sys", sys);

      sys.put("doc", new PSDocumentUtils());
      sys.put("i18n", new PSI18nUtils());
      sys.put("location", new PSLocationUtils());
      sys.put("ext", new PSExtensionUtils());
      sys.put("link", new PSLinkUtils());
      return rval;
   }

   /**
    * Run a single test case through velocity
    * 
    * @param ctx
    * @param template
    * @return the result of the velocity evaluation
    * @throws ParseErrorException
    * @throws MethodInvocationException
    * @throws ResourceNotFoundException
    * @throws IOException
    */
   public String run(VelocityContext ctx, String template)
         throws ParseErrorException, MethodInvocationException,
         ResourceNotFoundException, IOException
   {
      StringWriter out = new StringWriter();
      try
      {
         ms_engine.evaluate(ctx, out, "Velo", template);
      }
      catch (MethodInvocationException e)
      {
         throw e;
      }
      return out.toString();
   }

   /**
    * Perform single test
    * 
    * @param ctx
    * @param inputtemplate
    * @param expectedoutput
    * @throws ParseErrorException
    * @throws MethodInvocationException
    * @throws ResourceNotFoundException
    * @throws IOException
    */
   public void doTest(VelocityContext ctx, String inputtemplate,
         String expectedoutput) throws ParseErrorException,
         MethodInvocationException, ResourceNotFoundException, IOException
   {
      String out = run(ctx, inputtemplate);
      if (!out.equals(expectedoutput))
      {
         System.err.println(out);
      }
      assertEquals(expectedoutput, out);
   }

   /**
    * Test doc utils
    * 
    * @throws Exception
    */
   public void fixme_testDocUtils() throws Exception
   {
      PSSecurityFilter.authenticate(request, response, "admin1", "demo");
      VelocityContext ctx = getContext();
      String template = "$sys.doc.getDocument('../sys_psxCms/contentTypes.xml?sys_contenttype=311')";

      StringWriter out = new StringWriter();
      ms_engine.evaluate(ctx, out, "Velo", template);
      assertTrue(out.toString().contains("<PSXContentType"));
      
      out = new StringWriter();
      String clist = "$sys.doc.getDocument('/Rhythmyx/contentlist?" +
            "sys_deliverytype=filesystem&sys_assembly_context=301&" +
            "sys_contentlist=rffEiFullBinary&sys_context=1&sys_siteid=301')";
      ms_engine.evaluate(ctx, out, "Velo", clist);
      assertTrue(out.toString().contains("<contentlist"));
   }

   /**
    * @throws Exception
    */
   public void testExtUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      doTest(ctx, "$sys.ext.call('global/percussion/udf/', 'concat','a','b')",
            "ab");
   }

   /**
    * @throws Exception
    */
   public void testI18NUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      doTest(
            ctx,
            "$sys.i18n.getString('7006','en-us')",
            "The extension call does not have the appropriate number of parameters. {0} parameters were expected, {1} parameters were supplied.");
   }

   /**
    * @throws Exception
    */
   public void fixme_testLocationUtils() throws Exception
   {
      IPSRhythmyxInfo info = PSRhythmyxInfoLocator.getRhythmyxInfo();
      PSSecurityFilter.authenticate(request, response, "admin1", "demo");
      VelocityContext ctx = getContext();
      Map<String, String[]> params = new HashMap<String, String[]>();
      params.put("sys_variantid", new String[]
      {"332"});
      params.put("sys_contentid", new String[]
      {"500"});
      params.put("sys_revision", new String[]
      {"2"});
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyItem item = asm.createAssemblyItem();
      item.setParameters(params);
      item.normalize();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(item.getId());
      List<Node> nodes = cmgr.findItemsByGUID(ids, null);

      ctx.put("item", item);
      ctx.put("node", nodes.get(0));
      doTest(
            ctx,
            "$sys.location.generate($item)",
            "http://localhost:"
                  + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT)
                  + "/Rhythmyx/"
                  + IPSAssemblyService.ASSEMBLY_URL
                  + "?sys_revision=2&amp;sys_authtype=0&amp;sys_context=0&amp;sys_contentid=500&amp;sys_variantid=546");
      doTest(
            ctx,
            "$sys.location.generate($item,'rffPgCiPressRelease')",
            "http://localhost:"
                  + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT)
                  + "/Rhythmyx/"
                  + IPSAssemblyService.ASSEMBLY_URL
                  + "?sys_revision=2&amp;sys_authtype=0&amp;sys_context=0&amp;sys_contentid=500&amp;sys_variantid=546");
      doTest(
            ctx,
            "$sys.location.generate('rffPgEiPressRelease',$node,'//Sites/EnterpriseInvestments','public',301,1)",
            "/item500.html");

      doTest(ctx, "$sys.location.siteBase('301','no')", "http://127.0.0.1:"
            + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT) + "/EI_Home");
      doTest(ctx, "$sys.location.siteBase('301','yes')", "/EI_Home");
   }

   /**
    * @throws Exception
    */
   public void testLinkUtils() throws Exception
   {
      IPSRhythmyxInfo info = PSRhythmyxInfoLocator.getRhythmyxInfo();
      VelocityContext ctx = getContext();
      // This first won't be different without https enabled.
      doTest(ctx, "$sys.link.getAbsUrl('../rxs_foo', true)",
            "http://localhost:"
                  + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT)
                  + "/Rhythmyx/rxs_foo");
      doTest(ctx, "$sys.link.getAbsUrl('../rxs_foo', false)",
            "http://localhost:"
                  + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT)
                  + "/Rhythmyx/rxs_foo");
      // OK result given that this is from a cactus test
      doTest(ctx, "$sys.link.getRelUrl('rxs_foo')", "http://127.0.0.1:"
            + info.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT)
            + "/Rhythmyx/ServletRedirector/rxs_foo");
   }
   
   /**
    * Test system utilities for accessing session information.
    * 
    * @throws Exception
    */
   public void testSystemUtils() throws Exception
   {
      PSSessionUtils su = new PSSessionUtils();
      
      assertNotNull(su.getJSessionID());
      assertNotNull(su.getPSSessionID());
   }
}
