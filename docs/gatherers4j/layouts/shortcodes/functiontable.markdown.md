{{- $children := where .Page.Pages ".Params.show_in_table" true -}}
{{- if gt (len $children) 0 }}
### Gatherers

| Function | Description |
|---|---|
{{ range $children -}}
{{- $link := .Permalink }}{{ with .OutputFormats.Get "markdown" }}{{ $link = .Permalink }}{{ end -}}
| [{{ .Title }}]({{ $link }}) | {{ replace (.Params.description | default "") "|" "\\|" }} |
{{ end }}
{{- end -}}
