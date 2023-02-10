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

/**
* This class handles server event statistics for E2 server.
* <P><B>This class may implement IPSComponent interface to handle XML conversions</B></P>
*
* @author       Ravi Reddy
* @version      1.0
* @since        1.0
*/
public class PSServerEventStats extends Object
{
    /**
    * Displays the time elapsed since the server started. The time will be displayed
    * as number of days, hours, minutes and seconds since the server started.
    *
    * @return   The elapsed time since server started in string format
    */
    public String getServerRunningSince()
    {
        String  s =  new String();
        return s;
    }

    /**
    * Get the total number of events processed since the server started.
    *
    * @return   The total number of requests processed by the server
    */
    public int getEventsProcessed()
    {
        return 0;
    }

    /**
    * Get the current number of events under processing by the E2 server threads.
    *
    * @return   The number of events server is currently processing
    */
    public int getEventsProcessing()
    {
        return 0;
    }

    /**
    * The number of events waiting in the queue to be processed by E2 server.
    *
    * @return   Number of events waiting to be processed by server
    */
    public int getEventsWaiting()
    {
        return 0;
    }

    /**
    * Get the minimum amount of time taken any single event in micro seconds.
    *
    * @return Smallest time interval taken by any request
    */
    public int  getMinEventProcessTime()
    {
        return 0;
    }

    /**
    * Get the maximum amount of time taken any single event in micro seconds.
    * This will be less than or equal to maximum of the request timeout of all the
    * applications or the absolute request timeout of the server.
    *
    * @return   The maximum time taken by any of the events server processes
    */
    public int getMaxEventProcessTime()
    {
        return 0;
    }

    /**
    * Get the average time for event in micro seconds.
    *
    * @return   Average time taken for all the events processed since server started
    */
    public int getAverageEventProcessTime()
    {
        return 0;
    }
}
