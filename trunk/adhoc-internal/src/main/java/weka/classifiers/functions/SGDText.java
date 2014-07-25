/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    SGDText.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.functions;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.RandomizableClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Stopwords;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.stemmers.NullStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;

/**
 <!-- globalinfo-start -->
 * Implements stochastic gradient descent for learning a linear binary class SVM or binary class logistic regression on text data. Operates directly on String attributes.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -F
 *  Set the loss function to minimize. 0 = hinge loss (SVM), 1 = log loss (logistic regression)
 *  (default = 0)</pre>
 * 
 * <pre> -L
 *  The learning rate (default = 0.01).</pre>
 * 
 * <pre> -R &lt;double&gt;
 *  The lambda regularization constant (default = 0.0001)</pre>
 * 
 * <pre> -E &lt;integer&gt;
 *  The number of epochs to perform (batch learning only, default = 500)</pre>
 * 
 * <pre> -W
 *  Use word frequencies instead of binary bag of words.</pre>
 * 
 * <pre> -P &lt;# instances&gt;
 *  How often to prune the dictionary of low frequency words (default = 0, i.e. don't prune)</pre>
 * 
 * <pre> -M &lt;double&gt;
 *  Minimum word frequency. Words with less than this frequence are ignored.
 *  If periodic pruning is turned on then this is also used to determine which
 *  words to remove from the dictionary (default = 3).</pre>
 * 
 * <pre> -norm &lt;num&gt;
 *  Specify the norm that each instance must have (default 1.0)</pre>
 * 
 * <pre> -lnorm &lt;num&gt;
 *  Specify L-norm to use (default 2.0)</pre>
 * 
 * <pre> -lowercase
 *  Convert all tokens to lowercase before adding to the dictionary.</pre>
 * 
 * <pre> -S
 *  Ignore words that are in the stoplist.</pre>
 * 
 * <pre> -stopwords &lt;file&gt;
 *  A file containing stopwords to override the default ones.
 *  Using this option automatically sets the flag ('-S') to use the
 *  stoplist if the file exists.
 *  Format: one stopword per line, lines starting with '#'
 *  are interpreted as comments and ignored.</pre>
 * 
 * <pre> -tokenizer &lt;spec&gt;
 *  The tokenizing algorihtm (classname plus parameters) to use.
 *  (default: weka.core.tokenizers.WordTokenizer)</pre>
 * 
 * <pre> -stemmer &lt;spec&gt;
 *  The stemmering algorihtm (classname plus parameters) to use.</pre>
 * 
 <!-- options-end -->
 *
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @author Eibe Frank (eibe{[at]}cs{[dot]}waikato{[dot]}ac{[dot]}nz)
 *
 */
