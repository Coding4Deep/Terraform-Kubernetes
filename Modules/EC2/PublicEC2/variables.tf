variable "public_instances" {
  type = map(object({
    ami           = string
    instance_type = string
  }))
  description = "Map of public instance configurations"
}

variable "pem_file_name" {
  type        = string
  description = "Name of the PEM file (without .pem extension)"
}
variable "public_subnet_id" {
  type        = string
  description = "ID of the public subnet where EC2 instances will be launched"
}
variable "public_sg_id" {
  type        = string
  description = "ID of the security group for public EC2 instances"
}
variable "volume_size" {
  type        = number
  description = "Size of the root volume in GB"
}
variable "volume_type" {
    type        = string
    description = "Type of the root volume"
}

