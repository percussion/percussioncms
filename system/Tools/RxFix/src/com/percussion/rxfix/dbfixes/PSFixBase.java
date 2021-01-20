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
}
