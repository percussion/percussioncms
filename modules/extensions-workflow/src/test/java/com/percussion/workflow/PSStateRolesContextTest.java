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
package com.percussion.workflow;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The PSStateRolesContextTest class is a test class for the class
 * StateRolesContext.
 */
@Category(IntegrationTest.class)
public class PSStateRolesContextTest extends PSAbstractWorkflowTest 
{

   public PSStateRolesContextTest (String[] args)
   {
      
   }
   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      System.out.println("Entering Method ExecuteTest");
      Exception except = null;
      String exceptionMessage = "";
      PSStateRolesContext context = null;
      int workflowID = 1;
      int stateID = 3;
      int assignmentType = 0;
      
      try
      {
         context = new PSStateRolesContext(workflowID,
                                           connection,
                                           stateID,
                                           assignmentType);
         System.out.println("context = " + context);
      }
      catch (SQLException e) 
      {
         exceptionMessage = "SQL exception: ";
         except = e;
      }
      catch (PSRoleException e) 
      {
         exceptionMessage = "Role exception";
         except = e;
      }
      catch(PSEntryNotFoundException e)
      {
         System.out.println("State roles context not found.");
         except = e;
      }
      finally 
      {
         System.out.println("Exiting Method ExecuteTest");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }
   public static void main(String[] args)
   {
      PSStateRolesContextTest wfTest = new PSStateRolesContextTest(args);
      wfTest.Test();
   }
}
