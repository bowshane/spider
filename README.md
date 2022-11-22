# Spider

This java application will crawl web pages starting at a given url. At
 each page arbitrary custom work can be performed (e.g. scraping data).
 Our main use was to check for broken links on our web sites.

This spider initially evolved from debugging the *Heaton Research Spider*,
 developed by Jeff Heaton and released into the public domain. After
 massive redesign of the approaches to concurrency, logging, workload
 management, and database design it retains little semblence of its
 predecessor.

The Heaton Research Spider has some good ideas and served as a starting point
for developing a beta version of the ShaneBow Spider. Unfortunately, it is
flawed in both design and function.

The code dates back to Feb 2010 and was previously archived in a private SVN.

Executor - decide on rejected task operation

Workload - shutdown database connection is ok, or seeing if we are out of memory

SpiderParseHTML - add routine setBase(URL base) to handle case of redirects

SQL
*  shutdown
*  create error table
*  create host table
*  create parser table


Options - 