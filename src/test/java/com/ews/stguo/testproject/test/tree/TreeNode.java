package com.ews.stguo.testproject.test.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class TreeNode {

    private int id;
    private int pid;
    private int depth;
    private String name;

    public TreeNode(int id, int pid,int depth, String name) {
        this.id = id;
        this.pid = pid;
        this.depth = depth;
        this.name = name;
    }

    private static Map<Integer, List<TreeNode>> treeNodesByDepth;
    private static TreeNode testNode = new TreeNode(3, 1, 2, "3-depth-node-1");

    public static void main(String[] args) {
        // Setup data.
        setUp();
        // Find 3-depth-node-2 all of parent tree nodes.
        List<TreeNode> allParentNodes = getAllParentNodes(testNode.getPid(), testNode.getDepth());
        System.out.println(allParentNodes);
    }

    private static List<TreeNode> getAllParentNodes(int pid, int depth) {
        if ((depth - 1) < 0) {
            return new ArrayList<>();
        }
        TreeNode parentTreeNode = null;
        for (TreeNode treeNode : treeNodesByDepth.get(depth - 1)) {
            if (treeNode.getId() == pid) {
                parentTreeNode = treeNode;
                break;
            }
        }
        if (parentTreeNode == null) {
            return new ArrayList<>();
        }
        List<TreeNode> parentTreeNodes = new ArrayList<>();
        parentTreeNodes.addAll(getAllParentNodes(parentTreeNode.getPid(), parentTreeNode.getDepth()));
        parentTreeNodes.add(parentTreeNode);
        // List to Array.
        return parentTreeNodes;
    }

    private static void setUp() {
        // Suppose the datasource is a List.
        List<TreeNode> treeNodes = Arrays.asList(
                new TreeNode(0, 0, 0, "Root"),

                new TreeNode(1, 0, 1, "2-depth-node-1"),
                new TreeNode(2, 0, 1, "2-depth-node-2"),

                testNode,
                new TreeNode(4, 2, 2, "3-depth-node-2"),

                new TreeNode(5, 3, 3, "4-depth-node-1"),
                new TreeNode(6, 3, 3, "4-depth-node-2"),
                new TreeNode(7, 4, 3, "4-depth-node-3")
        );
        // Convert list as to map by depth.
        // It looks like as 0: {"Root"}, 1: {"2-depth-node-1", "2-depth-node-2"}
        treeNodesByDepth = treeNodes.stream().collect(Collectors.groupingBy(TreeNode::getDepth));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "id=" + id +
                ", pid=" + pid +
                ", depth=" + depth +
                ", name='" + name + '\'' +
                '}';
    }
}