public class SGDText extends RandomizableClassifier 
  implements UpdateableClassifier, WeightedInstancesHandler {
  
  /** For serialization */
  private static final long serialVersionUID = 7200171484002029584L;

  private static class Count implements Serializable {
    
    /**
     * For serialization
     */
    private static final long serialVersionUID = 2104201532017340967L;
    
    public double m_count;
    
    public double m_weight;
    
    public Count(double c) {
      m_count = c;
    }    
  }
  
  /**
   * The number of training instances at which to periodically prune the dictionary
   * of min frequency words. Empty or null string indicates don't prune
   */
  protected int m_periodicP = 0;
  
  /** Only consider dictionary words (features) that occur at least this many times */
  protected double m_minWordP = 3;
  
  /** Use word frequencies rather than bag-of-words if true */
  protected boolean m_wordFrequencies = false;
  
  /** The length that each document vector should have in the end */
  protected double m_norm = 1.0;
  
  /** The L-norm to use */
  protected double m_lnorm = 2.0;
  
  /** The dictionary (and term weights) */
  protected LinkedHashMap<String, Count> m_dictionary;
  
  /** Default (rainbow) stopwords */
  protected transient Stopwords m_stopwords;
  
  /** 
   * a file containing stopwords for using others than the default Rainbow 
   * ones.
   */
  protected File m_stopwordsFile = new File(System.getProperty("user.dir"));
  
  /** The tokenizer to use */
  protected Tokenizer m_tokenizer = new WordTokenizer();
  
  /** Whether or not to convert all tokens to lowercase */
  protected boolean m_lowercaseTokens;
  
  /** The stemming algorithm. */
  protected Stemmer m_stemmer = new NullStemmer();
  
  /** Whether or not to use a stop list */
  protected boolean m_useStopList; 
  
  /** The regularization parameter */
  protected double m_lambda = 0.0001;
  
  /** The learning rate */
  protected double m_learningRate = 0.01;
  
  /** Holds the current iteration number */
  protected double m_t;
  
  /** Holds the bias term */
  protected double m_bias;
  
  /** The number of training instances */
  protected double m_numInstances;
  
  /** The header of the training data */
  protected Instances m_data;
  
  /**
   *  The number of epochs to perform (batch learning). Total iterations is
   *  m_epochs * num instances 
   */
  protected int m_epochs = 500;
  
  /** 
   * Holds the current document vector (LinkedHashMap is more efficient
   * when iterating over EntrySet than HashMap) 
   */
  protected transient LinkedHashMap<String, Count> m_inputVector;
  
  /** the hinge loss function. */
  public static final int HINGE = 0;
  
  /** the log loss function. */
  public static final int LOGLOSS = 1;
  
  /** The current loss function to minimize */
  protected int m_loss = HINGE;
  
  /** Loss functions to choose from */
  public static final Tag [] TAGS_SELECTION = {
    new Tag(HINGE, "Hinge loss (SVM)"),
    new Tag(LOGLOSS, "Log loss (logistic regression)")    
  };
  
  protected double dloss(double z) {
    if (m_loss == HINGE) {
      return (z < 1) ? 1 : 0;
    } else {
      // log loss
      if (z < 0) {
        return 1.0 / (Math.exp(z) + 1.0);  
      } else {
        double t = Math.exp(-z);
        return t / (t + 1);
      }
    }    
  }
    
  /**
   * Returns default capabilities of the classifier.
   *
   * @return      the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    //attributes    
    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);
    
    result.enable(Capability.BINARY_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    
    // instances
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  
  /**
   * the stemming algorithm to use, null means no stemming at all (i.e., the
   * NullStemmer is used).
   *
   * @param value     the configured stemming algorithm, or null
   * @see             NullStemmer
   */
  public void setStemmer(Stemmer value) {
    if (value != null)
      m_stemmer = value;
    else
      m_stemmer = new NullStemmer();
  }

  /**
   * Returns the current stemming algorithm, null if none is used.
   *
   * @return          the current stemming algorithm, null if none set
   */
  public Stemmer getStemmer() {
    return m_stemmer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String stemmerTipText() {
    return "The stemming algorithm to use on the words.";
  }
  
  /**
   * the tokenizer algorithm to use.
   *
   * @param value     the configured tokenizing algorithm
   */
  public void setTokenizer(Tokenizer value) {
    m_tokenizer = value;
  }

  /**
   * Returns the current tokenizer algorithm.
   *
   * @return          the current tokenizer algorithm
   */
  public Tokenizer getTokenizer() {
    return m_tokenizer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String tokenizerTipText() {
    return "The tokenizing algorithm to use on the strings.";
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String useWordFrequenciesTipText() {
    return "Use word frequencies rather than binary "
      + "bag of words representation";
  }
  
  /**
   * Set whether to use word frequencies rather than binary
   * bag of words representation.
   * 
   * @param u true if word frequencies are to be used.
   */
  public void setUseWordFrequencies(boolean u) {
    m_wordFrequencies = u;
  }
  
  /**
   * Get whether to use word frequencies rather than binary
   * bag of words representation.
   * 
   * @param u true if word frequencies are to be used.
   */
  public boolean getUseWordFrequencies() {
    return m_wordFrequencies;
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String lowercaseTokensTipText() {
    return "Whether to convert all tokens to lowercase";
  }
  
  /**
   * Set whether to convert all tokens to lowercase
   * 
   * @param l true if all tokens are to be converted to
   * lowercase
   */
  public void setLowercaseTokens(boolean l) {
    m_lowercaseTokens = l;
  }
  
  /**
   * Get whether to convert all tokens to lowercase
   * 
   * @return true true if all tokens are to be converted to
   * lowercase
   */
  public boolean getLowercaseTokens() {
    return m_lowercaseTokens;
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String useStopListTipText() {
    return "If true, ignores all words that are on the stoplist.";
  }
  
  /**
   * Set whether to ignore all words that are on the stoplist. 
   * 
   * @param u true to ignore all words on the stoplist.
   */
  public void setUseStopList(boolean u) {
    m_useStopList = u;
  }
  
  /**
   * Get whether to ignore all words that are on the stoplist. 
   * 
   * @return true to ignore all words on the stoplist.
   */
  public boolean getUseStopList() {
    return m_useStopList;
  }
  
  /**
   * sets the file containing the stopwords, null or a directory unset the
   * stopwords. If the file exists, it automatically turns on the flag to
   * use the stoplist.
   *
   * @param value     the file containing the stopwords
   */
  public void setStopwords(File value) {
    if (value == null)
      value = new File(System.getProperty("user.dir"));

    m_stopwordsFile = value;
    if (value.exists() && value.isFile())
      setUseStopList(true);
  }

  /**
   * returns the file used for obtaining the stopwords, if the file represents
   * a directory then the default ones are used.
   *
   * @return          the file containing the stopwords
   */
  public File getStopwords() {
    return m_stopwordsFile;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String stopwordsTipText() {
    return "The file containing the stopwords (if this is a directory then the default ones are used).";
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String periodicPruningTipText() {
    return "How often (number of instances) to prune " +
                "the dictionary of low frequency terms. " +
                "0 means don't prune. Setting a positive " +
                "integer n means prune after every n instances";
  }
  
  /**
   * Set how often to prune the dictionary
   * 
   * @param p how often to prune
   */
  public void setPeriodicPruning(int p) {
    m_periodicP = p;
  }
  
  /**
   * Get how often to prune the dictionary
   * 
   * @return how often to prune the dictionary
   */
  public int getPeriodicPruning() {
    return m_periodicP;
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String minWordFrequencyTipText() {
    return "Ignore any words that don't occur at least "
      + "min frequency times in the training data. If periodic "
      + "pruning is turned on, then the dictionary is pruned " 
      + "according to this value";
      	
  }
  
  /**
   * Set the minimum word frequency. Words that don't occur
   * at least min freq times are ignored when updating weights. 
   * If periodic pruning is turned on, then min frequency is used
   * when removing words from the dictionary.
   * 
   * @param minFreq the minimum word frequency to use
   */
  public void setMinWordFrequency(double minFreq) {
    m_minWordP = minFreq;
  }
  
  /**
   * Get the minimum word frequency. Words that don't occur
   * at least min freq times are ignored when updating weights. 
   * If periodic pruning is turned on, then min frequency is used
   * when removing words from the dictionary.
   * 
   * @param return the minimum word frequency to use
   */
  public double getMinWordFrequency() {
    return m_minWordP;
  }  
  
  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String normTipText() { 
    return "The norm of the instances after normalization.";
  }
  
  /**
   * Get the instance's Norm.
   *
   * @return the Norm
   */
  public double getNorm() {
    return m_norm;
  }
  
  /**
   * Set the norm of the instances
   *
   * @param newNorm the norm to wich the instances must be set
   */
  public void setNorm(double newNorm) {
    m_norm = newNorm;
  }
  
  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String LNormTipText() { 
    return "The LNorm to use for document length normalization.";
  }
  
  /**
   * Get the L Norm used.
   *
   * @return the L-norm used
   */
  public double getLNorm() {
    return m_lnorm;
  }
  
  /**
   * Set the L-norm to used
   *
   * @param newLNorm the L-norm
   */
  public void setLNorm(double newLNorm) {
    m_lnorm = newLNorm;
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String lambdaTipText() {
    return "The regularization constant. (default = 0.0001)";
  }
  
  /**
   * Set the value of lambda to use
   * 
   * @param lambda the value of lambda to use
   */
  public void setLambda(double lambda) {
    m_lambda = lambda;
  }
  
  /**
   * Get the current value of lambda
   * 
   * @return the current value of lambda
   */
  public double getLambda() {
    return m_lambda;
  }
  
  /**
   * Set the learning rate.
   * 
   * @param lr the learning rate to use.
   */
  public void setLearningRate(double lr) {
    m_learningRate = lr;
  }
  
  /**
   * Get the learning rate.
   * 
   * @return the learning rate
   */
  public double getLearningRate() {
    return m_learningRate;
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String learningRateTipText() {
    return "The learning rate.";
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String epochsTipText() {
    return "The number of epochs to perform (batch learning). " +
                "The total number of iterations is epochs * num" +
                " instances.";
  }
  
  /**
   * Set the number of epochs to use
   * 
   * @param e the number of epochs to use
   */
  public void setEpochs(int e) {
    m_epochs = e;
  }
  
  /**
   * Get current number of epochs
   * 
   * @return the current number of epochs
   */
  public int getEpochs() {
    return m_epochs;
  }
  
  /**
   * Set the loss function to use.
   * 
   * @param function the loss function to use.
   */
  public void setLossFunction(SelectedTag function) {
    if (function.getTags() == TAGS_SELECTION) {
      m_loss = function.getSelectedTag().getID();
    }
  }
  
  /**
   * Get the current loss function.
   * 
   * @return the current loss function.
   */
  public SelectedTag getLossFunction() {
    return new SelectedTag(m_loss, TAGS_SELECTION);
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String lossFunctionTipText() {
    return "The loss function to use. Hinge loss (SVM), " +
                "log loss (logistic regression) or " +
                "squared loss (regression).";
  }
  
  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration<Option> listOptions() {

    Vector<Option> newVector = new Vector<Option>();
    newVector.add(new Option("\tSet the loss function to minimize. 0 = " +
        "hinge loss (SVM), 1 = log loss (logistic regression)\n\t" +
        "(default = 0)", "F", 1, "-F"));
    newVector.add(new Option("\tThe learning rate (default = 0.01).", "L", 1, "-L"));
    newVector.add(new Option("\tThe lambda regularization constant " +
                "(default = 0.0001)",
                "R", 1, "-R <double>"));
    newVector.add(new Option("\tThe number of epochs to perform (" +
                "batch learning only, default = 500)", "E", 1,
                "-E <integer>"));
    newVector.add(new Option("\tUse word frequencies instead of " +
    		"binary bag of words.", "W", 0, 
    		"-W"));
    newVector.add(new Option("\tHow often to prune the dictionary " +
    		"of low frequency words (default = 0, i.e. don't prune)", 
    		"P", 1, "-P <# instances>"));
    newVector.add(new Option("\tMinimum word frequency. Words with less " +
    		"than this frequence are ignored.\n\tIf periodic pruning " +
    		"is turned on then this is also used to determine which\n\t" +
    		"words to remove from the dictionary (default = 3).",
    		"M", 1, "-M <double>"));
    newVector.addElement(new Option(
        "\tSpecify the norm that each instance must have (default 1.0)",
        "norm", 1, "-norm <num>"));
    newVector.addElement(new Option(
        "\tSpecify L-norm to use (default 2.0)",
        "lnorm", 1, "-lnorm <num>"));
    newVector.addElement(new Option("\tConvert all tokens to lowercase " +
    		"before adding to the dictionary.",
        "lowercase", 0, "-lowercase"));
    newVector.addElement(new Option(
        "\tIgnore words that are in the stoplist.",
        "S", 0, "-S"));
    newVector.addElement(new Option(
        "\tA file containing stopwords to override the default ones.\n"
        + "\tUsing this option automatically sets the flag ('-S') to use the\n"
        + "\tstoplist if the file exists.\n"
        + "\tFormat: one stopword per line, lines starting with '#'\n"
        + "\tare interpreted as comments and ignored.",
        "stopwords", 1, "-stopwords <file>"));
    newVector.addElement(new Option(
        "\tThe tokenizing algorihtm (classname plus parameters) to use.\n"
        + "\t(default: " + WordTokenizer.class.getName() + ")",
        "tokenizer", 1, "-tokenizer <spec>"));
    newVector.addElement(new Option(
        "\tThe stemmering algorihtm (classname plus parameters) to use.",
        "stemmer", 1, "-stemmer <spec>"));
    
    return newVector.elements();
  }
  
  /**
   * Parses a given list of options. <p/>
   * 
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -F
   *  Set the loss function to minimize. 0 = hinge loss (SVM), 1 = log loss (logistic regression)
   *  (default = 0)</pre>
   * 
   * <pre> -L
   *  The learning rate (default = 0.01).</pre>
   * 
   * <pre> -R &lt;double&gt;
   *  The lambda regularization constant (default = 0.0001)</pre>
   * 
   * <pre> -E &lt;integer&gt;
   *  The number of epochs to perform (batch learning only, default = 500)</pre>
   * 
   * <pre> -W
   *  Use word frequencies instead of binary bag of words.</pre>
   * 
   * <pre> -P &lt;# instances&gt;
   *  How often to prune the dictionary of low frequency words (default = 0, i.e. don't prune)</pre>
   * 
   * <pre> -M &lt;double&gt;
   *  Minimum word frequency. Words with less than this frequence are ignored.
   *  If periodic pruning is turned on then this is also used to determine which
   *  words to remove from the dictionary (default = 3).</pre>
   * 
   * <pre> -norm &lt;num&gt;
   *  Specify the norm that each instance must have (default 1.0)</pre>
   * 
   * <pre> -lnorm &lt;num&gt;
   *  Specify L-norm to use (default 2.0)</pre>
   * 
   * <pre> -lowercase
   *  Convert all tokens to lowercase before adding to the dictionary.</pre>
   * 
   * <pre> -S
   *  Ignore words that are in the stoplist.</pre>
   * 
   * <pre> -stopwords &lt;file&gt;
   *  A file containing stopwords to override the default ones.
   *  Using this option automatically sets the flag ('-S') to use the
   *  stoplist if the file exists.
   *  Format: one stopword per line, lines starting with '#'
   *  are interpreted as comments and ignored.</pre>
   * 
   * <pre> -tokenizer &lt;spec&gt;
   *  The tokenizing algorihtm (classname plus parameters) to use.
   *  (default: weka.core.tokenizers.WordTokenizer)</pre>
   * 
   * <pre> -stemmer &lt;spec&gt;
   *  The stemmering algorihtm (classname plus parameters) to use.</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    reset();
    
    super.setOptions(options);
    
    String lossString = Utils.getOption('F', options);
    if (lossString.length() != 0) {
      setLossFunction(new SelectedTag(Integer.parseInt(lossString), 
          TAGS_SELECTION));
    }
    
    String lambdaString = Utils.getOption('R', options);
    if (lambdaString.length() > 0) {
      setLambda(Double.parseDouble(lambdaString));
    }
    
    String learningRateString = Utils.getOption('L', options);
    if (learningRateString.length() > 0) {
      setLearningRate(Double.parseDouble(learningRateString));
    }
    
    String epochsString = Utils.getOption("E", options);
    if (epochsString.length() > 0) {
      setEpochs(Integer.parseInt(epochsString));
    }        
    
    setUseWordFrequencies(Utils.getFlag("W", options));
    
    String pruneFreqS = Utils.getOption("P", options);
    if (pruneFreqS.length() > 0) {
      setPeriodicPruning(Integer.parseInt(pruneFreqS));
    }
    String minFreq = Utils.getOption("M", options);
    if (minFreq.length() > 0) {
      setMinWordFrequency(Double.parseDouble(minFreq));
    }
    
    String normFreqS = Utils.getOption("norm", options);
    if (normFreqS.length() > 0) {
      setNorm(Double.parseDouble(normFreqS));
    }
    String lnormFreqS = Utils.getOption("lnorm", options);
    if (lnormFreqS.length() > 0) {
      setLNorm(Double.parseDouble(lnormFreqS));
    }
    
    setLowercaseTokens(Utils.getFlag("lowercase", options));
    setUseStopList(Utils.getFlag("S", options));
    
    String stopwordsS = Utils.getOption("stopwords", options);
    if (stopwordsS.length() > 0) {
      setStopwords(new File(stopwordsS));
    } else {
      setStopwords(null);
    }
    
    String tokenizerString = Utils.getOption("tokenizer", options);
    if (tokenizerString.length() == 0) {
      setTokenizer(new WordTokenizer());
    } else {
      String[] tokenizerSpec = Utils.splitOptions(tokenizerString);
      if (tokenizerSpec.length == 0)
        throw new Exception("Invalid tokenizer specification string");
      String tokenizerName = tokenizerSpec[0];
      tokenizerSpec[0] = "";
      Tokenizer tokenizer = (Tokenizer) Class.forName(tokenizerName).newInstance();
      if (tokenizer instanceof OptionHandler)
        ((OptionHandler) tokenizer).setOptions(tokenizerSpec);
      setTokenizer(tokenizer);
    }
    
    String stemmerString = Utils.getOption("stemmer", options);
    if (stemmerString.length() == 0) {
      setStemmer(null);
    } else {
      String[] stemmerSpec = Utils.splitOptions(stemmerString);
      if (stemmerSpec.length == 0)
        throw new Exception("Invalid stemmer specification string");
      String stemmerName = stemmerSpec[0];
      stemmerSpec[0] = "";
      Stemmer stemmer = (Stemmer) Class.forName(stemmerName).newInstance();
      if (stemmer instanceof OptionHandler)
        ((OptionHandler) stemmer).setOptions(stemmerSpec);
      setStemmer(stemmer);
    }
  }
  
  /**
   * Gets the current settings of the classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    ArrayList<String> options = new ArrayList<String>();
    
    options.add("-F"); options.add("" + getLossFunction().getSelectedTag().getID());
    options.add("-L"); options.add("" + getLearningRate());
    options.add("-R"); options.add("" + getLambda());
    options.add("-E"); options.add("" + getEpochs());  
    if (getUseWordFrequencies()) {
      options.add("-W");
    }
    options.add("-P"); options.add("" + getPeriodicPruning());
    options.add("-M"); options.add("" + getMinWordFrequency());
    options.add("-norm"); options.add("" + getNorm());
    options.add("-lnorm"); options.add("" + getLNorm());
    if (getLowercaseTokens()) {
      options.add("-lowercase");
    }
    if (getUseStopList()) {
      options.add("-S");
    }
    if (!getStopwords().isDirectory()) {
      options.add("-stopwords"); options.add(getStopwords().getAbsolutePath());
    }
    
    options.add("-tokenizer");
    String spec = getTokenizer().getClass().getName();
    if (getTokenizer() instanceof OptionHandler)
      spec += " " + Utils.joinOptions(
          ((OptionHandler) getTokenizer()).getOptions());
    options.add(spec.trim());
    
    return options.toArray(new String[1]);
  }
  
  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Implements stochastic gradient descent for learning" +
                " a linear binary class SVM or binary class" +
                " logistic regression on text data. Operates directly on String " +
                "attributes.";
  }
  
  /**
   * Reset the classifier.
   */
  public void reset() {
    m_t = 1;
    m_dictionary = null;
  }

  /**
   * Method for building the classifier.
   * 
   * @param data the set of training instances.
   * @throws Exception if the classifier can't be built successfully.
   */
  public void buildClassifier(Instances data) throws Exception {
    reset();
    
    // can classifier handle the data?
    getCapabilities().testWithFail(data);
    
    m_dictionary = new LinkedHashMap<String, Count>(10000);
    
    m_numInstances = data.numInstances();
    m_data = new Instances(data, 0);
    data = new Instances(data);
    
    if (data.numInstances() > 0) {
      data.randomize(new Random(getSeed()));
      train(data);
    }    
  }
  
  protected void train(Instances data) throws Exception {
    for (int e = 0; e < m_epochs; e++) {
      for (int i = 0; i < data.numInstances(); i++) {
        if (e == 0) {
          updateClassifier(data.instance(i), true);
        } else {
          updateClassifier(data.instance(i), false);
        }
      }
    }
  }

  /**
   * Updates the classifier with the given instance.
   *
   * @param instance the new training instance to include in the model 
   * @exception Exception if the instance could not be incorporated in
   * the model.
   */
  public void updateClassifier(Instance instance) throws Exception {
    updateClassifier(instance, true);
  }
  
  protected void updateClassifier(Instance instance, boolean updateDictionary)
    throws Exception {
    
    if (!instance.classIsMissing()) {
      
      // tokenize
      tokenizeInstance(instance, updateDictionary);
      
      // --- 
      double wx = dotProd(m_inputVector);
      double y = (instance.classValue() == 0) ? -1 : 1;
      double z = y * (wx + m_bias);
      
      // Compute multiplier for weight decay
      double multiplier = 1.0;
      if (m_numInstances == 0) {
        multiplier = 1.0 - (m_learningRate * m_lambda) / m_t;
      } else {
        multiplier = 1.0 - (m_learningRate * m_lambda) / m_numInstances;
      }      
      for (Count c : m_dictionary.values()) {
        c.m_weight *= multiplier;
      }
      
      // Only need to do the following if the loss is non-zero
      if (m_loss != HINGE || (z < 1)) {
        // Compute Factor for updates
        double factor = m_learningRate * y * dloss(z);
        
        // Update coefficients for attributes
        for (Map.Entry<String, Count> feature : m_inputVector.entrySet()) {
          String word = feature.getKey();
          double value = (m_wordFrequencies) ? feature.getValue().m_count : 1;
          
          Count c = m_dictionary.get(word);
          if (c != null) {
            c.m_weight += factor * value;
          }
        }
        
        // update the bias
        m_bias += factor;
      }
      
      m_t++;
    }
  }
  
  protected void tokenizeInstance(Instance instance, boolean updateDictionary) {
    if (m_inputVector == null) {
      m_inputVector = new LinkedHashMap<String, Count>();
    } else {
      m_inputVector.clear();
    }
    
    if (m_useStopList && m_stopwords == null) {
      m_stopwords = new Stopwords();
      try {
        if (getStopwords().exists() && !getStopwords().isDirectory()) {
          m_stopwords.read(getStopwords());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (instance.attribute(i).isString() && !instance.isMissing(i)) {
        m_tokenizer.tokenize(instance.stringValue(i));
        
        while (m_tokenizer.hasMoreElements()) {
          String word = ((String)m_tokenizer.nextElement()).intern();
          if (m_lowercaseTokens) {
            word = word.toLowerCase().intern();              
          }
          
          word = m_stemmer.stem(word);
          
          if (m_useStopList) {
            if (m_stopwords.is(word)) {
              continue;
            }
          }
          
          Count docCount = m_inputVector.get(word);
          if (docCount == null) {
            m_inputVector.put(word, new Count(instance.weight()));
          } else {
            docCount.m_count += instance.weight();
          }
          
          if (updateDictionary) {
            Count count = m_dictionary.get(word);
            if (count == null) {
              m_dictionary.put(word, new Count(instance.weight()));
            } else {
              count.m_count += instance.weight();
            }
          }
        }
      }
    }
    
    if (updateDictionary) {
      pruneDictionary();
    }
  }
  
  protected void pruneDictionary() {
    if (m_periodicP <= 0 || m_t % m_periodicP > 0) {
      return;
    }
    
    Iterator<Map.Entry<String, Count>> entries = m_dictionary.entrySet().iterator();
    while (entries.hasNext()) { 
      Map.Entry<String, Count> entry = entries.next();
      if (entry.getValue().m_count < m_minWordP) {
        entries.remove();
      }
    }
  }
  
  public double[] distributionForInstance(Instance inst) throws Exception {
    double[] result = new double[2];
    
    tokenizeInstance(inst, false);
    double wx = dotProd(m_inputVector);
    double z = (wx + m_bias);
    
    if (z <= 0) {
      if (m_loss == LOGLOSS) {
        result[0] = 1.0 / (1.0 + Math.exp(z));
        result[1] = 1.0 - result[0];
      } else {
        result[0] = 1;
      }
    } else {
      if (m_loss == LOGLOSS) {
        result[1] = 1.0 / (1.0 + Math.exp(-z));
        result[0] = 1.0 - result[1];
      } else {
        result[1] = 1;
      }
    }
    
    return result;
  }
  
  protected double dotProd(Map<String, Count> document) {    
    double result = 0;
    
    // document normalization
    double iNorm = 0;
    double fv = 0;
    for (Count c : document.values()) {
      // word counts or bag-of-words?
      fv = (m_wordFrequencies) ? c.m_count : 1.0;
      iNorm += Math.pow(Math.abs(fv), m_lnorm);
    }
    iNorm = Math.pow(iNorm, 1.0 / m_lnorm);
    
    for (Map.Entry<String, Count> feature : document.entrySet()) {
      String word = feature.getKey();
      double freq = (feature.getValue().m_count / iNorm * m_norm);
      
      Count weight = m_dictionary.get(word);
      
      if (weight != null && weight.m_count >= m_minWordP) {
        result += freq * weight.m_weight;
      }      
    }
    
    return result;
  }
  
  public String toString() {
    if (m_dictionary == null) {
      return "SGDText: No model built yet.\n";
    }
        
    StringBuffer buff = new StringBuffer();
    buff.append("SGDText:\n\n");
    buff.append("Loss function: ");
    if (m_loss == HINGE) {
      buff.append("Hinge loss (SVM)\n\n");
    } else {
      buff.append("Log loss (logistic regression)\n\n");
    }
    
    buff.append("Dictionary size: " + m_dictionary.size() + "\n\n");
    
    buff.append(m_data.classAttribute().name() + " = \n\n");
    int printed = 0;
    
    Iterator<Map.Entry<String, Count>> entries = m_dictionary.entrySet().iterator();
    while (entries.hasNext()) { 
      Map.Entry<String, Count> entry = entries.next();
      
      if (printed > 0) {
        buff.append(" + ");
      } else {
        buff.append("   ");
      }
      
      buff.append(Utils.doubleToString(entry.getValue().m_weight, 12, 4)
          + " " + entry.getKey() + "\n");
      printed++;
    }
      
    if (m_bias > 0) {
      buff.append(" + " + Utils.doubleToString(m_bias, 12, 4));
    } else {
      buff.append(" - " + Utils.doubleToString(-m_bias, 12, 4));
    }
    
    return buff.toString();
  }
  
  /**
   * Returns the revision string.
   * 
   * @return            the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 7787 $");
  }
  
  /**
   * Main method for testing this class.
   */
  public static void main(String[] args) {
    runClassifier(new SGDText(), args);
  }
}

