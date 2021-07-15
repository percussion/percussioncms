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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

public class PSFileTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode() throws MalformedURLException
   {
      assertEqualsWithHash(new PSFileImpl(), new PSFileImpl());
      assertEqualsWithHash(new PSFileImpl(createUrl()), new PSFileImpl(createUrl()));
      
   }

   private URL createUrl() throws MalformedURLException
   {
      return new URL("http://www.mozilla.org/products/");
   }

   private static class PSFileImpl extends PSFile
   {
      protected PSFileImpl()
      {
         super();
      }
      
      protected PSFileImpl(URL fileName)
      {
         super(fileName);
      }


      @Override
      public Element toXml(Document doc)
      {
         throw new AssertionError();
      }

      @Override
      public void fromXml(Element sourceNode, IPSDocument parentDoc, ArrayList parentComponents) throws PSUnknownNodeTypeException
      {
         throw new AssertionError();
      }
      
   }
}
