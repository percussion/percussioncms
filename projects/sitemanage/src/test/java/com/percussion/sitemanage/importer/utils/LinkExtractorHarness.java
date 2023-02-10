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

package com.percussion.sitemanage.importer.utils;

import com.percussion.sitemanage.importer.PSLink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LinkExtractorHarness {

	private static final String COMMA = ",";
	private static final String CR = "\n";
	private static final String CRLF = "\r\n";

	private static final Logger log = LogManager.getLogger(LinkExtractorHarness.class);

	private LinkExtractorHarness() {
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(final String[] args) throws IOException {
		@SuppressWarnings("unchecked")
		final List<String> urls = FileUtils.readLines(new File(args[0]));
		final File f = new File(args[1]);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		final Iterator<String> urlIterator = urls.iterator();
		while (urlIterator.hasNext()) {
			try {
				final String site = urlIterator.next();
				System.out.print("Evaluating:" + site + CR);
				Document doc = Jsoup.connect(site)
					      .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					      .referrer("http://www.google.com")
					      .get();
				final List<PSLink> links = PSLinkExtractor
						.getLinksForDocument(doc, null, null, site);
				final Iterator<PSLink> linkIterator = links.iterator();
				while (linkIterator.hasNext()) {
					final PSLink link = linkIterator.next();
					writer.write(link.getLinkText() + COMMA
							+ link.getLinkPath() + COMMA
							+ link.getAbsoluteLink() + COMMA
							+ link.getPageName() + CRLF);
				}
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}
			
		}
		writer.close();
	}
}
