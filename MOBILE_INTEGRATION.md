# Como Integrar com App Mobile

Este documento explica como o aplicativo mobile deve enviar dados para o EmissorJava.

## Configurações Necessárias

### Endpoint do Servidor
```
http://[IP_DO_DESKTOP]:8084/api/items
```

### Token de Autenticação
```
emissor-token-2026
```

## Integração no App Mobile

### Android (Kotlin/Java)

#### Usando Retrofit (Recomendado)

```kotlin
// Interface da API
interface EmissorApi {
    @POST("api/items")
    suspend fun enviarItem(
        @Header("X-API-Token") token: String,
        @Body item: ItemRequest
    ): Response<ItemResponse>
}

// Data class
data class ItemRequest(
    val codigoReferencia: String,
    val quantidade: Int,
    val descricao: String
)

// Configuração do Retrofit
val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.1.100:8084/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(EmissorApi::class.java)

// Envio do item
suspend fun enviarItemParaDesktop(codigo: String, qtd: Int, desc: String) {
    try {
        val item = ItemRequest(codigo, qtd, desc)
        val response = api.enviarItem("emissor-token-2026", item)
        
        if (response.isSuccessful) {
            Log.d("Emissor", "Item enviado com sucesso!")
        } else {
            Log.e("Emissor", "Erro: ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("Emissor", "Erro ao enviar: ${e.message}")
    }
}
```

#### Usando HttpURLConnection (Nativo)

```java
public void enviarItem(String codigo, int quantidade, String descricao) {
    new Thread(() -> {
        try {
            URL url = new URL("http://192.168.1.100:8084/api/items");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-Token", "emissor-token-2026");
            conn.setDoOutput(true);
            
            String jsonBody = String.format(
                "{\"codigoReferencia\":\"%s\",\"quantidade\":%d,\"descricao\":\"%s\"}",
                codigo, quantidade, descricao
            );
            
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                Log.d("Emissor", "Item enviado com sucesso!");
            } else {
                Log.e("Emissor", "Erro: " + responseCode);
            }
            
            conn.disconnect();
        } catch (Exception e) {
            Log.e("Emissor", "Erro: " + e.getMessage());
        }
    }).start();
}
```

### iOS (Swift)

```swift
func enviarItem(codigo: String, quantidade: Int, descricao: String) {
    let url = URL(string: "http://192.168.1.100:8084/api/items")!
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("emissor-token-2026", forHTTPHeaderField: "X-API-Token")
    
    let body: [String: Any] = [
        "codigoReferencia": codigo,
        "quantidade": quantidade,
        "descricao": descricao
    ]
    
    request.httpBody = try? JSONSerialization.data(withJSONObject: body)
    
    URLSession.shared.dataTask(with: request) { data, response, error in
        if let error = error {
            print("Erro: \(error)")
            return
        }
        
        if let httpResponse = response as? HTTPURLResponse {
            if httpResponse.statusCode == 201 {
                print("Item enviado com sucesso!")
            } else {
                print("Erro: \(httpResponse.statusCode)")
            }
        }
    }.resume()
}
```

### React Native / Expo

```javascript
async function enviarItem(codigo, quantidade, descricao) {
  try {
    const response = await fetch('http://192.168.1.100:8084/api/items', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-API-Token': 'emissor-token-2026',
      },
      body: JSON.stringify({
        codigoReferencia: codigo,
        quantidade: quantidade,
        descricao: descricao,
      }),
    });

    if (response.ok) {
      const data = await response.json();
      console.log('Item enviado com sucesso!', data);
      return data;
    } else {
      console.error('Erro:', response.status);
    }
  } catch (error) {
    console.error('Erro ao enviar:', error);
  }
}

// Exemplo de uso
enviarItem('ABC123', 5, 'Produto teste');
```

### Flutter (Dart)

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

