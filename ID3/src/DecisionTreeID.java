import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.directory.AttributeModificationException;


public class DecisionTreeID {
	
	private static int numCol = 0;
	private static int numFilas = 0; //num filas totales
	
	public static ArrayList <String> atributos;//Lista de atributos
	public static List<ArrayList<String>> datos;//tabla de todos los datos
	public static double entropia_general;//entropia general
	public static Set<String> set_clasif;//cjto de datos sobre los que se clasifica.
	public static String nombreNodoAnterior = ""; //Aqui sí es un string que guarda el nombre del nodo anterior y no un nodo en sí mismo
	
	
	public DecisionTreeID() {
		
	}
	
	private  ArrayList<String> atributos (File f) throws FileNotFoundException{
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
	private  List<ArrayList<String>> tablaDatos (File f) throws FileNotFoundException{
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
	
	private  double calculateGeneralEntropy(List<ArrayList<String>> datos, Set<String> set_clasif) { //hacer bonito
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
	
	private  double entropy (Set<String> set_clasif, ArrayList<String> col, String value, ArrayList<String> colClasif, double s) {
		/* set_clasif -> conjunto que contiene los elementos sobre los que se calcula la entropia.(eg. {Si, no})
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
	
	private  double ganancia(ArrayList<String> col, ArrayList<String> colClasif,Set<String> set_clasif, double e_s) {
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
	
	private  Set <String> getSetClasif(ArrayList<String> columna) {
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
	
/*---------------------MARTA------------------------------------------------*/
	/*devuelve la columna de valores que se están teniendo en cuenta segun las filas activas en cada momento
	 * i -> indice de la columna
	 * filas -> filas activas*/
	private  ArrayList<String> columnaActual(int i, Boolean[] filas){
		ArrayList<String> colAct = new ArrayList<String>();
		
		//recorremos filas, el array de bool
		for(int f = 0; f<numFilas; f++){
			if(filas[f]) colAct.add(datos.get(i).get(f));
		}

		return colAct;
	}
	
	/*Devuelve el índice del atributo de máxima ganancia para un cjto de filas y columnas dado*/
	private  int atrMaxGanancia (Boolean[] filas, Boolean[] columnas){
		double maxg,g;
		int atributo;		
		atributo = 0;
		maxg = -1;
		
		ArrayList<String>colActual, colActualdeClasificacion; //columnaActual, las filas para calcular la ganancia.
		colActualdeClasificacion = columnaActual(numCol-1,filas); //reducimos la columna de clasificación a las filas actuales
		
		//System.out.println("Columna actual de clasificacion: "+colActualdeClasificacion.toString());//para comprobar(borrable)
		
		//recorremos todas las columnas excepto la última, que es sobre la cual se clasifica
		for(int col= 0; col<numCol-1; col++){
			if(columnas[col]){
				colActual = columnaActual(col,filas);
				//System.out.println("Atributo "+atributos.get(col)+" "+colActual.toString());//comprobar
				g = ganancia(colActual, colActualdeClasificacion, set_clasif, entropia_general);
				//System.out.println(" Ganancia "+atributos.get(col)+" es "+g);//comprobar
				if(g>maxg){
					maxg = g;
					atributo= col;
				}
				
			}
		}
		
		return atributo;
	}
	//resultado tras eliminar las filas que no tienen el valor del atributo actual igual al valor de la rama
	private  Boolean[] eliminarFilas (Boolean [] filas, String valor, int atributo){
		
		for(int f = 0; f<numFilas; f++){
			if(!datos.get(atributo).get(f).equals(valor)){
				filas[f]=false;
			}
			
		}
		return filas;
	}
	/*indica cuando clasificar: 
	 * 1-cuando para las filas actuales sólo hay uno de los valores a clasificar
	 */
	private  boolean clasificar(Boolean[]filas,Boolean[]columnas){
		int c=0;
		boolean clasificar = false;
		boolean recorrer = true;//para recorrer el array de columnas
		ArrayList<String> colActual = columnaActual(numCol-1, filas);
		Set<String> cjto_clasif = getSetClasif(colActual);
		
		if(cjto_clasif.size()==1)clasificar = true;
		
		/*La segunda condición para comprobar si hay que clasificar antes de escoger el atributo
		 * de máxima ganancia es ver si ya no quedan más atributos para añadir nuevos nodos.
		 */
		if(!clasificar){
			//recorremos el array de bool de columnas excepto la última para ver si hay columnas sin explorar, es decir a true
			while(recorrer&&c<numCol-1){
				if(columnas[c]){
					recorrer=false;//cuando se encuentra una columna no clasificada 
				}
				c++;
			}
			if(recorrer)clasificar = true;
		}
		return clasificar;
	}
	//Devuelve el nombre que se tiene que dar al nodo hoja
	private  String valorNodoHoja(Boolean[]filas, String nodoAnterior){
		String nombreHoja=nodoAnterior;
		
		int f = 0;
		/*mientras que el nombre del nodo anterior igual que la hoja anterior, ya sea vacía o no vacía se busca un nombre distinto
		 * el motivo por el que se busca un nodo de nombre distinto al nombre del nodo anterior es por ejemplo el caso en el que en el nodo X tiene una
		 * rama izda con hoja igual a "Y" y en la rama derecha de X el siguiente atributo de máxima ganancia sólo tiene una rama que directamente
		 * se clasifica y puede hacerlo como "Y" o como otros "valores hoja" "Z","J" . 
		 * Ejemplo Simpsons: cuando en la expansión por la rama izda se llega al nodo interno "peso", éste por la izda el siguiente nodo
		 * es hoja (de valor "H"), al continuar por la rama derecha de "peso", el siguiente (y último atributo que queda por explorar)
		 * es "edad", que en este caso sólo tiene una rama por la que seguir, por lo que este nodo interno "edad" no se pone
		 * y se clasifica directamente, como la clasificación puede ser "H" o "M", escogemos "M", de otra manera si se pone "H"
		 * habría que suprimir el nodo interno "peso" y sustituirlo por un nodo hoja.
		 */
			while(nodoAnterior.equals(nombreHoja)&&f<numFilas){
				if(filas[f])nombreHoja = datos.get(numCol-1).get(f);
				
				f++;
			}
			
		return nombreHoja;
	}
	private String ID3 (Boolean [] filas,Boolean [] columnas,String nodo){
		/*filas-> array de bool de las filas que están "activas" en cada momento
		 * columnas -> array de bool de las columnas "activas"
		 * nodo -> nodo que se instancia en cada momento. En un primer lugar es el nodo raíz que se pasa como nulo
		 * lo he puesto como un string de forma provisional*/
		
		ArrayList<String>columnaActual;
		Boolean [] filas2 = new Boolean[numFilas];
		Boolean [] columnas2 = new Boolean[numCol];		 
		//Caso base, poner un nodo hoja
		if (nodo != null && clasificar(filas,columnas)){
			nodo=valorNodoHoja(filas, nodo);
			System.out.println("Nodo hoja:"+nodo);
		}
		else{
			
			int atributo = atrMaxGanancia(filas,columnas);//se calcula el atributo de máxima ganancia.
			
			System.out.println("\n \n Max g->"+atributos.get(atributo));//imprimir atributo max ganancia
			if (nodo == null){
				nodo=atributos.get(atributo);//Contenido del if provisional, lo que se hace es crear nodo
				System.out.println("Raiz "+nodo);//Imprimir nombre del nodo
				
			}
			else {
				nodo = atributos.get(atributo);//también provisional, se instancia el de maxima ganancia
				System.out.println(nodo);//imprimir nombre del nodo
			}
			columnaActual = columnaActual(atributo,filas);
			//System.out.println("Columna act "+columnaActual.toString());//Comprobar valores de la columna que se está utilizando
			Set<String>valores_atributo = getSetClasif(columnaActual);//conseguir las ramas que tendrá el nodo
			
			//Si valores_atributo > 1 significa que hay más de una rama, sino, es posible clasificar
			if(valores_atributo.size()>1){
				//guardar el valor de filas y columnas para la vuelta atrás
				System.arraycopy(columnas, 0, columnas2, 0,columnas.length);
				System.arraycopy(filas, 0, filas2, 0,filas.length);
				
			//Para cada rama(v nombre de la rama), se añade un nodo hijo
				for(String v: valores_atributo){
					columnas2 [atributo]=false;//eliminar la columna del atributo de máx ganancia puesto
					filas2 = eliminarFilas(filas2,v,atributo);//eliminar las filas que no tienen valor v (que no pertenecen a la rama)
										
					String nodo2="";//crear nuevo nodo que será el hijo,que no es nulo,aun no se sabe si será hoja o no (nodo2)
					//añadir el nodo hijo (nodo2) al nodo (nodo) en la rama de nombre "v"
					System.out.println(atributos.get(atributo)+" -> Rama "+v);
					nodo2 = ID3(filas2,columnas2,nodo2);
					nombreNodoAnterior =nodo2;/*Se guarda el nombre del nodo anterior*/
					
					System.out.println("Vuelta atrás");
					//System.out.println("Nodo ANTERIOR:"+nodo2);//(para comprobaciones)
					//columnaActual = columnaActual(atributo,filas);//para comprobaciones
					//System.out.println("Columna al hacer backtrack "+columnaActual.toString());//comprobaciones
					
					//se recuperan los valores de filas y columnas al volver atrás
					columnas2 = columnas;
					filas2 = filas;
					
				}
			}else{
				//Si sólo hay una rama se clasifica
				System.out.println("Clasificar directamente, nodo hoja->"+valorNodoHoja(filas,nombreNodoAnterior));
			}
		
		}
			//System.out.println("Salir");//Comprobar que sale y vuelve atrás
			return nodo;
	}

	
//------------------------------------Hasta aquí funciones creadas por Marta--------------------------------------------------------------------	
	public void learnDT(String ficheroCSV) throws Exception { //Entrenar
		/*
		 * Crea el árbol de decisión a partir del dataset contenido en el 
		 * fichero cuyo nombre se le pasa como argumento. 
		 * f -> dataSet en formato .csv
		 */
		
		File f = new File(ficheroCSV); //Abrir dataset

		atributos = atributos(f);
		datos = tablaDatos(f);
		
		System.out.println("Nuestra tabla de datos: ");	
		//Primera fila despreciable, por ello, la elimino
		datos.remove(0);
		System.out.println(atributos);
		for (ArrayList<String> col : datos) {
			System.out.println(col);
		}	
		
		//1.- Determino el conjunto de elementos sobre los que clasifico (eg. {si,no})
		numCol = datos.size();
		
		//Añadido -------------------------
		numFilas = datos.get(0).size();
		set_clasif = getSetClasif(datos.get(numCol-1));
		
		//MARTA: INICIALIZAR ARRAYS DE BOOLEAN FILAS Y COLUMNAS;---------
		Boolean [] columnas= new Boolean[numCol];
		Boolean [] filas = new Boolean[numFilas];
		
		for (int i = 0; i<numCol; i++){
			columnas[i]=true;
		}
		for(int i=0; i<numFilas; i++){
			filas[i]=true;
		}
		//----------------------
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
		//-----------------
		entropia_general = e_s;
		int atributo = atrMaxGanancia(filas,columnas);

		
		//----------------
		
		//Muestro atributos y debajo la ganacia de cada atributo.
		System.out.println();
		System.out.println(atributos);
		System.out.println(ganancias);
		
		//-----------comprobaciones
		System.out.println(atributo+"--> "+ atributos.get(atributo));
		//----------- cuando esté lista la estructura del arbol se llama a la función ID3
		String nodo=null;//EL Objeto nodo inicial es nulo
		nodo= ID3(filas, columnas, nodo);
		System.out.println("NODO RAIZ"+nodo);
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
			DecisionTreeID dt = new DecisionTreeID();
			dt.learnDT(filename);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
