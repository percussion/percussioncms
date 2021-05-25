/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.utils;

import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.model.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

/**
 */
public class ItemServiceHelper {

	private static final Logger log = LogManager.getLogger(ItemServiceHelper.class);

	/**
	 * Method getItemXml.
	 * @param item Item
	 * @return String
	 */
	public static String getItemXml(Item item) {
		StringWriter sw = new StringWriter();
		try {

			JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.fragment", Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( item, sw );
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}
		sw.flush();
		return sw.toString();
	}


	/**
	 * Method getItemFromXml.
	 * @param is InputStream
	 * @return Item
	 * @throws JAXBException
	 */
	public static Item getItemFromXml(InputStream is) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
		Unmarshaller um = jc.createUnmarshaller();
		Item item =
			(Item)um.unmarshal( 
					is);
		return item;
	}
	/**
	 * Method getItemsFromXml.
	 * @param is InputStream
	 * @return Items
	 * @throws JAXBException
	 */
	public static Items getItemsFromXml(InputStream is) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance( new Class[] {Items.class} );
		Unmarshaller um = jc.createUnmarshaller();
		Items items =
			(Items)um.unmarshal( 
					is);
		return items;
	}

	/**
	 * Method getItemFromXml.
	 * @param string String
	 * @return Item
	 * @throws JAXBException
	 */
	public static Item getItemFromXml(String string) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream (string.getBytes());
		return getItemFromXml(input);
	}
	
	/**
	 * Method getItemsFromXml.
	 * @param string String
	 * @return Items
	 * @throws JAXBException
	 */
	public static Items getItemsFromXml(String string) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream (string.getBytes());
		return getItemsFromXml(input);
	}


	/**
	 * Method getItemDOM.
	 * @param item Item
	 * @return Document
	 */
	public static Document getItemDOM(Item item) {
		DocumentResult dr = new DocumentResult();
		try {

			JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.fragment", Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( item, dr );
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}

		return dr.getDocument();
	}
}
