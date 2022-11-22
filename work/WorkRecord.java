package com.shanebow.spider.work;
/**
* Copyright (c) 2010 by Richard T. Salamone, Jr.  All rights reserved.
* 
* WorkRecord: This class defines a unit of work to be processed by the Spider.
* Each record consists of a URL and its associated status, along with other
* useful tracking information such as the time last modified, a pointer to its
* source, and its depth as measured from the root WorkRecord.
* Developed for use with The Heaton Research Spider.
* 
* @author Rick Salamone
* @version 1.0
*/
import com.shanebow.spider.work.WorkException;
import com.shanebow.util.SBDate;
import java.net.*;

public class WorkRecord
	{
	public final static long NULL_ID = -1;
	/*
	* The values for the status
	*/ 
	public final static char WAITING = 'W'; // Waiting to be processed.
	public final static char ACTIVE  = 'A'; // Currently being processed.
	public final static char SUCCESS = 'S'; // Successfully processed.
	public final static char ERROR   = 'E'; // Unsuccessfully processed.

	private long   m_id = NULL_ID; //
	private long   m_idHost = NULL_ID; //
	private URL    m_url;       // The source of this URL
	private char   m_status;    // The current status of this URL
	private int    m_depth = 0; // The depth of this URL from spider starting point
	private long   m_idSource = NULL_ID;
	private long   m_lmdt = 0;  // last modified time/date
	private long   m_idParser = NULL_ID;  // parser used to search this page

	public WorkRecord()
		throws WorkException
		{
		throw new WorkException("Cannot use no arg constructor");
		}

	public WorkRecord( URL url, WorkRecord source )
		throws WorkException
		{
		this( url, source, WAITING );
		}

	public WorkRecord( URL url, WorkRecord source, char status )
		throws WorkException
		{
		m_url = url;
		m_status = status;
		if ( source != null )
			{
			m_depth = source.getDepth() + 1;
			m_idSource = source.getID();
			}
		m_lmdt = timeNow();
		}

	public WorkRecord( long id, long idHost, URL url, char status,
	           int depth, long idSource, long idParser, long lmdt )
	//	throws WorkException
		{
		m_id = id;
		m_idHost = idHost;
		m_url = url;
		m_status = status;
		m_depth = depth;
		m_idSource = idSource;
		m_idParser = idParser;
		m_lmdt = lmdt;
		}

	public boolean equals( URL url ) { return m_url.equals(url); }
	public String  toString() { return m_url.toString() + "(" + m_id + ")"; }
	public String  formatted()
		{
		return String.format( "%4d %4d %d %s %s",
			m_id, m_idSource, m_depth, "" + m_status, m_url ); // m_lmdt
		}

	public long   getID()       { return m_id; }
	public int    getDepth()    { return m_depth; }
	public long   getSourceID() { return m_idSource; }
	public char   getStatus()   { return m_status; }
	public URL    getURL()      { return m_url; }
	public long   getLMDT()     { return m_lmdt; }

	public void setID(long id) { m_id = id; }
	public void setStatus(char status) throws WorkException
		{
		m_status = status;
		m_lmdt = timeNow();
		}

	public void commit() throws WorkException {}

	// Static interface
	public static int _maxLengthURL = 255;
	/**
	* Convert the specified String to a URL.
	* @param  urlString  A String to convert into a URL.
	* @return The URL.
	* @throws WorkException if the string is too long or has other issues
	*/
	public static URL toURL(String url) throws WorkException
		{
		URL result = null;

		url = url.trim();
		if ((_maxLengthURL > 0) && (url.length() > _maxLengthURL))
			throw new WorkException("URL is too long, must be < "
			   + _maxLengthURL + " chars.");
		try { result = new URL(url); }
		catch (MalformedURLException e) { throw new WorkException(e); }
		return result;
		}

	public static final long timeNow() { return SBDate.timeNow(); }
	}
