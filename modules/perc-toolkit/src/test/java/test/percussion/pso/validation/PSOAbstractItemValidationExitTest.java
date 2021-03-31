/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import com.percussion.error.PSException;
import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.xml.PSXmlDocumentBuilder;

public class PSOAbstractItemValidationExitTest
{
   private static Log log = LogFactory.getLog(PSOAbstractItemValidationExitTest.class); 

   TestableItemValidationExit cut;
   
   Mockery context; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      
      cut = new TestableItemValidationExit();
   }
   
   @Test
   public final void testHasErrors()
   {
      Document err = PSXmlDocumentBuilder.createXmlDocument();
      boolean rslt = cut.hasErrors(err);
      assertFalse(rslt);
      PSItemErrorDoc.addError(err, "foo", "bar" , "xyzzy" , new Object[0]);
      rslt = cut.hasErrors(err);
      assertTrue(rslt);
   }
   
   @Test
   public final void testMatchDestinationStateBasics()
   {
      try
      {
         boolean rslt = cut.matchDestinationState("1" , "2", null);
         assertTrue(rslt);
         rslt = cut.matchDestinationState("1" , "2", "");
         assertTrue(rslt);
         rslt = cut.matchDestinationState("1", "2","*"); 
         assertTrue(rslt);
      } catch (PSException ex)
      {  
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
   }
   
   @Test
   public final void testMatchDestinationStateComplex()
   {
      final PSState state = context.mock(PSState.class);
      final IPSOWorkflowInfoFinder finder = context.mock(IPSOWorkflowInfoFinder.class);
      cut.setFinder(finder);
      try
      {
         
         context.checking(new Expectations(){{
            one(finder).findDestinationState("1","2");
            will(returnValue(state));
            atLeast(1).of(state).getName();
            will(returnValue("fi"));
         }});
         boolean rslt = cut.matchDestinationState("1" , "2", "fee,fi,fo,fum");
         assertTrue(rslt);
         context.assertIsSatisfied();
         
      } catch (PSException ex)
      {  
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
   }
   private class TestableItemValidationExit extends PSOAbstractItemValidationExit
   {

      @Override
      public boolean hasErrors(Document errorDoc)
      {
         return super.hasErrors(errorDoc);
      }

      @Override
      public void validateDocs(Document inputDoc, Document errorDoc,
            IPSRequestContext req, Object[] params)
      {
         
      }

      @Override
      public boolean matchDestinationState(String contentid,
            String transitionid, String allowedStates) throws PSException
      {
         return super.matchDestinationState(contentid, transitionid, allowedStates);
      }

      @Override
      public void setFinder(IPSOWorkflowInfoFinder finder)
      {         
         super.setFinder(finder);
      }
      
   }
}
