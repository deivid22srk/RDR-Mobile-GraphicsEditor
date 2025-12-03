# Dynamic Graphics Editor - Atualização

## Mudanças Implementadas

### 1. Leitura Dinâmica do XML
- O aplicativo agora lê o arquivo `graphics.xml` diretamente do dispositivo em `/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml`
- Não há mais valores hardcoded - tudo é carregado dinamicamente
- Ao iniciar, o app solicita acesso root automaticamente

### 2. Geração Dinâmica da Interface
- A UI é gerada automaticamente com base nos campos encontrados no XML
- Suporta diferentes tipos de campos:
  - **Boolean**: Switches para valores true/false
  - **Integer**: Campos numéricos inteiros
  - **Float**: Campos numéricos decimais
  - **String**: Campos de texto
- Se o arquivo XML mudar no futuro, a interface se adapta automaticamente

### 3. Modificação Pontual de Linhas
- Quando o usuário modifica um valor, apenas aquela linha específica do XML é alterada
- Não substitui o arquivo inteiro, preservando a estrutura original
- Sistema de tracking de mudanças - mostra quantos campos foram modificados antes de aplicar

### 4. Arquivos Criados/Modificados

#### Novos Arquivos:
- **XmlParser.kt**: Parser dinâmico de XML que extrai campos e tipos automaticamente
- **DynamicGraphicsScreen.kt**: Nova tela que gera a UI dinamicamente

#### Modificados:
- **RootManager.kt**: 
  - Adicionada função `updateGraphicsField()` para modificar um campo específico
  - Adicionada função `updateMultipleGraphicsFields()` para modificar múltiplos campos de uma vez
- **MainScreen.kt**: Atualizado para usar a nova `DynamicGraphicsScreen`

## Como Funciona

1. **Inicialização**: App solicita root e lê o graphics.xml do dispositivo
2. **Parse**: XmlParser analisa o XML e extrai todos os campos com seus tipos
3. **Renderização**: Interface é gerada automaticamente para cada campo
4. **Modificação**: Usuário altera valores desejados (campos modificados ficam destacados)
5. **Aplicação**: Ao clicar em "Apply Changes", apenas as linhas modificadas são atualizadas no arquivo original

## Vantagens

- ✅ Futuro-proof: Se o jogo adicionar novos campos no XML, o app automaticamente os suporta
- ✅ Eficiente: Modifica apenas o que foi alterado
- ✅ Seguro: Preserva a estrutura original do arquivo
- ✅ Visual: Mostra claramente quais campos foram modificados
- ✅ Sem hardcoding: Tudo é dinâmico e adaptável
