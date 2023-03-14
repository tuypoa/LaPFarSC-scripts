package br.lapfarsc.crystalcoformers.init;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import br.lapfarsc.crystalcoformers.dto.MoleculeUspexDTO;
import br.lapfarsc.crystalcoformers.dto.StructureUspexDTO;

public class InitUspexSummaryMolecules {

	//public static String PATH_XCRYSDEN = "/home/tuy/Documents/xcrysden-1.6.2-bin-shared/scripts";
	public static String PATH_XCRYSDEN = "/usr/share/xcrysden/scripts";
	
	public static void main(String[] args) throws Exception {
		System.out.println("InitUspexSummaryMolecules: ");
		if(args==null || args.length < 1) {
			System.err.println("--> Arg0 IS REQUIRED (search uspex path).");
			return;
		}	
		String rootPath = args[0];		
		File path = new File(rootPath);
		
		File pathINPUT = new File(rootPath+"/INPUT.txt");
		File pathMol1 = new File(rootPath+"/MOL_1");
		File pathMol2 = new File(rootPath+"/MOL_2");
		File pathCalFold = new File(rootPath+"/CalcFold1");
		
		if(!path.exists()) {
			System.err.println("--> Arg0: PATH DOES NOT EXIST: "+path.getAbsolutePath());
			return;
		}else if(!pathINPUT.exists() || !pathMol1.exists() || !pathMol2.exists() || !pathCalFold.exists()) {
			//verificar se existe:
			System.err.println("--> Arg0: USPEX calc folder not detected: "+path.getAbsolutePath());
			return;
		}
		
		System.out.println(path.getAbsolutePath());
		
		MoleculeUspexDTO mol1DTO = parseDataMoleculeUspexDTO( loadTextFile(pathMol1) );
		MoleculeUspexDTO mol2DTO = parseDataMoleculeUspexDTO( loadTextFile(pathMol2) );
		
		//criar uma pasta "LaPFarSC-data<timestamp>"
		//colocar txt com resumo dos dados, grafico, cif's ok 
		File pathNova = new File(rootPath+"/LaPFarSC-data"+Calendar.getInstance().getTimeInMillis());
		if(!pathNova.exists()) {
			pathNova.mkdir();
		}
		
		List<StructureUspexDTO> listaStruc = new ArrayList<StructureUspexDTO>();
		
		//percorrer log
		//Localizar arquivo result[0-9]*.txt com a info de SpaceGroup
		File results[] = path.listFiles(new FilenameFilter() {
								    @Override
								    public boolean accept(File dir, String name) {
								        return name.toLowerCase().startsWith("result") && name.toLowerCase().endsWith(".txt");
								    }
								});
		if(results.length == 0) {
			System.err.println("--> LOG FILE NOT FOUND: result<Date>.txt ");
			return;
		}
		File result1 = results[0];
		String log = loadTextFile(result1);
		if(log.indexOf("Read Seeds")==-1) {
			System.err.println("--> LOG FILE INCOMPLETE (result<Date>.txt) NOT FOUND: \"Read Seeds ...\" ");
			return;
		}
		String[] conteudoLog = log.split("\r\n");
		for (String line : conteudoLog) {
			if(line.startsWith("Structure ")) {
				StructureUspexDTO dto = new StructureUspexDTO();
				dto.setId( Integer.parseInt( line.substring( "Structure ".length(), line.indexOf("built") ).trim() ) );
				dto.setSymLine(line);
				dto.setSpaceGroup( Integer.parseInt( line.substring( line.indexOf("symmetry")+"symmetry".length(), line.indexOf("(",line.indexOf("symmetry")) ).trim() ) );
				dto.setRelaxDone(Boolean.FALSE);
				listaStruc.add(dto);
			}else if(line.startsWith("Read Seeds ")) {
				break;
			}	
		}
		int cifDataFindsymSTRUC = 1;
		for (StructureUspexDTO st : listaStruc) {
			int idx = log.indexOf("Structure"+st.getId()+" step1 at CalcFold1");
			int idxnext = log.indexOf("Structure"+(st.getId()+1)+" step1 at CalcFold1");
			if(idx!=-1) {
				String logStruc = log.substring(idx, idxnext==-1? log.length() : idxnext);
				if(logStruc.indexOf("is done")!=-1) {
					st.setRelaxDone(Boolean.TRUE);
					//verificar arquivo em "results1" -> symmetrized_structures.cif
					File arquivoCif = new File(path.getAbsoluteFile() + "/results1/symmetrized_structures.cif");
					if(arquivoCif.exists()) {
						String arqCif = loadTextFile(arquivoCif);
						String[] arqStruc = arqCif.split("data_findsym-STRUC-");
						
						st.setArquivoCif( "data_findsym-STRUC-"+arqStruc[cifDataFindsymSTRUC] );
						
						String atoms = st.getArquivoCif().substring( st.getArquivoCif().indexOf("_atom_site_occupancy")+"_atom_site_occupancy".length()  );
						String[] atomsLin = atoms.split("\n");
						HashMap<String, Integer> freqA = new HashMap<String, Integer>();
						int i = 0;
						for (String s : atomsLin) {
							if(s.length()>30) {
								String atom = s.substring(0,7).toUpperCase().trim();
								if(freqA.containsKey(atom)) {
									freqA.put(atom, freqA.get(atom)+1);
								}else {
									freqA.put(atom, 1);
								}
								i++;
							}
						}
						st.setCountAtoms(i);
						countRatioMolecules(st, freqA, mol1DTO.getFreqAtoms(), mol2DTO.getFreqAtoms());
					}
					File arquivoOut = new File(path.getAbsoluteFile() + "/results1/OUTPUT.txt");
					if(arquivoOut.exists()) {
						String arqOut = loadTextFile(arquivoOut);
						String listOut = arqOut.substring( arqOut.indexOf("\n", arqOut.indexOf("ID   Origin") ) );
						String[] linOut = listOut.split("\n");
						String lin = linOut[cifDataFindsymSTRUC];
						String eVVol = lin.substring( lin.indexOf("]")+1, lin.indexOf("[", lin.indexOf("]")+1) );
						do {
							eVVol = eVVol.replaceAll("  ", " ");
						}while(eVVol.indexOf("  ")!=-1);
						String[] reg = eVVol.trim().split(" ");
						st.setEnthalpy( Double.parseDouble(reg[0]) );
						st.setVolume( Double.parseDouble(reg[1]) );
					}
					//System.out.println( st.getSymLine() );
					cifDataFindsymSTRUC++;
				}else {
					// /CalcFold1/ERROR-OUTPUT-Gen1-Ind1-Step1
					st.setRelaxDone(Boolean.FALSE);
					File arquivoPwOutput = new File(pathCalFold.getAbsoluteFile() + "/ERROR-OUTPUT-Gen1-Ind"+st.getId()+"-Step1");
					if(arquivoPwOutput.exists()) {
						String arqPw = loadTextFile(arquivoPwOutput);
						if(arqPw.lastIndexOf("total energy        ")!=-1) {
							String enthalpy = arqPw.substring( arqPw.indexOf("=", arqPw.lastIndexOf("total energy        "))+1, arqPw.indexOf("Ry", arqPw.lastIndexOf("total energy        ") ) ).trim();
							st.setEnthalpy( Double.parseDouble( enthalpy ) * 13.6057039763  ); //1 Rydberg constant	13.6057039763 eV
						}
						//unit-cell volume          =   33785.3729 (a.u.)^3
						int idxVol = arqPw.indexOf("unit-cell volume");
						st.setVolume( Double.parseDouble( arqPw.substring( arqPw.indexOf("=",idxVol)+1 , arqPw.indexOf("(",idxVol) ).trim() ) );
						st.setVolume( ((double) st.getVolume()) * Math.pow(0.529177249,3) );  // 1 a.u. of length = 0.529177249 angstrom
						//number of atoms/cell      =          225
						int idxNatom = arqPw.indexOf("number of atoms/cell");
						st.setCountAtoms( Integer.parseInt( arqPw.substring( arqPw.indexOf("=",idxNatom)+1 , arqPw.indexOf("\n",idxNatom) ).replace("\r", "").trim() ) );
						//     site n.     atom 
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
						//System.out.println(st.getRatio());
					}
				}
				
				if(idxnext==-1 && st.getCountAtoms()==null) {
					// /CalcFold1/output
					File arquivoPwOutput = new File(pathCalFold.getAbsoluteFile() + "/output");
					if(arquivoPwOutput.exists()) {
						String arqPw = loadTextFile(arquivoPwOutput);
						if(arqPw.indexOf("convergence has been achieved")!=-1) {
							st.setRelaxDone(Boolean.TRUE);
						
							String enthalpy = arqPw.substring( arqPw.indexOf("=", arqPw.lastIndexOf("!    total energy"))+1, arqPw.indexOf("Ry", arqPw.lastIndexOf("!    total energy") ) ).trim();
							st.setEnthalpy( Double.parseDouble( enthalpy ) * 13.6057039763  ); //1 Rydberg constant	13.6057039763 eV
						}
						//unit-cell volume          =   33785.3729 (a.u.)^3
						int idxVol = arqPw.indexOf("unit-cell volume");
						st.setVolume( Double.parseDouble( arqPw.substring( arqPw.indexOf("=",idxVol)+1 , arqPw.indexOf("(",idxVol) ).trim() ) );
						st.setVolume( ((double) st.getVolume()) * Math.pow(0.529177249,3) );  // 1 a.u. of length = 0.529177249 angstrom
						//number of atoms/cell      =          225
						int idxNatom = arqPw.indexOf("number of atoms/cell");
						st.setCountAtoms( Integer.parseInt( arqPw.substring( arqPw.indexOf("=",idxNatom)+1 , arqPw.indexOf("\n",idxNatom) ).replace("\r", "").trim() ) );
						
						//converter Output em CIF
						st.setArquivoCif( converterOutput2CIF(arquivoPwOutput, pathNova) ); 
						//System.out.println(st.getArquivoCif());
						if(st.getArquivoCif()==null) {
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
							
						}else {
							String atoms = st.getArquivoCif().substring( st.getArquivoCif().indexOf("_atom_site_occupancy")+"_atom_site_occupancy".length()  );
							String[] atomsLin = atoms.split("\n");
							HashMap<String, Integer> freqA = new HashMap<String, Integer>();
							int i = 0;
							for (String s : atomsLin) {
								if(s.length()>30) {
									String atom = s.substring(11,18).toUpperCase().trim();
									if(freqA.containsKey(atom)) {
										freqA.put(atom, freqA.get(atom)+1);
									}else {
										freqA.put(atom, 1);
									}
									i++;
								}
							}
							st.setCountAtoms(i);
							countRatioMolecules(st, freqA, mol1DTO.getFreqAtoms(), mol2DTO.getFreqAtoms());
						}
					}
				}
			}
		}
		
		DecimalFormat df = new DecimalFormat("0.0000");
		StringBuilder dataSum = new StringBuilder();
		dataSum.append("id;spacegroup;ratio;mol1;mol2;atoms;relax;volume;enthalpy;output").append("\n");
		for (StructureUspexDTO st : listaStruc) {
			dataSum.append( st.getId() ).append(";");
			dataSum.append( st.getSpaceGroup() ).append(";");
			dataSum.append( st.getRatio() ).append(";");
			dataSum.append( st.getCountMol1() ).append(";");
			dataSum.append( st.getCountMol2() ).append(";");
			dataSum.append( st.getCountAtoms() ).append(";");
			dataSum.append( st.getRelaxDone() ).append(";");
			dataSum.append( st.getVolume()==null?"null":df.format( st.getVolume() ) ).append(";");
			dataSum.append( st.getEnthalpy()==null?"null":df.format( st.getEnthalpy() ) ).append(";");
			dataSum.append( st.getSymLine() );
			dataSum.append("\n");
			
			if(st.getArquivoCif()!=null) {
				saveTextFile( new File(pathNova+"/id"+st.getId()+"_ratio"+st.getRatio().replaceAll(":", "-")+".cif"), st.getArquivoCif());
			}
		}
		//
		saveTextFile( new File(pathNova+"/SUMMARY.txt"), dataSum.toString());
		
		//gerar grafico Seaborn Python
		/*ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","");
        Process p = builder.start();
        int exitCode = p.waitFor();
		if (exitCode != 0) {
			System.err.println(path.getPath());
			System.err.println(exitCode);
			System.err.println("----------------------------------");
		}
		*/
		System.out.println("FIM.");
	}
	
	
	public static void countRatioMolecules(StructureUspexDTO st, HashMap<String, Integer> freqA, HashMap<String, Integer> mol1, HashMap<String, Integer> mol2)throws Exception {
		if(freqA.size()>=2) {
			Set<String> set = freqA.keySet();
			String key1 = null;
			String key2 = null;
			for (String s : set) {
				if(key1==null) {
					key1 = s;
				}else if(key2==null) {
					key2 = s;
				}else {
					break;
				}
			}
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
			
			int delta = a1*b2 - a2*b1;
			int a = ( c1*b2 - c2*b1 ) / delta;
			int b = ( a1*c2 - a2*c1 ) / delta;

			st.setCountMol1(a);
			st.setCountMol2(b);
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
			if(a==0 || b==0) {
				st.setRatio(a+":"+b);
			}else {
				st.setRatio(((int)(a/x))+":"+((int)(b/x)));
			}
			//System.out.println(st.getRatio());
		}
	}
	
