package com.shanebow.spider.work.sql;
/**
* Copyright 2010 by Richard T. Salamone, Jr.
* 
* WorkDatabase: This class provides an interface to the physical
* database that stores every URL ever visited by the spider.
* 
* @author Rick Salamone
* @version 1.0
*/
import com.shanebow.spider.SpiderOptions;
import com.shanebow.util.SBLog;
import java.sql.*;
import java.net.*;

public class WorkDatabase
	{
	private static Connection		_connection = null;

	public static void disconnect()
		{
		try { if ( _connection != null ) _connection.close(); }
		catch ( Exception e ) {}
		finally { _connection = null; }
		}

	public static void connect( SpiderOptions opts )
		{
		try  { Class.forName( opts.dbClass ); } // Load the driver
		catch ( ClassNotFoundException cnfex ) { bail( "load driver", cnfex ); }

		try // connect to the database
			{
			_connection = DriverManager.getConnection( opts.dbURL, opts.dbUID, opts.dbPWD );
			_connection.setAutoCommit(true);
			}
		catch ( SQLException sqlex ) { bail( "connect", sqlex ); }
		}

	private Statement     m_statement = null;

	public WorkDatabase()
		throws SQLException
		{
		m_statement = _connection.createStatement();
		}

	private static final void log ( String caller, String msg )
		{
	//	SBLog.write ( SBLog.APP, caller, msg );
		}

	public ResultSet executeQuery ( String stmt, Object... parameters )
		throws SQLException
		{
		String sql = stuffParameters( stmt, parameters );
		log ( "QUERY", sql );
		return m_statement.executeQuery( sql );
		}

	public int executeUpdate ( String stmt, Object... parameters )
		throws SQLException
		{
		String sql = stuffParameters( stmt, parameters );
		log ( "UPDATE", sql );
		//	log( "executeUpdate", _connection.nativeSQL( sql ));
		int result = m_statement.executeUpdate( sql );
		log( "executeUpdate", "Insert " + (( result == 1 )? "success" : "failure"));
		return result;
		}

	public void closeStatement()
		{
		try
			{
			if ( m_statement != null )
				m_statement.close();
			}
		catch ( SQLException sqlex ) { 	logSQLError ( "closeStatement", sqlex ); }
		finally { m_statement = null; }
		}

	private String stuffParameters( String template, Object... parameters )
		{
		String[] pieces = template.split("\\?");
		String it = pieces[0];
		for ( int i = 0; i < parameters.length; i++ )
			{
			String field = "";
			if ( parameters[i] == null )
				field = "";
			else if (parameters[i] instanceof String)
				field = "'" + parameters[i].toString() + "'";
			else
				field = parameters[i].toString();
			it += field + pieces[i+1];
			}
// System.out.println("SQL: " + it);
		return it;
		}

	private static void bail( String msg, Exception ex )
		{
		if ( ex != null )
			{
			if ( ex instanceof SQLException )
				logSQLError ( msg, (SQLException)ex );
			SBLog.error ( msg, "Error: " + ex.toString() );
			}
		else SBLog.write ( msg );
		try
			{
			if ( _connection != null )
				_connection.close();
			_connection = null;
			}
		catch ( SQLException sqlex ) { 	logSQLError ( "shutdown", sqlex ); }
		System.exit ((ex == null) ? 0 : 1 );
		}

	private static final void logSQLError ( String caller, SQLException sqlex )
		{
		SBLog.error ( caller, " SQL Error: " + sqlex.toString() );
		while ((sqlex = sqlex.getNextException()) != null)
			SBLog.error ( "&", sqlex.getMessage());
		}
	}
