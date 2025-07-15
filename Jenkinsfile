pipeline {
    agent { label 'ubuntu' }

    triggers {
        githubPush()
    }

    environment {
        IMAGE = 'deepaksag/private-springapp'
    }
   
    stages{
        stage('Checkout') {
            steps {
                git branch: 'springboot', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        // stage('repo trivy scan') {
        //     steps {
        //         sh 'trivy fs  --severity HIGH,CRITICAL .'
        //     }
        // }

        // stage('Compile') {
        //     steps {
        //         sh 'mvn compile'
        //     }
        // }
        // stage('SonarQube Analysis') {
        //     steps {
        //         withSonarQubeEnv('sonarqube') {
        //             sh 'mvn sonar:sonar'
        //         }
        //     }
        // }
        stage('DockerImage Build') {
            steps {
                sh 'docker build -t $IMAGE:1.0.0 .'
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
   

    // post {
    //     success {
    //         emailext(
    //             subject: "✅ Jenkins Build SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}",
    //             body: """<html>
    //                         <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
    //                             <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 30px;">
    //                                 <h2 style="color: #4CAF50; text-align: center;">✅ Jenkins Build SUCCESS</h2>
    //                                 <hr style="border: none; border-top: 1px solid #eee;" />
    //                                 <p><strong>Job Name:</strong> ${JOB_NAME}</p>
    //                                 <p><strong>Build Number:</strong> #${BUILD_NUMBER}</p>
    //                                 <p><strong>Build Status:</strong> <span style="color: green;"><b>SUCCESS</b></span></p>
    //                                 <p><strong>Build URL:</strong> <a href="${BUILD_URL}" style="color: #2196F3;">View Build</a></p>
    //                                 <hr style="border: none; border-top: 1px solid #eee;" />
    //                                 <p style="font-size: 12px; color: #888;">This is an automated message sent by Jenkins CI server.</p>
    //                             </div>
    //                         </body>
    //                      </html>""",
    //             to: 'sagardeepak2002@gmail.com',
    //             from: 'deepsagar0701@gmail.com',
    //             replyTo: 'jenkins@example.com',
    //             mimeType: 'text/html'
    //         )
    //     }
    // }         
}
