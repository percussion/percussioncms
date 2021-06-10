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

package com.percussion.tools.simple;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is used to create template from an Content Editor application
 * XML file and write it to a new file(target).  A DTD must be specified to
 * validate the target and add it to the target. This class has a main method
 * and a public static method so that it can be used from command line or any
 * other java class.
 */
public class PSCETemplateGenerator
{

   private static final Logger log = LogManager.getLogger(PSCETemplateGenerator.class);

   /**
    * Extracts the &lt;PSXContentEditor> element from the source file, modifies
    * the element to use that as template and writes the modified element to the
    * target file. If the source has multiple &lt;PSXContentEditor> elements,
    * then it creates template for the first element found. If the source
    * content editor element contains multiproperty simple child fieldsets, it
    * will not create a template for the source file and throws
    * <code>PSCreateTemplateException</code>.
    *
    * <p>
    * The modifications include the following.
    * <ol>
    * <li>Adds the document type to the target with specified dtd or document
    * type path</li>
    * <li>Removes all existing table references and add a new dummy table
    * reference 'psx_dummy'.</li>
    * <li>Removes the &lt;PSXPageDataTank> element.</li>
    * </ol>
    *
    * @param source The source Xml file, may not be <code>null</code>.  Must
    * point to an existing Xml file and should have &lt;PSXContentEditor>
    * element. File is assumed to be in UTF-8.
    * @param target The file to write the extracted Xml to.  May not be <code>
    * null</code>.  File pointed to may or may not exist.  If it does not, then
    * it is created, including any necessary directories.  If it exists, it will
    * be overwritten.
    * @param dtd dtd to add it to the target and validate or to validate only.
    * May not be <code>null</code>.
    * @param docTypePath The document type path to add to the target. This
    * should be supplied if the dtd to add to the target is different from
    * the dtd supplied to validate. May be <code>null</code>
    *
    * @throws PSCreateTemplateException with detailed error message if it fails
    * to create the template.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs
    * @throws FileNotFoundException if any file cannot be located.
    * @throws SAXException if the source file cannot be parsed.
    */
   public void createTemplate(File source, File target,
      URL dtd, String docTypePath)
      throws IOException, FileNotFoundException, SAXException,
         PSCreateTemplateException
   {
      // validate params
      if (source == null || !source.exists())
         throw new IllegalArgumentException("source file is invalid");

      if(dtd == null)
         throw new IllegalArgumentException(
            "dtd must be specified to validate content editor template");

      String result = null;

         // load source doc
      try (FileInputStream in = new FileInputStream(source)){
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         // Check whether content editor element exists or not
         PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
         tree.setCurrent(doc.getDocumentElement());

         Element el = tree.getNextElement(CE_ROOT_ELEMENT_NAME);
         if (el == null)
            throw new IllegalArgumentException(
                    "Content editor element not found in source to create template");

         if (hasMultiPropertySimpleChild(doc)) {
            result = "Failed to create template for '" + source.getPath() + "'\n";

            result += "Reason: Templates do not support MultiProperty Simple " +
                    " Child field sets.";

            throw new PSCreateTemplateException(result);
         }

         //Remove Pagetank and table references as they are not valid for
         //templates
         List excludeList = new ArrayList();
         excludeList.add(PAGE_DATATANK_ELEMENT);
         excludeList.add(TABLE_REFERENCE_ELEMENT);

         //Add dummy table reference as dtd requires atleast one table reference
         //element in tableset.
         Map addList = new HashMap();
         Document dummyDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element tableRef = dummyDoc.createElement(TABLE_REFERENCE_ELEMENT);
         tableRef.setAttribute(TABLE_REFERENCE_NAME_ATTRIBUTE,
                 DUMMY_TABLE_REFERENCE_NAME);
         addList.put(TABLE_SET_ELEMENT, tableRef);

         /* Tell the extractor to remove the elements in exclude list, add
          * elements in add list, validate the target with dtd.
          *
          * If the document type is specified, it uses the given document type
          * to write it to the target, otherwise it uses the specified dtd url.
          */
         if (docTypePath == null || docTypePath.trim().length() == 0) {
            //Writes to the target with document type as given dtd url
            result = PSXmlExtractor.extract(source, target,
                    CE_ROOT_ELEMENT_NAME, dtd, excludeList, addList, true);
         } else {
            //Writes to the target and validates the target with given dtd
            result = PSXmlExtractor.extract(source, target,
                    CE_ROOT_ELEMENT_NAME, dtd, excludeList, addList, false);

            //Adds the given document type and rewrites to the target.
            if (result == null) {
               try(FileInputStream in2 = new FileInputStream(target)) {
                  doc = PSXmlDocumentBuilder.createXmlDocument(in2, false);
                  try(FileOutputStream out2 = new FileOutputStream(target)) {
                     PSXmlDocumentBuilder.write(doc, out2);
                  }
               }
            }
         }
         if (result != null)
            throw new PSCreateTemplateException(result);
      }

   }

