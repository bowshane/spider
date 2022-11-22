package com.shanebow.spider.work;
/**
* The Heaton Research Spider 
* Copyright 2007 by Heaton Research, Inc.
* 
* HTTP Programming Recipes for Java ISBN: 0-9773206-6-9
* http://www.heatonresearch.com/articles/series/16/
* 
* WorkException: This exception is thrown when the
* workload manager encounters an error.
* 
* This class is released under the:
* GNU Lesser General Public License (LGPL)
* http://www.gnu.org/copyleft/lesser.html
* 
* @author Jeff Heaton
* @version 1.1
*/
public class WorkException extends Exception
	{
	/**
	 * Serial id for this class.
	 */
	private static final long serialVersionUID = 1L;

  /**
   * Construct a WorkException with the specified message.
   * @param message The exception message.
   */
	public WorkException(String message) { super(message); }

  /**
   * Called to wrap another exception as a WorkException.
   * @param t An exception to be wrapped.
   */
	public WorkException(Throwable t)
		{
		super(t);
		}
	}
