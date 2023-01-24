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

package com.percussion.share.dao;


import com.percussion.error.PSExceptionUtils;
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
            log.error("FATAL... Unable to create JAXB Marshaller: {}",
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            return null;
         }
   }

   public static Unmarshaller createUnmarshaller(Class<?> aClass) {
      try {
         return get(aClass).createUnmarshaller();
      }catch (JAXBException e)
      {
         log.error("FATAL... Unable to create JAXB Unmarshaller: {}",
                 PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
