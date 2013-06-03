
import java.util.*;
import java.io.*;
import java.net.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.Iterator;


/**
 *
 * @author Eduardo Cortejoso y Carlos Farias
 */
public class cliente {    

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
     * Clase Node
     *
     * @param puerto: Numero del puerto a que debe conectarse
     * @param conocidos: path del archivo de texto que especificara los id de nodos conocidos por este nodo
     * @param biblioteca: path del archivo xspf que especifica la biblioteca local
     * @param id: Nombre del host o direccion ip
     * @param socket: Socket de comunicacion con el servidor
     *
     */
    public static class Node{
        public int puerto;
        public String conocidos;
        public String biblioteca;
        public String id;
        public Socket socket;

        Node(int p, String i){
            puerto = p;            
            id = i;
            try{
                System.out.println("Conectandose al servidor "+i+", por favor espere un momento...\n");
                socket = new Socket(i, p);
            } catch (IOException e){
                System.out.println("Error en conexion a "+ p +" "+i+" de socket: "+e);    
		puerto = -1;            
            }
        };

    }

    /*
     * Clase Consulta
     *
     * @param numero: Numero identificador del archivo de musica
     * @param autor: autor de la cancion
     * @param cancion: titulo de la cancion
     * @param nodo: nodo del cual se hizo consulta de la cancion
     * @param descargado: flag para saber si el archivo ha sido descargado o no
     *
     */
    public static class Consulta{
        public int numero;
        public String autor;
        public String cancion;
        public String nodo;
        public boolean descargado = false;
        public String path = "";

        Consulta(int num, String a, String c, String n){
            numero = num;
            autor= a;
            cancion = c;
            nodo = n;
        };

    }

    public static String removeSpaces(String s) {
        StringTokenizer st = new StringTokenizer(s," ",false);
        String t="";
        while (st.hasMoreElements()) t += st.nextElement();
        return t;
    }

    /**
    * main
    *
    * @param args Parametros de entrada, son: el puerto, el id del nodo asociado y (opcional) el directorio de descargas
    */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String node_id = "";
        BufferedReader in = null;
        PrintWriter out = null;
        Node nodo;
        //Process p=null;
        String[] r_final;
        String respuesta;
        List<Consulta> consultas = new ArrayList<Consulta>();        
        int puerto_serv = 16000;        
        String descfile="";
        boolean des_path_use = false;

        try{
            // Intenta recibir los parametros en caso de que hay 3 parametros (se incluye el directorio de descarga)
            if(args.length == 6){
                des_path_use = true;
                if (args[0].equals("-p")) puerto_serv = Integer.parseInt(args[1]);
                else if(args[0].equals("-d")) descfile = args[1];
                else if(args[0].equals("-n")) node_id = args[1];
                else {
                    System.out.println("Formato de llamada incorrecta");
                    System.exit(0);
                }

                if (args[2].equals("-p")) puerto_serv = Integer.parseInt(args[3]);
                else if(args[2].equals("-d")) descfile = args[3];
                else if(args[2].equals("-n")) node_id = args[3];
                else {
                    System.out.println("Formato de llamada incorrecta");
                    System.exit(0);
                }

                if (args[4].equals("-p")) puerto_serv = Integer.parseInt(args[5]);
                else if(args[4].equals("-d")) descfile = args[5];
                else if(args[4].equals("-n")) node_id = args[5];
                else {
                    System.out.println("Formato de llamada incorrecta");
                    System.exit(0);
                }
                
                if(!(descfile.charAt(descfile.length()-1) == '/')){
                    descfile = descfile + "/";
                }
            // Intenta recibir los parametros en caso de que hay 2 parametros (NO se incluye el directorio de descarga)
            } else if (args.length == 4){
                if (args[0].equals("-p")) puerto_serv = Integer.parseInt(args[1]);
                else if(args[0].equals("-n")) node_id = args[1];
                else {
                    System.out.println("Formato de llamada incorrecta");
                    System.exit(0);
                }

                if (args[2].equals("-p")) puerto_serv = Integer.parseInt(args[3]);
                else if(args[2].equals("-n")) node_id = args[3];
                else {
                    System.out.println("Formato de llamada incorrecta");
                    System.exit(0);
                }

            }


        } catch(ArrayIndexOutOfBoundsException e)  {
            System.out.println("Llamado a cliente incorrecto");
            System.exit(0);
        }

