package dataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree {
	private static class Node {
		String name;
		boolean leaf;
		List<Node> branches;
		List<String> tags;
		public Node(String name, boolean leaf, List<String> tags) {
			this.name=name;
			this.leaf=leaf;
			this.tags = tags;
			branches = new ArrayList<>();
			if(!leaf)
			for(int i=0;i<tags.size();i++) {
				branches.add(null);
			}
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return name+" "+tags;
		}
		
	}

	private Node root;

	public Tree() {
		root = null;
	}
	public boolean isEmpty(){
		return root==null;
	}

	public void add(String name, List<String> types) throws Exception {
		root = addRec(name,false, types, root);
		inserted=false;
	}
	public void add(String name) throws Exception {
		root = addRec(name,true, null, root);
		inserted=false;
	}
	boolean inserted;
	private Node addRec(String name, boolean isRoot, List<String> types, Node node) {
		if(node == null) {
			inserted=true;
			return new Node(name,isRoot,types);
		}
		if(node.leaf)return node;
		int i=0;
		while(!inserted && i<node.tags.size()) {
			Node aux=addRec(name, isRoot, types, node.branches.get(i));
			node.branches.set(i, aux);
			i++;
		}
		return node;
		
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    buildString(sb, "", true,root);
	    return sb.toString();
	}

	private void buildString(StringBuilder sb, String prefix, boolean isTail, Node tree) {
		List<Node> hijos = tree.branches;
	    sb.append(hijos.isEmpty() ? "|----- " : "|----- ").append(tree.name).append(System.lineSeparator());
	    prefix = prefix + ('\t');

	    for (int i = 0; i < hijos.size() - 1; i++) {
	        sb.append(prefix);
	        Node aux = hijos.get(i);
	        buildString(sb, prefix, false,aux);
	    }
	    if (hijos.size() >= 1) {
	        sb.append(prefix);
	        buildString(sb,prefix, true,hijos.get(hijos.size() - 1));
	    }
	}

}
