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

package com.percussion.cx;

public class PSGuid
{
    private long guid;

    public PSGuid()
    {
    }

    public PSGuid(int id)
    {
        setId(id);
        setRevision(-1);
        setType(101);
    }

    public PSGuid(long guid)
    {
        this.guid = guid;
    }

    public long getPSGuid()
    {
        return guid;
    }

    public void setPSGuid(long guid)
    {
        this.guid = guid;
    }

    public int getId()
    {
        return PSGuid.getId(guid);
    }

    public void setId(int id)
    {
        long mask = 0xffffffff00000000L;
        guid = mask & guid | Long.valueOf(id).longValue();
    }

    public int getRevision()
    {
        return getRevision(guid);
    }

    public void setRevision(int revision)
    {
        long mask = 0xffffffffffL;
        guid = mask & guid | Long.valueOf(revision).longValue() << 40;
    }

    public int getType()
    {
        return PSGuid.getType(guid);
    }

    public void setType(int type)
    {
        long mask = 0xffffff00ffffffffL;
        guid = mask & guid | Long.valueOf(type).longValue() << 32;
    }

    public String toString()
    {
        return getType()+"-"+getId()+"-"+getRevision();
    }

    public static int getId(long id)
    {
        return (int)(id & 0xffffffffL);
    }

    public static int getRevision(long id)
    {
        return (int)(id >> 40);
    }

    public static int getType(long id)
    {
        return (int)(id >> 32) & 0xff;
    }


}