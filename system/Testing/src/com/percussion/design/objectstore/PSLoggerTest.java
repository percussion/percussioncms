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

package com.percussion.design.objectstore;
import junit.framework.TestCase;

import java.security.SecureRandom;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

/**
 *   Unit tests for the PSLogger class
 */

public class PSLoggerTest extends TestCase
{
   /**
    *   Check that all options are off initially.
    */
   public void testOptionsInitiallyOff()
   {
      PSLogger logger = new PSLogger();
      assertTrue(!logger.isErrorLoggingEnabled());
      assertTrue(!logger.isServerStartStopLoggingEnabled());
      assertTrue(!logger.isAppStartStopLoggingEnabled());
      assertTrue(!logger.isAppStatisticsLoggingEnabled());
      assertTrue(!logger.isBasicUserActivityLoggingEnabled());
      assertTrue(!logger.isDetailedUserActivityLoggingEnabled());
      assertTrue(!logger.isMultipleHandlerLoggingEnabled());
   }

   /**
    *   Randomly turn arguments on and off and check that they are
    *   indeed on or off.
    */
   public void testOptionsEnabling()
   {
      final int ERROR_LOGGING = 1;
      final int SERVER_STARTSTOP = 2;
      final int APP_STARTSTOP = 4;
      final int APP_STATS = 8;
      final int DETAILED_USER = 16;
      final int BASIC_USER = 32;
      final int MULTIPLE_HANDLER = 64;
      
      PSLogger logger = new PSLogger();
      SecureRandom rand = new SecureRandom();
      int options = 0;

      for (int i = 0; i < 100; i++)
      {
         options = rand.nextInt(ERROR_LOGGING | SERVER_STARTSTOP | APP_STARTSTOP |
            APP_STATS | DETAILED_USER | BASIC_USER | MULTIPLE_HANDLER);

         logger.setErrorLoggingEnabled(0 != (options & ERROR_LOGGING));
         logger.setServerStartStopLoggingEnabled(0 != (options & SERVER_STARTSTOP));
         logger.setAppStartStopLoggingEnabled(0 != (options & APP_STARTSTOP));
         logger.setAppStatisticsLoggingEnabled(0 != (options & APP_STATS));
         logger.setBasicUserActivityLoggingEnabled(0 != (options & BASIC_USER));
         logger.setDetailedUserActivityLoggingEnabled(0 != (options & DETAILED_USER));
         logger.setMultipleHandlerLoggingEnabled(0 != (options & MULTIPLE_HANDLER));

         if (0 != (options & ERROR_LOGGING))
            assertTrue(logger.isErrorLoggingEnabled());
         else
            assertTrue(!logger.isErrorLoggingEnabled());
         if (0 != (options & SERVER_STARTSTOP))
            assertTrue(logger.isServerStartStopLoggingEnabled());
         else
            assertTrue(!logger.isServerStartStopLoggingEnabled());
         if (0 != (options & APP_STARTSTOP))
            assertTrue(logger.isAppStartStopLoggingEnabled());
         else
            assertTrue(!logger.isAppStartStopLoggingEnabled());
         if (0 != (options & APP_STATS))
            assertTrue(logger.isAppStatisticsLoggingEnabled());
         else
            assertTrue(!logger.isAppStatisticsLoggingEnabled());
         if (0 != (options & BASIC_USER))
            assertTrue(logger.isBasicUserActivityLoggingEnabled());
         else
            assertTrue(!logger.isBasicUserActivityLoggingEnabled());
         if (0 != (options & DETAILED_USER))
            assertTrue(logger.isDetailedUserActivityLoggingEnabled());
         else
            assertTrue(!logger.isDetailedUserActivityLoggingEnabled());
         if (0 != (options & MULTIPLE_HANDLER))
            assertTrue(logger.isMultipleHandlerLoggingEnabled());
         else
            assertTrue(!logger.isMultipleHandlerLoggingEnabled());
      } // end for
   }

   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSLogger logger1 = new PSLogger();
      final PSLogger logger2 = new PSLogger();
      
      assertFalse(logger1.equals(new Object()));
      assertEqualsWithHash(logger1, logger2);
      
      logger1.setErrorLoggingEnabled(true);
      assertFalse(logger1.equals(logger2));
      logger2.setErrorLoggingEnabled(true);
      assertEqualsWithHash(logger1, logger2);
   }
}
