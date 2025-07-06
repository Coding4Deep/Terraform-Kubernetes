# 🏗️ Enhanced Kubernetes DevOps Architecture on AWS

## 🎯 High-Level Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           🌐 EXTERNAL SERVICES & TOOLS                              │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  📦 GitHub        🐳 Docker Hub      🔍 SonarQube      🛡️ Security Tools          │
│  (Source Code)    (Container Reg)    (Code Quality)    (Trivy, OWASP, Checkov)      │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │ API Calls & Webhooks
                                        ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                                  ☁️ AWS CLOUD                                        │
│                                                                                      │
│ ┌─────────────────────────────┐              ┌─────────────────────────────────────┐ │
│ │        DEFAULT VPC          │              │           CUSTOM VPC                │ │
│ │      (10.0.0.0/16)          │◄─────────────┤         (172.31.0.0/16)             │ │
│ │                             │ VPC PEERING  │                                     │ │
│ │ ┌─────────────────────────┐ │              │ ┌─────────────────────────────────┐ │ │
│ │ │    PUBLIC SUBNET        │ │              │ │       PUBLIC SUBNET             │ │ │
│ │ │   (10.0.1.0/24)         │ │              │ │      (172.31.1.0/24)            │ │ │
│ │ │                         │ │              │ │                                 │ │ │
│ │ │ ┌─────────────────────┐ │ │              │ │ ┌─────────────────────────────┐ │ │ │
│ │ │ │   🔧 JENKINS +      │ │ │             │ │ │    🌐 INTERNET GATEWAY      │ │ │ │
│ │ │ │   ANSIBLE SERVER    │ │ │              │ │ │                             │ │ │ │
│ │ │ │                     │ │ │              │ │ │ ┌─────────────────────────┐ │ │ │ │
│ │ │ │ • CI/CD Pipeline    │ │ │              │ │ │ │    🔄 NAT GATEWAY      │ │ │ │ │
│ │ │ │ • Infrastructure    │ │ │              │ │ │ │                         │ │ │ │ │
│ │ │ │   Automation        │ │ │              │ │ │ └─────────────────────────┘ │ │ │ │
│ │ │ │ • SSH to K8s Nodes  │ │ │              │ │ └─────────────────────────────┘ │ │ │
│ │ │ └─────────────────────┘ │ │              │ └─────────────────────────────────┘ │ │
│ │ └─────────────────────────┘ │              │                                     │ │
│ └─────────────────────────────┘              │ ┌─────────────────────────────────┐ │ │
│                                              │ │       PRIVATE SUBNET            │ │ │
│                                              │ │      (172.31.2.0/24)            │ │ │
│                                              │ │                                 │ │ │
│                                              │ │  ☸️ KUBERNETES CLUSTER         │ │ │
│                                              │ │                                 │ │ │
│                                              │ │ ┌─────────────────────────────┐ │ │ │
│                                              │ │ │      🎯 MASTER NODE         │ │ │ │
│                                              │ │ │     (t3.medium EC2)         │ │ │ │
│                                              │ │ │                             │ │ │ │
│                                              │ │ │ • API Server (Port 6443)    │ │ │ │
│                                              │ │ │ • etcd Database             │ │ │ │
│                                              │ │ │ • Scheduler                 │ │ │ │
│                                              │ │ │ • Controller Manager        │ │ │ │
│                                              │ │ └─────────────────────────────┘ │ │ │
│                                              │ │                                 │ │ │
│                                              │ │ ┌─────────────────────────────┐ │ │ │
│                                              │ │ │    👷 WORKER NODE 1         │ │ │ │
│                                              │ │ │    (t3.large EC2)            │ │ │ │
│                                              │ │ │                              │ │ │ │
│                                              │ │ │ 📦 APPLICATION PODS:         │ │ │ │
│                                              │ │ │ ├─ 🍃 MongoDB (Database)     │ │ │ │
│                                              │ │ │ ├─ 🐰 RabbitMQ (Message Q)   │ │ │ │
│                                              │ │ │ └─ 📊 Prometheus (Monitor)   │ │ │ │
│                                              │ │ └─────────────────────────────┘  │ │ │
│                                              │ │                                  │ │ │
│                                              │ │ ┌─────────────────────────────┐  │ │ │
│                                              │ │ │    👷 WORKER NODE 2         │  │ │ │
│                                              │ │ │    (t3.large EC2)            │  │ │ │
│                                              │ │ │                              │ │ │ │
│                                              │ │ │ 📦 APPLICATION PODS:         │  │ │ │
│                                              │ │ │ ├─ ⚡ Memcached (Cache)      │  │ │ │
│                                              │ │ │ ├─ 🌱 Spring Boot (API)      │  │ │ │
│                                              │ │ │ └─ 📈 Grafana (Dashboard)    │  │ │ │
│                                              │ │ └─────────────────────────────┘ │ │ │
│                                              │ └─────────────────────────────────┘ │ │
│                                              └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────────┘

