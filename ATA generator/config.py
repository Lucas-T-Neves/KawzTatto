from datetime import datetime
from pathlib import Path


pasta_atual = Path(__file__).resolve().parent
#variáveis da automação

#ATA
#titulo da ata
titulo=f"ATA de reunião - {datetime.now().strftime('%d.%m.%Y')}"
#subtitulo
sub=f"Grupo 10"
#membros presentes(primeiro nome separados por espaço)
pres='Lucas Arthur Eric Clara Leonardo'
#membros ausentes
aus='Lucas Eric'
#assunto
ass='Definição de beneficiário'
#proximos passos
prox='O grupo foi fragmentado em 2 responsabilidades: Clara e Leonardo: pesquisar a solução da ONG e Lucas, Arthur e Eric: pesquisar com relação a solução do microempreendedor'
#proximos passos
obj='Cada grupo tem como objetivo desejável definir 3 possíveis soluções'

#DOC
#onde salvar o doc
pathSave = pasta_atual / f"{titulo}.docx"
#onde encontrar o template
pathTemplate = pasta_atual / "modelo.dotx"

#Formate
textsBold = ["Presentes:", "Ausentes (se houver):", "Assunto:", "Próximos passos:", "Objetivo:"]


