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

package com.percussion.tools.simple;

import com.percussion.utils.xml.PSSaxParseException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class is used to extract an element from an XML file and write it to
 * a new file.  A DTD may optionally be specified, and if so it is used to
 * validate the new Xml after it has been extracted.  This class has a main
 * method so that it may be used both from java or from the command line.
 */
public class PSXmlExtractor
{

   private static final Logger log = LogManager.getLogger(PSXmlExtractor.class);

   /**
    * Uses the specified Element to extract xml from the source file and write
    * it to the target file.  If a dtd is supplied, that is used to validate the
    * target.
    *
    * @param source The source Xml file, may not be <code>null</code>.  Must
    * point to an existing Xml file.  File is assumed to be in UTF-8.
    *
    * @param target The file to write the extracted Xml to.  May not be <code>
    * null</code>.  File pointed to may or may not exist.  If it does not, then
    * it is created, including any necessary directories.  If it exists, it will
    * be overwritten.
    *
    * @param element The element to extract.  Must exist in the source document.
    * May not be <code>null</code> or empty.  Will extract the first instance of
    * this element that is found.
    *
    * @param dtd An optional dtd to use to validate or add it to the target.
    * May be <code>null</code>. This must be provided if <code>dtdPath</code>
    * is not <code>null</code>
    *
    * @param excludeList a list of element names as Strings to exclude from the
    * extracted element.  May be <code>null</code>.  All elements matching this
    * name will be excluded.
    *
    * @param addList  a map of elements to add with parent element tag name as
    * key and the <code>Element</code> object as value. All elements will be
    * added after removing the elements from exclude list if provided. Adds the
    * element to all elements matching the parent element name. May be
    * <code>null</code>
    *
    * @param dtdPath The path of the dtd to use to add a <code>DOCTYPE</code>
    * element to the output document.  May be <code>null</code>, in which case
    * the element is not added.  May only be specified if the <code>dtd</code>
    * parameter is not <code>null</code>.
    *
    * @return <code>null</code> if dtd is supplied and extracted xml is
    * successfully validated, or if no dtd is supplid. Returns an error message
    * if a dtd is supplied and validation fails.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs
    * @throws FileNotFoundException if any file cannot be located.
    * @throws SAXException if the source file cannot be parsed.
    */
   public static String extract(File source, File target, String element,
      URL dtd, List excludeList, Map addList, String dtdPath)
         throws IOException, FileNotFoundException, SAXException
   {
      // validate params
      if (source == null || !source.exists())
         throw new IllegalArgumentException("source file is invalid");

      if (target == null)
         throw new IllegalArgumentException("target may not be null");

      if (element == null || element.trim().length() == 0)
         throw new IllegalArgumentException(
            "element may not be null or empty.");

      if(dtdPath != null && dtd == null)
         throw new IllegalArgumentException(
            "The dtd must be specified to add the DOCTYPE to the target");

      String result = null;
      try(FileInputStream in = new FileInputStream(source)) {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         // open target
         if (target.isDirectory())
            throw new IllegalArgumentException("target must be a file");
         File targetDir = target.getAbsoluteFile().getParentFile();
         targetDir.mkdirs();
         if (target.exists())
            target.delete();
         try (FileOutputStream out = new FileOutputStream(target)) {

            // Extract element and write it out
            PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
            tree.setCurrent(doc.getDocumentElement());
            String rootElementName = tree.getCurrentNodeName();

            Element el = tree.getNextElement(element);
            if (el == null)
               throw new IllegalArgumentException(
                       "element not found in source doc");

            Document newDoc =
                    PSXmlDocumentBuilder.createXmlDocument(rootElementName, dtd, null);
            PSXmlDocumentBuilder.replaceRoot(newDoc, el);
            if (excludeList != null) {
               Iterator i = excludeList.iterator();
               while (i.hasNext()) {
                  String exclude = (String) i.next();
                  removeElement(newDoc, exclude);
               }
            }

            if (addList != null) {
               Iterator i = addList.entrySet().iterator();
               while (i.hasNext()) {
                  Map.Entry entry = (Map.Entry) i.next();
                  String parentElement = (String) entry.getKey();
                  Element child = (Element) entry.getValue();
                  addElement(newDoc, parentElement, child);
               }
            }

            // validate with dtd if supplied
            if (dtd != null) {
               // Validating the document will write it out
               result = validate(newDoc, dtd);
            }

            PSXmlTreeWalker walker = new PSXmlTreeWalker(newDoc);
            walker.write(out);
            return result;
         }
      }
   }

