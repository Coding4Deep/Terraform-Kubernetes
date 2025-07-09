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



# COPY PEM FILE TO THE MASTER EC2 ONLY 
resource "null_resource" "copy_pem_to_master" {
  depends_on = [aws_instance.public_ec2]
  
  triggers = {
    instance_id = aws_instance.public_ec2["master"].id
  } 

  connection {
    host        = aws_instance.public_ec2["master"].public_ip
    user        = "ubuntu"
    type        = "ssh"
    private_key = file("${pathexpand("~")}/${var.pem_file_name}.pem")
  }

  provisioner "file" {
    source      = "${pathexpand("~")}/${var.pem_file_name}.pem"
    destination = "/home/ubuntu/${var.pem_file_name}.pem"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod 400 /home/ubuntu/${var.pem_file_name}.pem",
      "chown ubuntu:ubuntu /home/ubuntu/${var.pem_file_name}.pem"
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

