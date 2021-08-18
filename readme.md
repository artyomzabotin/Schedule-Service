# Scheduler Service

## Description
Scheduler service - is a microservice that is created to track working time. It accumulates reports with work description, that are sent through
Kafka messages from [Worker Service](https://github.com/artyomzabotin/Worker-Service).
In future user will be able to send notification to users, collect statistics per shops and many other features. Now there
are some REST endpoints to retrieve schedules.

## Getting started
Copy and start these applications: 

[Worker Service](https://github.com/artyomzabotin/Worker-Service)

[Scheduler Service](https://github.com/artyomzabotin/Schedule-Service)

[Service Registry](https://github.com/artyomzabotin/Service-Registry)

Install locally [Kafka](https://kafka.apache.org/downloads) and start Zookeeper and Kafka servers. Then you need to run Redis and Redis Commander using Docker:

````
docker-compose -f docker-compose.yml -up
````

Now you are ready to use application.

