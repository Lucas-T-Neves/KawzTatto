# Testa cada endpoint existente da API Kawz Tattoo por HTTP.
# Pré-condição: API rodando em http://localhost:8080 (.\mvnw.cmd spring-boot:run)
# Uso: powershell -File scripts\testar-endpoints.ps1
# Sai com código 0 se todos os cenários passarem; 1 caso contrário.

$base = "http://localhost:8080/api/v1"
$resultados = New-Object System.Collections.ArrayList
$script:token = $null

function Invoke-Http {
    param([string]$Method, [string]$Uri, $Body = $null, [switch]$SemToken, [string]$TokenForcado)
    $params = @{ Method = $Method; Uri = $Uri; UseBasicParsing = $true }
    if ($PSBoundParameters.ContainsKey('TokenForcado')) {
        $params.Headers = @{ Authorization = "Bearer $TokenForcado" }
    } elseif ($script:token -and -not $SemToken) {
        $params.Headers = @{ Authorization = "Bearer $script:token" }
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 5)
        $params.ContentType = "application/json"
    }
    try {
        $resp = Invoke-WebRequest @params
        return @{ status = [int]$resp.StatusCode; body = $resp.Content }
    } catch {
        $status = 0
        $bodyText = ""
        # PS 5.1: o corpo do erro chega em ErrorDetails.Message (stream já consumido)
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $bodyText = $_.ErrorDetails.Message
        }
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            if (-not $bodyText) {
                try {
                    $stream = $_.Exception.Response.GetResponseStream()
                    $reader = New-Object System.IO.StreamReader($stream)
                    $bodyText = $reader.ReadToEnd()
                } catch { }
            }
        }
        return @{ status = $status; body = $bodyText }
    }
}

function Check {
    param([string]$Nome, [int[]]$Esperado, [int]$Obtido, $Body = "")
    $ok = $Esperado -contains $Obtido
    [void]$resultados.Add([pscustomobject]@{
        Teste    = $Nome
        Esperado = ($Esperado -join "|")
        Obtido   = $Obtido
        OK       = $(if ($ok) { "PASS" } else { "FAIL" })
    })
    if (-not $ok -and $Body) { Write-Host "   corpo: $Body" }
}

function Check-Valor {
    param([string]$Nome, $Esperado, $Obtido)
    # Números comparados com tolerância (evita escala decimal: 3000 vs 3000.00)
    $nEsp = 0.0; $nObt = 0.0
    $ehNum = [double]::TryParse("$Esperado", [System.Globalization.NumberStyles]::Float,
        [System.Globalization.CultureInfo]::InvariantCulture, [ref]$nEsp) -and
        [double]::TryParse("$Obtido", [System.Globalization.NumberStyles]::Float,
        [System.Globalization.CultureInfo]::InvariantCulture, [ref]$nObt)
    $ok = $(if ($ehNum) { [math]::Abs($nEsp - $nObt) -lt 0.005 } else { "$Esperado" -eq "$Obtido" })
    [void]$resultados.Add([pscustomobject]@{
        Teste    = $Nome
        Esperado = "$Esperado"
        Obtido   = "$Obtido"
        OK       = $(if ($ok) { "PASS" } else { "FAIL" })
    })
}

