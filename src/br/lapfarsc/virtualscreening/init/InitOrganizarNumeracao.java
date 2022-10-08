package br.lapfarsc.virtualscreening.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.lapfarsc.virtualscreening.dto.ArquivosDTO;

public class InitOrganizarNumeracao {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("InitOrganizarNumeracao: ");
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
		

		File[] arquivos = path.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File arg0) {
				return arg0.getName().toLowerCase().endsWith(".sdf");
			}
		});
		int countHead = 0;
		int countRenamed = 0;
		int countGlobal = 1;
		try {
			if(arquivos!=null && arquivos.length > 0) {
				List<ArquivosDTO> arqs = new ArrayList<ArquivosDTO>();
				
				System.out.println("Lendo..."); 
				
				for (File file : arquivos) {
					String nome = file.getName();
					
					ArquivosDTO a = new ArquivosDTO();
					a.setFilename(nome);
					a.setNome( nome.substring( 0,  nome.lastIndexOf("y_")+2 ) );
					String corte = nome.substring( nome.lastIndexOf("y_")+2, ( nome.indexOf("_", nome.lastIndexOf("y_")+2)!=-1? nome.indexOf("_", nome.indexOf("y_")+2) : nome.lastIndexOf(".") ) );
					try{
						a.setIndexLocal( Integer.parseInt(corte ) );
					}catch(Exception e){						
						a.setNome( a.getNome()+corte);	
					}
					a.setIndexLocalCompare( nome.substring(0, nome.indexOf("_"))+String.format("%04d", a.getIndexLocal()!=null?a.getIndexLocal():999) );
					arqs.add(a);
				}
				
				Collections.sort(arqs);
				
				for (ArquivosDTO a : arqs) {
					File fileToMove = new File( path + "/" + a.getFilename() );					
					String novoNome = a.getNome()+
								(a.getIndexLocal()!=null?String.format("%03d", a.getIndexLocal()):"")+"_"+String.format("%05d", countGlobal++ )+".sdf";
					
					
				    if( fileToMove.renameTo(new File( path + "/" + novoNome) )){
				    	countRenamed++;
				    }
				}
				
				arquivos = path.listFiles(new FileFilter() {	
					@Override
					public boolean accept(File arg0) {
						return arg0.getName().toLowerCase().endsWith(".sdf");
					}
				});
				for (File file : arquivos) {
					String conteudo = loadTextFile(file);
					//
					int indexInicial = conteudo.indexOf("\n", conteudo.indexOf("\n", conteudo.indexOf("\n")+1)+1 ) +1;					
					conteudo = conteudo.substring( indexInicial );					
					String head = file.getName().substring(0, file.getName().lastIndexOf(".")) +"\n\nStructure written by LaPFarSC\n";
					conteudo = head + conteudo;
					
					File pasta = new File(path + "/PROCESSADOS/");
					if(!pasta.exists()){
						pasta.mkdir();
					}
					saveTextFile(new File(pasta.getPath()+"/" + file.getName()), conteudo);	
					countHead++;
				}
				
			}
			
		}finally {			
			System.out.println( ">> "+ countHead+ " CABECALHO OK.");
			System.out.println( ">> "+ countRenamed + " RENOMEADOS OK.");
			System.out.println("END.");	
		}
		
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
		}finally{
			if(br!=null) br.close();
			if(fr!=null) fr.close();
		}
		return null;
	}

	public static void saveTextFile(File fileOutput, String conteudo) throws IOException {
		FileOutputStream fos = null;
		try{
			/*if(!fileOutput.exists()){
				fileOutput.mkdir();
			}*/
			fos = new FileOutputStream(fileOutput);
			fos.write(conteudo.getBytes());
			fos.flush();
		}finally{
			if(fos!=null) fos.close();
		}
	}
}
