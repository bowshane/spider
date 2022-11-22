package com.shanebow.spider;
/********************************
* example from
* http://java.sun.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html
*
* Most extensions of ThreadPoolExecutor override one or more of the protected
* hook methods. Here is a subclass that adds a simple pause/resume feature:
*
* example from
* http://java.sun.com/javase/6/docs/api/java/util/concurrent/ExecutorService.html
*
* Interface ExecutorService extends Executor:
* An Executor that provides methods to manage termination and methods that can
* produce a Future for tracking progress of one or more asynchronous tasks.
*
* We use Class ThreadPoolExecutor implements ExecutorService, Executor
*
* An ExecutorService can be shut down, which will cause it to reject new tasks.
* Two different methods are provided for shutting down an ExecutorService.
* The shutdown() method will allow previously submitted tasks to execute before
* terminating, while the shutdownNow() method prevents waiting tasks from starting
* and attempts to stop currently executing tasks. Upon termination, an executor
* has no tasks actively executing, no tasks awaiting execution, and no new tasks
* can be submitted. An unused ExecutorService should be shut down to allow
* reclamation of its resources.
*
*
******************************/
import com.shanebow.util.SBLog;
import java.util.concurrent.*;

public class SpiderExecutor extends ThreadPoolExecutor
	{
	public static final String MODULE="Executor";

	private final Spider m_spider;

	public SpiderExecutor() throws IllegalArgumentException
		{
 		super(0, 0, 0, TimeUnit.SECONDS, null ); 
		throw new IllegalArgumentException("Spider Executor");
		}

	public SpiderExecutor( Spider spider )
		{
		super( spider.getOptions().corePoolSize, spider.getOptions().maximumPoolSize,
		       spider.getOptions().keepAliveTime, TimeUnit.SECONDS,
		       new ArrayBlockingQueue<Runnable>(spider.getOptions().poolQueueSize));
		m_spider = spider;
		// Set up a handler for rejected tasks (interface RejectedExecutionHandler):
		//  * ThreadPoolExecutor.AbortPolicy (default): the handler throws a runtime
		//    RejectedExecutionException upon rejection.
		//  * ThreadPoolExecutor.CallerRunsPolicy: is a handler that runs the rejected task
		//    directly in the calling thread of the execute method, unless the executor has
		//    been shut down, in which case the task is discarded. 
		//  * ThreadPoolExecutor.DiscardPolicy: simply drop a task that cannot be executed.
		//  * ThreadPoolExecutor.DiscardOldestPolicy: task at the head of the work queue is
 		//    dropped, and then execution is retried (which can fail again, etc.)
		//  * Roll your own: You may define and use other RejectedExecutionHandler classes.
		//    Doing so requires some care especially when policies are designed to work only
		//    under particular capacity or queuing policies.
     //	this.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		}

	private void log( String format, Object... args )
		{
		// SBLog.write( MODULE, String.format(format, args ));
		}

	protected void afterExecute(Runnable r, Throwable t )
		{
		super.afterExecute(r,t);
		log ( "afterExecute(%s,%s)", r.toString(),
		    ((t == null)? "-" : t.getClass().getSimpleName() + " " + t.toString()));
		m_spider.checkForWork();
		}

	/**
	* The following method shuts down an ExecutorService in two phases,
	* first by calling shutdown to reject incoming tasks, and then
	* calling shutdownNow, if necessary, to cancel any lingering tasks: 
	*/
	void shutdownAndAwaitTermination()
		{
	ExecutorService pool = this;
		pool.shutdown(); // Disable new tasks from being submitted
		try
			{
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS))
				{
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
				}
			}
		catch (InterruptedException ie)
			{
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			}
		}
	}
