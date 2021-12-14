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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
