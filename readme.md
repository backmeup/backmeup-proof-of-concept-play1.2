# Welcome to the BackMeUp Prototype

The aim of the BackMeUp Prototype is to provide a scalable personal data backup platform.
The BackMeUp Prototype will allow users to create backups of their personal data that is scattered across
the Web (e.g. on social networks, Web mail services, cloud storage provides or media sharing sites). 

BackMeUp will feature a modular architecture in which new services can be supported through connector plugins.
In addition to backup, users will also be able to schedule more complex processing workflows, including e.g.
encryption or data normalization/format conversion tasks.

## Prerequisites

The BackMeUp Prototype is built with the [Play Framework] (http://www.playframework.org/).
Instructions on how to set up Play on your machine are [available here] 
(http://www.playframework.org/documentation/1.2.3/install).

## Getting Started

1. Change to the project folder to which you cloned the project repository

2. Set up your local configuration: change to the folder `/conf` and...
   - create a copy of the file `application.conf.template` named `application.conf`. (Please DO create a copy and
     DON'T just rename the original file - otherwise you might accidentially delete the .template file from the
     repository with your next commit!)
     You can edit this file according to your local environment (e.g. set up a database connection instead
     of the default in-memory database, use a [Hadoop Distributed File System] (http://hadoop.apache.org/hdfs/)
     for backup storage, etc.) but to get started, the default config should be fine.
   - create a copy of the file `inital-data.yml.template` named `inital-data.yml`. (Again, DO create a copy,
     DON'T just rename the file!) You can edit this file to set up the initial data the application is bootstrapped
     with, but the defaults are sufficient to get you started.

3. Type `play dependencies` (or `play deps`) to resolve and download the managed dependencies.

3. Type `play run` to start the prototype. The application will be at http://localhost:9080/ You
   can log in to the application using a pre-set test account (username 'guest', password 'guest').

4. Typing `play eclipsify` (or `play ec`) will generate an Eclipse project.

## Deployment

For server deployment, you can generate a standard Java Web archive (.war) file. To do this, 
change into the application's parent folder and type

`play war backmeup-prototype -o backmeup-prototype --zip`

## Further Information 

More technical and developer information is begin made available in the
[Wiki] (https://github.com/backmeup/backmeup-prototype/wiki).
