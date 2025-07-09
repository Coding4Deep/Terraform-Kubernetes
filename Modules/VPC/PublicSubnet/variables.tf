variable "vpc_id" {
  type        = string
  description = "The ID of the VPC where the public subnet will be created."
}
variable "public_subnet_cidr_block" {
  type        = string
  description = "The CIDR block for the public subnet."
}
variable "public_subnet_az" {
  type        = string
  description = "The availability zone for the public subnet."
}
variable "vpc_name" {
  type        = string
  description = "The name of the VPC to which the public subnet belongs."
}

