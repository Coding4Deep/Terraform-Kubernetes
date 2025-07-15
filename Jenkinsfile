pipeline {
    agent { label 'ubuntu' }

    triggers {
        githubPush()
    }   

    environment {
        AWS_DEFAULT_REGION    = 'us-east-1'
        VAULT_ADDR            = credentials('vault_addr')
    }
    stages{
        stage('Checkout') {
            steps {
                git branch: 'ansible', url: 'https://github.com/Coding4Deep/Terraform-Kubernetes.git'
            }
        }
        // stage('Ansible Playbook') {
        //   steps {
        //     withVault(
        //       configuration: [vaultUrl: "${VAULT_ADDR}", vaultCredentialId: 'vault-jenkins-token'],
        //       vaultSecrets: [[path: 'aws-creds/myapp',engineVersion: 1, secretValues: [
        //         [envVar: 'AWS_ACCESS_KEY_ID', vaultKey: 'access_key'],
        //         [envVar: 'AWS_SECRET_ACCESS_KEY', vaultKey: 'secret_key']
        //       ]]]
        //     ){
        //       sh '''
        //          ansible-inventory --graph
        //          ansible-playbook  playbooks/hostname_change.yml
        //          ansible-playbook  playbooks/docker_install.yml
        //          ansible-playbook  playbooks/k8s_configure.yml
        //          ansible-playbook  playbooks/k8s_components.yml
        //          ansible-playbook  playbooks/kubeadm_init.yml
        //          ansible-playbook  playbooks/kubeadm_join.yml                
        //       '''
        //     }
        //   }
        // }
        stage('check kubernetes nodes') {
            steps {
                sh 'sleep 30'
                sh 'ansible master -m shell -a "kubectl get nodes"'
            }
        }
        stage('Terraform Apply') {
            steps {
                withCredentials([file(credentialsId: 'terraform-vars', variable: 'TFVARS')]) {
                    sh '''    
                    cd terraform && terraform init && terraform plan -var-file="$TFVARS"
                    '''
                }
            }
        }
    }
}
