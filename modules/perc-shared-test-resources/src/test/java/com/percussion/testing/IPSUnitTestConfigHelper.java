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
