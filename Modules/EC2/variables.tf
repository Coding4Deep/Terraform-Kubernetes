variable "pem_file_name" {
  description = "The name of the PEM file to create"
  type        = string
}   

variable "public_instances" {
  description = "Map of public instance configurations"
  type        = map(object({
    ami           = string
    instance_type = string
  }))
}
variable "public_subnet_id" {
  description = "ID of the public subnet where EC2 instances will be launched"
  type        = string
}
variable "public_sg_id" {
  description = "ID of the security group for public EC2 instances"
  type        = string
}
variable "volume_size" {
  description = "Size of the root volume in GB"
  type        = number
}
variable "volume_type" {
  description = "Type of the root volume"
  type        = string
}

