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
package com.percussion.rxfix.dbfixes;

import com.percussion.rxfix.IPSFix;
import com.percussion.rxfix.PSFixResult;
import com.percussion.rxfix.PSFixResult.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that underlies all fixes
 * 
 * @author dougrand
 */
public abstract class PSFixBase implements IPSFix
{
   /**
    * Results for this module
    */
   protected List<PSFixResult> m_results = new ArrayList<PSFixResult>();
   
   /**
    * Log a result
    * @param status the status
    * @param id
    * @param message
    */
   protected void log(Status status, String id, String message)
   {
      if (status.equals(Status.DEBUG)) return;
      m_results.add(new PSFixResult(status, id, getOperation(), message));
   }
   
   /**
    * Log an info result
    * @param id
    * @param message
    */
   protected void logInfo(String id, String message)
   {
      log(Status.INFO, id, message);
   }
   
   /**
    * Log a debug result
    * @param id
    * @param message
    */
   protected void logDebug(String id, String message)
   {
      log(Status.DEBUG, id, message);
   }
   
   /**
    * Log a failure result
    * @param id
    * @param message
    */
   protected void logFailure(String id, String message)
   {
      log(Status.FAILURE, id, message);
   }
   
   /**
    * Log a preview result
    * @param id
    * @param message
    */
   protected void logPreview(String id, String message)
   {
      log(Status.PREVIEW, id, message);
   }
   
   /**
    * Log a warning result
    * @param id
    * @param message
    */
   protected void logWarn(String id, String message)
   {
      log(Status.WARNING, id, message);
   }
   
   /**
    * Log a success result
    * @param id
    * @param message
    */
   protected void logSuccess(String id, String message)
   {
      log(Status.SUCCESS, id, message);
   }

   /**
    * Get the operation that this module performs 
    * @return the operation, never <code>null</code> or empty
    */
   public abstract String getOperation();

   /** (non-Javadoc)
    * @see com.percussion.rxfix.IPSFix#fix(boolean)
    */
   public void fix(@SuppressWarnings("unused") boolean preview) throws Exception
   {
      m_results.clear();
   }
   
   /**
    *  (non-Javadoc)
    * @see com.percussion.rxfix.IPSFix#getResults()
    */
   public List<PSFixResult> getResults()
   {
      return m_results;
   }

   public boolean removeStartupOnSuccess(){
      return false;
   }
}
