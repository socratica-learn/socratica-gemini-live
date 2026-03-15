output "project_number" {
  value = data.google_project.current.number
}

output "artifact_registry_repository" {
  value = google_artifact_registry_repository.containers.repository_id
}

output "artifact_registry_location" {
  value = google_artifact_registry_repository.containers.location
}

output "backend_service_account_email" {
  value = google_service_account.backend.email
}

output "frontend_service_account_email" {
  value = google_service_account.frontend.email
}

output "secret_names" {
  value = { for key, secret in google_secret_manager_secret.app : key => secret.secret_id }
}

output "backend_url" {
  value = var.deploy_services ? google_cloud_run_v2_service.backend[0].uri : ""
}

output "frontend_url" {
  value = var.deploy_services ? google_cloud_run_v2_service.frontend[0].uri : ""
}
