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
{{- if and (or (eq .Values.service.type "NodePort") (eq .Values.service.type "LoadBalancer")) .Values.service.nodePort }}
nodePort: {{ .Values.service.nodePort }}
{{- end }}
{{- end }}


{{/*
Custom + common labels
*/}}
{{- define "spring-app.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
{{- with .Values.labels }}
{{ toYaml . }}
{{- end }}
{{- end }}
