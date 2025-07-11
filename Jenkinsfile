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
        stage('Build & Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker build -t $IMAGE:latest .
                        docker push $IMAGE:latest
                        docker logout
                    '''
                }
            }
        }

    }
}