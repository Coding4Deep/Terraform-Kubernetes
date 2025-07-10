pipeline {
    agent { label 'worker-01' }

    triggers {
        githubPush()
    }   
    environment {
        AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        AWS_DEFAULT_REGION    = 'us-east-1'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'terraform', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        stage('credentials check'){
            steps {
               sh 'aws s3 ls'
            }
        }

        stage('Debug') {
            steps {
                sh 'echo $AWS_ACCESS_KEY_ID'
                sh 'echo $AWS_SECRET_ACCESS_KEY'
            }
        }
        stage('AWS Credential Check') {
            steps {
                sh 'aws sts get-caller-identity'
            }
        }
        stage('Terraform Init') {
            steps {
                sh 'terraform init'
            }
        }
        stage('Terraform Plan') {
            steps {
                sh 'terraform plan '
            }
        }
    }
}