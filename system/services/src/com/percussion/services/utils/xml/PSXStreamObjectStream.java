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
