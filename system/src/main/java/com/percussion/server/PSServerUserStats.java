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
package com.percussion.server;

import java.util.Date;

/**
* This class will be used to represent one user\session information
* on E2 server. All the current users on E2 server will be collected in
* a collection component and shown in UI of Admin applet.
* <P><B>This class may implement <code>IPSComponent interface to handle XML
* conversions</code></B></P>
*
* @author       Ravi Reddy
* @version      1.0
* @since        1.0
*/
public class PSServerUserStats extends Object
{
    /**
    Constructs the PSServerUserStats object from the existing data.
    */
    PSServerUserStats(int ID, String name, String host, boolean bIdle, Date startTime)
    {

    }

    /**
    * Get the ID of the session. This will be unique across an E2 server.
    *
    * @return   session unique id for each client connection
    */
    public int getSessionID()
    {
        return 0;
    }

    /**
    * Get the user name of this session. Can be anonymous.
    *
    * @return Name of the user connected to E2 server
    */
    public String getName()
    {
        return null;
    }

    /**
    * Returns true if the user session is idle or false if the session is busy.
    *
    * @return   <code>true</code> if there is no request processing for this user
    *           else <code>false</code>
    */
    public boolean IsUserIdle()
    {
        return true;
    }

    /**
    * True, if the user is logged into E2 server with proper credentials.
    */
    public boolean IsLoogedIn()
    {
        return true;
    }

    /**
    Return the host from where this connection is made.
    */
    public String getHostname()
    {
        return null;
    }

    /**
    If the user session is not busy, the idle time will be returned, else
    this method returns 0.
    */
    public int getIdleTime()
    {
        return 0;
    }

    private int         m_sessionID = 0;//user session ID
    private String      m_name;     //name of the user
    private boolean     m_busy = false;     //busy or idle
    private boolean     m_loggedIn; //weather loogied in or annonymous
    private String      m_hostName;
    private int         m_idleTime = 0;   //if idle, how long
}
