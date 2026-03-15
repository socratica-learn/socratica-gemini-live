variable "organization_id" {
  description = "Expected organization ID that owns the target project."
  type        = string
  default     = "809804464459"
}

variable "project_id" {
  description = "Target GCP project ID."
  type        = string
  default     = "project-8d21f1f6-2009-4dcf-bff"
}

variable "region" {
  description = "Primary region for Cloud Run and Artifact Registry."
  type        = string
  default     = "europe-west4"
}

variable "artifact_registry_repository" {
  description = "Artifact Registry repository for application containers."
  type        = string
  default     = "socratica"
}

variable "backend_service_name" {
  description = "Cloud Run service name for the Spring Boot API."
  type        = string
  default     = "socratica-backend"
}

variable "frontend_service_name" {
  description = "Cloud Run service name for the Vue frontend."
  type        = string
  default     = "socratica-frontend"
}

variable "backend_image" {
  description = "Full Artifact Registry image reference for the backend."
  type        = string
  default     = ""
}

variable "frontend_image" {
  description = "Full Artifact Registry image reference for the frontend."
  type        = string
  default     = ""
}

variable "deploy_services" {
  description = "Whether Cloud Run services should be created in this apply."
  type        = bool
  default     = false
}

variable "backend_public_url" {
  description = "Public URL of the backend service, used for OAuth redirect URIs after the first deploy."
  type        = string
  default     = ""
}

variable "frontend_public_url" {
  description = "Public URL of the frontend service, used for frontend redirects and CORS after the first deploy."
  type        = string
  default     = ""
}

variable "deletion_protection" {
  description = "Prevent accidental deletion of Cloud Run services."
  type        = bool
  default     = true
}

variable "backend_min_instances" {
  description = "Minimum number of backend instances."
  type        = number
  default     = 0
}

variable "backend_max_instances" {
  description = "Maximum number of backend instances."
  type        = number
  default     = 5
}

variable "frontend_min_instances" {
  description = "Minimum number of frontend instances."
  type        = number
  default     = 0
}

variable "frontend_max_instances" {
  description = "Maximum number of frontend instances."
  type        = number
  default     = 3
}

variable "backend_memory" {
  description = "Backend Cloud Run memory limit."
  type        = string
  default     = "1Gi"
}

variable "backend_cpu" {
  description = "Backend Cloud Run CPU limit."
  type        = string
  default     = "1"
}

variable "frontend_memory" {
  description = "Frontend Cloud Run memory limit."
  type        = string
  default     = "512Mi"
}

variable "frontend_cpu" {
  description = "Frontend Cloud Run CPU limit."
  type        = string
  default     = "1"
}

variable "mongodb_database" {
  description = "Logical Mongo database name used by the application."
  type        = string
  default     = "socratica_dev"
}

variable "gemini_model" {
  description = "Default Gemini text model."
  type        = string
  default     = "gemini-2.5-flash"
}

variable "gemini_live_model" {
  description = "Default Gemini Live model."
  type        = string
  default     = "gemini-live-2.5-flash-native-audio"
}

variable "mail_host" {
  description = "SMTP host."
  type        = string
  default     = "smtp.gmail.com"
}

variable "mail_port" {
  description = "SMTP port."
  type        = string
  default     = "587"
}

variable "mail_from" {
  description = "Default email sender."
  type        = string
  default     = "noreply@socratica.com"
}
