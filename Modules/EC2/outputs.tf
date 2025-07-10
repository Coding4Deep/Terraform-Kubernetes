output "pem_file_path" {
  value = module.PemFile.pem_file_path
}
output "public_instances_public_ips" {
  value = {
    for name, instance in module.PublicEC2.public_instances_public_ips :
    name => instance
  }
}
output "public_instances_private_ips" {
  value = {
    for name, instance in module.PublicEC2.public_instances_private_ips :
    name => instance
  }
}

output "private_instances_private_ips" {
  value = {
    for name, instance in module.PrivateEC2.private_instances_private_ips :
    name => instance
  }
}
