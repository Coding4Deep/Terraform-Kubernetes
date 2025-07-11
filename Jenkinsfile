pipeline{
    agent { label 'worker-01 '}

    triggers {
        githubPush()
    }
   
    environment {
        AWS_CREDS = credentials('aws-iam-creds')
        AWS_DEFAULT_REGION    = 'us-east-1'
    }

    stages{
        stage('Checkout') {
            steps {
                git branch: 'ansible', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        stage('Ansible Inventory') {
            steps {
                sh 'ansible-inventory --graph'
            }
        }
        // stage('Docker Installation') {
        //     steps {
        //         sh 'ansible-playbook  playbooks/docker_install.yml'
        //     }
        // }
    }
}