
#include <string>
#include <ranges>
#include <cctype>
#include <vector>
#include <algorithm>
#include <string_view>
#include <unordered_map>
using namespace std;

class Solution {

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
    static const int SINGLE_FREQUENCY_WITHOUT_NUMBER = 0;
    static const char OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')';
    static const char CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '(';

    int index = 0;
    int accumulatedAtomicFrequencyInBrackets = 0;

public:
    string countOfAtoms(string formula) {
        unordered_map<string, int> atomsToFrequency = createMapAtomsToFrequency(formula);
        return createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency);
    }

private:
    unordered_map<string, int> createMapAtomsToFrequency(string_view formula) {

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
        vector<int> atomicFrequencyPerOpenedBracket;
        unordered_map<string, int> atomsToFrequency;
        accumulatedAtomicFrequencyInBrackets = 0;

        for (index = formula.length() - 1; index >= 0; --index) {

            int atomicFrequency = extractAtomicFrequency(formula);
            if (formula[index] == OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleOpeningBracket(atomicFrequencyPerOpenedBracket, atomicFrequency);
                continue;
            }

            string atomicLabel = extractAtomicLabel(formula);
            if (accumulatedAtomicFrequencyInBrackets > 0) {
                atomicFrequency = max(atomicFrequency, 1) * accumulatedAtomicFrequencyInBrackets;
            }

            updateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency);

            if (index >= 0 && formula[index] == CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleClosingBracket(atomicFrequencyPerOpenedBracket);
                continue;
            }

            ++index;
        }
        return atomsToFrequency;
    }

    int extractAtomicFrequency(string_view formula) {
        int frequency = SINGLE_FREQUENCY_WITHOUT_NUMBER;
        int digitPosition = 1;
        while (index >= 0 && isdigit(formula[index])) {
            frequency += digitPosition * (formula[index] - '0');
            digitPosition *= 10;
            --index;
        }
        return frequency;
    }

    string extractAtomicLabel(string_view formula) {
        string atomicLabel;
        int capitalLettersFrequency = 0;

        while (index >= 0 && isalpha(formula[index]) && capitalLettersFrequency == 0) {
            atomicLabel.push_back(formula[index]);
            if (isCapitalLetter(formula[index])) {
                ++capitalLettersFrequency;
            }
            --index;
        }
        ranges::reverse(atomicLabel);

        return atomicLabel;
    }

    bool isCapitalLetter(char character) {
        return character >= 'A' && character <= 'Z';
    }

    void handleOpeningBracket(vector<int>& atomicFrequencyPerOpenedBracket, int atomicFrequency) {
        atomicFrequencyPerOpenedBracket.push_back(atomicFrequency);
        if (atomicFrequency != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
            accumulatedAtomicFrequencyInBrackets = max(accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency;
        }
    }

    void handleClosingBracket(vector<int>& atomicFrequencyPerOpenedBracket) {
        int atomicFrequencyMostRecentOpeningBracket = atomicFrequencyPerOpenedBracket.back();
        atomicFrequencyPerOpenedBracket.pop_back();

        if (atomicFrequencyPerOpenedBracket.empty()) {
            accumulatedAtomicFrequencyInBrackets = 0;
        }
        else if (atomicFrequencyMostRecentOpeningBracket > 0) {
            accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket;
        }
    }

    void updateMapAtomsToFrequency(unordered_map<string, int>& atomsToFrequency, const string& atomicLabel, int atomicFrequency) {
        if (atomsToFrequency.contains(atomicLabel)) {
            atomsToFrequency[atomicLabel] = atomsToFrequency[atomicLabel] + max(atomicFrequency, 1);
        }
        else if (!atomicLabel.empty()) {
            atomsToFrequency[atomicLabel] = atomicFrequency;
        }
    }

    string createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(const unordered_map<string, int>& atomsToFrequency) {
        vector<string> pairsAtomsAndFrequencies;
        for (const auto& [label, frequency] : atomsToFrequency) {
            string current = label;
            if (frequency != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
                current += to_string(frequency);
            }
            pairsAtomsAndFrequencies.push_back(current);
        }
        ranges::sort(pairsAtomsAndFrequencies, [](const auto& first, const auto& second) {return first < second; });

        string atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel;
        for (const auto& atomAndFrequency : pairsAtomsAndFrequencies) {
            atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.append(atomAndFrequency);
        }

        return atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel;
    }
};
