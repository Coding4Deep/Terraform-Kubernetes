rs.initiate()
kubectl exec -it -n spring  pod/mongo-set-0  -- /bin/bash

mongo

rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "mongo-set-0.mongo-svc.spring.svc.cluster.local:27017" },
    { _id: 1, host: "mongo-set-1.mongo-svc.spring.svc.cluster.local:27017" },
    { _id: 2, host: "mongo-set-2.mongo-svc.spring.svc.cluster.local:27017" }
  ]
});

   rs.status()
   show dbs
   use devopsdb
   db.stats()






✅ Dynamic Provisioning with NFS (Recommended)
You install a NFS provisioner (like nfs-subdir-external-provisioner) that acts as a dynamic PV provisioner using your NFS server as backend.

🔧 Step-by-Step: NFS-backed StorageClass with Dynamic Provisioning
1️⃣ Install NFS Subdir External Provisioner
Helm chart (most common way):

helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner/
helm install nfs-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  --set nfs.server=172.31.86.0 \
  --set nfs.path=/mnt/kubedata \
  --set storageClass.name=nfs-sc

Replace <NFS_SERVER_IP> with your NFS server and /exported/path with your shared directory.

2️⃣ StorageClass Example (auto-created by Helm)
The provisioner creates a StorageClass like:

apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-sc
provisioner: nfs.csi.k8s.io  # or something like "example.com/nfs"
parameters:
  archiveOnDelete: "true"
reclaimPolicy: Retain
volumeBindingMode: Immediate
Use nfs-sc as the storageClassName in your PVCs.

3️⃣ Create PVC Using the NFS StorageClass

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nfs-pvc
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: nfs-sc
  resources:
    requests:
      storage: 1Gi
Once applied, the dynamic provisioner will:
Create a subdirectory in the NFS export
Create a PV
Bind it to your PVC

4️⃣ Mount in Pod

volumes:
  - name: nfs-vol
    persistentVolumeClaim:
      claimName: nfs-pvc






 in a **kubeadm-based Kubernetes cluster running on EC2 (not EKS)**, you **cannot directly use `Service type: LoadBalancer`**, because:

### ❌ Why it doesn't work by default:

`Service type: LoadBalancer` requires integration with a **cloud provider** to provision a public cloud load balancer (e.g., AWS ELB).

* In EKS, this works out of the box.
* In kubeadm (even on EC2), this **won’t work unless you install the cloud integration yourself.**

---

## ✅ Solution: Install **AWS Cloud Controller Manager (CCM)**

To enable `LoadBalancer` support, you must install the **external AWS Cloud Controller Manager** (CCM), which enables your cluster to interact with AWS APIs.

### ✅ Steps to make `LoadBalancer` work:

### 1. **Tag your EC2 instances properly**

The CCM uses EC2 tags to identify cluster nodes.

```bash
aws ec2 create-tags --resources <instance-id> \
  --tags Key=kubernetes.io/cluster/<cluster-name>,Value=owned
```

Repeat this for all nodes (master and workers).

---

### 2. **Install AWS Cloud Controller Manager**

Use the [official AWS CCM manifests](https://github.com/kubernetes/cloud-provider-aws) or Helm chart:

You must disable the in-tree cloud provider when bootstrapping with kubeadm:

#### Edit `/etc/systemd/system/kubelet.service.d/10-kubeadm.conf` on each node if not present then create it

Set:

```bash
Environment="KUBELET_EXTRA_ARGS=--cloud-provider=external"
```

Then reload and restart:

```bash
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

---

### 3. **Install AWS CCM (external cloud provider)**

Apply the YAML or Helm chart from [cloud-provider-aws](https://github.com/kubernetes/cloud-provider-aws):

Example:

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/cloud-provider-aws/<version>/manifests/aws-cloud-controller-manager.yaml
```

Make sure:

* Your nodes can assume an IAM role with permissions like `ec2:DescribeInstances`, `ec2:CreateLoadBalancer`, etc.
* You expose AWS credentials using IRSA, IAM roles, or EC2 instance profiles.

---

### 4. **Now you can use LoadBalancer services**

After the CCM is installed and running, you can deploy a service like:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
spec:
  type: LoadBalancer
  selector:
    app: nginx
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
```

⏳ After a few seconds, you'll see `EXTERNAL-IP` populated with an AWS ELB.


---

## ✅ Summary

| Feature                           | Kubeadm on EC2   | EKS                |
| --------------------------------- | ---------------- | ------------------ |
| `LoadBalancer` service            | ❌ Not by default | ✅ Yes              |
| Requires Cloud Controller Manager | ✅ Yes            | ❌ Already built-in |
| Manual LoadBalancer setup         | ✅ Possible       | ❌ Not needed       |

---
