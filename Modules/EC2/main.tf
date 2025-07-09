module "PemFile" {
  source = "./PemFile"
  pem_file_name = var.pem_file_name
}

module "PublicEC2" {
  source = "./PublicEC2"

  public_instances = var.public_instances
  pem_file_name    = var.pem_file_name
  public_subnet_id = var.public_subnet_id
  public_sg_id     = var.public_sg_id
  volume_size      = var.volume_size
  volume_type      = var.volume_type
}
