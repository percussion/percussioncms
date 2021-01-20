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
