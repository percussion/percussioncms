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
import com.percussion.util.IPSBrandCodeConstants;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * PSSprintaCondition will resolve to <code>true</code> if sprinta is
 * allowed to be installed.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the typedef:
 * 
 *  <code>  
 *  &lt;typedef name="sprintaCondition"
 *              class="com.percussion.ant.install.PSSprintaCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task to determine if sprinta is allowed to be installed.
 * 
 *  <code>
 *  &lt;condition property="ALLOW_SPRINTA"&gt;
 *     &lt;sprintaCondition/&gt;
 *  &lt;/condition&gt;
 *  </code>
 * 
 * </pre>
 * 
 */
public class PSSprintaCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      Code code = InstallUtil.fetchBrandCode(getRootDir());
      if (code == null)
      {
         return false;
      }
      if (code.isAnEval())
      {
         return true;
      }
      if (code.isComponentLicensed(IPSBrandCodeConstants.SPRINTA))
      {
         return true;
      }
      return false;
   }
}
