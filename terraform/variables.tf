variable "requester_access_key" {
  type = string 
}
variable "requester_secret_key" {
  type = string 
}


variable "acceptor_access_key" {
  type = string 
}
variable "acceptor_secret_key" {
  type = string 
}

variable "acceptor_vpc_name" {
    type = string
    default = "k8s-project-vpc"
}

variable "acceptor_account_id" {
    type = string
    default = "665871828081"
}
