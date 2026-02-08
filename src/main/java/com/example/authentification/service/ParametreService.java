package com.example.authentification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.authentification.entity.Parametre;
import com.example.authentification.repository.ParametreRepository;

@Service
public class ParametreService {

    @Autowired
    private ParametreRepository parametreRepository;

    public int getIntValue(String key) throws Exception{
        Parametre param = parametreRepository.findByCle(key).orElseThrow();
        return Integer.parseInt(param.getValeur());
    }

    public void setParam(String cle, String valeur, String type, String desc) {
        Parametre param = parametreRepository.findByCle(cle).orElse(new Parametre());
        param.setCle(cle);
        param.setValeur(valeur);
        param.setType(type);
        param.setDescription(desc);
        parametreRepository.save(param);
    }
}
