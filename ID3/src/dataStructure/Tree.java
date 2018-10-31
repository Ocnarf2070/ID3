package dataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree {
	private static class Node {
		String nombre;
		List<Node> ramas;
		List<String> etiqueta;
		public Node(String nombre) {
			this.nombre=nombre;
			ramas = new ArrayList<>();
			etiqueta = new ArrayList<>();
		}
		public void add(Node hijo, String etiqueta) {
			ramas.add(hijo);
			this.etiqueta.add(etiqueta);
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return nombre+"<"+etiqueta+">";
		}
		
	}
	
	private Node raiz;
	
	public Tree(String nombre) {
		raiz=new Node(nombre);
	}	
	
	public void add(String padre, String nombre, String etiqueta) throws Exception {
		if(search(nombre)!=null) throw new Exception("Ya existe el nodo");
		Node nodo = new Node(nombre);
		Node father = search(padre);
		if(father==null) throw new Exception("No existe el padre");
		father.add(nodo, etiqueta);
	}

	public Node search(String nombre) {
		return searchRec(raiz,nombre);
	}

	private Node searchRec(Node arbol, String nombre) {
		if(arbol.nombre.equals(nombre))return arbol;
		else {
			Node aux=null;
			Iterator<Node> ramas = arbol.ramas.iterator();
			while(ramas.hasNext()&&aux==null) {
				aux=searchRec(ramas.next(),nombre);
			}
			return aux;
		}
		
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    buildString(sb, "", true,raiz);
	    return sb.toString();
	}

	private void buildString(StringBuilder sb, String prefix, boolean isTail, Node tree) {
		List<Node> hijos = tree.ramas;
	    sb.append(hijos.isEmpty() ? "|----- " : "|----- ").append(tree.nombre).append(System.lineSeparator());
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
