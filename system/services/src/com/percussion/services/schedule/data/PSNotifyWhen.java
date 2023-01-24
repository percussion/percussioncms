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
