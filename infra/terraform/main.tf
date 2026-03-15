data "google_project" "current" {
  project_id = var.project_id
}

locals {
  enabled_services = toset([
    "aiplatform.googleapis.com",
    "artifactregistry.googleapis.com",
    "cloudbuild.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "serviceusage.googleapis.com",
  ])

  backend_project_url  = trimspace(var.backend_public_url) != "" ? trimspace(var.backend_public_url) : "http://localhost:8080"
  frontend_project_url = trimspace(var.frontend_public_url) != "" ? trimspace(var.frontend_public_url) : "http://localhost:5173"

  base_secret_ids = {
    mongodb_uri             = "socratica-mongodb-uri"
    jwt_secret              = "socratica-jwt-secret"
    google_client_id        = "socratica-google-client-id"
    google_client_secret    = "socratica-google-client-secret"
    mail_username           = "socratica-mail-username"
    mail_password           = "socratica-mail-password"
  }

  secret_ids = local.base_secret_ids

  build_principals = toset([
    "serviceAccount:${data.google_project.current.number}@cloudbuild.gserviceaccount.com",
    "serviceAccount:${data.google_project.current.number}-compute@developer.gserviceaccount.com",
  ])

  build_roles = toset([
    "roles/artifactregistry.writer",
    "roles/logging.logWriter",
    "roles/storage.admin",
  ])

  build_role_bindings = {
    for pair in setproduct(local.build_principals, local.build_roles) :
    "${pair[0]}|${pair[1]}" => {
      member = pair[0]
      role   = pair[1]
    }
  }

  backend_roles = toset([
    "roles/aiplatform.user",
    "roles/secretmanager.secretAccessor",
  ])
}

resource "google_project_service" "required" {
  for_each           = local.enabled_services
  project            = var.project_id
  service            = each.value
  disable_on_destroy = false
}

resource "terraform_data" "project_org_guardrail" {
  input = data.google_project.current.org_id

  lifecycle {
    precondition {
      condition     = tostring(data.google_project.current.org_id) == tostring(var.organization_id)
      error_message = "Project ${var.project_id} is not attached to organization ${var.organization_id}."
    }
  }
}

resource "terraform_data" "service_image_guardrail" {
  count = var.deploy_services ? 1 : 0

  lifecycle {
    precondition {
      condition     = trimspace(var.backend_image) != "" && trimspace(var.frontend_image) != ""
      error_message = "backend_image and frontend_image must both be set when deploy_services=true."
    }
  }
}

resource "google_artifact_registry_repository" "containers" {
  location      = var.region
  project       = var.project_id
  repository_id = var.artifact_registry_repository
  format        = "DOCKER"
  description   = "Containers for Socratica Cloud Run services."

  depends_on = [
    google_project_service.required["artifactregistry.googleapis.com"],
  ]
}

resource "google_service_account" "backend" {
  account_id   = "socratica-backend"
  display_name = "Socratica backend runtime"
  project      = var.project_id

  depends_on = [
    google_project_service.required["iam.googleapis.com"],
  ]
}

resource "google_service_account" "frontend" {
  account_id   = "socratica-frontend"
  display_name = "Socratica frontend runtime"
  project      = var.project_id

  depends_on = [
    google_project_service.required["iam.googleapis.com"],
  ]
}

resource "google_project_iam_member" "backend_roles" {
  for_each = local.backend_roles
  project  = var.project_id
  role     = each.value
  member   = "serviceAccount:${google_service_account.backend.email}"
}

resource "google_project_iam_member" "build_roles" {
  for_each = local.build_role_bindings
  project  = var.project_id
  role     = each.value.role
  member   = each.value.member
}

resource "google_secret_manager_secret" "app" {
  for_each  = local.secret_ids
  project   = var.project_id
  secret_id = each.value

  replication {
    auto {}
  }

  depends_on = [
    google_project_service.required["secretmanager.googleapis.com"],
  ]
}

