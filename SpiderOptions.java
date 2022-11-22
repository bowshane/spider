package com.shanebow.spider;
/****************************************************************
*
* The Heaton Research Spider 
* Copyright 2007 by Heaton Research, Inc.
* 
* HTTP Programming Recipes for Java ISBN: 0-9773206-6-9
* http://www.heatonresearch.com/articles/series/16/
* 
* SpiderOptions: Contains options for the spider's execution.
* 
* This class is released under the:
* GNU Lesser General Public License (LGPL)
* http://www.gnu.org/copyleft/lesser.html
* 
* @author Jeff Heaton
* @version 1.1
*
* 20100410 RTS added member field Class parserClass
*              with a default value of SpiderParseHTML.class
*              to enable the SpiderWorker to dynamically
*              load subclasses of SpiderParseHTML which can
*              do customized searching.
* 20100416 RTS constants for the RAM & DB WorkloadManagers as
*              well as a default value for dbClass = JDBC_DRIVER
* 20100513 RTS added public constants for default pool sizes and
*              keep alive time.
*
*******************************************************************/
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class SpiderOptions
	{
	public static final int DEFAULT_POOL_QUEUE_SIZE = 5;
	public static final int DEFAULT_POOL_CORE_SIZE = 20;
	public static final int DEFAULT_POOL_MAX_SIZE = DEFAULT_POOL_CORE_SIZE;
	public static final int DEFAULT_POOL_KEEP_ALIVE = 60000;

	/**
	* Specifies the class of the parser that will be contructed to
	* process HTML type URLs - this must be SpiderParseHTML or a subclass.
	*/
	public Class parserClass = SpiderParseHTML.class;

	/**
	* How many milliseconds to wait when downloading pages.
	*/
	public int timeout = 120000; // was 60000;

	/**
	* The maximum depth to search pages. -1 specifies no maximum.
	*/
	public int maxDepth = -1;

	/**
	* What user agent should be reported to the web site.
	* This allows the web site to determine what browser is
	* being used.
	*/
	public String userAgent = null;

	/**
	* The core thread pool size.
	*/
	public int corePoolSize = DEFAULT_POOL_CORE_SIZE;

	/**
	* The maximum thread pool size.
	*/
	public int maximumPoolSize = DEFAULT_POOL_MAX_SIZE;

	/**
	* Capacity of the BlockingQueue used by the thread pool
	*/
	public int poolQueueSize = DEFAULT_POOL_QUEUE_SIZE;

	/**
	* How long, in seconds, to keep inactive threads alive.
	*/
	public long keepAliveTime = DEFAULT_POOL_KEEP_ALIVE; // 60000; // 60;

	/**
	* The URL to use for JDBC databases: defaults to URL_SPIDERWORK.
	*/
	public  static final String URL_SPIDERWORK = "jdbc:odbc:APO";
	public String dbURL =  URL_SPIDERWORK;

	/**
	* The user id to access the JDBC workload database.
	*/
	public String dbUID;

	/**
	* The password to access the JDBC workload database.
	*/
	public String dbPWD;

	/**
	* The class to use for JDBC connections, used to hold the workload.
	* Defaults to the Sun JDBC driver.
	*/
	public static final String JDBC_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
	public String dbClass = JDBC_DRIVER;

	/**
	* What class to use to process the workload.
	*/
	public static final String WORKLOAD_RAM
		= "com.shanebow.spider.work.ram.RAMWorkload";
	public static final String WORKLOAD_SQL
		= "com.shanebow.spider.work.sql.SQLWorkload";
	public String workload = WORKLOAD_RAM; // fully qualified class name

	/*
	* Specifies a class to be used a filter
	*/
//	public List<String> filter = new ArrayList<String>();

	/**
	* Load the spider settings from a configuration file.
	* 
	* @param inputFile
	*          The name of the configuration file.
	* @throws IOException
	*           Thrown if an I/O error occurs.
	* @throws SpiderException
	*           Thrown if there is an error mapping
	*           configuration items between the file and this object.
	public void load(String inputFile) throws IOException, SpiderException
		{
		FileReader f = new FileReader(new File(inputFile));
		BufferedReader r = new BufferedReader(f);
		String line;
		while ((line = r.readLine()) != null)
			{
			try { parseLine(line); }
			catch (IllegalArgumentException e) { throw (new SpiderException(e)); }
			catch (SecurityException e)        { throw (new SpiderException(e)); }
			catch (IllegalAccessException e)   { throw (new SpiderException(e)); }
			catch (NoSuchFieldException e)     { throw (new SpiderException(e)); }
			}
		r.close();
		f.close();
		}
	*/

	/**
	* Process each line of a configuration file.
	* 
	* @param line
	*          The line of text read from the configuration
	*          file.
	* @throws IllegalArgumentException if an invalid argument is specified.
	* @throws SecurityException        if a security exception occurs.
	* @throws IllegalAccessException   if a field cannot be accessed.
	* @throws NoSuchFieldException     if an invalid field is specified.
  @SuppressWarnings("unchecked")
	private void parseLine(String line)
		throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException
		{
		String name, value;
		int i = line.indexOf(':');
		if (i == -1) return;

		name = line.substring(0, i).trim();
		value = line.substring(i + 1).trim();

		if (value.trim().length() == 0)
			value = null;

		Field field = this.getClass().getField(name);
		if (field.getType() == String.class)
			{
			field.set(this, value);
			}
		else if (field.getType() == List.class)
			{
			List<String> list = (List<String>) field.get(this);
			list.add(value);
			}
		else
			{
			int x = Integer.parseInt(value);
			field.set(this, x);
			}
		}
	*/
	}