   /**
    * Checks whether the supplied content editor xml document has
    * 'multiPropertySimpleChild' field sets.
    *
    * @param doc the document to check, assumed not to be <code>null</code>
    *
    * @return <code>true</code> if field sets of type 'multiPropertySimpleChild'
    * exists, otherwise <code>false</code>
    */
   private boolean hasMultiPropertySimpleChild(Document doc)
   {
      boolean exists = false;

      NodeList elList = doc.getElementsByTagName(FIELD_SET_ELEMENT);
      if (elList != null)
      {
         for (int i=0; i < elList.getLength(); i++)
         {
            Element el = (Element)elList.item(i);
            if( el.getAttribute(FIELD_SET_TYPE_ATTRBUTE).equalsIgnoreCase(
               FIELD_SET_MULTI) )
            {
               exists = true;
               break;
            }
         }
      }

      return exists;
   }


   /**
    * The exception class to represent the exception to be thrown when
    * an error happens while creating the template.
    */
   public class PSCreateTemplateException extends Exception
   {
      /**
       * Constructs this exception object from the given message.
       *
       * @param message the message to set, may be <code>null</code>
       */
      public PSCreateTemplateException(String message)
      {
         super(message);
      }
   }


   /**
    * This class may be used from the command line.  This is essentially a
    * wrapper for {@link #createTemplate()}. Arguments expected are:
    *
    * <ol>
    * <li>source: The source Xml file.  Must point to an existing Xml file and
    * should have &lt;PSXContentEditor> element. File is assumed to be in UTF-8.
    * </li>
    *
    * <li> target: The file to write the extracted Xml to.  May not be <code>
    * null</code>.  File pointed to may or may not exist.  If it does not, then
    * it is created, including any necessary directories.  If it exists, it will
    * be overwritten.</li>
    *
    * <li>dtd: dtd to use to validate and add to the target, specifed as a file
    * or URL.
    * </li>
    *
    * <li>documentType: optional. If it is provided, this is used to set
    * document type of target, otherwise it uses the dtd given for validating.
    * </li>
    * </ol>
    *
    * Any errors are written to System.out
    */
   public static void main(String[] args)
   {
      File source = null;
      try
      {
         // get the args
         if (args.length < 3)
         {
            System.out.println("Incorrect number of arguments.");
            printUsage();
            return;
         }

         source = new File(args[0]);
         File target = new File(args[1]);
         URL dtd = null;
         try
         {
            String protocol = null;
            if(args[2].length() >= 5)
               protocol = args[2].substring(0,5);

            if( protocol != null && (protocol.equalsIgnoreCase("file:") ||
                protocol.equalsIgnoreCase("http:")) )
            {
               dtd = new URL(args[2]);
            }
            else
            {
               File dtdFile = new File(args[2]);
               dtd = dtdFile.toURL();
            }
         }
         catch (MalformedURLException e)
         {
            System.out.println("Invalid dtd specified");
            return;
         }

         String docTypePath = null;
         if(args.length >= 4)
            docTypePath = args[3];

         PSCETemplateGenerator generator = new PSCETemplateGenerator();
         generator.createTemplate(source, target, dtd, docTypePath);

         System.out.println("Successfully created template to the " + target
            + " from " + source + " file.");
      }
      catch(Throwable t)
      {
         System.out.println("Creation of template from " + source +
               " is failed:");
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
   }

   /**
    * Prints cmd line usage to the screen.
    */
   private static void printUsage()
   {
      System.out.println("Usage:");
      System.out.print("java com.percussion.tools.simple.PSCETemplateGenerator ");
      System.out.println(
         "<source file> <target file> <dtd file or url> [<document type path>]");
      System.out.println("Specify the optional document type path only if the ");
      System.out.println("document type to be added to the target file is ");
      System.out.println("different from the dtd which is used for validating.");
   }

   //Constants for element names are duplicated here to avoid the dependency on
   //ObjectStore code.

   /**
    * The tag name of root content editor element.
    */
   private static final String CE_ROOT_ELEMENT_NAME = "PSXContentEditor";

   /**
    * The tag name of page data tank element.
    */
   private static final String PAGE_DATATANK_ELEMENT = "PSXPageDataTank";

   /**
    * The tag name of table set element.
    */
   private static final String TABLE_SET_ELEMENT = "PSXTableSet";

   /**
    * The tag name of table reference element.
    */
   private static final String TABLE_REFERENCE_ELEMENT = "PSXTableRef";

   /**
    * The attribute name of table reference element attribute 'NAME'.
    */
   private static final String TABLE_REFERENCE_NAME_ATTRIBUTE = "name";

   /**
    * The tag name of field set element.
    */
   private static final String FIELD_SET_ELEMENT = "PSXFieldSet";

   /**
    * The attribute name of field set element attribute 'TYPE'
    */
   private static final String FIELD_SET_TYPE_ATTRBUTE = "type";

   /**
    * The type string for multi property simple child fieldset.
    */
   private static final String FIELD_SET_MULTI = "multiPropertySimpleChild";

   /**
    * The dummy table reference name.
    */
   private static final String DUMMY_TABLE_REFERENCE_NAME = "psx_dummy";
}
