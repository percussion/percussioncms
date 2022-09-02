/******************************************************************************
 *
 * [ CookieMatcher.java ]
 *
 * COPYRIGHT (c) 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package test.percussion.soln.p13n.tracking;

import javax.servlet.http.Cookie;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CookieMatcher extends TypeSafeMatcher<Cookie> {
	private String name;

	private String value;

	public CookieMatcher(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public void describeTo(Description description) {
		description.appendText("a Cookie named ").appendValue(name)
				.appendText(" with a value ").appendValue(value);
	}

	@Override
	public boolean matchesSafely(Cookie c) {
		return c.getName().equals(name) && c.getValue().equals(value);
	}

	@Factory
	public static <T> Matcher<Cookie> aCookie(String name, String value) {
		return new CookieMatcher(name, value);
	}
}