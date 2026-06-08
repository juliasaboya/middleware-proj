# Middleware Orientado a Mensagens

Aplicação Java com ActiveMQ embutido para simular um ambiente IoT com sensores e clientes. Os sensores publicam alertas
em tópicos JMS quando saem da faixa configurada, e os clientes podem assinar esses tópicos para acompanhar as mensagens
recebidas.

## Pré-requisitos

- Java 17
- Permissão para executar `mvnw`
- Conexão com a internet na primeira execução, caso as dependências Maven ainda não estejam em cache
- Se o projeto foi obtido por `.zip`, confirme que a pasta oculta `.mvn/` foi extraída junto com os arquivos do projeto
- Se a máquina não tiver Maven instalado globalmente, o comando `mvn` não estará disponível para recriar o wrapper

## Como executar

1. Entre na pasta do projeto:

```bash
cd /caminho/para/mom-proj
```

2. Dê permissão de execução ao Maven Wrapper, se necessário:

```bash
chmod +x mvnw
```

3. Confirme que o Maven Wrapper está completo:

```bash
ls -la .mvn/wrapper
```

O comando acima deve listar o arquivo `maven-wrapper.properties`. Se a pasta `.mvn/` não existir, a cópia do projeto
está incompleta e o `mvnw` não vai funcionar. Nesse caso, obtenha o projeto novamente ou, se já tiver Maven instalado na
máquina, recrie o wrapper com:

```bash
mvn -N wrapper:wrapper
```

Se o terminal retornar `command not found: mvn`, instale o Maven primeiro. No macOS com Homebrew:

```bash
brew install maven
mvn -v
```

Depois disso, volte para a pasta do projeto e execute novamente:

```bash
mvn -N wrapper:wrapper
```

4. Compile o projeto:

```bash
./mvnw clean compile
```

5. Execute a aplicação:

```bash
./mvnw exec:java
```

Ao iniciar, a aplicação sobe um broker ActiveMQ local em `tcp://localhost:61616` e abre o hub principal para
gerenciamento de sensores e clientes.