```

## 🔄 Detailed CI/CD Pipeline Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                            📋 COMPLETE CI/CD WORKFLOW                                    │
└─────────────────────────────────────────────────────────────────────────────────────────┘

STEP 1: CODE DEVELOPMENT
┌─────────────────────┐
│  👨‍💻 DEVELOPER      │
│                     │
│  • Writes Code      │
│  • Commits Changes  │
│  • Pushes to GitHub │
└─────────────────────┘
           │
           ▼
STEP 2: SOURCE CONTROL
┌─────────────────────┐
│  📦 GITHUB REPO    │
│                     │
│  • Source Code      │
│  • Dockerfile       │
│  • Helm Charts      │
│  • Jenkinsfile      │
└─────────────────────┘
           │ Webhook Trigger
           ▼
STEP 3: CI/CD AUTOMATION
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│  🔧 JENKINS        │      │  🔍 CODE QUALITY    │      │  🛡️ SECURITY SCANS  │
│                     │      │                     │      │                     │
│  • Pipeline Start   │ ────▶│  • SonarQube       │ ────▶│  • Trivy (Images)   │
│  • Code Checkout    │      │  • Code Analysis    │      │  • Checkov (IaC)    │
│  • Build Trigger    │      │  • Quality Gates    │      │  • OWASP ZAP        │
└─────────────────────┘      └─────────────────────┘      └─────────────────────┘
           │                                                        │
           ▼                                                        ▼
STEP 4: CONTAINER BUILD                              STEP 5: SECURITY VALIDATION
┌─────────────────────┐                              ┌─────────────────────┐
│  🐳 DOCKER BUILD   │                              │  ✅ SECURITY PASS   │
│                     │                              │                     │
│  • Build Image      │                              │  • Vulnerability    │
│  • Tag Image        │                              │    Assessment       │
│  • Push to Hub      │                              │  • Compliance Check │
└─────────────────────┘                              └─────────────────────┘
           │                                                        │
           └────────────────────────────────────────────────────────┘
                                      │
                                      ▼
STEP 6: DEPLOYMENT
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│   HELM DEPLOY       │      │   KUBERNETES        │      │  MONITORING         │
│                     │      │                     │      │                     │
│  • Helm Charts      │ ───▶│  • Pod Deployment   │ ────▶│  • Prometheus       │
│  • Config Maps      │      │  • Service Creation │      │  • Grafana          │
│  • Secrets          │      │  • Health Checks    │      │  • Alerts           │
└─────────────────────┘      └─────────────────────┘      └─────────────────────┘
```

## 🌐 Network Communication Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                           🔄 NETWORK TRAFFIC PATTERNS                                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

EXTERNAL TO JENKINS (Default VPC):
Internet ──► Internet Gateway ──► Jenkins Server (Port 8080, 22)

