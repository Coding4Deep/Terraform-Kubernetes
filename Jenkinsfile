pipeline {
    agent { label 'worker-01' }

    triggers {
        githubPush()
    }   

    environment {
        // AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
        // AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        AWS_CREDS = credentials('aws-iam-creds')
        AWS_DEFAULT_REGION    = 'us-east-1'
        VAULT_ADDR            = credentials('vault_addr')
        // VAULT_TOKEN           = credentials('vault_token')
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'terraform', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        // stage('credentials check'){
        //     steps {
        //        sh 'aws s3 ls'
        //     }
        // }
        // stage('Debug') {
        //     steps {
        //         sh 'echo $AWS_ACCESS_KEY_ID'
        //         sh 'echo $AWS_SECRET_ACCESS_KEY'
        //     }
        // }
        // stage('AWS Credential Check') {
        //     steps {
        //         sh 'aws sts get-caller-identity'
        //     }
        // }
        stage('Terraform Apply') {
          steps {
            withVault(
              configuration: [vaultUrl: "${VAULT_ADDR}", vaultCredentialId: 'vault-jenkins-token'],
              vaultSecrets: [[path: 'aws-creds/myapp',engineVersion: 1, secretValues: [
                [envVar: 'AWS_ACCESS_KEY_ID', vaultKey: 'access_key'],
                [envVar: 'AWS_SECRET_ACCESS_KEY', vaultKey: 'secret_key']
              ]]]
            ){
              sh '''
                terraform init
                terraform plan 
                terraform apply -auto-approve 
              '''
            }
          }
        }
    }
}