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
variable "vpc_id" {
  type        = string
  description = "VPC ID where the security groups will be created"
}   