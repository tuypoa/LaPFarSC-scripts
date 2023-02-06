package br.lapfarsc.moleculardynamics.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class InitReorganizarCarMdf {

	/**
	 * Reorganiza moléculas do arquivo .car e .mdf 
	 *
	 * 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("InitReorganizarCarMdf: ");
		if(args==null || args.length < 1) {
			System.out.println("--> Arg0: PATH DOS ARQUIVOS .CAR e .MDF"); 
			return;
		}
		String arq1Path = args[0];
		File arquivos = new File(arq1Path);		
		if(!arquivos.exists()) {
			System.out.println("--> PATH NAO EXISTE: "+arquivos.getAbsolutePath());
			return;
		}
		File[] arquivosCAR = arquivos.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File arg0) {
				return arg0.getName().toLowerCase().endsWith(".car") && !arg0.getName().endsWith("-OK.car");
			}
		});
		
		int countGlobal = 0;
		try {
			for (File fileCAR : arquivosCAR) {
				String conteudoCar = loadTextFile(fileCAR);
				String[] linhas = conteudoCar.split("\r\n");
				int countMol = 0;
				HashMap<String, String> idsMDF = new HashMap<String, String>();
				StringBuilder novoCar = new StringBuilder();
				for (String linha : linhas) {
					if(linha.equals("end")) {
						countMol++;
//						if(countMol==2) {
//							//System.out.println("break;");
//							break;
//						}
					}
					
					if(countMol==0) {
						novoCar.append(linha).append("\n");
					}else{
						if(linha.length()>70) {
							String id = linha.substring(51,62).trim();
							while (id.indexOf("  ")!=-1) id = id.replaceAll("  ", " ");
							id = id.replace(" ", "_");
							id = id + ":"+ linha.substring(0,7).trim();
							idsMDF.put( id , id.replace("ND", String.format("%04d", countMol)));
							//System.out.println(id.replace("ND", String.format("%04d", countMol)));
						}						
						novoCar.append(linha.replace(" ND  ", " "+String.format("%04d", countMol) )).append("\n");
					}
				}
				String path = fileCAR.getAbsolutePath().substring(0, fileCAR.getAbsolutePath().lastIndexOf(".") );
				System.out.print(">>"+ fileCAR.getName() +" >> ");
				saveTextFile( new File( path+"-OK.car" ) , novoCar.toString());
				System.out.println("\"-OK.car\" SALVO.");
				
				File fileMDF = new File(path+".mdf");
				System.out.print(">>"+ fileMDF.getName() +" >> ");
				if(!fileMDF.exists()) {
					System.out.println("\".mdf\" NÃO ENCONTRADO.");
				}else {
					String conteudoMdf = loadTextFile(fileMDF);
					linhas = conteudoMdf.split("\r\n");
					StringBuilder novoMdf = new StringBuilder();
					for (String linha : linhas) {
						if(linha.length()>60) {
							String id = linha.substring(0,19).trim();
							if(idsMDF.containsKey(id)) {
								String ini = idsMDF.get(id);
								while (ini.length()<19) ini += " "; 
								linha = ini+ linha.substring(19,linha.length());
							}
						}
						novoMdf.append(linha).append("\n");
					}
					saveTextFile( new File( path+"-OK.mdf" ) , novoMdf.toString());
					System.out.println("\"-OK.mdf\" SALVO.");
				}
				
				countGlobal++;
				
				//break;
			}

			
		}finally {			
			System.out.println(">> Arquivos CAR lidos: "+countGlobal);
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
