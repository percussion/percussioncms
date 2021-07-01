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
package com.percussion.services.utils.hibernate;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.type.Type;

/**
 * Handle update events that should notify the memory subsystem to evict
 * in-memory cached objects.
 * 
 * @author dougrand
 * 
 */
public class PSHibernateInterceptor extends EmptyInterceptor
{

   /**
    * Serialization global id
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSHibernateInterceptor.class);

   /**
    * Initialized in ctor, <code>true</code> if loads should be reported
    */
   private boolean m_reportLoad;

   /**
    * Initialized in ctor, <code>true</code> if saves should be reported
    */
   boolean m_reportSave;

   /**
    * Initialized in ctor, <code>true</code> if deletes should be reported
    */
   boolean m_reportDelete;

   /**
    * When a transaction starts, a set is pushed on the stack. The methods such
    * as <code>onSave</code> add guids to this set. When the transation
    * finishes, it is popped off the stack and notifications are sent for each
    * noted changes. A stack is required because transactions can be nested. We
    * use thread local storage because transactions are bound to threads.
    */
   private static ThreadLocal<Stack<Set<IPSGuid>>> ms_pendingChanges = 
      new ThreadLocal<>();

   /**
    * Empty array of classes for method lookup
    */
   private final Class[] NO_PARAMETERS = new Class[]
   {};

   /**
    * Empty array of objects for method invocation
    */
   private final Object[] NO_ARGS = new Object[]
   {};

   /**
    * Ctor
    * 
    * @param eventsString the events configured, may be <code>null</code> or
    *           empty
    */
   public PSHibernateInterceptor(List<String> eventsString) {
      if (eventsString != null && eventsString.size() > 0)
      {
         m_reportSave = eventsString.contains("persist");
         m_reportDelete = eventsString.contains("delete");
         m_reportLoad = eventsString.contains("load");
      }
   }

   @Override
   public void onDelete(Object arg0, 
         @SuppressWarnings("unused") Serializable arg1, 
         @SuppressWarnings("unused") Object[] arg2,
         @SuppressWarnings("unused") String[] arg3, 
         @SuppressWarnings("unused") Type[] arg4)
   {
      if (m_reportDelete)
      {
         reportEvent("delete", arg0.getClass().getName());
      }

      IPSGuid guid = getGuidFromObject(arg0);
      if (guid != null)
      {
         addGuid(guid);
      }
   }

   /**
    * Add a guid to the notification list
    * 
    * @param guid guid, assumed never <code>null</code>
    */
   private void addGuid(IPSGuid guid)
   {
      assert (!ms_pendingChanges.get().isEmpty());
      Set<IPSGuid> current = ms_pendingChanges.get().peek();
      current.add(guid);
   }

   @Override
   public boolean onSave(Object arg0, 
         @SuppressWarnings("unused") Serializable arg1, 
         @SuppressWarnings("unused") Object[] arg2,
         @SuppressWarnings("unused") String[] arg3, 
         @SuppressWarnings("unused") Type[] arg4)
   {
      if (m_reportSave)
      {
         reportEvent("save", arg0.getClass().getName());
      }

      IPSGuid guid = getGuidFromObject(arg0);
      if (guid != null)
      {
         addGuid(guid);
      }
      return false;
   }

   @Override
   public boolean onFlushDirty(Object arg0, 
         @SuppressWarnings("unused") Serializable arg1, 
         @SuppressWarnings("unused") Object[] arg2,
         @SuppressWarnings("unused") Object[] arg3, 
         @SuppressWarnings("unused") String[] arg4, 
         @SuppressWarnings("unused") Type[] arg5)
   {
      if (m_reportSave)
      {
         reportEvent("flush", arg0.getClass().getName());
      }

      IPSGuid guid = getGuidFromObject(arg0);
      if (guid != null)
      {
         addGuid(guid);
      }
      return false;
   }

