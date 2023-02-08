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

package com.percussion.soln.segment.ds.data;

import java.util.List;

import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.data.ISegmentDataService;

public class DSSegmentDao extends HibernateDaoSupport implements ISegmentDataService {

    @SuppressWarnings("unchecked")
    public void resetSegmentTree(boolean clear, String rootPath) {
        if (clear) {
            List<Segment> segments = (List<Segment>) getHibernateTemplate().find("from " + Segment.class.getSimpleName());
            getHibernateTemplate().deleteAll(segments);
        }
    }

    public void updateSegmentTree(Segments segments) {
        HibernateTemplate t = getHibernateTemplate();
        for (Segment seg : segments.getList()) {
            if (seg != null) {
                Segment old = t.get(Segment.class, seg.getId());
                if (old != null) {
                    t.delete(old);
                }
                t.save(seg);
            }
        }
    }

    public String getSegmentContentType() {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("getSegmentContentType is not yet supported");
    }

    @SuppressWarnings("unchecked")
    public Segments retrieveAllSegmentData() {
        HibernateTemplate t = getHibernateTemplate();
        return new Segments((List<Segment>) t.find("from " + Segment.class.getSimpleName()));
    }

    public Segment retrieveSegmentDataForId(String arg0) {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("retrieveSegmentDataForId is not yet supported");
    }
    
}
