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

package com.percussion.soln.segment;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="Segments")
public class Segments {
    
    private List<Segment> list;

    public Segments() {
        this(new ArrayList<Segment>());
    }

    public Segments(List<Segment> list) {
        super();
        this.list = list;
    }

    @XmlElement(name="segment")
    public List<Segment> getList() {
        return list;
    }

    public void setList(List<Segment> list) {
        this.list = list;
    }
    
}
