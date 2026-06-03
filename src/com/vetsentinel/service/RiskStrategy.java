package com.vetsentinel.service;

import com.vetsentinel.model.Diagnostico;

public interface RiskStrategy {
    boolean canHandle(Diagnostico diagnostico);
    String evaluate(Diagnostico diagnostico);
}
