pipeline {
    agent any

    environment {
        PATH = "/usr/local/bin:/usr/bin:/bin:${env.PATH}"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build('cucumber-restassured-tests:latest')
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    docker.image('cucumber-restassured-tests:latest').inside {
                        sh 'mvn clean test'
                    }
                }
            }
        }
    }

    post {
        always {
            junit '**/target/cucumber-reports/*.xml'

        }
    }
}