JENKINS TO EXTERNAL SERVICES:
Jenkins ──► GitHub (Port 443) - Code checkout, webhooks
Jenkins ──► Docker Hub (Port 443) - Image push/pull  
Jenkins ──► SonarQube (Port 9000) - Code analysis
Jenkins ──► Security Tools (Port 443) - Vulnerability scans

JENKINS TO KUBERNETES (Via VPC Peering):
Jenkins ──► VPC Peering ──► K8s Master (Port 6443) - API calls
Jenkins ──► VPC Peering ──► K8s Nodes (Port 22) - SSH access

KUBERNETES INTERNAL COMMUNICATION:
Master ──► Workers (Port 10250) - Kubelet API
Master ──► Workers (Port 30000-32767) - NodePort services
Workers ──► Master (Port 6443) - API server
Pods ──► Pods (All ports) - Inter-pod communication

KUBERNETES TO INTERNET (Via NAT Gateway):
K8s Nodes ──► NAT Gateway ──► Internet Gateway ──► Internet
• Container image pulls
• Package updates
• External API calls
```

## 🔐 Security Architecture & Access Control

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                               SECURITY LAYERS                                           │
└─────────────────────────────────────────────────────────────────────────────────────────┘

LAYER 1: NETWORK SECURITY
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                        │
│   DEFAULT VPC SECURITY GROUP (Jenkins/Ansible)                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  INBOUND RULES:                          OUTBOUND RULES:                        │   │
│  │  • SSH (22) ← Your IP Only              • All Traffic → Internet                │   │
│  │  • HTTP (8080) ← Your IP Only           • SSH (22) → Custom VPC                 │   │
│  │  • HTTPS (443) ← Your IP Only           • HTTPS (443) → External APIs           │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                        │
│   CUSTOM VPC SECURITY GROUPS                                                           │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  MASTER NODE:                           WORKER NODES:                           │   │
│  │  • SSH (22) ← Default VPC Only          • SSH (22) ← Default VPC Only           │   │
│  │  • API (6443) ← Default VPC Only        • Kubelet (10250) ← Master Node         │   │
│  │  • etcd (2379-2380) ← Worker Nodes      • NodePort (30000-32767) ← Internet     │   │
│  │  • Kubelet (10250) ← Worker Nodes       • Pod CIDR ← All Cluster Nodes          │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────────────────┘

LAYER 2: ACCESS CONTROL
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                         │
│   SSH KEY MANAGEMENT                                                                    │
│  • Private keys stored securely on Jenkins server                                       │
│  • Public keys deployed to all K8s nodes                                                │
│  • Key rotation policy implemented                                                      │
│                                                                                         │
│   KUBERNETES RBAC                                                                       │
│  • Service accounts for Jenkins deployment                                              │
│  • Role-based permissions for different namespaces                                      │
│  • Cluster admin access restricted                                                      │
│                                                                                         │
│   SECRETS MANAGEMENT                                                                    │
│  • Kubernetes secrets for database passwords                                            │
│  • Docker registry credentials                                                          │
│  • TLS certificates for secure communication                                            │
└─────────────────────────────────────────────────────────────────────────────────────────┘

LAYER 3: APPLICATION SECURITY
┌───────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                       │
│   CONTINUOUS SECURITY SCANNING                        b                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐   │
│  │   📊 SONARQUBE  │ │   🛡️ TRIVY      │  │   🔒 CHECKOV    │  │  ⚡ OWASP ZAP  │   │
│  │                 │  │                 │  │                 │  │                 │   │
│  │ • Code Quality  │  │ • Image Vulns   │  │ • IaC Security  │  │ • Web App Scan  │   │
│  │ • Security      │  │ • OS Packages   │  │ • Misconfig     │  │ • API Testing   │   │
│  │   Hotspots      │  │ • Dependencies  │  │   Detection     │  │ • Penetration   │   │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘   │
└───────────────────────────────────────────────────────────────────────────────────────┘
```

