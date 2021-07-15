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
 * objects and return results. It collects valid results as well as errors 
 * produced by single object operations.
 */
public class PSErrorResultsException extends Exception
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 8238544817254544094L;

   /**
    * A map with all successful results collected for the requested operation, 
    * never <code>null</code>, may be empty. Depending on the requested 
    * operation the map value represents the object returned for successful
    * execution.
    */
   private Map<IPSGuid, Object> results = new HashMap<IPSGuid, Object>();
   
   /**
    * A map with all errors collected for the requested operation, never 
    * <code>null</code>, may be empty. Depending on the operation the map
    * value represents the error produced for a failed execcution.
    */
   private Map<IPSGuid, Object> errors = new HashMap<IPSGuid, Object>();
   
   /**
    * A list of ids for all objects, never <code>null</code>, may be empty. The
    * order of the list is undefined
    */
   private List<IPSGuid> ids = new ArrayList<IPSGuid>();
   
   /**
    * Construct a new exception with empty results and errors collections.
    */
   public PSErrorResultsException()
   {
   }

   /**
    * Get the results map.
    * 
    * @return the results map, never <code>null</code>, may be empty.
    */
   public Map<IPSGuid, Object> getResults()
   {
      return results;
   }
   
   /**
    * Get all results as a list.  The order of the list is undefined.
    * 
    * @param ids the ids to get the results for, not <code>null</code>, may
    *    be empty.
    *    
    * @return a list with all result objects, never <code>null</code>, may be 
    *    empty.
    */
   @SuppressWarnings("unchecked")
   public List getResults(List<IPSGuid> ids)
   {
      List resultList = new ArrayList();
      for (IPSGuid id : ids)
      {
         Object result = results.get(id);
         if (result == null)
            throw new IllegalArgumentException("No result found for id: " + 
               id.toString());
         
         resultList.add(result);
      }
      
      return resultList;
   }
   
   /**
    * Add a new result to the results map.
    * 
    * @param id the id of the object for which the supplied result is, not
    *    <code>null</code>.
    * @param result the result object, not <code>null</code>.
    */
   public void addResult(IPSGuid id, Object result)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (result == null)
         throw new IllegalArgumentException("result cannot be null");
      
      ids.add(id);
      results.put(id, result);
   }
   
   /**
    * Remove the result for the supplied id. We ignore cases where the 
    * referenced result does not exist anymore.
    * 
    * @param id the id of the result to be removed, not <code>null</code>.
    */
   public void removeResult(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      if (results.remove(id) != null)
         ids.remove(id);
   }

   /**
    * Get the list of ids for all objects.  The order of the list is undefined.
    * 
    * @return the list of object ids in the order processed, never
    *    <code>null</code>, may be empty.
    */
   public List<IPSGuid> getIds()
   {
      return ids;
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
      if (!(b instanceof PSErrorResultsException))
         return false;
      
      PSErrorResultsException exception = (PSErrorResultsException) b;
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

