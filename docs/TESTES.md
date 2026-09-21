# Documentação dos Testes

Esta suíte tem dois níveis, seguindo a convenção padrão do Maven:

- **Testes unitários** (`*Test.java`) — rodam com o plugin **Surefire**, na fase
  `test`. Não sobem contexto Spring (exceto `CourseApplicationTests`, que já
  existia no projeto); usam **Mockito** para isolar a unidade sob teste.
- **Testes de integração** (`*IT.java`) — rodam com o plugin **Failsafe**, nas
  fases `integration-test`/`verify`. Sobem o contexto Spring completo, com
  banco **H2 real** e dados semeados por `TestConfig`, exercitando a cadeia
  real **Resource → Service → Repository → Banco**.

Rodar tudo (unitários + integração + relatório de cobertura):

```bash
./mvnw clean verify
```

O relatório de cobertura (JaCoCo) fica em `target/site/jacoco/index.html`
depois do `verify`; o build falha se a cobertura de linhas cair abaixo de 90%
(configurado em `pom.xml`).

> **Por que `*IT.java` e não `*Test.java` para os testes de integração?**
> É a convenção padrão do Maven: o Surefire só executa `*Test.java` por
> padrão; o Failsafe é quem executa `*IT.java`, nas fases de integração. Isso
> separa naturalmente "testes rápidos, sempre executados" de "testes mais
> lentos, com banco real", e ainda garante que uma falha de integração não
> passe despercebida: o Failsafe roda `verify` mesmo se algum teste falhar em
> `integration-test`, e só então reporta o erro.

## Testes unitários

### `services/UserServiceTest`

Testa `UserService` com o `UserRepository` mockado (Mockito), isolando a
lógica de tradução de exceções técnicas em exceções de domínio:

- `findAllShouldReturnAllUsers` — delega corretamente para `repository.findAll()`.
- `findByIdShouldReturnUserWhenIdExists` / `findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist` —
  confirma que um id inexistente vira `ResourceNotFoundException` (via
  `Optional.orElseThrow`).
- `insertShouldPersistAndReturnUser` — delega para `repository.save`.
- `deleteShouldDoNothingWhenIdExists` — caminho feliz.
- `deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist` — simula
  `EmptyResultDataAccessException` e confirma a tradução para
  `ResourceNotFoundException`. **Nota:** na versão atual do Spring Data JPA
  usada pelo projeto, o `deleteById` real não lança mais essa exceção para
  ids inexistentes (ver `UserResourceIT`); este teste garante que, *se*
  algum dia o repositório voltar a lançá-la, o serviço continua tratando-a
  corretamente — é a lógica do `catch`, não o comportamento do Spring Data,
  que está sob teste aqui.
- `deleteShouldThrowDataBaseExceptionWhenIdIsDependent` — simula
  `DataIntegrityViolationException` e confirma a tradução para
  `DataBaseException`.
- `updateShouldReturnUpdatedUserWhenIdExists` — confirma que apenas
  nome/e-mail/telefone são copiados do objeto recebido para a entidade
  gerenciada.
- `updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist` — simula
  `EntityNotFoundException` de `getReferenceById` e confirma a tradução, e
  que `save` nunca é chamado nesse caso.
- `findByIdShouldQueryRepositoryUsingGivenId` — confirma que o id usado na
  consulta é exatamente o id recebido (nenhuma transformação silenciosa).

### `services/ProductServiceTest`, `services/CategoryServiceTest`, `services/OrderServiceTest`

Mesma estratégia do `UserServiceTest` (repositório mockado), mas para
serviços mais simples que **não** têm tratamento de exceção próprio: os três
chamam `Optional.get()` diretamente. Cada suíte testa:

- Caminho feliz de `findAll` e `findById`.
- Que um id inexistente propaga `NoSuchElementException` — documentando,
  de forma intencional, uma lacuna existente no código (não corrigida por
  estar fora do escopo desta tarefa): diferente de `UserService`, estes
  serviços não convertem "não encontrado" em `ResourceNotFoundException`.

### `services/exceptions/ResourceNotFoundExceptionTest` e `DataBaseExceptionTest`

Testes simples de que a mensagem da exceção é montada/preservada
corretamente — é essa mensagem que acaba no corpo JSON de erro da API.

### `entities/enums/OrderStatusTest`

`OrderStatus` é persistido como um código inteiro, não pelo nome do enum.
Testa a ida e volta `código → enum` (`valueOf`) para todos os status
válidos, o `getCode()` de cada um, e que um código desconhecido lança
`IllegalArgumentException` — esse é exatamente o contrato do qual `Order`
depende para ler/gravar a coluna `order_status`.

### `entities/pk/OrderItemPKTest`

`OrderItemPK` é a `@EmbeddedId` de `OrderItem` (chave composta
`order + product`). Testa getters/setters e o contrato de
`equals`/`hashCode` baseado nos dois campos, que é o que dá identidade JPA
aos itens de pedido.

### `entities/UserTest`, `ProductTest`, `CategoryTest`, `OrderTest`, `OrderItemTest`, `PaymentTest`

Para cada entidade: construtor e getters/setters expõem os valores
corretamente, e o contrato de `equals`/`hashCode` (baseado em id, ou em
id+order+product conforme o caso) funciona — incluindo os casos-limite
`equals(null)`, `equals(outro tipo)` e `equals(mesma instância)`.

Além disso:

- `OrderTest` cobre a regra de negócio `getTotal()` (soma dos subtotais dos
  itens, incluindo o caso de pedido sem itens) e a conversão
  `OrderStatus ↔ código inteiro`, incluindo `setOrderStatus(null)` (que deve
  manter o status atual, não zerá-lo).
