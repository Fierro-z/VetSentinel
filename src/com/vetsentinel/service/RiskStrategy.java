package com.vetsentinel.service;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.RiskResult;

public interface RiskStrategy {
    boolean canHandle(Diagnostico diagnostico);
    RiskResult evaluate(Diagnostico diagnostico);
}
