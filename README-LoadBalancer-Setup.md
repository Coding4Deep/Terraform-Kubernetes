# Enabling LoadBalancer Service Type in Kubeadm Cluster on AWS EC2

This guide provides step-by-step instructions to enable LoadBalancer service type in a kubeadm cluster running on AWS EC2 instances by installing and configuring the AWS Cloud Controller Manager (CCM).

## Prerequisites
- Running kubeadm cluster on AWS EC2 instances
- kubectl configured to access your cluster
- AWS CLI installed and configured
- Administrative access to AWS account
- SSH access to all cluster nodes

## Overview
Unlike EKS clusters that have built-in AWS integration, kubeadm clusters require manual setup of:
1. AWS Cloud Controller Manager (CCM)
2. Proper EC2 instance tagging
3. IAM roles and policies
4. Security group configurations
5. Subnet tagging for load balancer placement

---

## Step 1: Tag EC2 Instances

### 1.1 Tag All Cluster Nodes
Tag all EC2 instances (master and worker nodes) with the cluster identifier:

```bash
# Replace 'your-cluster-name' with your actual cluster name
CLUSTER_NAME="your-cluster-name"

# Get instance IDs of all cluster nodes
aws ec2 describe-instances \
  --filters "Name=instance-state-name,Values=running" \
  --query 'Reservations[*].Instances[*].InstanceId' \
  --output text

# Tag each instance (replace INSTANCE_ID with actual IDs)
aws ec2 create-tags \
  --resources INSTANCE_ID \
  --tags Key=kubernetes.io/cluster/${CLUSTER_NAME},Value=owned
```

### 1.2 Tag Master Nodes Specifically
```bash
# Tag master nodes with additional role tag
aws ec2 create-tags \
  --resources MASTER_INSTANCE_ID \
  --tags Key=kubernetes.io/role/master,Value=1
```

### 1.3 Tag Worker Nodes Specifically
```bash
# Tag worker nodes with additional role tag
aws ec2 create-tags \
  --resources WORKER_INSTANCE_ID \
  --tags Key=kubernetes.io/role/node,Value=1
```

---

## Step 2: Tag Subnets for Load Balancer Placement

### 2.1 Tag Public Subnets (for Internet-facing Load Balancers)
```bash
# Get subnet IDs where your instances are running
aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=YOUR_VPC_ID" \
  --query 'Subnets[*].[SubnetId,Tags[?Key==`Name`].Value|[0]]' \
  --output table

# Tag public subnets for external load balancers
aws ec2 create-tags \
  --resources PUBLIC_SUBNET_ID \
  --tags Key=kubernetes.io/role/elb,Value=1 \
         Key=kubernetes.io/cluster/${CLUSTER_NAME},Value=owned
```

### 2.2 Tag Private Subnets (for Internal Load Balancers)
```bash
# Tag private subnets for internal load balancers
aws ec2 create-tags \
  --resources PRIVATE_SUBNET_ID \
  --tags Key=kubernetes.io/role/internal-elb,Value=1 \
         Key=kubernetes.io/cluster/${CLUSTER_NAME},Value=owned
```

---

## Step 3: Create IAM Role and Policy for Cloud Controller Manager