        if(args.length > 6){
            System.out.println("\n Numero excesivo de argumentos\n");
            System.exit(0);
        }        

        // Creo nodo asociado al cliente        
            nodo = new Node(puerto_serv, node_id);
            if(nodo.puerto==-1){
                System.out.println("Conexion a nodo fallido!");
                System.exit(0);
            }            

        
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));            
            String strLine;
            boolean salir;
            salir = false;            
            
            // Espero a que el usuario introduza un comando
            while (!salir) {			
                try{
                System.out.println("\nPor favor introduzca un comando: \n");
                strLine = br.readLine();                                
                String[] com_array = strLine.split(" ");

                // --------------------------------------------------------------------------
                // ---------------------- Caso de recibir una consulta ----------------------
                // --------------------------------------------------------------------------
                

                if(com_array[0].equals("C") || com_array[0].equals("c")){                    
                    in = new BufferedReader(new InputStreamReader(nodo.socket.getInputStream()));
                    out = new PrintWriter(nodo.socket.getOutputStream(), true);                    
                    String[] consulta = strLine.split(" ");
                    String sending="";

                    // ------------------------------------------------------------------------
                    // ---------------------- Caso de una consulta total ----------------------
                    // ------------------------------------------------------------------------

                    if (com_array.length == 1 ){
                        System.out.println("\n-----Consulta Total-----\n");
                        out.println("c");
                        respuesta = in.readLine();                        
                        r_final = respuesta.split("@@");
                        consultas = new ArrayList<Consulta>();
                        for(int i=0; i<r_final.length; i=i+4){
                            Consulta con = new Consulta(Integer.parseInt(r_final[i]), r_final[i+1], r_final[i+2], r_final[i+3]);
                            consultas.add(con);
                        }
                        Iterator<Consulta> iterador_consultas = consultas.listIterator();
                        System.out.println("Num Autor Cancion Nodo");
                        while(iterador_consultas.hasNext()){
                            Consulta con = (Consulta) iterador_consultas.next();
                            System.out.println(con.numero + " " + con.autor + " " + con.cancion + " " + con.nodo);                            
                         }
                    } else {

                        // ----------------------------------------------------------------------------
                        // ---------------------- Caso de una consulta por autor ----------------------
                        // ----------------------------------------------------------------------------

                        if(consulta[1].equals("-a")){
                            System.out.println("\n-----Consulta por autor-----\n");
                            sending ="c -a ";
                            for(int i=2; i<consulta.length; i++){
                                sending = sending + consulta[i] + " ";
                            }                            
                            out.println(sending);
                            respuesta = in.readLine();
                            if(respuesta.equals("Fail")){
                                System.out.println("\nNo se encontro canciones segun el criterio establecido\n");
                            } else{
                                consultas = new ArrayList<Consulta>();
                                r_final = respuesta.split("@@");
                                for(int i=0; i<r_final.length; i=i+4){
                                    Consulta con = new Consulta(Integer.parseInt(r_final[i]), r_final[i+1], r_final[i+2], r_final[i+3]);
                                    consultas.add(con);
                                }
                                Iterator<Consulta> iterador_consultas = consultas.listIterator();
                                System.out.println("Num Autor Cancion Nodo");
                                while(iterador_consultas.hasNext()){
                                    Consulta con = (Consulta) iterador_consultas.next();
                                    System.out.println(con.numero + " " + con.autor + " " + con.cancion + " " + con.nodo);                                    
                                }                                
                            }

                        // -----------------------------------------------------------------------------
                        // ---------------------- Caso de una consulta por titulo ----------------------
                        // -----------------------------------------------------------------------------

                        } else if(consulta[1].equals("-t")){
                            System.out.println("\n-----Consulta por titulo-----\n");
                            sending ="c -t ";
                            for(int i=2; i<consulta.length; i++){
                                sending = sending + consulta[i] + " ";
                            }
                            out.println(sending);
                            respuesta = in.readLine();
                            if(respuesta.equals("Fail")){
                                System.out.println("\nNo se encontro canciones segun el criterio establecido\n");
                            } else{
                                consultas = new ArrayList<Consulta>();
                                r_final = respuesta.split("@@");
                                for(int i=0; i<r_final.length; i=i+4){
                                    Consulta con = new Consulta(Integer.parseInt(r_final[i]), r_final[i+1], r_final[i+2], r_final[i+3]);
                                    consultas.add(con);
                                }
                                Iterator<Consulta> iterador_consultas = consultas.listIterator();
                                System.out.println("Num Autor Cancion Nodo");
                                while(iterador_consultas.hasNext()){
                                    Consulta con = (Consulta) iterador_consultas.next();
                                    System.out.println(con.numero + " " + con.autor + " " + con.cancion + " " + con.nodo);                                    
                                }
                            }
                        } else {
                            System.out.println("\nFormato de consulta errado\n");                            
                        }
                    }

                // ------------------------------------------------------------------
                // ---------------------- Caso de una descarga ----------------------
                // ------------------------------------------------------------------

                } else if (com_array[0].equals("D") || com_array[0].equals("d")){
                    System.out.println("\n-----Descarga-----\n");
                    String[] descarga = strLine.split(" ");
                    boolean found = false;
                    boolean check = true;
                    String send="";
                    String node_con="";
                    Consulta check_con=null;
                    in = new BufferedReader(new InputStreamReader(nodo.socket.getInputStream()));
                    out = new PrintWriter(nodo.socket.getOutputStream(), true);
                    Iterator<Consulta> iterador_consultas = consultas.listIterator();
                    if(descarga.length > 1){
                        char[] check_arg = descarga[1].toCharArray();                        
                        for(int i=0;i<check_arg.length;i++){ 
                            if(!Character.isDigit(check_arg[i])){
                                check = false;
                            }
                        }
                        if(check){
                            if(!iterador_consultas.hasNext()){
                                System.out.println("Por favor realice una consulta antes de descargar un archivo.");
                            } else {
                                while(iterador_consultas.hasNext()){
                                    Consulta con = (Consulta) iterador_consultas.next();
                                    if(con.numero == (Integer.parseInt(descarga[1]))){                                        
                                        node_con = con.nodo;
                                        found = true;
                                        send = con.cancion.toLowerCase()+"@@"+con.autor.toLowerCase();
                                        con.descargado = true;
                                        check_con=con;
                                    }
                                }
                                if(found == true){                                    
                                    if(!removeSpaces(nodo.id).equals(removeSpaces(node_con))){                                        
                                        Node node_act = new Node(puerto_serv, node_con);
	                                if(node_act.puerto != -1){
                                            BufferedReader in_s = new BufferedReader(new InputStreamReader(node_act.socket.getInputStream()));
                                            PrintWriter out_s = new PrintWriter(node_act.socket.getOutputStream(), true);
                                            out_s.println("d "+send);
                                            respuesta = in_s.readLine();
                                            String desc_file_name;
                                            if(des_path_use){
                                                desc_file_name = descfile + respuesta;
                                            } else {
                                                desc_file_name = respuesta;
                                            }
                                            check_con.path=desc_file_name;
                                            respuesta = in_s.readLine();                                        
                                            if(!removeSpaces(respuesta).equals(removeSpaces("Fail"))){                                            
                                                byte [] byte_array =  new byte[node_act.socket.getReceiveBufferSize()];
                                                FileOutputStream fos = new FileOutputStream(desc_file_name);
                                                InputStream is = node_act.socket.getInputStream();
                                                int bytesReceived = 0;
                                                while((bytesReceived = is.read(byte_array))>0){
                                                    fos.write(byte_array, 0, bytesReceived);
                                                }
                                                fos.close();
                                                is.close();
                                            } else {
                                                System.out.println("Path de archivo especificado en el xml invalido");
                                            }
					} else {
					    System.out.println("Nodo solicitado innacesible");
					}

                                    } else {
                                        out.println("d "+send);
                                        respuesta = in.readLine();
                                        String desc_file_name;
                                        if(des_path_use){
                                            desc_file_name = descfile + respuesta;
                                        } else {
                                            desc_file_name = respuesta;
                                        }
                                        check_con.path=desc_file_name;
                                        respuesta = in.readLine();                                        
                                        if(!removeSpaces(respuesta).equals(removeSpaces("Fail"))){                                            
                                            byte [] byte_array =  new byte[nodo.socket.getReceiveBufferSize()];
                                            FileOutputStream fos = new FileOutputStream(desc_file_name);
                                            InputStream is = nodo.socket.getInputStream();
                                            int bytesReceived = 0;
                                            while((bytesReceived = is.read(byte_array))>0){
                                                fos.write(byte_array, 0, bytesReceived);
                                            }
                                            fos.close();
                                            is.close();
                                            out.println("o");
                                            nodo.socket.close();
                                            nodo = new Node(puerto_serv, node_id);
                                            if(nodo.puerto==-1){
                                                System.out.println("Conexion a nodo fallido!");
                                                System.exit(0);
                                            }
                                        } else {
                                            System.out.println("Path de archivo especificado en el xml invalido");
                                        }
                                    }
                                } else {
                                    System.out.println("\nNumero de track no encontrado\n");
                                }
                            }
                        } else {
                            System.out.println("\nFormato de peticion de descarga errado, numero invalido\n");
                        }
                    } else {
                        System.out.println("\nFormato de peticion de descarga errado\n");
                    }

                // ---------------------------------------------------------------------------------------
                // ---------------------- Caso de una consulta de nodos alcanzables ----------------------
                // ---------------------------------------------------------------------------------------

                } else if (com_array[0].equals("A") || com_array[0].equals("a")){
                    System.out.println("\n-----Usuarios Conocidos-----\n");
                    in = new BufferedReader(new InputStreamReader(nodo.socket.getInputStream()));
                    out = new PrintWriter(nodo.socket.getOutputStream(), true);
                    out.println("a c");

                    respuesta = in.readLine();
                    r_final = respuesta.split(" ");
                    for(int i=0; i<r_final.length; i++){
                        System.out.println(r_final[i]);
                    }

                // ----------------------------------------------------------------------------------
                // ---------------------- Caso de una peticion de Reproduccion ----------------------
                // ----------------------------------------------------------------------------------


                } else if (com_array[0].equals("P") || com_array[0].equals("p")){

                    String[] descarga = strLine.split(" ");
                    boolean found = false;
                    boolean check = true;
                    String send="";
                    String node_con="";
                    Consulta check_con=null;
                    in = new BufferedReader(new InputStreamReader(nodo.socket.getInputStream()));
                    out = new PrintWriter(nodo.socket.getOutputStream(), true);
                    Iterator<Consulta> iterador_consultas = consultas.listIterator();
                    boolean descargado = false;
                    if(descarga.length > 1){
                        char[] check_arg = descarga[1].toCharArray();
                        for(int i=0;i<check_arg.length-1;i++){
                            if(!Character.isDigit(check_arg[i])){
                                check = false;
                            }
                        }
                        if(check){
                            if(!iterador_consultas.hasNext()){
                                System.out.println("Por favor realice una consulta antes de descargar un archivo.");
                            } else {
                                while(iterador_consultas.hasNext()){
                                    Consulta con = (Consulta) iterador_consultas.next();
                                    if(con.numero == (Integer.parseInt(descarga[1]))){
                                        node_con = con.nodo;
                                        found = true;
                                        send = con.cancion.toLowerCase()+"@@"+con.autor.toLowerCase();
                                        descargado = con.descargado;
                                        check_con=con;
                                    } 
                                }
                                if(found == true && descargado == false){
                                    if(!removeSpaces(nodo.id).equals(removeSpaces(node_con))){
                                        Node node_act = new Node(puerto_serv, node_con);
	                                if(node_act.puerto != -1){
                                            BufferedReader in_s = new BufferedReader(new InputStreamReader(node_act.socket.getInputStream()));
                                            PrintWriter out_s = new PrintWriter(node_act.socket.getOutputStream(), true);
                                            out_s.println("d "+send);
                                            respuesta = in_s.readLine();
                                            String desc_file_name;
                                            if(des_path_use){
                                                desc_file_name = descfile + respuesta;
                                            } else {
                                                desc_file_name = respuesta;
                                            }
                                            check_con.path=desc_file_name;
                                            respuesta = in_s.readLine();
                                            if(!removeSpaces(respuesta).equals(removeSpaces("Fail"))){                                            
                                                byte [] byte_array =  new byte[node_act.socket.getReceiveBufferSize()];
                                                FileOutputStream fos = new FileOutputStream(desc_file_name);
                                                InputStream is = node_act.socket.getInputStream();
                                                int bytesReceived = 0;
                                                while((bytesReceived = is.read(byte_array))>0){
                                                    fos.write(byte_array, 0, bytesReceived);
                                                }
                                                fos.close();
                                                is.close();
                                                String[] vlc_array = {"vlc", desc_file_name};
                                                Runtime.getRuntime().exec(vlc_array);
                                            } else {
                                                System.out.println("Path de archivo especificado en el xml invalido");
                                            } 
					} else {
					    System.out.println("Nodo solicitado innacesible");
					}

                                    } else {
                                        out.println("d "+send);
                                        respuesta = in.readLine();
                                        String desc_file_name;
                                        if(des_path_use){
                                            desc_file_name = descfile + respuesta;
                                        } else {
                                            desc_file_name = respuesta;
                                        }
                                        check_con.path=desc_file_name;
                                        respuesta = in.readLine();
                                        if(!removeSpaces(respuesta).equals(removeSpaces("Fail"))){                                            
                                            byte [] byte_array =  new byte[nodo.socket.getReceiveBufferSize()];
                                            FileOutputStream fos = new FileOutputStream(desc_file_name);
                                            InputStream is = nodo.socket.getInputStream();
                                            int bytesReceived = 0;
                                            while((bytesReceived = is.read(byte_array))>0){
                                                fos.write(byte_array, 0, bytesReceived);
                                            }
                                            fos.close();
                                            is.close();
                                            String[] vlc_array = {"vlc", desc_file_name};
                                            Runtime.getRuntime().exec(vlc_array);
                                            out.println("o");
                                            nodo.socket.close();
                                            nodo = new Node(puerto_serv, node_id);
                                            if(nodo.puerto==-1){
                                                System.out.println("Conexion a nodo fallido!");
                                                System.exit(0);
                                            }
                                        } else {
                                            System.out.println("Path de archivo especificado en el xml invalido");
                                        }
                                    }
                                } else if(found == true && descargado == true){
                                    String[] vlc_array = {"vlc", check_con.path};
                                    Runtime.getRuntime().exec(vlc_array);
                                }else  {
                                    System.out.println("\nNumero de track no encontrado\n");
                                }
                            }
                        } else {
                            System.out.println("\nFormato de peticion de reproduccion errado, debe especificar un numero\n");
                        }
                    } else {
                        System.out.println("\nFormato de peticion de reproduccion errado\n");
                    }
           
                
                } else if (com_array[0].equals("Q") || com_array[0].equals("q")) {
                    System.out.println("\n-----Salir del Sistema-----\n");                    
                    nodo.socket.close();
                    salir = true;
                } else {
                    System.out.println("\n-----Comando no valido-----\n");
                }
                } catch (Exception e){
                    out.println("o");
                    nodo.socket.close();
                    nodo = new Node(puerto_serv, node_id);
                    if(nodo.puerto==-1){
                        System.out.println("\nConexion a nodo fallido!\n");
                        System.exit(0);
                    }
            
                }
            }
        

        

    }





}
