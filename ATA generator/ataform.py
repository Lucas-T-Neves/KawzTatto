import config

class ATA_Formatter:

    membros=["Lucas Tertuliano das Neves", "Arthur Bispo dos Santos","Eric Barbosa Costa","Clara Carolina Augusto Garcia","Leonardo Ferraz Scavazza"]

    def __init__(self, titulo, sub, assnt, prox, obj):
            self.titulo = titulo
            self.sub = sub
            self.assnt = assnt
            self.prox = prox
            self.obj = obj
    
    def format_paragraphs(self):
            pres = []
            for p in config.pres.split():
                        for m in self.membros:
                            if p[:4] == m[:4]:
                                 pres.append(m)
            aus = []
            ause = config.aus
            for a in config.aus.split():
                     for m in self.membros:
                            if a[:4] == m[:4]:
                                aus.append(m)
            return [self.titulo,
                    self.sub,
                    f"Presentes: {', '.join(pres)}", 
                    f"Ausentes (se houver): {ause if not aus else ', '.join(aus)}",
                    f"Assunto: {self.assnt}",
                    f"Próximos passos: {self.prox}",
                    f"Objetivo: {self.obj}"]

