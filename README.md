## Opentracing
Opentracing is a vendor-neutral open standard for distributed tracing. Please see http://opentracing.io for more details. Trace distributed applications written in Java and Python deployed on Amazon EC2 Container Service (ECS) using Opentracing providers like OpenZipkin and Jaeger. Libraries are currently available in 9 languages like Go, JavaScript, Java, Python, Ruby, PHP, Objective-C, C++, C#.

Running a single container on a single server is easy. ECS is a cluster management service that helps you manage a group of clusters through a graphical user interface or by accessing a command line. With ECS you can install, operate, and scale your own cluster management infrastructure.

This github repo demonstrates tracing of two sample applications, one in Java and another in Python deployed on ECS, using two opensource OpenTracing providers, OpenZipkin - http://zipkin.io/ and Jaeger - https://uber.github.io/jaeger/. Both the Applications and the Opentracing providers will run as Tasks on an Amazon ECS cluster.

1. [Tracing of an Java app on ECS using OpenZipkin](https://github.com/cmanikandan/opentracing/tree/master/zipkindemo)
2. [Tracing of an Python App on ECS using Jaeger](https://github.com/cmanikandan/opentracing/tree/master/jaegerdemo)

## Prerequisites

You will need to have the latest version of the AWS CLI and the following software:

1. [Installing the AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
2. [Installing Gradle](https://gradle.org/install/) - For Java apps
3. [Installing Docker](https://docs.docker.com/engine/installation/)
4. [Installing Python](https://www.python.org/downloads/) - For Python apps
5. [Installing latest OpenJDK](http://openjdk.java.net/install/) - For Java apps





