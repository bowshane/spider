package com.shanebow.spider.work.ram;
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
import com.shanebow.spider.Spider;
import com.shanebow.spider.work.*;
import java.net.URL;
import java.util.List;
import java.util.Vector;

public class RAMWorkload
	implements Workload
	{
	private List<WorkRecord> m_workload = null;
	private int m_nextWaiting = 0;
	private Spider m_spider;

	/**
	* Setup this workload for use by the specified spider.
	* @param spider The spider using this workload.
	* @throws WorkException if there is an initialization error
	*/
	public void init(Spider spider)
		throws WorkException
		{
		m_spider = spider;
		if ( m_workload != null )
			throw new WorkException( "Trying to reinitialize existing workload" );
		WorkRecord._maxLengthURL = -1; // allow arbitrary length
		m_workload = new Vector<WorkRecord>();
		}

	/**
	* Clear the workload.
	* @throws WorkException if error precludes clearing the workload.
	*/
	public void clear() // throws WorkException;
		{
		m_workload.clear();
		m_nextWaiting = 0;
		}

	/**
	* @return true if there is no more work to do now.
	* @throws WorkException if cannot determine whether empty workload
	*/
	public boolean isEmpty()
		throws WorkException
		{
		return m_workload.size() <= m_nextWaiting;
		}

	/**
	* Add the specified URL to the workload.
	* @param  url    The URL to be added.
	* @return true   if the URL was added, false otherwise.
	* @throws WorkException
	*/
	public boolean add( String url )
		throws WorkException
		{
		return add( WorkRecord.toURL(url), null, WorkRecord.WAITING );
		}

	/**
	* Add the specified URL to the workload.
	* @param  url    The URL to be added.
	* @param  source The Workrecord that contains this URL.
	* @return true   if the URL was added, false otherwise.
	* @throws WorkException
	*/
	public boolean add(URL url, WorkRecord source, char status )
		throws WorkException
		{
		if ( contains(url))
			return false;
		WorkRecord record = new WorkRecord( url, source, status );
		m_workload.add( record );
		record.setID( m_workload.size() - 1);
		return true;
		}

	/**
	* Determine if the workload contains the specified URL.
	* @param url
	* @return
	* @throws WorkException
	*/
	public boolean contains(URL url) // throws WorkException
		{
		for ( WorkRecord work : m_workload )
			if ( work.equals(url))
				return true;
		return false;
		}

	/**
	* Get a new URL to work on. Wait if there are no URL's currently
	* available. Return null if done with the current host. The URL
	* returned is marked as in progress.
	* @return The next URL to parse
	* @throws WorkException if the next URL could not be obtained.
	*/
	public WorkRecord getWork() throws WorkException
		{
		if ( m_nextWaiting >= m_workload.size())
			return null;
		WorkRecord wr = m_workload.get(m_nextWaiting++);
		wr.setStatus( WorkRecord.ACTIVE );
		return wr;
		}

	public void shutdown()
		{
		for ( WorkRecord wr : m_workload )
			System.out.println( wr.formatted());
		}
	}
