/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.share.dao;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PSJaxbContext
{
   private static final Logger log = LogManager.getLogger(PSJaxbContext.class);

   // singleton pattern: one instance per class.
   private static Map<Class,PSJaxbContext> singletonMap = new ConcurrentHashMap<>();

   private Class clazz;

   // thread-local pattern: one marshaller/unmarshaller instance per thread
   private ThreadLocal<Marshaller> marshallerThreadLocal = new ThreadLocal<>();
   private ThreadLocal<Unmarshaller> unmarshallerThreadLocal = new ThreadLocal<>();

   // The static singleton getter needs to be thread-safe too,
   // so this method is marked as synchronized.
   public static PSJaxbContext get(Class clazz)
   {
      PSJaxbContext jaxb = singletonMap.computeIfAbsent(clazz, k -> new PSJaxbContext(k));
      return jaxb;
   }

   // the constructor needs to be private,
   // because all instances need to be created with the get method.
   private PSJaxbContext(Class clazz)
   {
      this.clazz = clazz;
   }

   public static Marshaller createMarshaller(Class<?> aClass) {
      try {
         Marshaller m = get(aClass).createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
         return m;
      }catch (JAXBException e)
         {
            log.error("FATAL... Unable to create JAXB Marshaller: {}",e.getMessage());
            log.debug(e.getMessage(),e);
            return null;
         }
   }

   public static Unmarshaller createUnmarshaller(Class<?> aClass) {
      try {
         return get(aClass).createUnmarshaller();
      }catch (JAXBException e)
      {
         log.error("FATAL... Unable to create JAXB Unmarshaller: {}",e.getMessage());
         log.debug(e.getMessage(),e);
         return null;
      }
   }

   /**
    * Gets/Creates a marshaller (thread-safe)
    * @throws JAXBException
    */
   public Marshaller createMarshaller() throws JAXBException
   {
      Marshaller m = marshallerThreadLocal.get();
      if (m == null)
      {
         JAXBContext jc = JAXBContext.newInstance(clazz);
         m = jc.createMarshaller();
         marshallerThreadLocal.set(m);
      }
      return m;
   }

   /**
    * Gets/Creates an unmarshaller (thread-safe)
    * @throws JAXBException
    */
   public Unmarshaller createUnmarshaller() throws JAXBException
   {
      Unmarshaller um = unmarshallerThreadLocal.get();
      if (um == null)
      {
         JAXBContext jc = JAXBContext.newInstance(clazz);
         um = jc.createUnmarshaller();
         unmarshallerThreadLocal.set(um);
      }
      return um;
   }
}
