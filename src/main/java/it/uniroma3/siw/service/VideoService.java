package it.uniroma3.siw.service;


import it.uniroma3.siw.model.Tratta;
import it.uniroma3.siw.model.Video;
import it.uniroma3.siw.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public Video getById(Long id) {
        return videoRepository.findById(id).orElse(null);
    }

    public void save(Video video) {
        this.videoRepository.save(video);
    }

    public Video getByNomeInTratta(String nome, Tratta tratta) {
        return videoRepository.findByNomeInTratta(nome.replace(" ", "").toLowerCase(), tratta.getId());
    }

    public void delete(Video video){
        this.videoRepository.delete(video);
    }
}
