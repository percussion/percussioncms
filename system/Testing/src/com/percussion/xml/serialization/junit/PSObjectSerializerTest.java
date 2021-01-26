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
package com.percussion.xml.serialization.junit;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author RammohanVangapalli
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSObjectSerializerTest
{
   static public class PersonList 
   {
      private final List<Person> mi_people = new ArrayList<>();
      
      public PersonList()
      {
         // 
      }
      
      public void addPerson(Person x)
      {
         mi_people.add(x);
      }
      
      public List<Person> getPersons()
      {
         return mi_people;
      }

      @Override
      public boolean equals(Object obj)
      {
         return EqualsBuilder.reflectionEquals(this, obj);
      }

      @Override
      public int hashCode()
      {
         return mi_people.hashCode();
      }
   }
   
   /**
    * Test object created in the ctor to be used by the tests, later.
    */
   static private Person person = null;

   /**
    * Instance of the serializer to perform the testing of serialization and
    * deserialization.
    */
   static private final PSObjectSerializer serializer = PSObjectSerializer
         .getInstance();

   /**
    * Input data for deserialization in case required. Do not indent this for
    * clarity. It is not declared final so that it can be reinitialized by
    * testSerialization() depending on which test executed first.
    */
   static private String serializedString = 
      "<person>" +
         "<name>" +
            "<first>Rammohan</first>" +
            "<last>Vangapalli</last>" +
         "</name>" +
         "<address>" +
            "<street>10 Germano Way</street>" +
            "<addressline2/>" +
            "<town>Andover</town>" +
            "<state>MA</state>" +
            "<zip>01810</zip>" +
         "</address>" +
         "<books>" +
            "<book>" +
               "<title>Life without God1</title>" +
               "<pubdate>09052010</pubdate>" +
            "</book>" +
            "<book>" +
               "<title>Life without God2</title>" +
               "<pubdate>09052011</pubdate>" +
            "</book>" +
            "<book>" +
               "<title>Life without God3</title>" +
               "<pubdate>09052012</pubdate>" +
            "</book>" +
            "<book>" +
               "<title>Life without God4</title>" +
               "<pubdate>09052013</pubdate>" +
            "</book>" +
         "</books>" +
      "</person>";

   public PSObjectSerializerTest(){}

   /**
    * Over ride this method to initialize the person object to be used by tests.
    * 
    * @throws Exception error
    */
   @Before
   public void setUp() throws Exception
   {
      PSXmlSerializationHelper.addType("person", Person.class);
      PSXmlSerializationHelper.addType("address", Address.class);
      PSXmlSerializationHelper.addType("book", Book.class);
      person = new Person("Rammohan", "Vangapalli");
      person
            .setAddress(new Address("10 Germano Way", "Andover", "MA", "01810"));
      person.addBook(new Book("Life without God1", "09052010"));
      person.addBook(new Book("Life without God2", "09052011"));
      person.addBook(new Book("Life without God3", "09052012"));
      person.addBook(new Book("Life without God4", "09052013"));
   }

   /**
    * Serialization test case. Serializes the object created in the ctor to XML
    * string.
    */
   @Test
   public void test01Serialization()
   {
      try
      {
         serializedString = serializer.toXmlString(person);
         assertTrue(serializedString.length() > 0);
      }
      catch (IOException | SAXException | IntrospectionException e)
      {
         // XXX Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Test case de-serialization. Restores the object from XML string and
    * compares this with th one created directly.
    */
   @Test
   public void test02DeSerialization() throws Exception
   {
      Person personNew = (Person) serializer.fromXmlString(serializedString);
      assertEquals(person, personNew);
   }
   
   /**
    * Test case serializes a collection and restores it, then compares 
    * for equality
    * @throws Exception error
    */
   @Test
   public void test03Collections1() throws Exception
   {
      PSXmlSerializationHelper.addType("p-list",PersonList.class);
      PersonList plist = new PersonList();
      
      Person a = new Person();
      Person b = new Person();
      Person c = new Person();
      
      a.setName(new Name("John", "Doe"));
      b.setName(new Name("Sally", "Fields"));
      c.setName(new Name("Jacob", "Marley"));
      
      plist.addPerson(a);
      plist.addPerson(b);
      plist.addPerson(c);
      
      String ser = serializer.toXmlString(plist);
      
      Object deser = serializer.fromXmlString(ser);
      
      assertEquals(plist, deser);
   }

}
