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
