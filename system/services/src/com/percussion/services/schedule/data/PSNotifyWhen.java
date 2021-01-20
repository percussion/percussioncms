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
package com.percussion.services.schedule.data;

import static com.percussion.utils.string.PSStringUtils.notBlank;

/**
 * Dictates when a notification email is sent to the role and cc list.
 *
 * @author Andriy Palamarchuk
 */
public enum PSNotifyWhen
{
   /**
    * Notify on each run regardless of success or failure.
    */
   ALWAYS("Always"),

   /**
    * Notify only when the task fails.
    */
   FAILURE("On Failure"),

   /**
    * Never notify.
    */
   NEVER("Never");

   /**
    * Creates new enumeration value.
    * @param label the value returned by {@link #getLabel()}.
    * Not blank.
    */
   private PSNotifyWhen(String label)
   {
      notBlank(label);
      m_label = label;
   }

   /**
    * A human-readable label for the enumeration value.
    * Is used by the UI.
    * @return the human-readable 
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * @see #getLabel()
    */
   private final String m_label; 
}
