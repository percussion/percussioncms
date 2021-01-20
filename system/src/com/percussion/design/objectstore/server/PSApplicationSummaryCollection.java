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

package com.percussion.design.objectstore.server;

import com.percussion.design.objectstore.PSApplication;

import java.util.HashMap;
import java.util.Map;

public class PSApplicationSummaryCollection
{
   PSApplicationSummaryCollection()
   {
      m_appSumsById = new HashMap();
      m_appIdsByName = new HashMap();
   }

   /**
    * Create a new collection of application summaries for the given
    * applications. New summaries can be added later, and summaries
    * can also be removed.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * 
    * @param   apps
    * 
    */
   PSApplicationSummaryCollection(PSApplication[] apps)
   {
      this();
      for (int i = 0; i < apps.length; i++)
      {
         PSApplication app = apps[i];
         addSummary(app, false);
      }
   }

   /**
    * Adds a new application summary and, if desired, allocates and
    * returns a new application id for the given application. If
    * you want to refresh an existing summary, pass <CODE>false</CODE>
    * for the <CODE>allocateId</CODE> parameter.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   app The application whose summary you want to add.
    * @param   allocateId If <CODE>true</CODE>, will allocate and
    * return a new, previously unused id for the application (and
    * set the application's id to this newly allocated id). If
    * <CODE>false</CODE>, will use the id in the <CODE>app</CODE>
    * object, possibly replacing a previous summary under that id.
    * 
    * @return   int
    */
   synchronized int addSummary(PSApplication app, boolean allocateId)
   {
      Integer idInt = new Integer(app.getId());
      int id = idInt.intValue();
      if (allocateId)
      {
         id = m_highestKnownId;
         while (true)
         {
            if (id < 1)
               break; // handle rare case of max positive int # of apps

            idInt = new Integer(id);
            Object taken = m_appSumsById.get(idInt);
            if (taken == null)
            {
               break;
            }
            id++;
         }
         app.setId(id);
      }

      if (id > m_highestKnownId)
         m_highestKnownId = id;

      // create a new summary
      PSApplicationSummary sum = new PSApplicationSummary(app);

      // add it to our collection of summaries
      m_appSumsById.put(idInt, sum);
      m_appIdsByName.put(app.getName().toLowerCase(), idInt);

      // return the app id, which may have been allocated by us
      return app.getId();
   }

   /**
    * Gets the summary for the application with the given id.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   appId The application id.
    * 
    * @return   PSApplicationSummary The summary for the application
    * with the given id, or <CODE>null</CODE> if no such summary
    * exists.
    */
   synchronized PSApplicationSummary getSummary(int appId)
   {
      return (PSApplicationSummary)m_appSumsById.get(new Integer(appId));
   }

   /**
    * Gets the summary for the application with the given name.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   appName The application name.
    * 
    * @return   PSApplicationSummary The summary for the application
    * with the given name, or <CODE>null</CODE> if no such summary
    * exists.
    */
   synchronized PSApplicationSummary getSummary(String appName)
   {
      Integer id = (Integer)m_appIdsByName.get(appName.toLowerCase());
      if (id == null)
         return null;
      return (PSApplicationSummary)m_appSumsById.get(id);
   }

   /**
    * Removes the summary for the application with the given id. If
    * no such application exists, nothing happens and no error
    * is reported. After this method returns, the application's id
    * is marked as available and may be allocated for a different
    * application.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   appId The application id.
    * 
    */
   synchronized void removeSummary(int appId)
   {
      PSApplicationSummary sum
         = (PSApplicationSummary)m_appSumsById.remove(new Integer(appId));
      if (sum != null)
         m_appIdsByName.remove(sum.getName().toLowerCase());
   }

   /**
    * Removes the summary for the application with the given name. If
    * no such application exists, nothing happens and no error
    * is reported. After this method returns, the application's id
    * is marked as available and may be allocated for a different
    * application.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   appName The application name.
    * 
    */
   synchronized void removeSummary(String appName)
   {
      Integer id = (Integer)m_appIdsByName.get(appName.toLowerCase());
      if (id != null)
         removeSummary(id.intValue());
   }

   /**
    * Gets an array of application summary objects (a snapshot of
    * the current state of the summary collections at the time of
    * the call). The returned array should be used inside a
    * synchronize block on this summary collection and the call
    * to getSummaries() should be inside this block.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @return   PSApplicationSummary[]
    */
   synchronized PSApplicationSummary[] getSummaries()
   {
      PSApplicationSummary[] sums
         = new PSApplicationSummary[m_appSumsById.size()];

      return (PSApplicationSummary[])m_appSumsById.values().toArray(sums);
   }

   /** a map from Integer (app id) objects to PSApplicationSummary objects */
   private Map m_appSumsById;

   /** a map from String (app name) objects to Integer (app id) objects */
   private Map m_appIdsByName;

   private int m_highestKnownId = 1;
}

