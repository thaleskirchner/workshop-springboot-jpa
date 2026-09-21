# Documentação da Aplicação

## O que a aplicação faz

O **Course** é uma API REST construída com **Spring Boot** que simula o backend de um
sistema de e-commerce simplificado. Ela expõe operações para consultar e manter os
dados de usuários (clientes), produtos, categorias, pedidos, itens de pedido e
pagamentos, persistindo tudo em um banco de dados relacional através do
**Spring Data JPA / Hibernate**.

A aplicação segue a arquitetura em camadas **Resource (Controller) → Service →
Repository**, comum em projetos didáticos de Spring Boot:

- **`resources`** — controladores REST (`@RestController`) responsáveis por receber
  requisições HTTP, delegar para a camada de serviço e devolver as respostas
  (`ResponseEntity`).
- **`services`** — regras de negócio da aplicação. Traduzem exceções técnicas de
  persistência (ex.: `EntityNotFoundException`, `DataIntegrityViolationException`)
  em exceções de domínio (`ResourceNotFoundException`, `DataBaseException`).
- **`repositories`** — interfaces `JpaRepository` do Spring Data, responsáveis pelo
  acesso ao banco de dados, sem código próprio (CRUD gerado automaticamente).
- **`entities`** — classes JPA que mapeiam as tabelas do banco (`User`, `Product`,
  `Category`, `Order`, `OrderItem`, `Payment`) e o pacote `enums` com o enum
  `OrderStatus`, além de `pk` com a chave composta `OrderItemPK`.
- **`config`** — `TestConfig`, um `CommandLineRunner` ativado pelo profile `test`
  que popula o banco H2 em memória com dados de exemplo ao iniciar a aplicação.
- **`resources.exceptions`** — tratamento global de exceções via `@ControllerAdvice`
  (`ResourceExceptionHandler`), que converte exceções de negócio em respostas HTTP
  padronizadas (`StandardError`), com timestamp, status, mensagem e o caminho
  requisitado.

## Modelo de domínio

| Entidade    | Descrição                                                        |
|-------------|-------------------------------------------------------------------|
| `User`      | Usuário/cliente do sistema.                                      |
| `Product`   | Produto disponível para venda.                                   |
| `Category`  | Categoria de produto (N:N com `Product`).                        |
| `Order`     | Pedido feito por um usuário, com status (`OrderStatus`).          |
| `OrderItem` | Item de um pedido (produto + quantidade + preço), chave composta. |
| `Payment`   | Pagamento associado a um pedido (1:1).                            |

### Relacionamentos

- `User` 1:N `Order` — um usuário pode ter vários pedidos.
- `Order` 1:N `OrderItem` — um pedido contém vários itens, mapeados pela chave
  composta `OrderItemPK` (par `order` + `product`).
- `Product` 1:N `OrderItem` — um produto pode aparecer em vários itens de pedido.
- `Product` N:N `Category`, através da tabela de junção `tb_product_category`.
- `Order` 1:1 `Payment` — um pedido tem no máximo um pagamento, com
  `cascade = ALL` (o pagamento é salvo/removido junto com o pedido) e a chave
  primária do pagamento compartilhada com o pedido (`@MapsId`).

### Regras de negócio relevantes

- `Order.getTotal()` soma o subtotal (`preço × quantidade`) de todos os itens do
  pedido.
- `OrderItem.getSubTotal()` calcula `preço × quantidade` de um item.
- `OrderStatus` é armazenado no banco como um código inteiro (`Integer`) e
  convertido para o enum através de `OrderStatus.valueOf(int)`; um código
  desconhecido lança `IllegalArgumentException`.
- `UserService.delete`: se houver violação de integridade referencial (ex.:
  usuário com pedidos associados), lança `DataBaseException` (HTTP 400). O
  código também trata `EmptyResultDataAccessException` convertendo-a em
  `ResourceNotFoundException` (HTTP 404) para id inexistente; na versão do
  Spring Data JPA usada neste projeto, porém, `deleteById` não faz mais essa
  checagem de existência antes de apagar, então, na prática, remover um id
  inexistente hoje devolve `204 No Content` em vez de `404` (comportamento
  confirmado pelos testes de integração — veja `docs/TESTES.md`).
