# Rijksmuseum API Test Automation Framework

This repository contains a Cucumber + RestAssured + JUnit test automation framework designed to validate the Rijksmuseum APIs.  
The entire test suite is wrapped inside a Docker for easy setup and execution on any machine with Docker installed.

# APIs Under Test

Search API - https://data.rijksmuseum.nl/docs/search
IIIF Image API - https://data.rijksmuseum.nl/docs/iiif/image
Content Negotiation API - https://data.rijksmuseum.nl/docs/http/content-negotiation-arguments

# Features 

Behavior-driven tests using Cucumber feature files
REST API validation with RestAssured
JUnit 4 as the test runner
Organized with a clear package structure (hooks, runners, steps, utils)
Detailed feature files under src/test/resources/features
Dockerized setup for simple builds & execution
Jenkins CI/CD pipeline setup for automated builds and test runs (partially implemented)


# Prerequisites

Docker installed and running


# Getting Started

## Follow these steps to clone the repository and run the API tests using Docker.

1. Clone the repository

* git clone https://github.com/sreelekshmi-mp/ing-qa-assignment.git
* cd ing-qa-assignment

2. Build the Docker image

* docker build -t cucumber-restassured-tests .

3. Run the tests inside the Docker container

* docker run --rm cucumber-restassured-tests

# Reports
Test reports will be generated in the target/cucumber-reports directory within the container including:

cucumber.xml - Detailed XML report.
cucumber.json - JSON format report.
cucumber-html - user-friendly HTML report for easy viewing in a browser. 
