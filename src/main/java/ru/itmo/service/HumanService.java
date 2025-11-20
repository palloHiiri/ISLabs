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

    public Human processGovernor(Human governor) {
        if (governor.getPassport() == null) {
            throw new IllegalArgumentException("Governor passport is required");
        }

        Human existingGovernor = humanRepository.findByPassport(governor.getPassport());

        if (existingGovernor != null) {
            if (!existingGovernor.getName().equals(governor.getName())) {
                throw new IllegalArgumentException(
                        "Governor with passport " + governor.getPassport() +
                                " already exists with different name: '" + existingGovernor.getName() +
                                "' instead of '" + governor.getName() + "'"
                );
            }
            return existingGovernor;
        } else {
            Long governorId = humanRepository.save(governor);
            governor.setId(governorId);
            return governor;
        }
    }


}
