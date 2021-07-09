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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class unit tests the {@link PSSearchProperties} object. It is written
 * to the JUnit framework. 
 *
 * @author paulhoward
 */
public class PSSearchPropertiesTest extends TestCase
{
   /**
    * Groups all tests implicitly. See framework description for more details.
    */
   public static TestSuite suite()
   {
      return new TestSuite(PSSearchConfigTest.class);
   }

   /**
    * The ctor needed for a JUnit test. See framework description for more 
    * details.
    */
   public PSSearchPropertiesTest(String name)
   {
      super(name);
   }

   /**
    * Makes a few objects and verifies that all appropriate bits are considered
    * and the skipped bits don't affect equals or hashcode.
    * 
    * @throws PSUnknownNodeTypeException If the <code>fromXml</code> method
    * fails unexpectedly. 
    */
   public void testEqualsAndHashCode()
      throws PSUnknownNodeTypeException
   {
      PSSearchProperties sp = new PSSearchProperties();
      PSSearchProperties sp2 = new PSSearchProperties();
      assertTrue("default objects not equal:", sp.equals(sp2));
      assertTrue("default object's hashcode not equal:",
            sp.hashCode() == sp2.hashCode());
      
      sp.setDefaultSearchLabel("a");
      assertTrue("changed label shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed label shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setDefaultSearchLabel("a");
      assertTrue("changed label should be equal:", sp.equals(sp2));
      assertTrue("changed label should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
      
      sp.setEnableTransformation(!sp.isEnableTransformation());
      assertTrue("changed enableTrans shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed enableTrans shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setEnableTransformation(sp.isEnableTransformation());
      assertTrue("changed enableTrans should be equal:", sp.equals(sp2));
      assertTrue("changed enableTrans should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
      
      sp.setId(89);
      assertTrue("changed id shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed id shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setId(sp.getId());
      assertTrue("changed id should be equal:", sp.equals(sp2));
      assertTrue("changed id should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
            
      sp.setTokenizeSearchContent(!sp.isTokenizeSearchContent());
      assertTrue("changed searchTok shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed searchTok shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setTokenizeSearchContent(sp.isTokenizeSearchContent());
      assertTrue("changed searchTok should be equal:", sp.equals(sp2));
      assertTrue("changed searchTok should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
            
      sp.setUserCustomizable(!sp.isUserCustomizable());
      assertTrue("changed userCustomizable shouldn't be equal:", 
            !sp.equals(sp2));
      assertTrue("changed userCustomizable shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setUserCustomizable(sp.isUserCustomizable());
      assertTrue("changed userCustomizable should be equal:", sp.equals(sp2));
      assertTrue("changed userCustomizable should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
            
      sp.setUserSearchable(!sp.isUserSearchable());
      assertTrue("changed userSearchable shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed userSearchable shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setUserSearchable(sp.isUserSearchable());
      assertTrue("changed userSearchable should be equal:", sp.equals(sp2));
      assertTrue("changed userSearchable should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());
            
      sp.setVisibleToGlobalQuery(!sp.isVisibleToGlobalQuery());
      assertTrue("changed userVisible shouldn't be equal:", !sp.equals(sp2));
      assertTrue("changed userVisible shouldn't have equal hashcode:",
            sp.hashCode() != sp2.hashCode());
      sp2.setVisibleToGlobalQuery(sp.isVisibleToGlobalQuery());
      assertTrue("changed userVisible should be equal:", sp.equals(sp2));
      assertTrue("changed userVisible should have equal hashcode:",
            sp.hashCode() == sp2.hashCode());

      //change bits that shouldn't affect the equals
      sp.setEnableTransformationLocked(!sp.isEnableTransformationLocked());            
      assertTrue("enableTransformationLocked bit affected equals but shouldn't", 
            sp.equals(sp2));
      assertTrue("enableTransformationLocked bit affected hash but shouldn't",
            sp.hashCode() == sp2.hashCode());
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      sp2.fromXml(sp.toXml(doc), null, null);
      assertTrue("fromXml bit affected equals but shouldn't", 
            sp.equals(sp2));
      assertTrue("fromXml bit affected hash but shouldn't",
            sp.hashCode() == sp2.hashCode());
   }

   /**
    * Verifies an exception is thrown when the enable transformation flag is
    * locked.
    */
   public void testEnableTransformLock()
   {
      PSSearchProperties sp = new PSSearchProperties();
      sp.setEnableTransformationLocked(true);
      try
      {
         sp.setEnableTransformation(!sp.isEnableTransformation());
         fail("Enable transformation lock didn't work.");
      }
      catch (IllegalStateException ise)
      {
         //expected
      }
   }

   /**
    * Tests the toXml and fromXml methods by creating objects, transforming
    * them and testing for equality.
    * 
    * @throws PSUnknownNodeTypeException If the <code>fromXml</code> method
    * fails unexpectedly. 
    */
   public void testXmlConversion()
      throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      //try the default guy first
      PSSearchProperties sp = new PSSearchProperties();
      PSSearchProperties tmp = new PSSearchProperties(sp.toXml(doc));
      assertTrue("Xml transformed obj didn't match original:", sp.equals(tmp));
      
      //flip all bits and try again
      flipBits(sp);
      tmp = new PSSearchProperties(sp.toXml(doc));
      assertTrue("non-default Xml transformed obj didn't match original:", 
         sp.equals(tmp));      
   }

   /**
    * Flips the value of every flag in sp, sets the id to 99 and sets the 
    * label to "test label".
    * 
    * @param sp Assumed not <code>null</code>.
    */
   private void flipBits(PSSearchProperties sp)
   {
      sp.setId(99);
      sp.setDefaultSearchLabel("test label");
      sp.setEnableTransformation(!sp.isEnableTransformation());
      sp.setEnableTransformationLocked(!sp.isEnableTransformationLocked());
      sp.setTokenizeSearchContent(!sp.isTokenizeSearchContent());
      sp.setUserCustomizable(!sp.isUserCustomizable());
      sp.setUserSearchable(!sp.isUserSearchable());
      sp.setVisibleToGlobalQuery(!sp.isVisibleToGlobalQuery());
   }

}
