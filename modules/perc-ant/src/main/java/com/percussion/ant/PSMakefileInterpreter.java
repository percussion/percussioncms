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
package com.percussion.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PSMakefileInterpreter
{
   public PSMakefileInterpreter(File makefile)
      throws IOException
   {
      m_rdr = new PSMakefileReader(makefile);
   }

   public void interpret()
      throws IOException
   {
      interpret(m_rdr);
   }

   public Map getMacros()
   {
      return (Map)m_macros.clone();
   }

   protected void interpret(PSMakefileReader rdr)
      throws IOException
   {
      String line = rdr.readLine();
      while (line != null)
      {
         processLine(line);
         line = rdr.readLine();
      }
   }

   protected void processLine(String line)
   {
      if (line.startsWith("#"))
         return; // skip comments

      // for now, we only support a very simple syntax
      // to support much more, we should use a real parser
      
      boolean shouldAppend = false;
      int eq = line.indexOf('=');
      if (eq > 0)
      {
         // get everything to the left of the eq
         String left = line.substring(0, eq).trim();

         if (left.endsWith("+"))
         {
            shouldAppend = true;
            eq++;
            left = left.substring(0, left.length() - 2).trim();
         }

         // if the left part is a valid identifier, then keep processing
         if (isValidIdentifier(left))
         {
            // get everything to the right of the eq
            String right = "";
            if (eq < line.length() - 1)
            {
               right = line.substring(eq + 1).trim();
            }

            // a heuristic to tell if the right side is ok - won't always work
            if (!right.startsWith("="))
            {
               if (shouldAppend)
               {
                  String str = (String)m_macros.get(left);
                  if (str == null)
                     str = "";
                  left = str + left;
               }
               defineMacro(left, expandMacros(right));
            }
         }
      }
   }

   protected String expandMacros(String line)
   {
      StringBuilder buf = new StringBuilder(line);
      for (int pos = 0; pos < buf.length(); pos++)
      {
         char c = buf.charAt(pos);
         if (c == '$')
         {
            int startVar = pos;
            pos++;
            c = buf.charAt(pos);
            if (c == '(')
            {
               int endVar;
               for (endVar = pos + 1; endVar < buf.length(); endVar++)
               {
                  c = buf.charAt(endVar);
                  if (c == ')')
                     break;
               }

               if (c == ')')
               {
                  String varName = buf.substring(startVar + 2, endVar);
                  String varValue = (String)m_macros.get(varName);
                  if (varValue == null)
                     varValue = "";
                  buf.replace(startVar, endVar + 1, varValue);
                  pos = startVar - 1;
               }
            }
         }
      }

      return buf.toString();
   }

   protected void defineMacro(String name, String value)
   {
      // System.out.println("Define macro " + name + " = " + value + "\n");
      m_macros.put(name, value);
   }

   protected static boolean isValidIdentifier(String str)
   {
      for (int i = 0; i < str.length(); i++)
      {
         char c = str.charAt(i);
         if (!
              (Character.isLetter(c) ||
               Character.isDigit(c)  ||
               c == '_' ))
         {
            return false;
         }
      }
      return true;
   }

   private final HashMap<String, String> m_macros = new HashMap<String, String>();
   private PSMakefileReader m_rdr;
}
