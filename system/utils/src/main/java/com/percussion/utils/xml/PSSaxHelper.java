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
package com.percussion.utils.xml;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Parsing utilities for SAX
 * 
 * @author dougrand
 */
public class PSSaxHelper
{

   /**
    * Parser factory used in the static helper methods here. The
    */
   static final SAXParserFactory ms_factory = SAXParserFactory.newInstance();

   /**
    * This method instantiates the passed content handler with the arguments
    * given. The passed handler must implement {@link ContentHandler} and
    * inherit from {@link DefaultHandler} or {@link DefaultHandler2}. If it
    * implements {@link LexicalHandler} then it will be set as the lexical
    * handler on the parser.
    * <p>
    * The first argument to the handler's constructor must be an
    * {@link XMLStreamWriter}, which is used to output the transformed source.
    * The constructor is found by finding the first constructor for the class
    * that matches the length and has a first argument that takes a 
    * {@link ContentHandler}.
    * 
    * @param source the source document in the form of a string, never
    *           <code>null</code> or empty
    * @param handler the content handler (and possibly lexical handler) class,
    *           which takes an {@link XMLStreamWriter} as the first argument in
    *           the constructor, never <code>null</code>.
    * @param args further arguments for the constructor, may be
    *           <code>null</code>.
    * @return a string result, never <code>null</code>.
    * @throws SAXException
    * @throws IOException
    * @throws XMLStreamException
    */
   @SuppressWarnings("unchecked")
   public static String parseWithXMLWriter(String source,
         Class<? extends DefaultHandler> handler, Object... args)
         throws SAXException, IOException, XMLStreamException
   {
      try
      {
         if (StringUtils.isBlank(source))
         {
            throw new IllegalArgumentException(
                  "source may not be null or empty");
         }
         if (handler == null)
         {
            throw new IllegalArgumentException("handler may not be null");
         }
         StringWriter writer = new StringWriter();
         XMLOutputFactory ofact = XMLOutputFactory.newInstance();
         XMLStreamWriter xmlwriter = ofact.createXMLStreamWriter(writer);
         int len = args != null ? args.length + 1 : 1;
         Object cargs[] = new Object[len];
         cargs[0] = xmlwriter;
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               cargs[i + 1] = args[i];
            }
         }
         Constructor c = null;
         for (Constructor constructor : handler.getConstructors())
         {
            if (constructor.getParameterTypes().length == len
                  && constructor.getParameterTypes()[0]
                        .isAssignableFrom(XMLStreamWriter.class))
            {
               c = constructor;
               break;
            }
         }
         if (c == null)
         {
            throw new RuntimeException("Found no matching constructor");
         }
         Object contenthandler = c.newInstance(cargs);
         SAXParser parser = newSAXParser(contenthandler);
         InputSource is = new InputSource(new StringReader(source));
         parser.parse(is, (DefaultHandler) contenthandler);
         xmlwriter.close();
         writer.close();
         String result = writer.toString();
         result = StringUtils.replace(result, PSSaxCopier.RX_FILLER, "");
         return result;
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (FactoryConfigurationError e)
      {
         throw new RuntimeException(e);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());
      }
      catch (ParserConfigurationException e)
      {
         throw new SAXException(e);
      }
   }

   /**
    * Creates a new instance of a SAXParser with the specified
    * handler. 
    * 
    * @param handler the handler of the parser. It should be an 
    * instance of {@link LexicalHandler} if the handler wants to
    * handle comments and/or CDADA. It may be <code>null</code> 
    * if the handler is unknown.
    * 
    * @return the created parser, never <code>null</code>.
    * 
    * @throws ParserConfigurationException  if a parser cannot be created which
    * satisfies the requested configuration
    * @throws SAXException if other SAX error occurs.
    */
   public static SAXParser newSAXParser(Object handler)
         throws ParserConfigurationException, SAXException
   {
      SAXParser parser = ms_factory.newSAXParser();
      if (handler instanceof LexicalHandler)
      {
         // set the optional extension handler for SAX2 to provide lexical
         // information about an XML document, such as comments and CDATA
         // section boundaries
         parser.setProperty("http://xml.org/sax/properties/lexical-handler",
               handler);
      }
      return parser;
   }
 
   /**
    * Determines if the specified element can be a self closed HTML
    * element or not. Some of the self closed HTML element may cause
    * problems in browser, Ephox or eKtron.
    * 
    * @param name the name of the element, may be blank.
    * 
    * @return <code>true</code> if the element cannot be self closed
    * HTML element; otherwise it can be a self closed HTML element. 
    */
   public static boolean canBeSelfClosedElement(String name)
   {
      return !ELEMENTS_NO_SELF_CLOSE_LIST.contains(name.toLowerCase());
   }
   
   /**
    * Array of empty elements that cause problems in browser, Ephox or eKtron when
    * self (or smart) closed by tidy or document builder. If you find a new
    * one add the tag name to this array.
    */
   public static final ArrayList<String> ELEMENTS_NO_SELF_CLOSE_LIST  = new ArrayList<>(Arrays.asList(
      "a","div","textarea","embed","object","script","h1","h2","h3","h4","h5","h6","iframe"));

   ;
}