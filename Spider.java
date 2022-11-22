package com.shanebow.spider;
/********************************************************************
* Copyright (c) 2010 Richard T. Salamone, Jr.  All rights reserved.
*
* Spider: This is the main class that implements the Shane Bow Spider.
* This spider initially evolved from debugging the Heaton Research Spider,
* developed by Jeff Heaton and released into the public domain. After
* massive redesign of the approaches to concurrency, logging, workload
* management, and database design it retains little semblence of its
* predecessor.
*
* @author Rick Salamone (based on work by Jeff Heaton)
* @version 1.0
*******************************************************/
import com.shanebow.spider.work.*;
import com.shanebow.util.SBLog;
import java.net.*;
import java.util.Date;
import java.util.concurrent.*;

public class Spider // extends Thread
	{
	public static final String MODULE="SPIDER";

	private final SpiderOptions  m_options;
	private final SpiderHook     m_hook;
	private final Workload       m_workload;   // list of work(url + status)
	private final SpiderExecutor m_threadPool; // manages the thread pool

	private Date m_startTime; // when the spider began
	private Date m_stopTime; // when the spider finished

	private boolean m_canceled = false;

	public Workload      getWorkload(){ return m_workload; }
	public SpiderHook    getHook()    { return m_hook; }
	public SpiderOptions getOptions() { return m_options; }

	public Spider(SpiderOptions options, SpiderHook hook )
		{
		m_hook = hook;
		m_options = options;
		m_threadPool = new SpiderExecutor(this);

		Workload w = null;
		try
			{
			Class parserClass = options.parserClass;
			SpiderWorker.htmlParserConstructor = parserClass.getConstructor(
			        WorkRecord.class, SpiderInputStream.class, Spider.class );
	//		        java.net.URL.class, SpiderInputStream.class, Spider.class );

			w = (Workload) Class.forName(options.workload).newInstance();
			w.init(this);
			}
		catch (Exception e)
			{
			System.err.format ( "Startup failed: %s\nEXITING\n", e.toString());
			System.exit(1);
			}
		m_workload = w;
		m_hook.initialized(this);
		}

	private void log( String format, Object... args )
		{
		SBLog.write( MODULE, String.format(format, args ));
		}

	public void process()
//	public void run()
		{
		this.m_startTime = new Date();
		log( "running at " + this.m_startTime.toString());
		guardedJoy();
		m_workload.shutdown();
		m_threadPool.shutdown();
		this.m_stopTime = new Date();
		log( "exiting at " + this.m_stopTime.toString());
		}
	public synchronized void cancel() { m_canceled = true; notifyAll(); }
	
	public synchronized void guardedJoy()
		{
		//This guard only loops once for each special event,
		// which may not be the event we're waiting for.
		BlockingQueue<Runnable> q = m_threadPool.getQueue();
		while( !m_canceled )
			{
			try
				{
				try
					{
					if ( !m_workload.isEmpty())
						{
						WorkRecord work;
						while ((q.remainingCapacity() > 1)
						   && ((work = m_workload.getWork()) != null))
							{
					//		log ( "got work: %s", work );
							Runnable worker = new SpiderWorker(this, work );
							m_threadPool.execute(worker);
							}
						}
					else if ( m_threadPool.getActiveCount() == 0 )
						{
						log ( "spider finished: normal termination condition" );
						break;
						}
			//		log( "checking... " );
					}
				catch (WorkException we) { log("getWork exception: " + we ); }
		//		log( "waiting");
				wait();
				}
			catch (InterruptedException e) { log( "interupted " + e.toString()); }
			}
		log( "EXIT: " + (m_canceled ? "canceled" : "work complete"));
		while ( m_threadPool.getActiveCount() > 0 ) // cancelled, wait for stragglers
			{
			try
				{
				log( "waiting for %d stragglers", m_threadPool.getActiveCount());
				wait();
				}
			catch (InterruptedException e) {}
			}
		}

	/**
	* Add a URL for processing. Accepts a SpiderURL.
	* @throws WorkException
	*/
	public boolean addURL( String url )
		{
		try { return addURL( new URL(url), null ); }
		catch (Exception e) { return false; }
		}

	public synchronized boolean addURL( URL url, WorkRecord source )
		throws WorkException
		{
		if ((m_options.maxDepth != -1)   // check the depth
		&&  (source != null)
		&&  (source.getDepth() >= m_options.maxDepth))
			{
			// log( "TOO DEEP addURL(%s, _, %d) max depth: %d", url,
			//        source.getDepth()+1, m_options.maxDepth );
			return false;
			}

		if ( m_workload.add(url, source, WorkRecord.WAITING))
			{
			// int depth = (source != null)? source.getDepth() + 1 : 0; 
			m_hook.urlAdded(url, (source != null)? source.getURL() : null );
			notifyAll();
			return true;
			}
	// else log( "failed workload.add(%s,%s)", url, source );
		return false;
		}

	public synchronized void checkForWork()
		{
	//	log ( "checkForWork()" );
		notifyAll();
		}
  /**
   * Generate basic status information about the spider.
   * 
   * @return The status of the spider.
   */
	public String getStatus()
		{
		StringBuilder result = new StringBuilder(); 
		result.append("Start time:");
		result.append(this.m_startTime.toString());
		result.append('\n');
		result.append("Stop time:");
		result.append(this.m_stopTime.toString());
		result.append('\n');
		result.append("Minutes Elapsed:");
		result.append((this.m_stopTime.getTime() - this.m_startTime.getTime()) / 60000);
		result.append('\n');

		return result.toString();
		}
	}
