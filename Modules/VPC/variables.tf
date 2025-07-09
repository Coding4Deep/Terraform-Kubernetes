variable "vpc_name" {
  type = string
}
variable "vpc_cidr_block" {
  type = string
}


# PUBLIC SUBNET VARIABLES
variable "public_subnet_cidr_block" {
  type        = string
  description = "The CIDR block for the public subnet."
}
variable "public_subnet_az" {
  type        = string
  description = "The availability zone for the public subnet."
}

# PRIVATE SUBNET VARIABLES
variable "private_subnet_cidr_block" {
  type        = string
  description = "The CIDR block for the private subnet."
}
variable "private_subnet_az" {
  type        = string
  description = "The availability zone for the private subnet."
}
