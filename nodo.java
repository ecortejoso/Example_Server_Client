
import java.util.*;
import java.io.*;
import java.net.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import nanoxml.*;
//import java.util.Iterator;

 
/**
 *
 * @author Eduardo Cortejoso y Carlos Farias
 */
public class nodo { 

    static List<Track> tracks = new ArrayList<Track>();
    static List<String> conocidos_list = new ArrayList<String>();
    static boolean salir = false;

    /*
     * Clase Node
     *
     * @param puerto: Numero del puerto a que debe conectarse
     * @param conocidos: path del archivo de texto que especificara los id de nodos conocidos por este nodo
     * @param biblioteca: path del archivo xspf que especifica la biblioteca local
     * @param id: Nombre del host o direccion ip
     *
     */

    public static class Node{
        public int puerto;
        public String id;
	public Socket socket;

        Node(int p, String i){
            puerto = p;
            id = i;
            try{
                System.out.println("Conectandose al servidor "+i+", por favor espere un momento...\n");
                socket = new Socket(i, p);
            } catch (IOException e){
                System.out.println("Error en conexion de socket a "+p+" "+i+" : "+e);
                puerto = -1;
            }
        };

    }

    /*
     * Clase Client
     *
     * @param puerto: Numero del puerto a que debe conectarse
     * @param nodo: Nombre del host o direccion ip del nodo al cual se conectara
     * @param descargas: path del directorio en donde seran almacenado las descargas
     *
     */

    public static class Client{
        public int puerto;
        public String nodo;
        public String descargas;
        

        Client(int p, String n, String d){
            puerto = p;
            nodo = n;
            descargas = d;
        };

    }

    /*
     * Clase Track
     *
     * @param location: path del track especificado
     * @param title: titulo del track
     * @param creator: autor o creador del track     
     *
     */

    public static class Track{
        public String location;
        public String title;
        public String creator;        


        Track(String l, String t, String c){
            location = l;
            title = t;
            creator = c;            
        };

    }

    /**
    * Procedimiento encargado de eliminar los espacios de un String.
    *
    * @param s String al cual se le removeran los espacios.
    * @return String sin espacios.
    */

    public static String removeSpaces(String s) {
        StringTokenizer st = new StringTokenizer(s," ",false);
        String t="";
        while (st.hasMoreElements()) t += st.nextElement();
        return t;
    }

    /**
    * main
    *
    * @param args Parametros de entrada, son: el puerto, el id del nodo asociado y el archivo de nodos conocidos y el archivo de tracklist xspf
    */

    public static void main(String[] args) throws FileNotFoundException, IOException {
        int puerto_serv = 16000;
        String xmlfile="";
        String confile="";
        String node_id="";
        FileReader reader;
        Vector<XMLElement> playlist;
        Vector<XMLElement> tracklist;
        Vector<XMLElement> attributes;
        Track pista;
        XMLElement xml;
        ServerSocket server = null;
        Socket clientSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;        
        String clientRequest;
        String[] entradaCliente;
        String cadena;
        // Intenta recibir los parametros
        try{
            if (args[0].equals("-p")) puerto_serv = Integer.parseInt(args[1]);
            else if(args[0].equals("-c")) confile = args[1];
            else if(args[0].equals("-b")) xmlfile = args[1];
            else if(args[0].equals("-i")) node_id = args[1];
            else {
                System.out.println("Formato de llamada incorrecta");
                System.exit(0);
            }

            if (args[2].equals("-p")) puerto_serv = Integer.parseInt(args[3]);
            else if(args[2].equals("-c")) confile = args[3];
            else if(args[2].equals("-b")) xmlfile = args[3];
            else if(args[2].equals("-i")) node_id = args[3];
            else {
                System.out.println("Formato de llamada incorrecta");
                System.exit(0);
            }

            if (args[4].equals("-p")) puerto_serv = Integer.parseInt(args[5]);
            else if(args[4].equals("-c")) confile = args[5];
            else if (args[4].equals("-b")) xmlfile = args[5];
            else if (args[4].equals("-i")) node_id = args[5];
            else {
                System.out.println("Formato de llamada incorrecta");
                System.exit(0);
            }

            if (args[6].equals("-p")) puerto_serv = Integer.parseInt(args[7]);
            else if(args[6].equals("-c")) confile = args[7];
            else if(args[6].equals("-b")) xmlfile = args[7];
            else if(args[6].equals("-i")) node_id = args[7];
            else {
                System.out.println("Formato de llamada incorrecta");
                System.exit(0);
            }
            


        } catch(ArrayIndexOutOfBoundsException e)  {
            System.out.println("Llamado a nodo incorrecto");
            System.exit(0);
        }        

        if(args.length > 8){
            System.out.println("\n Numero excesivo de argumentos\n");
            System.exit(0);
        } 


        // Lectura de archivo de conocidos
        FileInputStream fstream = new FileInputStream(confile);
        DataInputStream incom = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(incom));
        String strLine;
        while ((strLine = br.readLine()) != null){            
            conocidos_list.add(strLine);
        }

