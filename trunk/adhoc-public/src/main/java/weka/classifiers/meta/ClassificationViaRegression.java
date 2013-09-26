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
 *    ClassificationViaRegression.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.meta;

import weka.classifiers.Classifier;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;

/**
 <!-- globalinfo-start -->
 * Class for doing classification using regression methods. Class is binarized and one regression model is built for each class value. For more information, see, for example<br/>
 * <br/>
 * E. Frank, Y. Wang, S. Inglis, G. Holmes, I.H. Witten (1998). Using model trees for classification. Machine Learning. 32(1):63-76.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Frank1998,
 *    author = {E. Frank and Y. Wang and S. Inglis and G. Holmes and I.H. Witten},
 *    journal = {Machine Learning},
 *    number = {1},
 *    pages = {63-76},
 *    title = {Using model trees for classification},
 *    volume = {32},
 *    year = {1998}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.trees.M5P)</pre>
 * 
 * <pre> 
 * Options specific to classifier weka.classifiers.trees.M5P:
 * </pre>
 * 
 * <pre> -N
 *  Use unpruned tree/rules</pre>
 * 
 * <pre> -U
 *  Use unsmoothed predictions</pre>
 * 
 * <pre> -R
 *  Build regression tree/rule rather than a model tree/rule</pre>
 * 
 * <pre> -M &lt;minimum number of instances&gt;
 *  Set minimum number of instances per leaf
 *  (default 4)</pre>
 * 
 * <pre> -L
 *  Save instances at the nodes in
 *  the tree (for visualization purposes)</pre>
 * 
 <!-- options-end -->
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 6986 $ 
*/
public class ClassificationViaRegression 
  extends SingleClassifierEnhancer
  implements TechnicalInformationHandler {

  /** for serialization */
  static final long serialVersionUID = 4500023123618669859L;
  
  /** The classifiers. (One for each class.) */
  private Classifier[] m_Classifiers;

  /** The filters used to transform the class. */
  private MakeIndicator[] m_ClassFilters;

  /**
   * Default constructor.
   */
  public ClassificationViaRegression() {
    
    m_Classifier = new weka.classifiers.trees.M5P();
  }
    
  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
 
    return "Class for doing classification using regression methods. Class is "
      + "binarized and one regression model is built for each class value. For more "
      + "information, see, for example\n\n"
      + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing 
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;
    
    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "E. Frank and Y. Wang and S. Inglis and G. Holmes and I.H. Witten");
    result.setValue(Field.YEAR, "1998");
    result.setValue(Field.TITLE, "Using model trees for classification");
    result.setValue(Field.JOURNAL, "Machine Learning");
    result.setValue(Field.VOLUME, "32");
    result.setValue(Field.NUMBER, "1");
    result.setValue(Field.PAGES, "63-76");
    
    return result;
  }

  /**
   * String describing default classifier.
   * 
   * @return the default classifier classname
   */
  protected String defaultClassifierString() {
    
    return "weka.classifiers.trees.M5P";
  }

  /**
   * Returns default capabilities of the classifier.
   *
   * @return      the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();

    // class
    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capability.NOMINAL_CLASS);
    
    return result;
  }

  /**
   * Builds the classifiers.
   *
   * @param insts the training data.
   * @throws Exception if a classifier can't be built
   */
  public void buildClassifier(Instances insts) throws Exception {

    Instances newInsts;

    // can classifier handle the data?
    getCapabilities().testWithFail(insts);

    // remove instances with missing class
    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    m_Classifiers = AbstractClassifier.makeCopies(m_Classifier, insts.numClasses());
    m_ClassFilters = new MakeIndicator[insts.numClasses()];
    for (int i = 0; i < insts.numClasses(); i++) {
      m_ClassFilters[i] = new MakeIndicator();
      m_ClassFilters[i].setAttributeIndex("" + (insts.classIndex() + 1));
      m_ClassFilters[i].setValueIndex(i);
      m_ClassFilters[i].setNumeric(true);
      m_ClassFilters[i].setInputFormat(insts);
      newInsts = Filter.useFilter(insts, m_ClassFilters[i]);
      m_Classifiers[i].buildClassifier(newInsts);
    }
  }

  /**
   * Returns the distribution for an instance.
   *
   * @param inst the instance to get the distribution for
   * @return the computed distribution
   * @throws Exception if the distribution can't be computed successfully
   */
  public double[] distributionForInstance(Instance inst) throws Exception {
    
    double[] probs = new double[inst.numClasses()];
    Instance newInst;
    double sum = 0;

    for (int i = 0; i < inst.numClasses(); i++) {
      m_ClassFilters[i].input(inst);
      m_ClassFilters[i].batchFinished();
      newInst = m_ClassFilters[i].output();
      probs[i] = m_Classifiers[i].classifyInstance(newInst);
      if (probs[i] > 1) {
        probs[i] = 1;
      }
      if (probs[i] < 0){
	probs[i] = 0;
      }
      sum += probs[i];
    }
    if (sum != 0) {
      Utils.normalize(probs, sum);
    } 
    return probs;
  }

  /**
   * Prints the classifiers.
   * 
   * @return a string representation of the classifier
   */
  public String toString() {

    if (m_Classifiers == null) {
      return "Classification via Regression: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("Classification via Regression\n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append("Classifier for class with index " + i + ":\n\n");
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    return text.toString();
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 6986 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv the options for the learner
   */
  public static void main(String [] argv){
    runClassifier(new ClassificationViaRegression(), argv);
  }
}
