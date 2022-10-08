package br.lapfarsc.virtualscreening.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InitOtimizarMMFF94 {

	
	public static void main(String[] args) throws Exception {
		System.out.println("InitOtimizarMMFF94: ");
		if(args==null || args.length < 1) {
			System.out.println("--> Arg0 IS REQUIRED.");
			return;
		}		
		String rootPath = args[0];
		String javaHome = args[1];
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
		
		String outputFolder = path.getParent()+"/"+path.getName() + "-otimizada";
		File pasta = new File(outputFolder);
		if(!pasta.exists()){
			pasta.mkdir();
		}
		
		int ix = 0;
		System.out.println("|--------------------------------------------------|");
		System.out.print(" ");
		for (File file : arquivos) {
			String inputFile = file.getPath();
			String outputFile = file.getParent() + "-otimizada/"+file.getName();
			
			String cs = null;
			File arqSaida = new File(outputFile);
			if(arqSaida.exists()) {
				cs = loadTextFile(arqSaida);
			}
			
			if(!arqSaida.exists() || cs==null || cs.length()< 10) {
				String cmd = "molconvert -3:[hyperfine][mmff94][E] mol "+inputFile+" -o "+outputFile;
				ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","export JAVA_HOME="+javaHome+" && " +" " +cmd);
		        Process p = builder.start();
		        
		        
		        if( ! p.waitFor(10, TimeUnit.MINUTES) ) {
		        	new File(outputFile).delete();
		        	outputFile = outputFile.substring(0, outputFile.lastIndexOf(".") )+"-timeout.sdf";
		        	
		        	cmd = "molconvert -3:S{fine}[mmff94]L{3}[E] mol "+inputFile+" -o "+outputFile;
					builder = new ProcessBuilder("/bin/sh","-c","export JAVA_HOME="+javaHome+" && " +" " +cmd);
			        p = builder.start();
			     	//System.out.println("Abortado.");
		        }
	            int exitCode = p.waitFor();
	            //System.out.println(exitCode);
				if (exitCode != 0) {
					System.out.println(exitCode);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = "";
					String output = "";
					int i=0;
					while((line = bufferedReader.readLine()) != null){
						output += line + "\n";
						if(i++==5) break;
					}
					System.out.println("----------------------------------");
					System.out.println(inputFile);
					System.out.println(output);
					System.out.println("----------------------------------");
				}else {
					gravarMetadados(inputFile, outputFile);
				}
			}
			ix++;
			try{
				if(ix>0 && ix % (arquivos.length / 50) == 0) {
					System.out.print("*");
				}
			}catch (ArithmeticException e) {
				// TODO: handle exception
			}
			
			
			//System.out.println(inputFile);
			//System.out.println(outputFile);
			/*if(ix == 2) {
				System.exit(0);
			}*/
		}
		System.out.println(" ");
		System.out.println("FIM.");
	}

		
	public static void gravarMetadados(String inputFile, String outputFile) throws Exception {
		String input = loadTextFile(new File(inputFile));
		String metadados = input.substring( input.indexOf("\n", input.indexOf("END"))+1 );;
		File of = new File(outputFile) ;
		String conteudoOK = loadTextFile(of )	+ metadados;
		saveTextFile(of, conteudoOK);
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
