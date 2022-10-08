package br.lapfarsc.moleculardynamics.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import br.lapfarsc.moleculardynamics.dto.IndexDTO;

public class InitAddCarga {

	/**
	 * Adiciona a carga do arquivo do hep_redocking_chimera2.pdbqt
	 * Para colocar no arquivo "hep_redocking_chimera.pdbqt" 
	 * 
	 * 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("InitAddCarga: ");
		if(args==null || args.length < 2) {
			System.out.println("--> Arg0: PATH DO MODELO SEM CARGA"); //hep_redocking_chimera.pdbqt
			System.out.println("--> Arg1: PATH DO ARQUIVO COM CARGA"); //hep_redocking_chimera2.pdbqt
			return;
		}
		String arq1Path = args[0];
		File arquivoModelo = new File(arq1Path);		
		if(!arquivoModelo.exists()) {
			System.out.println("--> PATH NAO EXISTE: "+arquivoModelo.getAbsolutePath());
			return;
		}
		String arq2Path = args[1];
		File arquivoCarga = new File(arq2Path);
		if(!arquivoCarga.exists()) {
			System.out.println("--> PATH NAO EXISTE: "+arquivoCarga.getAbsolutePath());
			return;
		}
		
		
		int countGlobal = 0;
		try {
			String conteudoCarga = loadTextFile(arquivoCarga);
			HashMap<String, Float> catalogoCargas = new HashMap<String, Float>();
			String[] linhas = conteudoCarga.split("\n");
			for (String string : linhas) {
				if(string.length() > 70 ){
					int init = "HETATM    1  C1  GU3     2     ".length();
					string = string.substring(init, string.length());
					while(string.indexOf("  ")!=-1) string = string.replaceAll("  ", " ");
					String[] colunas = string.trim().split(" ");
					
					IndexDTO idto = new IndexDTO();
					idto.setX( Float.parseFloat( colunas[0].substring(0,colunas[0].indexOf(".")+3) ) );
					idto.setY( Float.parseFloat( colunas[1].substring(0,colunas[1].indexOf(".")+3) ) );
					idto.setZ( Float.parseFloat( colunas[2].substring(0,colunas[2].indexOf(".")+3) ) );
					Float carga = Float.parseFloat( colunas[5] );				
					
					catalogoCargas.put(idto.toString(), carga);	
					
					//System.out.println(string +";"+ colunas[1].substring(0,colunas[1].indexOf(".")+3));
				}				
			}
			System.out.println(">> Qtde de Cargas Encontradas: "+catalogoCargas.size());
			
			
			String conteudoModelo = loadTextFile(arquivoModelo);
			StringBuilder novoArquivo = new StringBuilder();
			
			linhas = conteudoModelo.split("\n");
			for (String string : linhas) {
				String linhaOK = string;
				if(string.length() > 70 ){
					int init = "HETATM    1  C1  GU3     2     ".length();
					string = string.substring(init, string.length());
					while(string.indexOf("  ")!=-1) string = string.replaceAll("  ", " ");
					String[] colunas = string.trim().split(" ");					
					IndexDTO idto = new IndexDTO();
					idto.setX( Float.parseFloat( colunas[0].substring(0,colunas[0].indexOf(".")+3) ) );
					idto.setY( Float.parseFloat( colunas[1].substring(0,colunas[1].indexOf(".")+3) ) );
					idto.setZ( Float.parseFloat( colunas[2].substring(0,colunas[2].indexOf(".")+3) ) );
					
					//System.out.println(string +";"+ colunas[1].substring(0,colunas[1].indexOf(".")+3));
					//System.out.print(idto.toString());
					Float carga = catalogoCargas.get(idto.toString());
					if(carga!=null){
						//System.out.print("--> "+carga);
						//System.out.println(linhaOK);
						String novaLinha = linhaOK.substring(0, linhaOK.lastIndexOf(".")-3 );
						novaLinha += String.format("%7.3f", carga).replace(",", ".");
						novaLinha += linhaOK.substring(linhaOK.lastIndexOf(".")+4, linhaOK.length());
						//System.out.println(novaLinha);
						linhaOK = novaLinha;
						countGlobal++;
					}
					//System.out.println();
				}
				novoArquivo.append(linhaOK).append("\n");
			}
			
			String path = arquivoModelo.getPath().replaceFirst(arquivoModelo.getName(), 
					arquivoModelo.getName().substring(0, arquivoModelo.getName().lastIndexOf("."))+
					"-CARGA-OK"+
					arquivoModelo.getName().substring(arquivoModelo.getName().lastIndexOf("."), arquivoModelo.getName().length()));
			saveTextFile(new File(path), novoArquivo.toString());
			
		}finally {			
			System.out.println( ">> Qtde de Cargas REDEFINIDAS: "+ countGlobal );
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