### 3.1 Create IAM Policy
```bash
# Create policy document
cat > aws-ccm-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "autoscaling:DescribeAutoScalingGroups",
                "autoscaling:DescribeLaunchConfigurations",
                "autoscaling:DescribeTags",
                "ec2:DescribeInstances",
                "ec2:DescribeRegions",
                "ec2:DescribeRouteTables",
                "ec2:DescribeSecurityGroups",
                "ec2:DescribeSubnets",
                "ec2:DescribeVolumes",
                "ec2:CreateSecurityGroup",
                "ec2:CreateTags",
                "ec2:CreateVolume",
                "ec2:ModifyInstanceAttribute",
                "ec2:ModifyVolume",
                "ec2:AttachVolume",
                "ec2:AuthorizeSecurityGroupIngress",
                "ec2:CreateRoute",
                "ec2:DeleteRoute",
                "ec2:DeleteSecurityGroup",
                "ec2:DeleteVolume",
                "ec2:DetachVolume",
                "ec2:RevokeSecurityGroupIngress",
                "ec2:DescribeVpcs",
                "elasticloadbalancing:AddTags",
                "elasticloadbalancing:AttachLoadBalancerToSubnets",
                "elasticloadbalancing:ApplySecurityGroupsToLoadBalancer",
                "elasticloadbalancing:CreateLoadBalancer",
                "elasticloadbalancing:CreateLoadBalancerPolicy",
                "elasticloadbalancing:CreateLoadBalancerListeners",
                "elasticloadbalancing:ConfigureHealthCheck",
                "elasticloadbalancing:DeleteLoadBalancer",
                "elasticloadbalancing:DeleteLoadBalancerListeners",
                "elasticloadbalancing:DescribeLoadBalancers",
                "elasticloadbalancing:DescribeLoadBalancerAttributes",
                "elasticloadbalancing:DetachLoadBalancerFromSubnets",
                "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
                "elasticloadbalancing:ModifyLoadBalancerAttributes",
                "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
                "elasticloadbalancing:SetLoadBalancerPoliciesForBackendServer",
                "elasticloadbalancing:AddTags",
                "elasticloadbalancing:CreateListener",
                "elasticloadbalancing:CreateTargetGroup",
                "elasticloadbalancing:DeleteListener",
                "elasticloadbalancing:DeleteTargetGroup",
                "elasticloadbalancing:DescribeListeners",
                "elasticloadbalancing:DescribeLoadBalancerPolicies",
                "elasticloadbalancing:DescribeTargetGroups",
                "elasticloadbalancing:DescribeTargetHealth",
                "elasticloadbalancing:ModifyListener",
                "elasticloadbalancing:ModifyTargetGroup",
                "elasticloadbalancing:RegisterTargets",
                "elasticloadbalancing:SetLoadBalancerPoliciesOfListener",
                "iam:CreateServiceLinkedRole",
                "kms:DescribeKey"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
EOF

# Create the policy
aws iam create-policy \
  --policy-name AWSCloudControllerManagerPolicy \
  --policy-document file://aws-ccm-policy.json
```

### 3.2 Create IAM Role
```bash
# Create trust policy for EC2
cat > trust-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# Create IAM role
aws iam create-role \
  --role-name AWSCloudControllerManagerRole \
  --assume-role-policy-document file://trust-policy.json

# Attach policy to role
aws iam attach-role-policy \
  --role-name AWSCloudControllerManagerRole \
  --policy-arn arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):policy/AWSCloudControllerManagerPolicy
```

### 3.3 Create Instance Profile and Attach to EC2 Instances
```bash
# Create instance profile
aws iam create-instance-profile \
  --instance-profile-name AWSCloudControllerManagerInstanceProfile

# Add role to instance profile
aws iam add-role-to-instance-profile \
  --instance-profile-name AWSCloudControllerManagerInstanceProfile \
  --role-name AWSCloudControllerManagerRole

# Attach instance profile to all cluster nodes
aws ec2 associate-iam-instance-profile \
  --instance-id INSTANCE_ID \
  --iam-instance-profile Name=AWSCloudControllerManagerInstanceProfile
```

---

## Step 4: Configure Security Groups

### 4.1 Update Security Group Rules
```bash
# Get security group ID of your cluster nodes
SECURITY_GROUP_ID=$(aws ec2 describe-instances \
  --instance-ids INSTANCE_ID \
  --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' \
  --output text)

# Allow load balancer health checks (adjust ports as needed)
aws ec2 authorize-security-group-ingress \
  --group-id $SECURITY_GROUP_ID \
  --protocol tcp \
  --port 30000-32767 \
  --source-group $SECURITY_GROUP_ID

# Allow communication between nodes
aws ec2 authorize-security-group-ingress \
  --group-id $SECURITY_GROUP_ID \
  --protocol all \
  --source-group $SECURITY_GROUP_ID
```

