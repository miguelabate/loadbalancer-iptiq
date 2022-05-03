# Load balancer

The code implements the behaviour of a loadbalancer as per requirements.   

#  Compile and run tests
To compile and run, you need at least Java 11 and maven installed.

In the command prompt tun:  
```
mvn clean package
```

#  Run main example

There is a main class providing an example of running a load balancer, registering providers and making calls.

To run it, do:
```
mvn compile exec:java -Dexec.mainClass="com.iptiq.ExampleLBRoundRobinEnhanced"
```

# Implementation

There are three classes implementing incremental requirements (steps) from the specifications.  

Steps 1,2 and 3 are implemented in LoadBalancerRandom.  

Steps 1,2, 3 and 4 are implemented in LoadBalancerRoundRobin.  

Steps 1,2,3,4,5,6,7 and 8 are implemented in LoadBalancerRoundRobinEnhanced.  

So the final implementation would be LoadBalancerRoundRobinEnhanced.  

#  Notes

- The LoadBalancer and Provider classes/interfaces use generics for the return type of get()
so it can accommodate any type that the services retrieves.
- The Provider also implements a method called getId() (apart from the get() and check()) that retrieves 
the assigned id of the provider.
- The methods in the different implementations of the LoadBalancer use a reentrant lock when working 
on the list of providers to achieve Thread safety.
- Manual node inclusion/exclusion is implemented via the interface ManualNodeManager. When implemented it provides two methods to include/exclude nodes given the provider id.
- HeartBeat check: upon creation of the loadbalancer, a thread is created 
to periodically run the heartbeat check on each node. To gracefully stop this thread, it's necessary to call the shutdown() method in the loadbalancer
- Cluster capacity limit: as part of the node stats, I store the current count of running executions on that node. So, if the nodes reaches the max, it doesn't allow more calls on it. 
If all nodes are to the max, the whole cluster will reject the new call. 





