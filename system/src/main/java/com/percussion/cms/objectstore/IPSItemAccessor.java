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
package com.percussion.cms.objectstore;

/**
 * Provides access interface to the implementing object.  Used by the
 * <code>PSItemDefExtractor</code>.  An implementing class may accept an
 * <code>IPSVisitor</code> object and act upon that object.  This was
 * implemented so that data from the definitions
 * (ie <code>PSContentEditor</code> etc) may be added to objects
 * not in the same package as the extractor and without adding public mutators,
 * that apply to the definition data, to those objects.
 */
public interface IPSItemAccessor
{

   /**
    * Accepts a vistor object.
    *
    * @param visitor the object to accept.
    */
   void accept(IPSVisitor visitor);

   /**
    * Returns a <code>PSItemField</code> by name.  The requested field
    * may not be available, in that case this returns <code>null</code>.
    *
    * @param fieldName - the field name to retrieve, must not be
    * <code>null</code> or empty.
    * @return - the <code>PSItemField</code> may be <code>null</code>.
    */
   public PSItemField getFieldByName(String fieldName);

}
