/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Package Manager.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 * VersionRangePackageConstraint
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 */
package org.pentaho.packageManagement;


/**
 * A concrete implementation of PackgageConstraint that encapsulates
 * ranged version number constraints. Handles constraints of the
 * form (u.v.w < package < x.y.z) and (package < u.v.w OR package > x.y.z)
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 44030 $
 */
public class VersionRangePackageConstraint extends PackageConstraint {

  /** the lower bound */
  protected String m_lowerBound;
  
  /** the comparison for the lower bound */
  protected VersionPackageConstraint.VersionComparison m_lowerConstraint;
  
  /** the upper bound */
  protected String m_upperBound;
  
  /** the comparison for the upper bound */
  protected VersionPackageConstraint.VersionComparison m_upperConstraint;
  
  /** is false for version1 <= package <= version2 */
  protected boolean m_boundOr;

  /**
   * Constructor
   * 
   * @param p the package to base this constraint on
   */
  public VersionRangePackageConstraint(Package p) {
    setPackage(p);
  }
  
  /**
   * Set the range bounds and constraints.
   * 
   * @param bound1 the first bound
   * @param comp1 the first comparison
   * @param bound2 the second bound
   * @param comp2 the second comparison
   * @throws Exception if the range constraint is malformed
   */
  public void setRangeConstraint(String bound1,
      VersionPackageConstraint.VersionComparison comp1,
      String bound2,
      VersionPackageConstraint.VersionComparison comp2) throws Exception {
    
    // ranged constraint doesn't allow =
    if (comp1 == VersionPackageConstraint.VersionComparison.EQUAL ||
        comp2 == VersionPackageConstraint.VersionComparison.EQUAL) {
      throw new Exception ("[VersionRangePackageConstraint] malformed version " +
      		"range constraint (= not allowed)!");
    }
    
    // if both inequalities are in the same direction then a plain
    // VersionPackageConstraint could be used to cover them both
    if (comp1.compatibleWith(comp2)) {
      throw new Exception("[VersionRangePackageConstraint] malformed " +
      		"version range constraint!");
    }
    
    // determine which type of range this is
    VersionPackageConstraint.VersionComparison boundsComp = 
      VersionPackageConstraint.compare(bound1, bound2);
    
    if (boundsComp == VersionPackageConstraint.VersionComparison.EQUAL) {
      throw new Exception("[VersionRangePackageConstraint] malformed version" +
                      " range - both bounds are equal!");
    }
    
    if (comp1 == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
        comp1 == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {            
      
      if (boundsComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
        m_boundOr = true; // it is an OR with respect to the two inequalities        
      } 
    } else {
      if (boundsComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
        m_boundOr = true; // it is an OR with respect to the two inequalities
      }
    }        
    
    // store bounds in ascending order
    if (boundsComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
      m_lowerBound = bound1;
      m_lowerConstraint = comp1;
      m_upperBound = bound2;
      m_upperConstraint = comp2;
    } else {
      m_lowerBound = bound2;
      m_lowerConstraint = comp2;
      m_upperBound = bound1;
      m_upperConstraint = comp1;
    }    
  }
  
  /**
   * Get the lower bound of this range
   * 
   * @return the lower bound
   */
  public String getLowerBound() {
    return m_lowerBound;
  }
  
  /**
   * Get the upper bound of this range
   * 
   * @return the upper bound
   */
  public String getUpperBound() {
    return m_upperBound;
  }
  
  /**
   * Get the lower comparison
   * 
   * @return the lower comparison
   */
  public VersionPackageConstraint.VersionComparison getLowerComparison() {
    return m_lowerConstraint;
  }
  
  /**
   * Get the upper comparison
   * 
   * @return the upper comparison
   */
  public VersionPackageConstraint.VersionComparison getUpperComparison() {
    return m_upperConstraint;
  }
  
  /**
   * Returns true if this is a bounded OR type of constraint
   * 
   * @return true if this is a bounded OR type of constraint
   */
  public boolean isBoundOR() {
    return m_boundOr;
  }
  
  protected static boolean checkConstraint(String toCheck, 
      VersionPackageConstraint.VersionComparison comp1,
      String bound1,
      VersionPackageConstraint.VersionComparison comp2,
      String bound2,
      boolean boundOr) {    
    
    boolean result1 = VersionPackageConstraint.
      checkConstraint(toCheck, comp1, bound1);
    boolean result2 =  VersionPackageConstraint.
      checkConstraint(toCheck, comp2, bound2);
    
    if (boundOr) {
      return (result1 || result2);
    } else {
      return (result1 && result2);
    }    
  }

  /**
   * Check the target package against the constraint embodied
   * in this PackageConstraint.
   * 
   * @param target a package to check with respect to the
   * encapsulated package and the constraint.
   * 
   * @return true if the constraint is met by the target package
   * with respect to the encapsulated package + constraint.
   * @throws Exception if the constraint can't be checked for some
   * reason.
   */
  public boolean checkConstraint(Package target) throws Exception {
    if (m_lowerConstraint == null || m_upperConstraint == null) {
      throw new Exception("[VersionRangePackageConstraint] No constraint has" +
      		" been set!");
    }
    
    String targetVersion = 
      target.getPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY).toString();

    
    return checkConstraint(targetVersion, m_lowerConstraint, m_lowerBound,
        m_upperConstraint, m_upperBound, m_boundOr);
  }
      