- `UserService.update`: busca a referência da entidade (`getReferenceById`) e
  atualiza apenas nome, e-mail e telefone; se o id não existir, lança
  `ResourceNotFoundException`.
- `ProductService.findById`, `CategoryService.findById` e `OrderService.findById`
  atualmente usam `Optional.get()` diretamente — se o id não existir, a aplicação
  lança `NoSuchElementException`, que não é tratada por
  `ResourceExceptionHandler` e por isso resulta em erro `5xx` em vez de `404`
  (comportamento coberto pelos testes desta suíte).

## Endpoints da API

### Usuários — `/users`

| Método   | Endpoint       | Descrição                     |
|----------|----------------|--------------------------------|
| `GET`    | `/users`       | Lista todos os usuários.       |
| `GET`    | `/users/{id}`  | Busca um usuário por id.       |
| `POST`   | `/users`       | Cria um novo usuário.          |
| `PUT`    | `/users/{id}`  | Atualiza nome/e-mail/telefone. |
| `DELETE` | `/users/{id}`  | Remove um usuário.             |

### Produtos — `/products`

| Método | Endpoint          | Descrição              |
|--------|-------------------|--------------------------|
| `GET`  | `/products`       | Lista todos os produtos. |
| `GET`  | `/products/{id}`  | Busca um produto por id. |

### Categorias — `/categories`

| Método | Endpoint             | Descrição               |
|--------|-----------------------|--------------------------|
| `GET`  | `/categories`         | Lista todas as categorias. |
| `GET`  | `/categories/{id}`    | Busca uma categoria por id. |

### Pedidos — `/orders`

| Método | Endpoint        | Descrição            |
|--------|-----------------|------------------------|
| `GET`  | `/orders`       | Lista todos os pedidos. |
| `GET`  | `/orders/{id}`  | Busca um pedido por id. |

## Tratamento de erros

`ResourceExceptionHandler` intercepta as exceções de negócio lançadas pela camada
de serviço e as converte em uma resposta JSON padronizada (`StandardError`):

- `ResourceNotFoundException` → `404 Not Found`.
- `DataBaseException` → `400 Bad Request`.

O corpo da resposta contém `timestamp`, `status`, `error`, `message` e `path`.

## Perfis e dados de teste

O profile `test` (ativo por padrão em `application.properties`) habilita o banco
H2 em memória e o `TestConfig`, que popula categorias, produtos, usuários,
pedidos, itens de pedido e um pagamento de exemplo — os mesmos dados usados como
base pelos testes de integração desta suíte (veja `docs/TESTES.md`).

## Testes e cobertura

O projeto tem duas suítes de teste (detalhadas em `docs/TESTES.md`):

- **Testes unitários** (`*Test.java`, executados pelo Maven Surefire na fase
  `test`): services (com o repositório mockado via Mockito), entidades,
  exceções e o `ResourceExceptionHandler`.
- **Testes de integração** (`*IT.java`, executados pelo Maven Failsafe nas
  fases `integration-test`/`verify`): endpoints REST via `MockMvc` contra um
  contexto Spring real e um banco H2 real, além de um teste de integração
  direto na camada de repositório para os mapeamentos JPA
  (`many-to-many`, `one-to-one` com cascade, chave composta).

Para rodar tudo, com relatório de cobertura (JaCoCo, mínimo de 90% de linhas
configurado em `pom.xml`):

```bash
./mvnw clean verify
```

O relatório HTML fica em `target/site/jacoco/index.html`.

## Tecnologias

- Java, Spring Boot (Web MVC, Data JPA)
- Hibernate / Jakarta Persistence
- H2 (testes/desenvolvimento) e PostgreSQL (produção, opcional)
- Jackson (serialização JSON)
- JUnit 5, Mockito e MockMvc (testes)
- JaCoCo (cobertura de testes)
- Maven
