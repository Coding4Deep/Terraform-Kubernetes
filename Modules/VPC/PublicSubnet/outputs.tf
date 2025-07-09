output "public_subnet_id" {
  value       = aws_subnet.public_subnet.id
  description = "The ID of the public subnet."
}
