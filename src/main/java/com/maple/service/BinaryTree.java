package com.maple.service;


import java.util.LinkedList;
import java.util.Queue;

public class BinaryTree {
    static class TreeNode{
        private int val;
        private TreeNode left;
        private TreeNode right;
        public TreeNode(int val){
            this.val = val;
            left = null;
            right = null;
        }
    }
    private TreeNode root;
    public void insert(int val){
        TreeNode current = root, parent = null;
        while (current != null && val != current.val){
            parent = current;
            if (val < current.val) current = current.left;
            else current = current.right;
        }
        if (root == null) root = new TreeNode(val);
        else if (current != null) return;
        else {
            if (val < parent.val) parent.left = new TreeNode(val);
            else parent.right = new TreeNode(val);
        }
    }
    public int remove(int val){
        if (root == null)
            throw new IllegalArgumentException();
        TreeNode current = root, parent = root;
        while (current != null && val != current.val){
            parent = current;
            if (val < current.val) current = current.left;
            else current = current.right;
        }
        if (current == null)
            throw new IllegalArgumentException();

        int result = current.val;
        if (current.left == null){
            if (current == root){
                root = current.right;
            }else if (current == parent.left){
                parent.left = current.right;
            }else {
                parent.right = current.right;
            }
        }else if (current.right == null){
            if (current == root){
                root = current.left;
            }else if (current == parent.left){
                parent.left = current.left;
            }else{
                parent.right = current.left;
            }
        }else{
            TreeNode success = current.left;
            while(success.right != null){
                parent = success;
                success = success.right;
            }
            current.val = success.val;
            if (success == parent.left) parent.left = success.left;
            else success.right = success.left;
        }
        return result;
    }
    public void levelTraverse(){
        if (root == null) return ;
        Queue<TreeNode> queue = new LinkedList<>();
        TreeNode current = root;
        queue.add(current);
        while (!queue.isEmpty()){
            TreeNode node = queue.remove();
            System.out.println(node.val);
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
    }
    public void inorder(){
        inorder(root);
    }
    private void inorder(TreeNode node){
        if (node != null){
            inorder(node.left);
            System.out.println(node.val);
            inorder(node.right);
        }
    }

    public static void main(String[] args) {
        BinaryTree tree = new BinaryTree();
        tree.insert(10);
        tree.insert(5);
        tree.insert(15);
        tree.remove(15);
        tree.levelTraverse();
    }
}
