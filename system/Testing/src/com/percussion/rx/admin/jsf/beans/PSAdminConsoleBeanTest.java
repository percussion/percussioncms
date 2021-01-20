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
package com.percussion.rx.admin.jsf.beans;

import static com.percussion.rx.admin.jsf.beans.PSAdminConsoleBean.EXIT_CMD;
import static com.percussion.rx.admin.jsf.beans.PSAdminConsoleBean.QUIT_CMD;
import static com.percussion.rx.admin.jsf.beans.PSAdminConsoleBean.STOP_SERVER_CMD;

import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSAdminConsoleBeanTest extends TestCase
{
   public void testGetCommand()
   {
      final PSAdminConsoleBean bean = new PSAdminConsoleBean("a", "b");
      assertNull(bean.getCommand());
      
      bean.setCommand(STOP_SERVER_CMD);
      assertEquals(STOP_SERVER_CMD, bean.getCommand());
      
      bean.setCommand(QUIT_CMD);
      assertEquals(STOP_SERVER_CMD, bean.getCommand());
      
      bean.setCommand(EXIT_CMD);
      assertEquals(STOP_SERVER_CMD, bean.getCommand());

      bean.setCommand(QUIT_CMD.toUpperCase());
      assertEquals(STOP_SERVER_CMD, bean.getCommand());

      bean.setCommand(CMD.toUpperCase());
      assertFalse(CMD.toLowerCase().equals(bean.getCommand()));
   }
   
   public void testSetResult()
   {
      final PSAdminConsoleBean bean = new PSAdminConsoleBean("a", "b");
      // nothing happens
      bean.setResult(null);
      bean.setResult("abc");
   }

   /**
    * A sample command.
    */
   private static final String CMD = "Sample CoMMand";
}
