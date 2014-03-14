LATEX = xelatex
DVIPDF = dvipdfm
SPELL = aspell
TEX = drummer
BIBTEX = bibtex
SOURCES = $(wildcard *.tex) $(wildcard *.bib)

default: pdf

pdf:
	$(LATEX) $(TEX).tex
	$(BIBTEX) $(TEX)
	$(LATEX) $(TEX).tex
	$(LATEX) $(TEX).tex
	make open

open:
	open $(TEX).pdf

clean:
	$(RM) -f *.aux *.blg *.dvi *.log *.toc *.lof *.lot *.cb *.bbl $(TEX).ps $(TEX).pdf *~ *.out
	$(RM) -i *-converted-to.pdf
