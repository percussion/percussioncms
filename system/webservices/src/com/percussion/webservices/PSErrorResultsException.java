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
package com.percussion.webservices;

import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
   private Map<IPSGuid, Object> results = new HashMap<>();
   
   /**
    * A map with all errors collected for the requested operation, never 
    * <code>null</code>, may be empty. Depending on the operation the map
    * value represents the error produced for a failed execcution.
    */
   private Map<IPSGuid, Object> errors = new HashMap<>();
   
   /**
    * A list of ids for all objects, never <code>null</code>, may be empty. The
    * order of the list is undefined
    */
   private List<IPSGuid> ids = new ArrayList<>();
   
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
      List resultList = new ArrayList<>();
      for (IPSGuid id : ids)
      {
         Object result = results.get(id);
         if (result != null)
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

   /**
    * Gets the list of id's with error in a single string form
    * @return a comma seperated list of guids in xx-xx-xxx form.
    */
   public String getAllErrorIdsString(){
      StringBuilder ret = new StringBuilder();
      for(Map.Entry<IPSGuid, Object> entry : errors.entrySet()){
         if(ret.length()==0)
            ret.append(entry.getKey().toString());
         else
            ret.append(", ").append(entry.getKey());
      }
      return ret.toString();
   }

   /**
    * Gets the list of id's with error in a single string form
    * @return a comma seperated list of guids in xx-xx-xxx form.
    */
   public String getAllErrorString(){
      StringBuilder ret = new StringBuilder();
      for(Map.Entry<IPSGuid, Object> entry : errors.entrySet()){
         if(ret.length()==0) {
            if(entry.getValue() instanceof Throwable)
               ret.append(((Throwable) entry.getValue()).getMessage());
            else
               ret.append(Objects.toString(entry.getValue(),""));
         }
         else {
            if(entry.getValue() instanceof Throwable)
               ret.append(", ").append(((Throwable) entry.getValue()).getMessage());
            else
               ret.append(", ").append(Objects.toString(entry.getValue(),""));
         }
      }
      return ret.toString();
   }



}

