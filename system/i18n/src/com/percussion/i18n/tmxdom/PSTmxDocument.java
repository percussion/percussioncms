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
package com.percussion.i18n.tmxdom;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.tools.PSCopyStream;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This is a wrapper around the TMX (Translation Memory Exchange) Document which
 * is an XML document. TMX has a DTD that very generic, however, for the purpose
 * of Rhythmyx i18n, we use only a subset of features provided by the TMX DTD.
 * This class provides means to
 * <ol>
 * <li>Create a TMX document from scratch </li>
 * <li>Modify an existing one</li>
 * <li>Merge two TMX documents with configurable merge options</li>
 * </ol>
 *
 **/
public class PSTmxDocument extends PSTmxNode
        implements IPSTmxDocument
{
   /**
    * Default constructor. Creates an empty TMX document. Initializes default
    * merge configuration object and XSL document for sorting.
    * @throws PSTmxDomException creation fails for any reason.
    */
   public PSTmxDocument()
           throws PSTmxDomException
   {
      try
      {
         createNew();
         init(true);
      }
      catch(Exception e)//any exception is fatal
      {
         throw new PSTmxDomException(e.getMessage());
      }
   }

   /**
    * Convenience ctor that calls {@link #PSTmxDocument(Document, boolean)
    * this(DOMDoc, true)}
    */
   public PSTmxDocument(Document DOMDoc)
           throws PSTmxDomException
   {
      this(DOMDoc, true);
   }

   /**
    * Constructor that takes the input TMX file as XML DOM document.
    * Creates an TMX document object from the supplied XML document. Initializes
    * default  merge configuration object and XSL document for sorting.
    *
    * @param DOMDoc the input XML DOM document, must not be <code>null</code>.
    * @param createDefault If <code>true</code>, a variant is also
    * added for the default language to each translation unit if it does not
    * already exist.  If <code>false</code>, no defaults are added.
    *
    * @throws PSTmxDomException if initialization fails for any reason
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   public PSTmxDocument(Document DOMDoc, boolean createDefaults)
           throws PSTmxDomException
   {
      if(DOMDoc == null)
         throw new IllegalArgumentException("DOMDoc must nt be null");
      try
      {
         m_DOMDocument = DOMDoc;
         m_DOMElement = m_DOMDocument.getDocumentElement();
         init(createDefaults);
      }
      catch(Exception e)
      {
         throw new PSTmxDomException(e.getMessage());
      }
   }

   /**
    * Helper method to create an empty TMX DOM documet. Sets the TMX version to
    * '1.4'.
    */
   private void createNew()
   {
      m_DOMDocument = PSXmlDocumentBuilder.createXmlDocument();
      m_DOMElement = PSXmlDocumentBuilder.createRoot(m_DOMDocument, TMXNODENAME);
      m_DOMElement.setAttribute("version", "1.4");
   }

   /**
    * Helper method to initialize the TMX document object. Sets default merge
    * configuratoin. Constructs required parts of the TMX document, viz. header
    * and body. Also initializes the sorting XSL document.
    *
    * @param createDefault If <code>true</code>, a variant is also
    * added for the default language to each translation unit if it does not
    * already exist.  If <code>false</code>, no defaults are added.
    *
    * @throws IOException
    * @throws SAXException
    */
   private void init(boolean createDefault)
           throws IOException, SAXException
   {
      m_PSTmxDocument = this;
      m_MergeConfig = new PSTmxMergeConfig();
      m_Header = new PSTmxHeader(m_PSTmxDocument);
      m_Body = new PSTmxBody(m_PSTmxDocument, createDefault);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public Document getDOMDocument()
   {
      return m_DOMDocument;
   }

   /**
    * Override the method defined in the interface {@link IPSTmxNode} to apply
    * sorting stylesheet.
    * @return String representation of the sorted XML document associated with
    * this TMX document. Never <code>null</code> or <code>empty</code>.
    * @throws PSTmxDomException
    */
   public String toString()
           throws PSTmxDomException
   {
      Document tempDoc = m_DOMDocument;
      //Apply sort style sheet before converting to a string
      try
      {
         //this check is not really required since the stylesheet is part of
         //the package.
         if(ms_xslMergeDoc != null)
         {
            tempDoc = transformXML(m_DOMDocument, ms_xslMergeDoc);
         }
      }
      catch(Exception e)
      {
         //any failure, we use the unsorted version (original)
      }
      return PSXmlDocumentBuilder.toString(tempDoc);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public Iterator getTranslationUnits()
   {
      return m_Body.getTraslationUnits();
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public Object[] getSupportedLanguages()
   {
      return m_Header.getSupportedLanguages();
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public void setMergeConfigDoc(Document mergDoc)
   {
      if(mergDoc==null)
         throw new IllegalArgumentException("mergDoc must not be null");
      m_MergeConfig.setConfigDoc(mergDoc);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxMergeConfig getMergeConfig()
   {
      return m_MergeConfig;
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxHeader getHeader()
   {
      return m_Header;
   }
   /**
    * Merges the header object from the supplied TMX document object with the
    * header of this TMX document object. Merging header simply means adding the
    * supported linguages from the supplied document, if they already do not exist.
    * @param srcDoc the TMX document object whose header is to merged with that
    * of current one.
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   protected void mergeHeader(IPSTmxDocument srcDoc)
   {
      if(srcDoc==null)
      {
         throw new IllegalArgumentException(
                 "srcDoc for merging headers must not be null");
      }
      Object[] langs = srcDoc.getSupportedLanguages();
      for(int i=0; langs!=null && i<langs.length;i++)
         addLanguage(langs[i].toString());
   }

   /**
    * Merges the body of the supplied TMX document object with the that of this
    * TMX document object using the merge configuration set.
    * @param srcDoc the TMX document object whose body is to merged with that
    * of current one.
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   protected void mergeBody(IPSTmxDocument srcDoc)
   {
      if(srcDoc==null)
      {
         throw new IllegalArgumentException(
                 "srcDoc for merging body must not be null");
      }
      Iterator<Map.Entry> iter = srcDoc.getTranslationUnits();
      Map.Entry entry = null;
      IPSTmxTranslationUnit srcTu = null;
      while(iter.hasNext())
      {
         entry = iter.next();
         srcTu = (IPSTmxTranslationUnit)entry.getValue();
         m_Body.merge(srcTu);
      }
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public void addLanguage(String language)
   {
      if(language == null)
         throw new IllegalArgumentException("language must not be null");
      //once we add a new language we need to add corresponding the stubs
      //for all tu s.
      m_Header.addLanguage(language);
      if(!language.equalsIgnoreCase(PSI18nUtils.DEFAULT_LANG))
      {
         Iterator< Map.Entry> iter = m_Body.getTraslationUnits();
         Map.Entry entry = null;
         IPSTmxTranslationUnit srcTu = null;
         while(iter.hasNext())
         {
            entry = (Map.Entry)iter.next();
            srcTu = (IPSTmxTranslationUnit)entry.getValue();
            srcTu.addTuv(createTranslationUnitVariant(language, ""), false);
         }
      }
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public void removeTranslationUnit(IPSTmxTranslationUnit tu)
   {
      m_Body.removeTranslationUnit(tu);
   }

   /**
    * Overriding the method from the interface {@link #IPSTmxNode}.
    * Only two types of nodes are allowed for merging, viz. {@link #IPSTmxDocument},
    * {@link #IPSTmxTranslationUnit}
    * @param node must not be <code>null</code>.
    * @throws PSTmxDomException if merge cannot proceed.
    * @throws IllegalArgumentExcpetion supplied node is <code>null</code>.
    */
   public void merge(IPSTmxNode node)
           throws PSTmxDomException
   {
      if(node == null)
      {
         throw new IllegalArgumentException(
                 "Node to be merged must not be null");
      }
      else if(node instanceof IPSTmxDocument)
      {
         IPSTmxDocument srcDoc = (IPSTmxDocument)node;
         mergeHeader(srcDoc);
         mergeBody(srcDoc);
      }
      else if(node instanceof IPSTmxTranslationUnit)
      {
         //Adds the supplied translation unit object to the document. Follows the merge
         //rules in the merge configuration document while adding.
         m_Body.merge(node);
      }
      else
      {
         String[] args = {"IPSTmxDocument", "IPSTmxTranslationUnit"};
         throw new PSTmxDomException("onlyNodeAllowedForMergeAre", args);
      }
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxDocument extract(String languageString) throws SAXException,
           TransformerException
   {
      if (languageString == null || languageString.trim().length() == 0)
         throw new IllegalArgumentException(
                 "languageString may not be null or empty");

      Document tempDoc = null;

      //Apply extract stylesheet
      if(ms_xslExtractDoc == null)
      {
         //this should never happen, throw runtime exception
         throw new RuntimeException("extract stylesheet not loaded");
      }

      Map params = new HashMap();
      params.put("extractlang", languageString);
      tempDoc = transformXML(m_DOMDocument, ms_xslExtractDoc, params);
      return new PSTmxDocument(tempDoc, false);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxTranslationUnit createTranslationUnit(String key,
                                                      String description)
   {
      if(key == null)
         throw new IllegalArgumentException("Key must not be null to create tu");
      if(description==null)
         description = "";
      Element tu = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_TU);
      tu.setAttribute(IPSTmxDtdConstants.ATTR_TUID, key);
      Element note = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_NOTE);
      note.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG,
              PSI18nUtils.DEFAULT_LANG);
      Text noteVal = m_DOMDocument.createTextNode(description);
      PSXmlDocumentBuilder.copyTree(note.getOwnerDocument(),
              note, noteVal, false);
      PSXmlDocumentBuilder.copyTree(tu.getOwnerDocument(), tu, note, false);
      return new PSTmxTranslationUnit(this, tu);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxTranslationUnitVariant createTranslationUnitVariant(
           String language, String value)
   {
      if(language == null)
         throw new IllegalArgumentException("Language must be null to create tuv");
      if(value==null)
         value = "";
      Element tuv = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_TUV);
      tuv.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG, language);
      Element seg = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_SEG);
      Text segVal = m_DOMDocument.createTextNode(value);
      PSXmlDocumentBuilder.copyTree(seg.getOwnerDocument(), seg, segVal, false);
      PSXmlDocumentBuilder.copyTree(tuv.getOwnerDocument(), tuv, seg, false);
      return new PSTmxTranslationUnitVariant(this, tuv);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxSegment createSegment(String value)
   {
      if(value==null)
         value = "";
      Element seg = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_SEG);
      Text segVal = m_DOMDocument.createTextNode(value);
      PSXmlDocumentBuilder.copyTree(seg.getOwnerDocument(), seg, segVal, false);
      return new PSTmxSegment(this, seg);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxNote createNote(String language, String value)
   {
      if(language == null || language.length() < 1)
      {
         throw new IllegalArgumentException(
                 "language must not be null or empty to create Note");
      }
      if(value == null || value.length() < 1)
      {
         throw new IllegalArgumentException(
                 "value must not be null or empty to create Note");
      }
      Element note = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_NOTE);
      note.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG, language);
      Text noteVal = m_DOMDocument.createTextNode(value);
      PSXmlDocumentBuilder.copyTree(note.getOwnerDocument(), note, noteVal, false);
      return new PSTmxNote(this, note);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public IPSTmxProperty createProperty(String type, String language, String value)
   {
      if(type == null || type.length() < 1)
      {
         throw new IllegalArgumentException(
                 "type must not be null or empty to create Property");
      }
      if(value == null || value.length() < 1)
      {
         throw new IllegalArgumentException(
                 "value must not be null or empty to create Property");
      }
      if(language == null)
         language = "";

      Element prop = m_DOMDocument.createElement(IPSTmxDtdConstants.ELEM_PROP);
      prop.setAttribute(IPSTmxDtdConstants.ATTR_TYPE, type);
      prop.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG, language);
      Text propVal = m_DOMDocument.createTextNode(value);
      PSXmlDocumentBuilder.copyTree(prop.getOwnerDocument(), prop, propVal, false);
      return new PSTmxProperty(this, prop);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public void save(File file)
           throws IOException, UnsupportedEncodingException, FileNotFoundException
   {
      save(file, true);
   }

   /*
    * Implementation of the method defined in the interface IPSTmxDocument.
    */
   public void save(File file, boolean createBackup)
           throws IOException, UnsupportedEncodingException, FileNotFoundException
   {
      //Create Backup File
      if(createBackup && file.exists())
      {
         try(FileInputStream fis = new FileInputStream(file)) {
            try (FileOutputStream fos = new FileOutputStream(
                    file.getAbsolutePath() + ".bak")) {
               PSCopyStream.copyStream(fis, fos);
               fos.flush();
            }
         }
      }
      //Save the file with UTF-8 encoding
      try(OutputStreamWriter writer = new OutputStreamWriter(
              new FileOutputStream(file), StandardCharsets.UTF_8)) {
         writer.write(toString());
         writer.flush();
      }
   }

   /**
    * Convenience method that calls {@link #transformXML(Document, Document,
    * Map) transformXML(srcDoc, xslDoc, null)}
    */
   public static Document transformXML(Document srcDoc, Document xslDoc)
           throws SAXException, TransformerException
   {
      return transformXML(srcDoc, xslDoc, null);
   }

   /**
    * Show how to transform a DOM tree into another DOM tree.
    * This uses the javax.xml.parsers to parse an XML file into a
    * DOM, and create an output DOM.
    *
    * @param srcDoc source document, must not be <code>null</code>.
    * @param xslDoc xsl Document for transforming, must not be <code>null</code>.
    * @param   params An optional map of parameters, where for each entry the
    * the key is the param name as a <code>String</code> and the value is an
    * object whose <code>toString</code> method will be used as the value of the
    * param.  The params are passed to the style sheet processor, and will be
    * used to set the value of any global parameters defined as an <code>
    * xsl:param</code> element in the stylesheet with a matching name (the
    * parameter must be defined as a child of the xsl:stylesheet
    * element).  If a matching parameter declaration is not found in the
    * stylesheet, the supplied parameter is silently ignored.  May be <code>
    * null</code> if no parameters are to be supplied.
    *
    * @return the transformed XML DOM Document. Never <code>null</code>.
    *
    * @throws SAXException
    * @throws TransformerException
    * @throws IllegalArgumentException
    */
   public static Document transformXML(Document srcDoc, Document xslDoc,
                                       Map params)
           throws SAXException, TransformerException
   {
      if(srcDoc==null)
      {
         throw new IllegalArgumentException("srcDoc must not be null.");
      }
      if(xslDoc == null)
      {
         throw new IllegalArgumentException("xslDoc must not be null.");
      }
      TransformerFactory tfactory = TransformerFactory.newInstance();
      if(!tfactory.getFeature(DOMSource.FEATURE))
      {
         throw new org.xml.sax.SAXNotSupportedException(
                 "DOM node processing not supported!");
      }

      Templates templates;
      Document outNode = PSXmlDocumentBuilder.createXmlDocument();
      DOMSource dsource = new DOMSource(xslDoc);
      templates = tfactory.newTemplates(dsource);
      Transformer transformer = templates.newTransformer();
      if (params != null)
      {
         Iterator< Map.Entry> iter = params.entrySet().iterator();
         while (iter.hasNext())
         {
            Map.Entry param = (Map.Entry)iter.next();
            transformer.setParameter(param.getKey().toString(),
                    param.getValue().toString());
         }
      }
      transformer.transform(
              new DOMSource(srcDoc), new DOMResult(outNode));

      return outNode;
   }

   /**
    * TMX header object. A TMX document must have one header. Never <code>null</code>
    * after the class object is constructed.
    */
   protected IPSTmxHeader m_Header;

   /**
    * TMX header object. A TMX document must have one header. Never<code>null</code>
    * after the object is created.
    */
   protected PSTmxBody m_Body;

   /**
    * XML DOM document representing the tmx resource this object is wrapping.
    * Never <code>null</code> after the object is created.
    */
   protected Document m_DOMDocument;

   /**
    * The merge configuration object. Never <code>null</code> after this object
    * is created. One of the main purposes of wrapinng the DOM document onto TMX
    * Document is to provide configurable merge mechanism. A default merge
    * configuration is shipped as part of the package. Can be changed later using
    * {@link #setMergeConfigDoc} method
    */
   protected PSTmxMergeConfig m_MergeConfig = null;

   /**
    * This is an XSL document that is used to sort the result TMX document
    * meaningfully. The stylesheet for this purpose is shipped part of the
    * project and not changeable. May be <code>null</code> after this object is
    * created if there was an error loading it during the static intializer.
    */
   protected static Document ms_xslMergeDoc = null;

   /**
    * This is an XSL document that is used to extract a single language from the
    * document. The stylesheet for this purpose is shipped part of the
    * project and not changeable. May be <code>null</code> after this object is
    * created if there was an error loading it during the static intializer.
    */
   protected static Document ms_xslExtractDoc = null;

   /**
    * Root element name for the TMX document.
    */
   public static final String TMXNODENAME =
           IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXROOT];

   /**
    * Name of the stylesheet used to sort the TMX document. This is shipped as
    * resource in the package. Whenever the document is converted to string, this
    * stylesheet is used to sorting.
    */
   public static final String SORTING_XSL = "sortresourcebundle.xsl";

   /**
    * Name of the stylesheet used to extract a language from the TMX document.
    * This is shipped as resource in the package.
    */
   public static final String EXTRACT_XSL = "extractresourcebundle.xsl";

   /**
    * main method for testing purpose.
    * @param    args
    */
   public static void main(String[] args) {

      try (InputStreamReader ir = new InputStreamReader(
              new FileInputStream(
                      "rxconfig/i18n/ResourceBundle.tmx"), StandardCharsets.UTF_8)) {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(ir, false);
         PSTmxDocument tmxdoc = new PSTmxDocument(doc);
         tmxdoc.addLanguage("en-us");

         try (InputStreamReader ir2 = new InputStreamReader(
                 new FileInputStream("c:/test.tmx"), StandardCharsets.UTF_8)) {

            Document doc2 = PSXmlDocumentBuilder.createXmlDocument(ir2, false);
            PSTmxDocument tmxdoc2 = new PSTmxDocument(doc2);

            IPSTmxTranslationUnit tu = null;
            tu = tmxdoc2.createTranslationUnit(
                    "psx.test.key@Content Title2", "<Note to translator>");

            tu.merge(tmxdoc2.createSegment("Test"));

            try (OutputStreamWriter or = new OutputStreamWriter(
                    new FileOutputStream(
                            "rxconfig/i18n/ResourceBundle.tmx"), StandardCharsets.UTF_8)) {
               PSXmlDocumentBuilder.write(tmxdoc.getDOMDocument(), or);
            }
         }
      } catch ( IOException | SAXException e) {
         e.printStackTrace();

      }
   }

      static
   {
      try
      {
         try(InputStream is = PSTmxDocument.class.getResourceAsStream(SORTING_XSL)) {
            ms_xslMergeDoc = PSXmlDocumentBuilder.createXmlDocument(is , false);
            try(InputStream is2 = PSTmxDocument.class.getResourceAsStream(EXTRACT_XSL)) {
               ms_xslExtractDoc = PSXmlDocumentBuilder.createXmlDocument(is2, false);
            }
         }
      }
      catch(Exception e)
      {
         //ignore errors. Will be handled at runtime.
      }

   }
}
