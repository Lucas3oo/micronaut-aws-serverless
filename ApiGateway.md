
# Go serverless with AWS API Gateway, AWS Lambda, AWS SAM and Micronaut (or SpringBoot)

To create cloud native solutions you can either package your microservice in a container or for a serverless approach
use AWS Lambda. In this article the serverless approach will be explored.

For a serverless setup AWS offers the following services for the web tier:
* AWS API Gateway
* AWS Lambda
* AWS Serverless Application Model for easy deployment of the above.

The latest version (V2) of AWS API Gateway is cheaper (~70%) and faster. It is called the [API Gateway HTTP API]
(https://aws.amazon.com/blogs/compute/announcing-http-apis-for-amazon-api-gateway/) as compared to the previous "API Gateway REST API".

There are some aspects of the combination of API Gateway and AWS Lambda that aren't obvious when you start.
Namely mapping between the API Gateway HTTP requests and the backing lambda. The other is how the HTTP is packaged when passed over to the lambda.

For the actually Java implementation there are some option like [SpringBoot](https://spring.io/projects/spring-boot), [Quarkus](https://quarkus.io) and [Micronaut](https://micronaut.io).

The basic idea is to front the microservice with the API Gateway and package the microservice into a AWS Lambda.
In this way you only pay for the invocations of the service and you have no EC2 or Auto scaling groups or container OS to worry about.

## Transforming Lambda events (payload) to HttpRequests in your lambda handler
If you write your lambda using the REST support in SpringBoot or Micronaut
then you are expecting a HttpRequest object at your REST controller.
But the HTTP request has already been handled by the API Gateway so
instead the API Gateway will transform the HTTP request to an Lambda event.
Luckily both SpringBoot and Micronaut have solutions for converting the Lambda event back to a HttpRequest object.

In Spring you need to implement your own proxy of `com.amazonaws.services.lambda.runtime.RequestStreamHandler`
that in turn creates the lambda handler for transforming the event.

```Java
public class StreamLambdaHandler implements RequestStreamHandler {
  private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> sHandler;
  // This means AWS Lambda executes the code as it starts the JVM, giving you better performance out of the gate.
  static {
    try {
      // For applications that take longer than 10 seconds to start, use the async builder:
      sHandler = new SpringBootProxyHandlerBuilder<AwsProxyRequest>().defaultProxy()
          .asyncInit()
          .springBootApplication(Application.class)
          .buildAndInitialize();
    }
    catch (ContainerInitializationException e) {
      throw new RuntimeException("Could not initialize Spring Boot application", e);
    }
  }

  public StreamLambdaHandler() {
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    sHandler.proxyStream(inputStream, outputStream, context);
  }
}
```

But with Micronaut they have designed the platform from start with the cloud in mind. So all you need in case of
Micronaut is to configure correct _runtime_ called `lambda_java` in your build (build.gradle):

```
micronaut {
  version(micronautVersion)
  runtime("lambda_java")
  testRuntime("junit5")
  processing {
    incremental(true)
    annotations("com.example.*")
  }
}
```

The handler class and method in case of Micronaut will be `io.micronaut.function.aws.proxy.MicronautLambdaHandler::handleRequest`.

All in all this also means that your microservice will not need any web server component like Tomcat or Jetty.

## Note about payload formats
AWS has actually two formats of the payload of the events passed between the API Gateway and the AWS Lambda;
[version 1.0 and version 2.0](https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html).

Micronaut as of version 3.9.x of `micronaut-function-aws-api-proxy` library only supports payload version 1.0.
So remember to specify that when you define the the API Gateway resources since default will otherwise be version 2.0.


## Mapping request in API Gateway to REST endpoint in your microservice.
There are a couple of ways to map or route requests to the API Gateway to the lambda.

You can either specify every endpoint in detail in the definition of the API gateway. Or you can simply say that all calls to the base URL will be forwarded to the lambda.

Detailed mapping can look like this in the Cloudformation template:

```yaml
  ServerlessFunction:
    Type: AWS::Serverless::Function
    Properties:
      Architectures:
        - arm64
      CodeUri:
        Bucket: !Ref CodeS3BucketName
        Key: !Ref CodeFileName
      Events:
        ApiGetAllEvent:
          Type: HttpApi
          Properties:
            Method: get
            Path: /api/v1/books
            PayloadFormatVersion: '1.0'
        ApiGetOneEvent:
          Type: HttpApi
          Properties:
            Method: get
            Path: /api/v1/books/{id}
            PayloadFormatVersion: '1.0'
        ApiPostEvent:
          Type: HttpApi
          Properties:
            Method: post
            Path: /api/v1/books
            PayloadFormatVersion: '1.0'
```

Or you can simply forward all using a _greedy_ path variable of proxy+:

```yaml
  ServerlessFunction:
    Type: AWS::Serverless::Function
    Properties:
      Architectures:
        - arm64
      CodeUri:
        Bucket: !Ref CodeS3BucketName
        Key: !Ref CodeFileName
      Events:
        ApiAnyEvent:
          Type: HttpApi
          Properties:
            Method: any
            Path: /{proxy+}
            PayloadFormatVersion: '1.0'
```





## Micronaut vs Spring
Which microservice platform is best for running AWS Lambdas? It as usually depends :-). But here are some key aspects
to consider when starting create AWS lambdas in Java.

One of the reasons as I see it is that your developers can run the code locally and easily create
unit tests/integration tests that don't require AWS services to run. The local setup will be very simple to maintain.
With both Spring and Micronaut the setup is almost identical compared to if the microservice was running in a container
instead.

Some pros and cons for the two frameworks:
* SpringBoot is a giant and you can easily find answers for issues on Stackoverflow. Not so much for Micronaut.
* The startup time is much faster with Micronaut since the framework pre-generate bootstrapping code using
Java source-level annotation processors compared with Spring that uses reflection to scan the code base at startup.
The [Annotation Processing Tool](https://docs.oracle.com/javase/7/docs/technotes/guides/apt/GettingStarted.html) has
been around since Java7 but only to my knowledge there aren't that many tools that has picked it up.
* Micronaut is heavily inspired by SpringBoot. It copies a lot of good stuff from SpringBoot like Spring-data but since
 Micronaut is using annotation processors it can in compile time check your find methods. If you have `findByFirstName`
 and the entity doesn't have any property called `FirstName` you will get an compilation error instead of discovering
 this at runtime when using Spring. Micronaut calls the data access layer for [Micronaut-data](https://github.com/micronaut-projects/micronaut-data)
* Both frameworks supports GraalVM to reduce start time even more. But building a native image can take a lot of time.
* With Spring there are quite a number of components for different cloud platforms and other integrations.
Luckily Micronaut makes Spring components available too in their [Spring integration](https://micronaut-projects.github.io/micronaut-spring/latest/guide/index.html).
* Micronaut and also Quarkus have been created with the cloud and containers as first citizens.
 Taking aspects like boot time and memory consumption in mind.


## Deploy with AWS Cloudformation and SAM
The Java application that is using Micronaut will be package in a JAR file and uploaded to an S3 bucket and then
the AWS lambda is defined using the JAR as its code base. Then an API Gateway V2 (HTTP API) is defined to route
to that lambda.

```
./gradlew deploySamStack
```

build.gradle (snippet):

```grovvy
plugins {
  id 'se.solrike.cloudformation'
  id 'seek.aws' apply false
}

def lambdaRepoS3Bucket = 'slrk-lambda-repo'
task uploadJarToS3(type: seek.aws.s3.UploadFile, dependsOn: jar) {
  group = 'AWS'
  description = 'Uploads the jar to S3 amazon storage so it can be referenced when defining the lambda function in AWS'
  bucket lambdaRepoS3Bucket
  file jar.archivePath
  key jar.archiveFileName.get()
}

task deploySamStack(type: se.solrike.cloudformation.CreateOrUpdateStackTask, dependsOn: uploadJarToS3) {
  group = 'AWS'
  description = 'Deployment of the API gateway and the lambda using a SAM Cloudformation template'
  Properties p = new Properties()
  p.codeFileName = jar.archiveFileName.get()
  p.codeS3BucketName = lambdaRepoS3Bucket
  p.environment = 'stage25'
  p.functionName = 'books-api'
  p.handler = 'io.micronaut.function.aws.proxy.MicronautLambdaHandler::handleRequest'
  capabilities = [
    'CAPABILITY_IAM',
    'CAPABILITY_AUTO_EXPAND'
  ]
  parameters = p
  stackName = "slrk-${p.environment}-aws-sam-${p.functionName}"
  templateFileName = 'aws-cloudformation/aws-sam-httpapi.yaml'
}
```

The endpoint URL for the API Gateway resource will typically be:

    https://<API_ID>.execute-api.<AWS_REGION>.amazonaws.com/api/v1/books

In a real world scenario you want to put your own domain name instead of the auto-generated domain name.


## Testing the REST API
In this very small sample app there one endpoint to list, retrieve and create books.

To create a book:

```
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"description":"The Silmarillion"}' \
  https://8wzj454ygf.execute-api.eu-north-1.amazonaws.com/api/v1/books

```

To list all books:

```
curl https://8wzj454ygf.execute-api.eu-north-1.amazonaws.com/api/v1/books
```

First time you invoke the lambda AWS will start your microservice. So depending on startup time of the microservice the
first invocation will take some additional seconds. This very small Micronaut REST API takes according to what
AWS Lambda service prints in the log around 5 seconds to start (called Init Duration). That also includes the
duration for AWS to allocate resource for the lambda and not only the boot time of the application.
More about cold and warm [starts of lambdas](https://docs.aws.amazon.com/lambda/latest/operatorguide/execution-environments.html)


## Run AWS lambda locally with Docker


## Get the code for this article
Visit my github repo at https://github.com/Lucas3oo/micronaut-aws-serverless

## References
- [Micronaut AWS SDK 2.x documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/)
- [Micronaut AWS Lambda Function documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/index.html#lambda)