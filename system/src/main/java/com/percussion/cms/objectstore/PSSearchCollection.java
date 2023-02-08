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

import java.util.Iterator;


/**
 * This class overrides collection to enforce that none of the members of
 * the collection can have the same internal name (case-insensitive).
 */
public class PSSearchCollection extends PSDbComponentCollection
{
   /**
    * ctor calls base class.
    */
   public PSSearchCollection()
      throws ClassNotFoundException
   {
      super(PSSearch.class.getName());
   }


   /**
    * Convenience method that calls {@link #add(PSSearch)}.
    *
    * @param comp The datatype must be PSSearch. Never <code>null</code>.
    */
   public void add(IPSDbComponent comp)
   {
      if (null == comp)
         throw new IllegalArgumentException("Null not allowed.");
      if (!(comp instanceof PSSearch))
         throw new IllegalArgumentException("Only PSSearch objects allowed.");

      add((PSSearch) comp);
   }

   /**
    * Adds the supplied component to this collection, checking first that a
    * search with the same internal name is not already present. If the
    * supplied search is already present (using the == method), then nothing
    * is done. If a search with the same internal name, but different
    * instance, the current search is added to the delete list and the supplied
    * search replaces the existing one.
    *
    * @param search Never <code>null</code>.
    */
   public void add(PSSearch search)
   {
      PSSearch s = getSearchObject(search.getInternalName());
      if (s == null)
         super.add(search);
      else if (s != search)
      {
         super.remove(s);
         super.add(search);
      }
   }


   /**
    * Convenience method that calls {@link #contains(PSSearch)}.
    *
    * @param comp The datatype must be a PSSearch. If <code>null</code>,
    *    <code>false</code> is returned.
    */
   public boolean contains(IPSDbComponent comp)
   {
      if (null == comp)
         return false;
      if (!(comp instanceof PSSearch))
         throw new IllegalArgumentException("Only PSSearch objects allowed.");

      return contains((PSSearch) comp);
   }

   /**
    * Scans the entire list looking for an entry that matches the supplied
    * search by internal name. For any entry, e, if
    * e.getInternalName().equalsIgnoreCase(search.getInternalName()) is
    * <code>true</code>, <code>true</code> is returned.
    *
    * @param search If <code>null</code>, <code>false</code> is returned.
    *
    * @return <code>true</code> if the internal name of search matches any
    *    entry in this list, case-insensitive, <code>false</code> otherwise.
    */
   public boolean contains(PSSearch search)
   {
      return getSearchObject(search.getInternalName()) != null;
   }

   /**
    * Scans the collection looking for a search that whose internal name
    * matches the supplied one (case-insensitive).
    *
    * @param internalName Assumed not <code>null</code>.
    *
    * @return The search object in this collection that has the supplied name,
    *    or <code>null</code>, if one is not found.
    */
   private PSSearch getSearchObject(String internalName)
   {
      Iterator iter = iterator();

      while (iter.hasNext())
      {
         PSSearch s = (PSSearch) iter.next();

         if (s.getInternalName().equalsIgnoreCase(internalName))
            return s;
      }

      return null;
   }
}
