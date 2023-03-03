package br.lapfarsc.crystalcoformer.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InitSearchSymFiles {
	
	public static String PWO2XSF = "pwo2xsf.sh"; //Source: $XCRYSDEN_TOPDIR/scripts/pwo2xsf.sh
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("InitSearchSymFiles: ");
		if(args==null || args.length < 1) {
			System.out.println("--> Arg0 IS REQUIRED (search path).");
			return;
		}	
		if(args.length < 2) {
			System.out.println("--> Arg1 IS REQUIRED (destination path)."); 
			return;
		}
		if(args.length < 3) {
			System.out.println("--> Arg2 IS REQUIRED (xcrysden/scripts path)."); 
			return;
		}
		String rootPath = args[0];		
		File path = new File(rootPath);
		if(!path.exists()) {
			System.out.println("--> Arg0: PATH DOES NOT EXIST: "+path.getAbsolutePath());
			return;
		}
		String dest = args[1];		
		File destPath = new File(dest);
		if(!destPath.exists()) {
			System.out.println("--> Arg1: PATH DOES NOT EXIST: "+destPath.getAbsolutePath());
			return;
		}		
		//ARG 2 /home/Documents/xcrysden-1.6.2-bin-shared/scripts
		String xcrysden = args[2];		
		File xcrysdenPath = new File(xcrysden);
		if(!xcrysdenPath.exists()) {
			System.out.println("--> Arg2: PATH DOES NOT EXIST: "+path.getAbsolutePath());
			return;
		}
		
		//buscar paths dos aqruivos em ate 10 niveis de pastas
		ArrayList<File> listFiles = buscarArquivosOutputPW(path, 10);
		
		for (File file : listFiles) {
			
			//Localizar arquivo result[0-9]*.txt com a info de SpaceGroup
			File results[] = file.getParentFile().getParentFile().listFiles(new FilenameFilter() {
									    @Override
									    public boolean accept(File dir, String name) {
									        return name.toLowerCase().startsWith("result") && name.toLowerCase().endsWith(".txt");
									    }
									});
			File result1 = results[0];
			String conteudo = loadTextFile(result1);
			String nome = file.getName();
			String index = null;
			if(nome.indexOf("-")!=-1) {
				//ERROR-OUTPUT-Gen1-Ind37-Step1
				index = nome.split("-")[3].substring(3);
			}else if(conteudo.indexOf("Local optimisation finished")==-1){
				//output
				File outs[] = file.getParentFile().listFiles(new FilenameFilter() {
				    @Override
				    public boolean accept(File dir, String name) {
				        return name.toUpperCase().startsWith("ERROR-OUTPUT-");
				    }
				});
				int i = 0;
				for (File o : outs) {
					int i1 = Integer.parseInt(o.getName().split("-")[3].substring(3));
					i = i1>i?i1:i;
				}
				index = String.valueOf(i+1);
			}
			if(index!=null) {
				//System.out.println(file.getPath());
				//System.out.println("Structure "+index+" built ");
				int idxSym = conteudo.indexOf("Structure "+index+" built ");
				
				String linhaSym = conteudo.substring(idxSym, conteudo.indexOf("\n",idxSym+1)).replaceAll("\\r", "");
				String symmetry = linhaSym.substring(linhaSym.indexOf("symmetry")+"symmetry".length(), linhaSym.indexOf("(",linhaSym.indexOf("symmetry"))).trim();
				System.out.println(linhaSym);
			}
				/*
				String nomeFile = file.getParentFile().getParentFile().getParentFile().getName()+"_"+file.getParentFile().getParentFile().getName() + "-id"+index+"-sym"+symmetry;
				//converter PWout -> XSF 
				// ./pwo2xsf.sh -ic output > teste3.xsf
				String cmd = xcrysdenPath.getPath()+"/"+PWO2XSF+" -ic "+file.getPath()+" > "+destPath.getPath()+"/"+nomeFile+".xsf";
				ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c",cmd);
		        Process p = builder.start();
	            int exitCode = p.waitFor();
	            //System.out.println(exitCode);
				if (exitCode != 0) {
					System.out.println(PWO2XSF+" ERROR--------------------");
					System.out.println(file.getPath());
					System.out.println(exitCode);
					System.out.println("----------------------------------");
				}
				
				File xsf = new File(destPath.getPath()+"/"+nomeFile+".xsf");
				if(xsf.exists()) {
					String cifFile = nome + ".cif";
					//converter XSF -> CIF
					//obabel -ixsf teste2.xsf -ocif -Oteste2.cif
					cmd = "cd "+destPath.getPath()+" && obabel -ixsf "+nomeFile+".xsf -ocif -O"+nomeFile+".cif";
					builder = new ProcessBuilder("/bin/sh","-c",cmd);
			        p = builder.start();
		            exitCode = p.waitFor();
		            //System.out.println(exitCode);
					if (exitCode != 0) {
						System.out.println("obabel XSF2CIF ERROR--------------");
						System.out.println(file.getPath());
						System.out.println(exitCode);
						System.out.println("----------------------------------");
					}else {
						
						//editar arquivo CIF para colocar metadados
						String metadados = ""
								+ "#[LaPFarSC] generated by USPEX: \n"
								+ "#[LaPFarSC]  "+ linhaSym +" \n"
								+ "#[LaPFarSC]  "+ file.getAbsolutePath() +" \n"
								+ "#[LaPFarSC] converted by xcrysden-script: "+PWO2XSF+" \n"
								+ "#[LaPFarSC] converted by command obabel \n"
								+ "";
						
						String cifMeta = "_symmetry_cell_setting           triclinic\n"
								+ "_symmetry_space_group_name_H-M   'P 1'\n"
								+ "_symmetry_Int_Tables_number      1\n"
								+ "_space_group_name_Hall           'P 1'\n"
								+ "loop_\n"
								+ "_symmetry_equiv_pos_site_id\n"
								+ "_symmetry_equiv_pos_as_xyz\n"
								+ "1 x,y,z\n";
						
						File of = new File(destPath.getPath()+"/"+nomeFile+".cif") ;
						String conteudoOK = loadTextFile(of);
						conteudoOK = conteudoOK.substring(0, conteudoOK.indexOf("_cell_")) + cifMeta +  conteudoOK.substring(conteudoOK.indexOf("_cell_"), conteudoOK.length());
						conteudoOK = metadados + conteudoOK;
						saveTextFile(of, conteudoOK);
						
						xsf.delete();
					}
				}
			}
			*/
			//System.exit(0);
		}
		
		System.out.println("FIM.");
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

	
	public static String loadTextFile(File fileName) throws Exception {
		
		try{
			Path path = Paths.get(new URI("file://"+fileName.getAbsolutePath()));
			StringBuilder sb = new StringBuilder();
			List<String> l = Files.readAllLines(path);
			for (String s : l) {
				sb.append(s).append("\r\n");	
			}
			return sb.toString();
		}catch(Exception e){
			
			System.out.println(fileName.getAbsolutePath());
			throw e;
		}
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
