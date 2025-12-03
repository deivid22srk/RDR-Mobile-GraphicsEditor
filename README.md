# RDR Graphics Editor

Editor de configurações gráficas e de idioma para Red Dead Redemption Mobile (Netflix Version).

## Características

- ✨ Material You / Material 3 com Dynamic Colors
- 🔧 Editor completo de configurações gráficas (graphics.xml)
- 🌍 Modificação de idioma preservando configurações existentes (netflix.dat)
- 🔐 Acesso Root necessário
- 📱 Interface moderna com Jetpack Compose
- 🔍 Feedback detalhado durante operações
- 💾 Preserva dados existentes ao modificar idioma

## Requisitos

- Android 8.0 (API 26) ou superior
- Acesso Root (Magisk, KernelSU, etc.)
- Red Dead Redemption Mobile instalado (com.netflix.NGP.Kamo)

## Configurações Editáveis

### Graphics Settings (graphics.xml)
Modifica **TODAS** as configurações do arquivo:
- Resolução (Width: 640-3840, Height: 360-2160)
- VSync e Frame Rate Limit (0-240 FPS)
- Qualidade de Sombras (0-4)
- Anti-Aliasing (0-4)
- Anisotropic Filtering (0-16)
- Motion Blur (Style 0-2, Strength 0-1)
- Dynamic Resolution
- Triple Buffering
- World/Terrain/Tree/Grass Streaming
- HDR com Peak Brightness e Paper White
- FSR3 Upscaling (Quality 0-4 + Sharpness)
- DLSS Upscaling (Quality 0-4)
- Screen Percentage (0.5-2.0)
- Mobile Preset (0-4)

### Language Settings (netflix.dat)
Modifica **APENAS** a linha `LANGUAGE=` preservando todo o resto do arquivo:
- 15 idiomas suportados:
  - English (US)
  - Português (Brasil)
  - Español (España/México)
  - Français
  - Deutsch
  - Italiano
  - 日本語
  - 한국어
  - 中文 (简体/繁體)
  - Русский
  - Polski
  - Türkçe
  - العربية

## Como Usar

1. Instale o APK
2. Abra o app
3. Conceda acesso root quando solicitado
4. Configure as opções desejadas
5. Clique em "Apply Changes"
6. Reinicie o jogo

## Build

```bash
./gradlew assembleDebug
```

## GitHub Actions

O projeto inclui um workflow do GitHub Actions que compila automaticamente o APK debug em cada push.

## Caminhos dos Arquivos

- Graphics: `/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml`
- Language: `/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat`

## Troubleshooting

Se "Apply Changes" não funcionar:
1. Verifique se o acesso root foi concedido
2. Verifique se o jogo está instalado
3. Verifique se os caminhos dos arquivos existem
4. Tente reiniciar o dispositivo

## Licença

Este projeto é fornecido como está para fins educacionais.
