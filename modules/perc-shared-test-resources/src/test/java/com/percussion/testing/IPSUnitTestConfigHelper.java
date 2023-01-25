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
package com.percussion.testing;


/**
 * If a JUnit test requires a Rhythmyx server to run and it is invoked as
 * a remote client (of the Rhythmyx server), it should implement this
 * interface in addition to the TestCase class. 
 */
public interface IPSUnitTestConfigHelper
{
   /**
    * Prop keys.
    */
   public static final String PROP_HOST_NAME = "hostName";
   public static final String PROP_PORT  = "port";
   public static final String PROP_USESSL = "useSSL";
   public static final String PROP_LOGIN_ID = "loginId";
   public static final String PROP_LOGIN_PW = "loginPw";
   public static final String PROP_SCHEME = "scheme";
   public static final String PROP_REALM = "realm";
   public static final String PROP_SERVER_ROOT = "serverRoot";
   
   /**
    * Conn type for Rx server. Properties are extracted from
    * conn_rxserver.properties file. The location of which is expected in
    * this same package com.percussion.testing. 
    */
   public static final int CONN_TYPE_RXSERVER = 0;
   
   /**
    * Conn type for tomcat server. Properties are extracted from
    * conn_tomcat.properties file. The location of which is expected in
    * this same package com.percussion.testing.
    */
   public static final int CONN_TYPE_TOMCAT = CONN_TYPE_RXSERVER + 1;
   
   /**
    * Conn type for Rx SQL server. Extracts DB props from Rx standard
    * conn_sql.properties file. The location of which is expected in
    * this same package com.percussion.testing.
    */
   public static final int CONN_TYPE_SQL = CONN_TYPE_TOMCAT + 1; 
   
   /**
    * Conn type for Rx ORA server. Extracts DB props from Rx standard
    * conn_ora.properties file. The location of which is expected in
    * this same package com.percussion.testing.
    */
   public static final int CONN_TYPE_ORA = CONN_TYPE_SQL + 1;
}
