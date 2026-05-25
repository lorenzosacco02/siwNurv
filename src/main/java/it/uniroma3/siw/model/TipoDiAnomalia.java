package it.uniroma3.siw.model;

public enum TipoDiAnomalia {
	// Tipi esistenti
	ROTTURA_BINARIO,
	OSTACOLO,
	DERAGLIAMENTO,

	// ❗ Nuovi tipi di anomalie strutturali/oggetti dall'IA
	DILATAZIONE_FERROVIA,
	PALO_INCLINATO,
	VEGETAZIONE_VICINA,
	SASSO_SU_BINARIO,

	//Nuovi oggetti dinamici specifici da YOLOv8
	PERSONA_SUI_BINARI,
	ANIMALE_SUI_BINARI,
	VEICOLO,

	ALTRO // Per i casi UNKNOWN_OBJECT o ALTRO
}