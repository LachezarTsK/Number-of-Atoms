
using System;
using System.Collections.Generic;

public class Solution
{
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
    private static readonly int SINGLE_FREQUENCY_WITHOUT_NUMBER = 0;
    private static readonly char OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = ')';
    private static readonly char CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES = '(';

    private int index;
    private int accumulatedAtomicFrequencyInBrackets;


    public string CountOfAtoms(string formula)
    {
        Dictionary<string, int> atomsToFrequency = CreateMapAtomsToFrequency(formula);
        return CreateConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(atomsToFrequency);
    }

    private Dictionary<string, int> CreateMapAtomsToFrequency(string formula)
    {

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
        Stack<int> atomicFrequencyPerOpenedBracket = [];
        Dictionary<string, int> atomsToFrequency = [];
        accumulatedAtomicFrequencyInBrackets = 0;

        for (index = formula.Length - 1; index >= 0; --index)
        {

            int atomicFrequency = ExtractAtomicFrequency(formula);
            if (formula[index] == OPENING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES)
            {
                HandleOpeningBracket(atomicFrequencyPerOpenedBracket, atomicFrequency);
                continue;
            }

            string atomicLabel = ExtractAtomicLabel(formula);
            if (accumulatedAtomicFrequencyInBrackets > 0)
            {
                atomicFrequency = Math.Max(atomicFrequency, 1) * accumulatedAtomicFrequencyInBrackets;
            }

            UpdateMapAtomsToFrequency(atomsToFrequency, atomicLabel, atomicFrequency);

            if (index >= 0 && formula[index] == CLOSING_BRACKET_FROM_LARGER_TO_SMALLER_INDEXES)
            {
                HandleClosingBracket(atomicFrequencyPerOpenedBracket);
                continue;
            }

            ++index;
        }
        return atomsToFrequency;
    }

    private int ExtractAtomicFrequency(string formula)
    {
        int frequency = SINGLE_FREQUENCY_WITHOUT_NUMBER;
        int digitPosition = 1;
        while (index >= 0 && char.IsDigit(formula[index]))
        {
            frequency += digitPosition * (formula[index] - '0');
            digitPosition *= 10;
            --index;
        }
        return frequency;
    }

    private string ExtractAtomicLabel(string formula)
    {
        StringBuilder atomicLabel = new();
        int capitalLettersFrequency = 0;

        while (index >= 0 && (char.IsLetter(formula[index])) && capitalLettersFrequency == 0)
        {
            atomicLabel.Append(formula[index]);
            if (IsCapitalLetter(formula[index]))
            {
                ++capitalLettersFrequency;
            }
            --index;
        }

        return string.Join("", atomicLabel.ToString().ToCharArray().Reverse());
    }

    private bool IsCapitalLetter(char character)
    {
        return character >= 'A' && character <= 'Z';
    }

    private void HandleOpeningBracket(Stack<int> atomicFrequencyPerOpenedBracket, int atomicFrequency)
    {
        atomicFrequencyPerOpenedBracket.Push(atomicFrequency);
        if (atomicFrequency != SINGLE_FREQUENCY_WITHOUT_NUMBER)
        {
            accumulatedAtomicFrequencyInBrackets = Math.Max(accumulatedAtomicFrequencyInBrackets, 1) * atomicFrequency;
        }
    }

    private void HandleClosingBracket(Stack<int> atomicFrequencyPerOpenedBracket)
    {
        int atomicFrequencyMostRecentOpeningBracket = atomicFrequencyPerOpenedBracket.Pop();
        if (atomicFrequencyPerOpenedBracket.Count == 0)
        {
            accumulatedAtomicFrequencyInBrackets = 0;
        }
        else if (atomicFrequencyMostRecentOpeningBracket > 0)
        {
            accumulatedAtomicFrequencyInBrackets /= atomicFrequencyMostRecentOpeningBracket;
        }
    }

    private void UpdateMapAtomsToFrequency(Dictionary<string, int> atomsToFrequency, string atomicLabel, int atomicFrequency)
    {
        if (atomsToFrequency.ContainsKey(atomicLabel))
        {
            atomsToFrequency[atomicLabel] = atomsToFrequency[atomicLabel] + Math.Max(atomicFrequency, 1);
        }
        else if (atomicLabel.Length > 0)
        {
            atomsToFrequency.Add(atomicLabel, atomicFrequency);
        }
    }

    private string CreateConstituentAtomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel(Dictionary<string, int> atomsToFrequency)
    {
        List<string> pairsAtomsAndFrequencies = [];
        foreach (string label in atomsToFrequency.Keys)
        {
            string current = label;
            if (atomsToFrequency[current] != SINGLE_FREQUENCY_WITHOUT_NUMBER)
            {
                current += atomsToFrequency[current];
            }
            pairsAtomsAndFrequencies.Add(current);
        }
        pairsAtomsAndFrequencies.Sort((first, second) => first.CompareTo(second));

        StringBuilder atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel = new();
        foreach (string atomAndFrequency in pairsAtomsAndFrequencies)
        {
            atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.Append(atomAndFrequency);
        }

        return atomsAndFrequenciesSortedAlphabeticallyPerAtomicLabel.ToString();
    }
}
