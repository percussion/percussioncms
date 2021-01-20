/*[ EmpireLockerId.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire;

import com.percussion.autotest.framework.QARequestContext;
import java.util.Properties;

class EmpireLockerId
{
   public EmpireLockerId(Properties props)
   {
      readFrom(props);
   }

   public EmpireLockerId(QARequestContext context)
   {
      m_clientURL = context.getClientURL();
      m_scriptName = context.getScriptName();
   }

   public void readFrom(Properties props)
   {
      m_clientURL = props.getProperty("qaClient");
      m_scriptName = props.getProperty("qaScript");
   }

   public void writeTo(Properties props)
   {
      if (m_scriptName != null)
         props.setProperty("qaScript", m_scriptName);

      if (m_clientURL != null)
         props.setProperty("qaClient", m_clientURL);
   }

   public boolean sameId(EmpireLockerId other)
   {
      boolean sameClient = (m_clientURL != null && other.m_clientURL != null
         && m_clientURL.equals(other.m_clientURL));
   
      boolean sameScript = (m_scriptName != null && other.m_scriptName != null
         && m_scriptName.equals(other.m_scriptName));

      return (sameClient && sameScript);
   }

   private String m_scriptName;
   private String m_clientURL;
}

