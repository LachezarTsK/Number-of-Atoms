
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {

    /*
    SINGLE_FREQUENCY_WITHOUT_NUMBER is implemented with the following considerations in mind.

    In the input formula, an occurrence of a single atom can be written in two ways.
    Either the atomic label followed by 1 or just the atomic label.
    Thus, for example, a single atom of H, can be written either H1 or H.

    This notation must be preserved in the output of the results, of course, provided that the single 
    atom is not within brackets with a larger frequency and/or the atom does not occur at multiple places
    in the input formula. 

    Examples for atomic label H

    input formula: H some other atoms 
    correct output for atomic label H: H

    input formula: H1 some other atoms 
    correct output for atomic label H: H1

    input formula: some other atoms (some other atoms (H)10)2 some other atoms 
    correct output for atomic label H: H20

    input formula: H some other atoms H1 some other atoms H some other atoms H5
    correct output for atomic label H: H8
     */
    private static final int SINGLE_FREQUENCY_WITHOUT_NUMBER = 0;
    private static final char OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')';
    private static final char CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '(';

    private int index;
    private int accumulatedAtomicFrequencyInBrackets;

    public String countOfAtoms(String formula) {
        Map<String, Integer> atomsToFrequency = createMapAtomsToFrequency(formula);
        return createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency);
    }

    private Map<String, Integer> createMapAtomsToFrequency(String formula) {

        /*
        If there is an opening bracket and the atomic frequency immediately preceding 
        the bracket is equal to zero, this atomic frequency is still added to 
        atomicFrequencyPerOpenedBracket in order to synchronize the opening and the closing 
        of the brackets with adding and removing values from atomicFrequencyPerOpenedBracket. 
        
        Thus, accumulatedAtomicFrequencyInBrackets can be correctly updated without many complications.
        
        The brackets are opened, respectively closed, in direction from larger to smaller indexes, 
        i.e., the opening bracket is always at a larger index than the closing bracket. In this way, 
        the frequency of the atoms within the brackets can be immediately updated with the value of 
        accumulatedAtomicFrequencyInBrackets.        
         */
        Deque<Integer> atomicFrequencyPerOpenedBracket = new ArrayDeque<>();
        Map<String, Integer> atomsToFrequency = new HashMap<>();
        accumulatedAtomicFrequencyInBrackets = 0;

        for (index = formula.length() - 1; index >= 0; --index) {

            int atomicFrequency = extractAtomicFrequency(formula);
            if (formula.charAt(index) == OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleOpeningBracket(atomicFrequencyPerOpenedBracket, atomicFrequency);
                continue;
            }

            String atomicLabel = extractAtomicLabel(formula);
            if (accumulatedAtomicFrequencyInBrackets > 0) {
                atomicFrequency = Math.max(atomicFrequency, 1) * accumulatedAtomicFrequencyInBrackets;
            }

            updateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency);

            if (index >= 0 && formula.charAt(index) == CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleClosingBracket(atomicFrequencyPerOpenedBracket);
                continue;
            }

            ++index;
        }
        return atomsToFrequency;
    }

    private int extractAtomicFrequency(String formula) {
        int frequency = SINGLE_FREQUENCY_WITHOUT_NUMBER;
        int digitPosition = 1;
        while (index >= 0 && Character.isDigit(formula.charAt(index))) {
            frequency += digitPosition * (formula.charAt(index) - '0');
            digitPosition *= 10;
            --index;
        }
        return frequency;
    }

    private String extractAtomicLabel(String formula) {
        StringBuilder atomicLabel = new StringBuilder();
        int capitalLettersFrequency = 0;

        while (index >= 0 && Character.isLetter(formula.charAt(index)) && capitalLettersFrequency == 0) {
            atomicLabel.append(formula.charAt(index));
            if (isCapitalLetter(formula.charAt(index))) {
                ++capitalLettersFrequency;
            }
            --index;
        }

        return atomicLabel.reverse().toString();
    }

    private boolean isCapitalLetter(char character) {
        return character >= 'A' && character <= 'Z';
    }

    private void handleOpeningBracket(Deque<Integer> atomicFrequencyPerOpenedBracket, int atomicFrequency) {
        atomicFrequencyPerOpenedBracket.addFirst(atomicFrequency);
        if (atomicFrequency != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
            accumulatedAtomicFrequencyInBrackets = Math.max(accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency;
        }
    }

    private void handleClosingBracket(Deque<Integer> atomicFrequencyPerOpenedBracket) {
        int atomicFrequencyMostRecentOpeningBracket = atomicFrequencyPerOpenedBracket.pollFirst();
        if (atomicFrequencyPerOpenedBracket.isEmpty()) {
            accumulatedAtomicFrequencyInBrackets = 0;
        } else if (atomicFrequencyMostRecentOpeningBracket > 0) {
            accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket;
        }
    }

    private void updateMapAtomsToFrequency(Map<String, Integer> atomsToFrequency, String atomicLabel, int atomicFrequency) {
        if (atomsToFrequency.containsKey(atomicLabel)) {
            atomsToFrequency.put(atomicLabel, atomsToFrequency.get(atomicLabel) + Math.max(atomicFrequency, 1));
        } else if (!atomicLabel.isEmpty()) {
            atomsToFrequency.put(atomicLabel, atomicFrequency);
        }
    }

    private String createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(Map<String, Integer> atomsToFrequency) {
        List<String> atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel = new ArrayList<>();
        for (String current : atomsToFrequency.keySet()) {
            if (atomsToFrequency.get(current) != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
                current += atomsToFrequency.get(current);
            }
            atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.add(current);
        }
        Collections.sort(atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel);

        return String.join("", atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel);
    }
}
