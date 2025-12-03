# RDR Graphics Editor

Editor de configurações gráficas e de idioma para Red Dead Redemption Mobile (Netflix Version).

## Características

- ✨ Material You / Material 3 com Dynamic Colors
- 🔧 Editor completo de configurações gráficas (graphics.xml)
- 🌍 Configuração de idioma (netflix.dat)
- 🔐 Acesso Root necessário
- 📱 Interface moderna com Jetpack Compose

## Requisitos

- Android 8.0 (API 26) ou superior
- Acesso Root
- Red Dead Redemption Mobile (com.netflix.NGP.Kamo)

## Configurações Editáveis

### Graphics Settings
- Resolução (Width/Height)
- VSync e Frame Rate Limit
- Qualidade de Sombras
- Anti-Aliasing
- Motion Blur
- Dynamic Resolution
- FSR3/DLSS Upscaling
- E muito mais...

### Language Settings
- Profile ID
- Idioma do jogo (suporte para 15+ idiomas)

## Build

```bash
./gradlew assembleDebug
```

## GitHub Actions

O projeto inclui um workflow do GitHub Actions que compila automaticamente o APK debug em cada push.

## Caminhos dos Arquivos

- Graphics: `/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml`
- Language: `/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat`

## Licença

Este projeto é fornecido como está para fins educacionais.
