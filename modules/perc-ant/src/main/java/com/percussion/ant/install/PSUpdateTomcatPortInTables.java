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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * See super class
 */
public class PSUpdateTomcatPortInTables extends PSExecSQLStmt
{
   // see base class
   @Override
   public void execute()
   {
      String sqlStr = getSql();
      String patternStr = "CATALINA_PORT";
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher(sqlStr);
      sqlStr = matcher.replaceAll(tomcatPort);
      setSql(sqlStr);
      super.execute();   
   }
   
   /**
    * @return Returns the tomcatPort.
    */
   public String getTomcatPort()
   {
      return tomcatPort;
   }
   /**
    * @param tokens The tomcatPort to set.
    */
   public void setTomcatPort(String token)
   {
      this.tomcatPort = token;
   }
   
   /**
    * Tomcat port from the tomcat panel
    */
   protected String tomcatPort = "9992";
}


