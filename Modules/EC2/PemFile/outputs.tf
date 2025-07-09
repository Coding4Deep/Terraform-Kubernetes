output "pem_file_path" {
  value = abspath(local_file.private_key_pem.filename)
}