# Espera a API subir (até ~90s)
$noAr = $false
for ($i = 0; $i -45 -lt 45; $i++) {
    $r = Invoke-Http -Method GET -Uri "$base/tatuadores" -SemToken
    if ($r.status -ge 200 -and $r.status -lt 600) { $noAr = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $noAr) { Write-Host "API não respondeu em 90s"; exit 1 }

# ---------------------------------------------------------
# TATUADORES
# ---------------------------------------------------------
$tat = Invoke-Http -Method POST -Uri "$base/tatuadores" -Body @{
    nome = "Kawz Teste"; email = "kawz@teste.com"; senha = "senha-segura" }
Check "POST /tatuadores cria" 201 $tat.status $tat.body
$tatId = ($tat.body | ConvertFrom-Json).id

# Login inicial: obtém o token JWT usado nas demais requisições protegidas
$login0 = Invoke-Http -Method POST -Uri "$base/tatuadores/autenticacao" -Body @{
    email = "kawz@teste.com"; senha = "senha-segura" } -SemToken
Check "POST /autenticacao login inicial -> 200" 200 $login0.status $login0.body
$login0Json = $login0.body | ConvertFrom-Json
Check-Valor "login autenticado=true" "True" $login0Json.autenticado
Check-Valor "login devolve token JWT" "sim" $(if ($login0Json.token) { "sim" } else { "nao" })
$script:token = $login0Json.token

$dup = Invoke-Http -Method POST -Uri "$base/tatuadores" -Body @{
    nome = "Repetido"; email = "kawz@teste.com"; senha = "senha-segura" }
Check "POST /tatuadores e-mail duplicado -> 422" 422 $dup.status $dup.body

$invalido = Invoke-Http -Method POST -Uri "$base/tatuadores" -Body @{
    nome = "X"; email = "nao-e-email"; senha = "123" }
Check "POST /tatuadores inválido -> 400" 400 $invalido.status $invalido.body

$list = Invoke-Http -Method GET -Uri "$base/tatuadores"
Check "GET /tatuadores lista" 200 $list.status

$pag1 = Invoke-Http -Method GET -Uri "$base/tatuadores?pagina=0&tamanho=1"
Check "GET /tatuadores?pagina=0&tamanho=1 responde" 200 $pag1.status $pag1.body
$pag1Json = $pag1.body | ConvertFrom-Json
Check-Valor "paginacao: content com 1 item" "1" @($pag1Json.content).Count
Check-Valor "paginacao: totalElements = 1" "1" $pag1Json.totalElements
Check-Valor "paginacao: totalPages = 1" "1" $pag1Json.totalPages
Check-Valor "paginacao: size = 1" "1" $pag1Json.size

$get = Invoke-Http -Method GET -Uri "$base/tatuadores/$tatId"
Check "GET /tatuadores/{id} existe" 200 $get.status

$naoExiste = Invoke-Http -Method GET -Uri "$base/tatuadores/99999"
Check "GET /tatuadores/99999 -> 404" 404 $naoExiste.status $naoExiste.body

$put = Invoke-Http -Method PUT -Uri "$base/tatuadores/$tatId" -Body @{
    nome = "Kawz Teste Editado"; email = "kawz@teste.com"; senha = $null }
Check "PUT /tatuadores/{id} atualiza" 200 $put.status $put.body

$loginErr = Invoke-Http -Method POST -Uri "$base/tatuadores/autenticacao" -Body @{
    email = "kawz@teste.com"; senha = "senha-errada" } -SemToken
Check "POST /autenticacao senha errada -> autenticado=false" 200 $loginErr.status $loginErr.body
$naoAutenticado = ($loginErr.body | ConvertFrom-Json).autenticado
[void]$resultados.Add([pscustomobject]@{ Teste = "autenticacao senha errada -> autenticado=false"; Esperado = "False"; Obtido = $naoAutenticado; OK = $(if ($naoAutenticado -eq $false) { "PASS" } else { "FAIL" }) })
Check-Valor "senha errada nao devolve token" "True" $(if (($loginErr.body | ConvertFrom-Json).token) { "False" } else { "True" })

# ---------------------------------------------------------
# SEGURANCA (JWT)
# ---------------------------------------------------------
$semToken = Invoke-Http -Method GET -Uri "$base/clientes" -SemToken
Check "GET /clientes sem token -> 401" 401 $semToken.status $semToken.body
$pd401 = $semToken.body | ConvertFrom-Json
Check-Valor "401 e ProblemDetail (type)" "https://kawz-tattoo-api.local/erros/nao-autenticado" $pd401.type
Check-Valor "401 ProblemDetail status=401" "401" $pd401.status

$tokenRuim = Invoke-Http -Method GET -Uri "$base/clientes" -TokenForcado "token-invalido"
Check "GET /clientes com token invalido -> 401" 401 $tokenRuim.status $tokenRuim.body

# ---------------------------------------------------------
# DOCUMENTACAO OPENAPI (springdoc)
# ---------------------------------------------------------
$apiDocs = Invoke-Http -Method GET -Uri "http://localhost:8080/v3/api-docs" -SemToken
Check "GET /v3/api-docs -> 200" 200 $apiDocs.status $apiDocs.body
$docsJson = $apiDocs.body | ConvertFrom-Json
Check-Valor "OpenAPI titulo = Kawz Tattoo API" "Kawz Tattoo API" $docsJson.info.title
Check-Valor "OpenAPI expoe /api/v1/clientes" "True" $(if ($docsJson.paths.'/api/v1/clientes') { "True" } else { "False" })
Check-Valor "OpenAPI declara security bearerAuth" "True" $(if ($docsJson.components.securitySchemes.bearerAuth) { "True" } else { "False" })

$swagger = Invoke-Http -Method GET -Uri "http://localhost:8080/swagger-ui/index.html" -SemToken
Check "GET /swagger-ui/index.html -> 200" 200 $swagger.status $swagger.body

# ---------------------------------------------------------
# CLIENTES
# ---------------------------------------------------------
$cli = Invoke-Http -Method POST -Uri "$base/clientes" -Body @{
    nome = "Ana Teste"; telefone = "11999999999"; instagram = "@ana";
    canalAquisicao = "Instagram"; linkConversa = $null; linkPasta = $null; status = "LEAD" }
Check "POST /clientes cria (sem cpf, conforme DER)" 201 $cli.status $cli.body
$cliId = ($cli.body | ConvertFrom-Json).id

$cliInvalida = Invoke-Http -Method POST -Uri "$base/clientes" -Body @{
    nome = ""; telefone = "11999999999"; instagram = "@x"; canalAquisicao = "Indicação";
    linkConversa = $null; linkPasta = $null; status = "LEAD" }
Check "POST /clientes sem nome -> 400" 400 $cliInvalida.status $cliInvalida.body

$cliLista = Invoke-Http -Method GET -Uri "$base/clientes"
Check "GET /clientes lista" 200 $cliLista.status

$cliGet = Invoke-Http -Method GET -Uri "$base/clientes/$cliId"
Check "GET /clientes/{id} existe" 200 $cliGet.status

$cliPut = Invoke-Http -Method PUT -Uri "$base/clientes/$cliId" -Body @{
    nome = "Ana Teste Silva"; telefone = "11988888888"; instagram = "@ana";
    canalAquisicao = "Instagram"; linkConversa = $null; linkPasta = $null; status = "CLIENTE" }
Check "PUT /clientes/{id} atualiza" 200 $cliPut.status $cliPut.body

# ---------------------------------------------------------
# PROJETOS
# ---------------------------------------------------------
$proj = Invoke-Http -Method POST -Uri "$base/projetos" -Body @{
    tatuadorId = $tatId; clienteId = $cliId; descricao = "Fechamento de braço";
    statusProjeto = "ORCAMENTO"; valorTotal = 3000.00; valorSinal = 500.00;
    quantidadeSessoes = 3; dataValidade = "2026-12-31" }
Check "POST /projetos cria" 201 $proj.status $proj.body
$projId = ($proj.body | ConvertFrom-Json).id
$projJson = $proj.body | ConvertFrom-Json
Check-Valor "POST /projetos saldo (nada recebido) = 3000.00" "3000.00" $projJson.saldo
Check-Valor "POST /projetos recebido = 0" "0" $projJson.recebido
Check-Valor "POST /projetos sinalPago=False" "False" $projJson.sinalPago

$sinalAlto = Invoke-Http -Method POST -Uri "$base/projetos" -Body @{
    tatuadorId = $tatId; clienteId = $cliId; descricao = "Sinal alto";
    statusProjeto = "ORCAMENTO"; valorTotal = 100.00; valorSinal = 500.00;
    quantidadeSessoes = 1; dataValidade = $null }
Check "POST /projetos sinal > total -> 422" 422 $sinalAlto.status $sinalAlto.body

$projLista = Invoke-Http -Method GET -Uri "$base/projetos"
Check "GET /projetos lista" 200 $projLista.status

$projGet = Invoke-Http -Method GET -Uri "$base/projetos/$projId"
Check "GET /projetos/{id} existe" 200 $projGet.status

$proj404 = Invoke-Http -Method GET -Uri "$base/projetos/99999"
Check "GET /projetos/99999 -> 404" 404 $proj404.status

$projPut = Invoke-Http -Method PUT -Uri "$base/projetos/$projId" -Body @{
    tatuadorId = $tatId; clienteId = $cliId; descricao = "Fechamento de braço atualizado";
    statusProjeto = "APROVADO"; valorTotal = 3500.00; valorSinal = 500.00;
    quantidadeSessoes = 4; dataValidade = "2026-12-31" }
Check "PUT /projetos/{id} atualiza" 200 $projPut.status $projPut.body

$status = Invoke-Http -Method PATCH -Uri "$base/projetos/$projId/status" -Body @{ status = "CONCLUIDO" }
Check "PATCH /projetos/{id}/status troca status" 200 $status.status $status.body

# ---------------------------------------------------------
# SESSOES
# ---------------------------------------------------------
$sess = Invoke-Http -Method POST -Uri "$base/sessoes" -Body @{
    projetoId = $projId; dataHorario = "2026-11-15T14:00:00"; localAtendimento = "Estudio Kawz";
    statusSessao = "AGENDADA"; observacoesSessao = "Primeira sessao" }
Check "POST /sessoes cria" 201 $sess.status $sess.body
$sessId = ($sess.body | ConvertFrom-Json).id

$sessLista = Invoke-Http -Method GET -Uri "$base/sessoes?projetoId=$projId"
Check "GET /sessoes?projetoId responde" 200 $sessLista.status
Check-Valor "GET /sessoes?projetoId devolve 1 sessao" 1 @(($sessLista.body | ConvertFrom-Json).content).Count

$sessGet = Invoke-Http -Method GET -Uri "$base/sessoes/$sessId"
Check "GET /sessoes/{id} existe" 200 $sessGet.status

$sess404 = Invoke-Http -Method GET -Uri "$base/sessoes/99999"
Check "GET /sessoes/99999 -> 404" 404 $sess404.status

$sessPut = Invoke-Http -Method PUT -Uri "$base/sessoes/$sessId" -Body @{
    projetoId = $projId; dataHorario = "2026-11-15T15:00:00"; localAtendimento = "Estudio Kawz";
    statusSessao = "CONFIRMADA"; observacoesSessao = "Sessao editada" }
Check "PUT /sessoes/{id} atualiza" 200 $sessPut.status $sessPut.body

$remarc = Invoke-Http -Method PATCH -Uri "$base/sessoes/$sessId/remarcar" -Body @{
    dataHorario = "2026-11-20T14:00:00" }
Check "PATCH /sessoes/{id}/remarcar" 200 $remarc.status $remarc.body
Check-Valor "remarcar muda status para REMARCADA" "REMARCADA" (($remarc.body | ConvertFrom-Json).statusSessao)

$sessSemProjeto = Invoke-Http -Method POST -Uri "$base/sessoes" -Body @{
    dataHorario = "2026-11-15T14:00:00"; localAtendimento = "Estudio"; statusSessao = "AGENDADA" }
Check "POST /sessoes sem projetoId -> 400" 400 $sessSemProjeto.status

# ---------------------------------------------------------
# MOVIMENTACOES FINANCEIRAS
# ---------------------------------------------------------
$mov = Invoke-Http -Method POST -Uri "$base/movimentacoes" -Body @{
    projetoId = $projId; naturezaTransacao = "ENTRADA"; categoria = "sinal";
    dataOcorrencia = "2026-10-23"; valorMonetario = 500.00;
    formaPagamento = "PIX"; statusPagamento = "pago" }
Check "POST /movimentacoes cria" 201 $mov.status $mov.body
$movId = ($mov.body | ConvertFrom-Json).id

# Regra de saldo alinhada à view: recebido = entradas pagas; saldo = total - recebido
# (valorTotal = 3500 após o PUT; sinal de 500 pago via movimentação)
$projDepois = Invoke-Http -Method GET -Uri "$base/projetos/$projId"
$projDepoisJson = $projDepois.body | ConvertFrom-Json
Check-Valor "saldo = 3500 - 500 = 3000.00" "3000.00" $projDepoisJson.saldo
Check-Valor "recebido = 500.00" "500.00" $projDepoisJson.recebido
Check-Valor "sinalPago = True" "True" $projDepoisJson.sinalPago

$movLista = Invoke-Http -Method GET -Uri "$base/movimentacoes?projetoId=$projId"
Check "GET /movimentacoes?projetoId responde" 200 $movLista.status
Check-Valor "GET /movimentacoes?projetoId devolve 1" 1 @(($movLista.body | ConvertFrom-Json).content).Count

$movGet = Invoke-Http -Method GET -Uri "$base/movimentacoes/$movId"
Check "GET /movimentacoes/{id} existe" 200 $movGet.status

$mov404 = Invoke-Http -Method GET -Uri "$base/movimentacoes/99999"
Check "GET /movimentacoes/99999 -> 404" 404 $mov404.status

$movPut = Invoke-Http -Method PUT -Uri "$base/movimentacoes/$movId" -Body @{
    projetoId = $projId; naturezaTransacao = "ENTRADA"; categoria = "sinal";
    dataOcorrencia = "2026-10-23"; valorMonetario = 600.00;
    formaPagamento = "PIX"; statusPagamento = "pago" }
Check "PUT /movimentacoes/{id} atualiza" 200 $movPut.status $movPut.body

$movSemValor = Invoke-Http -Method POST -Uri "$base/movimentacoes" -Body @{
    projetoId = $projId; naturezaTransacao = "ENTRADA"; categoria = "sinal";
    statusPagamento = "pago" }
Check "POST /movimentacoes sem valor -> 400" 400 $movSemValor.status $movSemValor.body

$movNatureza = Invoke-Http -Method POST -Uri "$base/movimentacoes" -Body @{
    projetoId = $projId; naturezaTransacao = "RECEITA"; categoria = "sinal";
    valorMonetario = 10.00; statusPagamento = "pago" }
Check "POST /movimentacoes natureza invalida -> 400" 400 $movNatureza.status $movNatureza.body

$movStatus = Invoke-Http -Method POST -Uri "$base/movimentacoes" -Body @{
    projetoId = $projId; naturezaTransacao = "ENTRADA"; categoria = "sinal";
    valorMonetario = 10.00; statusPagamento = "PAGO" }
Check "POST /movimentacoes statusPagamento 'PAGO' -> 400" 400 $movStatus.status $movStatus.body

# ---------------------------------------------------------
# CONTRATOS (leitura pela view vw_contratos)
# ---------------------------------------------------------
$contratoId = ($proj.body | ConvertFrom-Json).contratoId

$ctrLista = Invoke-Http -Method GET -Uri "$base/contratos"
Check "GET /contratos (view) responde" 200 $ctrLista.status
Check-Valor "GET /contratos devolve 1 linha" 1 @(($ctrLista.body | ConvertFrom-Json).content).Count

$ctrGet = Invoke-Http -Method GET -Uri "$base/contratos/$contratoId"
Check "GET /contratos/{id} (view) existe" 200 $ctrGet.status $ctrGet.body
$ctrView = $ctrGet.body | ConvertFrom-Json
Check-Valor "view: nome_cliente = Ana Teste Silva" "Ana Teste Silva" $ctrView.nomeCliente
Check-Valor "view: sinal_pago = true (movimentacao sinal/pago)" "True" $ctrView.sinalPago
Check-Valor "view: forma_pagamento = PIX" "PIX" $ctrView.formaPagamento

$ctr404 = Invoke-Http -Method GET -Uri "$base/contratos/99999"
Check "GET /contratos/99999 -> 404" 404 $ctr404.status

$ctrPut = Invoke-Http -Method PUT -Uri "$base/contratos/$contratoId" -Body @{
    limiteAlteracoesArte = "3 alteracoes"; toleranciaAtraso = "15 min";
    usoImagem = "Com autorizacao" }
Check "PUT /contratos/{id} atualiza termos" 200 $ctrPut.status $ctrPut.body

$ctrDepois = Invoke-Http -Method GET -Uri "$base/contratos/$contratoId"
$ctrView2 = $ctrDepois.body | ConvertFrom-Json
Check-Valor "view reflete termos atualizados" "3 alteracoes" $ctrView2.limiteAlteracoesArte

$assin = Invoke-Http -Method PATCH -Uri "$base/contratos/$contratoId/assinatura" -Body @{ data = "2026-10-23" }
Check "PATCH /contratos/{id}/assinatura" 200 $assin.status $assin.body
Check-Valor "assinatura grava data_assinatura" "2026-10-23" (($assin.body | ConvertFrom-Json).dataAssinatura)

# Ordem de exclusão respeita as FKs: sessao/movimentacao -> projeto -> cliente -> tatuador
$sessDel = Invoke-Http -Method DELETE -Uri "$base/sessoes/$sessId"
Check "DELETE /sessoes/{id}" 204 $sessDel.status $sessDel.body

$movDel = Invoke-Http -Method DELETE -Uri "$base/movimentacoes/$movId"
Check "DELETE /movimentacoes/{id}" 204 $movDel.status $movDel.body

$projDel = Invoke-Http -Method DELETE -Uri "$base/projetos/$projId"
Check "DELETE /projetos/{id}" 204 $projDel.status $projDel.body

$cliDel = Invoke-Http -Method DELETE -Uri "$base/clientes/$cliId"
Check "DELETE /clientes/{id}" 204 $cliDel.status

$cliDepois = Invoke-Http -Method GET -Uri "$base/clientes/$cliId"
Check "GET /clientes/{id} depois do DELETE -> 404" 404 $cliDepois.status

$tatDel = Invoke-Http -Method DELETE -Uri "$base/tatuadores/$tatId"
Check "DELETE /tatuadores/{id}" 204 $tatDel.status

# ---------------------------------------------------------
# Resumo
# ---------------------------------------------------------
$resultados | Format-Table -AutoSize
$falhas = @($resultados | Where-Object { $_.OK -eq "FAIL" }).Count
Write-Host "Total: $($resultados.Count) cenarios | PASS: $($resultados.Count - $falhas) | FAIL: $falhas"
if ($falhas -gt 0) { exit 1 } else { Write-Host "TUDO PASS"; exit 0 }
