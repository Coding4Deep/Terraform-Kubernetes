resource "aws_vpc" "vpc" {
  cidr_block           = var.vpc_cidr_block
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = var.vpc_name
  }
}


module "public_subnet" {
  source                   = "./PublicSubnet"
  vpc_id                   = aws_vpc.vpc.id
  public_subnet_cidr_block = var.public_subnet_cidr_block
  public_subnet_az         = var.public_subnet_az
  vpc_name                 = var.vpc_name
}

module "private_subnet" {
  source                    = "./PrivateSubnet"
  vpc_id                    = aws_vpc.vpc.id
  private_subnet_cidr_block = var.private_subnet_cidr_block
  private_subnet_az         = var.private_subnet_az
  vpc_name                  = var.vpc_name

  public_subnet_id = module.public_subnet.public_subnet_id
}
