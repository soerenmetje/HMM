package main.hmm.casino;

/**
 * Aufgabe: Implementieren Sie den Viterbi-Algorithmus fuer das HMM zu dem Beispiel des
 * unehrlichen Casinos, wie in der Vorlesung vorgestellt (siehe auch im Buch von
 * Durbin et al. "Biological sequence analysis" Seite 54-57).
 * <p>
 * Anhand  https://en.wikipedia.org/wiki/Viterbi_algorithm  implementiert.
 * <p>
 * Enthaelt die Implementation des Viterbi-Algorithmus, der aus einer uebergebenen Sequenz einen Zustands-Pfad generiert. F = Fair, L = Loaded.
 *
 * @author Soren Metje
 */
public class CasinoHMM {

    /**
     * beobachtbare Ereignisse
     */
    private static final char[] OBSERVATION_SPACE = {'1', '2', '3', '4', '5', '6'};

    /**
     * Zustaende in denen sich das Modell befindet
     */
    private static final char[] STATE_CHAR = {'F', 'L'};

    /**
     * Anzahl der Zustaende
     */
    private static final int STATE_COUNT = STATE_CHAR.length;

    /**
     * Uebergangswahrscheinlichen aus dem Startzustand in die jeweiligen Zustaende
     */
    private final double[] INIT_PROBABILITIES = new double[]{.5d, .5d};

    /**
     * Uebergangswahrscheinlichen zwischen den Zustaenden
     */
    private final double[][] TRANSITION_MATRIX = new double[][]{{.95d, .05d}, {.1d, .9d}};
    /**
     * Beobachtungswahrscheinlichketen der Ereignisse in den jeweiligen Zustaenden
     */
    private final double[][] EMISSION_MATRIX = new double[][]{{1d / 6, 1d / 6, 1d / 6, 1d / 6, 1d / 6, 1d / 6}, {.1d, .1d, .1d, .1d, .1d, .5d}};

    /**
     * Konstruktor.
     * Konvertiert die Matrizen fuer die Uebergangswahrscheinlichen und Beobachtungswahrscheinlichketen in den logarithmischen Raum
     */
    public CasinoHMM() {
        // convert into logspace (can be done before Viterbi-Algo is running)
        convertToLogspace(INIT_PROBABILITIES);
        convertToLogspace(TRANSITION_MATRIX);
        convertToLogspace(EMISSION_MATRIX);
    }

    /**
     * Konvertiert die uebergebene 3-dimensionale Matrix als Nebeneffekt in den logarithmischen Raum
     *
     * @param raum Raum
     */
    public static void convertToLogspace(double[][][] raum) {
        for (double[][] matrix : raum) {
            convertToLogspace(matrix);
        }
    }

    /**
     * Konvertiert die uebergebene Matrix als Nebeneffekt in den logarithmischen Raum
     *
     * @param matrix Matrix
     */
    public static void convertToLogspace(double[][] matrix) {
        for (double[] vector : matrix) {
            convertToLogspace(vector);
        }
    }

    /**
     * Konvertiert den uebergebenen Vektor als Nebeneffekt in den logarithmischen Raum
     *
     * @param vector Vektor
     */
    public static void convertToLogspace(double[] vector) {
        for (int j = 0; j < vector.length; j++) {
            vector[j] = Math.log(vector[j]);
        }
    }

    /**
     * Implementation des Viterbi-Algorithmus für den logarithmischen Raum
     *
     * @param observations Beobachtungsfolge
     * @return Zustands-Pfad
     */
    public char[] viterbi(final char[] observations) {

        // init
        int[] observationIndices = observationsToIndices(observations);
        int length = observationIndices.length;

        double[][] viterbiVar = new double[STATE_COUNT][length];
        int[][] viterbiArg = new int[STATE_COUNT][length];

        // init
        for (int stateIndex = 0; stateIndex < STATE_COUNT; stateIndex++) {

            viterbiVar[stateIndex][0] = INIT_PROBABILITIES[stateIndex] + EMISSION_MATRIX[stateIndex][observationIndices[0]]; // log-space
            viterbiArg[stateIndex][0] = -1;
        }

        // iterate observations indices
        for (int i = 1; i < length; i++) {
            // iterate states indices
            for (int j = 0; j < STATE_COUNT; j++) {


                //find max
                double maxProb = Double.NEGATIVE_INFINITY;
                int maxArg = -1; // maximizing argument

                for (int stateIndex = 0; stateIndex < STATE_COUNT; stateIndex++) {

                    double prob = viterbiVar[stateIndex][i - 1] + TRANSITION_MATRIX[stateIndex][j] + EMISSION_MATRIX[j][observationIndices[i]]; // log-space
                    if (prob > maxProb) {
                        maxProb = prob;
                        maxArg = stateIndex;
                    }
                }

                viterbiVar[j][i] = maxProb;
                viterbiArg[j][i] = maxArg;
            }
        }

        // backtrace init
        int zLast = -1;
        {
            double probLast = Double.NEGATIVE_INFINITY;
            for (int stateIndex = 0; stateIndex < STATE_COUNT; stateIndex++) {

                double prob = viterbiVar[stateIndex][length - 1];
                if (prob > probLast) {
                    zLast = stateIndex;
                    probLast = prob;
                }
            }
        }

        int[] z = new int[length]; // stateIndexPath
        char[] x = new char[length]; // statePath

        z[length - 1] = zLast;
        x[length - 1] = STATE_CHAR[zLast];

        // backtrace iterate
        for (int i = length - 1; i > 0; i--) {
            int m = viterbiArg[z[i]][i];
            z[i - 1] = m;
            x[i - 1] = STATE_CHAR[m];
        }

        return x;
    }

    /**
     * mappt Beaobachtung-Folge auf entsprechende Index-Folge
     *
     * @param observations Beobachtungs-Folge
     * @return entsprechende Index-Folge
     */
    private static int[] observationsToIndices(final char[] observations) {
        int length = observations.length;
        int[] ret = new int[length];

        for (int i = 0; i < length; i++) {
            ret[i] = obesrvationToIndex(observations[i]);
        }

        return ret;
    }

    /**
     * mappt Beaobachtung auf den entsprechenden Index
     *
     * @param observation Beobachtung
     * @return entsprechender Index
     */
    private static int obesrvationToIndex(final char observation) {
        for (int i = 0, observation_spaceLength = OBSERVATION_SPACE.length; i < observation_spaceLength; i++) {
            char c = OBSERVATION_SPACE[i];
            if (c == observation)
                return i;
        }
        return -1;
    }
}