---

## Step 5: Modify Kubelet Configuration

### 5.1 Update Kubelet on All Nodes
SSH into each node and update kubelet configuration:

```bash
# On each node, edit kubelet config
sudo nano /var/lib/kubelet/config.yaml

# Add or modify these settings:
# providerID: aws:///AVAILABILITY_ZONE/INSTANCE_ID
# Example: providerID: aws:///us-west-2a/i-1234567890abcdef0
```

### 5.2 Update Kubelet Service Arguments
```bash
# Edit kubelet service file
sudo nano /etc/systemd/system/kubelet.service.d/10-kubeadm.conf

# Add to ExecStart line:
# --cloud-provider=external
# --provider-id=aws:///AVAILABILITY_ZONE/INSTANCE_ID

# Restart kubelet
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

---

## Step 6: Install AWS Cloud Controller Manager

### 6.1 Create CCM Deployment Manifest
```bash
cat > aws-cloud-controller-manager.yaml << EOF
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: aws-cloud-controller-manager
  namespace: kube-system
  labels:
    k8s-app: aws-cloud-controller-manager
spec:
  selector:
    matchLabels:
      k8s-app: aws-cloud-controller-manager
  updateStrategy:
    type: RollingUpdate
  template:
    metadata:
      labels:
        k8s-app: aws-cloud-controller-manager
    spec:
      nodeSelector:
        node-role.kubernetes.io/control-plane: ""
      tolerations:
      - key: node.cloudprovider.kubernetes.io/uninitialized
        value: "true"
        effect: NoSchedule
      - key: node-role.kubernetes.io/control-plane
        effect: NoSchedule
      - key: node-role.kubernetes.io/master
        effect: NoSchedule
      serviceAccountName: cloud-controller-manager
      containers:
      - name: aws-cloud-controller-manager
        image: registry.k8s.io/provider-aws/cloud-controller-manager:v1.28.1
        args:
        - --v=2
        - --cloud-provider=aws
        - --cluster-name=${CLUSTER_NAME}
        - --cluster-cidr=10.244.0.0/16
        - --configure-cloud-routes=false
        - --use-service-account-credentials=true
        resources:
          requests:
            cpu: 200m
        env:
        - name: AWS_REGION
          value: us-west-2  # Change to your region
      hostNetwork: true
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: cloud-controller-manager
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  annotations:
    rbac.authorization.kubernetes.io/autoupdate: "true"
  name: system:cloud-controller-manager
rules:
- apiGroups:
  - ""
  resources:
  - events
  verbs:
  - create
  - patch
  - update
- apiGroups:
  - ""
  resources:
  - nodes
  verbs:
  - '*'
- apiGroups:
  - ""
  resources:
  - nodes/status
  verbs:
  - patch
- apiGroups:
  - ""
  resources:
  - services
  verbs:
  - list
  - patch
  - update
  - watch
- apiGroups:
  - ""
  resources:
  - services/status
  verbs:
  - list
  - patch
  - update
  - watch
- apiGroups:
  - ""
  resources:
  - serviceaccounts
  verbs:
  - create
- apiGroups:
  - ""
  resources:
  - persistentvolumes
  verbs:
  - get
  - list
  - update
  - watch
- apiGroups:
  - ""
  resources:
  - endpoints
  verbs:
  - create
  - get
  - list
  - watch
  - update
- apiGroups:
  - coordination.k8s.io
  resources:
  - leases
  verbs:
  - create
  - get
  - list
  - watch
  - update
