

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DecisionTreeID {

	public DecisionTreeID() {
		
		
	}
	
	public void learnDT(String ficheroCVS) throws IOException { //Entrenar
		/*crea el arbol de decisión a partir del dataset 
		 * contenido en el fichero cuyo nombre se le pasa como argumento. 
		 */
		File f = new File(ficheroCVS); //Abrir dataset
		FileReader fr = new FileReader (f);
        BufferedReader br = new BufferedReader(fr);
        String linea; //separamos con coma y linea, guardamos en tabla
        while((linea=br.readLine())!=null)//leer dataset
           System.out.println(linea);
        
        
        
        /* 1. Lectura del fichero CSV
         * 2. calculo estadistico
         * 3. Generacion del arbol -> etiquetado !!!!!
         * 4. Generacion del propio arbol
         * 5. 
         */
		
	}
	
	public void drawDecisionTree() { //Mostrar
		/*
		 * dibuja el árbol. 
		 */
	}
	
	public Object prediction (String[] registroCVS) { //Predecir
		/*
		 * devuelve la clase a la que pertenece el registro que queremos predecir. El registro y el fichero tienen que
         * tener las variables en el mismo orden. 
		 */
		
		
		return registroCVS;
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
