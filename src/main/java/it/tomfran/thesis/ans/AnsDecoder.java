package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnsDecoder {
  /** Sum of frequencies. */
  protected int M;
  /** Number of symbols. */
  protected int N;
  /** Symbols to index mapping. */
  protected HashMap<Integer, Integer> symbolsMapping;
  /** Index to symbols mapping. */
  protected HashMap<Integer, Integer> invSymbolsMapping;
  /** Symbol frequencies. */
  protected int[] frequencies;
  /** Cumulative array. */
  protected int[] cumulative;
  /** Symbol array. */
  protected int[] sym;
  /** Stream to read encoder info. */
  protected LongInputStream is;
  /** Number of states. */
  protected int stateCount;
  /** List with intermediate states. */
  protected int[] stateList;
  /** Current state index. */
  protected int stateIndex;

  /**
   * Build a decoder initializing the symbol statistics from the LongInputStream.
   *
   * @param lis stream containing an encoder representation.
   */
  public AnsDecoder(LongInputStream lis) throws IOException {
    is = lis;
    initializeVariables();
  }

  private void initializeVariables() throws IOException {
    // Read in order:
    // N
    // 2N ints representing mappings,
    // N frequencies
    // number of normalizations
    // initial state

    N = is.readInt(31);
    if (N == 0) {
      stateIndex = 0;
      stateList = new int[1];
      return;
    }
    symbolsMapping = new HashMap<>();
    invSymbolsMapping = new HashMap<>();
    int symbol, index;
    for (int i = 0; i < N; i++) {
      symbol = is.readInt(31);
      index = is.readInt(31);
      symbolsMapping.put(symbol, index);
      invSymbolsMapping.put(index, symbol);
    }
    // build frequencies and cumulative array
    M = 0;
    frequencies = new int[N];
    cumulative = new int[N];
    cumulative[0] = 1;
    for (int i = 0; i < N; i++) {
      frequencies[i] = is.readInt(31);
      M += frequencies[i];
      if (i > 0)
        cumulative[i] = cumulative[i-1] + frequencies[i-1];
    }
    // build sym array
    sym = new int[M+1];
    int pos = 1;
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < frequencies[i]; j++)
        sym[pos++] = i;
    }
    // initialize normalization count and initial state
    stateCount = is.readInt(31);
    stateIndex = 0;
    stateList = new int[stateCount];
    // fill states
    for (int i = 0; i < stateCount; i++)
      stateList[i] = readNextState();
  }

  private int readNextState() throws IOException {
    int ret = is.readInt(31);
    return ret;
  }

  /**
   * Decode the encoded sequence from the state.
   *
   * @return decoded integer List.
   */
  public List<Integer> decodeAll(){
    ArrayList<Integer> ret = new ArrayList<>();
    int e;
    while ((e = decode()) != -1)
      ret.add(e);

    return ret;
  }

  /**
   * Decode a single element from the state.
   *
   * @return decoded integer.
   */
  public int decode(){
//        System.out.println("State Index: " + stateIndex);
//
//        System.out.println("State count: " + stateCount);
    // if last state is reached, return -1
    if (stateIndex == stateCount) return -1;

    int fs, cs, r, symIndex, state, j;
    state = stateList[stateIndex];
    // remainder to identity symbol
    r = (int) (1 + (Long.remainderUnsigned(state-1, M)));
    // get freq and cumulative
    symIndex = sym[r];
    fs = frequencies[symIndex];
    cs = cumulative[symIndex];
    // update the state
    j = Integer.divideUnsigned(state-r, M);
    stateList[stateIndex] = j * fs - cs + r;

    // if current state is over, change to next one
    if (Long.compareUnsigned(stateList[stateIndex], 0L) == 0)
      stateIndex++;

    return invSymbolsMapping.get(symIndex);
  }


  /**
   * Print decoder info.
   */
  public void debugPrint(){
    System.out.println("---- Symbol mapping --------");
    for (Map.Entry<Integer, Integer> e : symbolsMapping.entrySet())
      System.out.println(e.getKey() + "->" + e.getValue());

    System.out.println("---- Inv Symbol mapping ----");
    for (Map.Entry<Integer, Integer> e : invSymbolsMapping.entrySet())
      System.out.println(e.getKey() + "->" + e.getValue());

    System.out.println("---- Frequencies -----------");
    for (int i = 0; i < N; i++)
      System.out.print(frequencies[i] + " ");
    System.out.println();

    System.out.println("---- Cumulative ------------");
    for (int i = 0; i < N; i++)
      System.out.print(cumulative[i] + " ");
    System.out.println();

    System.out.println("---- States ----------------");
    for (int i = 0; i < stateCount; i++) {
      System.out.println(stateList[i]);
    }
    System.out.println("Current state count: " + stateCount);
    System.out.println();
  }

  /**
   * Print the current state and its binary representation.
   */
  public void printState(){
    int state = stateList[stateIndex];
    double s = (state == 0)? 0 : Math.ceil(Math.log(state));
    System.out.println("ANS state -> " + state + " " + s + " bits");
  }
}
