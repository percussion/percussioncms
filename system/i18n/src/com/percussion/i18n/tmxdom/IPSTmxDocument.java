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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This interface defines methods to create, modify and merge TMX documents. A
 * TMX document is nothing more than an XML document however, the structure of the
 * document serves a special purpose. The DTD for TMX document corresponds to
 * to the Translation Memory Exchange Specification version 1.4. Details can
 * be found at:
 * <p>
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 * </p>
 * A TMX document (which is an XML document) consists of one header and one body
 * as child elements.
 * @see IPSTmxHeader
 * @see PSTmxBody
 **/
public interface IPSTmxDocument
   extends IPSTmxNode
{
   /**
    * Method to add a new supported language to the TMX document. The list of
    * supported languages is added as a list of &lt;prop&gt; elements the name
    * attribute being 'supportedlanguage ' defined in <code>IPSTmxDtdConstants</code>
    * interface.
    * @param    language    New language to be added. The syntax shall be like
    * "en-us" or "fr-ca". Nothing happens if language is already added to the
    * document. Must not be <code>null</code>.
    * @throws IllegalArgumentException if the argument is <code>null</code>
    * or <code>empty</code>
    * @see com.percussion.i18n.tmxdom.IPSTmxDtdConstants
   */
   public void addLanguage(String language);

   /**
    * Creates a copy of this document, with only the translation unit variants 
    * for the specified language string.  Will contain the same header and all 
    * translation units (keys) of this document, but with only the specified 
    * subset of language notes, properies, and variants.  If the specified 
    * language variants do not exist, then a document with no variants is 
    * returned.
    * 
    * @param languageString The identifier of the language for which the 
    * variants are to be extracted, may not be <code>null</code> or empty.
    * 
    * @return The TMX document, never <code>null</code>.
    * 
    * @throws SAXException if the transform required for the extraction cannot 
    * be performed.
    * @throws TransformerException if there are any other errors.
    */
   public IPSTmxDocument extract(String languageString) throws SAXException, 
      TransformerException;
   
   /**
   * Method to remove a translation unit matching the supplied one. A translation
   * unit assumed to be matching if the tuid attribute for the &lt;tu&gt; element
   * of the supplied one matches with that of one of the translational units in
   * the document.
   * @param    tu    Translation Unit object to be added, must not be
   * <code>null</code>.
   * @throws IllegalArgumentException if the argument is <code>null</code>
   */
   public void removeTranslationUnit(IPSTmxTranslationUnit tu);

   /**
    * Creates a translation unit with supplied parameters. This is created in
    * the fly and should be added to the document explicitly using add method.
    * @return Newly created Translation Unit object, never <code>null</code>.
    * @param    key    key is the unique identifier for the translation unit.
    * Must not be <code>null</code>
    * @param    description    Description of the keyword whose value needs to
    * be translated to other languages supported. This becomes the note for
    * the translator, may be <code>null</code> or <code>empty</code>.
    * @throws IllegalArgumentException if the argument key is <code>null</code>
    * or <code>empty</code>
    */
   public IPSTmxTranslationUnit createTranslationUnit(
      String key, String description);

   /**
    * Creates a translation unit variant with supplied parameters. This is
    * created in the fly and should be added to the document explicitly using
    * add method.
    * @param    language    Must not be <code>null</code> or <code>empty</code>.
    * @param    value    Translation value for the key in the specified language.
    * If <code>null</code> supplied <code>empty</code> assumed.
    * @return newly created translation unit variant object, never <code>null</code>.
    * @throws IllegalArgumentException if the argument language is
    * <code>null</code> or <code>empty</code>.
    */
   public IPSTmxTranslationUnitVariant createTranslationUnitVariant(
      String language, String value);

   /**
    * Creates a new segement object in the fly. Must be added to the document 
    * explicitly.
    * @return Newly created segement object, never <code>null</code>.
    * @param    value    Value of the segment, if <code>null</code> value
    * supplied , <code>empty</code> is assumed.
    */
   public IPSTmxSegment createSegment(String value);

   /**
    * Creates a new Note object in the fly. Must be added to the document explicitly.
    * @return newly created note object.
    * @param    language    language for the Note, must not be <code>null</code>
    * or <code>empty</code>
    * @param    value    Note value, must not be <code>null</code> or
    * <code>empty</code>
    * @throws IllegalArgumentException if any of the arguments is
    * <code>null</code> or <code>empty</code>.
    */
   public IPSTmxNote createNote(String language, String value);

   /**
    * Creates a new TMX prop object in the  fly. Must be added to the document
    * explicitly.
    * @param    type    Property type must not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language for the property, may be <code>null</code>
    * or <code>empty</code>
    * @param    value    Note value, must not be <code>null</code> or
    * <code>empty</code>
    * @return newly created Property object. Never <code>null</code>.
    * @throws IllegalArgumentException if any of the arguments <b>type</b>
    * or <b>value</b> is <code>null</code> or <code>empty</code>.
    */
   public IPSTmxProperty createProperty(
      String type, String language, String value);

   /**
    * Method to access the XML DOM document associated with TMX document.This
    * should never  be modified directly if the TMX document needs to be used
    * further. All  modifications should be done using the TMX DOM, not using
    * XML DOM.
    * @return DOM document associated with this TMX document.
    */
   public Document getDOMDocument();

   /**
    * Method to set the merge configuration XML DOM document. A TMX document has
    * a default merge config document set when it was created until a different
    * one is explicitly set using this method.
    * @param mergDoc merge configuration document, must not be <code>null</code>.
    * @see IPSTmxMergeConfig
    */
   public void setMergeConfigDoc(Document mergDoc);

   /**
    * Access method for the merge configuration XML DOM document.
    * @return Merge configuration document, never <code>null</code>.
    * @see IPSTmxMergeConfig
    */
   public IPSTmxMergeConfig getMergeConfig();

   /**
    * Method to get the TMX header node object of the TMX document.
    * @return header object, never <code>null</code>.
    */
   public IPSTmxHeader getHeader();

   /**
    * Method to get all translation units from the TMX Document.
    * @return iterator of translation unit objects. may be <code>null</code>.
    * @see IPSTmxTranslationUnit
    * @see IPSTmxHeader
    */
   public Iterator getTranslationUnits();

   /**
    * Returns the array of supported languages specified in the header of the
    * TMX document.
    * @return String array of languages, may be <code>null</code> or <code>empty</code>
    */
   public Object[] getSupportedLanguages();

   /**
    * Saves the TMX document to the file specified.
    * @param file file to save the document to. Must not be <code>null</code>
    * @throws IOException if save fails for any reason.
    */
   public void save(File file)
      throws IOException;

   /**
    * Saves the TMX document to the file specified creating a backup file
    * optionally. The backedup file name will be supplied file name appended
    * with '.bak'.
    * @throws IOException if save fails for any reason.
    * @param file file to save the document to. Must not be <code>null</code>
    * @param createBackup falg to request to create a backup file.
    */
   public void save(File file, boolean createBackup)
      throws IOException;
}
