{{/* Common labels applied to every object. */}}
{{- define "ead.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: ead-platform
{{- end -}}

{{/* Fully-qualified image reference for a service image name. */}}
{{- define "ead.image" -}}
{{- $root := index . 0 -}}
{{- $image := index . 1 -}}
{{ $root.Values.global.imageRegistry }}/{{ $root.Values.global.imageRepository }}/{{ $image }}:{{ $root.Values.global.imageTag }}
{{- end -}}

{{/* MongoDB connection string for a given database, assembled from the Secret at runtime. */}}
{{- define "ead.mongoUri" -}}
{{- $root := index . 0 -}}
{{- $db := index . 1 -}}
mongodb://$(MONGO_USERNAME):$(MONGO_PASSWORD)@mongodb-headless:{{ $root.Values.mongodb.port }}/{{ $db }}?authSource=admin
{{- end -}}

{{/* Environment variables shared by every Spring Boot service. */}}
{{- define "ead.springEnv" -}}
- name: MONGO_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.mongodb.existingSecret }}
      key: username
- name: MONGO_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.mongodb.existingSecret }}
      key: password
- name: RABBITMQ_HOST
  value: rabbitmq
- name: RABBITMQ_PORT
  value: {{ .Values.rabbitmq.port | quote }}
- name: RABBITMQ_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.rabbitmq.existingSecret }}
      key: username
- name: RABBITMQ_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.rabbitmq.existingSecret }}
      key: password
{{- end -}}

{{/* Liveness and readiness probes for Spring Boot actuator. */}}
{{- define "ead.springProbes" -}}
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: {{ .Values.probes.initialDelaySeconds }}
  periodSeconds: {{ .Values.probes.periodSeconds }}
  timeoutSeconds: {{ .Values.probes.timeoutSeconds }}
  failureThreshold: {{ .Values.probes.failureThreshold }}
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
  initialDelaySeconds: 10
  periodSeconds: {{ .Values.probes.periodSeconds }}
  timeoutSeconds: {{ .Values.probes.timeoutSeconds }}
  failureThreshold: {{ .Values.probes.failureThreshold }}
{{- end -}}

{{/* readOnlyRootFilesystem requires a writable tmp mount for the JVM. */}}
{{- define "ead.tmpVolume" -}}
- name: tmp
  emptyDir: {}
{{- end -}}

{{- define "ead.tmpVolumeMount" -}}
- name: tmp
  mountPath: /tmp
{{- end -}}
