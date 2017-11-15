## Opentracing of a Python application using Jaeger on Amazon ECS
The Python app is based on https://github.com/bryanl/apptracing-py with changes to make it completely Dockerized. The Python web application running on ECS, uses various modules like Flask, jaeger-client and accesses data in a Postgresql database running in another Docker container on ECS. Finally, Jaeger runs in another container on ECS. This demo assumes that everything is being deployed in **us-east-1** AWS Region. 
Jaeger, inspired by Dapper and OpenZipkin, is a distributed tracing system released as open source by Uber Technologies and more details can be found at https://github.com/jaegertracing/jaeger. This demo uses the **jaegertracing/all-in-one** Docker image, where all components run in a single container. Jaeger also supports Cassandra 3.x, ElasticSearch as persistent storage.

## Create an AWS key Pair
```
aws --region us-east-1 ec2 create-key-pair --key-name ecs-opentrace-key1 --query 'KeyMaterial' --output text > ecs-opentrace-key1.pem
chmod 400 ecs-opentrace-key1.pem
aws --region us-east-1 ec2 describe-key-pairs
```

## Create an ECS Cluster
Launch the ECS cluster with one EC2 instance which has more than 3 GB of memory, as three containers are launched, where each container has 1 GB of memory allocated in the [ECS taskdefinition](https://github.com/cmanikandan/opentracing/blob/master/jaegerdemo/jaeger-task-definition.json).

```
ecs-cli configure -cluster ecs-opentracing-jaeger --region us-east-1
ecs-cli up --keypair ecs-opentrace-key1 --capability-iam --size 1 --instance-type t2.medium --port 22 --force
```

## Create an ECR registry for the Database and Application
Using the AWS management console or AWS CLI, create two ECR Registry entries - **psql-data** and **jaegerapp** to store the Docker images and note down the Registry names.

1. In the ECR console, choose Get Started or Create repository.
2. Enter a name for the repository, for example: **psql-data and jaegerapp**
3. Choose Next step and follow the instructions.

## Build and push the Database Image on Amazon EC2 Container Registry (ECR)
```
cd db
aws ecr get-login --no-include-email --region us-east-1

Run the docker login command that was returned in the previous step.

docker build -t psql-data .
docker tag psql-data:latest awsaccountid.dkr.ecr.us-east-1.amazonaws.com/psql-data:latest
docker push awsaccountid.dkr.ecr.us-east-1.amazonaws.com/psql-data:latest
```

## Build and push the Python application image on ECR
```
cd app
docker build -t jaegerapp .
docker tag jaegerapp:latest awsaccountid.dkr.ecr.us-east-1.amazonaws.com/jaegerapp:latest
docker push awsaccountid.dkr.ecr.us-east-1.amazonaws.com/jaegerapp:latest
```

## Create a task definition
```
Note: Go to the jaegerdemo directory and update the jaeger-task-definition.json file with the correct ECR image ids

aws ecs register-task-definition --cli-input-json file://jaeger-task-definition.json
aws ecs list-task-definitions --region us-east-1
aws ecs list-clusters
```

## Create a Cloudwatch log group and Run the Task on ECS
```
aws logs create-log-group --log-group-name ecs-log-streaming
aws ecs run-task --cluster ecs-opentracing-jaeger  --task-definition jaeger-stack --count 1 --region us-east-1
```

## Test the applications 
Note: Make sure security groups are open for the Jaeger and Application ports in the EC2 instance of the ECS Cluster

```
Access the Application to generate some tracing data -
curl http://<<IP address of the Task>>:5000/people
curl http://<<IP address of the Task>>:5000/people/47

Access the Jaeger console on a browser - http://<<IP address>>:16686
```
