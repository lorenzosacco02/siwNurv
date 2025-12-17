package it.uniroma3.siw.controller;

import java.util.Map;

// Classe per rappresentare il Bounding Box
class BBoxDto {
    public Integer x;
    public Integer y;
    public Integer w;
    public Integer h;
    // Getters/Setters omessi per brevità, ma necessari in un vero progetto
}

// Data Transfer Object per ricevere l'alert dal Python
public class AlertDtoController {
    public String project;
    public Integer frame_idx;
    public Double time_s;
    public BBoxDto bbox;
    public Integer area;
    public String label;
    public Double conf;
    // ❗ Campi critici per le anomalie strutturali:
    public String severity; // CRITICA, MEDIO, BASSO
    public String details;  // Dettagli tecnici

    public String source_video;
    public Long videoId; // Opzionale

    // Getters/Setters omessi per brevità
}