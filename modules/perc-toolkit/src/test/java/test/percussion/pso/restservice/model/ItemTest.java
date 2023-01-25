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
package test.percussion.pso.restservice.model;

import com.percussion.pso.restservice.model.*;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class ItemTest {
    
    /**
     * Field xml.
     */
    String xml = "<p> blah&#160;isn&apos;t going to show THESE WORDS:</p> <h1>hello</h1> hello";
    /**
     * Field expectedNoXml.
     */
    String expectedNoXml = " blah\u00a0isn't going to show THESE WORDS: hello hello";
    /**
     * Field testItem.
     */
    Item testItem;

    /**
     * Method setUp.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        testItem = new Item();
        List<Field> fields = new ArrayList<Field>();
        /*Field field = new Field();
        field.setName("testField");
        field.setStringValue("testFieldValue");
        */
        
        Field field = new Field("name","Value");
        fields.add(field);
        testItem.setFields(fields);
        XhtmlValue value1 = new XhtmlValue();
        String s1="<div class=\"rxbodyfield\"><p>test</p></div>";
        value1.setStringValue(s1);
        StringValue value2 = new StringValue();
        value2.setStringValue("Test");
        List<Value> values = new ArrayList<Value>();
        values.add((Value)value1);
        values.add((Value)value2);
        field.setValues(values);
        
        Relationships rel = new Relationships();
        testItem.setRelationships(rel);
        
        Slot slot1 = new Slot();
        slot1.setName("slotname");
        slot1.setType("SlotTyoe");
        
        rel.setSlots(Collections.singletonList(slot1));
        SlotItem item1 = new SlotItem();
        item1.setContentId(1);
        item1.setFolder("//Sites/Folder");
        item1.setRevision(1);
        item1.setSite("Site");
        item1.setTemplate("template");
        slot1.setItems(Collections.singletonList(item1));
        
        Translation trans1 = new Translation();
        trans1.setContentId(1);
        trans1.setLocale("locale");
        rel.setTranslations(Collections.singletonList(trans1));
        
    }

    /**
     * Method testCreateXML.
     * @throws IOException
     * @throws SAXException
     * @throws JAXBException
     */
    @Test
    public void testCreateXML() throws IOException, SAXException, JAXBException {
    	outputXML(testItem);
    	System.out.println("String is "+testItem.getFields().get(0).getValues().get(0).getStringValue());
    	testItem.getFields().get(0).setStringValue("Tester");
    	outputXML(testItem);
    	
    }


    /**
     * Method outputXML.
     * @param testItem Item
     * @throws JAXBException
     */
    public void outputXML(Item testItem) throws JAXBException {
    
    	JAXBContext ctx  =  JAXBContext.newInstance(Item.class);
		Marshaller marshaller = ctx.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
		
		marshaller.marshal(testItem,System.out); 	
    }
   

}
