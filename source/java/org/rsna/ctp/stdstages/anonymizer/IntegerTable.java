/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.ctp.stdstages.anonymizer;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jdbm.RecordManager;
import jdbm.htree.HTree;
import org.apache.log4j.Logger;
import org.rsna.util.JdbmUtil;

/**
 * A database for tracking assigned integer replacements for text strings.
 */
public class IntegerTable {

	static final Logger logger = Logger.getLogger(IntegerTable.class);

	File dir;
	final static ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<>();
	final static ConcurrentHashMap<String,AtomicInteger> counterPerType = new ConcurrentHashMap<>();
	// final static AtomicInteger globalCounter = new AtomicInteger(0);

	/**
	 * Constructor; create an IntegerTable from a database file.
	 * @param dir the directory in which the database is to be created.
	 * @throws Exception if the table cannot be loaded.
	 */
	public IntegerTable(File dir) throws Exception {
		this.dir = dir;
	}

	/**
	 * Commit and close the IntegerTable.
	 */
	public void close() {
		System.out.println("IntegerTable: close() called");
	}

	/**
	 * Get a String containing an integer replacement for text of a specified type.
	 * @param type any String identifying the category of text being replaced, for example "ptid".
	 * @param text the text string to be replaced by an integer string.
	 * @param width the minimum width of the replacement string. If the width parameter
	 * is negative or zero, no padding is provided.
	 * @return the replacement string, with leading zeroes if necessary to pad the
	 * replacement string to the required width.
	 */
	public String getInteger(String type, String text, int width) {
		try {
			final String trimmedText = text.trim();
			final String trimmedType = type.trim();
			logger.debug("getInteger: \""+trimmedType+"\", \""+trimmedText+"\", "+width);
			String key = trimmedType + "/" + trimmedText;
			logger.debug("...searching for "+key);
			

			Integer value = map.computeIfAbsent(key, k -> {
				// int newValue = typeCounter.incrementAndGet();
				final AtomicInteger typeCounter = counterPerType.computeIfAbsent(trimmedType, _k -> { return new AtomicInteger(0); });
				int newValue = typeCounter.incrementAndGet();
				logger.debug("IntegerTable: creating new value for "+k+": "+newValue);
				return Integer.valueOf(newValue);
			});
			
			

			logger.debug("IntegerTable: "+ key + " => " + value);
			logger.debug("...success");
			int intValue = value.intValue();
			String format = (width > 0) ? ("%0"+width+"d") : ("%d");
			return String.format(format, intValue);
		}
		catch (Exception e) {
			logger.warn("Unable to create integer for (\""+type+"\",\""+text+"\","+width+")", e);
			return "error";
		}
	}

}
