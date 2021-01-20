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


package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.PSServer;
import com.percussion.server.content.PSFormContentParser;
import com.percussion.util.PSEntrySet;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Element;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Class for utility methods used by deployment components
 */
public class PSDeployComponentUtils
{
   /**
    * Utility method to get a required attibute value, validating that it is
    * not <code>null</code> or empty.
    *
    * @param source Element to get the attribute from, may not be
    * <code>null</code>.
    * @param attName The name of the attribute to get, may not be
    * <code>null</code> or empty
    *
    * @return The attribute value, never <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the specified attribute cannot be
    * found with a non-empty value.
    */
   public static String getRequiredAttribute(Element source, String attName)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (attName == null || attName.trim().length() == 0)
         throw new IllegalArgumentException("attName may not be null or empty");

      String val = source.getAttribute(attName);
      if (val == null || val.trim().length() == 0)
      {
         Object[] args = {source.getTagName(), attName, "empty"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      return val;
   }

   /**
    * Gets the specified element's data from the supplied tree, validating that
    * it is not <code>null</code> and optionally not empty.
    *
    * @param tree The tree walker to use, assumed to be set at the parent node
    * and may not be <code>null</code>.
    * @param parent The name of the parent node, may not be <code>null</code> or
    * empty.
    * @param node The name of the node, may not be <code>null</code> or empty.
    * @param notEmpty If <code>true</code>, will also enforce that element value
    * is not empty.
    *
    * @return The value, never <code>null</code>, may be empty only if
    * <code>notEmpty</code> is <code>false</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * pass validation.
    */
   public static String getRequiredElement(PSXmlTreeWalker tree, String parent,
      String node, boolean notEmpty)  throws PSUnknownNodeTypeException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree may not be null");

      if (parent == null || parent.trim().length() == 0)
         throw new IllegalArgumentException("parent may not be null or empty");

      if (node == null || node.trim().length() == 0)
         throw new IllegalArgumentException("node may not be null or empty");

      String temp = tree.getElementData(node);
      if (temp == null || (notEmpty && temp.trim().length() == 0))
      {
         Object[] args = { parent, node, temp == null ?
            "null" : temp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      return temp;
   }

   /**
    * Clone a list from an iterator.
    *
    * @param iter The iterator to be cloned, may not <code>null</code>.
    *
    * @return The cloned list, will not be <code>null</code>, but may be empty.
    */
   public static List cloneList(Iterator iter)
   {
      if (iter == null)
         throw new IllegalArgumentException("iter may not be null");

      List list = new ArrayList();
      while (iter.hasNext())
         list.add(iter.next());
      return list;
   }

   /**
    * Get next element from the <code>tree</code> and <code>flags</code>,
    * validating the parameters and the retrieved element that it is not
    * <code>null</code>.
    *
    * @param tree The tree to get the next element from, may not be
    * <code>null</code>.
    * @param flags The appropriate <code>PSXmlTreeWalker.GET_NEXT_xxx</code>
    * flags.
    * @param nodeName The expected XML node name, may not be <code>null</code>
    * or empty.
    *
    * @return The retrieved and validated element, never <code>null</code>.
    *
    * @throws IllegalArgumentException if there is an invalid parameters.
    * @throws PSUnknownNodeTypeException if fail to retrieve next element from
    * the <code>tree</code>.
    */
   public static Element getNextRequiredElement(PSXmlTreeWalker tree,
      int flags, String nodeName) throws PSUnknownNodeTypeException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree may not be null");

      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "nodeName may not be null or empty");

      Element element = tree.getNextElement(flags);

      if ( element == null )
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, nodeName);
      }
      return element;
   }

   /**
    * Gets the element data from an attribute and validates that the data
    * is a legal value.  If the data is <code>null</code> or empty, it will be
    * set with a default value (assumed to be the value at index 0 of the legal
    * value array).
    *
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *        that should contain the specified attribute.
    * @param attrName the name of the attribute to retrieve data from;
    *        not <code>null</code> or emtpy.
    * @param legalValues the array of permitted values (case sensitive), with a
    *        default value at index 0.
    * @return The index into the array of the value to use.
    * 
    * @throws PSUnknownNodeTypeException if the node has an illegal value
    * @throws IllegalStateException if tree is not postioned at an Element.
    */
   public static int getEnumeratedAttributeIndex(PSXmlTreeWalker tree,
                                                  String attrName,
                                                  String[] legalValues)
         throws PSUnknownNodeTypeException
   {
      if (null == tree)
         throw new IllegalArgumentException("tree cannot be null");
      if (null == attrName)
         throw new IllegalArgumentException("attrName cannot be null");
      if (null == legalValues || legalValues.length == 0)
         throw new IllegalArgumentException("legalValues");
      if (tree.getCurrent() == null)
         throw new IllegalStateException(
            "tree must be positioned on an element");

      int index = 0;
      String data = tree.getElementData(attrName);
      if (null == data || data.trim().length() == 0)
         // no value means use the default
         index = 0;
      else
      {
         // make sure the value is legal
         boolean found = false;
         for (int i = 0; i < legalValues.length; i++)
         {
            if (legalValues[i] != null && legalValues[i].equals(data))
            {
               found = true;
               index = i;
               break;
            }
         }

         if (!found)
         {
            String parentName = tree.getCurrent().getNodeName();
            Object[] args = {parentName, attrName, data};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      return index;
   }

   /**
    * Strips leading "../", cannonical file root, or server request root e.g.
    * "/Rhythmyx/" from a path.
    *
    * @param path The path, may not be <code>null</code> or empty, may or may
    * not begin with the prefix.
    * 
    * @return The stripped path, never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if <code>path</code> is invalid
    */
   public static String stripPathPrefix(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      String prefix = "../";
      if (path.startsWith(prefix))
         path = path.substring(prefix.length());
      else
      {
         // strip root dir if a cannonical path is supplied
         String tmpPath;
         tmpPath = getNormalizedPath(path);
         String rootPath = PSServer.getRxDir().getAbsolutePath();
         boolean isWin = File.separatorChar == WIN_SEP_CHAR;
         if(isWin)
         {
            rootPath = rootPath.toLowerCase();
            tmpPath = tmpPath.toLowerCase();
         }

         if (tmpPath.startsWith(getNormalizedPath(rootPath)))
            path = path.substring(rootPath.length());

         // strip any leading slash
         if (path.startsWith(UNIX_SEP_STRING))
            path = path.substring(UNIX_SEP_STRING.length());

         // remove rx root if supplied
         String rxRoot = PSServer.getRequestRoot() + UNIX_SEP_CHAR;
         // strip leading slash from root
         if (rxRoot.startsWith(UNIX_SEP_STRING))
            rxRoot = rxRoot.substring(UNIX_SEP_STRING.length());

         tmpPath = PSServer.isCaseSensitiveURL() ? path :
            path.toLowerCase();

         if (tmpPath.startsWith(rxRoot))
            path = path.substring(rxRoot.length());
      }

      return path;
   }

   /**
    * Gets the appname from the supplied path
    *
    * @param path The path, may be a URL or just a relative file reference,
    * never <code>null</code> or empty.
    *
    * @return The name of the app, or <code>null</code> if it cannot be
    * determined.
    */
   public static String getAppName(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      String appName = null;

      // see if its a url
      try
      {
         URL url = new URL(path);
         path = url.getFile();
      }
      catch (MalformedURLException e)
      {
      }

      // if now we don't have a path, we're done
      if (path != null && path.trim().length() > 0)
      {
         // convert slashes
         path = getNormalizedPath(path);
         // stip any leading "../" or "/Rhythmyx"
         path = stripPathPrefix(path);
         StringTokenizer toker = new StringTokenizer(path, "/");

         // next should be the appname
         if (toker.hasMoreTokens())
            appName = toker.nextToken();
      }

      return appName;
   }

   /**
    * Normalizes the supplied path to use "/" file separators.
    *
    * @param path The path to normalize, may not be <code>null</code> or empty.
    *
    * @return The normalized path, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>path</code> is
    * <code>null</code> or empty.
    */
   public static String getNormalizedPath(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      return path.replace(WIN_SEP_CHAR, UNIX_SEP_CHAR);
   }


   /**
    * Parse provided url and return map of query params.
    * 
    * @param url The url, may not be <code>null</code>, may be empty.
    * @param base Used to return the base part of the url, ignored if 
    * <code>null</code>, otherwise the base portion of the url is appended onto
    * the buffer.
    * @return The params, key is param name as a <code>String</code>, value is
    * the param value as a <code>String</code>, or a <code>Collection</code>
    * of <code>String</code> values.  Never <code>null</code>, may
    * be empty.
    */
   public static Map parseParams(String url, StringBuffer base)
   {
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      
      HashMap params = new HashMap();
      int start = url.indexOf("?");
      if (start > -1)
      {
         if (base != null)
            base.append(url.substring(0, start));
         
         String paramString = url.substring(start + 1);
         if (paramString.trim().length() > 0)
         {
            try
            {
               PSFormContentParser.parseParameterString(params, paramString);
            }
            catch (PSRequestParsingException e)
            {
               // bad url, not our problem, just return empty map
            }
         }
      }
      return params;
   }


   /**
    * Converts the suppiled parameter to a list of {@link PSParam} objects,
    * handling params with repeated values.
    * 
    * @param entry Map entry where key is the param name as a 
    * <code>String</code>, and value is the value of the param either as a 
    * <code>String</code> or <code>Collection</code> of <code>String</code>
    * objects.  May not be <code>null</code>.
    * 
    * @return A list of {@link PSParam} objects, never <code>null</code>, or
    * emtpy.  Size of list will be equal to the number of values.
    */
   public static List convertToParams(Map.Entry entry)
   {
      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");
      
      List paramList = new ArrayList();
      String paramName = (String)entry.getKey();
      Object val = entry.getValue();
      // handle repeated params
      if (val instanceof Collection)
      {
         Iterator values = ((Collection)val).iterator();
         int index = 0;
         while (values.hasNext())
         {                  
            String tmpName = paramName + "[" + index++ + "]"; 
            paramList.add(new PSParam(tmpName, new PSTextLiteral(
               (String)values.next())));
         }
      }
      else
      {
         paramList.add(new PSParam(paramName, new PSTextLiteral(
            (String)val)));
      }
   
      return paramList;
   }


   /**
    * Converts the supplied parameter map to a list of <code>Map.Entry</code>
    * objects, repeating any params that have multiple values.
    * 
    * @param paramMap The map to convert, may not be <code>null</code>.  
    * Key is the param name as a <code>String</code>, value is the param value 
    * as a <code>String</code> or a <code>Collection</code> of 
    * <code>String</code> objects.
    * 
    * @return An iterator over zero or more <code>Map.Entry</code> objects, 
    * never <code>null</code>.
    */
   public static Iterator convertToEntries(Map paramMap)
   {
      if (paramMap == null)
         throw new IllegalArgumentException("paramMap may not be null");
      
      List entryList = new ArrayList();
      
      Iterator entries = paramMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         if (entry.getValue() instanceof Collection)
         {
            Iterator values = ((Collection)entry.getValue()).iterator();
            while (values.hasNext())
            {
               entryList.add(new PSEntrySet(entry.getKey(), values.next()));
            }
         }
         else
         {
            entryList.add(new PSEntrySet(entry.getKey(), entry.getValue()));
         }
      }
      
      return entryList.iterator();
   }


   /**
    * Separator used in file paths on windows.
    */
   private static final char WIN_SEP_CHAR = '\\';

   /**
    * Separator used in file paths on unix.
    */
   private static final char UNIX_SEP_CHAR = '/';

   /**
    * String form of {@link #UNIX_SEP_CHAR}
    */
   private static final String UNIX_SEP_STRING = UNIX_SEP_CHAR + "";
}
