> 
> ORIGINAL PROBLEM SPECIFICATIONS
> https://github.com/YamanNoaiseh/InsightDataEngineering_CodingChallenge/tree/7418f6838e213e42d33ec287f22f1a0d701b9ca0
> 
> ====================================================================================================================



A code for Insight Data Engineering Fellowship program application.


Author: Yaman Noaiseh


Implementation Tools:
- Tools used: Java SE 7 on Eclipse MARS.2

- External libraries (for handling JSON files): I used TWO Java libraries, native to the Java ecosystem. Both libraries exist in the Java Enterprise Edition.
 
- Library 1: [ javax.json-1.0.jar ]
http://www.java2s.com/Code/Jar/j/Downloadjavaxjson10jar.htm 

Direct download: 

http://www.java2s.com/Code/JarDownload/javax.json/javax.json-1.0.jar.zip 

This library is indirectly used as it provides services to the other library.

- Library 2: [javax.json-api-1.0.jar ]
http://www.java2s.com/Code/Jar/j/Downloadjavaxjsonapi10jar.htm 

Direct download: 

http://www.java2s.com/Code/JarDownload/javax.json/javax.json-api-1.0.jar.zip 



Data structures used:

•	HashMap: it provides constant-time performance – O(1) – for the basic operations needed.

•	HashSet: to store graph edges that fail in the time check (60-seconds window) before removing them from the graph.

•	PriorityQueue: it has the best balance between time and space complexity. While the “Heap Sort” algorithm takes O(n log(n)) time complexity, it provides O(1) space complexity, which is very valuable in handling large amounts of data. Furthermore, my code iterates through only half of the items stored in the PriorityQueue object to find the median.


Implementation Notes:

- I created my own implementation of the Graph data structure, utilizing existing Java interfaces. This is undirected graph as the edge “userX <-> userY” is the same as the edge “userY <-> userX”.

- The code ignores blank lines within the input file, if any.

- I wrote a simple method to truncate the median, rather than calling external methods.
