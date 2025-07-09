output "public_subnet_id" {
  value       = module.VPC.public_subnet_id
  description = "The ID of the public subnet created in the VPC."
}

output "private_subnet_id" {
  value       = module.VPC.private_subnet_id
  description = "The ID of the private subnet created in the VPC."
} 

output "vpc_id" {
  value       = module.VPC.vpc_id
  description = "The ID of the VPC created."
} 

output "pem_file_path" {
  value       = module.EC2.pem_file_path
  description = "The path to the PEM file created for SSH access."
} 

output "public_instances_public_ips" {
  value = {
    for name, instance in module.EC2.public_instances_public_ips :
    name => instance
  }
  description = "A map of public IPs for the public EC2 instances."
}
output "public_instances_private_ips" {
  value = {
    for name, instance in module.EC2.public_instances_private_ips :
    name => instance
  }
  description = "A map of private IPs for the public EC2 instances."
}
