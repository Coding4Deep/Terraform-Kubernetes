output "public_subnet_id" {
  value       = module.public_subnet.public_subnet_id
  description = "The ID of the public subnet created in the VPC."
}
output "private_subnet_id" {
  value       = module.private_subnet.private_subnet_id
  description = "The ID of the private subnet created in the VPC."
}

output "vpc_id" {
  value       = aws_vpc.vpc.id
  description = "The ID of the VPC created."
}