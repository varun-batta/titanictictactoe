package com.varunbatta.titanictictactoe;

import com.facebook.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by varun on 2/28/2018.
 */

public class GameRequest {
    public String graphPath;
    public Map<String, String> parameters;
    public HttpMethod httpMethod;

    GameRequest() {
        graphPath = "";
        parameters = new HashMap<String, String>();
        httpMethod = HttpMethod.GET;
    }

    public GameRequest createNewGameRequest(String graphPath, Map<String, String> parameters, HttpMethod httpMethod) {
        this.graphPath = graphPath;
        if (parameters != null) {
            this.parameters = parameters;
        }
        if (httpMethod != null) {
            this.httpMethod = httpMethod;
        }
        return this;
    }
}
