pipeline {
    agent { label 'worker-01' }

    triggers {
        githubPush()
    }

    environment {
        IMAGE = 'deepaksag/k8s-spring'
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
                withCredentials([usernamePassword(credentialsId: 'dockerhub-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
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
   

    post {
        success {
            emailext(
                subject: "✅ Jenkins Build SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}",
                body: """<html>
                            <body>
                                <p>Build Status: SUCCESS</p>
                                <p>The Jenkins job <b>${JOB_NAME}</b> completed <b>successfully</b>.</p>
                                <p><a href="${BUILD_URL}">Click here to view the build</a></p>
                            </body>
                        </html>""",
                to: 'sagardeepak2002@gmail.com',
                from: 'deepsagar0701@gmail.com',
                replyTo: 'jenkins@example.com',
                mimeType: 'text/html'
            )
        }
    }         
}