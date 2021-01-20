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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;

import java.util.Set;

import org.w3c.dom.Element;

/**
 * Interface for classes that implement the cataloging of field information
 */
public interface IPSFieldCataloger
{
   /**
    * Get all the system, shared and local fields for all the content editors
    * conforming to the following dtd:
    * 
    * <pre><code>
    *  &lt;!ELEMENT PSXContentEditorFieldCataloger (System?, Shared?, Local?)&gt;
    *  &lt;!ELEMENT System(SearchField+)&gt;
    *  &lt;!ELEMENT Shared(SearchField+)&gt;
    *  &lt;!ELEMENT Local(SearchField+)&gt;
    * 
    *  &lt;!-- The resultOnly flag indicates that this field can only be used
    *     as part of the results, it cannot be used as a search field.
    *     name is the fields submit name. --&gt;
    *  &lt;!ELEMENT SearchField (Field+)&gt;
    *  &lt;!ATTLIST SearchField
    *     name CDATA #REQUIRED
    *     resultOnly (yes|no) &quot;no&quot;
    *     &gt;
    * 
    *  &lt;!-- If contentTypeId is valid, it will be included. If a displayName
    *     is available, it will be included. If choices are supplied, they are
    *     included. DisplayChoices element is defined in the sys_ContentEditor
    *     dtd. --&gt;
    *  &lt;!ELEMENT Field (DisplayChoices?)&gt;
    *  &lt;!ATTLIST Field
    *     contentTypeId CDATA #IMPLIED
    *     displayName CDATA #IMPLIED
    *     datatype CDATA #REQUIRED
    *     &gt;
    * </code></pre>
    * 
    * @param controlFlags An OR'd set of the <code>FLAG_xxx</code> values
    * defined in this interface. 

    * @param fields An optional set of field names to retrieve.  If specified,
    * only fields matching the specified names case-sensitive will be 
    * returned, if <code>null</code> or empty, all fields are returned.
    * 
    * @return An element containing the above format. Never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public Element getCEFieldXml(int controlFlags, Set<String> fields) 
      throws PSCmsException;

   /**
    * Convenience method to get all fields, the equivalent of calling 
    * {@link #getCEFieldXml(int, Set) getCEFieldXml(controlFlags, null)}
    */
   public Element getCEFieldXml(int controlFlags)  throws PSCmsException;
   
   /**
    * If set, all fields will be included, unless further resticted by other
    * flags.
    */
   public static final int FLAG_INCLUDE_ALL = 0x0;

   /**
    * If set, fields not visisble to end users but can be used for search
    * criteria will be included. Basically, this means the field is not
    * read-only and is not user searchable. An example of a hidden field is the
    * system field <code>sys_objecttype</code>
    */
   public static final int FLAG_INCLUDE_HIDDEN = 0x1;

   /**
    * If set, then fields that are considered 'result only' will be included in
    * the returned doc. A result-only field is one whose value can be returned
    * in the result set, but it can't be used as part of the search criteria.
    * Basically, this is a computed field (not stored in db). An example of this
    * is the system field <code>sys_assignees</code>.
    * <p>
    * Binary fields are never included.
    */
   public static final int FLAG_INCLUDE_RESULTONLY = 0x1 << 1;

   /**
    * If set, only fields from content types visible to the user's current
    * community will be included. 
    */
   public static final int FLAG_RESTRICT_TOUSERCOMMUNITY = 0x1 << 2;

   /**
    * If set, only fields that can be included in the results of user 
    * customizable searches are included.
    */
   public static final int FLAG_USER_SEARCH = 0x1 << 3;

   /**
    * If set, then the contenttypes that have hidden from menu values as 1 will
    * be hidden.
    */
   public static final int FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU = 0x1 << 4;
   
   /**
    * If set, field choices will be returned without entries, but will include 
    * the filter, if any.  
    */
   public static final int FLAG_EXCLUDE_CHOICES = 0x1 << 5;
}

