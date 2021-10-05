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

package   com.percussion.log;


/**
 * IPSLogReader   defines   an interface for reading log messages
 * (PSLogInformation sub-objects)   from a log implementation. Log readers
 * should   only be   used by   the   PSLogManager object.
 * <p>
 * Log entries can be   located   using   one   of the following mechanisms:
 * <ul>
 *    <li>within   a   specified   time range</li>
 *    <li>by   application   id</li>
 *    <li>by   application   id and time   range</li>
 * </ul>
 *
 * @author       Tas Giakouminakis
 * @version       1.0
 * @since          1.0
 */
public interface IPSLogReader   {
   /**
    *   Close   the   log   reader.   This should   only be   called when   it is   no longer
    *   needed.   Any   subsequent attempts to read from this object will throw
    *   an exception. It is not an error to call close() on a closed log reader.
    *
    *   @see #isOpen
    */
    public   void close();

   /**
    *   Use to query whether the log reader is open or not.
    *
    *   @return   Returns true if the log reader is open, false if the log
    *   reader is not open.
    *
    *   @see #close
    */
    public boolean isOpen();

   /**
    *   Read log messages   using   the   specified   filter.
    *
    *   @param         filter                           the   log   message   filter
    *
    *   @exception   IllegalStateException      if close has already been   called
    *                                                         on this   reader
    */
   public   void read(IPSLogReaderFilter filter)
      throws java.lang.IllegalStateException;
}
