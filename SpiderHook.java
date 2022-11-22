package com.shanebow.spider;
/********************************************************************
* The Heaton Research Spider 
* Copyright 2007 by Heaton Research, Inc.
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
* 
* HTTP Programming Recipes for Java ISBN: 0-9773206-6-9
* http://www.heatonresearch.com/articles/series/16/
* 
* SpiderHook: This interface defines a class that the
* spider can report its findings to.
* 
* This class is released under the:
* GNU Lesser General Public License (LGPL)
* http://www.gnu.org/copyleft/lesser.html
* 
* @version 1.1
* @author Jeff Heaton
* @author Rick Salamone
* 20100407 RTS added method urlAdded(url, source, depth )
* 20100417 RTS modified method spiderURLError(url) to accept 
*              two addional parameters: String desc, ErrorLevel.
*              Also added enum ErrorLevel.
* 20100517 RTS renamed class to SpiderHook 
* 20100517 RTS renamed methods to make orthogonal 
*******************************************************/
import java.io.*;
import java.net.*;

public interface SpiderHook
	{
	/**
	* The types of link that can be encountered.
	*/
	public enum URLType { HYPERLINK, IMAGE, SCRIPT, STYLE }

	/**
	* The levels of url error that are reported.
	*/
	public enum ErrorLevel { INFO, WARNING, SEVERE }

	/**
	* Called when the spider has finished initialization, but before it
	* begins processing work. This method
	* provides the SpiderHook class with the spider
	* object.
	* 
	* @param spider - The spider that will be working with this object.
	*/
	public void initialized(Spider spider);

	/**
	* Called when the spider encounters a URL.
	* @param url    the URL that the spider found.
	* @param source the page that the URL was found on.
	* @param type   the URLType of the found URL
	* @return true if the spider should scan this URL for links
	*/
	public boolean urlFound(URL url, URL source, URLType type);

	/**
	* Called when the spider successfully adds a URL to the workload.
	* @param url    the URL that the spider added.
	* @param source the page that the URL was found on.
	*/
	public void urlAdded (URL url, URL source );

	/**
	* Called when the spider is about to process a NON-HTML URL.
	* @param url    the URL that the spider found.
	* @param stream An InputStream to read the page contents from.
	* @throws IOException if an IO error occurs during the processing
	*/
	public void urlProcess(URL url, InputStream stream)
		throws IOException;

	/**
	* Called when the spider is ready to process an HTML URL.
	* @param url   The URL that the spider is about to process.
	* @param parse An object that will allow you you to parse the page
	* @throws IOException if an IO error occurs during the processing
	*/
	public void urlProcess(URL url, SpiderParseHTML parse)
      throws IOException;

	/**
	* Called when the spider encounters an error while processing a URL
	* 
	* @param url   The URL that generated an error.
	* @param desc  A description of the error
	* @param level The severity of the error
	*/
	public void urlError(URL url, String desc, ErrorLevel level );
	}