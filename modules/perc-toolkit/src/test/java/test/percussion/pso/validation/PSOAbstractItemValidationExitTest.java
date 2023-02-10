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
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   private static final Logger log = LogManager.getLogger(PSOAbstractItemValidationExitTest.class);

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
