import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
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
		
		DecisionTreeID dt= new DecisionTreeID();
		
		dt.learnDT("datosTabla.csv");
		
		//no he puesto la ruta del azucar en sangre (tension media) porque va a fallar
		String[] registro= {"Alta","Alto","Alto","No","No"};
		System.out.println("Resultado 1= "+ dt.prediction(registro)); //tiene que ser SI
		
		String[] registro2= {"Alta","Alto","Alto","No","Si"};
		System.out.println("Resultado 2= "+ dt.prediction(registro2)); //tiene que ser SI
		
		String[] registro3= {"Alta","Alto","Alto","Si","Si"};
		System.out.println("Resultado 3= "+ dt.prediction(registro3)); //tiene que ser NO
		
		String[] registro4= {"Baja","Alto","Alto","Si","Si"};
		System.out.println("Resultado 4= "+ dt.prediction(registro4)); //tiene que ser SI
	}

}
