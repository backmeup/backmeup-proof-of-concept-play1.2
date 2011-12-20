# Requirements

This project is built with the [Play Framework] (http://www.playframework.org/). Instructions on how to set up 
Play on your machine are [available here] (http://www.playframework.org/documentation/1.2.3/install).

# Getting Started

1. Type `play dependencies` to resolve the managed dependencies.

2. Type `play run` to start. The application will be at http://localhost:9000/

3. You can log in to the application using a test account (username 'guest', password 'guest').

`play eclipsify` will generate an Eclipse project.

# Configuring Connectors

The prototype will (eventually) provide a number of connectors to different online storage and content sharing
services. Currently, the project includes connectors to the following services:

* [Dropbox] (http://dropbox.com)
* [Amazon S3] (http://aws.amazon.com/s3/)
* [Google Cloud Storage] (code.google.com/apis/storage/)

## Configuring the Dropbox Connector

Dropbox uses OAuth authentication. That means you need to create an app at the [Dropbox Developer Site]
(http://www.dropbox.com/developers). After creating the app, you will receive your unique "application key"
and "application secret" tokens. 

Create a copy of the file /conf/dropbox.properties.template named /conf/dropbox.properties and paste
your application tokens in there.

## Configuring the Amazon S3 Connector

Todo...

## Configuring the Google Cloud Storag Connector

Todo...

# Deployment

For server deployment, you can generate a standard Java Web archive (.war) file. To do this, 
change into the application's parent folder and type

`play war backmeup-prototype -o backmeup-prototype --zip`