- apiGroups:
  - ""
  resources:
  - secrets
  verbs:
  - get
  - list
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: system:cloud-controller-manager
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:cloud-controller-manager
subjects:
- kind: ServiceAccount
  name: cloud-controller-manager
  namespace: kube-system
EOF
```

### 6.2 Deploy AWS Cloud Controller Manager
```bash
# Replace CLUSTER_NAME in the manifest
sed -i "s/\${CLUSTER_NAME}/$CLUSTER_NAME/g" aws-cloud-controller-manager.yaml

# Apply the manifest
kubectl apply -f aws-cloud-controller-manager.yaml
```

---

## Step 7: Verify Installation

### 7.1 Check CCM Pod Status
```bash
kubectl get pods -n kube-system -l k8s-app=aws-cloud-controller-manager
kubectl logs -n kube-system -l k8s-app=aws-cloud-controller-manager
```

### 7.2 Check Node Status
```bash
kubectl get nodes -o wide
# Nodes should show external IP addresses and provider IDs
```

---

## Step 8: Test LoadBalancer Service

### 8.1 Create Test Application
```bash
cat > test-loadbalancer.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.20
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-loadbalancer
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"  # Use NLB
    service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 80
    protocol: TCP
  selector:
    app: nginx
EOF

kubectl apply -f test-loadbalancer.yaml
```

### 8.2 Verify LoadBalancer Creation
```bash
# Check service status
kubectl get svc nginx-loadbalancer

# Wait for EXTERNAL-IP to be assigned
kubectl get svc nginx-loadbalancer -w

# Test access
curl http://EXTERNAL-IP
```

---

## Troubleshooting

### Common Issues and Solutions

1. **CCM Pod CrashLoopBackOff**
   - Check IAM permissions
   - Verify instance profile attachment
   - Check cluster name in CCM configuration

2. **LoadBalancer Stuck in Pending**
   - Verify subnet tagging
   - Check security group rules
   - Ensure proper IAM permissions for ELB operations

3. **Nodes Not Getting External IPs**
   - Verify kubelet configuration with `--cloud-provider=external`
   - Check provider-id format in kubelet config

4. **Health Check Failures**
   - Verify security group allows traffic on NodePort range (30000-32767)
   - Check target group health in AWS console

### Useful Commands for Debugging
```bash
# Check CCM logs
kubectl logs -n kube-system -l k8s-app=aws-cloud-controller-manager -f

# Describe service events
kubectl describe svc nginx-loadbalancer

# Check node provider IDs
kubectl get nodes -o jsonpath='{.items[*].spec.providerID}'

# Verify AWS resources
aws elbv2 describe-load-balancers
aws elbv2 describe-target-groups
```

---

## Important Notes

1. **Region Configuration**: Update AWS region in CCM deployment and ensure all resources are in the same region
2. **Cluster CIDR**: Adjust cluster CIDR in CCM configuration to match your cluster setup
3. **Load Balancer Type**: Use annotations to specify ALB vs NLB based on your requirements
4. **Cost Considerations**: LoadBalancers incur AWS charges; monitor usage
5. **Security**: Regularly review and update IAM policies and security group rules

---

## Additional Annotations for LoadBalancer Services

```yaml
# Common LoadBalancer annotations
metadata:
  annotations:
    # Load balancer type (classic, nlb, alb)
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
    
    # Internet-facing or internal
    service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
    
    # Specify subnets
    service.beta.kubernetes.io/aws-load-balancer-subnets: "subnet-12345,subnet-67890"
    
    # SSL certificate ARN
    service.beta.kubernetes.io/aws-load-balancer-ssl-cert: "arn:aws:acm:region:account:certificate/cert-id"
    
    # Backend protocol
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: "tcp"
    
    # Health check settings
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-interval: "30"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-timeout: "5"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-healthy-threshold: "2"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-unhealthy-threshold: "2"
```

This completes the setup for enabling LoadBalancer service type in your kubeadm cluster on AWS EC2!
