{{- $children := where .Page.RegularPagesRecursive ".Params.show_in_table" true -}}
{{- $sorted := sort $children ".Params.linkTitle" -}}
{{- if gt (len $sorted) 0 }}
| Title | Description |
|---|---|
{{ range $sorted -}}
{{- $link := .Permalink }}{{ with .OutputFormats.Get "markdown" }}{{ $link = .Permalink }}{{ end -}}
| [`{{ .Title }}`]({{ $link }}) | {{ replace (.Params.description | default "") "|" "\\|" }} |
{{ end }}
{{- else }}
No child pages available to display.
{{ end -}}
