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
package com.percussion.webservices;

import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This exception may be thrown by web services which operate on multiple
 * objects and return no results. It collects valid operations as well as 
 * errors produced by single object operations.
 */
public class PSErrorsException extends RuntimeException
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -1748271945334011728L;

   /**
    * A list of ids of all objects for which the requested operation was 
    * successful, never <code>null</code>, may be empty.
    */
   private List<IPSGuid> results = new ArrayList<IPSGuid>();
   
   /**
    * A map with all errors collected for the requested operation, never 
    * <code>null</code>, may be empty. Depending on the operation the map
    * value represents the error produced for a failed execcution.
    */
   private Map<IPSGuid, Object> errors = new HashMap<IPSGuid, Object>();
   
   /**
    * A list of ids for all objects, never <code>null</code>, may be empty.
    */
   private List<IPSGuid> ids = new ArrayList<IPSGuid>();
   
   /**
    * Construct a new exception with empty results and errors collections.
    */
   public PSErrorsException()
   {
   }

   /**
    * Get the list of ids for all objects.  The order of the ids is undefined.
    * 
    * @return the list of object ids, never <code>null</code>, may be empty.
    */
   public List<IPSGuid> getIds()
   {
      return ids;
   }

   /**
    * Get the list of successful results.  The order of the results is 
    * undefined.
    * 
    * @return a list with successful results, never <code>null</code>, may be 
    *    empty.
    */
   public List<IPSGuid> getResults()
   {
      return results;
   }
   
   /**
    * Add the supplied did to the list of successful results.
    * 
    * @param id the id of the object for which the requested operation
    *    executed successful, not <code>null</code>.
    */
   public void addResult(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      ids.add(id);
      results.add(id);
   }

   /**
    * Get the errors map.
    * 
    * @return the errors map, never <code>null</code>, may be empty.
    */
   public Map<IPSGuid, Object> getErrors()
   {
      return errors;
   }
   
   /**
    * Add a new error to the errors map.
    * 
    * @param id the id of the object that produced the error, not 
    *    <code>null</code>.
    * @param error the error, not <code>null</code>.
    */
   public void addError(IPSGuid id, Object error)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (error == null)
         throw new IllegalArgumentException("error cannot be null");
      
      ids.add(id);
      errors.put(id, error);
   }
   
   /**
    * Does this exception contain errors?
    * 
    * @return <code>true</code> if it does, <code>false</code> otherwise.
    */
   public boolean hasErrors()
   {
      return !errors.isEmpty();
   }

   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSErrorsException))
         return false;
      
      PSErrorsException exception = (PSErrorsException) b;
      EqualsBuilder builder = new EqualsBuilder();
      builder.append(ids, exception.ids);
      builder.append(results, exception.results);
      builder.append(errors, exception.errors);

      return builder.isEquals();
   }

   @Override
   public int hashCode()
   {
      HashCodeBuilder builder = new HashCodeBuilder();
      builder.append(ids);
      builder.append(results);
      builder.append(errors);
      
      return builder.hashCode();
   }
}

