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

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
      public void fromXml(Element sourceNode, IPSDocument parentDoc, List parentComponents) throws PSUnknownNodeTypeException
      {
         throw new AssertionError();
      }
      
   }
}
