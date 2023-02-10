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

package com.percussion.design.objectstore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * The IPSDocumentMapping interface must be implemented by any class which
 * will be used as a document (eg, XML) mapping in a PSDataMapping object.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSDocumentMapping
{
   /**
    * Get the fields which must be retrieved from the back-end(s) in
    * order to use this mapping. The field name syntax is
    * <code>field-name</code>.
    *
    * @return     the fields which must be selected from the Xml
    *             document in order to use this mapping
    */
//   public abstract String[] getFieldsForUpdate();

   
   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public abstract void validate(IPSValidationContext cxt) throws PSSystemValidationException;

   /**
    * Abstract toXml function for DocumentMappings
    */
   public abstract Element toXml(Document doc);

   /**
    * Abstract fromXml function for DocumentMappings
    */
   public abstract void fromXml(Element sourceNode, IPSDocument parentDoc, 
                        List parentComponents)
      throws PSUnknownNodeTypeException;
}

