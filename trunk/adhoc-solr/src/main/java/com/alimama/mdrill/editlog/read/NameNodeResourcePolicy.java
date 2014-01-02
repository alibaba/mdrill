package com.alimama.mdrill.editlog.read;


import java.util.Collection;

import com.alimama.mdrill.editlog.defined.CheckableNameNodeResource;



public class NameNodeResourcePolicy {

 public static boolean areResourcesAvailable(
      Collection<? extends CheckableNameNodeResource> resources,
      int minimumRedundantResources) {
    if (resources.isEmpty()) {
      return true;
    }
    
    int requiredResourceCount = 0;
    int redundantResourceCount = 0;
    int disabledRedundantResourceCount = 0;
    for (CheckableNameNodeResource resource : resources) {
      if (!resource.isRequired()) {
        redundantResourceCount++;
        if (!resource.isResourceAvailable()) {
          disabledRedundantResourceCount++;
        }
      } else {
        requiredResourceCount++;
        if (!resource.isResourceAvailable()) {
          // Short circuit - a required resource is not available.
          return false;
        }
      }
    }
    
    if (redundantResourceCount == 0) {
      // If there are no redundant resources, return true if there are any
      // required resources available.
      return requiredResourceCount > 0;
    } else {
      return redundantResourceCount - disabledRedundantResourceCount >=
          minimumRedundantResources;
    }
  }
}
