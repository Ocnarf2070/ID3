import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataStructure.Tree;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Tree arbol = new Tree();
		String [] one = {"a"};
		String [] two = {"a","b"};
		String [] three = {"a","b","c"};
		arbol.add("0", Arrays.asList(two));
		arbol.add("2", Arrays.asList(three));
		arbol.add("4", Arrays.asList(two));
		arbol.add("9");
		arbol.add("10");
		arbol.add("8",Arrays.asList(one));
		arbol.add("1");
		arbol.add("2", Arrays.asList(three));
		arbol.add("7");
		arbol.add("6");
		arbol.add("5");
		arbol.add("3");
		
		System.out.println(arbol);
	}

}
