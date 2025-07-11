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
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL .'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }
    }
}