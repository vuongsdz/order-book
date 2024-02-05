package org.example.dispatchers;

import org.example.entities.MatchingResult;

public interface MatchingResultListener {
    void onMatched(MatchingResult result);
}
