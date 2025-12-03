#!/bin/bash
# Script de teste manual para verificar se o arquivo graphics.xml está sendo modificado

echo "═══════════════════════════════════════════════════════"
echo "🔍 TESTE MANUAL - RDR Mobile Graphics Editor"
echo "═══════════════════════════════════════════════════════"
echo ""

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verificar se ADB está disponível
if ! command -v adb &> /dev/null; then
    echo -e "${RED}❌ ADB não encontrado. Instale Android SDK Platform Tools.${NC}"
    exit 1
fi

# Verificar se dispositivo está conectado
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ Nenhum dispositivo Android conectado via ADB.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Dispositivo Android conectado${NC}"
echo ""

# Verificar root
echo -e "${BLUE}[1/7]${NC} Verificando acesso root..."
ROOT_CHECK=$(adb shell su -c "id" 2>/dev/null | grep -c "uid=0")
if [ "$ROOT_CHECK" -eq 0 ]; then
    echo -e "${RED}❌ Dispositivo não tem root ou root não autorizado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Root disponível${NC}"
echo ""

# Verificar se o jogo está instalado
echo -e "${BLUE}[2/7]${NC} Verificando se o jogo RDR Mobile está instalado..."
GAME_INSTALLED=$(adb shell pm list packages | grep -c "com.netflix.NGP.Kamo")
if [ "$GAME_INSTALLED" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Jogo RDR Mobile NÃO está instalado${NC}"
    echo -e "${YELLOW}   Package: com.netflix.NGP.Kamo${NC}"
    echo ""
else
    echo -e "${GREEN}✅ Jogo RDR Mobile instalado${NC}"
    echo ""
fi

# Verificar se o arquivo graphics.xml existe
echo -e "${BLUE}[3/7]${NC} Verificando arquivo graphics.xml..."
FILE_PATH="/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"

if adb shell su -c "test -f '$FILE_PATH' && echo 'exists'" | grep -q "exists"; then
    echo -e "${GREEN}✅ Arquivo graphics.xml EXISTE${NC}"
    echo -e "${BLUE}   Localização: $FILE_PATH${NC}"
    
    # Mostrar permissões
    PERMISSIONS=$(adb shell su -c "ls -la '$FILE_PATH'" 2>/dev/null)
    echo -e "${BLUE}   Permissões: $PERMISSIONS${NC}"
    
    # Fazer backup
    BACKUP_PATH="/sdcard/Download/graphics_backup_$(date +%s).xml"
    adb shell su -c "cp '$FILE_PATH' '$BACKUP_PATH'" 2>/dev/null
    echo -e "${GREEN}   Backup criado: $BACKUP_PATH${NC}"
    echo ""
    
    # Mostrar conteúdo atual
    echo -e "${BLUE}[4/7]${NC} Conteúdo ANTES da modificação:"
    echo -e "${YELLOW}─────────────────────────────────────────────────────${NC}"
    adb shell su -c "cat '$FILE_PATH'" | head -20
    echo -e "${YELLOW}─────────────────────────────────────────────────────${NC}"
    echo ""
else
    echo -e "${YELLOW}⚠️  Arquivo graphics.xml NÃO EXISTE ainda${NC}"
    echo -e "${YELLOW}   Isso é normal se o jogo nunca foi aberto${NC}"
    echo -e "${YELLOW}   O app criará o arquivo ao aplicar configurações${NC}"
    echo ""
fi

# Instruções para o usuário
echo -e "${BLUE}[5/7]${NC} ${YELLOW}AÇÃO MANUAL NECESSÁRIA:${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════${NC}"
echo -e "1. Abra o app ${GREEN}RDR Graphics Editor${NC} no dispositivo"
echo -e "2. Aceite a solicitação de ${RED}ROOT${NC} (Magisk/SuperSU)"
echo -e "3. Modifique uma configuração (ex: Resolution X para 1920)"
echo -e "4. Clique no botão ${GREEN}Apply Changes${NC}"
echo -e "5. Aguarde a mensagem de sucesso"
echo -e "${YELLOW}═══════════════════════════════════════════════════════${NC}"
echo ""
read -p "Pressione ENTER após aplicar as mudanças no app..."
echo ""

# Verificar se o arquivo foi modificado
echo -e "${BLUE}[6/7]${NC} Verificando se o arquivo foi modificado..."
if adb shell su -c "test -f '$FILE_PATH' && echo 'exists'" | grep -q "exists"; then
    echo -e "${GREEN}✅ Arquivo graphics.xml EXISTE após modificação${NC}"
    echo ""
    
    # Mostrar conteúdo após modificação
    echo -e "${BLUE}[7/7]${NC} Conteúdo DEPOIS da modificação:"
    echo -e "${GREEN}─────────────────────────────────────────────────────${NC}"
    adb shell su -c "cat '$FILE_PATH'" | head -20
    echo -e "${GREEN}─────────────────────────────────────────────────────${NC}"
    echo ""
    
    # Comparar timestamps se backup existe
    if [ -n "$BACKUP_PATH" ]; then
        BACKUP_TIME=$(adb shell su -c "stat -c %Y '$BACKUP_PATH'" 2>/dev/null)
        FILE_TIME=$(adb shell su -c "stat -c %Y '$FILE_PATH'" 2>/dev/null)
        
        if [ "$FILE_TIME" -gt "$BACKUP_TIME" ]; then
            echo -e "${GREEN}✅ SUCESSO! Arquivo foi MODIFICADO (timestamp mais recente)${NC}"
            echo -e "${GREEN}   Backup: $(date -d @$BACKUP_TIME 2>/dev/null || echo $BACKUP_TIME)${NC}"
            echo -e "${GREEN}   Atual:  $(date -d @$FILE_TIME 2>/dev/null || echo $FILE_TIME)${NC}"
        else
            echo -e "${RED}❌ FALHA! Arquivo NÃO foi modificado (mesmo timestamp)${NC}"
            echo -e "${RED}   O bug ainda existe!${NC}"
        fi
    fi
else
    echo -e "${RED}❌ FALHA! Arquivo graphics.xml NÃO EXISTE${NC}"
    echo -e "${RED}   O app não conseguiu criar/modificar o arquivo${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Logs do app (últimas 50 linhas):${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
adb logcat -d -s "libsu:*" "RDRGraphicsEditor:*" "*:E" | tail -50
echo ""

echo -e "${GREEN}✅ Teste concluído!${NC}"
