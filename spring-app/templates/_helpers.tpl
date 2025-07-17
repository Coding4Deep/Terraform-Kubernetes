{{/*
Generate a full name: release-name + chart name (e.g., spring-app-myapp)
*/}}
{{- define "spring-app.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end }}


{{- define "namespaceName" -}}
{{- default "default" .Values.namespace -}}
{{- end }}




{{/*
Define optional nodePort for Service
*/}}
{{- define "spring-app.nodePort" -}}
{{- if and (or (eq .Values.deployment.service.type "NodePort") (eq .Values.deployment.service.type "LoadBalancer")) .Values.deployment.service.nodePort }}
nodePort: {{ .Values.deployment.service.nodePort }}
{{- end }}
{{- end }}


{{/*
Custom + common labels
*/}}
{{- define "spring-app.labels" -}}
{{- with .Values.labels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
NFS Template
*/}}
{{- define "nfs.details" -}}
{{- with .Values.nfs }}
{{- toYaml . }}
{{- end }}
{{- end }}