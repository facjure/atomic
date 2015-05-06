Installation
============

Download the latest version of [Datomic Free](http://www.datomic.com/get-datomic.html):

    wget https://my.datomic.com/downloads/free/0.9.5067
    unzip datomic-free-0.9.5067.zip

For [Datomic Pro Starter](http://downloads.datomic.com/pro.html), sign for a
free license key. Once you download the key, make sure you swap the dependencies
in [project.clj](project.clj).

## Configuration

[Environ](https://github.com/weavejester/environ) is used to manage environment
variables for AWS, Heroku, Cassandra and other storage engines.

Add the follwing keys in `~/.lein/profiles.clj`:

Setup the type in env:

    :datomic-dbtype = :free
    :datomic-name ("test")

Postgres (Heroku):

    :datomic-jdbc-url

AWS:

    :aws-access-key
    :aws-secret-key

Let's see if our setup works.

    lein deps (first time)
    lein repl

If all goes well, you should see a repl with no errors.

## Running Datomic

If you purchased a Pro-Starter license, copy the relevant config properties from
`config/samples/<type>.properties` to `config/dev.properties`. Edit
config/dev.properties and paste your license key.

Start the transactor (it runs datomic):

    cd <datomic-folder>
    bin/transactor config/samples/free-transactor-template.properties

## Docker (Pro Starter)

For OS X, the following steps are based on [this image](https://pointslope.com/blog/datomic-pro-starter-edition-in-15-minutes-with-docker).

Install [boot2docker](http://boot2docker.io)

Install Fig

	brew install fig

Clone the image

	git clone https://github.com/pointslope/docker-datomic-example.git
	cd docker-datomic-example

Start Docker

	boot2docker up
	fig up

