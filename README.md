# Serverless EU VAT Checker using REST  

A simple AWS Lambda function using Java to check EU VAT numbers via HTTP GET.

## Build

It is required to build prior to deploying. You can build the deployment artifact using Gradle or Maven.

### Gradle

In order to build using Gradle simply run

```bash
$ gradle wrapper # to build the gradle wrapper jar
$ ./gradlew build # to build the application jar
```

The expected result should be similar to:

```bash
Starting a Gradle Daemon, 1 incompatible Daemon could not be reused, use --status for details
:compileJava
:processResources
:classes
:jar
:assemble
:buildZip
:compileTestJava UP-TO-DATE
:processTestResources UP-TO-DATE
:testClasses UP-TO-DATE
:test UP-TO-DATE
:check UP-TO-DATE
:build

BUILD SUCCESSFUL

Total time: 8.195 secs
```

### Maven

In order to build using Maven simply run

```bash
$ mvn clean package
```

## Deploy using Serverless

After having built the deployment artifact using Gradle or Maven as described above you can deploy the serverless method running the following commands:

```bash
# Install serverless globally
npm install serverless -g
```

Then, check the version to make sure you are using V1.16.0, or later:

```bash
$ serverless -v
```

Login to the serverless platform (optional)

```bash
$ sls login
```

```bash
$ sls deploy
```

The expected result should be similar to:

```bash
Serverless: Packaging service...
Serverless: Creating Stack...
Serverless: Checking Stack create progress...
.....
Serverless: Stack create finished...
Serverless: Uploading CloudFormation file to S3...
Serverless: Uploading artifacts...
Serverless: Updating Stack...
Serverless: Checking Stack update progress...
.................................
Serverless: Stack update finished...
Service Information
service: aws-java-maven
stage: dev
region: us-east-1
api keys:
  None
endpoints:
  GET - https://XXXXXXX.execute-api.us-east-1.amazonaws.com/dev/vatNumber/isValid
functions:
  vatChecker: aws-java-maven-dev-vatChecker
```

More details on Serverless @ https://serverless.com/framework/docs/getting-started/ 

## Usage

You can now invoke the Lambda function directly and even see the resulting log via

```bash
$ serverless invoke --function vatChecker --log
```

The expected result should be similar to:

```bash
{
    "statusCode": 400,
    "body": "{\"error\":\"Missing query string parameters {number} and {country}. The parameter {country} holds a two character EU country code, for example BE. And {number} holds the VAT number you want to validate.\"",
    "headers": {
        "X-Powered-By": "Stephan Janssen (sja@devoxx.com) AWS EU VAT Lambda",
        "Content-Type": "application/json"
    },
    "isBase64Encoded": false
}
--------------------------------------------------------------------
START RequestId: XXXXXXXXX Version: $LATEST
2017-04-27 14:09:25 <XXXXXXXXX> INFO  com.serverless.Handler:29 - received: {}
END RequestId: XXXXXXXXX
REPORT RequestId: XXXXXXXXX	Duration: 165.92 ms	Billed Duration: 200 ms 	Memory Size: 1024 MB	Max Memory Used: 48 MB
```

Finally you can send an HTTP request directly to the endpoint using a tool like curl

```bash
$ curl -X GET 'https://XXXXXXX.execute-api.us-east-1.amazonaws.com/dev/vatNumber/isValid?country=IE&number=63388047V'
```

The expected result should be similar to:

```bash
 {
"isValid": true,
"name": "GOOGLE IRELAND LIMITED",
"address": "3RD FLOOR ,GORDON HOUSE ,BARROW STREET ,DUBLIN 4"
}
```

## Function timouts

Some VAT checks can take between 6 and 10 seconds.  This means the default Lamba function timeout of 6 seconds should be increated to 10 seconds.

