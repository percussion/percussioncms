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
package com.percussion.share.test;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import com.percussion.cms.IPSConstants;
import com.percussion.share.test.xml.PSXhtmlValidator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.xml.sax.SAXParseException;

/**
 * Useful Hamcrest Matchers for Unit testing with JUnit:
 * {@link Assert#assertThat(Object, Matcher)}.
 * Its recommended that you do a static import
 * of this class to get its static methods.
 * <p>
 * Highly recommend that you read about Hamcrest here:
 * <a href="http://code.google.com/p/hamcrest/wiki/Tutorial">
 * http://code.google.com/p/hamcrest/wiki/Tutorial
 * </a>
 * 
 * @author adamgent
 *
 */
public class PSMatchers
{
    private static final Logger log = LogManager.getLogger(IPSConstants.TEST_LOG);


    /**
     * Matches valid urls.
     * @return never <code>null</code>.
     */
    public static Matcher<String> validUrl() {
        return new UrlMatcher();
    }
    
    /**
     * Matches blank strings.
     * @return never <code>null</code>.
     */
    public static Matcher<String> blankString() {
        return new StringMatcher("blank string")
        {
            @Override
            protected boolean doesMatch(String item) throws Exception
            {
                return isBlank(item);
            }
        };
    }
    
    /**
     * Matches Empty Strings
     * @return never <code>null</code>.
     */
    public static Matcher<String> emptyString() {
        return new StringMatcher("empty string")
        {
            @Override
            protected boolean doesMatch(String item) throws Exception
            {
                return isEmpty(item);
            }
        };
    }
    
    /**
     * Matches Valid XHTML. The doctype needs to be in the HTML for this to work.
     * @return never <code>null</code>.
     */
    public static Matcher<String> validXhtml() {
        return new TypeSafeMatcher<String>()
        {

            private Collection<SAXParseException> errors = new ArrayList<SAXParseException>();

            @Override
            public boolean matchesSafely(String item)
            {
                PSXhtmlValidator validator = new PSXhtmlValidator();
                InputStream stream;
                try
                {
                    stream = IOUtils.toInputStream(item, "UTF-8");
                }
                catch (UnsupportedCharsetException e)
                {
                    log.error("Exception in method matchesSafely(): "+e.getMessage());
                    return false;
                }
                boolean valid = validator.isValid(stream);
                errors = validator.getErrors();
                return valid;

            }

            @Override
            public void describeTo(Description d)
            {
                d.appendValueList("", "\n", "", errors);
            }
            
        };
    }
    
    public static Matcher<String> containsRegEx(String regex) {
        return new RegexFinder(regex);
    }
    
    public abstract static class StringMatcher extends TypeSafeMatcher<String> {
        protected Exception exception;
        protected String name;

        public StringMatcher(String name)
        {
            super();
            this.name = name;
        }

        @Override
        public boolean matchesSafely(String item)
        {
            try {
                return doesMatch(item);
            }
            catch (Exception e) {
                this.exception = e;
            }
            return false;
        }
        
        protected abstract boolean doesMatch(String item) throws Exception;

        protected String getErrorMessage() {
            return "a " + name + 
            (exception == null ? "" : " because: " + exception.getMessage());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(getErrorMessage());
        }
    }
    
    public static class UrlMatcher extends TypeSafeMatcher<String> {

        private String errorMessage = "";
        
        @Override
        public boolean matchesSafely(String spec)
        {
            try
            {
                new URL(spec);
            }
            catch (MalformedURLException e)
            {
                this.errorMessage = e.getMessage();
                return false;
            }
            return true;
            
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("not a url because: " + errorMessage);
        }
    }
    
    public static class RegexMatcher extends TypeSafeMatcher<String>{
        private final String regex;

        public RegexMatcher(String regex){
            this.regex = regex;
        }

        public void describeTo(Description description){
            description.appendText("matches regex=");
        }

        @Override
        public boolean matchesSafely(String item)
        {
            if (item == null) return false;
            return item.matches(regex);
        }
        
    }
    
    public static class RegexFinder extends TypeSafeMatcher<String>{
        private final String regex;

        public RegexFinder(String regex){
            this.regex = regex;
        }

        public void describeTo(Description description){
            description.appendText("cannot finder pattern: " + regex);
        }

        @Override
        public boolean matchesSafely(String item)
        {
            if (item == null) return false;
            Pattern p = Pattern.compile(regex);
            return p.matcher(item).find();
        }
        
    }
    
}

