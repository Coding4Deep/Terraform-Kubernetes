variable "vpc_id" {
  type        = string
  description = "The ID of the VPC"
}

variable "private_subnet_cidr_block" {
  type        = string
  description = "The CIDR block for the private subnet"
}

variable "private_subnet_az" {
  type        = string
  description = "The availability zone for the private subnet"
}

variable "public_subnet_id" {
  type        = string
  description = "The ID of the public subnet"
}

variable "vpc_name" {
  type        = string
  description = "The name of the VPC"
}