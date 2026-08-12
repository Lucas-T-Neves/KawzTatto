from win32com.client import constants
import win32com.client
import sys
import config

openWord = win32com.client.Dispatch("Word.Application")
doc = openWord.Documents.Add(Template=config.pathTemplate)


class WordInstance:

    def __init__(self):

        
        openWord.Visible = True
        openWord.DisplayAlerts = False
        

    def create_and_populate_paragraphs(self, textos):
        print(textos)
        while doc.Paragraphs.Count < len(textos):
            doc.Paragraphs.Add()

        i = 1
        for p in doc.Paragraphs:
            rng = p.Range
            rng.End = rng.End - 1
            rng.Text = textos[i-1]
            p.Format.Alignment = 1
            print(i)
            i+=1
    
    def format_text(self, texts):
        i = 0
        for p in doc.Paragraphs:
            rng = p.Range
            rng.Font.Name = "Arial"
            rng.Font.Size = 13
            if i == 0:
                p.Range.Font.Bold = True
                p.Range.Font.Size = 22
            elif i == 1:
                p.Range.Font.Bold = True
                p.Range.Font.Size = 16
            if i > 1:
                rn = p.Range  
                rn.Find.ClearFormatting()
                rn.Find.Text = texts[i-2]
                if rn.Find.Execute():
                            rn.Font.Bold = True
            i+=1

    def save(self):
        section = doc.Sections(1)
        section.PageSetup.VerticalAlignment = win32com.client.constants.wdAlignVerticalCenter
        doc.SaveAs2(config.pathSave, FileFormat=16)  # FileFormat=16 corresponds to .docx format
        doc.Close()
        openWord.Quit()




    






