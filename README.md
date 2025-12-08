# RDR Graphics Editor

Editor de configurações gráficas para Red Dead Redemption Mobile (Netflix Version) e outros jogos que usam arquivos XML para configurações.

## Características

- ✨ Material You / Material 3 com Dynamic Colors
- 🔧 Editor dinâmico de configurações XML
- 📁 Usa Storage Access Framework (SAF) nativo do Android
- 🚫 **Não requer ROOT** - usa o seletor de arquivos padrão do Android
- 📱 Interface moderna com Jetpack Compose
- 🔍 Leitura e escrita segura de arquivos via SAF
- 💾 Modifica apenas as linhas alteradas, preservando o resto do XML

## Requisitos

- Android 8.0 (API 26) ou superior
- Acesso aos arquivos de configuração do jogo
- **Não requer acesso root**

## Como Usar

1. Instale o APK
2. Abra o app
3. Clique em "Select File" para escolher o arquivo XML
4. Navegue até a pasta de dados do jogo usando o seletor de arquivos do Android
5. Selecione o arquivo `graphics.xml` ou outro arquivo XML de configuração
6. Edite as configurações desejadas
7. Clique em "Apply Changes" para salvar
8. Reinicie o jogo

## Configurações Editáveis

O editor carrega dinamicamente todos os campos do XML, incluindo:

### Graphics Settings (graphics.xml)
- Resolução (Width/Height)
- VSync e Frame Rate Limit
- Qualidade de Sombras
- Anti-Aliasing
- Anisotropic Filtering
- Motion Blur
- Dynamic Resolution
- Triple Buffering
- World/Terrain/Tree/Grass Streaming
- HDR (Peak Brightness, Paper White)
- FSR3 Upscaling
- DLSS Upscaling
- Screen Percentage
- E muito mais...

## Storage Access Framework (SAF)

Este aplicativo usa o Storage Access Framework do Android, que permite:
- Selecionar arquivos de qualquer local acessível
- Ler e escrever em arquivos com permissão do usuário
- Manter permissões persistentes para arquivos selecionados
- Funcionar sem necessidade de acesso root

## Build

```bash
./gradlew assembleDebug
```

## Caminhos Comuns dos Arquivos

Para RDR Mobile (Netflix):
- Graphics: `/data/data/com.netflix.NGP.Kamo/files/graphics.xml`
- Ou: `/Android/data/com.netflix.NGP.Kamo/files/`

**Nota:** Use o seletor de arquivos do Android para navegar até esses locais.

## Troubleshooting

Se não conseguir selecionar o arquivo:
1. Certifique-se de que o jogo está instalado
2. Use um gerenciador de arquivos com acesso root para verificar a localização exata
3. Alguns arquivos podem requerer acesso root no sistema (nesse caso, use um gerenciador de arquivos com root para copiar o arquivo para uma pasta acessível)

Se "Apply Changes" não funcionar:
1. Verifique se o arquivo foi selecionado corretamente
2. Certifique-se de ter permissão de escrita no arquivo
3. Tente copiar o arquivo para uma pasta acessível (como Downloads) primeiro

## Licença

Este projeto é fornecido como está para fins educacionais.
