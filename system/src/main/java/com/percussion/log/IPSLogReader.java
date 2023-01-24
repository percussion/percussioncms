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
