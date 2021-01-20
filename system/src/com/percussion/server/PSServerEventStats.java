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
