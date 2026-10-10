{{- with (.Get 0) }}
{{- with resources.Get . }}{{ .Content }}{{ else }}{{ errorf "The %q shortcode was unable to find %q. See %s" $.Name . $.Position }}{{ end }}
{{- else }}{{ errorf "The %q shortcode requires a single positional parameter; the relative path to a file in the assets directory. See %s" .Name .Position }}{{ end -}}
