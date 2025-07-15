resource "aws_instance" "public_ec2" {
  for_each                     = var.public_instances

  ami                          = each.value.ami
  instance_type                = each.value.instance_type
  subnet_id                    = var.public_subnet_id
  vpc_security_group_ids       = [var.public_sg_id]
  key_name                     = var.pem_file_name

  root_block_device {
    volume_size = var.volume_size        
    volume_type = var.volume_type      
    delete_on_termination = true
  }

  tags = {
    Name    = each.key
    project = "kubernetes"
  }
}

resource "aws_eip" "public_ip" {
  for_each = var.public_instances

  instance = aws_instance.public_ec2[each.key].id
  domain   = "vpc" 
  tags = {
    Name    = "${each.key}-eip"
    project = "kubernetes"
  }
}

# COPY PEM FILE TO THE MASTER EC2 ONLY 
resource "null_resource" "copy_pem_to_master" {
  depends_on = [
    aws_instance.public_ec2,
    aws_eip.public_ip
  ]
  
  triggers = {
    instance_id = aws_instance.public_ec2["master"].id
    eip_id      = aws_eip.public_ip["master"].id
  } 

  connection {
    host        = aws_eip.public_ip["master"].public_ip
    user        = "ubuntu"
    type        = "ssh"
    private_key = file("${pathexpand("~")}/${var.pem_file_name}.pem")
    timeout     = "5m"
    
    # Add retry logic for connection
    agent       = false
  }

  # Wait for instance to be ready
  provisioner "remote-exec" {
    inline = [
      "echo 'Waiting for instance to be ready...'",
      "until [ -f /var/lib/cloud/instance/boot-finished ]; do sleep 2; done",
      "echo 'Instance is ready !'"
    ]
  }

  provisioner "file" {
    source      = "${pathexpand("~")}/${var.pem_file_name}.pem"
    destination = "/home/ubuntu/${var.pem_file_name}.pem"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod 400 /home/ubuntu/${var.pem_file_name}.pem",
      "chown ubuntu:ubuntu /home/ubuntu/${var.pem_file_name}.pem",
      "echo 'PEM file copied and permissions set successfully'"
    ]
  }
}

# COPY PEM FILE TO THE ALL EC2 INSTANCES

# resource "null_resource" "copy_pem_to_all" {
#   for_each = aws_instance.public_ec2

#   triggers = {
#     instance_id = each.value.id
#   }

#   connection {
#     host        = each.value.public_ip
#     user        = "ubuntu"
#     type        = "ssh"
#     private_key = file("${pathexpand("~")}/${var.key_name}.pem")
#   }

#   provisioner "file" {
#     source      = "${pathexpand("~")}/${var.key_name}.pem"
#     destination = "/home/ubuntu/${var.key_name}.pem"
#   }

#   provisioner "remote-exec" {
#     inline = [
#       "chmod 400 /home/ubuntu/${var.key_name}.pem",
#       "chown ubuntu:ubuntu /home/ubuntu/${var.key_name}.pem"
#     ]
#   }
# }

