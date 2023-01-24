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

package com.percussion.utils.container;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IPSJdbcJettyDbmsDefConstants
{

   public static final String JETTY_DS_PREFIX="perc.ds";
   public static final String JETTY_NAME_SUFFIX="name";
   public static final String JETTY_UID_SUFFIX="uid";
   public static final String JETTY_PWD_SUFFIX="pwd";
   public static final String JETTY_IDLE_MS="idle.ms";
   public static final String JETTY_CONNECTIONS_MIN="connections.min";
   public static final String JETTY_CONNECTIONS_MAX="connections.max";
   public static final String JETTY_PWD_ENCRYPTED_SUFFIX="pwd.encrypted";
   public static final String JETTY_BACKEND_NAME_SUFFIX="backend.name";
   public static final String JETTY_DRIVER_NAME_SUFFIX="driver.name";
   public static final String JETTY_DRIVER_CLASS_SUFFIX="driver.class";
   public static final String JETTY_CONNECTIONTEST_SUFFIX="connectiontest";
   public static final String JETTY_SERVER_SUFFIX="server";
   
   public static final List<String> JETTY_DS_PARAMS = 
         Stream.of(JETTY_DS_PREFIX,JETTY_NAME_SUFFIX,JETTY_UID_SUFFIX,JETTY_PWD_SUFFIX,JETTY_CONNECTIONS_MIN,JETTY_CONNECTIONS_MAX,JETTY_BACKEND_NAME_SUFFIX,JETTY_BACKEND_NAME_SUFFIX,JETTY_DRIVER_CLASS_SUFFIX,JETTY_CONNECTIONTEST_SUFFIX,JETTY_SERVER_SUFFIX)
         .collect(Collectors.toList());
   
   public static final String JETTY_CONN_PREFIX="perc.conn";
   public static final String JETTY_CONN_NAME_SUFFIX="name";
   public static final String JETTY_CONN_SCHEMA_SUFFIX="schema";
   public static final String JETTY_CONN_DS_SUFFIX="ds";
   public static final String JETTY_CONN_DB_SUFFIX="db";
   public static final String JETTY_CONN_DEFAULT_SUFFIX="default";
   
   public static final List<String> JETTY_CONN_PARAMS = 
         Stream.of(JETTY_CONN_NAME_SUFFIX,JETTY_CONN_SCHEMA_SUFFIX,JETTY_CONN_DS_SUFFIX,JETTY_CONN_DB_SUFFIX,JETTY_CONN_DEFAULT_SUFFIX)
         .collect(Collectors.toList());
  
  

   
   
}
