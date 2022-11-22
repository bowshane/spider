package com.shanebow.spider.work;
/**
* Copyright (c) 2010 by Richard T. Salamone, Jr.  All rights reserved.
* 
* Workload: This interface defines a workload for the spider.
* A workload is essentially a list of URLs along with their
* processing status: waiting, active, successfully completed,
* or failed.
* Developed for use with the Heaton Research Spider .
* 
* @author Rick Salamone
* @version 1.0
*/
import java.net.URL;
import com.shanebow.spider.Spider;

public interface Workload
	{
	/**
	* Setup this workload for use by the specified spider.
	* @param spider The spider using this workload.
	* @throws WorkException if there is an initialization error
	*/
	public void init(Spider spider) throws WorkException;

	/**
	* Clear the workload.
	* @throws WorkException if error precludes clearing the workload.
	*/
	public void clear() throws WorkException;

	/**
	* @return true if there is no more work to do now.
	* @throws WorkException if problem determining if
	*           the workload is empty.
	*/
	public boolean isEmpty() throws WorkException;

	/**
	* Add the specified URL to the workload.
	* @param  url    The URL to be added.
	* @return true   if the URL was added, false otherwise.
	* @throws WorkException
	*/
	public boolean add( String url ) throws WorkException;

	/**
	* Add the specified URL to the workload.
	* @param  url    The URL to be added.
	* @param  source The Workrecord that contains this URL.
	* @return true   if the URL was added, false otherwise.
	* @throws WorkException
	*/
	public boolean add(URL url, WorkRecord source, char status ) throws WorkException;

	/**
	* Determine if the workload contains the specified URL.
	* @param url
	* @return true if the workload already contains the URL
	* @throws WorkException
	*/
//	public boolean contains(URL url) throws WorkException;

	/**
	* Get a new URL to work on. Wait if there are no URL's currently
	* available. Return null if done with the current host. The URL
	* returned is marked as in progress.
	* @return The next URL to parse
	* @throws WorkException if the next URL could not be obtained.
	*/
	public WorkRecord getWork() throws WorkException;

	public void shutdown();
	}
