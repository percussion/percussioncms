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
package com.percussion.util;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Utility class to produce the error documents to be created by item level
 * validation exits.
 * Conforms to the sys_ItemValidation.dtd.
 */
public class PSItemErrorDoc
{
   /**
    * Adds a new error entry to the provided document. If the root element
    * is already there it will be used, if not a new root element will be
    * created and added.
    *
    * @param doc the document to add the new error to, not <code>null</code>.
    * @param submitName the field submit name for which to add a new error
    *    message, not <code>null</code> or empty.
    * @param displayName the field display name for which to add a new error
    *    message, not <code>null</code> or empty.
    * @param pattern the message string pattern, which will be formatted 
    *    together with the provided arguments, not <code>null</code> or
    *    empty.
    * @param args an array of String objects, containing all arguments which
    *    need to be formatted to the string pattern supplied, might be
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if doc, submitName, displayName or
    *    pattern are <code>null</code> or if submitName, displayName or
    *    pattern are empty. Also if the provided document contains an 
    *    unknown root element.
    */
   public static void addError(Document doc, String submitName, 
      String displayName, String pattern, Object[] args)
   {
      if (submitName == null || submitName.trim().length() == 0)
         throw new IllegalArgumentException(
            "submitName cannot be null or empty");
      if (displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName cannot be null or empty");
      
      String[] submitNames = { submitName };
      String[] displayNames = { displayName };
      addError(doc, submitNames, displayNames, pattern, args);
   }
   /**
    * Adds a new error entry to the provided document. If the root element
    * is already there it will be used, if not a new root element will be
    * created and added.
    *
    * @param doc the document to add the new error to, not <code>null</code>.
    * @param submitNames an array of field submit names for which to add a 
    *    new error message, not <code>null</code> or empty.
    * @param displayNames an arry of field display names for which to add a 
    *    new error message, not <code>null</code> or empty.
    * @param pattern the message string pattern, which will be formatted 
    *    together with the provided arguments, not <code>null</code> or
    *    empty.
    * @param args an array of String objects, containing all arguments which
    *    need to be formatted to the string pattern supplied, might be
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if doc, submitNames, displayNames or
    *    pattern are <code>null</code> or if submitNames, displayNames or
    *    pattern are empty. If submitNames and displayNames do not have the
    *    the same length or if the provided document contains an 
    *    unknown root element.
    */
   public static void addError(Document doc, String[] submitNames, 
      String[] displayNames, String pattern, Object[] args)
   {
      if (submitNames == null || submitNames.length == 0)
         throw new IllegalArgumentException(
            "submitName cannot be null or empty");
      if (displayNames == null || displayNames.length == 0)
         throw new IllegalArgumentException(
            "displayName cannot be null or empty");
      if (submitNames.length != displayNames.length)
         throw new IllegalArgumentException(
            "submitNames and displayNames must have the same length");
      if (pattern == null || pattern.trim().length() == 0)
         throw new IllegalArgumentException(
            "pattern cannot be null or empty");
      
      Element root = getRoot(doc);

      Element validationError = doc.createElement(VALIDATION_ERROR_ELEM);
      root.appendChild(validationError);
      
      Element errorFieldSet = doc.createElement(ERROR_FIELD_SET_ELEM);
      validationError.appendChild(errorFieldSet);
      
      for (int i=0; i<submitNames.length; i++)
      {
         Element errorField = doc.createElement(ERROR_FIELD_ELEM);
         errorField.setAttribute(SUBMIT_NAME_ATTR, 
            submitNames[i].toString());
         errorField.setAttribute(DISPLAY_NAME_ATTR, 
            displayNames[i].toString());
         errorFieldSet.appendChild(errorField);
      }
      
      Element errorMessage = doc.createElement(ERROR_MESSAGE_ELEM);
      validationError.appendChild(errorMessage);

      Element patternElem = doc.createElement(PATTERN_ELEM);
      Text patternText = doc.createTextNode(pattern);
      patternElem.appendChild(patternText);
      errorMessage.appendChild(patternElem);
      
      if (args != null && args.length > 0)
      {
         for (int i=0; i<args.length; i++)
         {
            Element argument = doc.createElement(ARGUMENT_ELEM);
            Text argumentText = doc.createTextNode(args[i].toString());
            argument.appendChild(argumentText);
            errorMessage.appendChild(argument);
         }
      }
   }
   
   /**
    * Gets or creates the root from the provided document and validates it.
    *
    * @param doc the document we need the root element from, 
    *    not <code>null</code>.
    * @return the root element, never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is 
    *    <code>null</code> or the contains an unknown root.
    */
   private static Element getRoot(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("document cannot be null");

      Element root = doc.getDocumentElement();
      if (root == null)
      {
         root = doc.createElement(VALIDATION_ERROR_SET_ELEM);
         doc.appendChild(root);
      }
      else if (!root.getNodeName().equals(VALIDATION_ERROR_SET_ELEM))
         throw new IllegalArgumentException("document contains unknown root");
      
      return root;
   }
   
   // test
   public static void main(String[] args)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      String submitName = "name";
      String displayName = "label";
      String pattern1 = "Fields with errors= {0}.";
      Object[] args1 = { "1" };
      PSItemErrorDoc.addError(doc, submitName, displayName, pattern1, args1);
      
      String[] submitNames = { "name1", "name2" };
      String[] displayNames = { "label1", "label2" };
      String pattern2 = "Fields with errors= {0}.";
      Object[] args2 = { "2" };
      PSItemErrorDoc.addError(doc, submitNames, displayNames, pattern2, args2);
      
      System.out.println("...error document:\n" + PSXmlDocumentBuilder.toString(doc));
   }
   
   /** 
    * The error set element name as specified in sys_ItemValidation.dtd.
    */
   public final static String VALIDATION_ERROR_SET_ELEM = "ValidationErrorSet";
   /** 
    * The error element name as specified in sys_ItemValidation.dtd.
    */
   public final static String VALIDATION_ERROR_ELEM = "ValidationError";
   /** 
    * The error field set element name as specified in sys_ItemValidation.dtd.
    */
   public final static String ERROR_FIELD_SET_ELEM = "ErrorFieldSet";
   /** 
    * The error field element name as specified in sys_ItemValidation.dtd.
    */
   public final static String ERROR_FIELD_ELEM = "ErrorField";
   /** 
    * The error message element name as specified in sys_ItemValidation.dtd. 
    */
   public final static String ERROR_MESSAGE_ELEM = "ErrorMessage";
   /** 
    * The error message pattern element name as specified in 
    * sys_ItemValidation.dtd. 
    */
   public final static String PATTERN_ELEM = "Pattern";
   /** 
    * The error message argument element name as specified in 
    * sys_ItemValidation.dtd. 
    */
   public final static String ARGUMENT_ELEM = "Argument";

   /** 
    * The submit name attribute name as specified in sys_ItemValidation.dtd.
    */
   public final static String SUBMIT_NAME_ATTR = "submitName";
   /** 
    * The display name attribute name as specified in sys_ItemValidation.dtd.
    */
   public final static String DISPLAY_NAME_ATTR = "displayName";
}
