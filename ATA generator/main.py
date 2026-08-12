from ataform import *
from pywin import *
import config



def main ():

    formatter = ATA_Formatter(config.titulo, config.sub, config.ass, config.prox, config.obj)
    textos = formatter.format_paragraphs()
    word = WordInstance()
    word.create_and_populate_paragraphs(textos)
    word.format_text(config.textsBold)
    word.save()

if __name__ == "__main__":
    main()