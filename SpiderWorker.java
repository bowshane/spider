package com.shanebow.spider;
/**
* The Heaton Research Spider
* Copyright 2007 by Heaton Research, Inc.
* 
* HTTP Programming Recipes for Java ISBN: 0-9773206-6-9
* http://www.heatonresearch.com/articles/series/16/
* 
* SpiderWorker: This class forms the workloads that are
* passed onto the thread pool.
* 
* This class is released under the:
* GNU Lesser General Public License (LGPL)
* http://www.gnu.org/copyleft/lesser.html
* 
* @author Jeff Heaton
* @version 1.1
*
* 20100410 RTS modified SpiderParseHTML construction to dynamically
*              load m_spider.getOptions().parserClass
* 20100416 RTS modified test to determine if HTML content
*              from contentType.equalsIgnoreCase("text/html")
*              to conentType.toLowerCase().startsWith("text/html")
* 20100417 RTS modified calls to spiderURLError(url) to provide two 
*              addional parameters: desc, ErrorLevel. Also changed
*              error handling to report to the SpiderHook rather
*              than to the logger.
* 20100517 RTS constructor accepts a WorkRecord rather than a URL 
* 20100517 RTS modified to use SBLog 
*/
import com.shanebow.spider.work.WorkException;
import com.shanebow.spider.work.WorkRecord;
import com.shanebow.util.SBLog;
import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;

public class SpiderWorker
	implements Runnable
	{
	/**
	* The logger
	*/
	public static final String MODULE="Worker ";
	private void log( String format, Object... args )
		{
		SBLog.write( MODULE + m_work, String.format(format, args ));
		}

	/**
	* The contructor for the html parser specified in the SpiderOptions.
	* The parser class must be a subclass of SpiderParseHTML.
	*/
	static Constructor htmlParserConstructor;

	/**
	* Member variables
	*/
	private WorkRecord m_work;   // Includes URL, depth, source, status, etc
	private Spider     m_spider; // The Spider object that this worker belongs to

	/**
	* Construct a SpiderWorker object.
	* 
	* @param spider - Spider that launched this Worker
	* @param work   - WorkRecord to be processed
	*/
	public SpiderWorker(Spider spider, WorkRecord work )
		{
		m_spider = spider;
		m_work = work;
		}

	/*
	* run() is called by the thread pool to process one single URL.
	*/
	public void run()
		{
		URL workURL = m_work.getURL();
		URL connURL = null;
		URLConnection connection = null;
		InputStream is = null;

		try
			{
			// get the URL's contents
			connection = workURL.openConnection();
			connection.setConnectTimeout(m_spider.getOptions().timeout);
			connection.setReadTimeout(m_spider.getOptions().timeout);
			if (m_spider.getOptions().userAgent != null)
				{
				connection.setRequestProperty("User-Agent", m_spider.getOptions().userAgent);
				}

			// read the URL
			connURL = connection.getURL();
			if (!workURL.equals(connURL)) // save the URL(for redirect's)
				log( "*************** REDIRECT ****************\n  %s\n  %s", workURL, connURL );
			is = connection.getInputStream();
			String contType = connection.getContentType();
			if (( contType != null )
			&&    contType.toLowerCase().startsWith("text/html"))
				{
				SpiderParseHTML parser = (SpiderParseHTML)(htmlParserConstructor.newInstance(
//				   connURL, new SpiderInputStream(is, null), m_spider));
				   m_work, new SpiderInputStream(is, null), m_spider));
//parser.setBase(connURL);
//				m_spider.getHook().urlProcess(workURL, parser);
				m_spider.getHook().urlProcess(connURL, parser);
				}
			else // non-HTML page
				{
				m_spider.getHook().urlProcess(workURL, is);
				}
			m_work.setStatus(WorkRecord.SUCCESS);
			}
		catch (IOException e)
			{
			urlError( workURL, e, SpiderHook.ErrorLevel.INFO );
			return;
			}
		catch (Throwable e)
			{
			urlError( workURL, e, SpiderHook.ErrorLevel.SEVERE );
			// e.printStackTrace();
			// System.exit(1);
			return;
			}
		finally
			{
			try { if (is != null) is.close(); }
			catch (IOException e) {}
			}

		try
			{
			if (!workURL.equals(connURL)) // save the URL(for redirect's)
				m_spider.getWorkload().add(connURL, m_work, WorkRecord.SUCCESS);
			}
		catch (WorkException e) { urlError( connURL, e, SpiderHook.ErrorLevel.INFO ); }
		}

	private void urlError(URL url, Throwable e, SpiderHook.ErrorLevel severity )
		{
		m_spider.getHook().urlError( url, e.toString(), severity );
		try { m_work.setStatus(WorkRecord.ERROR); }
		catch (WorkException e1)
			{
			m_spider.getHook().urlError( url, "Mark work failed: " + e.toString(),
                 SpiderHook.ErrorLevel.WARNING );
			}
		}
	}
