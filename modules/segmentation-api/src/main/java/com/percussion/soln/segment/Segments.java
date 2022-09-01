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
