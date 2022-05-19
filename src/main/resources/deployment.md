1.) Linux Environment.

Deploy to a kubernetes cluster or similar. 

2.) Monitor

Kubernetes has many plugins that provide advanced observability over Kubernetes clusters and workloads.

3.) Doc Reporter 

a.) Consider having separate microservices for reporter/analyzer that can scale independently based on load. 
Nest behind spring cloud gateway secured with an idp such as Keycloak/Okta.
Consider using a message bus such as Kafka for microservice communication. 
b.) Reactor core provides many features for baking in resilience
such as doOnError() or onErrorReturn() or Hystrix commands for non reactive stack may be a good choice.
c.) Consider using https for microservice communication.