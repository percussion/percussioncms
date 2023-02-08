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
package com.percussion.xml.serialization.junit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * A sample Name class used in unit test of the
 * {@link com.percussion.xml.serialization.PSObjectSerializer} class. As can be
 * seen it is a simple java bean with a default ctor (required) and setXxx() and
 * getXxx() methods.
 */
public class Name
{
   String first;

   String last;

   /**
    * Default ctor. Required by serializer.
    */
   public Name()
   {
   }

   public Name(String first, String last)
   {
      this.first = first;
      this.last = last;
   }

   public String getFirst()
   {
      return first;
   }

   public void setFirst(String first)
   {
      this.first = first;
   }

   public String getLast()
   {
      return last;
   }

   public void setLast(String last)
   {
      this.last = last;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Name)) return false;
      Name name = (Name) o;
      return Objects.equals(getFirst(), name.getFirst()) && Objects.equals(getLast(), name.getLast());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getFirst(), getLast());
   }
}
