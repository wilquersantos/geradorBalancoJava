# Configuração do Firewall do Windows

Para permitir que o app mobile acesse o EmissorJava na porta 8084, você precisa configurar o Firewall do Windows.

## Método 1: PowerShell (Recomendado - Rápido)

Abra o PowerShell como Administrador e execute:

```powershell
New-NetFirewallRule -DisplayName "EmissorJava" -Direction Inbound -Protocol TCP -LocalPort 8084 -Action Allow -Profile Private,Domain
```

Para remover a regra depois:
```powershell
Remove-NetFirewallRule -DisplayName "EmissorJava"
```

## Método 2: Interface Gráfica

1. **Abrir Firewall do Windows**
   - Pressione `Win + R`
   - Digite `wf.msc` e pressione Enter

2. **Criar Nova Regra**
   - Clique em "Regras de Entrada" (Inbound Rules)
   - No painel direito, clique em "Nova Regra..." (New Rule)

3. **Tipo de Regra**
   - Selecione "Porta" (Port)
   - Clique em "Avançar" (Next)

4. **Protocolo e Porta**
   - Selecione "TCP"
   - Selecione "Portas locais específicas" (Specific local ports)
   - Digite: `8084`
   - Clique em "Avançar" (Next)

5. **Ação**
   - Selecione "Permitir a conexão" (Allow the connection)
   - Clique em "Avançar" (Next)

6. **Perfil**
   - Deixe marcados: "Domínio" e "Particular" (Domain and Private)
   - Desmarque "Público" por segurança (opcional)
   - Clique em "Avançar" (Next)

7. **Nome**
   - Nome: `EmissorJava`
   - Descrição: `Permite acesso ao servidor EmissorJava na porta 8084`
   - Clique em "Concluir" (Finish)

## Verificar se a Regra Foi Criada

### PowerShell
```powershell
Get-NetFirewallRule -DisplayName "EmissorJava" | Format-Table -AutoSize
```

### Interface Gráfica
1. Abra `wf.msc`
2. Vá em "Regras de Entrada"
3. Procure por "EmissorJava" na lista

## Testar Conectividade

### Do próprio desktop
Abra o navegador e acesse:
```
http://localhost:8084/api/health
```

### Do celular (mesmo Wi-Fi)
1. Descubra o IP do desktop:
   ```powershell
   ipconfig
   ```
   Procure por "Endereço IPv4" (ex: 192.168.1.100)

2. No navegador do celular, acesse:
   ```
   http://192.168.1.100:8084/api/health
   ```

3. Deve retornar:
   ```json
   {"status":"UP","service":"EmissorJava"}
   ```

## Troubleshooting

### Erro: "Não foi possível conectar"

1. **Verificar se app está rodando**
   ```powershell
   netstat -an | findstr :8084
   ```
   Deve aparecer uma linha com `0.0.0.0:8084` ou `[::]:8084`

2. **Verificar regra do firewall**
   ```powershell
   Get-NetFirewallRule -DisplayName "EmissorJava"
   ```

3. **Testar porta localmente**
   ```powershell
   Test-NetConnection -ComputerName localhost -Port 8084
   ```

4. **Desabilitar temporariamente o firewall para teste**
   ```powershell
   # CUIDADO: Isso desabilita o firewall! Use apenas para teste
   Set-NetFirewallProfile -Profile Domain,Private -Enabled False
   
   # Para reativar depois
   Set-NetFirewallProfile -Profile Domain,Private -Enabled True
   ```

### App mobile não conecta, mas localhost funciona

- **Certifique-se que estão na mesma rede Wi-Fi**
- **Verifique se o IP está correto no app mobile**
- **Alguns roteadores bloqueiam comunicação entre dispositivos**
  - Acesse as configurações do roteador
  - Procure por "AP Isolation" ou "Client Isolation"
  - Desabilite essa opção

### Firewall de terceiros (Avast, Norton, etc.)

Se você usa antivírus com firewall próprio:
1. Acesse as configurações do antivírus
2. Procure por "Firewall" ou "Rede"
3. Adicione exceção para porta 8084
4. Ou adicione o Java (`java.exe`) às exceções

## Segurança

### Recomendações

1. **Não abra para perfil Público**
   - Mantenha apenas para redes Privadas e de Domínio

2. **Use apenas na rede local**
   - Não exponha na internet

3. **Altere o token padrão**
   - Edite `application.properties`
   - Mude `app.security.token` para algo único

4. **Monitore acessos**
   - Verifique os logs do aplicativo
   - Em `application.properties`, ajuste:
     ```properties
     logging.level.com.emissor=DEBUG
     ```

### Restrição por IP (Opcional)

Se quiser permitir apenas um IP específico:

```powershell
New-NetFirewallRule -DisplayName "EmissorJava" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort 8084 `
  -Action Allow `
  -RemoteAddress 192.168.1.50 `
  -Profile Private,Domain
```

Substitua `192.168.1.50` pelo IP do seu celular.

## Restaurar Configurações Padrão

Para remover completamente a regra:

```powershell
Remove-NetFirewallRule -DisplayName "EmissorJava"
```

## Script Automático de Configuração

Crie um arquivo `setup-firewall.bat` na pasta do projeto:

```batch
@echo off
echo ============================================
echo Configurando Firewall para EmissorJava
echo ============================================
echo.

net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERRO: Execute este script como Administrador!
    pause
    exit /b 1
)

echo Criando regra no Firewall...
powershell -Command "New-NetFirewallRule -DisplayName 'EmissorJava' -Direction Inbound -Protocol TCP -LocalPort 8084 -Action Allow -Profile Private,Domain" >nul 2>&1

if %errorLevel% equ 0 (
    echo.
    echo [OK] Firewall configurado com sucesso!
    echo A porta 8084 esta liberada para acesso local.
    echo.
) else (
    echo.
    echo [INFO] Regra ja existe ou houve um erro.
    echo.
)

echo Pressione qualquer tecla para continuar...
pause >nul
```

Execute `setup-firewall.bat` como Administrador.
