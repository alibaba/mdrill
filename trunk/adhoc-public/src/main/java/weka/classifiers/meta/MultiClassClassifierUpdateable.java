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
 *    MultiClassClassifierUpdateable.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.meta;

import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 <!-- globalinfo-start -->
 * A metaclassifier for handling multi-class datasets with 2-class classifiers. This classifier is also capable of applying error correcting output codes for increased accuracy. The base classifier must be an updateable classifier
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -M &lt;num&gt;
 *  Sets the method to use. Valid values are 0 (1-against-all),
 *  1 (random codes), 2 (exhaustive code), and 3 (1-against-1). (default 0)
 * </pre>
 * 
 * <pre> -R &lt;num&gt;
 *  Sets the multiplier when using random codes. (default 2.0)</pre>
 * 
 * <pre> -P
 *  Use pairwise coupling (only has an effect for 1-against1)</pre>
 * 
 * <pre> -S &lt;num&gt;
 *  Random number seed.
 *  (default 1)</pre>
 * 
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.functions.Logistic)</pre>
 * 
 * <pre> 
 * Options specific to classifier weka.classifiers.functions.Logistic:
 * </pre>
 * 
 * <pre> -D
 *  Turn on debugging output.</pre>
 * 
 * <pre> -R &lt;ridge&gt;
 *  Set the ridge in the log-likelihood.</pre>
 * 
 * <pre> -M &lt;number&gt;
 *  Set the maximum number of iterations (default -1, until convergence).</pre>
 * 
 <!-- options-end -->
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Len Trigg (len@reeltwo.com)
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * 
 * @version $Revision: 7801 $
 */
public class MultiClassClassifierUpdateable 
  extends MultiClassClassifier 
  implements OptionHandler, UpdateableClassifier {
  
  /** For serialization */
  private static final long serialVersionUID = -1619685269774366430L;  

  /**
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "A metaclassifier for handling multi-class datasets with 2-class "
      + "classifiers. This classifier is also capable of "
      + "applying error correcting output codes for increased accuracy. "
      + "The base classifier must be an updateable classifier";
  }

  public void buildClassifier(Instances insts) throws Exception {
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }
    
    if (!(m_Classifier instanceof UpdateableClassifier)) {
      throw new Exception("Base classifier must be updateable!");
    }
    
    super.buildClassifier(insts);                
  }
  
  /**
   * Updates the classifier with the given instance.
   *
   * @param instance the new training instance to include in the model 
   * @exception Exception if the instance could not be incorporated in
   * the model.
   */
  public void updateClassifier(Instance instance) throws Exception {
    if (!instance.classIsMissing()) {

      if (m_Classifiers.length == 1) {
        ((UpdateableClassifier)m_Classifiers[0]).updateClassifier(instance);
        return;
      }

      for (int i = 0; i < m_Classifiers.length; i++) {
        if (m_Classifiers[i] != null) {
          m_ClassFilters[i].input(instance);
          Instance converted = m_ClassFilters[i].output();
          if (converted != null) {
            converted.dataset().setClassIndex(m_ClassAttribute.index());
            ((UpdateableClassifier)m_Classifiers[i]).
            updateClassifier(converted);
            
            if (m_Method == METHOD_1_AGAINST_1) {
              m_SumOfWeights[i] += converted.weight();
            }
          }
        }
      }
    }
  }
  
  /**
   * Returns the distribution for an instance.
   *
   * @param inst the instance to get the distribution for
   * @return the distribution
   * @throws Exception if the distribution can't be computed successfully
   */
  public double[] distributionForInstance(Instance inst) throws Exception {
    
    double[] probs = new double[inst.numClasses()];
    if (m_Method == METHOD_1_AGAINST_1) {
      double[][] r = new double[inst.numClasses()][inst.numClasses()];
      double[][] n = new double[inst.numClasses()][inst.numClasses()];

      for(int i = 0; i < m_ClassFilters.length; i++) {
        if (m_Classifiers[i] != null && m_SumOfWeights[i] > 0) {
          Instance tempInst = (Instance)inst.copy(); 
          tempInst.setDataset(m_TwoClassDataset);
          double [] current = m_Classifiers[i].distributionForInstance(tempInst);  
          Range range = new Range(((RemoveWithValues)m_ClassFilters[i])
                                  .getNominalIndices());
          range.setUpper(m_ClassAttribute.numValues());
          int[] pair = range.getSelection();
          if (m_pairwiseCoupling && inst.numClasses() > 2) {
            r[pair[0]][pair[1]] = current[0];
            n[pair[0]][pair[1]] = m_SumOfWeights[i];
          } else {
            if (current[0] > current[1]) {
              probs[pair[0]] += 1.0;
            } else {
              probs[pair[1]] += 1.0;
            }
          }
        }
      }
      if (m_pairwiseCoupling && inst.numClasses() > 2) {
        return pairwiseCoupling(n, r);
      }
      Utils.normalize(probs);
    } else {    
      probs = super.distributionForInstance(inst);
    }
    
    if (probs.length == 1) {
      // ZeroR made the prediction
      return new double[m_ClassAttribute.numValues()];
    }
    
    return probs;
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 7801 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv the options
   */
  public static void main(String [] argv) {
    runClassifier(new MultiClassClassifierUpdateable(), argv);
  }
}

