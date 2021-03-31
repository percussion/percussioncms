/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.utils PSOSlotContentsTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOSlotContentsTest extends TestCase
{
   private static Log log = LogFactory.getLog(PSOSlotContentsTest.class); 
   
   JUnit4Mockery mocks = new JUnit4Mockery() {{
       setImposteriser(ClassImposteriser.INSTANCE);
       }};
   
   private List<PSAaRelationship> rels = new ArrayList<PSAaRelationship>();

   IPSTemplateSlot ourSlot; 
   IPSTemplateSlot otherSlot;
   IPSAssemblyTemplate template; 
   
   /**
    * @param name
    */
   public PSOSlotContentsTest(String name)
   {
      super(name);
   }
   /**
    * @see junit.framework.TestCase#setUp()
    */
   @Before
   protected void setUp() throws Exception
   {
      super.setUp();
      
      
   }
   /**
    * Test method for {@link com.percussion.pso.utils.PSOSlotContents#PSOSlotContents()}.
    */
   @Test
   public void testPSOSlotContents()
   {
      PSOSlotContents contents = new PSOSlotContents(); 
                
      final PSLocator parent = new PSLocator(1,1); 

      final IPSGuid slot1 = new PSLegacyGuid(1L); 
      final IPSGuid slot2 = new PSLegacyGuid(2L);
      
      final PSAaRelationship rel1 = mocks.mock(PSAaRelationship.class, "rel1"); 
      final PSAaRelationship rel2 = mocks.mock(PSAaRelationship.class, "rel2"); 
      final PSAaRelationship rel3 = mocks.mock(PSAaRelationship.class, "rel3"); 
      final PSAaRelationship rel4 = mocks.mock(PSAaRelationship.class, "rel4"); 

      rels.add(rel1);
      rels.add(rel2);
      rels.add(rel3);
      rels.add(rel4);
      
      final IPSContentWs cws = mocks.mock(IPSContentWs.class, "cws"); 
      final IPSGuidManager gmgr = mocks.mock(IPSGuidManager.class, "gmgr"); 
     
      try
      {
         mocks.checking(new Expectations(){{
            atLeast(1).of(cws).loadContentRelations(with(any(PSRelationshipFilter.class)), with(equal(true)));will(returnValue(rels));
            ignoring(gmgr).makeLocator(with(any(IPSGuid.class)));will(returnValue(parent));
            atLeast(1).of(rel1).getSlotId();will(returnValue(slot1));
            atLeast(1).of(rel2).getSlotId();will(returnValue(slot2)); 
            atLeast(1).of(rel3).getSlotId();will(returnValue(slot1));
            atLeast(1).of(rel4).getSlotId();will(returnValue(slot1));
            atLeast(1).of(rel1).getSortRank();will(returnValue(3));
            atLeast(1).of(rel2).getSortRank();will(returnValue(1)); 
            atLeast(1).of(rel3).getSortRank();will(returnValue(2)); 
            atLeast(1).of(rel4).getSortRank();will(returnValue(1)); 
         }}); 

         contents.setCws(cws); 
         contents.setGmgr(gmgr);

         List<PSAaRelationship> r2 = contents.getSlotContents(new PSLegacyGuid(3L), new PSLegacyGuid(1L));

         assertNotNull(r2); 
         assertEquals(3, r2.size()); 
         assertEquals(1, r2.get(0).getSortRank());
         assertEquals(3, r2.get(2).getSortRank());
      } catch (PSErrorException ex)
      {
            fail("Unexpected Exception");          
            log.error("Unexpected Exception " + ex,ex);
      }
   }
}
