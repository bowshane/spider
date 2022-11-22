package com.shanebow.spider;
/**
* The Heaton Research Spider 
* Copyright 2007 by Heaton Research, Inc.
*
* HTTP Programming Recipes for Java ISBN: 0-9773206-6-9
* http://www.heatonresearch.com/articles/series/16/
*
* SpiderParseHTML: This class layers on top of the
* ParseHTML class and allows the spider to extract what
* link information it needs. A SpiderParseHTML class can be
* used just like the ParseHTML class, with the spider
* gaining its information in the background.
*
* This class is released under the:
* GNU Lesser General Public License (LGPL)
* http://www.gnu.org/copyleft/lesser.html
*
* @author Jeff Heaton
* @version 1.1
*
* 20100410 RTS modified addURL() to add support for the "file" protocol
* 20100418 RTS modified read() to add support for frames tag
* 20100418 RTS modified read() to handle base tag without href attribute
* 20100517 RTS modified to use SBLog
* 20100517 RTS constructor requires WorkRecord instead of URL
*/
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import com.shanebow.web.html.URLUtility;
import com.shanebow.spider.Spider;
import com.shanebow.spider.SpiderHook;
import com.shanebow.spider.work.WorkException;
import com.shanebow.spider.work.WorkRecord;
import com.shanebow.util.SBLog;
import java.io.*;
import java.net.*;

public class SpiderParseHTML extends ParseHTML
	{
	/**
	* The logger.
	*/
	private static final String MODULE="SpiderParseHTML";
	private void log( String format, Object... args )
		{
		SBLog.write( MODULE, String.format(format, args ));
		}

	/**
	* The Spider that this page is being parsed for.
	*/
	private Spider spider;

	/**
	* The URL that is being parsed.
	*/
	private WorkRecord m_work;
	private URL m_base;
	public final URL getBase() { return m_base; }
	public final void setBase(URL base) { m_base = base; }

	/**
	* The InputStream that is being parsed.
	*/
	private SpiderInputStream stream;

	/**
	* Construct a SpiderParseHTML object. This object allows the SpiderHook
	* to parse the HTML, while collecting link information in the background.
	* @param work   - the WorkRecord containing the URL that is being
	*                 parsed, this is used for relative links.
	* @param is     - the InputStream being parsed.
	* @param spider - the Spider that is parsing.
	* @throws WorkException if an error occurred in the workload management.
	*/
	public SpiderParseHTML(WorkRecord work, SpiderInputStream is, Spider spider)
		throws WorkException
		{
		super(is);
		this.stream = is;
		this.spider = spider;
		m_work = work;
		m_base = work.getURL();
		}

	/**
	* Get the InputStream being parsed.
	* @return The InputStream being parsed.
	*/
	public SpiderInputStream getStream() { return this.stream; }

	/**
	* Read all characters on the page. This will discards the characters, but
	* simultaneously causes this object to examine the tags to find links.
	* @throws IOException upon I/O error.
	*/
	public void readAll() throws IOException
		{
		while (read() != -1)
			; // do nothing loop
		}

	/**
	* Read a single character and pass it on to the caller after
	* processeing any tags that are used for navigation. This allows
	* the spider to transparently gather its links.
	* @return The character read (a value of "0" indicates an HTML tag)
	* @throws IOException upon I/O error.
	*/
  @Override
	public int read() throws IOException
		{
		int result = super.read();
		if (result == 0)
			{
			HTMLTag tag = getTag();
			if (tag.getName().equalsIgnoreCase("a"))
				{
				String href = tag.getAttributeValue("href");
				handleA(href);
				}
			else if (tag.getName().equalsIgnoreCase("img"))
				{
				String src = tag.getAttributeValue("src");
				addURL(src, SpiderHook.URLType.IMAGE);
				}
			else if (tag.getName().equalsIgnoreCase("style"))
				{
				String src = tag.getAttributeValue("src");
				addURL(src, SpiderHook.URLType.STYLE);
				}
			else if (tag.getName().equalsIgnoreCase("link"))
				{
				String href = tag.getAttributeValue("href");
				if ( href.endsWith(".css"))
					addURL(href, SpiderHook.URLType.STYLE);
				else if ( href.endsWith(".ico"))
					addURL(href, SpiderHook.URLType.IMAGE);
				else
					addURL(href, SpiderHook.URLType.SCRIPT);
				}
			else if (tag.getName().equalsIgnoreCase("script"))
				{
				String src = tag.getAttributeValue("src");
				if ( src != null )
					addURL(src, SpiderHook.URLType.SCRIPT);
				}
			else if (tag.getName().equalsIgnoreCase("frame"))
				{
				String src = tag.getAttributeValue("src");
				addURL(src, SpiderHook.URLType.HYPERLINK);
				}
			else if (tag.getName().equalsIgnoreCase("base"))
				{
				String href = tag.getAttributeValue("href");
				if ( href != null )
					m_base = new URL(m_base, href);
				}
			}
		return result;
		}

	/**
	* This method is called when an anchor(A) tag is found.
	* 
	* @param href - the link found.
	* @throws IOException upon I/O error.
	*/
	private void handleA(String href)
		throws IOException
		{
		if ( href == null )
			return;
		String ref = href.trim().toLowerCase();
		if ( !URLUtility.containsInvalidURLCharacters(href)
		&&   !ref.startsWith("javascript:")
		&&   !ref.startsWith("rstp:")
		&&   !ref.startsWith("rtsp:")
		&&   !ref.startsWith("news:")
		&&   !ref.startsWith("irc:")
		&&   !ref.startsWith("mailto:"))
			addURL(href.trim(), SpiderHook.URLType.HYPERLINK);
		}

	/**
	* Used internally, to add a URL to the spider's workload.
	* @param u    - the URL of the link to add
	* @param type - the URLType of the link
	* @throws IOException upon I/O error
	*/
	private void addURL(String u, SpiderHook.URLType type)
		throws IOException
		{
		if ( u == null )
			return;

		try
			{
			URL url = URLUtility.constructURL(m_base, u, true);
			url = WorkRecord.toURL(url.toString());
			String lcProtocol = url.getProtocol().toLowerCase();
			if (lcProtocol.equals("http")
			||  lcProtocol.equals("https")
			||  lcProtocol.equals("file")) // base.getProtocol())
				{
				if (this.spider.getHook().urlFound(url, m_work.getURL(), type))
					{
					try { this.spider.addURL(url, m_work ); }
					catch (WorkException e) { throw new IOException(e.getMessage()); }
					}
				}
		//	else log.format( "BAD PROTOCOL: %s URL(%s)", url.getProtocol(), url );
			}
		catch (MalformedURLException e) { log( "Malformed URL found: %s", u); }
		catch (WorkException e)         { log( "Invalid URL found:" + u); }
		}
	}
