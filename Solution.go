
package main

import (
    "slices"
    "strconv"
    "strings"
    "unicode"
)

/*
SINGLE_FREQUENCY_WITHOUT_NUMBER is implemented with the following considerations in mind.

In the input formula, an occurrence of a single atom can be written in two ways.
Either the atomic label followed by 1 or just the atomic label.
Thus, for example, a single atom of H, can be written either H1 or H.

This notation must be preserved in the output of the results, of course, provided that the single
atom is not within brackets with a larger frequency and/or the atom does not occur at multiple places
in the input formula.

# Examples for atomic label H

input formula: H some other atoms
correct output for atomic label H: H

input formula: H1 some other atoms
correct output for atomic label H: H1

input formula: some other atoms (some other atoms (H)10)2 some other atoms
correct output for atomic label H: H20

input formula: H some other atoms H1 some other atoms H some other atoms H5
correct output for atomic label H: H8
*/
const SINGLE_FREQUENCY_WITHOUT_NUMBER = 0
const OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')'
const CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '('

var index int
var accumulatedAtomicFrequencyInBrackets int

func countOfAtoms(formula string) string {
    var atomsToFrequency map[string]int = createMapAtomsToFrequency(formula)
    return createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency)
}

func createMapAtomsToFrequency(formula string) map[string]int {

    /*
       If there is an opening bracket and the atomic frequency immediately preceding
       the bracket is equal to zero, this atomic frequency is still added to
       atomicFrequencyPerOpenedBracket in order to synchronize the opening and the closing
       of the brackets with adding and removing values from atomicFrequencyPerOpenedBracket.

       Thus, accumulatedAtomicFrequencyInBrackets can be correctly updated without many complications.

       The brackets are opened, respectively closed, in direction from larger to smaller indexes,
       i.e., the opening bracket is always at a larger index than the closing bracket. In this way,
       the frequency of the atoms within the brackets can be immediately updated with the value of
       accumulatedAtomicFrequencyInBrackets.
    */
    atomicFrequencyPerOpenedBracket := []int{}
    atomsToFrequency := map[string]int{}
    accumulatedAtomicFrequencyInBrackets = 0

    for index = len(formula) - 1; index >= 0; index-- {

        atomicFrequency := extractAtomicFrequency(formula)
        if formula[index] == OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES {
            handleOpeningBracket(&atomicFrequencyPerOpenedBracket, atomicFrequency)
            continue
        }

        atomicLabel := extractAtomicLabel(formula)
        if accumulatedAtomicFrequencyInBrackets > 0 {
            atomicFrequency = max(atomicFrequency, 1) * accumulatedAtomicFrequencyInBrackets
        }

        updateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency)

        if index >= 0 && formula[index] == CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES {
            handleClosingBracket(&atomicFrequencyPerOpenedBracket)
            continue
        }
        index++
    }
    return atomsToFrequency
}

func extractAtomicFrequency(formula string) int {
    frequency := SINGLE_FREQUENCY_WITHOUT_NUMBER
    digitPosition := 1
    for index >= 0 && unicode.IsDigit(rune(formula[index])) {
        frequency += digitPosition * int(formula[index] - '0')
        digitPosition *= 10
        index--
    }
    return frequency
}

func extractAtomicLabel(formula string) string {
    atomicLabel := []byte{}
    capitalLettersFrequency := 0

    for index >= 0 && unicode.IsLetter(rune(formula[index])) && capitalLettersFrequency == 0 {
        atomicLabel = append(atomicLabel, formula[index])
        if unicode.IsUpper(rune(formula[index])) {
            capitalLettersFrequency++
        }
        index--
    }
    slices.Reverse(atomicLabel)

    return string(atomicLabel)
}

func handleOpeningBracket(atomicFrequencyPerOpenedBracket *[]int, atomicFrequency int) {
    *atomicFrequencyPerOpenedBracket = append(*atomicFrequencyPerOpenedBracket, atomicFrequency)
    if atomicFrequency != SINGLE_FREQUENCY_WITHOUT_NUMBER {
        accumulatedAtomicFrequencyInBrackets = max(accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency
    }
}

func handleClosingBracket(atomicFrequencyPerOpenedBracket *[]int) {
    atomicFrequencyMostRecentOpeningBracket := (*atomicFrequencyPerOpenedBracket)[len(*atomicFrequencyPerOpenedBracket) - 1]
    *atomicFrequencyPerOpenedBracket = (*atomicFrequencyPerOpenedBracket)[:len(*atomicFrequencyPerOpenedBracket) - 1]

    if len(*atomicFrequencyPerOpenedBracket) == 0 {
        accumulatedAtomicFrequencyInBrackets = 0
    } else if atomicFrequencyMostRecentOpeningBracket > 0 {
        accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket
    }
}

func updateMapAtomsToFrequency(atomsToFrequency map[string]int, atomicLabel string, atomicFrequency int) {
    if containsKey(atomsToFrequency, atomicLabel) {
        atomsToFrequency[atomicLabel] = atomsToFrequency[atomicLabel] + max(atomicFrequency, 1)
    } else if len(atomicLabel) > 0 {
        atomsToFrequency[atomicLabel] = atomicFrequency
    }
}

func containsKey[Key comparable, Value any](mapToCheck map[Key]Value, key Key) bool {
    var has bool
    _, has = mapToCheck[key]
    return has
}

func createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency map[string]int) string {
    pairsAtomsAndFrequencies := []string{}
    for label, frequency := range atomsToFrequency {
        current := label
        if frequency != SINGLE_FREQUENCY_WITHOUT_NUMBER {
            current += strconv.Itoa(frequency)
        }
        pairsAtomsAndFrequencies = append(pairsAtomsAndFrequencies, current)
    }
    slices.Sort(pairsAtomsAndFrequencies)

    atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel := strings.Builder{}
    for _, atomAndFrequency := range pairsAtomsAndFrequencies {
        atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.WriteString(atomAndFrequency)
    }

    return atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.String()
}
