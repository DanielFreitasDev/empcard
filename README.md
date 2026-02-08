# EmpCard

Sistema web completo para gestão de empréstimos de cartão de crédito, com controle de pessoas, cartões, lançamentos,
pagamentos parciais, cálculo mensal por competência e relatório analítico em PDF.

## Visão geral

O EmpCard foi criado para substituir planilhas manuais em cenários de cobrança com múltiplos cartões e múltiplas
pessoas.

Principais capacidades:

- Cadastro de pessoas com CPF (armazenado sem máscara e exibido com máscara).
- Cadastro de cartões com dia de fechamento e dia de vencimento.
- Registro de lançamentos:
- Avulso
- Parcelado
- Fixo (recorrente)
- Registro de pagamentos (inclusive parciais).
- Simulação mensal por competência com:
- saldo anterior
- juros e multa por pessoa
- compras do mês por cartão
- pagamentos do mês
- saldo final (incluindo crédito para mês seguinte quando houver pagamento maior)
- Relatório analítico por pessoa, agrupado por cartão, com exportação em PDF.
- Segurança com login/senha e perfis `ADMIN` e `CONSULTA`.
- Setup inicial: criação obrigatória do primeiro usuário administrador no primeiro boot.
- API REST documentada com Swagger/OpenAPI.

## Stack

- Java 21
- Spring Boot 3.5
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- OpenAPI/Swagger (`springdoc`)
- Geração de PDF com `openhtmltopdf`

## Arquitetura

Pacotes principais em `src/main/java/io/freitas/empcard`:

- `controller`: páginas Thymeleaf
- `controller/api`: endpoints REST
- `service`: regras de negócio
- `repository`: acesso a dados (JPA)
- `model`: entidades JPA
- `dto`: contratos de entrada/saída
- `mapper`: mapeamento manual Entity <-> DTO
- `exception`: tratamento global padronizado
- `security`: autenticação/autorização
- `config`: configurações gerais
- `util`: utilitários de máscara, moeda e competência

## Regras de negócio implementadas

### Competência da compra

Regra aplicada para fechamento:

- se `dia da compra >= dia de fechamento`, a compra entra na competência seguinte.
- caso contrário, entra na competência do mês atual.

### Vencimento da competência

- se `dia de vencimento <= dia de fechamento`, o vencimento cai no mês seguinte.
- caso contrário, no mesmo mês da competência.

### Juros e multa

Por pessoa, são configurados:

- `jurosMensal` (%)
- `multaAtraso` (%)

Encargos do mês são aplicados sobre o saldo anterior positivo.

### Pagamento parcial e crédito

O cálculo mensal considera pagamentos parciais. Se o pagamento superar o devido do mês, o saldo fica negativo e funciona
como crédito para a competência seguinte.

## Frontend

- Mobile-first e responsivo.
- Máscaras automáticas:
- CPF
- cartão 16 dígitos
- CEP
- celular/WhatsApp
- moeda brasileira (`R$ 0,00`)
- Campos textuais em maiúsculo.
- Integração ViaCEP no cadastro de pessoa:
- endpoint: `https://viacep.com.br/ws/{cep}/json/`
- campos preenchidos pela API ficam bloqueados para edição.

## Segurança

Perfis:

- `ADMIN`: acesso total (CRUDs e gestão de usuários).
- `CONSULTA`: somente visualização.

Fluxo inicial:

1. primeira execução sem usuários
2. redirecionamento automático para `/setup/inicial`
3. criação do primeiro usuário administrador
4. login normal em `/login`

## Banco de dados

Migrations Flyway em:

- `src/main/resources/db/migration`

Convenção aplicada:

- tabelas no plural (`pessoas`, `cartoes`, `lancamentos`, `pagamentos`, `usuarios`)
- modelos no singular (`Pessoa`, `Cartao`, etc.)

## Como executar localmente

Pré-requisitos:

- Java 21
- PostgreSQL

Variáveis de ambiente esperadas:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE` (`dev` ou `prod`)

Comandos:

```bash
./mvnw clean verify
./mvnw spring-boot:run -Pdev
```

## Docker

Build da imagem:

```bash
docker build -t empcard:latest .
```

Execução do container:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/empcard \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  empcard:latest
```

## Swagger

- UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`

Acesso ao Swagger é restrito ao perfil `ADMIN`.

## Fluxos principais na UI

- `Pessoas`: listagem, novo, visualizar, editar, desativar, excluir (com validação de vínculo).
- `Cartões`: listagem, novo, visualizar, editar, desativar, excluir (com validação de vínculo).
- `Lançamentos`: listagem, novo, visualizar, editar, desativar, excluir.
- `Pagamentos`: listagem, novo, visualizar, editar, excluir.
- `Relatórios`: relatório mensal por pessoa e exportação para PDF.
- `Usuários`: cadastro de usuário, desativação e redefinição de senha.

## Testes

Executar:

```bash
./mvnw test
```

O teste usa H2 em memória (`src/test/resources/application.properties`) para validar subida do contexto Spring.

## Observações

- CPF, CEP, celular, WhatsApp e número de cartão são persistidos apenas com dígitos.
- Máscaras são aplicadas na visualização do frontend.
- O relatório mensal é calculado dinamicamente com base em lançamentos + pagamentos.

