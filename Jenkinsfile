pipeline{
    agent { label 'worker-01 '}

    // triggers {
    //     githubPush()
    // }
   
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
        stage('Change Hostname of all node') {
            steps {
                sh 'ansible-playbook  playbooks/hostname_change.yml'
            }
        }
        stage('Docker Installation') {
            steps {
                sh 'ansible-playbook  playbooks/docker_install.yml'
            }
        }
        stage('Kubernetes pre-requisites and configuration') {
            steps {
                sh 'ansible-playbook  playbooks/k8s_configure.yml'
            }
        }
        stage('Kubernetes components Installation') {
            steps {
                sh 'ansible-playbook  playbooks/k8s_components.yml'
            }
        }
        stage('Kubernetes init') {
            steps {
                sh 'ansible-playbook  playbooks/kubeadm_init.yml'
            }
        }
        stage('Kubernetes joining nodes') {
            steps {
                sh 'ansible-playbook  playbooks/kubeadm_join.yml'
            }
        }
        stage('check kubernetes nodes') {
            steps {
                sh 'sleep 30'
                sh 'ansible master -m shell -a "kubectl get nodes"'
            }
        }
    }
}