package com.percussion.soln.segment.rx;

import static java.util.Collections.*;

import java.io.StringReader;
import java.util.List;


public class SegmentAliasUtil {
    
    public static List<String> parseAliases(String input) {
        if (input == null) throw new IllegalArgumentException("input cannot be null");
        if ("".equals(input)) return emptyList();
        StringReader sr = new StringReader(input);
        SegmentAliasFieldLexer lexer = new SegmentAliasFieldLexer(sr);
        SegmentAliasFieldParser parser = new SegmentAliasFieldParser(lexer);
        try {
            parser.startRule();
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad input: " + input, e);
        }
        return parser.getAliases();
    }

}
