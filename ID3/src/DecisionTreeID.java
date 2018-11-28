import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


public class DecisionTreeID {
	
	private static int numCol = 0;
	public DecisionTreeID() {

	}
	
	private static ArrayList<String> atributos (File f) throws FileNotFoundException{
		/*
		 * Como la primera línea del fichero solo tiene valor semántico, la guardo aparte.
		 * f -> fichero de entrada csv.
		 */
		String p = "";
		ArrayList<String> atributos = new ArrayList<>();
		try (Scanner sc = new Scanner(f)){				
			if(sc.hasNextLine()) {
				p = sc.nextLine(); 
				try(Scanner s = new Scanner(p).useDelimiter("[;]")){		
					if(s.hasNext())
						s.next(); //Primera columna despreciable ({Pacientes}, no la tengo en cuenta
					while(s.hasNext()) { 
						//Leo parámetros uno a uno y lo guardo (eg. [Paciente, Presion Arterial, ...])					
						atributos.add(s.next()); 
					}
				}
			}
		}
		return atributos;
	}
	private static List<ArrayList<String>> tablaDatos (File f) throws FileNotFoundException{
		/*
		 * f -> fichero csv de entrada, el cual tiene la tabla de datos que se utilizara para hacer el algoritmo ID3.
		 * Leo el fichero y devuelvo una lista de listas(columnas de la tabla)
		 */
		String p = "";
		List<ArrayList<String>> filas = new ArrayList<ArrayList<String>>(); 
		ArrayList<String> columnas;
				
		try (Scanner sc = new Scanner(f)){	
			
			if(sc.hasNextLine()) {
				sc.nextLine(); //id
			}
			
			while(sc.hasNextLine()) { //Leo cada fila, cada linea
				//identifico las columnas
				p = sc.nextLine();
				try(Scanner s = new Scanner(p).useDelimiter("[;]")){//elemento entre el separador ;
					columnas = new ArrayList<String>();
					while(s.hasNext()) { //leo columnas de las filas
						columnas.add(s.next()); 
					}
					
					filas.add(columnas);					
				}	
			}			
		} 
		//Al leer el fichero, no me queda otra que leer por filas, por eso cambio mi estructura a un array de columnas para facilitarme la vida.
		int size = filas.get(0).size();
		List<ArrayList<String>> datos = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < size; i++) {			
			ArrayList<String> col = new ArrayList<>();
			for (ArrayList<String> list : filas) {
				col.add(list.get(i)); //columna				
			}
			datos.add(col);			
		}
		//cambio las filas por las columnas porque conviene tener cada columna separada y antes cuando leia el fichero no lo podia hacer
		return datos;
	}
	
	private static double calculateGeneralEntropy(List<ArrayList<String>> datos, Set<String> set_clasif) { //hacer bonito
		/*
		 * datos -> Lista de columnas, cojo la última columna y calculo la entropía general
		 * set_clasif -> conjunto que contiene los elementos sobre los que se calcula la entropia.(eg. {Si, no})
		 */
		
		//1.- Cuento el numero de elementos según los valores clasificadores. (eg {Sí = sv{1}, No = sv{2}})
		//int numCol = datos.size();
		ArrayList<Double> sv = new ArrayList<>();		
		for(String c : set_clasif) {
			double num = 0.0;
			for (String p : datos.get(numCol-1)) {
				if(p.equalsIgnoreCase(c))
					num += 1.0;
			}
			sv.add(num);
		}
		double e = 0.0;
		double s = datos.get(0).size();
		for(Double d : sv)
			e += (d/s) * (Math.log(d/s)/Math.log(2));
		e = (-1)*e;
		return e;
	}
	
	private static double entropy (Set<String> set_clasif, ArrayList<String> col, String value, ArrayList<String> colClasif, double s) {
		/* colClasif  -> columna sobre la cual se calculan las entropias, en este caso la ultima. (eg. Administrar Farmaco F)
		 * set_clasif -> conjunto que contiene los elementos sobre los que se calcula la entropia.(eg. {Si, no})
		 * value      -> elemento sobre el que calculo la entropia. (eg. Alta, Baja, Media)
		 * colClasif  -> columna sobre la cual se calculan las entropias, en este caso la ultima. (eg. Administrar Farmaco F)
		 * s          -> numero de elementos sobre el que calculo la entropia. (eg. Alta = 6)
		 */
		double e = 0.0;
		for(String clasif : set_clasif) {
			int i = 0;
			double sv = 0.0;
			for(String element : col) {
				if(element.equalsIgnoreCase(value) && clasif.equalsIgnoreCase(colClasif.get(i))) 					
					sv += 1.0;
				i++;
			}
			if(sv == 0.0)
				e += 0.0;
			else
				e += (sv/s)*(Math.log(sv/s) / Math.log(2));
		}
		e = (-1)*e;		
		return e;
	}
	
	private static double ganancia(ArrayList<String> col, ArrayList<String> colClasif,Set<String> set_clasif, double e_s) {
		/*
		 * col        -> columna de la cual calculo la ganancia. (eg. Presion Alta)
		 * colClasif  -> columna sobre la cual se calculan las entropias, en este caso la ultima. (eg. Administrar Farmaco F)
		 * set_clasif -> conjunto que contiene los elementos sobre los que se calcula la entropia.(eg. {Si, no})
		 * e_s        -> entropia general.
		 */
		//1. Filtro los elementos de la columna sobre los que clasifico, para contarlos luego para la entropia. (eg. Presion Arterial = {Alta, Media, Baja})
		Set <String> col_values = getSetClasif(col);
		
		//2. Itero sobre col_values para calcular las entropias para calcular la ganancia (eg. E(Alta|{si,no}))
		ArrayList<Double> entropias = new ArrayList<>(); //Lista donde guardo las entropias
		double e = 0.0; //Variable auxiliar que uso para guardar la entropia momentaneamente para calcular la ganancia dinamicamente.
		double size = col.size();
		double g = 0.0; //Variable donde calculo la ganacia dinamicamente.
		for (String value : col_values) { //{Alta, Media, Baja}
			double s = 0.0; 
			for (String string : col) {
				if(string.equalsIgnoreCase(value))
					s += 1.0;
			}
			e = entropy(set_clasif, col, value, colClasif, s);
			entropias.add(e); //Guardo las entropias, borrar luego pq no lo necesito **********
			g += (s/size)*e;
		}
		//3. Completo el calculo de la ganancia.
		g = e_s - g;
		
		return g;		
	}
	
	private static Set <String> getSetClasif(ArrayList<String> columna) {
		/*
		 * columna -> columna de la cual quiero calcular la entropía. (eg. Presión Arterial o Colesterol)
		 * returns => los elementos que necesito para calcular la entropía. (eg. {Alta, Media, Baja})
		 */
		Set <String> values = new TreeSet<>();		
		for (String s : columna) {
			values.add(s);
		}
		return values;
	}

	public static void learnDT(String ficheroCSV) throws Exception { //Entrenar
		/*
		 * Crea el árbol de decisión a partir del dataset contenido en el 
		 * fichero cuyo nombre se le pasa como argumento. 
		 * f -> dataSet en formato .csv
		 */
		
		File f = new File(ficheroCSV); //Abrir dataset

		ArrayList<String> atributos = atributos(f);
		List<ArrayList<String>> datos = tablaDatos(f);
		
		System.out.println("Nuestra tabla de datos: ");	
		//Primera fila despreciable, por ello, la elimino
		datos.remove(0);
		System.out.println(atributos);
		for (ArrayList<String> col : datos) {
			System.out.println(col);
		}	
		
		//1.- Determino el conjunto de elementos sobre los que clasifico (eg. {si,no})
		numCol = datos.size();
		Set<String> set_clasif = getSetClasif(datos.get(numCol-1));
		//2.- Calculo la entropia general E(S)
		double e_s = calculateGeneralEntropy(datos, set_clasif);
		//3.- Calculo ganancias
		ArrayList<Double> ganancias = new ArrayList<>();
		double g = 0.0;
		for (ArrayList<String> col : datos) {
			if(!col.equals(datos.get(numCol-1))) {
				g = ganancia(col, datos.get(numCol-1),  set_clasif, e_s);
				ganancias.add(g);
			}
				
		}
		
		//Muestro atributos y debajo la ganacia de cada atributo.
		System.out.println();
		System.out.println(atributos);
		System.out.println(ganancias);
		
		/*
		 * De ahora en adelante toca el algoritmo ID3.
		 * Coger de "ganancias" el valor más grande y asociarlo al atributo.
		 * Yo creo que iterando sobre "ganancias" con for normal,  se puede guardar el índice del mayor,
		 * en este caso i=0 de ganancias nos da la mayor ganancia.
		 * Este valor está asociado al atributo de la columna i de "datos".
		 * 
		 * Until here, if there is any question, ask Iman.
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


	public static void main(String[] args) throws Exception {
		String filename = "datosTabla.csv";
		try {
			learnDT(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
