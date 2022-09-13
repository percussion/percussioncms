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
