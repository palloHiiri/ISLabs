package ru.itmo.service;

import lombok.AllArgsConstructor;
import ru.itmo.model.Human;
import ru.itmo.repository.HumanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class HumanService {
    private final HumanRepository humanRepository;


    @Transactional(readOnly = true)
    public List<Human> getAllHumans() {
        return humanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Human getHumanById(Long id) {
        return humanRepository.findById(id);
    }

    @Transactional
    public Long addHuman(Human human) {
        return humanRepository.save(human);
    }

    @Transactional
    public void updateHuman(Human human) {
        humanRepository.update(human);
    }

    @Transactional
    public void deleteHuman(Human human) {
        humanRepository.delete(human);
    }

    @Transactional(readOnly = true)
    public boolean existsByPassport(Long passport) {
        return humanRepository.existsByPassport(passport);
    }


}
