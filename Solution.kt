
import kotlin.math.max

class Solution {

    private companion object {

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
        const val SINGLE_FREQUENCY_WITHOUT_NUMBER = 0
        const val OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')'
        const val CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '('
    }

    private var index = 0
    private var accumulatedAtomicFrequencyInBrackets = 0

    fun countOfAtoms(formula: String): String {
        val atomsToFrequency: MutableMap<String, Int> = createMapAtomsToFrequency(formula)
        return createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency)
    }

    private fun createMapAtomsToFrequency(formula: String): MutableMap<String, Int> {

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
        val atomicFrequencyPerOpenedBracket = ArrayDeque<Int>()
        val atomsToFrequency = mutableMapOf<String, Int>()
        accumulatedAtomicFrequencyInBrackets = 0

        index = formula.length
        while (--index >= 0) {

            var atomicFrequency = extractAtomicFrequency(formula)
            if (formula[index] == OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleOpeningBracket(atomicFrequencyPerOpenedBracket, atomicFrequency)
                continue
            }

            val atomicLabel = extractAtomicLabel(formula)
            if (accumulatedAtomicFrequencyInBrackets > 0) {
                atomicFrequency = max(atomicFrequency, 1) * accumulatedAtomicFrequencyInBrackets
            }

            updateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency)

            if (index >= 0 && formula[index] == CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
                handleClosingBracket(atomicFrequencyPerOpenedBracket)
                continue
            }
            ++index
        }
        return atomsToFrequency
    }

    private fun extractAtomicFrequency(formula: String): Int {
        var frequency = SINGLE_FREQUENCY_WITHOUT_NUMBER
        var digitPosition = 1
        while (index >= 0 && Character.isDigit(formula[index])) {
            frequency += digitPosition * (formula[index] - '0')
            digitPosition *= 10
            --index
        }
        return frequency
    }

    private fun extractAtomicLabel(formula: String): String {
        val atomicLabel = StringBuilder()
        var capitalLettersFrequency = 0

        while (index >= 0 && Character.isLetter(formula[index]) && capitalLettersFrequency == 0) {
            atomicLabel.append(formula[index])
            if (isCapitalLetter(formula[index])) {
                ++capitalLettersFrequency
            }
            --index
        }

        return atomicLabel.reverse().toString()
    }

    private fun isCapitalLetter(character: Char): Boolean {
        return character in 'A'..'Z'
    }

    private fun handleOpeningBracket(atomicFrequencyPerOpenedBracket: ArrayDeque<Int>, atomicFrequency: Int) {
        atomicFrequencyPerOpenedBracket.addFirst(atomicFrequency)
        if (atomicFrequency != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
            accumulatedAtomicFrequencyInBrackets = max(accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency
        }
    }

    private fun handleClosingBracket(atomicFrequencyPerOpenedBracket: ArrayDeque<Int>) {
        val atomicFrequencyMostRecentOpeningBracket = atomicFrequencyPerOpenedBracket.removeFirst()
        if (atomicFrequencyPerOpenedBracket.isEmpty()) {
            accumulatedAtomicFrequencyInBrackets = 0
        } else if (atomicFrequencyMostRecentOpeningBracket > 0) {
            accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket
        }
    }

    private fun updateMapAtomsToFrequency(atomsToFrequency: MutableMap<String, Int>, atomicLabel: String, atomicFrequency: Int) {
        if (atomsToFrequency.containsKey(atomicLabel)) {
            atomsToFrequency[atomicLabel] = atomsToFrequency[atomicLabel]!! + max(atomicFrequency, 1)
        } else if (atomicLabel.isNotEmpty()) {
            atomsToFrequency[atomicLabel] = atomicFrequency
        }
    }

    private fun createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency: MutableMap<String, Int>): String {
        val atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel = mutableListOf<String>()
        for ((label, frequency) in atomsToFrequency) {
            var current = label
            if (frequency != SINGLE_FREQUENCY_WITHOUT_NUMBER) {
                current += frequency
            }
            atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.add(current)
        }
        atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.sort()

        return atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.joinToString("")
    }
}
