# globalbank

This Lagom project is an ES/CQRS based project for money transfers between accounts.  
It has been designed with the capability to run in a distributed environment.
The bookentry microservice is for the write side while the bookentry-stream
microservice is for the read side.  To avoid reading stale data that may potentially be
introduced as a result of eventual consistency (which is usually just a few seconds), 
API clients can use the write side to query real-time, up to date account state and balances.

To run the project, execute the following command:
    
    sbt runAll
    
To test the project:

    sbt test

To do performance test:

    sbt gatling:test