resource "aws_security_group" "PublicEC2SecurityGroup" {
  name        = var.public_ec2_sg_name
  description = "Security group for public EC2 instances"
  vpc_id      = var.vpc_id

  dynamic "ingress" {
    for_each = local.ingress_rules
    content {
      from_port   = ingress.value.from_port
      to_port     = ingress.value.to_port
      protocol    = ingress.value.protocol
      cidr_blocks = ingress.value.cidr_blocks
      description = ingress.value.description
    }
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }
  tags = {
    Name = var.public_ec2_sg_name
  }
}

resource "aws_security_group" "PrivateEC2SecurityGroup" {
  name        = var.private_ec2_sg_name
  tags = {
    Name = var.private_ec2_sg_name
  }
  description = "Security group for private EC2 instances"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [aws_security_group.PublicEC2SecurityGroup.id]
    description     = "Allow all traffic from PublicEC2SecurityGroup only"
  }
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
    description = "Allow all traffic within PrivateEC2SecurityGroup"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }
}