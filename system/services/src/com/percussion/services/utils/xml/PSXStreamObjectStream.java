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
package com.percussion.services.utils.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.CGLIBProxyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 
 * An implementation of {@link PSObjectStream} that uses
 * the popular XML Serialization XStream.
 * @author adamgent
 *
 * @param <T>
 */
public class PSXStreamObjectStream<T> extends PSObjectStream<T>
{

   private XStream m_xstream = new XStream();

   /***
    * Initialize the xstream security framework
    */
   private static void initSecurityFramework(XStream stream){
      stream.addPermission(NoTypePermission.NONE);
      stream.addPermission(NullPermission.NULL);
      stream.addPermission(PrimitiveTypePermission.PRIMITIVES);
      stream.addPermission(CGLIBProxyTypePermission.PROXIES);
      stream.allowTypeHierarchy(Collection.class);
      stream.allowTypeHierarchy(Set.class);
      stream.allowTypeHierarchy(List.class);
      stream.allowTypeHierarchy(String.class);
      stream.allowTypesByWildcard(new String[] {
              "com.percussion.**"
      });

      stream.denyTypesByWildcard(new String[]{ "sun.reflect.**", "sun.tracing.**", "com.sun.corba.**" });
      stream.denyTypesByRegExp(new String[]{ ".*\\.ws\\.client\\.sei\\..*", ".*\\$ProxyLazyValue", "com\\.sun\\.jndi\\..*Enumerat(?:ion|tor),.*\\$URLData" });
   }

   public PSXStreamObjectStream() throws IOException
   {
      super();
      initSecurityFramework(m_xstream);

   }
   
   @Override
   protected ObjectOutputStream createObjectOutputStream(Writer writer) throws IOException
   {
      return m_xstream.createObjectOutputStream(writer);
   }

   @Override
   protected ObjectInputStream createObjectInputStream(Reader reader) throws IOException
   {
      return m_xstream.createObjectInputStream(reader);
   }

}
