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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
