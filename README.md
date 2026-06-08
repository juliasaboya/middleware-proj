# Middleware Orientado a Mensagens

Aplicação Java com ActiveMQ embutido para simular um ambiente IoT com sensores e clientes. Os sensores publicam alertas
em tópicos JMS quando saem da faixa configurada, e os clientes podem assinar esses tópicos para acompanhar as mensagens
recebidas.

## Pré-requisitos

- Java 17
- Permissão para executar `mvnw`
- Conexão com a internet na primeira execução, caso as dependências Maven ainda não estejam em cache

## Como executar

1. Entre na pasta do projeto:

```bash
cd /caminho/para/mom-proj
```

2. Dê permissão de execução ao Maven Wrapper, se necessário:

```bash
chmod +x mvnw
```

3. Compile o projeto:

```bash
./mvnw clean compile
```

4. Execute a aplicação:

```bash
./mvnw exec:java
```

Ao iniciar, a aplicação sobe um broker ActiveMQ local em `tcp://localhost:61616` e abre o hub principal para
gerenciamento de sensores e clientes.
