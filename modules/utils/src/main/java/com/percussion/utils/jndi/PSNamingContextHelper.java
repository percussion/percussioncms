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
package com.percussion.utils.jndi;

import org.apache.commons.lang.StringUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Iterator;
import java.util.Map;

/**
 * Takes a set of property values and binds them into the jndi context to allow
 * later lookups to succeed. Only used for local testing, on a server the "real"
 * bindings are used.
 * 
 * @author dougrand
 */
public class PSNamingContextHelper
{
   /**
    * The actual naming context to use. This is setup to use the mock naming
    * provider.
    */
   Context m_ctx = null;

   /**
    * The initial bindings to set, never used afterward.
    */
   Map m_bindings = null;

   /**
    * The root jndi path
    */
   String m_root = null;

   /**
    * @throws NamingException
    * 
    */
   public PSNamingContextHelper() throws NamingException {
      m_ctx = new InitialContext();
   }

   /**
    * @return Returns the props.
    */
   public Map getBindings()
   {
      return m_bindings;
   }

   /**
    * @param props The props to set, never <code>null</code>
    * @throws NamingException If there is a problem storing a name/value
    */
   @SuppressWarnings(value = "unchecked")
   public void setBindings(Map props) throws NamingException
   {
      if (props == null)
      {
         throw new IllegalArgumentException("props may not be null");
      }
      m_bindings = props;
      Iterator<Map.Entry> iter = m_bindings.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry entry = iter.next();
         m_ctx.bind(m_root + (String) entry.getKey(), entry.getValue());
      }
   }

   /**
    * Add a single additional binding to the mock jndi bindings
    * 
    * @param key the name of the binding, the root will be prepended, never
    *           <code>null</code> or empty.
    * @param binding the bound data, never <code>null</code>.
    * @throws NamingException
    */
   public void addBinding(String key, Object binding) throws NamingException
   {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      if (binding == null)
      {
         throw new IllegalArgumentException("binding may not be null");
      }
      m_ctx.bind(m_root + key, binding);
   }

   /**
    * Add a single additional binding to the mock jndi bindings
    * 
    * @param key the name of the binding, never <code>null</code> or empty.
    * @param binding the bound data, never <code>null</code>.
    * @throws NamingException
    */
   public void addBareBinding(String key, Object binding)
         throws NamingException
   {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      if (binding == null)
      {
         throw new IllegalArgumentException("binding may not be null");
      }
      m_ctx.bind(key, binding);
   }

   /**
    * @return Returns the root.
    */
   public String getRoot()
   {
      return m_root;
   }

   /**
    * @param root The root to set.
    */
   public void setRoot(String root)
   {
      if (!root.endsWith("/") && !root.isEmpty())
      {
         root = root + "/";
      }
      m_root = root;
   }
}
