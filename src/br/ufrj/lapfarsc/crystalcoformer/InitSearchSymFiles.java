package br.ufrj.lapfarsc.crystalcoformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class InitSearchSymFiles {

	
	
	public static void main(String[] args) throws Exception {
		System.out.println("InitSearchSymFiles: ");
		if(args==null || args.length < 1) {
			System.out.println("--> Arg0 IS REQUIRED.");
			return;
		}		
		String rootPath = args[0];		
		File path = new File(rootPath);
		if(!path.exists()) {
			System.out.println("--> PATH DOES NOT EXIST: "+path.getAbsolutePath());
			return;
		}
		
		//buscar paths dos aqruivos em ate 4 niveis de pastas
		ArrayList<File> listFiles = buscarArquivosOutputPW(path, 4);
		for (File file : listFiles) {
			System.out.println(file.getPath());
		}
	}
	
	public static ArrayList<File> buscarArquivosOutputPW(File pasta, int nivel) throws Exception{
		if(nivel==0) return new ArrayList<File>();
		nivel--;
		File[] arquivos = pasta.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File arg0) {
				return arg0.isFile() && !arg0.getName().toLowerCase().endsWith(".sdf");
			}
		});
		
		File[] pastas = pasta.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}
		});
		
		ArrayList<File> listPW = new ArrayList<File>();
		
		FileReader fr = null;
	    BufferedReader br = null;
	    try{
			for (File file : arquivos) {
		    	try{
					//LER ARQUIVO
					StringBuilder conteudo = new StringBuilder();
					fr = new FileReader(file);											
					br = new BufferedReader(fr);
			        int read, N = 1024;
			        char[] buffer = new char[N];
			        
			        read = br.read(buffer, 0, N);
		            if(read !=-1) {
		            	String text = new String(buffer, 0, read);
		            	conteudo.append(text);
		            }
		            if(conteudo.toString().indexOf("Quantum ESPRESSO")!=-1) {
		            	listPW.add(file);
		            }
				}catch(Exception e){
					System.out.println(file.getAbsolutePath());
					throw e;
				}
		    }
		}finally{
			if(br!=null) br.close();
			if(fr!=null) fr.close();
		}
	    
	    ArrayList<File> listOutput = new ArrayList<File>();
	    
		for (File file : listPW) {
			String conteudo = loadTextFile(file);
			if(conteudo.indexOf(" Cartesian axes")!=-1) {
				if(conteudo.indexOf(" No symmetry found")==-1) {
					listOutput.add(file);
				}
			}
		}
	    
		for (File subpasta : pastas) {
			listOutput.addAll( buscarArquivosOutputPW(subpasta, nivel) );
		}
		return listOutput;
	}	
	
	public static String loadTextFile(File file) throws IOException {
		FileReader fr = null;
	    BufferedReader br = null;
		try{
			StringBuilder conteudo = new StringBuilder();
			fr = new FileReader(file);											
			br = new BufferedReader(fr);
	        int read, N = 1024;
	        char[] buffer = new char[N];
	        
	        //int i = 0;			        
	        while(true) {
	            read = br.read(buffer, 0, N);
	            String text = new String(buffer, 0, read);
	            conteudo.append(text);
	            if(read < N){
	            	if(conteudo.length()>0){
	            		return conteudo.toString();
	            	}
	            	break;
	            }		            
	        }
		}catch(Exception e){
			System.out.println(file.getAbsolutePath());
			throw e;
		}finally{
			if(br!=null) br.close();
			if(fr!=null) fr.close();
		}
		return null;
	}
	
}