resource "google_cloud_run_v2_service" "backend" {
  count               = var.deploy_services ? 1 : 0
  name                = var.backend_service_name
  location            = var.region
  project             = var.project_id
  ingress             = "INGRESS_TRAFFIC_ALL"
  deletion_protection = var.deletion_protection

  template {
    service_account = google_service_account.backend.email
    timeout         = "300s"

    scaling {
      min_instance_count = var.backend_min_instances
      max_instance_count = var.backend_max_instances
    }

    containers {
      image = var.backend_image

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = var.backend_cpu
          memory = var.backend_memory
        }
      }

      env {
        name  = "GOOGLE_CLOUD_PROJECT"
        value = var.project_id
      }

      env {
        name  = "GOOGLE_CLOUD_REGION"
        value = var.region
      }

      env {
        name  = "MONGODB_DATABASE"
        value = var.mongodb_database
      }

      env {
        name  = "FRONTEND_URL"
        value = local.frontend_project_url
      }

      env {
        name  = "CORS_ALLOWED_ORIGINS"
        value = local.frontend_project_url
      }

      env {
        name  = "GOOGLE_REDIRECT_URI"
        value = "${local.backend_project_url}/api/auth/oauth/google/callback"
      }

      env {
        name  = "GEMINI_MODEL"
        value = var.gemini_model
      }

      env {
        name  = "GEMINI_LIVE_MODEL"
        value = var.gemini_live_model
      }

      env {
        name  = "MAIL_HOST"
        value = var.mail_host
      }

      env {
        name  = "MAIL_PORT"
        value = var.mail_port
      }

      env {
        name  = "MAIL_FROM"
        value = var.mail_from
      }

      env {
        name = "SPRING_DATA_MONGODB_URI"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["mongodb_uri"].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["jwt_secret"].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "GOOGLE_CLIENT_ID"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["google_client_id"].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "GOOGLE_CLIENT_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["google_client_secret"].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "MAIL_USERNAME"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["mail_username"].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "MAIL_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["mail_password"].secret_id
            version = "latest"
          }
        }
      }

      startup_probe {
        failure_threshold = 12
        period_seconds    = 10
        timeout_seconds   = 5

        http_get {
          path = "/api/auth/health"
          port = 8080
        }
      }

      liveness_probe {
        failure_threshold = 3
        period_seconds    = 30
        timeout_seconds   = 5

        http_get {
          path = "/api/auth/health"
          port = 8080
        }
      }
    }
  }

  traffic {
    percent = 100
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
  }

  depends_on = [
    terraform_data.project_org_guardrail,
    terraform_data.service_image_guardrail,
    google_project_service.required["run.googleapis.com"],
    google_project_iam_member.backend_roles,
    google_secret_manager_secret.app,
  ]
}

resource "google_cloud_run_v2_service" "frontend" {
  count               = var.deploy_services ? 1 : 0
  name                = var.frontend_service_name
  location            = var.region
  project             = var.project_id
  ingress             = "INGRESS_TRAFFIC_ALL"
  deletion_protection = var.deletion_protection

  template {
    service_account = google_service_account.frontend.email
    timeout         = "300s"

    scaling {
      min_instance_count = var.frontend_min_instances
      max_instance_count = var.frontend_max_instances
    }

    containers {
      image = var.frontend_image

      ports {
        container_port = 80
      }

      resources {
        limits = {
          cpu    = var.frontend_cpu
          memory = var.frontend_memory
        }
      }

      env {
        name  = "BACKEND_UPSTREAM"
        value = google_cloud_run_v2_service.backend[0].uri
      }
    }
  }

  traffic {
    percent = 100
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
  }

  depends_on = [
    terraform_data.project_org_guardrail,
    terraform_data.service_image_guardrail,
    google_project_service.required["run.googleapis.com"],
  ]
}

resource "google_cloud_run_v2_service_iam_member" "backend_public" {
  count    = var.deploy_services ? 1 : 0
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.backend[0].name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_v2_service_iam_member" "frontend_public" {
  count    = var.deploy_services ? 1 : 0
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.frontend[0].name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
