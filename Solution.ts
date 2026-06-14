
function countOfAtoms(formula: string): string {
    const util = new Util();
    const atomsToFrequency: Map<string, number> = createMapAtomsToFrequency(formula, util);
    return createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency);
};

class Util {

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
    static SINGLE_FREQUENCY_WITHOUT_NUMBER = 0;
    static OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')';
    static CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '(';
    static ASCII_ZERO = 48;

    index = 0;
    accumulatedAtomicFrequencyInBrackets = 0;
}

function createMapAtomsToFrequency(formula: string, util: Util): Map<string, number> {

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
    const atomicFrequencyPerOpenedBracket: number[] = new Array();
    const atomsToFrequency: Map<string, number> = new Map();
    util.accumulatedAtomicFrequencyInBrackets = 0;

    for (util.index = formula.length - 1; util.index >= 0; --util.index) {

        let atomicFrequency = extractAtomicFrequency(formula, util);
        if (formula.charAt(util.index) === Util.OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
            handleOpeningBracket(atomicFrequencyPerOpenedBracket, atomicFrequency, util);
            continue;
        }

        const atomicLabel = extractAtomicLabel(formula, util);
        if (util.accumulatedAtomicFrequencyInBrackets > 0) {
            atomicFrequency = Math.max(atomicFrequency, 1) * util.accumulatedAtomicFrequencyInBrackets;
        }

        updateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency);

        if (util.index >= 0 && formula.charAt(util.index) === Util.CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES) {
            handleClosingBracket(atomicFrequencyPerOpenedBracket, util);
            continue;
        }

        ++util.index;
    }
    return atomsToFrequency;
}

function extractAtomicFrequency(formula: string, util: Util): number {
    let frequency = Util.SINGLE_FREQUENCY_WITHOUT_NUMBER;
    let digitPosition = 1;
    while (util.index >= 0 && isDigit(formula.charAt(util.index))) {
        frequency += digitPosition * (formula.codePointAt(util.index) - Util.ASCII_ZERO);
        digitPosition *= 10;
        --util.index;
    }
    return frequency;
}

function extractAtomicLabel(formula: string, util: Util): string {
    const atomicLabel: string[] = new Array();
    let capitalLettersFrequency = 0;

    while (util.index >= 0 && isLetter(formula.charAt(util.index)) && capitalLettersFrequency === 0) {
        atomicLabel.push(formula.charAt(util.index));
        if (isCapitalLetter(formula.charAt(util.index))) {
            ++capitalLettersFrequency;
        }
        --util.index;
    }

    return atomicLabel.reverse().join('');
}

function isDigit(character: string): boolean {
    return /[0-9]/.test(character);
}

function isLetter(character: string): boolean {
    return /[a-zA-Z]/.test(character);
}

function isCapitalLetter(character: string): boolean {
    return /[A-Z]/.test(character);
}

function handleOpeningBracket(atomicFrequencyPerOpenedBracket: number[], atomicFrequency: number, util: Util): void {
    atomicFrequencyPerOpenedBracket.push(atomicFrequency);
    if (atomicFrequency !== Util.SINGLE_FREQUENCY_WITHOUT_NUMBER) {
        util.accumulatedAtomicFrequencyInBrackets = Math.max(util.accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency;
    }
}

function handleClosingBracket(atomicFrequencyPerOpenedBracket: number[], util: Util): void {
    const atomicFrequencyMostRecentOpeningBracket = atomicFrequencyPerOpenedBracket.pop();
    if (atomicFrequencyPerOpenedBracket.length === 0) {
        util.accumulatedAtomicFrequencyInBrackets = 0;
    } else if (atomicFrequencyMostRecentOpeningBracket > 0) {
        util.accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket;
    }
}

function updateMapAtomsToFrequency(atomsToFrequency: Map<string, number>, atomicLabel: string, atomicFrequency: number): void {
    if (atomsToFrequency.has(atomicLabel)) {
        atomsToFrequency.set(atomicLabel, atomsToFrequency.get(atomicLabel) + Math.max(atomicFrequency, 1));
    } else if (atomicLabel.length > 0) {
        atomsToFrequency.set(atomicLabel, atomicFrequency);
    }
}

function createConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency: Map<string, number>): string {
    const pairsAtomsAndFrequencies: string[] = new Array();
    for (let current of atomsToFrequency.keys()) {
        if (atomsToFrequency.get(current) !== Util.SINGLE_FREQUENCY_WITHOUT_NUMBER) {
            current += atomsToFrequency.get(current);
        }
        pairsAtomsAndFrequencies.push(current);
    }
    pairsAtomsAndFrequencies.sort((first, second) => first.localeCompare(second));

    const atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel = new Array();
    for (let atomAndFrequency of pairsAtomsAndFrequencies) {
        atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.push(atomAndFrequency);
    }

    return atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.join('');
}