   /**
    * Uses the specified Element to extract xml from the source file and write
    * it to the target file.  If a dtd is supplied, that is used to validate the
    * target.
    *
    * @param source The source Xml file, may not be <code>null</code>.  Must
    * point to an existing Xml file.  File is assumed to be in UTF-8.
    *
    * @param target The file to write the extracted Xml to.  May not be <code>
    * null</code>.  File pointed to may or may not exist.  If it does not, then
    * it is created, including any necessary directories.  If it exists, it will
    * be overwritten.
    *
    * @param element The element to extract.  Must exist in the source document.
    * May not be <code>null</code> or empty.  Will extract the first instance of
    * this element that is found.
    *
    * @param dtd An optional dtd to use to validate or add it to the target.
    * May be <code>null</code>. This must be provided if <code>addDtd</code>
    * is specified as <code>true</code>
    *
    * @param excludeList a list of element names as Strings to exclude from the
    * extracted element.  May be <code>null</code>.  All elements matching this
    * name will be excluded.
    *
    * @param addList  a map of elements to add with parent element tag name as
    * key and the <code>Element</code> object as value. All elements will be
    * added after removing the elements from exclude list if provided. Adds the
    * element to all elements matching the parent element name. May be
    * <code>null</code>
    *
    * @param addDtd if <code>true</code> the dtd must be specified and it
    * validates the target with the dtd and adds the dtd to the target,
    * otherwise it validates the target if the dtd is specified.
    *
    * @return <code>null</code> if dtd is supplied and extracted xml is
    * successfully validated, or if no dtd is supplid. Returns an error message
    * if a dtd is supplied and validation fails.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs
    * @throws FileNotFoundException if any file cannot be located.
    * @throws SAXException if the source file cannot be parsed.
    */
   public static String extract(File source, File target, String element,
      URL dtd, List excludeList, Map addList, boolean addDtd)
         throws IOException, FileNotFoundException, SAXException
   {
      // validate params
      if (source == null || !source.exists())
         throw new IllegalArgumentException("source file is invalid");

      if (target == null)
         throw new IllegalArgumentException("target may not be null");

      if (element == null || element.trim().length() == 0)
         throw new IllegalArgumentException(
            "element may not be null or empty.");

      if(addDtd && dtd == null)
         throw new IllegalArgumentException(
            "The dtd must be specified to add the dtd to the target");

      String dtdPath = addDtd ? dtd.toExternalForm() : null;
      return extract(source, target, element, dtd, excludeList, addList,
         dtdPath);
   }

   /**
    * Convenience Version for
    * {@link #extract(File, File, String, URL, List, Map)}. Assumes
    * <code>null</code> for <code>addList</code> parameter.
    */
   public static String extract(File source, File target, String element,
      URL dtd, List excludeList)
         throws IOException, FileNotFoundException, SAXException
   {
      return extract(source, target, element, dtd, excludeList, null);
   }

   /**
    * Convenience Version for
    * {@link #extract(File, File, String, URL, List, Map, boolean)}. Assumes
    * <code>false</code> for <code>addDtd</code> parameter.
    */
   public static String extract(File source, File target, String element,
      URL dtd, List excludeList, Map addList)
         throws IOException, FileNotFoundException, SAXException
   {
      return extract(source, target, element, dtd, excludeList, addList, false);
   }

   /**
    * Validates the document using the supplied dtd.
    *
    * @param doc The doc to validate, assumed not <code>null</code>.
    * @param dtd The dtd to use, assumed not <code>null</code>.
    *
    * @return <code>null</code> if doc validates, an error message if not.
    *
    * @throws IOException if any io error occurs
    */
   private static String validate(Document doc, URL dtd)
      throws IOException
   {
      /*
       * add doctype for the dtd - need to read and write the doc line by line,
       * adding the doctype as we go, since in the dom, DOCTYPE is readonly
       */
      String result = null;

      try
      {
         // write doc to buffer
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         PSXmlDocumentBuilder.write(doc, bout);
         // now we have the doc with the dtd specified, read it in and validate
         ByteArrayInputStream docIn = null;
         try
         {
            docIn = new ByteArrayInputStream(
               bout.toByteArray());
            PSXmlDocumentBuilder.createXmlDocument(docIn, true);
         }
         catch (SAXException e)
         {
            if (e instanceof PSSaxParseException)
            {
               result = "Document has failed to validate: \n";
               Iterator errors = ((PSSaxParseException)e).getExceptions();
               while (errors.hasNext())
               {
                  SAXParseException spe = (SAXParseException)errors.next();
                  result += "Error: " + spe.getLocalizedMessage() + ", Line: " +
                     spe.getLineNumber() + ", Column: " + spe.getColumnNumber()
                     + "\n";
               }
            }
            else
            {
               result = e.toString();
            }
         } finally {
            if (bout!=null) 
               try { bout.close();} catch (Exception e) {/*ignore*/ }
            if (docIn!=null) 
               try { docIn.close();} catch (Exception e) {/*ignore*/ }
         }

      }
      catch (UnsupportedEncodingException e)
      {
         // not likely!
         result = e.toString();
      }

      return result;
   }