Future<void> enviarItem(String codigo, int quantidade, String descricao) async {
  final url = Uri.parse('http://192.168.1.100:8084/api/items');
  
  try {
    final response = await http.post(
      url,
      headers: {
        'Content-Type': 'application/json',
        'X-API-Token': 'emissor-token-2026',
      },
      body: jsonEncode({
        'codigoReferencia': codigo,
        'quantidade': quantidade,
        'descricao': descricao,
      }),
    );

    if (response.statusCode == 201) {
      print('Item enviado com sucesso!');
      final data = jsonDecode(response.body);
      print(data);
    } else {
      print('Erro: ${response.statusCode}');
    }
  } catch (e) {
    print('Erro ao enviar: $e');
  }
}
```

## Fluxo de Integração

1. **Usuário bipa código de barras no mobile**
   - App mobile captura o código via câmera/scanner
   
2. **Usuário informa quantidade e descrição**
   - Interface do mobile coleta dados adicionais
   
3. **App mobile envia para desktop**
   - POST para `http://[IP]:8084/api/items`
   - Inclui header `X-API-Token`
   - Body JSON com os dados
   
4. **Desktop recebe e armazena**
   - API valida token e dados
   - Salva no banco SQLite
   - Retorna confirmação (HTTP 201)
   
5. **Desktop exibe na interface**
   - Tabela JavaFX atualiza automaticamente
   - Usuário visualiza item recebido

## Descoberta do IP do Desktop

### No Desktop (EmissorJava)
Execute no PowerShell:
```powershell
ipconfig
```
Procure por "Endereço IPv4" na rede ativa.

### Configuração no Mobile
Substitua `192.168.1.100` pelo IP real do desktop.

## Testando a Conexão

### Teste de Health Check
Antes de enviar dados, teste se o servidor está acessível:

```javascript
// JavaScript/React Native
fetch('http://192.168.1.100:8084/api/health')
  .then(res => res.json())
  .then(data => console.log('Servidor OK:', data))
  .catch(err => console.error('Servidor inacessível:', err));
```

### Resposta Esperada
```json
{
  "status": "UP",
  "service": "EmissorJava"
}
```

## Códigos de Resposta HTTP

| Código | Significado | Ação |
|--------|-------------|------|
| 201 | Criado com sucesso | Item foi salvo |
| 400 | Dados inválidos | Verificar campos obrigatórios |
| 401 | Token inválido | Verificar header X-API-Token |
| 500 | Erro no servidor | Verificar logs do desktop |

## Tratamento de Erros

```kotlin
// Exemplo Android
when (response.code()) {
    201 -> {
        // Sucesso
        showToast("Item enviado com sucesso!")
    }
    400 -> {
        // Validação
        showToast("Dados inválidos. Verifique os campos.")
    }
    401 -> {
        // Token
        showToast("Erro de autenticação. Verifique a configuração.")
    }
    else -> {
        // Outros
        showToast("Erro ao enviar. Tente novamente.")
    }
}
```

## Checklist de Integração

- [ ] Desktop e mobile na mesma rede Wi-Fi
- [ ] Firewall do Windows permite porta 8084
- [ ] IP do desktop configurado no app mobile
- [ ] Token correto no header
- [ ] Formato JSON correto
- [ ] Campos obrigatórios preenchidos
- [ ] Tratamento de erros implementado

## Suporte

Para problemas de conexão:
1. Verifique se desktop e mobile estão na mesma rede
2. Teste com navegador mobile: `http://[IP]:8084/api/health`
3. Verifique firewall do Windows
4. Confirme que EmissorJava está rodando

## Exemplo Completo com Scanner de Código de Barras

### Android + ML Kit

```kotlin
// Activity do Scanner
class ScannerActivity : AppCompatActivity() {
    
    private lateinit var api: EmissorApi
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configurar scanner e API
    }
    
    fun onBarcodeScanned(barcode: String) {
        // Mostrar dialog para quantidade e descrição
        showInputDialog(barcode)
    }
    
    private fun showInputDialog(codigo: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Item Bipado: $codigo")
            .setView(R.layout.dialog_item_input)
            .setPositiveButton("Enviar") { dialog, _ ->
                val quantidade = findViewById<EditText>(R.id.quantidade).text.toString().toInt()
                val descricao = findViewById<EditText>(R.id.descricao).text.toString()
                
                enviarParaDesktop(codigo, quantidade, descricao)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
    }
    
    private fun enviarParaDesktop(codigo: String, qtd: Int, desc: String) {
        lifecycleScope.launch {
            try {
                val item = ItemRequest(codigo, qtd, desc)
                val response = api.enviarItem("emissor-token-2026", item)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@ScannerActivity, 
                        "Item enviado com sucesso!", 
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ScannerActivity, 
                        "Erro: ${response.code()}", 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScannerActivity, 
                    "Erro de conexão: ${e.message}", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}
```
