# Implementation

There are three classes implementing different types of load balancers according to the specifications.  

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
- 




