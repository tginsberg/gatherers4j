{{/*

Template design:

- This file generates a title followed by zero or more sections.
- The title, and each section, are designed under the assumption that it is the
  last page element, and so does not add extra trailing newlines.
- Each section, other than the first, shall introduce a separator line.

Local change: this is Docsy v0.18.0's layouts/all.md plus the infinite-stream and parallel markers (see
layouts/_partials/infinite-streams.html and layouts/_partials/parallel.html), written at the top of the page content.
Docsy's own all.md has no hook for that. Compare against the upstream file when upgrading Docsy.

*/ -}}

# {{ .Title | strings.TrimSpace }}
{{ $needSeparator := false -}}

{{ with .Description | strings.TrimSpace }}
> {{ replace . "\n" "\n> " }}
{{ $needSeparator = true -}}
{{ end -}}

{{ with (partialCached "td/root-page.html" site site.Home.Permalink).OutputFormats.Get "LLMS" }}
{{- if $needSeparator }}
---
{{ end }}
Site [llms.txt]( {{- .RelPermalink -}} )
{{ $needSeparator = true -}}
{{ end -}}

{{ with .RenderShortcodes | strings.TrimSpace }}
{{- if $needSeparator }}
---
{{ end }}
{{ with partial "infinite-streams.html" $ -}}
> **{{ .title }}.** {{ .text }}

{{ end -}}
{{ with partial "parallel.html" $ -}}
> **{{ .title }}.** {{ .text }}

{{ end -}}
{{ . }}
{{ $needSeparator = true -}}
{{ end -}}

{{ with .Pages }}
{{- if $needSeparator }}
---
{{ end }}
Section pages:

{{ range . -}}
- [ {{- .Title | strings.TrimSpace -}} ]( {{- .RelPermalink -}} )
  {{- with .Description | strings.TrimSpace -}}
    : {{ . -}}
  {{ end }}
{{ end -}}

{{ end -}}
