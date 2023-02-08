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
 * A sample address class used in unit test of the
 * {@link com.percussion.xml.serialization.PSObjectSerializer} class. As can be
 * seen it is a simple java bean with a default ctor (required) and setXxx() and
 * getXxx() methods.
 */
public class Address
{
   private String street;

   private String addressLine2;

   private String town;

   private String state;

   private String zip;

   /**
    * Default ctor. Required by serializer.
    */
   public Address()
   {
   }

   /**
    * Ctro taking all the required information to build the object.
    * 
    * @param street
    * @param town
    * @param state
    * @param zip
    */
   public Address(String street, String town, String state, String zip)
   {
      this.street = street;
      this.town = town;
      this.state = state;
      this.zip = zip;
   }

   public void setStreet(String addressLine1)
   {
      this.street = addressLine1;
   }

   public void setAddressLine2(String addressLine2)
   {
      this.addressLine2 = addressLine2;
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public String getTown()
   {
      return town;
   }

   public void setTown(String town)
   {
      this.town = town;
   }

   public String getZip()
   {
      return zip;
   }

   public void setZip(String zip)
   {
      this.zip = zip;
   }

   public String getStreet()
   {
      return street;
   }

   public String getAddressLine2()
   {
      return addressLine2;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Address)) return false;
      Address address = (Address) o;
      return Objects.equals(getStreet(), address.getStreet()) && Objects.equals(getAddressLine2(), address.getAddressLine2()) && Objects.equals(getTown(), address.getTown()) && Objects.equals(getState(), address.getState()) && Objects.equals(getZip(), address.getZip());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getStreet(), getAddressLine2(), getTown(), getState(), getZip());
   }
}
