resource "aws_instance" "private_ec2" {
  for_each                     = var.private_instances

  ami                          = each.value.ami
  instance_type                = each.value.instance_type
  subnet_id                    = var.private_subnet_id
  vpc_security_group_ids       = [var.private_sg_id]
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