        // Lectura del archivo xspf especificado
        xml = new XMLElement();
        reader = new FileReader(xmlfile);
        String location = "";
        String title = "";
        String creator = "";
        xml.parseFromReader(reader);        
        playlist = xml.getChildren();
        Enumeration<XMLElement> si = playlist.elements();
        XMLElement tl =(XMLElement)si.nextElement();
        tracklist = tl.getChildren();
        Enumeration<XMLElement> s = tracklist.elements();
        while(s.hasMoreElements()) {            
            XMLElement track=(XMLElement)s.nextElement();
            attributes = track.getChildren();
            Enumeration<XMLElement> sf = attributes.elements();
            while(sf.hasMoreElements()) {
                XMLElement child=(XMLElement)sf.nextElement();
                String ele_name = child.getName();                
                if(ele_name.equals("location")){                    
                    location = child.getContent();                    
                } else if(ele_name.equals("title")){                    
                    title = child.getContent();                    
                } else if(ele_name.equals("creator")){                    
                    creator = child.getContent();                    
                }
                /*
                XMLElement location = (XMLElement)track.getChildren().elementAt(0);
                XMLElement title = (XMLElement)track.getChildren().elementAt(1);
                XMLElement creator = (XMLElement)track.getChildren().elementAt(2);
                XMLElement album = (XMLElement)track.getChildren().elementAt(3);
                XMLElement extension = (XMLElement)track.getChildren().elementAt(4);
                XMLElement genre = (XMLElement)extension.getChildren().elementAt(0);
                */

            }
            pista = new Track(location, title, creator);            
            tracks.add(pista);
        }

