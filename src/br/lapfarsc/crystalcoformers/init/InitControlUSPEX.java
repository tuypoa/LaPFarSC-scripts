package br.lapfarsc.crystalcoformers.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import br.lapfarsc.crystalcoformers.dto.MoleculeUspexDTO;
import br.lapfarsc.crystalcoformers.dto.StructureUspexDTO;

public class InitControlUSPEX {

	public static void main(String[] args) throws Exception {
		System.out.println("InitControlUSPEX: ");
		if(args==null || args.length < 2) {
			System.err.println("--> Arg0 IS REQUIRED (search uspex path).");
			System.err.println("--> Arg1 IS REQUIRED (name CalcFold1/<QE_output_file>).");
			return;
		}	
		String rootPath = args[0];	
		String outputName = args[1];
		
		File path = new File(rootPath);
	
		File pathINPUT = new File(rootPath+"/INPUT.txt");
		File pathMol1 = new File(rootPath+"/MOL_1");
		File pathMol2 = new File(rootPath+"/MOL_2");
		File pathCalFold = new File(rootPath+"/CalcFold1");
		
		String ratio = null ;
		do {
			
			if(!path.exists()) {
				System.err.println("--> Arg0: PATH DOES NOT EXIST: "+path.getAbsolutePath());
				return;
			}else if(!pathINPUT.exists() || !pathMol1.exists() || !pathMol2.exists() || !pathCalFold.exists()) {
				//verificar se existe:
				System.err.println("--> Arg0: USPEX calc folder not detected: "+path.getAbsolutePath());
				//return;
			}
			
			//System.out.println(path.getAbsolutePath());
			
			MoleculeUspexDTO mol1DTO = parseDataMoleculeUspexDTO( loadTextFile(pathMol1) );
			MoleculeUspexDTO mol2DTO = parseDataMoleculeUspexDTO( loadTextFile(pathMol2) );
			
			StructureUspexDTO st = new StructureUspexDTO();
			st.setRelaxDone(Boolean.FALSE);
			
			// /CalcFold1/output
			File arquivoPwOutput = new File(pathCalFold.getAbsoluteFile() + "/"+outputName);
			if(arquivoPwOutput.exists()) {
				String arqPw = loadTextFile(arquivoPwOutput);
				if(arqPw.indexOf("convergence has been achieved")!=-1) {
					st.setRelaxDone(Boolean.TRUE);
				
					String enthalpy = arqPw.substring( arqPw.indexOf("=", arqPw.lastIndexOf("!    total energy"))+1, arqPw.indexOf("Ry", arqPw.lastIndexOf("!    total energy") ) ).trim();
					st.setEnthalpy( Double.parseDouble( enthalpy ) * 13.6057039763  ); //1 Rydberg constant	13.6057039763 eV
				}
				//unit-cell volume          =   33785.3729 (a.u.)^3
				int idxVol = arqPw.indexOf("unit-cell volume");
				//System.out.println(idxVol);
				if(idxVol!=-1) {
					st.setVolume( Double.parseDouble( arqPw.substring( arqPw.indexOf("=",idxVol)+1 , arqPw.indexOf("(",idxVol) ).trim() ) );
					st.setVolume( ((double) st.getVolume()) * Math.pow(0.529177249,3) );  // 1 a.u. of length = 0.529177249 angstrom
					//number of atoms/cell      =          225
					int idxNatom = arqPw.indexOf("number of atoms/cell");
					st.setCountAtoms( Integer.parseInt( arqPw.substring( arqPw.indexOf("=",idxNatom)+1 , arqPw.indexOf("\n",idxNatom) ).replace("\r", "").trim() ) );
					
					String atoms = arqPw.substring( arqPw.indexOf("     site n.     atom "), arqPw.indexOf("     number of k points"));
					String[] atomsLin = atoms.split("\n");
					HashMap<String, Integer> freqA = new HashMap<String, Integer>();
					int i = 0;
					for (String s : atomsLin) {
						if(s.indexOf("tau(")!=-1) {
							String atom = s.substring(18,25).toUpperCase().trim();
							if(freqA.containsKey(atom)) {
								freqA.put(atom, freqA.get(atom)+1);
							}else {
								freqA.put(atom, 1);
							}
							i++;
						}
					}
					if(st.getCountAtoms()!=i) {
						System.out.println("--> "+arquivoPwOutput.getAbsolutePath());
						System.out.println("--> Atoms count: "+st.getCountAtoms()+" <> "+i);
					}
					countRatioMolecules(st, freqA, mol1DTO.getFreqAtoms(), mol2DTO.getFreqAtoms());
					
					
					if(ratio == null ){
						ratio = "Ratio ["+st.getCountMol1()+" : "+st.getCountMol2() +"] ";
						System.out.println();
						System.out.print( ratio );
					}
					
					if(ratio != null ){
						System.out.print( "." );
					}
					
					
					if(st.getCountMol1() == 0 || st.getCountMol2() == 0) {
						//System.out.println("KILL ** relax="+st.getRelaxDone()+" / ratio = "+st.getRatio());
						
						String pid =  null;
						Runtime run = Runtime.getRuntime();
						Process pr = run.exec("ps aux");
						pr.waitFor();
						BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
						String line = "";
						while ((line=buf.readLine())!=null) {
							if(line.indexOf(outputName)!=-1 && line.indexOf("mpirun")!=-1) {
								//System.out.println(line);
								while(line.indexOf("  ")!=-1) {
									line = line.replaceAll("  ", " ");
								}
								pid = line.split(" ")[1];
								break;
							}
						}
				        if(pid!=null && pid.matches("[0-9]+")) {
				        	long lpid = Long.parseLong(pid)+1; 
				        	pr = run.exec("kill "+pid);
				        	pr.waitFor();
				        	pr = run.exec("kill "+lpid);
				        	pr.waitFor();
							
				        	buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
							//line = "";
							//while ((line=buf.readLine())!=null) {
					        System.out.println("kill "+pid +" / ratio = "+st.getRatio());
					        //}
					        ratio = null;
				        }
					}
				}
			}else {
				System.out.println("File not found: "+arquivoPwOutput.getName());
			}
			
			Thread.sleep(30000);
		}while ( new File(rootPath+"/still_running").exists() );
	}
	
