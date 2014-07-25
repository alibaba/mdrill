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
 *    AggregateableEvaluation.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers;

import weka.core.FastVector;
import weka.core.Instances;

/**
 * Subclass of Evaluation that provides a method for aggregating the
 * results stored in another Evaluation object.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7613 $
 */
public class AggregateableEvaluation extends Evaluation {

  /**
   * For serialization
   */
  private static final long serialVersionUID = 8734675926526110924L;

  /**
   * Constructs a new AggregateableEvaluation object
   * 
   * @param data the Instances to use
   * @throws Exception if a problem occurs
   */
  public AggregateableEvaluation(Instances data) throws Exception {
    super(data);
  }
  
  /**
   * Constructs a new AggregateableEvaluation object
   * 
   * @param data the Instances to use
   * @param costMatrix the cost matrix to use
   * @throws Exception if a problem occurs
   */
  public AggregateableEvaluation(Instances data, CostMatrix costMatrix) 
    throws Exception {
    super(data, costMatrix);
  }

  /**
   * Constructs a new AggregateableEvaluation object based
   * on an Evaluation object
   * 
   * @param evaluation the Evaluation object to use
   */
  public AggregateableEvaluation(Evaluation eval) throws Exception {
    super(eval.m_Header, eval.m_CostMatrix);
    
    m_NoPriors = eval.m_NoPriors;
    m_NumTrainClassVals = eval.m_NumTrainClassVals;
    m_TrainClassVals = eval.m_TrainClassVals;
    m_TrainClassWeights = eval.m_TrainClassWeights;
    m_PriorEstimator = eval.m_PriorEstimator;
    m_MinTarget = eval.m_MinTarget;
    m_MaxTarget = eval.m_MaxTarget;
    m_ClassPriorsSum = eval.m_ClassPriorsSum;
    m_ClassPriors = eval.m_ClassPriors;
  }
  
  /**
   * Adds the statistics encapsulated in the supplied
   * Evaluation object into this one. Does not perform
   * any checks for compatibility between the supplied
   * Evaluation object and this one.
   * 
   * @param evaluation the evaluation object to aggregate
   */
  public void aggregate(Evaluation evaluation) {
    m_Incorrect += evaluation.incorrect();
    m_Correct += evaluation.correct();
    m_Unclassified += evaluation.unclassified();
    m_MissingClass += evaluation.m_MissingClass;
    m_WithClass += evaluation.m_WithClass;
    
    if (evaluation.m_ConfusionMatrix != null) {
      double [][] newMatrix = evaluation.confusionMatrix();
      if (newMatrix != null) {
        for(int i = 0; i < m_ConfusionMatrix.length; i++) {
          for(int j = 0; j < m_ConfusionMatrix[i].length; j++) {
            m_ConfusionMatrix[i][j] += newMatrix[i][j];
          }
        }
      }
    }
    double [] newClassPriors = evaluation.m_ClassPriors;
    if (newClassPriors != null) {
      for(int i = 0; i < this.m_ClassPriors.length; i++) {
        m_ClassPriors[i] = newClassPriors[i];
      }
    }
    m_ClassPriorsSum = evaluation.m_ClassPriorsSum;
    m_TotalCost += evaluation.totalCost();
    m_SumErr += evaluation.m_SumErr;
    m_SumAbsErr += evaluation.m_SumAbsErr;
    m_SumSqrErr += evaluation.m_SumSqrErr;
    m_SumClass += evaluation.m_SumClass;
    m_SumSqrClass += evaluation.m_SumSqrClass;
    m_SumPredicted += evaluation.m_SumPredicted;
    m_SumSqrPredicted += evaluation.m_SumSqrPredicted;
    m_SumClassPredicted += evaluation.m_SumClassPredicted;
    m_SumPriorAbsErr += evaluation.m_SumPriorAbsErr;
    m_SumPriorSqrErr += evaluation.m_SumPriorSqrErr;
    m_SumKBInfo += evaluation.m_SumKBInfo;
    double [] newMarginCounts = evaluation.m_MarginCounts;
    if (newMarginCounts != null) {
      for(int i = 0; i < m_MarginCounts.length; i++) {
        m_MarginCounts[i] += newMarginCounts[i];
      }
    }
    m_SumPriorEntropy += evaluation.m_SumPriorEntropy;
    m_SumSchemeEntropy += evaluation.m_SumSchemeEntropy;
    m_TotalSizeOfRegions += evaluation.m_TotalSizeOfRegions;
    m_TotalCoverage += evaluation.m_TotalCoverage;
    
    FastVector predsToAdd = evaluation.m_Predictions;
    if (predsToAdd != null ) {
      if (m_Predictions == null) {
        m_Predictions = new FastVector();
      }
      for (int i = 0; i < predsToAdd.size(); i++) {
        m_Predictions.addElement(predsToAdd.elementAt(i));
      }
    }
  }
}