## 📊 Application Architecture & Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                        🏗️ THREE-TIER APPLICATION ARCHITECTURE                           │
└─────────────────────────────────────────────────────────────────────────────────────────┘

PRESENTATION TIER (External Access):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🌐 LOAD BALANCER / INGRESS CONTROLLER                                                  │
│  • Routes external traffic to Spring Boot API                                           │
│  • SSL termination and certificate management                                           │
│  • Rate limiting and DDoS protection                                                    │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
APPLICATION TIER (Worker Node 2):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🌱 SPRING BOOT APPLICATION POD                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  • REST API Endpoints                                                           │   │
│  │  • Business Logic Processing                                                    │   │
│  │  • Authentication & Authorization                                               │   │
│  │  • Connection to Database & Cache                                               │   │
│  │  • Message Queue Integration                                                    │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                        │
│  ⚡ MEMCACHED POD (Caching Layer)                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  • In-memory caching for frequently accessed data                               │   │
│  │  • Session storage for user authentication                                      │   │
│  │  • Reduces database load and improves response time                             │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
DATA TIER (Worker Node 1):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🍃 MONGODB POD (Primary Database)                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  • Document-based NoSQL database                                                │   │
│  │  • Persistent volume for data storage                                           │   │
│  │  • Replica set for high availability                                            │   │
│  │  • Automated backups and recovery                                               │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
│  🐰 RABBITMQ POD (Message Broker)                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  • Asynchronous message processing                                              │   │
│  │  • Queue management for background tasks                                        │   │
│  │  • Event-driven architecture support                                            │   │
│  │  • Dead letter queues for error handling                                        │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

DATA FLOW PATTERN:
User Request → Load Balancer → Spring Boot API → Check Memcached → Query MongoDB
                                                ↓