   @Override
   public boolean onLoad(Object arg0, 
         @SuppressWarnings("unused") Serializable arg1, 
         @SuppressWarnings("unused") Object[] arg2,
         @SuppressWarnings("unused") String[] arg3, 
         @SuppressWarnings("unused") Type[] arg4)
   {
      if (m_reportLoad)
      {
         reportEvent("load", arg0.getClass().getName());
      }
      return false;
   }

   @Override
   public void afterTransactionBegin(Transaction tx)
   {
      super.afterTransactionBegin(tx);
      Stack<Set<IPSGuid>> stack = ms_pendingChanges.get();
      if (stack == null)
      {
         stack = new Stack<>();
         ms_pendingChanges.set(stack);
      }
      stack.push(new HashSet<>());
   }

   @Override
   public void afterTransactionCompletion(Transaction tx)
   {
      //need to clear the set whether success or rollback
      Set<IPSGuid> current = ms_pendingChanges.get().pop();

      if (tx.getStatus() == TransactionStatus.COMMITTED)
      {
         for (IPSGuid guid : current)
         {
            PSNotificationHelper.notifyInvalidation(guid);
         }
      }
   }

   /**
    * Report event
    * 
    * @param string name of event
    * @param entityName entity name
    */
   private void reportEvent(String string, String entityName)
   {
      Exception e = new Exception();
      e.fillInStackTrace();

      // Walk the stack trace to find the first percussion class references
      StackTraceElement elements[] = e.getStackTrace();
      StackTraceElement percel = null;
      int skip = 3;
      for (StackTraceElement el : elements)
      {
         if (skip > 0)
         {
            skip--;
            continue;
         }
         if (el.getClassName().startsWith("com.percussion."))
         {
            percel = el;
            break;
         }
      }

      String className = percel.getClassName();
      String methodName = "";
      if(percel.getMethodName() !=null)
         methodName = percel.getMethodName() ;
      
      String lineNumber = "";
      if(percel!=null)
         lineNumber = String.valueOf(percel.getLineNumber());
      
      ms_log.info("Hibernate event "
            + string
            + " on entity "
            + entityName
            + " called from "
            + (percel != null ? percel.getClassName() : ":"
                  + methodName + " at line "
                  + lineNumber));
   }

   /**
    * Get the guid from the object, if the object has a <code>getGUID</code>
    * method.
    * 
    * @param ob the object, assumed never <code>null</code>
    * @return the guid of the object, or <code>null</code> if the object
    *         doesn't implement a <code>getGUID</code> method.
    */
   private IPSGuid getGuidFromObject(Object ob)
   {
      IPSGuid guid = null;

      if (ob instanceof PSAclEntryImpl)
      {
         // TODO: remove when we stop resource caching using community views
         PSAclEntryImpl aclEntry = (PSAclEntryImpl) ob;
         return new PSGuid(PSTypeEnum.ACL, aclEntry.getAclId());
      }
      else if (ob instanceof PSAccessLevelImpl)
      {
         // TODO: remove when we stop resource caching using community views
         
         // make up an acl since we'll just flush all of them any how
         return new PSGuid(PSTypeEnum.ACL, 0);
      }
      
      try
      {
         Method gguid = ob.getClass().getMethod("getGUID", NO_PARAMETERS);
         guid = (IPSGuid) gguid.invoke(ob, NO_ARGS);
      }
      catch (NoSuchMethodException nme)
      {
         // Ignore
      }
      catch (IllegalArgumentException e)
      {
         ms_log
               .error("Problem retrieving guid from object " + ob.toString(), e);
      }
      catch (IllegalAccessException e)
      {
         ms_log
               .error("Problem retrieving guid from object " + ob.toString(), e);
      }
      catch (InvocationTargetException e)
      {
         ms_log.error("Problem retrieving guid from object " + ob.toString(), e
               .getTargetException());
      }

      return guid;
   }

}
