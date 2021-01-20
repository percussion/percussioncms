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

package com.percussion.ant.install;

import com.percussion.install.Code;
import com.percussion.install.InstallUtil;

import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * This is a condition to determine if a Rhythmyx component is licensed or not.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the typedef:
 * 
 *  <code>  
 *  &lt;typedef name="licenseProdCondition"
 *              class="com.percussion.ant.instlal.PSLicenseProdCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task.
 * 
 *  <code>
 *  &lt;condition property="COMPONENT5_LICENSE"&gt;
 *     &lt;licenseProdCondition componentId="5"/&gt;
 *  &lt;/condition&gt;
 *  </code>
 * 
 * </pre>
 * 
 */
public class PSLicenseProdCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      Code code = InstallUtil.fetchBrandCode(getRootDir());
      return (code == null) ? false : code.isComponentLicensed(componentId);
   }
   
   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/
   
   /**
    * Returns the id of the component whose license is to be verified. This id
    * uniquely identifies a component. See the ComponentMap.xml file to
    * obtain the name of the component corresponding to this id.
    *
    * @return the id of the component whose license is to be verified.
    */
   public int getComponentId()
   {
      return componentId;
   }
   
   /**
    *  Sets the id of the component whose license is to be verified.
    *
    *  @param componentId the id of the component whose license is to be
    *  verified
    */
   public void setComponentId(int componentId)
   {
      this.componentId = componentId;
   }
   
   /**************************************************************************
    * Bean properties
    **************************************************************************/
   
   /**
    * The id of the component whose license is to be verified. This id should be
    * obtained from the ComponentMap.xml file and set using the Installshield
    * UI.
    */
   public int componentId = 0;
   
}



