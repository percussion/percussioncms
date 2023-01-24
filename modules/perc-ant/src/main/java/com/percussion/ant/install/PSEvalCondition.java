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
import com.percussion.install.PSLogger;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * PSEvalCondition will resolve to <code>true</code> if an eval is installed
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="evalCondition"
 *              class="com.percussion.ant.install.PSEvalCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to determine if an eval is installed.
 *
 *  <code>
 *  &lt;condition property="EVAL"&gt;
 *     &lt;evalCondition/&gt;
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSEvalCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      Code code = InstallUtil.fetchBrandCode(getRootDir());
      if (code == null)
      {
         PSLogger.logInfo("PSEvalCondition : Brand code is null");
         return false;
      }
      if(code.isAnEval())
      {
         PSLogger.logInfo("PSEvalCondition : Brand code is eval");
         return true;
      }
      return false;
   }
}
