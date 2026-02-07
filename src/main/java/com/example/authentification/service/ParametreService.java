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
        Parametre param = parametreRepository.findByCle("MAX_FAILED_ATTEMPTS").orElseThrow();
        return Integer.parseInt(param.getValeur());
    }
}
