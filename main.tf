terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 2.0"
    }
  }
  required_version = ">= 0.12"
  backend "s3" {
    bucket  = "deepak-kubernetes-project-bucket"
    key     = "terraform/state"
    region  = "us-east-1"
    encrypt = true
  }
}

# provider "vault" {
#   # address         = var.vault_addr
#   # token           = var.vault_token
#   skip_tls_verify = true
# }

# data "vault_generic_secret" "aws_creds" {
#   path = "aws-creds/myapp"
# }

provider "aws" {
  # access_key = data.vault_generic_secret.aws_creds.data["access_key"]
  # secret_key = data.vault_generic_secret.aws_creds.data["secret_key"]
  region     = var.aws_region
}

# module "S3" {
#   source = "./Modules/S3"
# }

module "VPC" {
  source         = "./Modules/VPC"
  vpc_cidr_block = var.vpc_cidr_block
  vpc_name       = var.vpc_name

  public_subnet_cidr_block = var.public_subnet_cidr_block
  public_subnet_az         = var.public_subnet_az

  private_subnet_cidr_block = var.private_subnet_cidr_block
  private_subnet_az         = var.private_subnet_az
}

module "SecurityGroup" {
  source              = "./Modules/SecurityGroup"
  public_ec2_sg_name  = var.public_ec2_sg_name
  private_ec2_sg_name = var.private_ec2_sg_name
  vpc_id              = module.VPC.vpc_id
}

module "EC2" {
  source = "./Modules/EC2"
  pem_file_name   = var.pem_file_name
  volume_size      = var.volume_size
  volume_type      = var.volume_type

  public_instances = var.public_instances
  public_subnet_id = module.VPC.public_subnet_id
  public_sg_id     = module.SecurityGroup.public_ec2_sg_id

  private_instances = var.private_instances
  private_subnet_id = module.VPC.private_subnet_id
  private_sg_id     = module.SecurityGroup.private_ec2_sg_id
}