        // Creacion de streams necesario para cumplir funciones de servidor
                try{
                    server = new ServerSocket(puerto_serv);
                    clientSocket = server.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);                    
                } catch (IOException e){
                    System.out.println("Excepcion en la construccion del servidor "+node_id+":"+e);
                }

        // Ciclo de espera a peticiones del cliente
        while(!salir){            

            try{
                clientRequest = in.readLine();
                entradaCliente = clientRequest.split(" ");


                // -----------------------------------------------------------------------------------------------
                // ---------------------- Caso de recibir una consulta de nodos alcanzables ----------------------
                // -----------------------------------------------------------------------------------------------


                if(entradaCliente[0].equals("a")){
		    if(entradaCliente[1].equals("c")){
			String answer = node_id;
                        boolean visited_node=false;
		        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
			String respuesta = "";
		        String[] r_final;
                        String[] r_answer;
        	        while(iterador_conocidos.hasNext()){			
        	            String con_act = (String) iterador_conocidos.next();  
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            r_answer = answer.split(" ");
                            for(int i=0;i<r_answer.length;i++){
                                if(removeSpaces(r_answer[i]).equals(removeSpaces(con_act))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){
                                Node nodo_act = new Node(puerto_serv, con_act);				
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);
                                    out_a.println("a n "+answer);
                                    respuesta = in_a.readLine();                                    
                                    r_final = respuesta.split(" ");                                        
                                    for(int j=0; j<r_final.length; j++){
                                        answer = answer+" "+r_final[j];
                                    }                                        
                                    nodo_act.socket.close();                                        
                                }                                
                            }                                                    
        	        }
        	        out.println(answer);
                        
		    } else if(entradaCliente[1].equals("n")){
			String answer = node_id;
                        boolean visited_node=false;
                        String entrada = "";
			Iterator<String> iterador_conocidos = conocidos_list.listIterator();
			String respuesta = "";
			String[] r_final;                        
                        for(int i=2; i<entradaCliente.length; i++){
                            entrada = entrada+entradaCliente[i]+" ";
                        }                        
			while(iterador_conocidos.hasNext()){
        	            String con_act = (String) iterador_conocidos.next();

			    for(int i=2; i<entradaCliente.length; i++){                                
				if(removeSpaces(entradaCliente[i]).equals(removeSpaces(con_act))){
                                    visited_node=true;
                                }
                            }
                            if(!visited_node){
                                Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);
                                    out_a.println("a n "+answer+" "+entrada+" "+con_act);
                                    respuesta = in_a.readLine();
                                    r_final = respuesta.split(" ");
                                    for(int j=0; j<r_final.length; j++){
                                        answer = answer+" "+r_final[j];
                                    }
                                }                                    
                                nodo_act.socket.close();                                    				
			    }                                                                                   
			}                        
                        out.println(answer);

		    }


                // --------------------------------------------------------------------------
                // ---------------------- Caso de recibir una consulta ----------------------
                // --------------------------------------------------------------------------


                } else if(entradaCliente[0].equals("c")){
                    Iterator<Track> iterador_tracks = tracks.listIterator();
                    boolean found = false;
                    String answer = "";
                    String result = "";
                    int count=1;
                    String countstring = "";
                    cadena = "";



                    // ----------------------------------------------------------------------------------
                    // ---------------------- Caso de recibir una consulta general ----------------------
                    // ----------------------------------------------------------------------------------


                    if(entradaCliente.length == 1){                                            
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        boolean visited_node=false;
                        String visited = node_id;

                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            countstring = Integer.toString(count);
                            count++;
                            answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";
                        }                        
                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split(" ");
                            String[] result_list;
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            for(int i=0;i<visit_list.length; i++){                                
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){
                                Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);
                                    out_a.println("c n "+count+" "+visited);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    visited = visited +" "+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();                                
				}
                            }
                        }
                        out.println(answer);


                    } else if(entradaCliente[1].equals("n")){                        
                        String visited = node_id;                        
                        boolean visited_node=false;                        
                        count = Integer.parseInt(entradaCliente[2]);
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            countstring = Integer.toString(count);
                            count++;
                            answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";
                        }                        
                        for(int i=3;i<entradaCliente.length;i++){
                            visited = visited+" "+entradaCliente[i];
                        }
                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split(" ");
                            String[] result_list;
                            for(int i=0; i<visit_list.length;i++){                                
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){
                                Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);                                    
                                    out_a.println("c n "+count+" "+visited);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    visited = visited+" "+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();
				}
                            }
                        }
                        out.println(answer+"__"+visited);

                    // -------------------------------------------------------------------------------------
                    // ---------------------- Caso de recibir una consulta por titulo ----------------------
                    // -------------------------------------------------------------------------------------

                        
                    } else if (entradaCliente[1].equals("-t")) {                        
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        boolean visited_node=false;
                        String visited = node_id;

                        for(int i=2; i<entradaCliente.length; i++){
                            cadena = cadena + entradaCliente[i] + " ";
                        }
                        cadena = cadena.toLowerCase();
                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            String cad1 = removeSpaces(cadena);
                            String cad2 = removeSpaces(cur_track.title.toLowerCase());
                            if(cad1.equals(cad2)){
                                countstring = Integer.toString(count);
                                count++;
                                found = true;
                                answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";                               
                            }
                        }
                        
                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split("%%");
                            String[] result_list;
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            for(int i=0; i<visit_list.length;i++){
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){                                
                                Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);                                
                                    out_a.println("c nt -t "+count+" "+visited+" "+cadena);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    visited = visited+"%%"+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();
				}

                            }
                        }
                        
                        if(found == true){
                            out.println(answer);
                        } else {
                            out.println("Fail");
                        }

                    } else if (entradaCliente[1].equals("nt")) {                        
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        boolean visited_node=false;
                        String visited = node_id+"%%"+entradaCliente[4];
                        count = Integer.parseInt(entradaCliente[3]);

                        for(int i=5; i<entradaCliente.length; i++){
                            cadena = cadena + entradaCliente[i] + " ";
                        }
                        cadena = cadena.toLowerCase();
                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            String cad1 = removeSpaces(cadena);
                            String cad2 = removeSpaces(cur_track.title.toLowerCase());
                            if(cad1.equals(cad2)){
                                countstring = Integer.toString(count);
                                count++;
                                found = true;
                                answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";
                            }
                        }                        

                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split("%%");
                            String[] result_list;
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            for(int i=0; i<visit_list.length;i++){
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){
                                    Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);
                                    out_a.println("c nt -t "+count+" "+visited+" "+cadena);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    if(!result_list[0].equals("Fail")){
                                        found = true;
                                    }
                                    visited = visited+"%%"+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();
				}
                            }
                        }
                        if(found == true){
                            out.println(answer+"__"+visited);
                        } else {
                            out.println("Fail__"+visited);
                        }
                        
                        
                        
                        
                        
                    // ------------------------------------------------------------------------------------
                    // ---------------------- Caso de recibir una consulta por autor ----------------------
                    // ------------------------------------------------------------------------------------
                        
                    } else if (entradaCliente[1].equals("-a")) {                        
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        boolean visited_node=false;
                        String visited = node_id;

                        for(int i=2; i<entradaCliente.length; i++){
                            cadena = cadena + entradaCliente[i] + " ";
                        }
                        cadena = cadena.toLowerCase();
                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            String cad1 = removeSpaces(cadena);
                            String cad2 = removeSpaces(cur_track.creator.toLowerCase());
                            if(cad1.equals(cad2)){
                                countstring = Integer.toString(count);
                                count++;
                                found = true;
                                answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";                                
                            }
                        }

                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split("%%");
                            String[] result_list;
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            for(int i=0; i<visit_list.length;i++){
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){                                
                                Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);                                
                                    out_a.println("c na -a "+count+" "+visited+" "+cadena);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    visited = visited+"%%"+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();
				}

                            }
                        }

                        if(found == true){
                            out.println(answer);
                        } else {
                            out.println("Fail");
                        }

                    } else if (entradaCliente[1].equals("na")) {                        
                        Iterator<String> iterador_conocidos = conocidos_list.listIterator();
                        boolean visited_node=false;
                        String visited = node_id+"%%"+entradaCliente[4];
                        count = Integer.parseInt(entradaCliente[3]);

                        for(int i=5; i<entradaCliente.length; i++){
                            cadena = cadena + entradaCliente[i] + " ";
                        }
                        cadena = cadena.toLowerCase();
                        while(iterador_tracks.hasNext()){
                            Track cur_track = (Track) iterador_tracks.next();
                            String cad1 = removeSpaces(cadena);
                            String cad2 = removeSpaces(cur_track.creator.toLowerCase());
                            if(cad1.equals(cad2)){
                                countstring = Integer.toString(count);
                                count++;
                                found = true;
                                answer = answer + countstring + "@@" + cur_track.creator + "@@" + cur_track.title + "@@" + node_id + "@@";
                            }
                        }

                        while(iterador_conocidos.hasNext()){
                            String con_act = (String) iterador_conocidos.next();
                            String[] visit_list = visited.split("%%");
                            String[] result_list;
			    if(con_act.equals("")){
                                visited_node = true;
			    } 
                            for(int i=0; i<visit_list.length;i++){
                                if(removeSpaces(con_act).equals(removeSpaces(visit_list[i]))){
                                    visited_node = true;
                                }
                            }
                            if(!visited_node){
                                    Node nodo_act = new Node(puerto_serv, con_act);
                                if(nodo_act.puerto != -1){
                                    BufferedReader in_a = new BufferedReader(new InputStreamReader(nodo_act.socket.getInputStream()));
                                    PrintWriter out_a = new PrintWriter(nodo_act.socket.getOutputStream(), true);
                                    out_a.println("c na -a "+count+" "+visited+" "+cadena);
                                    result = in_a.readLine();
                                    result_list = result.split("__");
                                    if(!result_list[0].equals("Fail")){
                                        found = true;
                                    }
                                    visited = visited+"%%"+result_list[1];
                                    answer = answer+result_list[0];
                                    nodo_act.socket.close();
				}
                            }
                        }
                        if(found == true){
                            out.println(answer+"__"+visited);
                        } else {
                            out.println("Fail__"+visited);
                        }

                    }


                // --------------------------------------------------------------------------
                // ---------------------- Caso de recibir una descarga ----------------------
                // --------------------------------------------------------------------------


                } else if(entradaCliente[0].equals("d")){
                    Iterator<Track> iterador_tracks = tracks.listIterator();
                    String[] incoming_track;
                    OutputStream out_s = clientSocket.getOutputStream();                    
                    incoming_track = clientRequest.split("@@");                    
                    while(iterador_tracks.hasNext()){
                        Track cur_track = (Track) iterador_tracks.next();
                        String aut1 = removeSpaces(incoming_track[1]);
                        String aut2 = removeSpaces(cur_track.creator.toLowerCase());
                        String tit1 = removeSpaces(incoming_track[0].substring(2));
                        String tit2 = removeSpaces(cur_track.title.toLowerCase());
                        if(aut1.equals(aut2) && tit1.equals(tit2)){
                            String song_file = cur_track.location.substring(cur_track.location.lastIndexOf("/")+1);
                            out.println(song_file);
                            File send_file = new File(cur_track.location);
                            if(send_file.exists()){
                                out.println("ok");
                                InputStream in_s = new FileInputStream(send_file);
                                byte[] byte_array = new byte[clientSocket.getSendBufferSize()];
                                int bytesRead = 0;
                                while((bytesRead = in_s.read(byte_array))>0){
                                    out_s.write(byte_array, 0, bytesRead);
                                }
                                //in_s.close();
                                out_s.flush();
                                out_s.close();
                            } else {                                
                                out.println("Fail");
                            }
                            
                        }
                    }


                }else if(entradaCliente[0].equals("o")){                    
                    clientSocket.close();
                    clientSocket = server.accept();
                    //String blah = "-p "+puerto_serv+" -i "+node_id+" -b "+xmlfile+" -c "+confile;
                    //String[] blahaux = blah.split(blah);
                    //main(blahaux);
                } else {
                    System.out.println("Formato de llamada incorrecto "+entradaCliente[0]);                    
                }

                
            } catch (IOException e){
                try{
                clientSocket = server.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                //out_s = clientSocket.getOutputStream();
                } catch(Exception ep){
                    System.out.println("Excepcion en cerradura/apertura de socket:  " + ep);
                }
            } catch(NullPointerException e){
                try{                    
                    clientSocket = server.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    //out_s = clientSocket.getOutputStream();
                } catch (IOException ei){
                    System.out.println("Excepcion en la construccion del servidor: "+ei);
                }
            }


        }


        

    }
}
