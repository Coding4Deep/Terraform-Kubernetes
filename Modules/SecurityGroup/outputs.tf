output "public_ec2_sg_id" {
  value       = aws_security_group.PublicEC2SecurityGroup.id
  description = "ID of the public EC2 security group"
}
output "private_ec2_sg_id" {
  value       = aws_security_group.PrivateEC2SecurityGroup.id
  description = "ID of the private EC2 security group"
} 