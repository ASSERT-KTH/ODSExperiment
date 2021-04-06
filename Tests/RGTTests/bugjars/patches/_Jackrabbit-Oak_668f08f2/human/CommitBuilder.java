/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.mk.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.mk.store.NotFoundException;
import org.apache.jackrabbit.mk.store.RevisionStore;
import org.apache.jackrabbit.mk.util.PathUtils;

/**
 *
 */
public class CommitBuilder {

    private Id baseRevId;

    private final String msg;

    private final RevisionStore store;

    // key is a path
    private final Map<String, MutableNode> staged = new HashMap<String, MutableNode>();
    // change log
    private final List<Change> changeLog = new ArrayList<Change>();

    public CommitBuilder(Id baseRevId, String msg, RevisionStore store) throws Exception {
        this.baseRevId = baseRevId;
        this.msg = msg;
        this.store = store;
    }

    public void addNode(String parentNodePath, String nodeName, NodeTree node) throws Exception {
        Change change = new AddNode(parentNodePath, nodeName, node);
        change.apply();
        // update change log
        changeLog.add(change);
    }

    public void removeNode(String nodePath) throws NotFoundException, Exception {
        Change change = new RemoveNode(nodePath);
        change.apply();
        // update change log
        changeLog.add(change);
    }

    public void moveNode(String srcPath, String destPath) throws NotFoundException, Exception {
        Change change = new MoveNode(srcPath, destPath);
        change.apply();
        // update change log
        changeLog.add(change);
    }

    public void copyNode(String srcPath, String destPath) throws NotFoundException, Exception {
        Change change = new CopyNode(srcPath, destPath);
        change.apply();
        // update change log
        changeLog.add(change);
    }

    public void setProperty(String nodePath, String propName, String propValue) throws Exception {
        Change change = new SetProperty(nodePath, propName, propValue);
        change.apply();
        // update change log
        changeLog.add(change);
    }

    public Id /* new revId */ doCommit() throws Exception {
        if (staged.isEmpty()) {
            // nothing to commit
            return baseRevId;
        }

        Id currentHead = store.getHeadCommitId();
        if (!currentHead.equals(baseRevId)) {
            // todo gracefully handle certain conflicts (e.g. changes on moved sub-trees, competing deletes etc)
            // update base revision to new head
            baseRevId = currentHead;
            // clear staging area
            staged.clear();
            // replay change log on new base revision
            for (Change change : changeLog) {
                change.apply();
            }
        }

        Id rootNodeId = persistStagedNodes();

        Id newRevId;
        store.lockHead();
        try {
            currentHead = store.getHeadCommitId();
            if (!currentHead.equals(baseRevId)) {
                StoredNode baseRoot = store.getRootNode(baseRevId);
                StoredNode theirRoot = store.getRootNode(currentHead);
                StoredNode ourRoot = store.getNode(rootNodeId);

                rootNodeId = mergeTree(baseRoot, ourRoot, theirRoot);

                baseRevId = currentHead;
            }

            if (store.getCommit(currentHead).getRootNodeId().equals(rootNodeId)) {
                // the commit didn't cause any changes,
                // no need to create new commit object/update head revision
                return currentHead;
            }
            MutableCommit newCommit = new MutableCommit();
            newCommit.setParentId(baseRevId);
            newCommit.setCommitTS(System.currentTimeMillis());
            newCommit.setMsg(msg);
            StringBuilder diff = new StringBuilder();
            for (Change change : changeLog) {
                if (diff.length() > 0) {
                    diff.append('\n');
                }
                diff.append(change.asDiff());
            }
            newCommit.setChanges(diff.toString());
            newCommit.setRootNodeId(rootNodeId);
            newRevId = store.putHeadCommit(newCommit);
        } finally {
            store.unlockHead();
        }

        // reset instance
        staged.clear();
        changeLog.clear();

        return newRevId;
    }

    //--------------------------------------------------------< inner classes >

