package dataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	    buildString(sb, "", true,root,"");
	    return sb.toString();
	}

	private void buildString(StringBuilder sb, String prefix, boolean isTail, Node tree, String opcion) {
		List<Node> hijos = tree.branches;
	    sb.append(hijos.isEmpty() ? "|-----" : "|-----").append(opcion + "-----").append(tree.name).append(System.lineSeparator());
	    prefix = prefix + ('\t');
	    //Las iniciales para ponerlas delante de opcion => tension alta
	    String s=tree.name;
	    String iniciales=""+s.charAt(0);
	    for(int i=0;i<s.length()-1;i++) {
			if(Character.isWhitespace(s.charAt(i))) {
				iniciales=iniciales+(s.charAt(i+1));
			}
		}
	    
	    for (int i = 0; i < hijos.size(); i++) {
	        sb.append(prefix);
	        Node aux = hijos.get(i);
	        StringBuilder et= new StringBuilder();
	        et.append("");
	        
	        if(tree.tags!=null) {
	        	et.append(iniciales + "=>" + tree.tags.get(i));
	        }
	        
	        buildString(sb,prefix, false,aux,et.toString());
	    }
	   
	}
	
	public String recorrerArbol(String[] registro,ArrayList<String> atributos,Set<String> hojas) {
		Node actual= root;
		int indiceAt=0;
		while(!hojas.contains(actual.name)) {
			for(int i=0;i<atributos.size();i++) {
				if(actual.name.compareTo(atributos.get(i))==0) {
					indiceAt=i;
					break;
				}
			}
			String valorAt= registro[indiceAt];
			
			List<String> pValores= actual.tags;
			
			for(int i=0; i<pValores.size(); i++) {
				if(pValores.get(i).compareTo(valorAt)==0) {
					actual=actual.branches.get(i);
					break;
				}
			}
		}
		return actual.name;
	}

}
