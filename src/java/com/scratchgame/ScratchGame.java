package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScratchGame {
    private final GameConfig config;
    

    public ScratchGame(GameConfig config) {
        this.config = config;
        this.random = new Random();
        this.objectMapper = new ObjectMapper();
    }


} 