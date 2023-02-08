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

package com.percussion.data.jdbc.sqlparser;

/**
 * A class to allow us to throw SQL exceptions that are meant
 * to be caught and rethrown as SQLExceptions, but don't
 * require us to edit all of the JavaCC generated files
 * and add them to the throws clauses.
 */
public class UncheckedSQLException extends RuntimeException
{
   public UncheckedSQLException(String msg)
   {
      super(msg);
   }
}
