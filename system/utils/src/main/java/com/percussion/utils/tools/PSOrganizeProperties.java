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
package com.percussion.utils.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Utility class used to organize a property file into ascending order
 */
public class PSOrganizeProperties
{

   public static final String HEADER_COMMENTS = "HEADER_COMMENTS";

   /**
    * Performs the actual sort
    * @param file, cannot be <code>null</code>.
    * @throws IOException upon any error
    */
   public static void execute(File file) throws IOException
   {
      Map<String, List<String>> m_comments =
              new HashMap<>();

      Properties props = loadPropsFile(file, m_comments);
      Map<String, String> propsMap = new TreeMap<>(new Comparator<String>() {
         /**
          * Compare case-insensitive.
          */
         public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
         }
      });
      
      Iterator it = props.keySet().iterator();
      while(it.hasNext())
      {
         String key = (String)it.next();
         propsMap.put(key, props.getProperty(key, ""));
      }
      savePropsFile(file, propsMap, m_comments);
   }
    
   
   /**
    * Load the specified properties file
    * @param file cannot be <code>null</code>.
    * @param m_comments
    * @return the Properties, never <code>null</code>.
    * @throws IOException upon error
    */
   protected static Properties loadPropsFile(File file, Map<String, List<String>> comments_list) throws IOException
   {
      if(file == null)
         throw new IllegalArgumentException("file cannot be null.");
      if(!file.exists() || !file.isFile())
         throw new IllegalArgumentException(
            "File does not exist: " + file.getAbsolutePath());
      Properties props = new Properties();

      String headerComments = getHeaderComments(file);
      parseComments(file, comments_list);
      InputStream is = new FileInputStream(file);
      try
      {
         props.load(is);
         props.setProperty(HEADER_COMMENTS,headerComments);
         return props;
      }
      finally
      {
         if(is != null)
         {
            try
            {
               is.close();
            }
            catch(IOException ignore){}
         }
      }
      
   }
   
   /**
    * Parses the header comment from the properties file and
    * stores it for later use.
    * @param file
    * @throws IOException
    */
   private static String getHeaderComments(File file) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try
      {
         String line = null;
         StringBuilder sb = new StringBuilder();
         while((line = reader.readLine()) != null)
         {
            String trimmedLine = line.trim();            
            if(trimmedLine.startsWith("#") || trimmedLine.length() == 0)
            {
               if(trimmedLine.startsWith("#### "))
                  break;
               sb.append(line);
               sb.append("\n");
            }
            else
            {               
               break;
            }
         }
         return sb.toString();
      }
      finally
      {
         try
         {
            if(reader != null)
               reader.close();
         }
         catch(IOException ignore){}
      }
   }
   
   /**
    * Parses out all comments except the header comments and adds them
    * to the comments map using the following entry as the key.
    * @param file
    * @param headerComments
    * @param comments_list
    * @throws IOException
    */
   private static void parseComments(File file, Map<String, List<String>> comments_list)  throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try
      {
         String line = null;
         boolean okToParse = false;
         List<String> comments = new ArrayList<String>();
         while((line = reader.readLine()) != null)
         {
            String trimmedLine = line.trim();            
            if(!okToParse)
            {
               if(trimmedLine.startsWith("#") || trimmedLine.length() == 0)
               {
                  if(trimmedLine.startsWith("#### "))
                     okToParse = true; 
               }
               else                              
                  okToParse = true;               
            }
            else
            {
               if(trimmedLine.startsWith("#") || trimmedLine.length() == 0)
               {
                  if(trimmedLine.startsWith("#### "))
                     comments.clear(); // dump collected comments
                  else
                     comments.add(trimmedLine);
               }
               else if(trimmedLine.indexOf('=') != -1 && !comments.isEmpty())
               {
                  String[] splitLine = trimmedLine.split("=");
                  comments_list.put(splitLine[0], comments);
                  comments = new ArrayList();
               }
            }
         }
         
      }
      finally
      {
         try
         {
            if(reader != null)
               reader.close();
         }
         catch(IOException ignore){}
      }
   }
   
   /**
    * Formats and saves the properties back to the specified file
    * @param file assumed not <code>null</code>.
    * @param props the sorted properties to be saved.
    * @param comments_list
    * @throws IOException upon any error.
    */
   private static void savePropsFile(File file, Map<String, String> props, Map<String, List<String>> comments_list)
      throws IOException
   {
      String headerComments = props.get(HEADER_COMMENTS);
      StringBuilder sb = new StringBuilder();

      if(headerComments != null && headerComments.length() > 0)
      {
         sb.append(headerComments);
         props.remove(HEADER_COMMENTS);
      }

      // iterator must be created after props remove the header_commonets
      // to avoid concurrentmodificationexception.
      Iterator it = props.keySet().iterator();


      String lastPrefix = "";
      while(it.hasNext())
      {
         String key = (String)it.next();
         String val = escapeValue(props.get(key));
         int pos = key.indexOf('.');
         String prefix = pos == -1 ? key : key.substring(0, key.indexOf('.'));
         if(!lastPrefix.equals(prefix))
         {
            if(lastPrefix.length() > 0)
               sb.append("\n");
            sb.append("#### ");
            sb.append(prefix);
            sb.append(" ####\n");
            lastPrefix = prefix;
         }
         String escapedKey = escapeValue(key);
         addComments(sb, escapedKey, comments_list);
         sb.append(escapedKey);
         sb.append("=");
         sb.append(val);
         sb.append("\n");
         
      }
      FileOutputStream fo = new FileOutputStream(file);
      try
      {
         fo.write(sb.toString().getBytes("utf8"));
      }
      finally
      {
         if(fo != null)
         {
            try
            {
               fo.close();
            }
            catch(IOException ignore){}
         }
      }
      
   }
   
   /**
    * Adds all the comments for the specified key
    * @param sb
    * @param key
    */
   private static void addComments(StringBuilder sb, String key, Map<String, List<String>> comments)
   {
      if(comments.containsKey(key))
      {
         for(String comment : comments.get(key))
         {
            sb.append(comment);
            sb.append("\n");
         }
      }
   }
   
   /**
    * Handles escaping the property value to remain ISO 8859-1
    * compatible
    * @param value the value, assumed not <code>null</code>
    * @return the escaped value, never <code>null</code>.
    */
   private static String escapeValue(String value)
   {
      StringBuilder sb = new StringBuilder();
      StringReader reader = new StringReader(value);
      int c = -1;
      try
      {
         while((c = reader.read()) != -1)
         {
            char pos = (char)c;
            if(pos == '\\')
            {
              sb.append("\\\\");
            }
            else if(pos == '\b')
            {
               sb.append("\\b");
            }
            else if(pos == '\f')
            {
               sb.append("\\f");
            }
            else if(pos == '\n')
            {
               sb.append("\\n");
            }
            else if(pos == '\r')
            {
               sb.append("\\r");
            }
            else if(pos == '\t')
            {
               sb.append("\\t");
            }
            else if(pos == '"')
            {
               sb.append("\\\"");
            }            
            else if(pos == '#')
            {
               sb.append("\\#");
            }
            else if(pos == ':')
            {
               sb.append("\\:");
            }
            else if((pos >= 32 && pos <= 126))
            {
               sb.append(pos);
            }
            else
            {
               sb.append("\\u00");
               sb.append(Integer.toHexString(pos));
            }
         }
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return sb.toString();
   }
   
   /**
    * The main method for this class.
    * <p>
    * <pre> 
    * Arguments:
    * [0] = The file path of the properties file
    * </pre>
    * </p>
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception
   {
      if(args.length > 0)
      {
         PSOrganizeProperties op = new PSOrganizeProperties();
         File file = new File(args[0]);
         if(!file.getName().toLowerCase().trim().endsWith(".properties"))
         {
            System.out.println("Not a properties file. Nothing to do.");
         }
         else
         {
            op.execute(file);
            System.out.println("Properties file has been organized.");
         }      
      }
   }



   
   
}
