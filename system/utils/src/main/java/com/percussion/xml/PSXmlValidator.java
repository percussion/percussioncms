/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.xml;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for validating an xml file against a specified Schema
 * and for well-formed ness.
 */
public class PSXmlValidator
{
   /*
    * Private ctor to inhibit instantiation.
    */
   private PSXmlValidator()
   {
      
   }
   
   /**
    * Validates an Xml file against a specified Xml Schema (xsd).
    * @param XmlFile the xml file to be validated, cannot be <code>null</code>
    * or empty.
    * @param SchemaFile the xsd file to validate against, 
    * cannot be <code>null</code> or empty.
    * @param errors list where errors will be stored. 
    * May be <code>null</code>, in which case no errors will be stored.
    * @return <code>true</code> if xml is valid against schema and well formed.
    */
   public static boolean validateXmlAgainstSchema(
      File XmlFile, File SchemaFile, List<Exception> errors)
   {
      if(XmlFile == null || !XmlFile.exists() || !XmlFile.isFile())
         throw new IllegalArgumentException(
            "XmlFile cannot be null and must exist.");
      if(SchemaFile == null || !SchemaFile.exists() || !SchemaFile.isFile())
         throw new IllegalArgumentException(
            "SchemaFile cannot be null and must exist.");
      
      DocumentBuilderFactory factory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
              new PSXmlSecurityOptions(
                      true,
                      true,
                      true,
                      false,
                      true,
                      false
              )
      );
      factory.setValidating(true);
      factory.setNamespaceAware(true);
      factory.setAttribute(
         "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
         XMLConstants.W3C_XML_SCHEMA_NS_URI);
      
      try
      {
         factory.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            SchemaFile.toURL().toString());
      }
      catch (Exception ignore){}
      
      PSSaxErrorHandler errorHandler = new PSSaxErrorHandler();
      errorHandler.throwOnFatalErrors(false);
      try
      {
         DocumentBuilder parser = factory.newDocumentBuilder();
         
         parser.setErrorHandler(errorHandler);


         parser.parse(XmlFile);
      }
      
      catch (SAXException e)
      {
         if(errors != null)
            errors.add(e);
         return false;
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
      boolean hasErrors = 
         errorHandler.numErrors() > 0 || errorHandler.numFatalErrors() > 0;
      if (errors != null)
      {
         Iterator errorIt = errorHandler.errors();
         Iterator fatalIt = errorHandler.fatalErrors();
         while (errorIt.hasNext())
         {
            errors.add((Exception)errorIt.next());
         }
         while (fatalIt.hasNext())
         {
            errors.add((Exception)fatalIt.next());
         }
      }
      return !hasErrors;
   }
   
   
   
   

}
