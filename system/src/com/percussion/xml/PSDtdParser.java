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

package com.percussion.xml;

//java
import com.percussion.design.catalog.PSCatalogException;
import com.percussion.server.IPSServerErrors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dtd.DTDGrammarBucket;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class is used for parsing DTD. It uses Xerces
 * <code>XMLGrammarPreparser</code> class to do the actual DTD parsing.
 * For usage, see the <code>main</code> method of <code>PSDtd</code> class.
 */
public class PSDtdParser
   extends XMLDTDLoader
   implements ErrorHandler,
               XMLErrorHandler,
               XMLDTDHandler
{

   public PSDtdParser()
   {
      super();
   }

    //
    // ErrorHandler methods
    //

   /**
    * @see org.xml.sax.ErrorHandler
    */
   public void warning(SAXParseException exception) throws SAXException
   {
      throw exception;
   }

   /**
    * @see org.xml.sax.ErrorHandler
    */
   public void error(SAXParseException exception) throws SAXException
   {
      throw exception;
   }

   /**
    * @see org.xml.sax.ErrorHandler
    */
   public void fatalError(SAXParseException exception) throws SAXException
   {
      throw exception;
   }

    //
    // XMLErrorHandler methods
    //

   /**
    * @see org.apache.xerces.xni.parser.XMLErrorHandler
    */
   public void warning(String domain, String key, XMLParseException ex)
      throws XNIException
   {
      throw ex;
   }

   /**
    * @see org.apache.xerces.xni.parser.XMLErrorHandler
    */
   public void error(String domain, String key, XMLParseException ex)
      throws XNIException
   {
      throw ex;
   }

   /**
    * @see org.apache.xerces.xni.parser.XMLErrorHandler
    */
   public void fatalError(String domain, String key, XMLParseException ex)
      throws XNIException
   {
      throw ex;
   }

   /**
    * This is a callback method invoked when Xerces starts DTD parsing.
    *
    * @param locator  The document locator, <code>null</code> if the document
    *                 location cannot be reported during the parsing of
    *                 the document DTD. However, it is <em>strongly</em>
    *                 recommended that a locator be supplied that can
    *                 at least report the base system identifier of the
    *                 DTD.
    * @param augs Additional information that may include infoset
    *                      augmentations.
    *
    * @throws XNIException Thrown by handler to signal an error.
    */
   public void startDTD(XMLLocator locator, Augmentations augs)
      throws XNIException
   {
      if(fDTDGrammar != null )
         fDTDGrammar.startDTD(locator, augs);
      if (fDTDHandler != null)
         fDTDHandler.startDTD(locator, augs);
    }

   /**
    * Version of {@link #parseXmlForDtd(Document, boolean)} that creates a
    * <code>Document</code> from the supplied file.
    * See that method for documentation on other params and exceptions.
    * @param The file to read in, may not be <code>null</code>.
    * @throws IllegalArgumentException if xmlFile is <code>null</code> or
    * does not exist
    */
   public void parseXmlForDtd(File xmlFile, boolean generate)
      throws IOException, PSCatalogException, SAXException
   {
      if ((xmlFile == null) || (!xmlFile.exists()))
         throw new IllegalArgumentException("xmlFile may not be null and should exist");
      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(xmlFile);
         parseXmlForDtd(fis, generate);
      }
      finally
      {
         try
         {
            if (fis != null)
               fis.close();
         }
         catch  (Exception e)
         {
         }
      }
   }

   /**
    * Version of {@link #parseXmlForDtd(Document, boolean)} that creates a
    * <code>Document</code> from the input stream.
    * See that method for documentation on other params and exceptions.
    * @param insm the stream representing the XML file to parse for DTD, may not
    * be <code>null</code>. This method does not close the stream.
    * @throw IllegalArgumentException if ins is <code>null</code>
    */
   public void parseXmlForDtd(InputStream insm, boolean generate)
      throws IOException, PSCatalogException, SAXException
   {
      if (insm == null)
         throw new IllegalArgumentException("ins may not be null");
      InputSource ins = new InputSource(insm);
      parseXmlForDtd(ins, generate);
   }

   /**
    * Version of {@link #parseXmlForDtd(Document, boolean)} that creates a
    * <code>Document</code> from a URI.
    * See that method for documentation on other params and exceptions.
    * @param uri the absolute path of the XML file to parse for DTD, may not
    * be <code>null</code> or empty. If URI is invalid then getDtd() method
    * will return <code>null</code>.
    * @throw IllegalArgumentException if uri is <code>null</code> or empty
    */
   public void parseXmlForDtd(String uri, boolean generate)
      throws IOException, PSCatalogException, SAXException
   {
      if ((uri == null) || (uri.trim().length() == 0))
         throw new IllegalArgumentException("uri may not be null or empty");

      URL url = null;
      try
      {
         url = new URL(uri);
      }
      catch (MalformedURLException mue)
      {
         //it may be a local file path
         File f = new File(uri);
         if (f.isFile())
            parseXmlForDtd(f, generate);
         return;
      }
      if (url == null)
         return;
      InputStream is = null;
      try
      {
         is = url.openStream();
         parseXmlForDtd(is, generate);
      }
      finally
      {
         try
         {
            if (is != null)
               is.close();
         }
         catch  (Exception e)
         {
         }
      }
   }

   /**
    * Version of {@link #parseXmlForDtd(Document, boolean)} that creates a
    * <code>Document</code> from the input source.
    * See that method for documentation on other params and exceptions.
    * @param ins the input source representing the XML file to parse for DTD,
    * may not be <code>null</code>
    * @throw IllegalArgumentException if ins is <code>null</code>
    */
   public void parseXmlForDtd(InputSource ins, boolean generate)
      throws IOException, PSCatalogException, SAXException
   {
      if (ins == null)
         throw new IllegalArgumentException("xis may not be null");
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      //make a copy of the input stream
      StringBuffer buffer = new StringBuffer();
      int read = 0;
      byte[] buf = new byte[1024];
      InputStream is = ins.getByteStream();
      while (true)
      {
         read = is.read(buf);
         if (read == -1)
            break;
         buffer.append(new String(buf, 0, read));
      }
      String strDtd = buffer.toString();
      ByteArrayInputStream bis = new ByteArrayInputStream(strDtd.getBytes());

      //now parse the xml file
      InputSource insXml = new InputSource(bis);
      Document doc = null;
      try
      {
         db.setErrorHandler(this);
         doc = db.parse(insXml);
      }
      catch (SAXParseException spe)
      {
         String err = spe.getMessage().toUpperCase();
         if (err.indexOf("PREMATURE") != -1)
         {
            // if the xml file only contains DOCTYPE definition and no actual
            // XML element then xerces throws a SAXParseException with message
            // PREMATURE END OF FILE. For such cases, we find the root element
            // from the DTD defined in the DOCTYPE and then add a dummy root
            // element to the XML.
            PSSaxDtdParser saxDtdParser = new PSSaxDtdParser();
            String root = saxDtdParser.getRootElement(strDtd);
            if ((root == null) || (root.trim().length() == 0))
               return;
            String rootEl = "<" + root + "/>";
            strDtd = strDtd + rootEl;
            bis = new ByteArrayInputStream(strDtd.getBytes());
            insXml = new InputSource(bis);
            doc = db.parse(insXml);
         }
         else
         {
            throw spe;
         }
      }
      if (doc != null)
         parseXmlForDtd(doc, generate);
   }

   /**
    * parses an XML file and if DTD is present, parses the DTD, otherwise
    * optionally generates one from the xml.
    * @param doc the Document object representing the XML file to parse for DTD,
    * may not be <code>null</code>
    * @param generate if <code>true</code> and XML file does not contain
    * DocType then uses DTDGenerator to generate a DTD for the XML file
    * @throw IllegalArgumentException if doc is <code>null</code>
    * @throws IOException if any error occurs parsing the DTD from external subset
    * @throws PSCatalogException if any error occurs parsing the DTD
    * @throws SAXException if any error occurs parsing the DTD
    */
   public void parseXmlForDtd(Document doc, boolean generate)
      throws IOException, PSCatalogException, SAXException
   {
      fDTDGrammar = null;
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      DocumentType docType = doc.getDoctype();
      if (docType != null)
      {
         // First check the internal subset
         String internalSubset = docType.getInternalSubset();
         if (!((internalSubset == null) || (internalSubset.trim().length() == 0)))
         {
            ByteArrayInputStream bais = new ByteArrayInputStream(
               internalSubset.getBytes());
            parseDtd(bais, null);
            if (fDTDGrammar != null)
               return;
         }

         // then check the system identifier of the external subset
         String systemId = docType.getSystemId();
         if (!((systemId == null) || (systemId.trim().length() == 0)))
         {
            parseDtd(systemId);
            if (fDTDGrammar != null)
               return;
         }

         // finally check the public identifier of the external subset
         String publicId = docType.getPublicId();
         if (!((publicId == null) || (publicId.trim().length() == 0)))
         {
            parseDtd(publicId);
            if (fDTDGrammar != null)
               return;
         }
      }

      // no DTD exists, see if we should generate one
      if (generate)
         generateDtd(doc);
   }

   /**
    * Generates a DTD for the input Document object
    * @param doc the Document object for which DTD is to be generated,
    * may not be <code>null</code>
    * @throw IllegalArgumentException if doc is <code>null</code>
    */
   public void generateDtd(Document doc)
      throws IOException, PSCatalogException
   {
      fDTDGrammar = null;
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      PSDtdGenerator dtdGen = new PSDtdGenerator();
      dtdGen.generateDtd(doc);

      // collect the DTD in a byte array
      ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
      dtdGen.writeDtd(bout);

      // now parse the DTD with the parser
      String strDtd = bout.toString();
      if ((strDtd == null) || (strDtd.trim().length() == 0))
         return;

      ByteArrayInputStream bais = new ByteArrayInputStream(
         strDtd.getBytes());
      parseDtd(bais, null);
   }

  /**
   * parses the DTD represented the input dtdFile parameter.
   * @param dtdFile the File object representing the DTD file to parse, may not
   * be <code>null</code>
   * @throw IllegalArgumentException if dtdFile is <code>null</code> or does not
   * exist
   */
   public void parseDtd(File dtdFile)
      throws IOException, PSCatalogException
   {
      if ((dtdFile == null) || (!dtdFile.exists()))
         throw new IllegalArgumentException("dtdFile may not be null and should exist");

      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(dtdFile);
         parseDtd(fis, null);
      }
      finally
      {
         try
         {
            if (fis != null)
               fis.close();
         }
         catch  (Exception e)
         {
         }
      }
   }

  /**
   * parses the DTD represented the input insm parameter.
   * @param insm the stream representing the DTD to parse, may not be
   * <code>null</code>. This method does not close this stream.
   * @param encoding the encoding of the input stream, may be
   * <code>null</code> or empty in which case "UTF-8" encoding is used.
   * @throw IllegalArgumentException if insm is <code>null</code>
   */
   public void parseDtd(InputStream insm, String encoding)
      throws IOException, PSCatalogException
   {
      if (insm == null)
         throw new IllegalArgumentException("insm may not be null");

      parseDtd(new XMLInputSource(null, null, null, insm, encoding));
   }

  /**
   * parses the DTD represented by the input uri parameter.
   * @param uri the Absolute or Relative path or URL to the DTD to parse,
   * may not be <code>null</code> or empty
   * @throw IllegalArgumentException if uri is <code>null</code> or empty
   */
   public void parseDtd(String uri)
      throws IOException, PSCatalogException
   {
      if ((uri == null) || (uri.trim().length() == 0))
         throw new IllegalArgumentException("uri may not be null or empty");

      URL url = null;
      try
      {
         url = new URL(uri);
      }
      catch (MalformedURLException mue)
      {
         //it may be a local file path
         File f = new File(uri);
         if (f.isFile())
            parseDtd(f);
         return;
      }
      if (url == null)
         return;
      InputStream is = null;
      try
      {
         is = url.openStream();
         parseDtd(is, null);
      }
      finally
      {
         try
         {
            if (is != null)
               is.close();
         }
         catch  (Exception e)
         {
         }
      }
   }

  /**
   * parses the DTD represented the input xis parameter.
   * @param xis the input source representing the DTD to parse, may not be
   * <code>null</code>
   * @throw IllegalArgumentException if xis is <code>null</code>
   */
   public void parseDtd(XMLInputSource xis)
      throws IOException, PSCatalogException
   {
      fDTDGrammar = null;
      if (xis == null)
         throw new IllegalArgumentException("xis may not be null");

      //make a copy of the input stream
      StringBuffer buffer = new StringBuffer();
      int read = 0;
      byte[] buf = new byte[1024];
      InputStream is = xis.getByteStream();
      while (true)
      {
         read = is.read(buf);
         if (read == -1)
            break;
         buffer.append(new String(buf, 0, read));
      }
      String strDtd = buffer.toString();
      ByteArrayInputStream bis = new ByteArrayInputStream(strDtd.getBytes());

      //now parse the dtd
      String systemId = xis.getSystemId();
      XMLInputSource xisDtd = new XMLInputSource(null, systemId,
         null, bis, null);

      try
      {
         setErrorHandler(this);
         loadGrammar(xisDtd);
      }
      catch (XNIException xni)
      {
         String err = xni.getMessage().toUpperCase();
         if (err.indexOf("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD") != -1)
         {
            //dtd contains DOCTYPE declaration
            bis = new ByteArrayInputStream(strDtd.getBytes());
            try
            {
               parseXmlForDtd(bis, false);
            }
            catch (SAXException se)
            {
               se.printStackTrace();
               throw new PSCatalogException(
                  IPSServerErrors.XML_PARSER_SAX_ERROR,
                  se.toString());
            }
            return;
         }
         else
         {
            xni.printStackTrace();
            throw new PSCatalogException(
               IPSServerErrors.XML_PARSER_SAX_ERROR,
               xni.toString());
         }
      }
   }


   /**
    * Returns the DTD Grammar object encapsulating the parsed DTD
    * @return the DTD Grammar object encapsulating the parsed DTD, may be
    * <code>null</code> if <code>parseDTD</code> method has not been called
    * or errors occurred during DTD parsing
    */
   public PSDtd getDtd()
   {
      if (fDTDGrammar == null)
         return null;
      if (fDTDGrammar instanceof PSDtd)
         return (PSDtd)fDTDGrammar;
      return null;
   }

   /**
    * Returns a Grammar object by parsing the contents of the
    * entity pointed to by source.
    *
    * @param source the location of the entity which forms the starting point
    * of the grammar to be constructed.
    * @return DTD in memory. Never <code>null</code>.
    * @throws IOException When a problem is encountered reading the entity
    * @throws XNIException When a condition arises (such as a FatalError) that
    *         requires parsing of the entity be terminated.
    */
   public Grammar loadGrammar(XMLInputSource source)
      throws IOException, XNIException
   {
      reset();
      fDTDScanner.reset();

      // create a new configuration and set it to parse parameter and
      // general entities then reset the  <code>XMLEntityManager</code> using
      // this new configuration
      StandardParserConfiguration parserConf = new StandardParserConfiguration(
         fSymbolTable);
      parserConf.setFeature(EXTERNAL_PARAMETER_ENTITIES, true);
      parserConf.setFeature(EXTERNAL_GENERAL_ENTITIES, true);
      fEntityManager.reset(parserConf);

      fDTDGrammar = new PSDtd(source, fSymbolTable, new XMLDTDDescription(
         source.getPublicId(),
         source.getSystemId(),
         source.getBaseSystemId(),
         fEntityManager.expandSystemId(source.getSystemId(), null, false),
         null));
      fGrammarBucket = new DTDGrammarBucket();
      fDTDScanner.setInputSource(source);
      try
      {
         fDTDScanner.scanDTDExternalSubset(true);
      }
      catch (EOFException e)
      {
      }
      endDTD(null);
      if(fDTDGrammar != null && fGrammarPool != null)
      {
         fGrammarPool.cacheGrammars(XMLDTDDescription.XML_DTD,
            new Grammar[] {fDTDGrammar});
      }
      return fDTDGrammar;
   }

    /**
     * Constant for the feature identifier: external general entities
     */
    protected static final String EXTERNAL_GENERAL_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;

    /**
     * Constant for the feature identifier: external parameter entities
     */
    protected static final String EXTERNAL_PARAMETER_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;

}