User Response ← Load Balancer ← Spring Boot API ← Process Data ← RabbitMQ Tasks
```

## 📊 Monitoring & Observability Stack

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                           📈 COMPREHENSIVE MONITORING SETUP                              │
└─────────────────────────────────────────────────────────────────────────────────────────┘

METRICS COLLECTION (Worker Node 1):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  📊 PROMETHEUS POD                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  MONITORING TARGETS:                                                            │   │
│  │  • Kubernetes cluster metrics (nodes, pods, services)                          │   │
│  │  • Application metrics from Spring Boot (/actuator/prometheus)                 │   │
│  │  • MongoDB metrics (connections, operations, performance)                      │   │
│  │  • RabbitMQ metrics (queue depth, message rates)                               │   │
│  │  • Memcached metrics (hit ratio, memory usage)                                 │   │
│  │  • System metrics from EC2 instances (CPU, memory, disk)                      │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

VISUALIZATION (Worker Node 2):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  📈 GRAFANA POD                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │  DASHBOARD CATEGORIES:                                                          │   │
│  │  • Infrastructure Overview (EC2, VPC, Network)                                  │   │
│  │  • Kubernetes Cluster Health (Nodes, Pods, Resources)                           │   │
│  │  • Application Performance (Response times, Error rates)                        │   │
│  │  • Database Monitoring (MongoDB performance, connections)                       │   │
│  │  • Message Queue Status (RabbitMQ queues, throughput)                           │   │
│  │  • CI/CD Pipeline Metrics (Build times, Success rates)                          │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

ALERTING SYSTEM:
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🚨 ALERT MANAGER                                                                       │
│  • High CPU/Memory usage on nodes                                                       │
│  • Pod restart loops or failures                                                        │
│  • Database connection issues                                                           │
│  • Application error rate spikes                                                        │
│  • CI/CD pipeline failures                                                              │
│  • Security scan failures                                                               │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## 🚀 Deployment Strategy & Scaling

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                            ⚖️ AUTO-SCALING CONFIGURATION                                │
└─────────────────────────────────────────────────────────────────────────────────────────┘

HORIZONTAL POD AUTOSCALER (HPA):
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🌱 SPRING BOOT APPLICATION                                                             │
│  • Min Replicas: 2, Max Replicas: 10                                                   │
│  • CPU Threshold: 70%, Memory Threshold: 80%                                           │
│  • Scale up when average CPU > 70% for 2 minutes                                       │
│  • Scale down when average CPU < 30% for 5 minutes                                     │
│                                                                                         │
│  🍃 MONGODB                                                                             │
│  • Replica Set: 3 nodes (Primary + 2 Secondary)                                        │
│  • Automatic failover and data replication                                             │
│  • Persistent volumes with automatic backup                                             │
│                                                                                         │
│  🐰 RABBITMQ                                                                            │
│  • Cluster mode: 3 nodes for high availability                                         │
│  • Queue mirroring across all nodes                                                    │
│  • Auto-scaling based on queue depth                                                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

CLUSTER AUTOSCALER:
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🖥️ EC2 WORKER NODES                                                                   │
│  • Min Nodes: 2, Max Nodes: 6                                                          │
│  • Scale up when pods cannot be scheduled due to resource constraints                  │
│  • Scale down when nodes are underutilized for 10+ minutes                             │
│  • Instance types: t3.large (current), t3.xlarge (scale up option)                     │
└─────────────────────────────────────────────────────────────────────────────────────────┘

DEPLOYMENT STRATEGIES:
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  🔄 ROLLING UPDATES                                                                    │
│  • Zero-downtime deployments                                                           │
│  • Gradual replacement of old pods with new ones                                       │
│  • Automatic rollback on failure                                                       │
│                                                                                        │
│  🔵🟢 BLUE-GREEN DEPLOYMENT (Future Enhancement)                                      │
│  • Complete environment switch                                                         │
│  • Instant rollback capability                                                         │
│  • Full testing before traffic switch                                                  │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## 🛠️ Implementation Checklist

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              ✅ PROJECT IMPLEMENTATION PHASES                          │
└─────────────────────────────────────────────────────────────────────────────────────────┘

PHASE 1: INFRASTRUCTURE SETUP
□ Create Terraform configurations for VPCs and subnets
□ Set up Internet Gateway and NAT Gateway
□ Configure VPC Peering between Default and Custom VPC
□ Create Security Groups with proper rules
□ Launch EC2 instances (Jenkins/Ansible + K8s nodes)
□ Generate and distribute SSH key pairs

PHASE 2: KUBERNETES CLUSTER SETUP
□ Install Docker on all nodes
□ Install kubeadm, kubelet, kubectl
□ Initialize Kubernetes master node
□ Join worker nodes to the cluster
□ Install CNI plugin (Flannel/Calico)
□ Configure kubectl access from Jenkins server

PHASE 3: CI/CD PIPELINE SETUP
□ Install and configure Jenkins with required plugins
□ Set up GitHub webhook integration
□ Configure Docker Hub credentials
□ Install SonarQube, Trivy, Checkov, OWASP ZAP
□ Create Jenkins pipeline (Jenkinsfile)
□ Test end-to-end pipeline execution

PHASE 4: APPLICATION DEPLOYMENT
□ Create Helm charts for all application components
□ Deploy MongoDB with persistent storage
□ Deploy RabbitMQ in cluster mode
□ Deploy Memcached for caching
□ Deploy Spring Boot application
□ Configure services and ingress

PHASE 5: MONITORING & SECURITY
□ Deploy Prometheus for metrics collection
□ Deploy Grafana with custom dashboards
□ Set up AlertManager for notifications
□ Configure RBAC and network policies
□ Implement secrets management
□ Set up automated backups

PHASE 6: TESTING & OPTIMIZATION
□ Perform load testing
□ Test auto-scaling functionality
□ Validate security configurations
□ Test disaster recovery procedures
□ Optimize resource allocation
□ Document operational procedures
```