- `OrderItemTest` cobre `getSubTotal()` (`preço × quantidade`).
- `ProductTest` cobre `getOrders()`, que percorre os itens do produto e
  devolve os pedidos distintos correspondentes (usando reflection para
  popular o lado inverso `items`, já que em produção quem o popula é o
  Hibernate ao carregar a entidade, não código da aplicação).

### `resources/exceptions/StandardErrorTest`

Testa os getters/setters do DTO `StandardError`, que é o formato do corpo
JSON devolvido em toda resposta de erro tratada pela API.

### `resources/exceptions/ResourceExceptionHandlerTest`

Testa **diretamente** (sem subir o Spring) os dois métodos de
`ResourceExceptionHandler`, com um `HttpServletRequest` mockado: confirma
que `ResourceNotFoundException` vira `404` e `DataBaseException` vira `400`,
com `status`, `error`, `message` e `path` corretos no corpo. O teste de
integração `UserResourceIT` confirma o mesmo comportamento fim a fim, via
HTTP real.

## Testes de integração

Todos usam `@SpringBootTest` (contexto completo, perfil `test`, banco H2 em
memória semeado por `TestConfig`) e, para os que fazem requisições HTTP,
`@AutoConfigureMockMvc`.

### `resources/UserResourceIT`

Cobre o ciclo CRUD completo de `/users` fim a fim (Controller → Service →
Repository → H2):

- `findAllShouldReturnSeededUsers` / `findByIdShouldReturnUserWhenIdExists` —
  leitura dos dados semeados.
- `findByIdShouldReturnStandardErrorWithNotFoundWhenIdDoesNotExist` —
  confirma o corpo `StandardError` completo (404) para id inexistente.
- `insertShouldPersistUserAndReturnCreated` — `POST` grava no banco de
  verdade e devolve `201` com `Location` e corpo com id gerado.
- `updateShouldReturnUpdatedUserWhenIdExists` /
  `updateShouldReturnStandardErrorWithNotFoundWhenIdDoesNotExist` — `PUT`
  atualiza e persiste; ou devolve `404` para id inexistente.
- `deleteShouldReturnNoContentWhenUserHasNoDependentOrders` — cria um
  usuário descartável (sem pedidos), remove com sucesso (`204`) e confirma
  que um `GET` subsequente já devolve `404`.
- `deleteShouldReturnNoContentWhenIdDoesNotExist` — **comportamento real
  observado**, e não o que o código de `UserService.delete` sugeriria à
  primeira vista: a implementação atual de `deleteById` do Spring Data JPA
  não faz mais uma checagem de existência antes de apagar, então remover um
  id inexistente devolve `204`, não `404`. O ramo de código que trata
  `EmptyResultDataAccessException` (e que o `UserServiceTest` testa
  isoladamente) está com esse comportamento, mas atualmente não é
  alcançado pelo repositório real nesta versão do framework.
- `deleteShouldReturnBadRequestWhenUserHasDependentOrders` — remove o
  usuário id 1 (que tem pedidos associados) e confirma `400` com
  `DataBaseException`, causado por uma violação real de integridade
  referencial no H2. Este teste é o único da classe que **não** roda dentro
  da transação de rollback automático do teste (veja o Javadoc do método): a
  violação de FK só aparece quando o `DELETE` é de fato enviado ao banco, e
  uma transação de teste adia esse envio até um commit que nunca acontece
  (rollback). O teste usa `@Transactional(propagation = NOT_SUPPORTED)` para
  rodar fora dessa transação e `@DirtiesContext` para proteger os demais
  testes de qualquer efeito colateral.

### `resources/ProductResourceIT`, `resources/CategoryResourceIT`, `resources/OrderResourceIT`

Cobrem os endpoints somente-leitura de `/products`, `/categories` e
`/orders`, validando dados semeados (incluindo, no caso de `OrderResourceIT`,
o cálculo de `total`, a lista de itens e a presença/ausência de `payment`
conforme o status do pedido). O teste `findByIdShouldFailWhenIdDoesNotExist`
de cada uma documenta a mesma lacuna descrita acima para
`ProductService`/`CategoryService`/`OrderService`: como não há
`ResourceNotFoundException` nesse caminho, a exceção
(`NoSuchElementException`) simplesmente se propaga para fora de
`mockMvc.perform(...)`, em vez de virar uma resposta HTTP `404`.

### `repositories/JpaRelationshipIT`

Vai direto à camada de repositório (sem HTTP), para provar que os
mapeamentos JPA declarados nas entidades realmente funcionam contra o banco
real — e não apenas "no papel":

- `manyToManyBetweenProductAndCategoryShouldBePersistedBothWays` — grava um
  produto com uma categoria nova, força `flush`+`clear` do
  `EntityManager` (para garantir que a leitura seguinte vem do banco, não do
  cache de primeiro nível) e confirma a associação nos dois sentidos
  (`Product.categories` e `Category.products`) via a tabela de junção
  `tb_product_category`.
- `oneToOneOrderPaymentShouldCascadeAndShareId` — grava um `Order` com um
  `Payment` associado (`cascade = ALL`) e confirma que o pagamento é
  persistido junto e compartilha o id do pedido (`@MapsId`).
- `orderItemShouldBeFoundByItsCompositeEmbeddedId` — grava um `OrderItem` e
  confirma que ele pode ser recuperado pela chave composta
  (`OrderItemPK`), e que `getSubTotal()` reflete o valor persistido.
