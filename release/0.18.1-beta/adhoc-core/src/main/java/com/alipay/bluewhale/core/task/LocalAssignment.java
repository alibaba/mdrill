package com.alipay.bluewhale.core.task;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于supervisor中获取topology的任务
 *
 */
public class LocalAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    private String       topologyId;
    private Set<Integer> taskIds;

    public LocalAssignment(String topologyId, Set<Integer> taskIds) {
        this.topologyId = topologyId;
        this.taskIds = new HashSet<Integer>(taskIds);
    }

    public String getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(String topologyId) {
        this.topologyId = topologyId;
    }

    public Set<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(Set<Integer> taskIds) {
        this.taskIds = new HashSet<Integer>(taskIds);
    }

    @Override
    public boolean equals(Object localAssignment) {
        if (localAssignment instanceof LocalAssignment
                && ((LocalAssignment) localAssignment).getTopologyId().equals(
                        topologyId)
                && ((LocalAssignment) localAssignment).getTaskIds().equals(
                        taskIds)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.taskIds.hashCode() + this.topologyId.hashCode();
    }
}
