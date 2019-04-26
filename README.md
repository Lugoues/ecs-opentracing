## License

This library is licensed under the Apache 2.0 License.

 ## Opentracing of Java and Python Applications on Amazon Elastic Container Service (ECS)
Opentracing is a vendor-neutral open standard for distributed tracing and is a Cloud Native Computing Foundation member project. Please see http://opentracing.io for more details. Trace distributed applications written in Java and Python deployed on Amazon Elastic Container Service (ECS) using Opentracing tracers like CNCF Jaeger among others, the full list is available at https://opentracing.io/docs/supported-tracers/. OpenTracing abstracts away the differences among numerous tracer implementations. This means that instrumentation would remain the same irrespective of the tracer system being used by the developer. Libraries are currently available in 9 languages including Go, JavaScript, Java, Python, Ruby, PHP, Objective-C, C++ and C#.

From https://opentracing.io/docs/overview/what-is-tracing/ - Distributed tracing, also called distributed request tracing, is a method used to profile and monitor applications, especially those built using a microservices architecture. Distributed tracing helps pinpoint where failures occur and what causes poor performance.

Running a single docker container on a single server is easy. Amazon ECS is a cluster management service that helps you manage a group of clusters through a graphical user interface or by accessing a command line. With ECS you can install, operate, and scale your own cluster management infrastructure.

This github repo demonstrates tracing of two sample applications, one in Java and another in Python deployed on ECS, using an OpenTracing tracer, CNCF Jaeger - https://www.jaegertracing.io/. Both the Applications and the Opentracing tracers will run as containers in a single Task on an Amazon ECS cluster.

1. [Tracing of an Java app on ECS using Jaeger](https://github.com/aws-samples/ecs-opentracing/tree/master/javaapp)
2. [Tracing of an Python App on ECS using Jaeger](https://github.com/aws-samples/ecs-opentracing/tree/master/pythonapp)

Note: If you create a single ECS Cluster, please run one app at a time, as there will be port conflicts, when both Java and Python applications are run on the same cluster. Also, you will incur billing charges in your AWS account, when you run these samples.

## Prerequisites

You will need to have the latest version of the AWS CLI, AWS ECS CLI and the following software installed on your laptop or development environment like AWS Cloud9:

1. [Installing the AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
2. [Installing the AWS ECS CLI](https://github.com/aws/amazon-ecs-cli)
3. [Installing Gradle](https://gradle.org/install/) - For building Java apps
4. [Installing Docker](https://docs.docker.com/engine/installation/)
5. [Installing Python](https://www.python.org/downloads/) - For Python apps
6. [Installing latest OpenJDK](http://openjdk.java.net/install/) or Amazon Corretto (https://aws.amazon.com/corretto/) - For Java apps

## Creating an ECS Cluster with one EC2 Node

## Clone the git repository
```
git clone https://github.com/aws-samples/ecs-opentracing
```

## Create an AWS key Pair
```
cd Opentracing
aws --region eu-west-1 ec2 create-key-pair --key-name ecs-opentrace-key2 --query 'KeyMaterial' --output text > ecs-opentrace-key2.pem
chmod 400 ecs-opentrace-key2.pem
aws --region eu-west-1 ec2 describe-key-pairs
```

## Create an ECS Cluster
Create the ECS cluster using the [AWS ECS CLI](http://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_CLI_installation.html) with one EC2 instance which has more than 3 GB of memory, as three containers are launched, where each container has 1 GB of memory allocated in the [ECS taskdefinition](./jaeger-task-definition.json).

```
ecs-cli configure -cluster ecs-opentracing-jaeger --region eu-west-1
ecs-cli up --keypair ecs-opentrace-key2 --capability-iam --size 1 --instance-type t2.large --port 22 --force --region eu-west-1
```
Wait till you get a "Cluster creation succeeded" message.
