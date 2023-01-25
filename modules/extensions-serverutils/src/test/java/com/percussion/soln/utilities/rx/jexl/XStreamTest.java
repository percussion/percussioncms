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

package test.percussion.soln.utilities.rx.jexl;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class XStreamTest {
    @Test
    public void shouldWriteXML() throws UnsupportedEncodingException {
        XStream xs = new XStream();
        //Might fail because of local encoding problems.
        byte[] bs = xs.toXML("Why and when to upgrade to Rhythmyx 6.x and what’s included.").getBytes("UTF-8");
        ByteArrayInputStream bi = new ByteArrayInputStream(bs);
        String data = (String) xs.fromXML(bi);
        assertEquals("Why and when to upgrade to Rhythmyx 6.x and what’s included.", data);
    }
}
