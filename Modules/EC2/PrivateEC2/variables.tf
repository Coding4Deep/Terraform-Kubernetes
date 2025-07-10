variable "private_instances" {
  type = map(object({
    ami           = string
    instance_type = string
  }))
  description = "Map of private instance configurations"
}

variable "pem_file_name" {
  type        = string
  description = "Name of the PEM file (without .pem extension)"
}
variable "private_subnet_id" {
  type        = string
  description = "ID of the private subnet where EC2 instances will be launched"
}
variable "private_sg_id" {
  type        = string
  description = "ID of the security group for private EC2 instances"
}
variable "volume_size" {
  type        = number
  description = "Size of the root volume in GB"
}
variable "volume_type" {
    type        = string
    description = "Type of the root volume"
}

