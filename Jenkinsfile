pipeline {
    agent { label 'ubuntu' }

    triggers {
        githubPush()
    }   

    environment {
        AWS_DEFAULT_REGION    = 'us-east-1'
        VAULT_ADDR            = 'http://54.86.123.139:8200'
    }
    stages{
        stage('Checkout') {
            steps {
                git branch: 'ansible', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        stage('Ansible Playbook') {
          steps {
            withVault(
              configuration: [vaultUrl: "${VAULT_ADDR}", vaultCredentialId: 'vault-jenkins-token'],
              vaultSecrets: [[path: 'aws-creds/myapp',engineVersion: 1, secretValues: [
                [envVar: 'AWS_ACCESS_KEY_ID', vaultKey: 'access_key'],
                [envVar: 'AWS_SECRET_ACCESS_KEY', vaultKey: 'secret_key']
              ]]]
            ){
              sh '''
                
                 cd terraform && terraform init && terraform plan  && terraform apply --auto-approve            
              '''
            }
          }
        }
        stage('check kubernetes nodes') {
            steps {
                sh 'sleep 20'
                sh 'ansible master -m shell -a "kubectl get nodes"'
            }
        }
        // stage('Ansible Playbook') {
        //     steps {
        //         withCredentials([file(credentialsId: 'terraform-vars', variable: 'TFVARS')]) {
        //             withVault(
        //               configuration: [vaultUrl: "${VAULT_ADDR}", vaultCredentialId: 'vault-jenkins-token'],
        //               vaultSecrets: [[
        //                 path: 'aws-creds/myapp',
        //                 engineVersion: 1,
        //                 secretValues: [
        //                   [envVar: 'AWS_ACCESS_KEY_ID', vaultKey: 'access_key'],
        //                   [envVar: 'AWS_SECRET_ACCESS_KEY', vaultKey: 'secret_key']
        //                 ]
        //               ]]
        //             ) {
        //               sh '''
        //                 cd terraform && terraform init && terraform plan -var-file="$TFVARS" && terraform apply --auto-approve -var-file="$TFVARS"
        //               '''
        //             }
        //         }
        //     }
        // }
    }
}
