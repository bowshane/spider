package com.shanebow.spider.work.sql;
/**
* Copyright 2010 by Richard T. Salamone, Jr.
* 
* WorkloadManager: This interface defines a workload manager. A
* workload manager handles the lists of URLs that have been
* processed, resulted in an error, and are waiting to be processed.
* Developed for use with the Heaton Research Spider.
* 
* @author Rick Salamone
* @version 1.0
*/
import com.shanebow.spider.Spider;
import com.shanebow.spider.work.*;
import com.shanebow.util.SBLog;
import java.net.URL;
import java.sql.*;

public final class SQLWorkload
	implements Workload
	{
	public static final String MODULE="SQLWork";
	public static final String TBL_WORK = "spiderWork";

	private void log( String format, Object... args )
		{
		SBLog.write( MODULE, String.format(format, args ));
		}

	Spider m_spider;
	java.util.Queue<WorkRecord> m_cache = null;

	/**
	* Setup this workload manager for the specified spider.
	* @param spider The spider using this workload manager.
	* @throws WorkException if there is an initialization error
	*/
	public SQLWorkload() {}

	public void init(Spider spider)
		throws WorkException
		{
		m_spider = spider;
		WorkDatabase.connect( spider.getOptions());
		try { SQLWork.setWorkDatabase( new WorkDatabase()); }
		catch ( SQLException e) { throw new WorkException(e); }
		m_cache = new java.util.concurrent.ArrayBlockingQueue<WorkRecord>( spider.getOptions().poolQueueSize + 10);
log ( "initialized" );
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
	* @param  url    The URL to be added
	* @param  source The page that contains this URL
	* @param  status The processing status of this URL
	* @return True   if the URL was added, false otherwise.
	* @throws WorkException
	*/
	public boolean add(URL url, WorkRecord source, char status )
		throws WorkException
		{
		int result = 0;
		int depth = 0;
		long idSource = SQLWork.NULL_ID;
		WorkDatabase dbStatement = null;
		ResultSet rs = null;
		String urlString = url.toString().trim();
		try
			{
			dbStatement = new WorkDatabase();
			// first see if the database contains this url
			int hash = computeHash(url);
			String stmt = "SELECT url FROM " + TBL_WORK + " WHERE url_hash = ?;";
			rs = dbStatement.executeQuery( stmt, hash );
			while (rs.next())
				if ( urlString.equals(rs.getString(1))) // TBL_WORK.url
					return false;

			if ( source != null )
				{
				depth = source.getDepth() + 1;
				idSource = source.getID();
				}
			stmt = "INSERT INTO " + TBL_WORK
			     + " (idHost,url,status,depth,idSource,idParser,lmdt,url_hash)"
			     + " VALUES(?,?,?,?,?,?,?,?);";
			result = dbStatement.executeUpdate( stmt, getHostID(url), urlString,
				                "" + status, depth, idSource, getParserID(), SQLWork.timeNow(), hash );
			}
		catch ( Exception ex )
			{
//			spider.getHook().urlError(
		log ( "add(%s) EXCEPTION %s", url, ex );
			throw new WorkException(ex);
			}
		finally
			{
			if ( dbStatement != null)
				{
				if ( rs != null )
					{try { rs.close(); } catch (Exception e) {}}
				dbStatement.closeStatement();
				}
			}
		return (result == 1);
		}

	private long getHostID(URL url)
		{
		return SQLWork.NULL_ID;
		}

	private long getParserID()
		{
		return SQLWork.NULL_ID;
		}

	/**
	* Clear the workload.
	* @throws WorkException if error precludes clearing the workload.
	*/
	public void clear()
		throws WorkException
		{
		}

	/**
	* Determines whether the workload is empty. As a side effect,
	* this implementation fills the work cache.
	* @return true if there are no more workload units.
	* @throws WorkException if problem determining whether empty
	*/
	public boolean isEmpty() throws WorkException
		{
		if ( m_cache.peek() != null ) // if there's something in the cache
			return false;              //  then it's not empty
		WorkDatabase dbStatement = null;
		ResultSet rs = null;
		try // get Waiting items from the queue
			{
			dbStatement = new WorkDatabase();
			String stmt = "SELECT id,idHost,url,status,depth,idSource,idParser,lmdt"
			            + " FROM " + TBL_WORK + " WHERE status = ?;";
			rs = dbStatement.executeQuery( stmt, "" + WorkRecord.WAITING );
			while (rs.next())
				{
				try // to create a new WorkRecord & put into the queue
					{
					WorkRecord wr = new SQLWork(
							rs.getLong(1),             // id
							rs.getLong(2),             // idHost
							new URL(rs.getString(3)),  // url
							rs.getString(4).charAt(0), // status
							rs.getInt(5),              // depth
							rs.getLong(6),             // idSource
							rs.getLong(7),             // idParser
							rs.getLong(8));            // lmdt
					if ( !m_cache.offer( wr ))
						break;
					}
				catch (Exception e) {}
				}
			}
		catch ( Exception ex ) { throw new WorkException(ex); }
		finally
			{
			if ( dbStatement != null)
				{
				if ( rs != null ) {try { rs.close(); } catch (Exception e) {}}
				dbStatement.closeStatement();
				}
			}
		return m_cache.peek() == null;
		}

	/**
	* Get a new URL to work on. Wait if there are no URL's currently
	* available. Return null if done with the current host. The URL
	* returned is marked as in progress.
	* @return The next URL to parse
	* @throws WorkException if the next URL could not be obtained.
	*/
	public WorkRecord getWork()
		throws WorkException
		{
		isEmpty(); // fills the cache
		return m_cache.poll();
		}

	private int computeHash(URL url)
		{
		String str = url.toString().trim();
		return (0x7FFF & str.hashCode());
		}

	public void shutdown()
		{
		WorkDatabase.disconnect();
		}
	}