   /**
    * Removes all occurences of the specified element from the document.
    *
    * @param doc The document from which to remove the element.  Assumed not
    * <code>null</code>.
    * @param elementName The name of the element to remove.  Assumed not <code>
    * null</code>.
    */
   private static void removeElement(Document doc, String elementName)
   {
      NodeList elList = doc.getElementsByTagName(elementName);
      if (elList == null)
         return;

      for (int i=0; i < elList.getLength(); i++)
      {
         Node node = elList.item(i);
         Node parent = node.getParentNode();
         parent.removeChild(node);
      }
   }

   /**
    * Adds the element to all occurrences of specified element in the document.
    *
    * @param doc The document to which add the element.  Assumed not to be
    * <code>null</code>.
    * @param elementName The name of the parent element to which the element to
    * add.  Assumed not to be <code>null</code> or empty.
    * @param element the element to add, Assumed not to be <code>null</code>
    */
   private static void addElement(Document doc, String elementName,
      Element element)
   {
      NodeList elList = doc.getElementsByTagName(elementName);
      if (elList == null)
         return;

      for (int i=0; i < elList.getLength(); i++)
      {
         Node node = elList.item(i);
         Node elementCopy = doc.importNode(element, true);
         node.appendChild(elementCopy);
      }
   }

   /**
    * This class may be used from the command line.  This is essentially a
    * wrapper for {@link #extract(File, File, String, URL, List)}.  Arguments
    * expected are:
    *
    * <ol>
    * <li>source: The source Xml file.  Must point to an existing Xml file.
    * File is assumed to be in UTF-8.</li>
    *
    * <li> target: The file to write the extracted Xml to.  May not be <code>
    * null</code>.  File pointed to may or may not exist.  If it does not, then
    * it is created, including any necessary directories.  If it exists, it will
    * be overwritten.</li>
    *
    * <li> element: The element to extract.  Must exist in the source document.
    * Will extract the first instance of this element that is found.
    *
    * <li>dtd: Optional. dtd to use to validate the target, specifed as a file.
    * If specifying any excludes, use "null" as a placeholder</li>
    *
    * <li>excludes: Optional. one or more elements to exclude from the extracted
    * element, separated by spaces as separate arguments.
    * </li>
    * </ol>
    *
    * Any errors are written to System.out
    */
   public static void main(String[] args)
   {
      File source = null;
      String element = null;
      try
      {
         // get the args
         if (!(args.length >= 3))
         {
            System.out.println("Incorrect number of arguments.");
            printUsage();
            return;
         }

         source = new File(args[0]);
         File target = new File(args[1]);
         element = args[2];
         URL dtd = null;
         if (args.length >= 4 && !args[3].trim().equalsIgnoreCase("null"))
         {
            try
            {
               File dtdFile = new File(args[3]);
               dtd = dtdFile.toURL();
            }
            catch (MalformedURLException e)
            {
               System.out.println("Invalid dtd specified");
               return;
            }
         }

         List excludeList = null;
         if (args.length >= 5)
         {
            excludeList = new ArrayList();
            for (int i = 4; i < args.length; i++)
            {
               excludeList.add(args[i]);
            }
         }

         String result = PSXmlExtractor.extract(source, target, element, dtd,
            excludeList);
         if (result != null)
         {
            System.out.println("Extraction of " + element + " from " + source +
               " failed:");
            System.out.println(result);
         }
         else
         {
            System.out.println("Extraction of " + element + " from " + source +
               " succeeded.");
         }
      }
      catch(Throwable t)
      {
         System.out.println("Extraction of " + element + " from " + source +
            " failed:");
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
      System.out.print("java com.percussion.tools.simple.PSXmlExtractor ");
      System.out.println(
         "<source file> <target file> <element> [<dtd file> <exludes>]");
   }
}
