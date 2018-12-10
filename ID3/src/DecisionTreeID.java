import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.directory.AttributeModificationException;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import dataStructure.Tree;


public class DecisionTreeID {
	
	private static int numCol = 0;
	private static int numFilas = 0; //num filas totales
	
	public static ArrayList <String> atributos;//Lista de atributos
	public static List<ArrayList<String>> datos;//tabla de todos los datos
	public static double entropia_general;//entropia general
	public static Set<String> set_clasif;//cjto de datos sobre los que se clasifica.
	public static String nombreNodoAnterior = ""; //Aqui sí es un string que guarda el nombre del nodo anterior y no un nodo en sí mismo
	public static Tree arbol;
	
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
		
		ArrayList<String>colActual, colActualdeClasificacion; 
		colActualdeClasificacion = columnaActual(numCol-1,filas); //clasificación con las filas actuales
		
		//recorremos todas las columnas excepto la última, que es sobre la cual se clasifica
		for(int col= 0; col<numCol-1; col++){
			if(columnas[col]){
				colActual = columnaActual(col,filas);//columna de la cual se calcula la ganancia
				g = ganancia(colActual, colActualdeClasificacion, set_clasif, entropia_general);
				
				if(g>maxg){
					maxg = g;
					atributo= col;
				}				
			}
		}		
		return atributo;
	}
	
	private  Boolean[] eliminarFilas (Boolean [] filas, String valor, int atributo){
		
		for(int f = 0; f<numFilas; f++){
			if(!datos.get(atributo).get(f).equals(valor)){
				filas[f]=false;
			}
			
		}
		return filas;
	}
	//indica cuando clasificar dadas unas filas y unas columnas 
	private  boolean clasificar(Boolean[]filas,Boolean[]columnas){
		int c=0;
		boolean clasificar = false;
		boolean recorrer = true;//para recorrer el array de columnas
		ArrayList<String> colActual = columnaActual(numCol-1, filas);
		Set<String> cjto_clasif = getSetClasif(colActual);
		
		//1-cuando para las filas actuales sólo hay uno de los valores a clasificar
		if(cjto_clasif.size()==1)clasificar = true;
		
		// 2- No quedan más atributos (columnas a TRUE) para añadir nuevos nodos.
		if(!clasificar){
			while(recorrer&&c<numCol-1){
				if(columnas[c]){
					recorrer=false; 
				}
				c++;
			}
			if(recorrer){
				clasificar = true;
				System.out.println("Hola");
			}
		}
		return clasificar;
	}
	
	//Devuelve el nombre que se tiene que dar al nodo hoja
	private  String valorNodoHoja(Boolean[]filas, String nodoAnterior){
		Boolean hayNodoHoja=false;
		String v,nombreHoja="";	
		int numValores,valorestot,nv,indNhoja,j;
		
		ArrayList<String> colActual = columnaActual(numCol-1, filas);
		Set<String> cjto_clasif = getSetClasif(colActual);
		numValores = cjto_clasif.size();
		List<String> vals =new ArrayList<String>(cjto_clasif);
		Integer numVals[] = new Integer [vals.size()];
		
		
		if(numValores==1)nombreHoja = vals.get(0);//si sólo hay un valor diferente
		else{
			//si hay mas de uno, se coge el de mayor porcentaje
			valorestot=0;
			for(int vi = 0;vi<numVals.length;vi++){
				numVals[vi]=0;
			}
			for(int i = 0; i<filas.length;i++){
				if (filas[i]){
					valorestot++;
					v = datos.get(numCol-1).get(i);
					j = 0;
					while (j< vals.size()){
						if(v.equals(vals.get(j)))numVals[j]++;
						j++;
					}
				}
			}			
			nv = 0;
			indNhoja=0;
			double porcent, mayorPorcent = 0;
			while(nv<numVals.length&& !hayNodoHoja){
				porcent=(numVals[nv]/valorestot);
				if(porcent>=mayorPorcent){
					mayorPorcent = porcent;
					indNhoja=nv;
					if((porcent>0.5))hayNodoHoja = true;					
				}
				nv++;
			}			
			nombreHoja=vals.get(indNhoja);
		}
		return nombreHoja;
	}
	private Tree ID3 (Boolean [] filas,Boolean [] columnas,Tree arbol) throws Exception{		
		ArrayList<String>columnaActual;
		Boolean [] filas2 = new Boolean[numFilas];
		Boolean [] columnas2 = new Boolean[numCol];
		Set<String>valores_atributo;
		List<String> lista_valores;
		String nomnodo;
		int atributo;
			
			atributo = atrMaxGanancia(filas,columnas);//se calcula el atributo de máxima ganancia.
			columnaActual = columnaActual(atributo,filas);
			valores_atributo = getSetClasif(columnaActual);//conseguir las ramas que tendrá el nodo			
			
			if (arbol == null){
				arbol = new Tree();//crear arbol
			}
			
			if(valores_atributo.size()>1){
				nomnodo = atributos.get(atributo);
				lista_valores = new ArrayList<String>(valores_atributo);
				arbol.add(nomnodo, lista_valores);
				nombreNodoAnterior="";
			}	
			//Para cada rama(v nombre de la rama), se añade un nodo hijo
			System.arraycopy(columnas, 0, columnas2, 0,columnas.length);
			System.arraycopy(filas, 0, filas2, 0,filas.length);
				for(String v: valores_atributo){
					columnas2 [atributo]=false;//eliminar la columna del atributo de máx ganancia puesto
					filas2 = eliminarFilas(filas2,v,atributo);//eliminar las filas que no tienen valor v (que no pertenecen a la rama)
															
					if(clasificar(filas2,columnas2)){//Caso base, poner un nodo hoja
						nomnodo=valorNodoHoja(filas2, nombreNodoAnterior);
						arbol.add(nomnodo);
						nombreNodoAnterior = nomnodo;
					}else{
						arbol = ID3(filas2,columnas2, arbol);
					}					
					//se recuperan los valores de filas y columnas al volver atrás
					System.arraycopy(columnas, 0, columnas2, 0,columnas.length);
					System.arraycopy(filas, 0, filas2, 0,filas.length);					
				}			
			return arbol;
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
		entropia_general = calculateGeneralEntropy(datos, set_clasif);
		//3.- Calculo ganancias
		ArrayList<Double> ganancias = new ArrayList<>();
		double g = 0.0;
		for (ArrayList<String> col : datos) {
			if(!col.equals(datos.get(numCol-1))) {
				g = ganancia(col, datos.get(numCol-1),  set_clasif, entropia_general);
				ganancias.add(g);
			}
				
		}
				
			
		//----------- cuando esté lista la estructura del arbol se llama a la función ID3
		
		arbol = null;//EL Objeto nodo inicial es nulo
		arbol = ID3(filas, columnas, arbol);
		this.drawDecisionTree();
		System.out.println(arbol.toString());
	}
	

	public void drawDecisionTree() { //Mostrar
		JFrame frame= new JFrame("Salida");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTextArea texto=new JTextArea(arbol.toString()); 
		texto.setEditable(false);
		texto.setFont(texto.getFont().deriveFont(20f));
		
		frame.getContentPane().add(texto,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		//System.out.print(arbol.toString());
	}

	public Object prediction (String[] registroCVS) { //Predecir
		/*
		 * devuelve la clase a la que pertenece el registro que queremos predecir. El registro y el fichero tienen que
		 * tener las variables en el mismo orden. 
		 */
		/*recibo la lista de datos en el orden de las variables
		 * tengo la lista de variables (atributos), le puedo "asignar a cada dato su variable"
		 * el nombre del nodo es el nombre del atributo que se está calculando o la respuesta, que es set_clasif
		 * algoritmo: bajar en la i rama del atributo del nodo para el i valor, el i valor es la i tag del nodo (el segundo hijo del nodo es el de la segunda tag)
		 * 	para el nodo actual, busco el numero de su atributo en atributos[], ese es el número
		 * 	del valor para ese atributo en registro[], con el valor de registro busco el mismo
		 * 	String en tag, la posicion del String en tag es la rama por la que tengo que tirar
		 * 	CB/final= el Nodo pertenece a set_clasif, podria decir que si es una hoja pero no veo como esta hecho bool leaf en tree y esto sirve
		 * la que voy a liar con tal de no poner "Nodo" publico
		 * */
		String res= arbol.recorrerArbol(registroCVS,atributos,set_clasif);
		return res;

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
