package com.shanebow.spider.work.sql;
/**
* Copyright (c) 2010 by Richard T. Salamone, Jr.  All rights reserved.
* 
* SQLWork: Extends WorkRecord to allow a change to a record's status be
* committed to the database.
* 
* @author Rick Salamone
* @version 1.0
*/
import com.shanebow.spider.work.WorkException;
import com.shanebow.spider.work.WorkRecord;

public class SQLWork extends WorkRecord
	{
	// Static interface
	static private WorkDatabase _db; // a SQL statement shared to update work status/lmdt
	static void setWorkDatabase(WorkDatabase db) { _db = db; }

	public SQLWork() throws WorkException { super(); }

	public SQLWork( long id, long idHost, java.net.URL url, char status,
	           int depth, long idSource, long idParser, long lmdt )
		{
		super( id, idHost, url, status, depth, idSource, idParser, lmdt );
		}

	@Override
	public synchronized void setStatus(char status)
		throws WorkException
		{
		super.setStatus(status);
		try
			{
			String stmt = "UPDATE " + SQLWorkload.TBL_WORK
			            + " SET status = ?, lmdt = ? WHERE id = ?;";
			_db.executeUpdate( stmt, "" + status, getLMDT(), getID());
			}
		catch ( Exception ex ) { throw new WorkException(ex); }
		}
	}
