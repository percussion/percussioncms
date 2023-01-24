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
package com.percussion.utils.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Process command line arguments into a useful form. Arguments that exist but
 * have no value return the string "true", other arguments return their value.
 * Arguments that have no value will return <code>true</code> for the 
 * method {@link #isFlag(String)}
 */
public class PSParseArguments
{
   /**
    * Ctor
    * @param args command line arguments, must not be <code>null</code>
    */
   public PSParseArguments(String args[])
   {
      if (args == null)
      {
         throw new IllegalArgumentException("args must never be null");
      }
      
      for(int i = 0; i < args.length; i++)
      {
         int n = i + 1;
         String arg = args[i];
         String nextarg = null;
         if (n < args.length)
         {
            nextarg = args[n];
         }
         
         if (arg.charAt(0) == '-')
         {
            String argname = arg.length() > 1 ? arg.substring(1) : null;
            if (argname == null)
            {
               throw new IllegalArgumentException("Invalid argument found: " + 
                     arg);
            }
            
            if (nextarg == null || nextarg.charAt(0) == '-')
            {
               m_flagArguments.add(argname);
               m_args.put(argname, "true");
            }
            else
            {
               m_args.put(argname, nextarg);
               i++; // Increment past next argument
            }
         }
         else
         {
            m_rest.add(arg);
         }
      }
      
   }
   
   /**
    * Returns the arguments that were not associated with named flags
    * @return the unassociated args, never <code>null</code> but may be 
    * empty.
    */
   public List getRest()
   {
      return m_rest;
   }   
   
   /**
    * Get the value of the named arg
    * @param name the argument, must never be <code>null</code> or empty
    * @return the value, may be <code>null</code>
    */
   public String getArgument(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name must never be null");
      }
      
      return (String) m_args.get(name);
   }
   
   /**
    * Return if this named argument is a flag
    * @param name the argument, must never be <code>null</code> or empty
    * @return <code>true</code> if the given argument is a flag
    */
   public boolean isFlag(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name must never be null");
      }
      
      return m_flagArguments.contains(name);
   }
   
   /**
    * Holds correspondence between individual argument and its value. 
    * Initialized in the ctor.
    */
   private Map<String,String> m_args = new HashMap<String,String>();
   
   /**
    * Holds the set of arguments that are flag arguments.
    * Initialized in the ctor.
    */
   private Set<String> m_flagArguments = new HashSet<String>();
   
   /**
    * Holds arguments that are not associated with a -argument. Initialized
    * in the ctor.
    */
   private List<String> m_rest = new ArrayList<String>();
}