    MutableNode getOrCreateStagedNode(String nodePath) throws Exception {
        MutableNode node = staged.get(nodePath);
        if (node == null) {
            MutableNode parent = staged.get("/");
            if (parent == null) {
                parent = new MutableNode(store.getRootNode(baseRevId), store, "/");
                staged.put("/", parent);
            }
            node = parent;
            String names[] = PathUtils.split(nodePath);
            for (int i = names.length - 1; i >= 0; i--) {
                String path = PathUtils.getAncestorPath(nodePath, i);
                node = staged.get(path);
                if (node == null) {
                    // not yet staged, resolve id using staged parent
                    // to allow for staged move operations
                    ChildNode cne = parent.getChildNodeEntry(names[names.length - i - 1]);
                    if (cne == null) {
                        throw new NotFoundException(nodePath);
                    }
                    node = new MutableNode(store.getNode(cne.getId()), store, path);
                    staged.put(path, node);
                }
                parent = node;
            }
        }
        return node;
    }

    void moveStagedNodes(String srcPath, String destPath) throws Exception {
        MutableNode node = staged.get(srcPath);
        if (node != null) {
            staged.remove(srcPath);
            staged.put(destPath, node);
            for (Iterator<String> it = node.getChildNodeNames(0, -1); it.hasNext(); ) {
                String childName = it.next();
                moveStagedNodes(PathUtils.concat(srcPath, childName), PathUtils.concat(destPath, childName));
            }
        }
    }

    void copyStagedNodes(String srcPath, String destPath) throws Exception {
        MutableNode node = staged.get(srcPath);
        if (node != null) {
            staged.put(destPath, new MutableNode(node, store, destPath));
            for (Iterator<String> it = node.getChildNodeNames(0, -1); it.hasNext(); ) {
                String childName = it.next();
                copyStagedNodes(PathUtils.concat(srcPath, childName), PathUtils.concat(destPath, childName));
            }
        }
    }

    void removeStagedNodes(String nodePath) throws Exception {
        MutableNode node = staged.get(nodePath);
        if (node != null) {
            staged.remove(nodePath);
            for (Iterator<String> it = node.getChildNodeNames(0, -1); it.hasNext(); ) {
                String childName = it.next();
                removeStagedNodes(PathUtils.concat(nodePath, childName));
            }
        }
    }

    Id /* new id of root node */ persistStagedNodes() throws Exception {
        // sort paths in in depth-descending order
        ArrayList<String> orderedPaths = new ArrayList<String>(staged.keySet());
        Collections.sort(orderedPaths, new Comparator<String>() {
            public int compare(String path1, String path2) {
                // paths should be ordered by depth, descending
                int result = getDepth(path2) - getDepth(path1);
                return (result != 0) ? result : 1;
            }

            int getDepth(String path) {
                return PathUtils.getDepth(path);
            }
        });
        // iterate over staged entries in depth-descending order
        Id rootNodeId = null;
        for (String path : orderedPaths) {
            // persist node
            Id id = store.putNode(staged.get(path));
            if (PathUtils.denotesRoot(path)) {
                rootNodeId = id;
            } else {
                staged.get(PathUtils.getParentPath(path)).add(new ChildNode(PathUtils.getName(path), id));
            }
        }
        if (rootNodeId == null) {
            throw new Exception("internal error: inconsistent staging area content");
        }
        return rootNodeId;
    }

    /**
     * Performs a three-way merge of the trees rooted at <code>ourRoot</code>,
     * <code>theirRoot</code>, using the tree at <code>baseRoot</code> as reference.
     *
     * @param baseRoot
     * @param ourRoot
     * @param theirRoot
     * @return id of merged root node
     * @throws Exception
     */
    Id /* id of merged root node */ mergeTree(StoredNode baseRoot, StoredNode ourRoot, StoredNode theirRoot) throws Exception {
        // as we're going to use the staging area for the merge process,
        // we need to clear it first
        staged.clear();

        // recursively merge 'our' changes with 'their' changes...
        mergeNode(baseRoot, ourRoot, theirRoot, "/");

        return persistStagedNodes();
    }

