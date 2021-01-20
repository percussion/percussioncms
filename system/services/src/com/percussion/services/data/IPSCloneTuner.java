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
package com.percussion.services.data;

/**
 * This interface defines one method that makes the colne a valid object that
 * can be persisted. Every object that supports clone (or copy construction)
 * must implement this.
 */
public interface IPSCloneTuner
{
   /**
    * Tune the self and return so that the returned object can be persisted.
    * Typically adjusts the id and and may additional changes to the object to
    * make it persistable. Please note that this does not clone itself and it
    * assumes it is already clone of a persisted object and makes changes to
    * self and returns.
    * 
    * @param newId new id for the object. An exact clone will have same id as
    * the its clone parent that must be changed or reset before persisting. This
    * id will be set on the self.
    * @return the tuned version of the self object that can be persisted. Never
    * <code>null</code>.
    */
   Object tuneClone(long newId);
}