	public static String converterOutput2CIF(File outputPw, File path) throws Exception {
		
		String nomeFile = outputPw.getName();
		//converter PWout -> XSF 
		// ./pwo2xsf.sh -ic output > teste3.xsf
		String cmd = PATH_XCRYSDEN+"/pwo2xsf.sh -ic "+outputPw.getPath()+" > "+path.getPath()+"/"+nomeFile+".xsf";
		ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c",cmd);
        Process p = builder.start();
        int exitCode = p.waitFor();
        //System.out.println(exitCode);
        File xsf = new File(path.getPath()+"/"+nomeFile+".xsf");
		String xsfConteudo = loadTextFile(xsf);
        
        if (exitCode != 0 || xsfConteudo.length() <= 10 ) {
			System.err.println("pwo2xsf.sh ERROR--------------------");
			System.err.println(path.getPath());
			System.err.println(exitCode);
			System.err.println("----------------------------------");
			return null;
		}
		
		if(xsf.exists()) {
			//converter XSF -> CIF
			//obabel -ixsf teste2.xsf -ocif -Oteste2.cif
			cmd = "cd "+path.getPath()+" && obabel -ixsf "+nomeFile+".xsf -ocif -O"+nomeFile+".cif";
			builder = new ProcessBuilder("/bin/sh","-c",cmd);
	        p = builder.start();
            exitCode = p.waitFor();
            //System.out.println(exitCode);
			if (exitCode != 0) {
				System.err.println("obabel XSF2CIF ERROR--------------");
				System.err.println(path.getPath());
				System.err.println(exitCode);
				System.err.println("----------------------------------");
			}else {
				//editar arquivo CIF para colocar metadados
				String metadados = ""
						+ "#[LaPFarSC] generated by USPEX: \n"
						+ "#[LaPFarSC]  "+ path.getAbsolutePath() +" \n"
						+ "#[LaPFarSC] converted by xcrysden-script: pwo2xsf.sh \n"
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
				
				File of = new File(path.getPath()+"/"+nomeFile+".cif") ;
				String conteudoOK = loadTextFile(of);
				conteudoOK = conteudoOK.substring(0, conteudoOK.indexOf("_cell_")) + cifMeta +  conteudoOK.substring(conteudoOK.indexOf("_cell_"), conteudoOK.length());
				conteudoOK = metadados + conteudoOK;
				//saveTextFile(of, conteudoOK);
				of.delete();
				xsf.delete();
				
				return conteudoOK;
			}
		}
		return null;
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
