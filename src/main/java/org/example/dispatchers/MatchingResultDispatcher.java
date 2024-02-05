package org.example.dispatchers;

import org.example.entities.MatchingResult;

import java.util.ArrayList;
import java.util.List;

public class MatchingResultDispatcher {
    private static final MatchingResultDispatcher INSTANCE = new MatchingResultDispatcher();

    public static MatchingResultDispatcher getInstance() {
        return INSTANCE;
    }

    private final List<MatchingResultListener> listeners;

    private MatchingResultDispatcher() {
        listeners = new ArrayList<>();
    }

    public void registerListener(MatchingResultListener listener) {
        listeners.add(listener);
    }

    public void dispatch(MatchingResult result) {
        for (MatchingResultListener listener : listeners) {
            listener.onMatched(result);
        }
    }
}
