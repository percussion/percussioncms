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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the {@link PSUISet} class
 */
public class PSUISetTest extends TestCase
{
   public PSUISetTest(String s)
   {
      super( s );
   }


   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSUISetTest( "testDeepCopy" ) );
      suite.addTest( new PSUISetTest( "testMergeAndDiff" ) );
      return suite;
   }


   /**
    * Tests that a cloned instance is equal to cloning instance.  Assumes that
    * <code>equal()</code> is implemented correctly.
    * 
    * @throws Exception if the test fails.
    */ 
   public void testDeepCopy() throws Exception
   {
      // build a object
      PSUISet uiSet = new PSUISet();
      uiSet.setChoices(new PSChoices(PSChoices.TYPE_GLOBAL));
      uiSet.setControl(new PSControlRef("myControl"));
      // uiSet.setCustomActionGroup
      uiSet.setDefaultSet("default");
      uiSet.setErrorLabel(new PSDisplayText("errorLabel"));
      uiSet.setLabel(new PSDisplayText("Author Age:"));
      uiSet.setName("set_1");
      uiSet.setReadOnlyRules(new PSCollection(PSRule.class));

      // copy it
      PSUISet uiSetCopy = (PSUISet) uiSet.clone();

      // are they equal?
      assertTrue(uiSet.equals(uiSetCopy));
   }
   
   /**
    * Test that merging a partial uiset with another and then diffing them
    * produces the same result.
    * 
    * @throws Exception if the test fails.
    */
   public void testMergeAndDiff() throws Exception
   {
      PSUISet uiSet = new PSUISet();
      uiSet.setChoices(new PSChoices(PSChoices.TYPE_GLOBAL));
      uiSet.setControl(new PSControlRef("myControl"));
      uiSet.setCustomActionGroup(new PSCustomActionGroup(new PSLocation(
         PSLocation.PAGE_ROW_EDIT, PSLocation.TYPE_ROW), new PSActionLinkList(
            new PSActionLink(new PSDisplayText("linkText")))));
      uiSet.setDefaultSet("default");
      uiSet.setErrorLabel(new PSDisplayText("errorLabel"));
      uiSet.setLabel(new PSDisplayText("Author Age:"));
      uiSet.setName("set_1");
      uiSet.setReadOnlyRules(new PSCollection(PSRule.class));
      
      PSUISet partial = new PSUISet();
      partial.setControl(new PSControlRef("newControl"));
      
      PSUISet merged = partial.merge(uiSet);
      assertEquals(merged.getControl(), partial.getControl());
      PSUISet testMerged = (PSUISet)merged.clone();
      testMerged.setControl(uiSet.getControl());
      assertEquals(uiSet, testMerged);
      
      PSUISet diff = merged.demerge(uiSet);
      assertEquals(partial, diff);
   }


   /**
    * @return a new instance of a choice set
    */ 
   private static PSChoices newPSChoices()
   {
      PSEntry entry1 = new PSEntry( "1111", new PSDisplayText( "one" ) );
      PSEntry entry2 = new PSEntry( "2222", new PSDisplayText( "two" ) );
      PSEntry entry3 = new PSEntry( "3333", new PSDisplayText( "three" ) );
      PSCollection choiceCol = new PSCollection( entry1.getClass() );
      choiceCol.add( entry1 );
      choiceCol.add( entry2 );
      choiceCol.add( entry3 );
      return new PSChoices( choiceCol );
   }


   /**
    * @return a new instance of a collection of <code>PSRule</code> objects
    */ 
   private static PSCollection newPSRules()
   {
      PSRule rule = new PSRule( new PSExtensionCallSet() );
      PSCollection ruleCol = new PSCollection( rule.getClass() );
      ruleCol.add( rule );
      ruleCol.add( rule );
      ruleCol.add( rule );
      return ruleCol;
   }


   /**
    * @return a new instance of a control reference with parameters
    */ 
   private static PSControlRef newPSControlRef()
   {
      PSParam param2 = new PSParam( "ALIGN", new PSTextLiteral( "center" ) );
      PSCollection parameters2 = new PSCollection( param2.getClass() );
      parameters2.add( param2 );
      return new PSControlRef( "sys_TextBox", parameters2 );
   }
}