	public static void countRatioMolecules(StructureUspexDTO st, HashMap<String, Integer> freqA, HashMap<String, Integer> mol1, HashMap<String, Integer> mol2)throws Exception {
		if(freqA.size()>=2) {
			Set<String> set = freqA.keySet();
			String key1 = null;
			String key2 = null;
			for (String s : set) {
				if(!"H".equals(s)) {
					if(key1==null) {
						key1 = s;
					}else if(key2==null) {
						key2 = s;
					}else {
						break;
					}
				}
			}
			//System.out.println(key1);
			//System.out.println(key2);
			Integer a1 = mol1.get(key1);
			a1 = (a1==null?0:a1);
			Integer b1 = mol2.get(key1);
			b1 = (b1==null?0:b1);
			Integer c1 = freqA.get(key1);
			c1 = (c1==null?0:c1);
			
			Integer a2 = mol1.get(key2);
			a2 = (a2==null?0:a2);
			Integer b2 = mol2.get(key2);
			b2 = (b2==null?0:b2);
			Integer c2 = freqA.get(key2);
			c2 = (c2==null?0:c2);
			/*System.out.println(a1);
			System.out.println(a2);
			System.out.println(b1);
			System.out.println(b2);
			*/
			int delta = a1*b2 - a2*b1;
			int a = ( c1*b2 - c2*b1 ) / delta;
			int b = ( a1*c2 - a2*c1 ) / delta;

			st.setCountMol1(a);
			st.setCountMol2(b);
			if(a==0 || b==0) {
				st.setRatio(a+":"+b);
			}else {
				double x = a;
				double y = b;
				double resto;
				do{
			        resto = x % y;
			        x = y;
			        y = resto;
			    } while(resto!=0);
				//System.out.println("-------");
				//System.out.println("ratio: "+a+" : "+b );
				//System.out.println("mdc: "+ x );
				
				st.setRatio(((int)(a/x))+":"+((int)(b/x)));
			}
			//System.out.println(st.getRatio());
		}
	}
	

	
	public static MoleculeUspexDTO parseDataMoleculeUspexDTO(String conteudo) throws Exception {
		
		String nome = conteudo.substring(conteudo.indexOf(":")+1, conteudo.indexOf("\n")).trim();
		String natom = conteudo.substring(conteudo.indexOf("Number of atoms:")+"Number of atoms:".length(), conteudo.indexOf("\n",conteudo.indexOf("Number of atoms:"))).trim();
		
		MoleculeUspexDTO dto = new MoleculeUspexDTO();
		dto.setConteudo(conteudo);
		dto.setNome(nome);
		dto.setQtdeAtoms(Integer.parseInt(natom));
		
		HashMap<String, Integer> freqA = new HashMap<String, Integer>();
		String[] lin = conteudo.split("\n");
		for (String s : lin) {
			if(s.length()>50) {
				String atom = s.substring(0,2).toUpperCase().trim();
				if(freqA.containsKey(atom)) {
					freqA.put(atom, freqA.get(atom)+1);
				}else {
					freqA.put(atom, 1);
				}
			}
		}
		dto.setFreqAtoms(freqA);
		/*
		System.out.println("-----"+nome);
		Set<String> a = freqA.keySet();
		for (String s : a) {
			System.out.println(s+" = "+ freqA.get(s) );
		}
		*/
		return dto;
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

}
