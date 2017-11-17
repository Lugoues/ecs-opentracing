## Opentracing of a Python application using Jaeger on Amazon ECS
The Python app is based on https://github.com/bryanl/apptracing-py with changes to make it completely Dockerized. The Python web application running on ECS, uses various python modules like flask, jaeger-client and accesses data in a Postgresql database running in another Docker container on ECS. Finally, Jaeger runs in another container on ECS. This demo assumes that everything is being deployed in **us-east-1** AWS Region. 
Jaeger, inspired by [Dapper](https://research.google.com/pubs/pub36356.html) and OpenZipkin, is a distributed tracing system released as open source by Uber Technologies and more details can be found at https://github.com/jaegertracing/jaeger. This demo uses the **jaegertracing/all-in-one** Docker image, where all components run in a single container. Jaeger also supports Cassandra 3.x, ElasticSearch as persistent storage.

## Clone the git repository
```
git clone https://github.com/aws-samples/ecs-opentracing
```

## Create an AWS key Pair
```
aws --region us-east-1 ec2 create-key-pair --key-name ecs-opentrace-key2 --query 'KeyMaterial' --output text > ecs-opentrace-key2.pem
chmod 400 ecs-opentrace-key2.pem
aws --region us-east-1 ec2 describe-key-pairs
```

## Create an ECS Cluster
Create the ECS cluster using the [AWS ECS CLI](http://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_CLI_installation.html) with one EC2 instance which has more than 3 GB of memory, as three containers are launched, where each container has 1 GB of memory allocated in the [ECS taskdefinition](./jaeger-task-definition.json).

```
ecs-cli configure -cluster ecs-opentracing-jaeger --region us-east-1
ecs-cli up --keypair ecs-opentrace-key2 --capability-iam --size 1 --instance-type t2.medium --port 22 --force
```

## Create an Amazon EC2 Container Registry (ECR) registry for the Database Microservice and the Python application Microservice
Using the AWS management console or AWS CLI, create two ECR Registry entries: **psql-data** and **jaegerapp** to store the Docker images.

```
aws ecr create-repository --repository-name psql-data
aws ecr create-repository --repository-name jaegerapp
```

## Build and push the Database image to ECR
```
cd jaegerdemo
cd db
aws ecr get-login --no-include-email --region us-east-1

<<Run the docker login command that was returned in the previous step>>

docker build -t psql-data .
aws ecr describe-repositories
docker tag psql-data:latest <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/psql-data:latest
docker push <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/psql-data:latest
```

## Build and push the Python application image to ECR
```
cd jaegerdemo
cd app
docker build -t jaegerapp .
aws ecr describe-repositories
docker tag jaegerapp:latest <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/jaegerapp:latest
docker push <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/jaegerapp:latest
```

## Create a task definition

Note: Update the [jaeger-task-definition.json](./jaeger-task-definition.json) file and replace <<awsaccountid>> with the correct AWS account id.
```
cd jaegerdemo
aws ecr describe-repositories
aws ecs register-task-definition --cli-input-json file://jaeger-task-definition.json
aws ecs list-task-definitions --region us-east-1
aws ecs list-clusters
```

## Create a Cloudwatch log group (if you have previously not created a log group called ecs-log-streaming) and Run the Task on ECS
```
aws logs create-log-group --log-group-name ecs-log-streaming
aws ecs run-task --cluster ecs-opentracing-jaeger  --task-definition jaeger-stack --count 1 --region us-east-1
```

## Test the applications 
Note: Make sure security groups are open for Inbound for the Jaeger and Application TCP ports:5000,16686 in the EC2 instance of the ECS Cluster

```
Access the Application to generate some tracing data -
curl http://<<IP address of the Task>>:5000/people
curl http://<<IP address of the Task>>:5000/people/47

Access the Jaeger console on a browser - http://<<IP address>>:16686
```

## Clean up
1. Delete the Amazon ECS Cluster from the AWS management console or via AWS CLI as per http://docs.aws.amazon.com/AmazonECS/latest/developerguide/delete_cluster.html
2. Delete the EC2 instance launched via the ECS Cluster
2. Delete the Repository from the AWS management ECS console or via AWS CLI
3. Delete log group
```
aws logs delete-log-group --log-group-name ecs-log-streaming-zipkin
```
