![Build](https://github.com/ajablonski/ct-delay/actions/workflows/scala.yml/badge.svg)

# Requirements
* SBT
* Node

# Local development

## With SBT

`sbt server/run` will run the application locally. You'll need to set the APP_CTA_TRAIN_API_KEY and APP_CTA_BUS_API_KEY environment variables.

## Packaged

`./run.sh` will package the application, create a Docker image, and deploy that image locally using the configuration in the docker-compose.yml file. You'll need to provide secrets specified in the `docker-compose.yml` file, as well as an .env file (https://docs.docker.com/compose/environment-variables/) for any additional hosts you would like to allow Play to respond to.

## With Docker
