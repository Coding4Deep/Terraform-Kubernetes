
---

## 🖥️ Architecture Example

| Role       | Hostname/IP       |
|------------|-------------------|
| NFS Server | `192.168.56.10`   |
| NFS Client | `192.168.56.20`   |

---

## ✅ Step 1: Install NFS on the Server

Run the following commands **on the NFS Server**:

```bash
sudo apt update
sudo apt install -y nfs-kernel-server
````

---

## ✅ Step 2: Create and Export a Shared Directory

```bash
sudo mkdir -p /mnt/kubedata # can change accordingt to you 
sudo chown nobody:nogroup /mnt/kubedata
sudo chmod 777 /mnt/kubedata
```

### Edit the exports file:

```bash
sudo vi /etc/exports
```

Add the following line (adjust IP or subnet as needed):

```bash
/mnt/kubedata 192.168.56.0/24(rw,sync,no_subtree_check,no_root_squash)
```

> 🔹 **no\_root\_squash**: Allows the root user on clients to write to the share
> 🔹 **Security Tip**: For better control, use a specific IP instead of a subnet

### Apply the export configuration:

```bash
sudo exportfs -rav
```

### Enable and start the NFS service:

```bash
sudo systemctl enable nfs-server
sudo systemctl start nfs-server
```

---

## ✅ Step 3: Install NFS Utilities on the Client

Run the following **on the NFS Client**:

```bash
sudo apt update
sudo apt install -y nfs-common
```

---

## ✅ Step 4: Mount the NFS Share on the Client

```bash
sudo mkdir -p /mnt/kubedata
sudo mount 192.168.56.10:/mnt/kubedata /mnt/kubedata
```

> 🔸 Replace `192.168.56.10` with your actual NFS server's IP.

### Test the mount:

```bash
touch /mnt/kubedata/testfile
ls -l /mnt/kubedata
```

---

## ✅ Step 5: Make the Mount Persistent (Optional)

Edit the `/etc/fstab` on the **client** to mount it at boot:

```bash
sudo nano /etc/fstab
```

Add this line:

```bash
192.168.56.10:/mnt/kubedata /mnt/kubedata nfs defaults 0 0
```

### Then test:

```bash
sudo umount /mnt/kubedata
sudo mount -a
```

---

## 🔧 NFS Troubleshooting

| Problem                    | Fix                                                                   |
| -------------------------- | --------------------------------------------------------------------- |
| 🔥 Firewall blocks access  | Ensure port `2049` is open on both server and client                  |
| ❌ Mount fails              | Check `/etc/exports`, run `exportfs -v`, verify IPs and access rights |
| 🔒 Permission issues       | Temporarily try `chmod 777` on the shared directory for debugging     |
| ❗ Not mounted after reboot | Ensure correct entry in `/etc/fstab`                                  |



