variable "aws_region" {
  type    = string
  default = "us-east-1"
}
variable "vault_token" {
  type      = string
  sensitive = true
}
variable "vault_addr" {
  type = string
}


# VPC VARIABLES
variable "vpc_cidr_block" {
  type    = string
  default = "10.0.0.0/16"
}
variable "vpc_name" {
  type    = string
  default = "k8s-project-vpc"
}

# PUBLIC SUBNET VARIABLES
variable "public_subnet_cidr_block" {
  type    = string
  default = "10.0.1.0/24"
}
variable "public_subnet_az" {
  type    = string
  default = "us-east-1a"
}

# PRIVATE SUBNET VARIABLES
variable "private_subnet_cidr_block" {
  type    = string
  default = "10.0.2.0/24"
}
variable "private_subnet_az" {
  type    = string
  default = "us-east-1a"
}

# SECURITY GROUP VARIABLES
variable "public_ec2_sg_name" {
  type        = string
  default     = "K8S_PublicEC2SG"
  description = "Name of the public EC2 security group"
}
variable "private_ec2_sg_name" {
  type        = string
  default     = "K8S_PrivateEC2SG"
  description = "Name of the private EC2 security group"
}


# PEM FILE VARIABLES
variable "pem_file_name" {
  description = "The name of the PEM file to create"
  type        = string
  default     = "k8s-project-key"
}

# EC2 VOLUME VARIABLES
variable "volume_size" {
  description = "Size of the root volume in GB"
  type        = number
  default     = 30  
} 
variable "volume_type" {
  description = "Type of the root volume"
  type        = string
  default     = "gp2"
}


# PUBLIC EC2 VARIABLES
variable "public_instances" {
  description = "Map of public instance configurations"
  type = map(object({
    ami           = string
    instance_type = string
  }))
  default = {
    master = {
      ami           = "ami-020cba7c55df1f615" 
      instance_type = "t2.medium"
    }
  }
}

# PRIVATE EC2 VARIABLES
variable "private_instances" {
  description = "Map of private instance configurations"
  type = map(object({
    ami           = string
    instance_type = string
  }))
  default = {
    worker-01 = {
      ami           = "ami-020cba7c55df1f615" 
      instance_type = "t2.large"
    },
    worker-02 = {
      ami           = "ami-020cba7c55df1f615" 
      instance_type = "t2.large"
    }
  }
}