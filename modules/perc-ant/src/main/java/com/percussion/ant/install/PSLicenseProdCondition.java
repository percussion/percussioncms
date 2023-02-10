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



