## Opentracing of a Java application using Openzipkin on Amazon ECS
The sample Java app is based on https://github.com/binblee/zipkin-demo with changes to make it completely Dockerized. The Java application running on ECS, uses two simple microservices: **taskdemo and taskbackend** deployed as two Tasks on Amazon ECS. The Java application is based on the Spring Framework - https://projects.spring.io/spring-framework/ and uses Spring Cloud Sleuth - https://cloud.spring.io/spring-cloud-sleuth/ Finally, OpenZipkin runs in another container on ECS and uses the **openzipkin/zipkin** Docker image where all the components run in a single container. In addition, Zipkin also supports Cassandra, ElasticSearch and MySQL as storage backends. This demo assumes that everything is being deployed in **us-east-1** AWS Region. You can find more details on Zipkin at http://zipkin.io/

## Clone the git repository
```
git clone https://github.com/aws-samples/ecs-opentracing
```

## Create an AWS key Pair
```
aws --region us-east-1 ec2 create-key-pair --key-name ecs-opentrace-key1 --query 'KeyMaterial' --output text > ecs-opentrace-key1.pem
chmod 400 ecs-opentrace-key1.pem
aws --region us-east-1 ec2 describe-key-pairs
```

## Create an ECS Cluster
Launch the ECS cluster with one EC2 instance which has more than 3 GB of memory, as three containers are launched, where each container has 1 GB of memory allocated in the [ECS taskdefinition](https://github.com/aws-samples/ecs-opentracing/blob/master/zipkindemo/zipkinapp-taskdefinition.json).

```
ecs-cli configure -cluster ecs-opentracing-demo1 --region us-east-1
ecs-cli up --keypair ecs-opentrace-key1 --capability-iam --size 1 --instance-type t2.medium --port 22 --force
```

## Create an Amazon EC2 Container Registry (ECR) registry for the Front end app and the Backend app
Using the AWS management console or AWS CLI, create two ECR Registry entries: **demoapp** and **backend-app** to store the Docker images and note down the Registry names.

1. In the ECS console under Repositories, choose Get Started or Create repository.
2. Enter a name for the repository, for example: **demoapp and jaegerapp**
3. Choose Next step and follow the instructions.


## Build and push the demoapp Java microservice Image on ECR
```
cd tracedemo
./gradlew clean
./gradlew build

aws ecr get-login --no-include-email --region us-east-1
<< Run the docker login command  that was returned in the previous step >>

docker build -t demoapp .
docker tag demoapp:latest <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/demoapp:latest
docker push <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/demoapp:latest
```

## Build and push the demobackend Java microservice Image on ECR
```
cd tracebackend
./gradlew clean
./gradlew build

aws ecr get-login --no-include-email --region us-east-1
<< Run the docker login command  that was returned in the previous step >>

docker build -t backend-app .
docker tag backend-app:latest <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/backend-app:latest
docker push <<awsaccountid>>.dkr.ecr.us-east-1.amazonaws.com/backend-app:latest
```

## Create and register a task definition

Note: Go to the zipkindemo directory and update the [ECS taskdefinition](https://github.com/aws-samples/ecs-opentracing/blob/master/zipkindemo/zipkinapp-taskdefinition.json) file with the correct ECR image ids.

```
aws ecs register-task-definition --cli-input-json file://zipkinapp-taskdefinition.json
aws ecs list-task-definitions --region us-east-1
aws ecs list-clusters
```

## Create a Cloudwatch log group and Run the Task on ECS
```
aws logs create-log-group --log-group-name ecs-log-streaming-zipkin
aws ecs run-task --cluster ecs-opentracing-demo1  --task-definition zipkin-stack --count 1 --region us-east-1
```

## Test the applications 
Note: Make sure the security groups are open for the OpenZipkin and Application ports in the EC2 instance of the ECS Cluster

```
Test the Applications and generate some traffic -
curl http://<<IP address of the task>>:8080/
curl http://<<IP address of the task>>:8080/backend
curl http://<<IP address of the task>>:8090

Access the Zipkin homepage and check the tracing data on a browser -
http://<<IP address of the task>>:9411

```

## Clean up
1. Delete the Amazon ECS Cluster from the AWS management console or via AWS CLI as per http://docs.aws.amazon.com/AmazonECS/latest/developerguide/delete_cluster.html
2. Delete the Repository from the AWS management ECS console or via AWS CLI
