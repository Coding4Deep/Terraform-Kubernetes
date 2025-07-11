pipeline {
    agent { label 'worker-01' }

    triggers {
        githubPush()
    }
   
    stages{
        stage('Checkout') {
            steps {
                git branch: 'springboot', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        stage('repo trivy scan') {
            steps {
                sh 'trivy fs  --severity HIGH,CRITICAL .'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('DockerImage Build') {
            steps {
                sh 'docker build -t deepaksag/k8s-spring:1.0.0 .'
            }
        }
    }
}