  protected PackageConstraint 
    checkTargetVersionRangePackageConstraint(VersionRangePackageConstraint target)
      throws Exception {
    // TODO
    
    String targetLowerBound = target.getLowerBound();
    String targetUpperBound = target.getUpperBound();
    
    VersionPackageConstraint.VersionComparison targetLowerComp = 
      target.getLowerComparison();
    VersionPackageConstraint.VersionComparison targetUpperComp =
      target.getUpperComparison();
    
    if (!m_boundOr) {
      if (target.isBoundOR()) {
        // construct two VersionPackageConstraints to represent the two
        // target open-ended inequalities
        Package p = (Package)target.getPackage().clone();
        p.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, 
            target.getLowerBound());
        VersionPackageConstraint lowerC = new VersionPackageConstraint(p);
        lowerC.setVersionConstraint(target.getLowerComparison());
        
        p = (Package)p.clone();
        p.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, 
            target.getUpperBound());
        VersionPackageConstraint upperC = new VersionPackageConstraint(p);
        upperC.setVersionConstraint(target.getUpperComparison());
        
        PackageConstraint coveringLower = checkTargetVersionPackageConstraint(lowerC);
        if (coveringLower != null) {
          // return this one. It is possible that both could be covered, but one is
          // sufficient (and we have no mechanism for handling two fully bounded
          // ranges!
          return coveringLower;
        }
        
        PackageConstraint coveringUpper = checkTargetVersionPackageConstraint(upperC);
        return coveringUpper; // if we can't cover target at all then this will be null 
      } else {
        // us and target are both bounded AND
        String resultLowerBound = null;
        String resultUpperBound = null;
        VersionPackageConstraint.VersionComparison resultLowerComp = null;
        VersionPackageConstraint.VersionComparison resultUpperComp = null;
        
        VersionPackageConstraint.VersionComparison lowerComp = 
          VersionPackageConstraint.compare(m_lowerBound, targetLowerBound);
        if (lowerComp == VersionPackageConstraint.VersionComparison.EQUAL) {
          resultLowerBound = m_lowerBound;
          resultLowerComp = VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL;
          // select the most restrictive inequality
          if (targetLowerComp == VersionPackageConstraint.VersionComparison.GREATERTHAN || 
              m_lowerConstraint == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            resultLowerComp = VersionPackageConstraint.VersionComparison.GREATERTHAN;
          }
        } else if (lowerComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
          resultLowerBound = m_lowerBound;
          resultLowerComp = m_lowerConstraint;
        } else {
          resultLowerBound = targetLowerBound;
          resultLowerComp = targetLowerComp;
        }
        
        VersionPackageConstraint.VersionComparison upperComp = 
          VersionPackageConstraint.compare(m_upperBound, targetUpperBound);
        if (upperComp == VersionPackageConstraint.VersionComparison.EQUAL) {
          resultUpperBound = m_upperBound;
          resultUpperComp = VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL;
          // select the most restrictive inequality
          if (targetUpperComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
              m_upperConstraint == VersionPackageConstraint.VersionComparison.LESSTHAN) {
            resultUpperComp = VersionPackageConstraint.VersionComparison.LESSTHAN;
          }
        } else if (upperComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
          resultUpperBound = m_upperBound;
          resultUpperComp = m_upperConstraint;
        } else {
          resultUpperBound = targetUpperBound;
          resultUpperComp = targetUpperComp;
        }
        
        // now check for incompatibility - if resultUpper is less than result lower
        // then the ranges are disjoint
        VersionPackageConstraint.VersionComparison disjointCheck =
          VersionPackageConstraint.compare(resultUpperBound, resultLowerBound);
        if (disjointCheck ==
          VersionPackageConstraint.VersionComparison.LESSTHAN ||
          disjointCheck == 
            VersionPackageConstraint.VersionComparison.EQUAL) {
          // TODO if EQUAL then could actually return a VersionPackageConstraint
          // with an EQUAL constraint.
          return null;
        }
        
        // otherwise, we're good to go...
        VersionRangePackageConstraint result = new VersionRangePackageConstraint(getPackage());
        result.setRangeConstraint(resultLowerBound, resultLowerComp, 
            resultUpperBound, resultUpperComp);
        
        return result;
      }
    } else {
      // we are bounded OR
      if (!target.isBoundOR()) {
        // construct two VersionPackageConstraints to represent our two
        // open-ended inequalities
        Package p = (Package)getPackage().clone();
        p.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, m_lowerBound);
        VersionPackageConstraint lowerC = new VersionPackageConstraint(p);
        lowerC.setVersionConstraint(m_lowerConstraint);
        
        p = (Package)p.clone();
        p.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, 
            m_upperBound);
        VersionPackageConstraint upperC = new VersionPackageConstraint(p);
        upperC.setVersionConstraint(m_upperConstraint);
        
        PackageConstraint coveringLower = target.checkTargetVersionPackageConstraint(lowerC);
        if (coveringLower != null) {
          // return this one. It is possible that both could be covered, but one is
          // sufficient (and we have no mechanism for handling two fully bounded
          // ranges!
          return coveringLower;
        }
        
        PackageConstraint coveringUpper = checkTargetVersionPackageConstraint(upperC);
        return coveringUpper; // if the target can't cover us then this will be null
      } else {
        // both us and target are bounded ORs. Just need the greatest upper bound
        // and the smallest lower bound of the two
        String resultLowerBound = null;
        String resultUpperBound = null;
        VersionPackageConstraint.VersionComparison resultLowerComp = null;
        VersionPackageConstraint.VersionComparison resultUpperComp = null;
        
        VersionPackageConstraint.VersionComparison lowerComp = 
          VersionPackageConstraint.compare(m_lowerBound, targetLowerBound);
        if (lowerComp == VersionPackageConstraint.VersionComparison.EQUAL) {
          resultLowerBound = m_lowerBound;
          resultLowerComp = VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL;
          // select the most restrictive inequality
          if (targetLowerComp == VersionPackageConstraint.VersionComparison.LESSTHAN || 
              m_lowerConstraint == VersionPackageConstraint.VersionComparison.LESSTHAN) {
            resultLowerComp = VersionPackageConstraint.VersionComparison.LESSTHAN;
          }
        } else if (lowerComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
          resultLowerBound = m_lowerBound;
          resultLowerComp = m_lowerConstraint;
        } else {
          resultLowerBound = targetLowerBound;
          resultLowerComp = targetLowerComp;
        }
        
        VersionPackageConstraint.VersionComparison upperComp = 
          VersionPackageConstraint.compare(m_upperBound, targetUpperBound);
        if (upperComp == VersionPackageConstraint.VersionComparison.EQUAL) {
          resultUpperBound = m_upperBound;
          resultUpperComp = VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL;
          // select the most restrictive inequality
          if (targetUpperComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
              m_upperConstraint == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            resultUpperComp = VersionPackageConstraint.VersionComparison.GREATERTHAN;
          }
        } else if (upperComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
          resultUpperBound = m_upperBound;
          resultUpperComp = m_upperConstraint;
        } else {
          resultUpperBound = targetUpperBound;
          resultUpperComp = targetUpperComp;
        }
        
        VersionRangePackageConstraint result = new VersionRangePackageConstraint(getPackage());
        result.setRangeConstraint(resultLowerBound, resultLowerComp, 
            resultUpperBound, resultUpperComp);
        
        return result; 
      }
    }    
  }
  
  protected PackageConstraint checkTargetVersionPackageConstraint(VersionPackageConstraint target) 
    throws Exception {
    
    VersionPackageConstraint.VersionComparison targetComp = 
        target.getVersionComparison();
    
    String targetVersion = 
      target.getPackage().
        getPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY).toString();
    
    VersionPackageConstraint.VersionComparison lowerComp = 
      VersionPackageConstraint.compare(targetVersion, m_lowerBound);
    VersionPackageConstraint.VersionComparison upperComp = 
      VersionPackageConstraint.compare(targetVersion, m_upperBound);
    
    boolean lowerCheck = false;
    boolean upperCheck = false;
    String coveringLowerBound = null;
    String coveringUpperBound = null;
    VersionPackageConstraint.VersionComparison coveringLowerConstraint = null;
    VersionPackageConstraint.VersionComparison coveringUpperConstraint = null;
    
    
    // equals is easy
    if (targetComp == VersionPackageConstraint.VersionComparison.EQUAL) {
      
      // If our range contains the target version number then the target
      // is the least restrictive constraint that covers both
      if (checkConstraint(target.getPackage())) {
        return target;
      } else {
        return null; // incompatible
      }
    } else {            
      if (m_boundOr) {
        // Check against our lower bound (our lower bound is a < or <=) --------------
        // lower bound < case
        if (m_lowerConstraint == VersionPackageConstraint.VersionComparison.LESSTHAN) {
          if (lowerComp == VersionPackageConstraint.VersionComparison.EQUAL ||
              lowerComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {
              // not compatible with our lower bound
              lowerCheck = false;
            } else {
              lowerCheck = true;
              // adjust the bounds
              coveringLowerBound = m_lowerBound;
              coveringLowerConstraint = m_lowerConstraint;
            }
          } else if (lowerComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
            lowerCheck = true;
            coveringLowerBound = targetVersion;
            coveringLowerConstraint = targetComp;
            if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {
              coveringUpperBound = null; // same direction as our lower constraint (so no upper bound)
            } else {
              // target's comparison is > or >= (new upper bound = our lower bound)
              coveringUpperBound = m_lowerBound;
              coveringUpperConstraint = m_lowerConstraint;
            }
          }
        } else {
          // lower bound <= case
          if (lowerComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {          
              // not compatible with our lower bound
              lowerCheck = false;
            } else {
              lowerCheck = true;
              // adjust bounds
              coveringLowerBound = m_lowerBound;
              coveringLowerConstraint = m_lowerConstraint;
            }
          } else if (lowerComp == VersionPackageConstraint.VersionComparison.EQUAL) {
            // if the target version is equal to our lower bound then the target constraint
            // can't be >
            if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
              lowerCheck = false;
            } else {
              // target constraint is < or <=
              lowerCheck = true;
              coveringLowerBound = targetVersion;
              coveringLowerConstraint = targetComp;
              
              coveringUpperBound = null; // same direction as our lower bound (so no upper bound)
            }
          } else {
            // target version is < or = to our lower bound
            lowerCheck = true;
            coveringLowerBound = targetVersion;
            coveringLowerConstraint = targetComp;
            if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {
              coveringUpperBound = null; // same direction as our lower constraint (so no upper bound)
            } else {
              // target's comparison is > or >= (new upper bound = our lower bound)
              coveringUpperBound = m_lowerBound;
              coveringUpperConstraint = m_lowerConstraint;
            }
          }
        }
        // end check against our lower bound ----------------
        
        // check against our upper bound (if necessary)
        if (!lowerCheck) {
          // Check against our upper bound (our upper bound is a > or >=) --------------
          // upper bound > case
          if (m_upperConstraint == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            if (upperComp == VersionPackageConstraint.VersionComparison.EQUAL ||
                upperComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
              if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
                  targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {              
                // not compatible with our upper bound
                upperCheck = false;
              } else {
                lowerCheck = true;
                // adjust the bounds
                coveringUpperBound = m_upperBound;
                coveringUpperConstraint = m_upperConstraint;
              }
            } else if (upperComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
              upperCheck = true;
              coveringUpperBound = targetVersion;
              coveringUpperConstraint = targetComp;
              if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
                  targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {
                coveringLowerBound = null; // same direction as our upper constraint (so no lower bound)
              } else {
                // target's comparison is < or <= (new lower bound = our upper bound)
                coveringLowerBound = m_upperBound;
                coveringLowerConstraint = m_upperConstraint;
              }
            }                            
          } else {
            // upper bound >= case
            if (upperComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
              if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
                  targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {            
                // not compatible with our upper bound
                upperCheck = false;
              } else {
                upperCheck = true;
                // adjust bounds
                coveringUpperBound = m_upperBound;
                coveringUpperConstraint = m_upperConstraint;
              }
            } else if (upperComp == VersionPackageConstraint.VersionComparison.EQUAL) {
              // if the target version is equal to our upper bound then the target constraint
              // can't be <
              if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
                upperCheck = false;
              } else {
                // target constraint is > or >=
                upperCheck = true;
                coveringUpperBound = targetVersion;
                coveringUpperConstraint = targetComp;
                
                coveringLowerBound = null; // same direction as our upper bound (so no lower bound)
              }
            } else {
              // target version is > or = to our upper bound
              upperCheck = true;
              coveringUpperBound = targetVersion;
              coveringUpperConstraint = targetComp;
              if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
                  targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {
                coveringLowerBound = null; // same direction as our upper constraint (so no lower bound)
              } else {
                // target's comparison is < or <= (new lower bound = our upper bound)
                coveringLowerBound = m_upperBound;
                coveringLowerConstraint = m_upperConstraint;
              }
            }
          }
        }
        
        // now return the appropriate type of package constraint to cover this and the
        // target
        
        if (!lowerCheck && !upperCheck) {
          // this shouldn't be possible
          throw new Exception("[VersionRangePackageConstraint] This shouldn't be possible!!");
        }
        
        if (coveringLowerBound != null && coveringUpperBound != null) {
          VersionRangePackageConstraint result = new VersionRangePackageConstraint(getPackage());
          result.setRangeConstraint(coveringLowerBound, coveringLowerConstraint, 
              coveringUpperBound, coveringUpperConstraint);
          return result;
        }
        
        String newVersionNumber = (coveringLowerBound != null)
          ? coveringLowerBound
          : coveringUpperBound;
        VersionPackageConstraint.VersionComparison newConstraint =
          (coveringLowerConstraint != null)
            ? coveringLowerConstraint
            : coveringUpperConstraint;
        Package p = (Package)getPackage().clone();
        
        p.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, 
            newVersionNumber);
        VersionPackageConstraint result = new VersionPackageConstraint(p);
        result.setVersionConstraint(newConstraint);
        
        return result;        
      } // end bounded OR case //////////
      else {
        // bounded AND case        
        if (lowerComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
          if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
              targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL ||
              targetComp == VersionPackageConstraint.VersionComparison.EQUAL) {
            // outside of our range and inequality is in the wrong direction
            lowerCheck = false;
          } else {
            // outside our range, but inequality in right direction - our range satisfies both
            lowerCheck = true;
            coveringLowerBound = m_lowerBound;
            coveringLowerConstraint = m_lowerConstraint;
            coveringUpperBound = m_upperBound;
            coveringUpperConstraint = m_upperConstraint;
          }          
        } else if (lowerComp == VersionPackageConstraint.VersionComparison.EQUAL) {
          // satisfiable if target comp is > or >=
          if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL ||
              targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            lowerCheck = true;
            coveringLowerBound = m_lowerBound;
            coveringLowerConstraint = 
              (m_lowerConstraint == VersionPackageConstraint.VersionComparison.GREATERTHAN || 
                  targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN)
                  ? VersionPackageConstraint.VersionComparison.GREATERTHAN
                  : VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL;
            coveringUpperBound = m_upperBound;
            coveringUpperConstraint = m_upperConstraint;
          } else {
            // target comp is < or <= - can satisfy only if <= and our lower constraint is >=
            if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL &&
                m_lowerConstraint == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL) {
              VersionPackageConstraint.VersionComparison newComp = 
                VersionPackageConstraint.VersionComparison.EQUAL;
              VersionPackageConstraint result = 
                new VersionPackageConstraint(target.getPackage());
              result.setVersionConstraint(newComp);
              // we're done
              return result;              
            }
          }
        } else if (lowerComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
          // handle the within range (but not on the upper boundary) case here
          if (upperComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
            if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {
              lowerCheck = true; upperCheck = true;
              coveringLowerBound = m_lowerBound;
              coveringLowerConstraint = m_lowerConstraint;
              coveringUpperBound = targetVersion;
              coveringUpperConstraint = targetComp;
            } else {
              coveringLowerBound = targetVersion;
              coveringLowerConstraint = targetComp;
              coveringUpperBound = m_upperBound;
              coveringUpperConstraint = m_upperConstraint;
            }
          }
        }
        
        if (coveringLowerBound == null || coveringUpperBound == null) {
          // consider the upper bound
          if (upperComp == VersionPackageConstraint.VersionComparison.EQUAL) {
            // satisfiable if target comp is < or <=
            if (targetComp == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL ||
                targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN) {
              upperCheck = true;
              coveringUpperBound = m_upperBound;
              coveringUpperConstraint = 
                (m_upperConstraint == VersionPackageConstraint.VersionComparison.LESSTHAN || 
                    targetComp == VersionPackageConstraint.VersionComparison.LESSTHAN)
                    ? VersionPackageConstraint.VersionComparison.LESSTHAN
                    : VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL;
              coveringLowerBound = m_lowerBound;
              coveringLowerConstraint = m_lowerConstraint;
            } else {
              // target comp is > or >= - can satisfy only if >= and our upper constraint is <=
              if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL &&
                  m_upperConstraint == VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL) {
                VersionPackageConstraint.VersionComparison newComp = 
                  VersionPackageConstraint.VersionComparison.EQUAL;
                VersionPackageConstraint result = 
                  new VersionPackageConstraint(target.getPackage());
                result.setVersionConstraint(newComp);
                // we're done
                return result;              
              }
            }
          } else if (upperComp == VersionPackageConstraint.VersionComparison.GREATERTHAN) {
            if (targetComp == VersionPackageConstraint.VersionComparison.GREATERTHAN ||
                targetComp == VersionPackageConstraint.VersionComparison.GREATERTHANOREQUAL ||
                targetComp == VersionPackageConstraint.VersionComparison.EQUAL) {
              // outside of our range and inequality is in the wrong direction
              upperCheck = false;
            } else {
              // outside our range, but inequality in right direction - our range satisfies both
              upperCheck = true;
              coveringUpperBound = m_upperBound;
              coveringUpperConstraint = m_upperConstraint;
              coveringLowerBound = m_lowerBound;
              coveringLowerConstraint = m_lowerConstraint;
            } 
          }
        }
        
        if (coveringUpperBound == null && coveringLowerBound == null) {
          // we can't satisfy both
          return null;
        }
        
        if (coveringUpperBound == null || coveringLowerBound == null) {
          // this shouldn't happen - either we can't cover it (in which case
          // both should be null) or we can (in which case both are non-null).
          throw new Exception("[VersionRangePackageConstraint] This shouldn't be possible!!");
        }
        
        VersionRangePackageConstraint result = 
          new VersionRangePackageConstraint(getPackage());
        
        result.setRangeConstraint(coveringLowerBound, coveringLowerConstraint, 
            coveringUpperBound, coveringUpperConstraint);
        return result;
      }
      // check for incompatible first
      /*boolean checkLower = 
        VersionPackageConstraint.checkConstraint(targetVersion, m_lowerConstraint, m_lowerBound); */
      
      
      /*VersionPackageConstraint lowerBoundCover = 
        new VersionPackageConstraint((Package)(getPackage().clone()));
      lowerBoundCover.setVersionConstraint(m_lowerConstraint); */
      
    }    
  }

  @Override
  public PackageConstraint checkConstraint(PackageConstraint target)
      throws Exception {
    
    if (m_lowerConstraint == null || m_upperConstraint == null) {
      throw new Exception("[VersionRangePackageConstraint] No constraint has" +
                " been set!");
    }
    
    // have to cover the case where the target is a VersionPackageConstraint or
    // a VersionRangePackageConstraint
    if (!(target instanceof VersionRangePackageConstraint) &&
        !(target instanceof VersionPackageConstraint)) {
      throw new Exception("[VersionRangePackageConstraint] incompatible " +
      		"target constraint!");
    }
        
    
    // target is a VersionPackageConstraint
    if (target instanceof VersionPackageConstraint) {
      PackageConstraint result = 
        checkTargetVersionPackageConstraint((VersionPackageConstraint)target);
      return result;
    } else if (target instanceof VersionRangePackageConstraint) {
      // target is VersionRangePackageConstraint
      PackageConstraint result =
        checkTargetVersionRangePackageConstraint((VersionRangePackageConstraint)target);
      return result;
    }
        
    return null;
  }
}
