output "private_instances_private_ips" {
  value = {
    for name, instance in aws_instance.private_ec2 :
    name => instance.private_ip
  }
}   
