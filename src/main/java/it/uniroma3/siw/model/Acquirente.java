package it.uniroma3.siw.model;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Acquirente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nome;    //esempio "Trenord Lombardia"

    @Column(unique = true)
    private String codiceAssociazione;  //codice per identificare acquirente
    private String telegramChatId;      //inizialmente null poi lo assegna il bot

    //costruttore vuoto per JPA
    // JPA permette la comunicazione tra JAVA e SQL
    public Acquirente(){}       //quando chiedo di cercare un oggetto acquirente con determinati values nel database, per restituirlo JPA deve prima crearlo quindi uso un costruttore vuoto

    //metodo per generare il codiceAssociazione
    @PrePersist
    public void generaCodice(){
        if(this.codiceAssociazione==null){
            // Faccio generare una stringa casuale es: "NURV-A3F9-..." e prende i primi 8 caratteri generati random
            this.codiceAssociazione = "NURV-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        }
    }

    //Getter e Setter
    public Long getId(){ return id; }
    public void setId(Long id){ this.id=id;}

    public String getNome(){ return nome;}
    public void setNome(String nome){ this.nome = nome;}

    public String getCodiceAssociazione(){ return codiceAssociazione;}
    public void setCodiceAssociazione(String codiceAssociazione){ this.codiceAssociazione = codiceAssociazione;}

    public String getTelegramChatId(){ return telegramChatId;}
    public void setTelegramChatId(String telegramChatId){ this.telegramChatId = telegramChatId;}
}
