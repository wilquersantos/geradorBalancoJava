@echo off
echo ============================================
echo Configurando Firewall para EmissorJava
echo ============================================
echo.

net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERRO: Execute este script como Administrador!
    echo Clique com botao direito e selecione "Executar como Administrador"
    echo.
    pause
    exit /b 1
)

echo Criando regra no Firewall do Windows...
echo Porta: 8084
echo Protocolo: TCP
echo.

powershell -Command "New-NetFirewallRule -DisplayName 'EmissorJava' -Direction Inbound -Protocol TCP -LocalPort 8084 -Action Allow -Profile Private,Domain -ErrorAction SilentlyContinue"

if %errorLevel% equ 0 (
    echo.
    echo [OK] Firewall configurado com sucesso!
    echo.
    echo A porta 8084 esta liberada para acesso na rede local.
    echo Agora o app mobile podera se conectar ao EmissorJava.
    echo.
) else (
    echo.
    echo [INFO] A regra ja existe ou houve um erro menor.
    echo Verifique manualmente: wf.msc
    echo.
)

echo ============================================
echo.
echo Para descobrir o IP deste computador, execute:
echo   ipconfig
echo.
echo Use este IP no app mobile para conectar.
echo.
echo ============================================
pause