    void mergeNode(StoredNode baseNode, StoredNode ourNode, StoredNode theirNode, String path) throws Exception {
        NodeDelta theirChanges = new NodeDelta(
                store, store.getNodeState(baseNode), store.getNodeState(theirNode));
        NodeDelta ourChanges = new NodeDelta(
                store, store.getNodeState(baseNode), store.getNodeState(ourNode));

        // merge non-conflicting changes
        MutableNode mergedNode = new MutableNode(theirNode, store, path);
        staged.put(path, mergedNode);

        mergedNode.getProperties().putAll(ourChanges.getAddedProperties());
        mergedNode.getProperties().putAll(ourChanges.getChangedProperties());
        for (String name : ourChanges.getRemovedProperties().keySet()) {
            mergedNode.getProperties().remove(name);
        }

        for (Map.Entry<String, Id> entry : ourChanges.getAddedChildNodes ().entrySet()) {
            mergedNode.add(new ChildNode(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, Id> entry : ourChanges.getChangedChildNodes ().entrySet()) {
            mergedNode.add(new ChildNode(entry.getKey(), entry.getValue()));
        }
        for (String name : ourChanges.getRemovedChildNodes().keySet()) {
            mergedNode.remove(name);
        }

        List<NodeDelta.Conflict> conflicts = theirChanges.listConflicts(ourChanges);
        // resolve/report merge conflicts
        for (NodeDelta.Conflict conflict : conflicts) {
            String conflictName = conflict.getName();
            String conflictPath = PathUtils.concat(path, conflictName);
            switch (conflict.getType()) {
                case PROPERTY_VALUE_CONFLICT:
                    throw new Exception(
                            "concurrent modification of property " + conflictPath
                                    + " with conflicting values: \""
                                    + ourNode.getProperties().get(conflictName)
                                    + "\", \""
                                    + theirNode.getProperties().get(conflictName));

                case NODE_CONTENT_CONFLICT: {
                    if (ourChanges.getChangedChildNodes().containsKey(conflictName)) {
                        // modified subtrees
                        StoredNode baseChild = store.getNode(baseNode.getChildNodeEntry(conflictName).getId());
                        StoredNode ourChild = store.getNode(ourNode.getChildNodeEntry(conflictName).getId());
                        StoredNode theirChild = store.getNode(theirNode.getChildNodeEntry(conflictName).getId());
                        // merge the dirty subtrees recursively
                        mergeNode(baseChild, ourChild, theirChild, PathUtils.concat(path, conflictName));
                    } else {
                        // todo handle/merge colliding node creation
                        throw new Exception("colliding concurrent node creation: " + conflictPath);
                    }
                    break;
                }

                case REMOVED_DIRTY_PROPERTY_CONFLICT:
                    mergedNode.getProperties().remove(conflictName);
                    break;

                case REMOVED_DIRTY_NODE_CONFLICT:
                    mergedNode.remove(conflictName);
                    break;
            }

        }
    }

    //--------------------------------------------------------< inner classes >

    public static class NodeTree {
        public Map<String, String> props = new HashMap<String, String>();
        public Map<String, NodeTree> nodes = new HashMap<String, NodeTree>();

        void toJson(StringBuffer buf) {
            toJson(buf, this);
        }

        private static void toJson(StringBuffer buf, NodeTree node) {
            buf.append('{');
            for (String name : node.props.keySet()) {
                if (buf.charAt(buf.length() - 1) != '{')  {
                    buf.append(',');
                }
                buf.append('"').append(name).append("\":").append(node.props.get(name));
            }
            for (String name : node.nodes.keySet()) {
                if (buf.charAt(buf.length() - 1) != '{')  {
                    buf.append(',');
                }
                buf.append('"').append(name).append("\":");
                toJson(buf, node.nodes.get(name));
            }
            buf.append('}');
        }
    }

    abstract class Change {
        abstract void apply() throws Exception;
        abstract String asDiff();
    }

    class AddNode extends Change {
        String parentNodePath;
        String nodeName;
        NodeTree node;

        AddNode(String parentNodePath, String nodeName, NodeTree node) {
            this.parentNodePath = parentNodePath;
            this.nodeName = nodeName;
            this.node = node;
        }

        @Override
        void apply() throws Exception {
            recursiveAddNode(parentNodePath, nodeName, node);
        }

        @Override
        String asDiff() {
            StringBuffer diff = new StringBuffer("+");
            diff.append('"').append(PathUtils.concat(parentNodePath, nodeName)).append("\":");
            node.toJson(diff);
            return diff.toString();
        }

        private void recursiveAddNode(String parentPath, String name, NodeTree node) throws Exception {
            MutableNode modParent = getOrCreateStagedNode(parentPath);
            if (modParent.getChildNodeEntry(name) != null) {
                throw new Exception("there's already a child node with name '" + name + "'");
            }
            String newPath = PathUtils.concat(parentPath, name);
            MutableNode newChild = new MutableNode(store, newPath);
            newChild.getProperties().putAll(node.props);

            // id will be computed on commit
            modParent.add(new ChildNode(name, null));
            staged.put(newPath, newChild);

            for (String childName : node.nodes.keySet()) {
                recursiveAddNode(PathUtils.concat(parentPath, name), childName, node.nodes.get(childName));
            }
        }
    }

    class RemoveNode extends Change {
        String nodePath;

        RemoveNode(String nodePath) {
            this.nodePath = nodePath;
        }

        @Override
        void apply() throws Exception {
            String parentPath = PathUtils.getParentPath(nodePath);
            String nodeName = PathUtils.getName(nodePath);

            MutableNode parent = getOrCreateStagedNode(parentPath);
            if (parent.remove(nodeName) == null) {
                throw new NotFoundException(nodePath);
            }

            // update staging area
            removeStagedNodes(nodePath);
        }

        @Override
        String asDiff() {
            StringBuffer diff = new StringBuffer("-");
            diff.append('"').append(nodePath).append('"');
            return diff.toString();
        }
    }

    class MoveNode extends Change {
        String srcPath;
        String destPath;

        MoveNode(String srcPath, String destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        void apply() throws Exception {
            if (PathUtils.isAncestor(srcPath, destPath)) {
                throw new Exception("target path cannot be descendant of source path: " + destPath);
            }

            String srcParentPath = PathUtils.getParentPath(srcPath);
            String srcNodeName = PathUtils.getName(srcPath);

            String destParentPath = PathUtils.getParentPath(destPath);
            String destNodeName = PathUtils.getName(destPath);

            MutableNode srcParent = getOrCreateStagedNode(srcParentPath);
            if (srcParentPath.equals(destParentPath)) {
                if (srcParent.getChildNodeEntry(destNodeName) != null) {
                    throw new Exception("node already exists at move destination path: " + destPath);
                }
                if (srcParent.rename(srcNodeName, destNodeName) == null) {
                    throw new NotFoundException(srcPath);
                }
            } else {
                ChildNode srcCNE = srcParent.remove(srcNodeName);
                if (srcCNE == null) {
                    throw new NotFoundException(srcPath);
                }

                MutableNode destParent = getOrCreateStagedNode(destParentPath);
                if (destParent.getChildNodeEntry(destNodeName) != null) {
                    throw new Exception("node already exists at move destination path: " + destPath);
                }
                destParent.add(new ChildNode(destNodeName, srcCNE.getId()));
            }

            // update staging area
            moveStagedNodes(srcPath, destPath);
        }

        @Override
        String asDiff() {
            StringBuffer diff = new StringBuffer(">");
            diff.append('"').append(srcPath).append("\":\"").append(destPath).append('"');
            return diff.toString();
        }
    }

    class CopyNode extends Change {
        String srcPath;
        String destPath;

        CopyNode(String srcPath, String destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        void apply() throws Exception {
            String srcParentPath = PathUtils.getParentPath(srcPath);
            String srcNodeName = PathUtils.getName(srcPath);

            String destParentPath = PathUtils.getParentPath(destPath);
            String destNodeName = PathUtils.getName(destPath);

            MutableNode srcParent = getOrCreateStagedNode(srcParentPath);
            ChildNode srcCNE = srcParent.getChildNodeEntry(srcNodeName);
            if (srcCNE == null) {
                throw new NotFoundException(srcPath);
            }

            MutableNode destParent = getOrCreateStagedNode(destParentPath);
            destParent.add(new ChildNode(destNodeName, srcCNE.getId()));

            if (srcCNE.getId() == null) {
                // a 'new' node is being copied

                // update staging area
                copyStagedNodes(srcPath, destPath);
            }
        }

        @Override
        String asDiff() {
            StringBuffer diff = new StringBuffer("*");
            diff.append('"').append(srcPath).append("\":\"").append(destPath).append('"');
            return diff.toString();
        }
    }

    class SetProperty extends Change {
        String nodePath;
        String propName;
        String propValue;

        SetProperty(String nodePath, String propName, String propValue) {
            this.nodePath = nodePath;
            this.propName = propName;
            this.propValue = propValue;
        }

        @Override
        void apply() throws Exception {
            MutableNode node = getOrCreateStagedNode(nodePath);

            Map<String, String> properties = node.getProperties();
            if (propValue == null) {
                properties.remove(propName);
            } else {
                properties.put(propName, propValue);
            }
        }

        @Override
        String asDiff() {
            StringBuffer diff = new StringBuffer("^");
            diff.append('"').append(PathUtils.concat(nodePath, propName)).append("\":").append(propValue);
            return diff.toString();
        }
    }
}
