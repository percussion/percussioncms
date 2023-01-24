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
package com.percussion.test.io;

import java.io.OutputStream;
import java.io.IOException;

public interface DataLoader
{
   public long getLong(String name);

   public void setLong(String name, long val);

   public double getDouble(String name);

   public void setDouble(String name, double val);

   public String getString(String name);

   public void setString(String name, String val);

   public DataLoader getChildLoader(String name, Object ob);

   public void write(OutputStream stream) throws IOException;
}
