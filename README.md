# Pipe
A producer/consumer pipeline and a test framework for it.

This was an assignment for an operating systems course demonstrating the use of various synchronization techniques. I created a producer/consumer pipeline that can be set to use either binary sempahores (mutex locks) and busy waiting or counting semaphores without busy waiting to grant access.

The test framework creates a pipeline and producers/consumers according to a set of parameters and logs metrics for the given parameterization.
