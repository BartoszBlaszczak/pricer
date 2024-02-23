This application:
- is written in 
  - Java 21 (according to [requirements](REQUIREMENTS.pdf))
  - Spring Boot framework: the most common framework for java web apps (however, there are better solutions)
- uses MongoDB as a database. There is no need to use any SQL db
- has simple source packaging because the logic of this application is negligible (no need for e.g., hexagonal architecture that should be used for more complicated domain)
- has no security layer because of simplicity and the nature of the demo. 
Of course, for the real production service, there should be security level for at least creating, updating and deleting price lists.
The security solution should be adapted to the environment.