/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.tools.simple;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Program that converts a Retroguard obfuscation exclusion file (rx.rgs) into
 * YGuard xml exclusion entries. 
 */
public class PSRgs2Xml
{

   private static final Logger log = LogManager.getLogger(PSRgs2Xml.class);

   /**
    * Construct a new converter.
    * @param rgsPath path to the rgs file to be converted, cannot be 
    * <code>null</code> or empty and the file must exist.
    * @param xmlPath the path to the YGuard xml file to be used as output.
    * Cannot be <code>null</code> or empty.
    */
   public PSRgs2Xml(String rgsPath, String xmlPath)
   {
      m_rgsPath = rgsPath;
      m_xmlPath = xmlPath;
   }
   
   /**
    * Converts the rgs file to the YGuard xml expose file.
    *
    */
   public void convert()
   {
       BufferedReader in = null;
       BufferedWriter out = null;
       try
       {
          in = new BufferedReader(new FileReader(m_rgsPath));
          out = new BufferedWriter(new FileWriter(m_xmlPath, false));
          String line = null;
          out.write("<expose>\n");
          while((line = in.readLine()) != null)
          {
             out.write(transform(line));   
          }
          out.write("</expose>\n");          
          
       }
       catch(IOException e)
       {
          log.error(PSExceptionUtils.getMessageForLog(e));
          log.debug(PSExceptionUtils.getDebugMessageForLog(e));
       }
       finally
       {
          try
          {          
             if(in != null)
                in.close();
             if(out != null)
                out.close();   
          }
          catch(IOException io)
          {
             log.error(io.getMessage());
             log.debug(io.getMessage(), io);
          }
       }
   }
   
   /**
    * Does the work of transforming an rgs file entry into an YGuard xml
    * entry.
    * @param line one line from the rgs file
    * @return an aml entry as a string.
    */
   private static String transform(String line)
   {
      line = line.trim();
      StringBuilder sb = new StringBuilder();
      
      if(line.length() == 0)
         return "\n";
      if(line.startsWith("#"))
      {
         if(line.length() > 1)
           return "<!-- " + line.substring(1) + " -->\n";
      }
      else
      {
      
      StringTokenizer st = new StringTokenizer(line, " ");
      String[] tokens = new String[st.countTokens() + 1];
      int idx = 0;
      while(st.hasMoreTokens())
         tokens[idx++] = st.nextToken();
         
      String type = tokens[0].substring(1).toLowerCase();
      
      sb.append("<" + type);
      if(type.equals("class"))
      {
        
        String access = (tokens.length > 2) ?
           tokens[2].toLowerCase() :
           "friendly";
        boolean hasWildcard = tokens[1].indexOf("*") != -1;
        sb.append(" methods=\"" + access + "\"");
        sb.append(" fields=\"" + access + "\"");
        if(hasWildcard)
        {
        
           sb.append(">\n");     
           sb.append("\t<patternset>\n");
           sb.append("\t\t<include");
        }
        else
        {
           sb.append("\n      ");
        }
        sb.append(" name=\"");
        sb.append(path2Package(tokens[1]));
        sb.append("\"");
        if(hasWildcard)
        {
           sb.append("/>\n");       
           sb.append("\t</patternset>\n");
           sb.append("</" + type + ">\n");
        }
        else
        {
           sb.append("/>\n");
        }
      }
      else if(type.equals("method"))
      {
         File file = new File(tokens[1]);
         StringTokenizer st2 = new StringTokenizer(tokens[2], "()");

         String returnVal = "";
         String signiture = "";
         while(st2.hasMoreTokens())
         {

            if(st2.countTokens() == 2)
            {
               signiture = st2.nextToken();               
            }
            returnVal = st2.nextToken();
         }
         sb.append(" class=\"");
         sb.append(path2Package(file.getParent()));
         sb.append("\"");
         sb.append("\n        name=\"");
         sb.append(parseTypes(returnVal) + " ");
         sb.append(file.getName() + "(");
         sb.append(parseTypes(signiture) + ")\"");         
         sb.append("/>\n");
      }
      else if(type.equals("field"))
      {
         File file = new File(tokens[1]);
         sb.append(" class=\"");
         sb.append(path2Package(file.getParent()));
         sb.append("\"");
         sb.append("\n       name=\"");
         sb.append(file.getName() + "\"");         
         sb.append("/>\n");
      }
           
      }
      
      return sb.toString();
      
   }
   
   /**
    * Parses out the type Mnemonics and converts them to the java representation
    * used in YGuard.
    * @param s string containing the type mnemonics
    * @return converted string, Never <code>null</code>, may be empty.
    */
   private static String parseTypes(String s)
   {
      if(s == null)
         return "";
      List types = new ArrayList();
      StringBuilder sb = new StringBuilder();
      int pos = 0;
      int arrayDimensions = 0;
      String current = "";
      while(pos < s.length())
      {
         current = s.substring(pos, pos + 1);
         pos++;
         if(current.equals("["))
         {
            arrayDimensions++;
         }
         else if (current.equals("L"))
         {
            int sPos = pos;
            pos = s.indexOf(";", pos) + 1;
            sb.append(path2Package(s.substring(sPos, pos - 1)));
            for(int i = 0; i < arrayDimensions; i++)
               sb.append("[]");
            arrayDimensions = 0;
            types.add(sb.toString());
            sb.setLength(0);   
            
         }
         else
         {
            sb.append((String)ms_types.get(current));
            for(int i = 0; i < arrayDimensions; i++)
               sb.append("[]");
            arrayDimensions = 0;
            types.add(sb.toString());
            sb.setLength(0);              
         }         
      }
      sb.setLength(0);
      Iterator it = types.iterator();
      while(it.hasNext())
      {
         sb.append((String)it.next());
         if(it.hasNext())
            sb.append(",");
      }
      
      return sb.toString();   
   }
   
   /**
    * Returns a package string from a path string
    * @param path the path string to be converted, may be <code>null</code>.
    * @return the converted string, May be <code>null</code>.
    */
   private static String path2Package(String path)
   {
      if(path == null)
         return path;
      StringBuilder sb = new StringBuilder();
      StringTokenizer st = new StringTokenizer(path, "\\/");
      while(st.hasMoreTokens())
      {
        sb.append(st.nextToken());
        if(st.hasMoreTokens())
           sb.append("."); 
      }
      return sb.toString();
   }
   
   
   /**
    * Main method
    * Usage: java PSRgs2Xml [rgsfilePath] [xmlOuputFilePath]
    * @param args
    */
   public static void main(String[] args)
   {
      if(args.length >= 2)
      {
         PSRgs2Xml converter = new PSRgs2Xml(
            args[0],
            args[1]);
         converter.convert(); 
      }
      else
      {
         System.out.println(
           "Usage: java PSRgs2Xml [rgsfilePath] [xmlOuputFilePath]");
      }  
   }
   
   private String m_rgsPath;
   private String m_xmlPath;
   
   private static Map ms_types = new HashMap();
   static
   {
      ms_types.put("B", "byte");
      ms_types.put("C", "char");
      ms_types.put("D", "double");
      ms_types.put("F", "float");
      ms_types.put("I", "int");
      ms_types.put("J", "long");
      ms_types.put("S", "short");
      ms_types.put("V", "void");
      ms_types.put("Z", "boolean");          
   }
 
   

}
